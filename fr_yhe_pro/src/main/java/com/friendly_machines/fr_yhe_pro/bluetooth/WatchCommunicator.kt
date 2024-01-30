package com.friendly_machines.fr_yhe_pro.bluetooth

import android.bluetooth.le.ScanFilter
import android.companion.BluetoothLeDeviceFilter
import android.companion.DeviceFilter
import android.os.Binder
import android.util.Log
import com.friendly_machines.fr_yhe_api.watchprotocol.IWatchCommunication
import com.friendly_machines.fr_yhe_api.watchprotocol.IWatchCommunicator
import com.friendly_machines.fr_yhe_api.watchprotocol.IWatchListener
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchMessageDecodingException
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchPhoneCallControlAnswer
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchProfileSex
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponseAnalyzationResult
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponseType
import com.friendly_machines.fr_yhe_pro.Crc16rev
import com.friendly_machines.fr_yhe_pro.bluetooth.WatchCharacteristic.bigIndicationPortCharacteristic
import com.friendly_machines.fr_yhe_pro.bluetooth.WatchCharacteristic.indicationPortCharacteristic
import com.friendly_machines.fr_yhe_pro.bluetooth.WatchCharacteristic.writingPortCharacteristic
import com.friendly_machines.fr_yhe_pro.command.WatchANotificationPushCommand
import com.friendly_machines.fr_yhe_pro.command.WatchASetTodayWeatherCommand
import com.friendly_machines.fr_yhe_pro.command.WatchCommand
import com.friendly_machines.fr_yhe_pro.command.WatchGGetDeviceInfoCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSAddAlarmCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSGetAllAlarmsCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetTimeCommand
import com.friendly_machines.fr_yhe_pro.command.WatchWGetWatchDialInfoCommand
import com.friendly_machines.fr_yhe_pro.indication.DPhoneCallControl
import com.friendly_machines.fr_yhe_pro.indication.WatchResponseFactory
import com.polidea.rxandroidble3.RxBleConnection
import com.polidea.rxandroidble3.RxBleDevice
import com.polidea.rxandroidble3.exceptions.BleCharacteristicNotFoundException
import com.polidea.rxandroidble3.exceptions.BleConflictingNotificationAlreadySetException
import com.polidea.rxandroidble3.exceptions.BleGattException
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import java.nio.BufferUnderflowException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Calendar
import java.util.Date
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

/**
 * We only ever see the Data Channel Payload. The Preamble, Access Address, Data Channel PDU header and BLE CRC are done by RxAndroidBle internally and do not count to the MTU.
 *
 * For Bluetooth LE, the Data Channel Payload hos these parts:
 *   1. Logical Link Control and Adaption Protocol (L2CAP) Header (4 B): Length (2 B) and channel ID (2 B)
 *   2. Attribute Protocol (ATT) Header (3 B): Opcode (1 B) and attribute handle (2 B)
 *   3. The actual ATT payload (up to 244 B)
 */
class WatchCommunicator: IWatchCommunicator {
    private var bleDisposables = CompositeDisposable()
    private var mtu: Int = 23

    private var connecting: Boolean = false
    private var connection: RxBleConnection? = null

    companion object {
        val deviceFilter: DeviceFilter<*> = BluetoothLeDeviceFilter.Builder().setScanFilter(ScanFilter.Builder().setServiceUuid(WatchCharacteristic.serviceUuid).build()).build()

        fun compatibleWith(scanRecord: android.bluetooth.le.ScanRecord?): Boolean {
            if (scanRecord != null)
                return scanRecord.serviceUuids.find { it == WatchCharacteristic.serviceUuid } != null
            else
                return false
        }

        const val TAG: String = "WatchCommunicator"
    }

    private val commandQueue = PublishSubject.create<WatchCommand>().toSerialized()

