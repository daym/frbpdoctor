package com.friendly_machines.fr_yhe_pro.bluetooth

import android.os.Binder
import android.util.Log
import com.friendly_machines.fr_yhe_api.commondata.WatchWearingArm
import com.friendly_machines.fr_yhe_api.watchprotocol.IWatchBinder
import com.friendly_machines.fr_yhe_api.watchprotocol.IWatchCommunicator
import com.friendly_machines.fr_yhe_api.watchprotocol.IWatchListener
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchMessageDecodingException
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchPhoneCallControlAnswer
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchProfileSex
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponseAnalysisResult
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponseType
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchTimePosition
import com.friendly_machines.fr_yhe_pro.Crc16
import com.friendly_machines.fr_yhe_pro.bluetooth.WatchCharacteristic.bigIndicationPortCharacteristic
import com.friendly_machines.fr_yhe_pro.bluetooth.WatchCharacteristic.indicationPortCharacteristic
import com.friendly_machines.fr_yhe_pro.bluetooth.WatchCharacteristic.writingPortCharacteristic
import com.friendly_machines.fr_yhe_pro.command.WatchANotificationPushCommand
import com.friendly_machines.fr_yhe_pro.command.WatchASetTodayWeatherCommand
import com.friendly_machines.fr_yhe_pro.command.WatchCGetFileCountCommand
import com.friendly_machines.fr_yhe_pro.command.WatchCGetFileListCommand
import com.friendly_machines.fr_yhe_pro.command.WatchCommand
import com.friendly_machines.fr_yhe_pro.command.WatchGGetDeviceInfoCommand
import com.friendly_machines.fr_yhe_pro.command.WatchGGetDeviceNameCommand
import com.friendly_machines.fr_yhe_pro.command.WatchGGetElectrodeLocationCommand
import com.friendly_machines.fr_yhe_pro.command.WatchGGetEventReminderInfoCommand
import com.friendly_machines.fr_yhe_pro.command.WatchGGetMacAddressCommand
import com.friendly_machines.fr_yhe_pro.command.WatchGGetMainThemeCommand
import com.friendly_machines.fr_yhe_pro.command.WatchGGetManualModeStatusCommand
import com.friendly_machines.fr_yhe_pro.command.WatchGGetRealBloodOxygenCommand
import com.friendly_machines.fr_yhe_pro.command.WatchGGetRealTemperatureCommand
import com.friendly_machines.fr_yhe_pro.command.WatchGGetScreenInfoCommand
import com.friendly_machines.fr_yhe_pro.command.WatchGGetScreenParametersCommand
import com.friendly_machines.fr_yhe_pro.command.WatchGGetUserConfigCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHGetBloodHistoryCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHGetSleepHistoryCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHGetSportHistoryCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHGetTemperatureHistoryCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSAddAlarmCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSGetAllAlarmsCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetDndModeCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetMainThemeCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetTimeCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetTimeLayoutCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetUserInfoCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetWatchWearingArmCommand
import com.friendly_machines.fr_yhe_pro.command.WatchWGetWatchDialInfoCommand
import com.friendly_machines.fr_yhe_pro.command.WatchWSetCurrentWatchDialCommand
import com.friendly_machines.fr_yhe_pro.indication.DCameraControl
import com.friendly_machines.fr_yhe_pro.indication.DFindMobile
import com.friendly_machines.fr_yhe_pro.indication.DMusicControl
import com.friendly_machines.fr_yhe_pro.indication.DPhoneCallControl
import com.friendly_machines.fr_yhe_pro.indication.DSos
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
class WatchCommunicator : IWatchCommunicator {
    private var bleDisposables = CompositeDisposable()
    private var mtu: Int = 23

    private var connecting: Boolean = false
    private var connection: RxBleConnection? = null

