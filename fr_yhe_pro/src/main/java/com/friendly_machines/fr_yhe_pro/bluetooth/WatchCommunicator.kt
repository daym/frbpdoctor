package com.friendly_machines.fr_yhe_pro.bluetooth

import android.os.Binder
import android.util.Log
import com.friendly_machines.fr_yhe_api.commondata.DayOfWeekPattern
import com.friendly_machines.fr_yhe_api.commondata.PushMessageType
import com.friendly_machines.fr_yhe_api.commondata.SkinColor
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
import com.friendly_machines.fr_yhe_pro.command.WatchAGetRealData
import com.friendly_machines.fr_yhe_pro.command.WatchANotificationPushCommand
import com.friendly_machines.fr_yhe_pro.command.WatchAPushMessageCommand
import com.friendly_machines.fr_yhe_pro.command.WatchASetSportModeCommand
import com.friendly_machines.fr_yhe_pro.command.WatchASetTodayWeatherCommand
import com.friendly_machines.fr_yhe_pro.command.WatchCGetFileCountCommand
import com.friendly_machines.fr_yhe_pro.command.WatchCGetFileListCommand
import com.friendly_machines.fr_yhe_pro.command.WatchCommand
import com.friendly_machines.fr_yhe_pro.command.WatchDAlarmCommand
import com.friendly_machines.fr_yhe_pro.command.WatchDCameraControlCommand
import com.friendly_machines.fr_yhe_pro.command.WatchDConnectOrDisconnectCommand
import com.friendly_machines.fr_yhe_pro.command.WatchDDynamicCodeCommand
import com.friendly_machines.fr_yhe_pro.command.WatchDEndEcgCommand
import com.friendly_machines.fr_yhe_pro.command.WatchDFindMobileCommand
import com.friendly_machines.fr_yhe_pro.command.WatchDInflatedBloodMeasurementResultCommand
import com.friendly_machines.fr_yhe_pro.command.WatchDLostReminderCommand
import com.friendly_machines.fr_yhe_pro.command.WatchDMeasurementResultCommand
import com.friendly_machines.fr_yhe_pro.command.WatchDMeasurementStatusAndResultCommand
import com.friendly_machines.fr_yhe_pro.command.WatchDMusicControlCommand
import com.friendly_machines.fr_yhe_pro.command.WatchDPhoneCallControlCommand
import com.friendly_machines.fr_yhe_pro.command.WatchDPpiDataCommand
import com.friendly_machines.fr_yhe_pro.command.WatchDRegularReminderCommand
import com.friendly_machines.fr_yhe_pro.command.WatchDSleepReminderCommand
import com.friendly_machines.fr_yhe_pro.command.WatchDSosCommand
import com.friendly_machines.fr_yhe_pro.command.WatchDSportModeCommand
import com.friendly_machines.fr_yhe_pro.command.WatchDSportModeControlCommand
import com.friendly_machines.fr_yhe_pro.command.WatchDSwitchDialCommand
import com.friendly_machines.fr_yhe_pro.command.WatchDSyncContactsCommand
import com.friendly_machines.fr_yhe_pro.command.WatchDUpgradeResultCommand
import com.friendly_machines.fr_yhe_pro.command.WatchGGetChipSchemeCommand
import com.friendly_machines.fr_yhe_pro.command.WatchGGetDeviceInfoCommand
import com.friendly_machines.fr_yhe_pro.command.WatchGGetMainThemeCommand
import com.friendly_machines.fr_yhe_pro.command.WatchGGetUserConfigCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHDeleteAllHistoryCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHDeleteBloodHistoryCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHDeleteBloodOxygenHistoryCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHDeleteComprehensiveHistoryCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHDeleteHeartHistoryCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHDeleteSleepHistoryCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHDeleteSportHistoryCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHDeleteSportModeHistoryCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHDeleteTemperatureHistoryCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHGetAllHistoryCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHGetBloodHistoryCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHGetBloodOxygenHistoryCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHGetComprehensiveHistoryCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHGetHeartHistoryCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHGetSleepHistoryCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHGetSportHistoryCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHGetTemperatureHistoryCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHHistorySportModeCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSAddAlarmCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSDeleteAlarmCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSFindPhoneCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetAntiLossCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSGetAllAlarmsCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSModifyAlarmCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetAccidentMonitoringCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetDndModeCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetHeartAlarmCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetHeartMonitorCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetLanguageCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetLongSittingCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetMainThemeCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetRegularReminderCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetScheduleSwitchCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetScreenLitTimeCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetSkinColorCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetSleepReminderCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetTemperatureAlarmCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetTemperatureMonitorCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetTimeCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetTimeLayoutCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetUnitCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetUserInfoCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetWatchWearingArmCommand
import com.friendly_machines.fr_yhe_pro.command.WatchWControlDownloadCommand
import com.friendly_machines.fr_yhe_pro.command.WatchWDeleteWatchDialCommand
import com.friendly_machines.fr_yhe_pro.command.WatchWGetWatchDialInfoCommand
import com.friendly_machines.fr_yhe_pro.command.WatchWNextDownloadChunkCommand
import com.friendly_machines.fr_yhe_pro.command.WatchWNextDownloadChunkMetaCommand
import com.friendly_machines.fr_yhe_pro.command.WatchWSetCurrentWatchDialCommand
import com.friendly_machines.fr_yhe_pro.indication.DAlarm
import com.friendly_machines.fr_yhe_pro.indication.DCameraControl
import com.friendly_machines.fr_yhe_pro.indication.DConnectOrDisconnect
import com.friendly_machines.fr_yhe_pro.indication.DDynamicCode
import com.friendly_machines.fr_yhe_pro.indication.DEndEcg
import com.friendly_machines.fr_yhe_pro.indication.DFindMobile
import com.friendly_machines.fr_yhe_pro.indication.DInflatedBloodMeasurementResult
import com.friendly_machines.fr_yhe_pro.indication.DLostReminder
import com.friendly_machines.fr_yhe_pro.indication.DMeasurementResult
import com.friendly_machines.fr_yhe_pro.indication.DMeasurementStatusAndResult
import com.friendly_machines.fr_yhe_pro.indication.DMusicControl
import com.friendly_machines.fr_yhe_pro.indication.DPhoneCallControl
import com.friendly_machines.fr_yhe_pro.indication.DPpiData
import com.friendly_machines.fr_yhe_pro.indication.DRegularReminder
import com.friendly_machines.fr_yhe_pro.indication.DSleepReminder
import com.friendly_machines.fr_yhe_pro.indication.DSos
import com.friendly_machines.fr_yhe_pro.indication.DSportMode
import com.friendly_machines.fr_yhe_pro.indication.DSportModeControl
import com.friendly_machines.fr_yhe_pro.indication.DSwitchDial
import com.friendly_machines.fr_yhe_pro.indication.DSyncContacts
import com.friendly_machines.fr_yhe_pro.indication.DUpgradeResult
import com.friendly_machines.fr_yhe_pro.indication.WatchResponseFactory
import com.polidea.rxandroidble3.RxBleConnection
import com.polidea.rxandroidble3.RxBleDevice
import com.polidea.rxandroidble3.exceptions.BleCharacteristicNotFoundException
import com.polidea.rxandroidble3.exceptions.BleConflictingNotificationAlreadySetException
import com.polidea.rxandroidble3.exceptions.BleDisconnectedException
import com.polidea.rxandroidble3.exceptions.BleGattException
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import java.nio.BufferUnderflowException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.time.ZonedDateTime
import java.util.UUID
import java.util.concurrent.TimeUnit
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
        val handled: Boolean = listeners.map {
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
                it.onWatchHeartAlarm()
            } else if (response is DRegularReminder) {
                // TODO: handle message text
                it.onWatchRegularReminder()
            } else if (response is DSleepReminder) {
                it.onWatchSleepReminder()
            } else if (response is DAlarm) {
                it.onWatchAlarm()
            } else if (response is DConnectOrDisconnect) {
                it.onWatchConnectOrDisconnect()
            } else if (response is DDynamicCode) {
                it.onWatchDynamicCode()
            } else if (response is DEndEcg) {
                it.onWatchEndEcg()
            } else if (response is DInflatedBloodMeasurementResult) {
                it.onWatchInflatedBloodMeasurementResult()
            } else if (response is DLostReminder) {
                it.onWatchLostReminder()
            } else if (response is DMeasurementResult) {
                it.onWatchMeasurementResult()
            } else if (response is DMeasurementStatusAndResult) {
                it.onWatchMeasurementStatusAndResult()
            } else if (response is DPpiData) {
                it.onWatchPpiData()
            } else if (response is DSportMode) {
                it.onWatchSportMode()
            } else if (response is DSportModeControl) {
                it.onWatchSportModeControl()
            } else if (response is DSwitchDial) {
                it.onWatchSwitchDial()
            } else if (response is DSyncContacts) {
                it.onWatchSyncContacts()
            } else if (response is DUpgradeResult) {
                it.onWatchUpgradeResult()
            } else {
                return
            }
        }.any { x -> x }
        enqueueCommand(
            if (response is DPhoneCallControl) {
                WatchDPhoneCallControlCommand(handled)
            } else if (response is DMusicControl) {
                WatchDMusicControlCommand(handled)
            } else if (response is DCameraControl) {
                WatchDCameraControlCommand(handled)
            } else if (response is DFindMobile) {
                WatchDFindMobileCommand(handled)
            } else if (response is DSos) {
                WatchDSosCommand(handled)
            } else if (response is DRegularReminder) {
                WatchDRegularReminderCommand(handled)
            } else if (response is DSleepReminder) {
                WatchDSleepReminderCommand(handled)
            } else if (response is DAlarm) {
                WatchDAlarmCommand(handled)
            } else if (response is DConnectOrDisconnect) {
                WatchDConnectOrDisconnectCommand(handled)
            } else if (response is DDynamicCode) {
                WatchDDynamicCodeCommand(handled)
            } else if (response is DEndEcg) {
                WatchDEndEcgCommand(handled)
            } else if (response is DInflatedBloodMeasurementResult) {
                WatchDInflatedBloodMeasurementResultCommand(handled)
            } else if (response is DLostReminder) {
                WatchDLostReminderCommand(handled)
            } else if (response is DMeasurementResult) {
                WatchDMeasurementResultCommand(handled)
            } else if (response is DMeasurementStatusAndResult) {
                WatchDMeasurementStatusAndResultCommand(handled)
            } else if (response is DPpiData) {
                WatchDPpiDataCommand(handled)
            } else if (response is DSportMode) {
                WatchDSportModeCommand(handled)
            } else if (response is DSportModeControl) {
                WatchDSportModeControlCommand(handled)
            } else if (response is DSwitchDial) {
                WatchDSwitchDialCommand(handled)
            } else if (response is DSyncContacts) {
                WatchDSyncContactsCommand(handled)
            } else if (response is DUpgradeResult) {
                WatchDUpgradeResultCommand(handled)
            } else {
                return
            }
        )
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
        bleDisposables.add(
            connection!!.writeCharacteristic(
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
                .retryWhen { errors ->
                    errors.flatMap { error ->
                        if (error is BleDisconnectedException) {
                            Observable.timer(1, TimeUnit.SECONDS) // TODO: check
                        } else {
                            Observable.error(error)
                        }
                    }
                }
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({ connection ->
                    run {
                        Log.d(TAG, "Connection established")
                        this.connecting = false
                        this.connection = connection
                        //listeners.forEach { it.onConnected() }
                        setupIndications(indicationPortCharacteristic) { input ->
                            Log.d(TAG, "indication received: ${input.contentToString()}")
                            val buf = ByteBuffer.wrap(input).order(ByteOrder.BIG_ENDIAN)
                            try {
                                onIndicationReceived(buf)
                            } catch (e: BufferUnderflowException) {
                                notifyListenersOfException(e)
                                Log.e(TAG, "Exception: $e")
                            } catch (e: WatchMessageDecodingException) {
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
                            } catch (e: WatchMessageDecodingException) {
                                notifyListenersOfException(e)
                                Log.e(TAG, "Exception: $e")
                            }
                        }
                        setupSender()
                        requestMtu(connection)
                    }
                }, { throwable ->
                    run {
                        this.connecting = false
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

        override fun setUserSkinColor(skinColor: SkinColor) = enqueueCommand(WatchSSetSkinColorCommand(skinColor))
        override fun setUserSleep(startHour: Byte, startMinute: Byte, repeats: UByte) = enqueueCommand(WatchSSetSleepReminderCommand(startHour, startMinute, repeats))

        override fun setScheduleEnabled(enabled: Boolean) = enqueueCommand(WatchSSetScheduleSwitchCommand(enabled))

        override fun setWeather(
            weatherType: Int, temp: Byte, maxTemp: Byte, minTemp: Byte, dummy: Byte/*0*/, month: Byte, dayOfMonth: Byte, dayOfWeekMondayBased: Byte, location: String
        ) = enqueueCommand(WatchASetTodayWeatherCommand("1FIXME", "2FIXME", "3FIXME", WatchASetTodayWeatherCommand.WeatherCode.Cloudy /* FIXME weatherType.toShort()*//*FIXME*/))

        override fun setMessage(type: com.friendly_machines.fr_yhe_api.commondata.MessageTypeMed, time: Int, title: String, content: String) = enqueueCommand(
            WatchANotificationPushCommand(type.code /* FIXME */, title, content)
        )

        override fun setMessage2(type: Byte, time: Int, title: String, content: String) = enqueueCommand(
            WatchANotificationPushCommand(type /* FIXME */, title, content)
        )

        override fun pushMessage(pushMessageType: PushMessageType, message: String) = enqueueCommand(
            WatchAPushMessageCommand(pushMessageType, message)
        )

        override fun setTime() {
            val dateTime = ZonedDateTime.now()
            val year = dateTime.year.toShort()
            val month = dateTime.month.value.toByte()
            val day = dateTime.dayOfMonth.toByte()
            val hour = dateTime.hour.toByte()
            val minute = dateTime.minute.toByte()
            val second = dateTime.second.toByte()
            val weekDay = (dateTime.dayOfWeek.value - 1).toByte() // shuffle so monday is 0
            enqueueCommand(
                WatchSSetTimeCommand(year, month, day, hour, minute, second, weekDay)
            )
        }

        override fun getBatteryState() {
            // FIXME
        }

        override fun getAlarm() = enqueueCommand(WatchSGetAllAlarmsCommand())

        override fun addAlarm(
            alarmId: Byte, hour: Byte, minute: Byte, weekPattern: Byte, enabled: Boolean
        ) = enqueueCommand(
            WatchSAddAlarmCommand(alarmId, hour, minute, weekPattern, enabled)
        )

        override fun editAlarm(
            id: Int, oldHour: Byte, oldMinute: Byte, enabled: Boolean, newHour: Byte, newMinute: Byte, weekPattern: BooleanArray
        ) = enqueueCommand(
            WatchSModifyAlarmCommand(
                oldHour, oldMinute, enabled, newHour, newMinute, 0 /* FIXME weekPattern*/
            )
        )

        override fun deleteAlarm(hour: Byte, minute: Byte) = enqueueCommand(WatchSDeleteAlarmCommand(hour, minute))

        override fun bindWatch(userId: Long, key: ByteArray) {
            // FIXME
        }

        override fun unbindWatch() {
            // FIXME
        }

        // TODO: WatchGGetUserConfigCommand instead ??
        override fun getDeviceConfig() = enqueueCommand(WatchGGetDeviceInfoCommand())
        override fun getBpData() = enqueueCommand(WatchHGetBloodHistoryCommand()) // WatchHGetComprehensiveMeasurementDataCommand() // WatchHGetBloodHistoryCommand()
        override fun getSleepData(startTime: Int, endTime: Int) = enqueueCommand(WatchHGetSleepHistoryCommand()) // FIXME: Add times.
        override fun getRawBpData(startTime: Int, endTime: Int) {
            // FIXME
        }

        override fun getStepData() {
            // FIXME
        }

        override fun getHeatData() = enqueueCommand(WatchHGetTemperatureHistoryCommand())
        override fun getWatchDial() = enqueueCommand(WatchWGetWatchDialInfoCommand())
        override fun startWatchFaceDownload(length: UInt, dialPlateId: Int, blockNumber: Short, version: Short, crc: UShort) = enqueueCommand(WatchWControlDownloadCommand.start(length = length, dialPlateId = dialPlateId, blockNumber = blockNumber, version = version, crc = crc))
        override fun sendWatchFaceDownloadChunk(chunk: ByteArray) = enqueueCommand(WatchWNextDownloadChunkCommand(chunk))
        override fun nextWatchFaceDownloadChunkMeta(deltaOffset: Int, packetCount: UShort, crc: UShort) = enqueueCommand(WatchWNextDownloadChunkMetaCommand(deltaOffset, packetCount, crc))
        override fun stopWatchFaceDownload(length: UInt) = enqueueCommand(WatchWControlDownloadCommand.stop(length))

        override fun selectWatchFace(id: Int) = enqueueCommand(WatchWSetCurrentWatchDialCommand(id))
        override fun deleteWatchDial(id: Int) = enqueueCommand(WatchWDeleteWatchDialCommand(id))

        override fun getSportData() = enqueueCommand(WatchHGetSportHistoryCommand())
        override fun getFileCount() = enqueueCommand(WatchCGetFileCountCommand())
        override fun getFileList() = enqueueCommand(WatchCGetFileListCommand(1, 2)) // FIXME

        override fun setWatchWearingArm(arm: WatchWearingArm) = enqueueCommand(WatchSSetWatchWearingArmCommand(arm))
        override fun setWatchTimeLayout(watchTimePosition: WatchTimePosition, rgb565Color: UShort) = enqueueCommand(WatchSSetTimeLayoutCommand(watchTimePosition, rgb565Color))
        override fun getGDeviceInfo() {
            // FIXME: Re-enable
//            enqueueCommand(WatchGGetDeviceInfoCommand())
//            enqueueCommand(WatchGGetDeviceNameCommand())
//            enqueueCommand(WatchGGetScreenInfoCommand())
//            enqueueCommand(WatchGGetElectrodeLocationCommand())
//            enqueueCommand(WatchGGetEventReminderInfoCommand())
//            enqueueCommand(WatchGGetMacAddressCommand())
//            enqueueCommand(WatchGGetManualModeStatusCommand())
//            enqueueCommand(WatchGGetRealBloodOxygenCommand())
//            enqueueCommand(WatchGGetRealTemperatureCommand())
//            enqueueCommand(WatchGGetScreenParametersCommand())
            enqueueCommand(WatchGGetUserConfigCommand()) // this one encompasses a lot!
        }

        override fun getMainTheme() {
            enqueueCommand(WatchGGetMainThemeCommand())
        }

        override fun setMainTheme(index: Byte) {
            enqueueCommand(WatchSSetMainThemeCommand(index))
        }

        override fun setLanguage(language: Byte) {
            enqueueCommand(WatchSSetLanguageCommand(language))
        }

        override fun setAntiLoss(enabled: Boolean) {
            enqueueCommand(WatchSSetAntiLossCommand(if (enabled) 2 else 0))
        }

        override fun setDndSettings(mode: Byte, startTimeHour: Byte, startTimeMin: Byte, endTimeHour: Byte, endTimeMin: Byte) = enqueueCommand(WatchSSetDndModeCommand(mode, startTimeHour, startTimeMin, endTimeHour, endTimeMin))

        override fun setRegularReminder(startHour: Byte, startMinute: Byte, endHour: Byte, endMinute: Byte, dayOfWeekPattern: Set<DayOfWeekPattern>, intervalInMinutes: Byte, message: String?) = enqueueCommand(WatchSSetRegularReminderCommand(1, startHour, startMinute, endHour, endMinute, dayOfWeekPattern, intervalInMinutes, message))
        override fun setHeartMonitoring(enabled: Boolean, interval: Byte, maxValue: UByte) {
            enqueueCommand((WatchSSetHeartMonitorCommand(1, if (enabled) interval else 0)))
        }

        override fun setHeartAlarm(enabled: Boolean, minValue: Byte, maxValue: UByte) {
            enqueueCommand((WatchSSetHeartAlarmCommand(if (enabled) 1 else 0, minValue, maxValue.toByte())))
        }

        override fun setUnits(distance: Byte, weight: Byte, temperature: Byte, timeFormat: Byte, bloodSugarUnit: Byte, uricAcidUnit: Byte) {
            enqueueCommand(WatchSSetUnitCommand(distance, weight, temperature, timeFormat, bloodSugarUnit, uricAcidUnit))
        }

        override fun setAccidentMonitoringEnabled(enabled: Boolean) = enqueueCommand(WatchSSetAccidentMonitoringCommand(enabled))
        override fun setTemperatureMonitoring(enabled: Boolean, interval: Byte, maxValue: UByte) {
            enqueueCommand((WatchSSetTemperatureAlarmCommand(enabled, maxValue)))
            // FIXME this is not safe.
            enqueueCommand((WatchSSetTemperatureMonitorCommand(1, interval)))
        }

        override fun setLongSitting(startHour1: Byte, startMinute1: Byte, endHour1: Byte, endMinute1: Byte, startHour2: Byte, startMinute2: Byte, endHour2: Byte, endMinute2: Byte, repeats: UByte, interval: Byte) = enqueueCommand(WatchSSetLongSittingCommand(startHour1, startMinute1, endHour1, endMinute1, startHour2, startMinute2, endHour2, endMinute2, repeats, interval))
        override fun setScreenTimeLit(screenTimeLit: Byte) = enqueueCommand(WatchSSetScreenLitTimeCommand(screenTimeLit))
        override fun getChipScheme() = enqueueCommand(WatchGGetChipSchemeCommand())
        override fun setSportMode(sportState: com.friendly_machines.fr_yhe_api.commondata.SportState, sportType: com.friendly_machines.fr_yhe_api.commondata.SportType) = enqueueCommand(WatchASetSportModeCommand(sportState, sportType))
        override fun getRealData(sensorType: com.friendly_machines.fr_yhe_api.commondata.RealDataSensorType, measureType: com.friendly_machines.fr_yhe_api.commondata.RealDataMeasureType, duration: Byte) = enqueueCommand(WatchAGetRealData(sensorType, measureType, duration))

        override fun setStepGoal(steps: Int) {
            // FIXME
        }

        override fun addListener(that: IWatchListener): IWatchBinder {
            this@WatchCommunicator.addListener(that)
            return this@WatchCommunicator.binder // FIXME is that right?
        }

        override fun removeListener(listener: IWatchListener) {
            listeners.remove(listener)
        }

        override fun resetSequenceNumbers() {
        }

        override fun analyzeResponse(response: WatchResponse, expectedResponseType: WatchResponseType): WatchResponseAnalysisResult {
            // FIXME
            when (expectedResponseType) {
                WatchResponseType.SetMessage -> {
                    return if (response is WatchANotificationPushCommand.Response) {
                        if (response.status == 0.toByte()) {
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

                WatchResponseType.SetSkinColor -> {
                    return if (response is WatchSSetSkinColorCommand.Response) {
                        WatchResponseAnalysisResult.Ok
                    } else {
                        WatchResponseAnalysisResult.Mismatch
                    }
                }

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

                WatchResponseType.SetRegularReminder -> { // dummy
                    return if (response is WatchSSetRegularReminderCommand.Response) {
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

                WatchResponseType.SetWatchScheduleEnabled -> { // dummy
                    return if (response is WatchSSetScheduleSwitchCommand.Response) {
                        WatchResponseAnalysisResult.Ok
                    } else {
                        WatchResponseAnalysisResult.Mismatch
                    }
                }

                WatchResponseType.SetWeather -> {
                    return if (response is WatchASetTodayWeatherCommand.Response) { // dummy
                        WatchResponseAnalysisResult.Ok
                    } else {
                        WatchResponseAnalysisResult.Mismatch
                    }
                }

                WatchResponseType.SetHeartMonitoring -> {
                    return if (response is WatchSSetHeartMonitorCommand.Response) { // FIXME what about SSetHeartAlarmCommand ?
                        WatchResponseAnalysisResult.Ok
                    } else {
                        WatchResponseAnalysisResult.Mismatch
                    }
                }

                WatchResponseType.SetHeartAlarm -> {
                    return if (response is WatchSSetHeartAlarmCommand.Response) {
                        WatchResponseAnalysisResult.Ok
                    } else {
                        WatchResponseAnalysisResult.Mismatch
                    }
                }

                WatchResponseType.SetUnits -> {
                    return if (response is WatchSSetUnitCommand.Response) {
                        WatchResponseAnalysisResult.Ok
                    } else {
                        WatchResponseAnalysisResult.Mismatch
                    }
                }

                WatchResponseType.SetLanguage -> {
                    return if (response is WatchSSetLanguageCommand.Response) {
                        WatchResponseAnalysisResult.Ok
                    } else {
                        WatchResponseAnalysisResult.Mismatch
                    }
                }

                WatchResponseType.SetAntiLoss -> {
                    return if (response is WatchSSetAntiLossCommand.Response) {
                        WatchResponseAnalysisResult.Ok
                    } else {
                        WatchResponseAnalysisResult.Mismatch
                    }
                }

                WatchResponseType.SetTemperatureMonitoring -> {
                    return if (response is WatchSSetTemperatureMonitorCommand.Response) { // FIXME what about SSetTemperatureAlarmCommand ?
                        WatchResponseAnalysisResult.Ok
                    } else {
                        WatchResponseAnalysisResult.Mismatch
                    }
                }

                WatchResponseType.SetAccidentMonitoringEnabled -> {
                    return if (response is WatchSSetAccidentMonitoringCommand.Response) {
                        WatchResponseAnalysisResult.Ok
                    } else {
                        WatchResponseAnalysisResult.Mismatch
                    }
                }

                WatchResponseType.SetSportMode -> {
                    return if (response is WatchASetSportModeCommand.Response) {
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


        // Delete history methods for sync acknowledgment
        override fun deleteBloodHistory() = enqueueCommand(WatchHDeleteBloodHistoryCommand())
        override fun deleteSleepHistory() = enqueueCommand(WatchHDeleteSleepHistoryCommand())
        override fun deleteTemperatureHistory() = enqueueCommand(WatchHDeleteTemperatureHistoryCommand())
        override fun deleteSportHistory() = enqueueCommand(WatchHDeleteSportHistoryCommand())
        override fun deleteAllHistory() = enqueueCommand(WatchHDeleteAllHistoryCommand())
        override fun deleteSportModeHistory() = enqueueCommand(WatchHDeleteSportModeHistoryCommand())
        override fun deleteComprehensiveHistory() = enqueueCommand(WatchHDeleteComprehensiveHistoryCommand())
        override fun deleteHeartHistory() = enqueueCommand(WatchHDeleteHeartHistoryCommand())

        // Additional history data collection methods
        override fun getAllHistoryData() = enqueueCommand(WatchHGetAllHistoryCommand())
        override fun getHeartHistoryData() = enqueueCommand(WatchHGetHeartHistoryCommand())
        override fun getSportModeHistoryData() = enqueueCommand(WatchHHistorySportModeCommand())
        override fun getBloodOxygenHistoryData() = enqueueCommand(WatchHGetBloodOxygenHistoryCommand())
        override fun getComprehensiveHistoryData() = enqueueCommand(WatchHGetComprehensiveHistoryCommand())

        // Additional delete methods
        override fun deleteBloodOxygenHistory() = enqueueCommand(WatchHDeleteBloodOxygenHistoryCommand())
    }

    override val binder = WatchCommunicationServiceBinder()
}