    private fun setupSender() {
        // TODO .onBackpressureBuffer().flatMap(bytesAndFilter -> {}, 1/*serialized communication*/)
        bleDisposables.add(commandQueue.subscribe({
            sendAll(it)
        }, {
            Log.e(TAG, "setupSender")
        }))
    }

    private fun setupIndications(characteristicUuid: UUID, callback: (input: ByteArray) -> Unit) {
        try {
            val indicationObservable: Observable<ByteArray> = connection!!.setupIndication(characteristicUuid).flatMap { it }

            // FIXME also discoverServices ("andThen")

            val disposable = indicationObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(callback) { throwable ->
                run {
                    Log.e(TAG, "Indication error: $throwable")
                    notifyListenersOfException(throwable)
                }
            }

            bleDisposables.add(disposable)
        } catch (e: BleCharacteristicNotFoundException) {
            Log.e(TAG, "Characteristic not found: $characteristicUuid")
            notifyListenersOfException(e)
            throw e
        } catch (e: BleConflictingNotificationAlreadySetException) {
            Log.e(
                TAG, "Conflicting indication already set for characteristic: $characteristicUuid"
            )
            notifyListenersOfException(e)
            throw e
        } catch (e: BleGattException) {
            Log.e(TAG, "Gatt error: $e")/*if (e.type == BleGattOperationType.NOTIFICATION) {
                Log.e(TAG, "Notification setup error for characteristic: $characteristicUuid")
            }*/
            notifyListenersOfException(e)
            throw e
        }
    }

    private var listeners = HashSet<IWatchListener>()

    private var sendingSequenceNumber = AtomicInteger(1) // verified; our first packet after the reset packet needs to be with sendingSequenceNumber > 0

    private fun calculateCrc(code: Short, payloadLength: Short, payload: ByteArray): Short {
        val buf = ByteBuffer.allocate(2 + 2 + payload.size).order(ByteOrder.BIG_ENDIAN)
        buf.putShort(code)
        buf.order(ByteOrder.LITTLE_ENDIAN)
        buf.putShort(payloadLength)
        buf.put(payload)
        return Crc16rev.crc16(buf.array()).toShort()
    }

    //private var receivingBuffers = ConcurrentHashMap<Int, Pair<Int, ByteArrayOutputStream>>() // packet_0_serial -> (current_serial, buffer)
    private fun onIndicationReceived(buf: ByteBuffer) {
        // TODO assemble from chunks
        buf.order(ByteOrder.BIG_ENDIAN)
        val code = buf.short
        buf.order(ByteOrder.LITTLE_ENDIAN)
        val payloadLength = buf.short
        val payload = ByteArray(payloadLength.toUShort().toInt() - 4 - 2)
        buf.get(payload)
        val expectedCrc = buf.short
        val newCrc = calculateCrc(code, payloadLength, payload)
        if (expectedCrc != newCrc) {
            throw WatchMessageDecodingException("crc mismatch")
        }
        val buf = ByteBuffer.wrap(payload)
        val response = WatchResponseFactory.parse(code, buf)
        listeners.forEach {
            it.onWatchResponse(response)
            if (response is DPhoneCallControl) {
                // FIXME check if (response.answer == answer call)
                it.onWatchPhoneCallControl(WatchPhoneCallControlAnswer.Accept)
            }
        }
    }

    private fun encodeMessage(arguments: ByteArray, code: Short): ByteArray {
        var buffer = ByteBuffer.allocate(2 + 2 + arguments.size).order(ByteOrder.BIG_ENDIAN)
        buffer.putShort(code)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        buffer.putShort((2 + 2 + arguments.size + 2).toShort())
        buffer.put(arguments)
        val rawBuffer0 = buffer.array()
        val crc = Crc16rev.crc16(rawBuffer0) // FIXME
        // Note: for GetTemp crc should be 0x58, 0xdf.toByte()
        buffer = ByteBuffer.allocate(buffer.limit() + 2).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put(rawBuffer0)
        buffer.putShort(crc.toShort())
        return buffer.array()
    }