    companion object {
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
        return Crc16.crc16(buf.array()).toShort()
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
            } else if (response is DMusicControl) {
                it.onWatchMusicControl(response.control)
            } else if (response is DCameraControl) {
                it.onWatchCameraControl(response.control)
            } else if (response is DFindMobile) {
                it.onWatchFindMobilePhone()
            } else if (response is DSos) {
                it.onWatchInitiateSos()
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
        val crc = Crc16.crc16(rawBuffer0) // FIXME
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

    override fun addListener(listener: IWatchListener): IWatchBinder {
        this.listeners.add(listener)
        return this.binder
    }

    private fun notifyListenersOfException(exception: Throwable) {
        this.listeners.forEach {
            it.onException(exception)
        }
    }

    inner class WatchCommunicationServiceBinder : Binder(), IWatchBinder {
        //        fun getService(): WatchCommunicationService {
//            return this@WatchCommunicationService
//        }
        override fun setProfile(height: Int, weight: Int, sex: WatchProfileSex, age: Byte, arm: WatchWearingArm?) {
            enqueueCommand(
                WatchSSetWatchWearingArmCommand(
                    arm ?: WatchWearingArm.Left
                )
            )
            enqueueCommand(
                WatchSSetUserInfoCommand(
                    height, weight, when (sex) {
                        WatchProfileSex.Female -> 0 // FIXME test.
                        WatchProfileSex.Male -> 1
                    }, age
                )
            )
        }

        override fun setWeather(
            weatherType: Short, temp: Byte, maxTemp: Byte, minTemp: Byte, dummy: Byte/*0*/, month: Byte, dayOfMonth: Byte, dayOfWeekMondayBased: Byte, location: String
        ) = enqueueCommand(WatchASetTodayWeatherCommand("1FIXME", "2FIXME", "3FIXME", 42/*FIXME*/))

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
            val weekDay = ((calendar[Calendar.DAY_OF_WEEK] - 2) % 7).toByte() // shuffle so monday is 0
            enqueueCommand(
                WatchSSetTimeCommand(year, month, day, hour, minute, second, weekDay)
            )
        }

        override fun getBatteryState() {
            // FIXME
        }

        override fun getAlarm() = enqueueCommand(WatchSGetAllAlarmsCommand())

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

        // TODO: WatchGGetUserConfigCommand instead ??
        override fun getDeviceConfig() = enqueueCommand(WatchGGetDeviceInfoCommand())
        override fun getBpData() = enqueueCommand(WatchHGetBloodHistoryCommand())
        override fun getSleepData(startTime: Int, endTime: Int) = enqueueCommand(WatchHGetSleepHistoryCommand()) // FIXME: Add times.
        override fun getRawBpData(startTime: Int, endTime: Int) {
            // FIXME
        }

        override fun getStepData() {
            // FIXME
        }

        override fun getHeatData() = enqueueCommand(WatchHGetTemperatureHistoryCommand())
        override fun getWatchDial() = enqueueCommand(WatchWGetWatchDialInfoCommand())
        override fun selectWatchDial(id: Int) = enqueueCommand(WatchWSetCurrentWatchDialCommand(id))

        override fun getSportData() = enqueueCommand(WatchHGetSportHistoryCommand())
        override fun getFileCount() = enqueueCommand(WatchCGetFileCountCommand())
        override fun getFileList() = enqueueCommand(WatchCGetFileListCommand(1, 2)) // FIXME

        override fun setWatchWearingArm(arm: WatchWearingArm) = enqueueCommand(WatchSSetWatchWearingArmCommand(arm))
        override fun setWatchTimeLayout(watchTimePosition: WatchTimePosition, rgb565Color: UShort) = enqueueCommand(WatchSSetTimeLayoutCommand(watchTimePosition, rgb565Color))
        override fun getGDeviceInfo() {
            enqueueCommand(WatchGGetDeviceInfoCommand())
            enqueueCommand(WatchGGetDeviceNameCommand())
            enqueueCommand(WatchGGetScreenInfoCommand())
            enqueueCommand(WatchGGetElectrodeLocationCommand())
            enqueueCommand(WatchGGetEventReminderInfoCommand())
            enqueueCommand(WatchGGetMacAddressCommand())
            enqueueCommand(WatchGGetManualModeStatusCommand())
            enqueueCommand(WatchGGetRealBloodOxygenCommand())
            enqueueCommand(WatchGGetRealTemperatureCommand())
            enqueueCommand(WatchGGetScreenParametersCommand())
            enqueueCommand(WatchGGetUserConfigCommand()) // this one encompasses a lot!
        }

        override fun getMainTheme() {
            enqueueCommand(WatchGGetMainThemeCommand())
        }

        override fun setMainTheme(index: Byte) {
            enqueueCommand(WatchSSetMainThemeCommand(index))
        }

        override fun setDndSettings(mode: Byte, startTimeHour: Byte, startTimeMin: Byte, endTimeHour: Byte, endTimeMin: Byte) = enqueueCommand(WatchSSetDndModeCommand(mode, startTimeHour, startTimeMin, endTimeHour, endTimeMin))

        override fun setStepGoal(steps: Int) {
            // FIXME
        }

        override fun addListener(that: IWatchListener): IWatchBinder {
            this@WatchCommunicator.addListener(that)
            return this@WatchCommunicator.binder // FIXME is that right?
        }

        override fun removeListener(it: IWatchBinder) {
            TODO("Not yet implemented") // FIXME
        }

        override fun resetSequenceNumbers() {
        }

        override fun analyzeResponse(response: WatchResponse, expectedResponseType: WatchResponseType): WatchResponseAnalysisResult {
            // FIXME
            when (expectedResponseType) {
                WatchResponseType.SetMessage -> {
                    return if (response is WatchANotificationPushCommand.Response) {
                        if (response.dummy == 0.toByte()) {
                            WatchResponseAnalysisResult.Ok
                        } else {
                            WatchResponseAnalysisResult.Err
                        }
                    } else {
                        WatchResponseAnalysisResult.Mismatch
                    }
                }

//                WatchResponseType.ChangeAlarm -> {
//                    return if (response is WatchSAddAlarmCommand.Response) {
//                        if (response.status == 0.toByte()) {
//                            WatchResponseAnalysisResult.Ok
//                        } else {
//                            WatchResponseAnalysisResult.Err
//                        }
//                    } else {
//                        WatchResponseAnalysisResult.Mismatch
//                    }
//                }
//
//                WatchResponseType.SetStepGoal -> {
//                    return if (response is WatchSetStepGoalCommand.Response) {
//                        if (response.status == 0.toByte()) {
//                            WatchResponseAnalysisResult.Ok
//                        } else {
//                            WatchResponseAnalysisResult.Err
//                        }
//                    } else {
//                        WatchResponseAnalysisResult.Mismatch
//                    }
//                }
//
//                WatchResponseType.Unbind -> {
//                    return if (response is WatchUnbindCommand.Response) {
//                        if (response.status == 0.toByte()) {
//                            WatchResponseAnalysisResult.Ok
//                        } else {
//                            WatchResponseAnalysisResult.Err
//                        }
//                    } else {
//                        WatchResponseAnalysisResult.Mismatch
//                    }
//                }
//
//                WatchResponseType.Bind -> {
//                    return if (response is WatchBindCommand.Response) {
//                        if (response.status == 0.toByte()) {
//                            WatchResponseAnalysisResult.Ok
//                        } else {
//                            WatchResponseAnalysisResult.Err
//                        }
//                    } else {
//                        WatchResponseAnalysisResult.Mismatch
//                    }
//                }
//
                WatchResponseType.SetProfile -> {
                    return if (response is WatchSSetUserInfoCommand.Response) {
                        if (response.status == 0.toByte()) {
                            WatchResponseAnalysisResult.Ok
                        } else {
                            WatchResponseAnalysisResult.Err
                        }
                    } else {
                        WatchResponseAnalysisResult.Mismatch
                    }
                }

                WatchResponseType.GetAlarms -> {
                    return if (response is WatchSGetAllAlarmsCommand.Response) { // Err... not that great since the real data comes with a big response.
                        WatchResponseAnalysisResult.Ok
                    } else {
                        WatchResponseAnalysisResult.Mismatch
                    }
                }

                WatchResponseType.SetDndSettings -> { // dummy
                    return if (response is WatchSSetDndModeCommand.Response) {
                        WatchResponseAnalysisResult.Ok
                    } else {
                        WatchResponseAnalysisResult.Mismatch
                    }
                }

                WatchResponseType.SetWatchWearingArm -> { // dummy
                    return if (response is WatchSSetWatchWearingArmCommand.Response) {
                        WatchResponseAnalysisResult.Ok
                    } else {
                        WatchResponseAnalysisResult.Mismatch
                    }
                }

                WatchResponseType.ChangeWatchDial -> {
                    return if (response is WatchWSetCurrentWatchDialCommand.Response) {
                        WatchResponseAnalysisResult.Ok
                    } else {
                        WatchResponseAnalysisResult.Mismatch
                    }

                }

                WatchResponseType.GetWatchDials -> {
                    return if (response is WatchWGetWatchDialInfoCommand.Response) {
                        WatchResponseAnalysisResult.Ok
                    } else {
                        WatchResponseAnalysisResult.Mismatch
                    }
                }

                else -> {
                    TODO("Not implemented")
                    return WatchResponseAnalysisResult.Err
                }
            }
        }

        // FIXME
        fun removeListener(that: IWatchListener) {
            return this@WatchCommunicator.removeListener(that)
        }
    }

    override val binder = WatchCommunicationServiceBinder()
}