    /** Take the given client message and send it, if necessary splitting it into different bluetooth packets */
    private fun sendInternal(
        sendingSequenceNumber: Int,
        code: Short,
        arguments: ByteArray,
    ) {
        val rawPacket = encodeMessage(arguments, code)
        bleDisposables.add(connection!!.writeCharacteristic(
            writingPortCharacteristic, rawPacket
        ).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({ _: ByteArray? ->
            Log.d(
                TAG, "Write characteristic successful"
            )
        }) { throwable: Throwable ->
            Log.e(
                TAG, "Write characteristic error: $throwable"
            )
            notifyListenersOfException(throwable)
        })
    }

    private fun requestMtu(connection: RxBleConnection) {
        val disposable = connection.requestMtu(256).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({ mtu ->
            run {
                // mtu: 251
                this.mtu = mtu

                listeners.forEach {
                    it.onMtuResponse(mtu)
                }
            }
        }, { throwable ->
            run {
                Log.e(TAG, "MTU request failed: $throwable")
                notifyListenersOfException(throwable)
            }
        })
        bleDisposables.add(disposable)
    }

    private fun enqueueCommand(
        command: WatchCommand,
    ) {
        commandQueue.onNext(command)
    }

    /** Connect to bleDevice and start sending commandQueue entries as needed. Also register for notifications and call listeners as necessary. */
    override fun start(bleDevice: RxBleDevice, keyDigest: ByteArray) {
        assert(!this.connecting)
        this.connecting = true
        bleDisposables.clear()
        bleDisposables.add(
            bleDevice.establishConnection(false) // TODO timeout less than 30 s
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({ connection ->
                    run {
                        Log.d(TAG, "Connection established")
                        this.connection = connection
                        setupIndications(indicationPortCharacteristic) { input ->
                            Log.d(TAG, "indication received: ${input.contentToString()}")
                            val buf = ByteBuffer.wrap(input).order(ByteOrder.BIG_ENDIAN)
                            try {
                                onIndicationReceived(buf)
                            } catch (e: BufferUnderflowException) {
                                notifyListenersOfException(e)
                                Log.e(TAG, "Exception: $e")
                            }
                        }
                        setupIndications(bigIndicationPortCharacteristic) { input ->
                            Log.d(TAG, "big indication received: ${input.contentToString()}")
                            val buf = ByteBuffer.wrap(input).order(ByteOrder.BIG_ENDIAN)
                            try {
                                onIndicationReceived(buf)
                            } catch (e: BufferUnderflowException) {
                                notifyListenersOfException(e)
                                Log.e(TAG, "Exception: $e")
                            }
                        }
                        setupSender()
                        requestMtu(connection)
                    }
                }, { throwable ->
                    run {
                        Log.e(TAG, "Connection error: $throwable")
                        notifyListenersOfException(throwable)
                    }
                })
        )
    }

    /** Send command immediately */
    private fun sendAll(
        command: WatchCommand
    ) {
        val sendingSequenceNumber = this.sendingSequenceNumber.getAndAdd(1)
        sendInternal(
            sendingSequenceNumber,
            command.operation.code,
            command.arguments,
        )
    }

    override fun stop() {
        bleDisposables.clear()
    }

    override fun removeListener(listener: IWatchListener) {
        this.listeners.remove(listener)
    }

    override fun addListener(listener: IWatchListener): IWatchCommunication {
        this.listeners.add(listener)
        return this.binder
    }

    private fun notifyListenersOfException(exception: Throwable) {
        this.listeners.forEach {
            it.onException(exception)
        }
    }

    inner class WatchCommunicationServiceBinder : Binder(), IWatchCommunication {
        //        fun getService(): WatchCommunicationService {
//            return this@WatchCommunicationService
//        }
        override fun setProfile(height: Byte, weight: Byte, sex: WatchProfileSex, age: Byte) {
            // FIXME enqueueCommand(WatchSSetProfileCommand(height, weight, sex, age))
        }

        override fun setWeather(
            weatherType: Short, temp: Byte, maxTemp: Byte, minTemp: Byte, dummy: Byte/*0*/, month: Byte, dayOfMonth: Byte, dayOfWeekMondayBased: Byte, location: String
        ) = enqueueCommand(WatchASetTodayWeatherCommand("FIXME", "FIXME",  "FIXME", 42/*FIXME*/))

        override fun setMessage(type: com.friendly_machines.fr_yhe_api.commondata.MessageTypeMed, time: Int, title: String, content: String) = enqueueCommand(
            WatchANotificationPushCommand(type.code /* FIXME */, title, content)
        )

        override fun setMessage2(type: Byte, time: Int, title: String, content: String) = enqueueCommand(
            WatchANotificationPushCommand(type /* FIXME */, title, content)
        )

        override fun setTime() {
            val calendar = Calendar.getInstance()
            calendar.time = Date()
            val year = calendar[Calendar.YEAR].toShort()
            val month = calendar[Calendar.MONTH].toByte()
            val day = calendar[Calendar.DAY_OF_MONTH].toByte()
            val hour = calendar[Calendar.HOUR].toByte()
            val minute = calendar[Calendar.MINUTE].toByte()
            val second = calendar[Calendar.SECOND].toByte()
            val weekDay = calendar[Calendar.DAY_OF_WEEK].toByte() // FIXME shuffle so monday is 0
            enqueueCommand(
                WatchSSetTimeCommand(year, month, day, hour, minute, second, weekDay)
            )
        }

        override fun getBatteryState() {
            // FIXME
        }
        override fun getAlarm() {
            enqueueCommand(WatchSGetAllAlarmsCommand())
        }

        override fun addAlarm(
            id: Int, enabled: Boolean, hour: Byte, min: Byte, title: com.friendly_machines.fr_yhe_api.commondata.AlarmTitleMed, repeats: BooleanArray
        ) = enqueueCommand(
            WatchSAddAlarmCommand(0 /* FIXME */, hour, min, 0, 0), // FIXME
        )

        override fun editAlarm(
            id: Int, enabled: Boolean, hour: Byte, min: Byte, title: com.friendly_machines.fr_yhe_api.commondata.AlarmTitleMed, repeats: BooleanArray
        ) {
            // FIXME
        }

        override fun bindWatch(userId: Long, key: ByteArray) {
            // FIXME
        }

        override fun unbindWatch() {
            // FIXME
        }
        override fun getDeviceConfig() = enqueueCommand(WatchGGetDeviceInfoCommand())
        override fun getBpData() {
            // FIXME
        }
        override fun getSleepData(startTime: Int, endTime: Int) {
            // FIXME
        }
        override fun getRawBpData(startTime: Int, endTime: Int) {
            // FIXME
        }
        override fun getStepData() {
            // FIXME
        }
        override fun getHeatData() {
            // FIXME
        }
        override fun getWatchFace() = enqueueCommand(WatchWGetWatchDialInfoCommand())
        override fun getSportData() {
            // FIXME
        }
        override fun setStepGoal(steps: Int) {
            // FIXME
        }
        override fun addListener(that: IWatchListener): IWatchCommunication {
            this@WatchCommunicator.addListener(that)
            return this@WatchCommunicator.binder // FIXME is that right?
        }

        override fun removeListener(it: IWatchCommunication) {
            TODO("Not yet implemented") // FIXME
        }

        override fun resetSequenceNumbers() {
        }

        override fun analyzeResponse(response: WatchResponse, expectedResponseType: WatchResponseType): WatchResponseAnalyzationResult {
            // FIXME
            return WatchResponseAnalyzationResult.Err
        }

        // FIXME
        fun removeListener(that: IWatchListener) {
            return this@WatchCommunicator.removeListener(that)
        }
    }
    override val binder = WatchCommunicationServiceBinder()
}