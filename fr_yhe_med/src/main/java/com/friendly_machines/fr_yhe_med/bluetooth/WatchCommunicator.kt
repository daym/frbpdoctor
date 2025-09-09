package com.friendly_machines.fr_yhe_med.bluetooth

import android.os.Binder
import android.util.Log
import com.friendly_machines.fr_yhe_api.commondata.DayOfWeekPattern
import com.friendly_machines.fr_yhe_api.commondata.PushMessageType
import com.friendly_machines.fr_yhe_api.commondata.SkinColor
import com.friendly_machines.fr_yhe_api.watchprotocol.IWatchBinder
import com.friendly_machines.fr_yhe_api.watchprotocol.IWatchCommunicator
import com.friendly_machines.fr_yhe_api.watchprotocol.IWatchListener
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchMessageDecodingException
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchMessageEncodingException
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchPhoneCallControlAnswer
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchProfileSex
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponseAnalysisResult
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponseType
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchTimePosition
import com.friendly_machines.fr_yhe_api.commondata.WatchWearingArm
import com.friendly_machines.fr_yhe_med.bluetooth.WatchCharacteristic.bigNotificationCharacteristic
import com.friendly_machines.fr_yhe_med.bluetooth.WatchCharacteristic.bigWritingPortCharacteristic
import com.friendly_machines.fr_yhe_med.bluetooth.WatchCharacteristic.decodeBigMessage
import com.friendly_machines.fr_yhe_med.bluetooth.WatchCharacteristic.decodeMessage
import com.friendly_machines.fr_yhe_med.bluetooth.WatchCharacteristic.decodeVariableLengthInteger
import com.friendly_machines.fr_yhe_med.bluetooth.WatchCharacteristic.encodeFirstPacket
import com.friendly_machines.fr_yhe_med.bluetooth.WatchCharacteristic.encodeInternal3
import com.friendly_machines.fr_yhe_med.bluetooth.WatchCharacteristic.encodeMessage
import com.friendly_machines.fr_yhe_med.bluetooth.WatchCharacteristic.encodeMiddlePacket
import com.friendly_machines.fr_yhe_med.bluetooth.WatchCharacteristic.notificationCharacteristic
import com.friendly_machines.fr_yhe_med.bluetooth.WatchCharacteristic.writingPortCharacteristic
import com.friendly_machines.fr_yhe_med.command.WatchBindCommand
import com.friendly_machines.fr_yhe_api.commondata.WatchChangeAlarmAction
import com.friendly_machines.fr_yhe_med.command.WatchChangeAlarmCommand
import com.friendly_machines.fr_yhe_med.command.WatchCommand
import com.friendly_machines.fr_yhe_med.command.WatchDeviceInfoCommand
import com.friendly_machines.fr_yhe_med.command.WatchGetAlarmCommand
import com.friendly_machines.fr_yhe_med.command.WatchGetBatteryStateCommand
import com.friendly_machines.fr_yhe_med.command.WatchGetBpDataCommand
import com.friendly_machines.fr_yhe_med.command.WatchGetDeviceConfigCommand
import com.friendly_machines.fr_yhe_med.command.WatchGetHeatDataCommand
import com.friendly_machines.fr_yhe_med.command.WatchGetRawBpDataCommand
import com.friendly_machines.fr_yhe_med.command.WatchGetSleepDataCommand
import com.friendly_machines.fr_yhe_med.command.WatchGetSportDataCommand
import com.friendly_machines.fr_yhe_med.command.WatchGetStepDataCommand
import com.friendly_machines.fr_yhe_med.command.WatchGetWatchFaceCommand
import com.friendly_machines.fr_yhe_med.command.WatchSetMessageCommand
import com.friendly_machines.fr_yhe_med.command.WatchSetProfileCommand
import com.friendly_machines.fr_yhe_med.command.WatchSetStepGoalCommand
import com.friendly_machines.fr_yhe_med.command.WatchSetTimeCommand
import com.friendly_machines.fr_yhe_med.command.WatchSetWatchFaceCommand
import com.friendly_machines.fr_yhe_med.command.WatchSetWeatherCommand
import com.friendly_machines.fr_yhe_med.command.WatchUnbindCommand
import com.friendly_machines.fr_yhe_med.notification.WatchNotificationFromWatch
import com.friendly_machines.fr_yhe_med.notification.WatchResponseFactory
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
import io.reactivex.rxjava3.subjects.Subject
import java.nio.BufferUnderflowException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.util.Calendar
import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * We only ever see the Data Channel Payload. The Preamble, Access Address, Data Channel PDU header and BLE CRC are done by RxAndroidBle internally and do not count to the MTU.
 *
 * For Bluetooth LE, the Data Channel Payload hos these parts:
 *   1. Logical Link Control and Adaption Protocol (L2CAP) Header (4 B): Length (2 B) and channel ID (2 B)
 *   2. Attribute Protocol (ATT) Header (3 B): Opcode (1 B) and attribute handle (2 B)
 *   3. The actual ATT payload (up to 244 B)
 */
private const val B = 1
private const val BLE_L2CAP_HEADER_SIZE: Int = 4 * B

// ATT_MTU counts starting from here.
private const val BLE_ATT_HEADER_SIZE: Int = 3 * B
internal const val BLE_L2CAP_ATT_HEADER_SIZE: Int = BLE_L2CAP_HEADER_SIZE + BLE_ATT_HEADER_SIZE

// Our packets usually have this maximal overhead--except for the first packet (which has 6 B). TODO nicer?
private const val ENCODED_PACKET_MAJORITY_HEADER_SIZE: Int = 4 * B

public class WatchCommunicator : IWatchCommunicator {
    private var bleDisposables = CompositeDisposable()
    private var mtu: Int = 23
    private var maxPacketPayloadSize: Int = 23 - BLE_L2CAP_ATT_HEADER_SIZE - ENCODED_PACKET_MAJORITY_HEADER_SIZE

    private var connecting: Boolean = false
    private lateinit var keyDigest: ByteArray
    private var connection: RxBleConnection? = null

    companion object {
        const val TAG: String = "WatchCommunicator"
        val cipher: Cipher = Cipher.getInstance("AES/CBC/NoPadding")
    }

    private val commandQueue = PublishSubject.create<WatchCommand>().toSerialized()

    private fun enqueueCommand(
        command: WatchCommand,
    ) {
        commandQueue.onNext(command)
    }

    private fun setupSender(commandQueue: Subject<WatchCommand>) {
        // TODO .onBackpressureBuffer().flatMap(bytesAndFilter -> {}, 1/*serialized communication*/)
        bleDisposables.add(commandQueue.subscribe({
            sendAll(it)
        }, {
            Log.e(TAG, "setupSender: $it")
        }))
    }

    private fun setupNotifications(characteristicUuid: UUID, callback: (input: ByteArray) -> Unit) {
        try {
            val notificationObservable: Observable<ByteArray> = connection!!.setupNotification(characteristicUuid).flatMap { it }

            // FIXME also discoverServices ("andThen")

            val disposable = notificationObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(callback) { throwable ->
                run {
                    Log.e(TAG, "Notification error: $throwable")
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
                TAG, "Conflicting notification already set for characteristic: $characteristicUuid"
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

    private fun decryptMessage(wrap: ByteBuffer): ByteArray {
        val encryptionMode = wrap.get() // if != 0, encrypted; usually: 1
        val contents: ByteArray = if (encryptionMode.toInt() != 0) {
            if (encryptionMode.toInt() != 1) {
                throw WatchMessageDecodingException("decryptMessage error: unknown encryptionMode")
            }
            val rawIv = ByteArray(16)
            wrap.get(rawIv)
            val ivParameterSpec = IvParameterSpec(rawIv)
            val remaining = wrap.remaining()
            val rawContents = ByteArray(remaining)
            wrap.get(rawContents)
            try {
                val secretKeySpec = SecretKeySpec(keyDigest, "AES")
                val instance = Cipher.getInstance("AES/CBC/NoPadding")
                instance.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec)
                instance.doFinal(rawContents)
            } catch (e: NoSuchAlgorithmException) {
                throw WatchMessageDecodingException("decryptMessage error", e)
            } catch (e: NoSuchPaddingException) {
                throw WatchMessageDecodingException("decryptMessage error", e)
            } catch (e: InvalidAlgorithmParameterException) {
                throw WatchMessageDecodingException("decryptMessage error", e)
            } catch (e: IllegalBlockSizeException) {
                throw WatchMessageDecodingException("decryptMessage error", e)
            } catch (e: BadPaddingException) {
                throw WatchMessageDecodingException("decryptMessage error", e)
            } catch (e: InvalidKeyException) {
                Log.e(TAG, e.toString())
                throw WatchMessageDecodingException("decryptMessage error", e)
            }
        } else {
            val remaining = wrap.remaining()
            val rawContents = ByteArray(remaining)
            wrap.get(rawContents)

            rawContents
        }
        return contents
    }

    private var listeners = HashSet<IWatchListener>()

    private var sendingSequenceNumber = AtomicInteger(1) // verified; our first packet after the reset packet needs to be with sendingSequenceNumber > 0

    //private var receivingBuffers = ConcurrentHashMap<Int, Pair<Int, ByteArrayOutputStream>>() // packet_0_serial -> (current_serial, buffer)
    private fun onNotificationReceived(messageBuf: ByteBuffer) {
        val result = try {
            decodeMessage(ByteBuffer.wrap(decryptMessage(messageBuf)))
        } catch (e: WatchMessageDecodingException) {
            notifyListenersOfException(e)
            return
        } catch (e: BufferUnderflowException) {
            notifyListenersOfException(e)
            return
        }
        if (result.command.toInt() == 0) { // resets sequence numbers
            sendingSequenceNumber.set(1) // verified.
            // Note: sequenceNumber == 0, ackedSequenceNumber == 1--8
        }

        val response = WatchResponseFactory.parse(
            result.command, ByteBuffer.wrap(result.arguments)
        )
        Log.d(TAG, "-> decoded: $response")
        listeners.forEach {
            it.onWatchResponse(response)
            // TODO onMusicControl maybe ?
            if (response is WatchNotificationFromWatch) {
                if (response.eventCode == WatchNotificationFromWatch.EVENT_CODE_ANSWER_PHONE_CALL) {
                    it.onWatchPhoneCallControl(WatchPhoneCallControlAnswer.Accept)
                }
            } else if (response is WatchDeviceInfoCommand.Response) {
                it.onResetSequenceNumbers()
            }
        }
    }

    private fun onBigNotificationReceived(messageBuf: ByteBuffer) {
        val result = try {
            decodeBigMessage(ByteBuffer.wrap(decryptMessage(messageBuf)))
        } catch (e: WatchMessageDecodingException) {
            notifyListenersOfException(e)
            return
        } catch (e: BufferUnderflowException) {
            notifyListenersOfException(e)
            return
        }
        listeners.forEach {
            when (result.command) {
                else -> it.onBigWatchRawResponse(rawResponse = result)
            }
        }
    }

    /** Take the given client message and send it via channel 3, if necessary split it into different bluetooth packets */
    private fun sendInternal3(
        sendingSequenceNumber: Int, type: Byte, // FIXME 1:ota; otherwise:font
        body: ByteArray /* command body */, connection: RxBleConnection
    ) {
        val rawMessage = encodeInternal3(body, sendingSequenceNumber, type)
        Log.d(TAG, "gonna write to watch (big) ${rawMessage.contentToString()}")
        val encryptedMessage = encryptMessage(rawMessage)
        val totalMessageSize = encryptedMessage.size
        val buf = ByteBuffer.wrap(encryptedMessage).order(ByteOrder.BIG_ENDIAN)
        var packetIndex = 0
        run {
            val chunkSize = buf.remaining().coerceAtMost(maxPacketPayloadSize)
            val packetPayload = ByteArray(chunkSize)
            buf.get(packetPayload)
            val chunk = encodeFirstPacket(packetIndex, packetPayload, totalMessageSize)
            bleDisposables.add(connection.writeCharacteristic(bigWritingPortCharacteristic, chunk).observeOn(AndroidSchedulers.mainThread()).subscribe({ _: ByteArray? ->
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
        while (buf.hasRemaining()) {
            packetIndex += 1
            val chunkSize = buf.remaining().coerceAtMost(maxPacketPayloadSize)
            val packetPayload = ByteArray(chunkSize)
            buf.get(packetPayload)
            assert(packetIndex == 0) // FIXME
            val chunk = encodeMiddlePacket(packetIndex, packetPayload)
            bleDisposables.add(connection.writeCharacteristic(bigWritingPortCharacteristic, chunk).observeOn(AndroidSchedulers.mainThread()).subscribe({ _: ByteArray? ->
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
    }

    /** Encrypt the given plainText using keyDigest */
    private fun encryptMessage(
        plainText: ByteArray
    ): ByteArray {
        var paddingLength = 16 - plainText.size % 16
        if (paddingLength == 16) {
            paddingLength = 0
        }
        val paddedPlainText = ByteArray(plainText.size + paddingLength)
        System.arraycopy(plainText, 0, paddedPlainText, 0, plainText.size)
        val iv = ByteArray(cipher.blockSize)
        SecureRandom().nextBytes(iv)
        val ivParameterSpec = IvParameterSpec(iv)
        val cipherText = try {
            val instance = Cipher.getInstance("AES/CBC/NoPadding")
            instance.init(Cipher.ENCRYPT_MODE, SecretKeySpec(keyDigest, "AES"), ivParameterSpec)
            instance.doFinal(paddedPlainText)
        } catch (e: Exception) {
            Log.e(TAG, "encryption error")
            throw WatchMessageEncodingException("encryption error", e)
        }
        val encryptionMode = 1.toByte()
        return ByteBuffer.allocate(1 + iv.size + cipherText.size).order(ByteOrder.BIG_ENDIAN).put(encryptionMode).put(iv).put(cipherText).array()
    }

    /** Take the given client message and send it, if necessary splitting it into different bluetooth packets */
    private fun sendInternal(
        sendingSequenceNumber: Int,
        command: Short,
        body: ByteArray, /* command body */
    ) {
        val contents = encodeMessage(body, sendingSequenceNumber, command)
        Log.d(TAG, "gonna write to watch (small) ${contents.contentToString()}")
        val totalMessage = encryptMessage(contents)
        val totalMessageSize = totalMessage.size
        val buf = ByteBuffer.wrap(totalMessage).order(ByteOrder.BIG_ENDIAN)
        var packetIndex = 0
        run {
            val chunkSize = buf.remaining().coerceAtMost(maxPacketPayloadSize)
            val chunk = ByteArray(chunkSize)
            buf.get(chunk)
            val rawPacket = encodeFirstPacket(packetIndex, chunk, totalMessageSize)
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
        while (buf.hasRemaining()) {
            packetIndex += 1
            val chunkSize = buf.remaining().coerceAtMost(maxPacketPayloadSize)
            val chunk = ByteArray(chunkSize)
            buf.get(chunk)
            assert(packetIndex == 0) // FIXME
            val rawPacket = encodeMiddlePacket(packetIndex, chunk)
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
    }

    /** Add the given packet to the buffer. If we can assemble an entire message, return that message. */
    private fun bufferPacket(input: ByteArray): ByteBuffer? {
        val buf = ByteBuffer.wrap(input).order(ByteOrder.BIG_ENDIAN)
        val packetIndex = decodeVariableLengthInteger(buf)
        if (packetIndex == 0) {
            val messageLength = decodeVariableLengthInteger(buf)
            //receivingBuffers[command] = Pair(messageLength, ByteArrayOutputStream())
            val protocolVersion = buf.get() / 16 // rest is reserved
            Log.i(TAG, "Watch protocol version is $protocolVersion")
            assert(protocolVersion == 4)
            // TODO: Collect together enough packets to have messageLength
            assert(messageLength == buf.array().size - buf.position())
            return buf
        } else {
            Log.e(TAG, "Internal error: Watch protocol buffering not implemented")
            notifyListenersOfException(RuntimeException("watch protocol buffering not implemented"))
            return null
        }
    }

    private fun requestMtu(connection: RxBleConnection) {
        val disposable = connection.requestMtu(256).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({ mtu ->
            run {
                // mtu: 251
                this.mtu = mtu
                this.maxPacketPayloadSize = (mtu - BLE_L2CAP_ATT_HEADER_SIZE - ENCODED_PACKET_MAJORITY_HEADER_SIZE).coerceAtMost(251 - BLE_L2CAP_ATT_HEADER_SIZE - ENCODED_PACKET_MAJORITY_HEADER_SIZE)

                //setupSender(commandQueue = commandQueue)
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

    /** Connect to bleDevice and start sending commandQueue entries as needed. Also register for notifications and call listeners as necessary. */
    override fun start(bleDevice: RxBleDevice, keyDigest: ByteArray) {
        this.keyDigest = keyDigest
        assert(!this.connecting)
        this.connecting = true
        bleDisposables.clear()
        bleDisposables.add(
            bleDevice.establishConnection(false) // TODO timeout less than 30 s
                .retryWhen { errors ->
                    errors.flatMap { error ->
                        if (error is com.polidea.rxandroidble3.exceptions.BleDisconnectedException) {
                            Observable.timer(1, TimeUnit.SECONDS)
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
                        setupNotifications(notificationCharacteristic) { input ->
                            Log.d(TAG, "Notification received: ${input.contentToString()}")
                            bufferPacket(input)?.let {
                                onNotificationReceived(it)
                            }
                        }
                        setupNotifications(bigNotificationCharacteristic) { input ->
                            Log.d(TAG, "Big notification received: ${input.contentToString()}")
                            bufferPacket(input)?.let {
                                // note: big: messageLengthRaw - 17 is the total payload len
                                onBigNotificationReceived(it)
                            }
                        }
                        setupSender(commandQueue = commandQueue)
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
            enqueueCommand(WatchSetProfileCommand(height.toByte(), weight.toByte(), sex, age))
        }

        override fun setWeather(
            weatherType: Int, temp: Byte, maxTemp: Byte, minTemp: Byte, dummy: Byte/*0*/, month: Byte, dayOfMonth: Byte, dayOfWeekMondayBased: Byte, location: String
        ) = enqueueCommand(WatchSetWeatherCommand(weatherType.toShort(), temp, maxTemp, minTemp, dummy, month, dayOfMonth, dayOfWeekMondayBased, WatchCharacteristic.encodeWatchString(location)))

        override fun setMessage(type: com.friendly_machines.fr_yhe_api.commondata.MessageTypeMed, time: Int, title: String, content: String) = enqueueCommand(
            WatchSetMessageCommand(
                type.code, time, WatchCharacteristic.encodeWatchString(title), WatchCharacteristic.encodeWatchString(content)
            )
        )

        override fun setMessage2(type: Byte, time: Int, title: String, content: String) = enqueueCommand(
            WatchSetMessageCommand(
                type, time, WatchCharacteristic.encodeWatchString(title), WatchCharacteristic.encodeWatchString(content) // FIXME
            )
        )

        override fun pushMessage(pushMessageType: PushMessageType, message: String) = enqueueCommand(WatchGetBatteryStateCommand()) // dummy

        override fun setTime() {
            val currentTimeInSeconds = System.currentTimeMillis() / 1000
            val instance: Calendar = Calendar.getInstance()
            val timezoneInSeconds = (instance.get(Calendar.DST_OFFSET) + instance.get(Calendar.ZONE_OFFSET)) / 1000
            enqueueCommand(
                WatchSetTimeCommand(
                    currentTimeInSeconds.toInt(), timezoneInSeconds
                )
            )
        }

        override fun getBatteryState() = enqueueCommand(WatchGetBatteryStateCommand())
        override fun getAlarm() = enqueueCommand(WatchGetAlarmCommand())

        override fun addAlarm(id: Int, enabled: Boolean, hour: Byte, min: Byte, title: com.friendly_machines.fr_yhe_api.commondata.AlarmTitleMed, repeats: BooleanArray) = enqueueCommand(
            WatchChangeAlarmCommand(WatchChangeAlarmAction.Add, id, enabled, hour, min, title, repeats),
        )

        override fun editAlarm(id: Int, enabled: Boolean, hour: Byte, min: Byte, title: com.friendly_machines.fr_yhe_api.commondata.AlarmTitleMed, repeats: BooleanArray) = enqueueCommand(
            WatchChangeAlarmCommand(WatchChangeAlarmAction.Edit, id, enabled, hour, min, title, repeats),
        )
        
        override fun deleteAlarm(x: Byte, y: Byte) = enqueueCommand(WatchGetBatteryStateCommand()) // dummy

        override fun bindWatch(userId: Long, key: ByteArray) = enqueueCommand(
            WatchBindCommand(userId, key)
        )

        override fun unbindWatch() = enqueueCommand(WatchUnbindCommand())
        override fun getDeviceConfig() = enqueueCommand(WatchGetDeviceConfigCommand())
        override fun getBpData() = enqueueCommand(WatchGetBpDataCommand()) // FIXME arguments?
        override fun getSleepData(startTime: Int, endTime: Int) = enqueueCommand(WatchGetSleepDataCommand(startTime, endTime))
        override fun getRawBpData(startTime: Int, endTime: Int) = enqueueCommand(WatchGetRawBpDataCommand(startTime, endTime))
        override fun getStepData() = enqueueCommand(WatchGetStepDataCommand())
        override fun getHeatData() = enqueueCommand(WatchGetHeatDataCommand())
        override fun getWatchDial() = enqueueCommand(WatchGetWatchFaceCommand())
        override fun selectWatchFace(id: Int) = enqueueCommand(WatchSetWatchFaceCommand(id))
        override fun deleteWatchDial(id: Int) = enqueueCommand(WatchGetBatteryStateCommand()) // dummy

        override fun getSportData() = enqueueCommand(WatchGetSportDataCommand())
        override fun setStepGoal(steps: Int) = enqueueCommand(WatchSetStepGoalCommand(steps))
        override fun setLanguage(language: Byte) = enqueueCommand(WatchGetBatteryStateCommand()) // dummy

        override fun setUserSkinColor(enum: SkinColor) = enqueueCommand(WatchGetBatteryStateCommand()) // dummy
        override fun setUserSleep(hour: Byte, minute: Byte, repeats: UByte) = enqueueCommand(WatchGetBatteryStateCommand()) // dummy
        override fun setScheduleEnabled(enabled: Boolean) = enqueueCommand(WatchGetBatteryStateCommand()) // dummy
        override fun setRegularReminder(startHour: Byte, startMinute: Byte, endHour: Byte, endMinute: Byte, dayOfWeekPattern: Set<DayOfWeekPattern>, intervalInMinutes: Byte, message: String?) = enqueueCommand(WatchGetBatteryStateCommand()) // dummy
        override fun setHeartMonitoring(enabled: Boolean, interval: Byte, maxValue: UByte) = enqueueCommand(WatchGetBatteryStateCommand()) // dummy
        override fun setTemperatureMonitoring(enabled: Boolean, interval: Byte, maxValue: UByte) = enqueueCommand(WatchGetBatteryStateCommand()) // dummy
        override fun setLongSitting(startHour1: Byte, startMinute1: Byte, endHour1: Byte, endMinute1: Byte, startHour2: Byte, startMinute2: Byte, endHour2: Byte, endMinute2: Byte, repeats: UByte, interval: Byte) = enqueueCommand(WatchGetBatteryStateCommand()) // dummy
        override fun setScreenTimeLit(screenTimeLit: Byte) = enqueueCommand(WatchGetBatteryStateCommand()) // dummy
        override fun getChipScheme() = enqueueCommand(WatchGetBatteryStateCommand()) // dummy
        override fun setSportMode(sportState: com.friendly_machines.fr_yhe_api.commondata.SportState, sportType: com.friendly_machines.fr_yhe_api.commondata.SportType) = enqueueCommand(WatchGetBatteryStateCommand()) // dummy
        override fun getRealData(sensorType: com.friendly_machines.fr_yhe_api.commondata.RealDataSensorType, measureType: com.friendly_machines.fr_yhe_api.commondata.RealDataMeasureType, duration: Byte) = enqueueCommand(WatchGetBatteryStateCommand()) // dummy

        override fun setAccidentMonitoringEnabled(enabled: Boolean) = enqueueCommand(WatchGetBatteryStateCommand()) // dummy

        override fun addListener(watchListener: IWatchListener): IWatchBinder {
            this@WatchCommunicator.addListener(watchListener)
            return this@WatchCommunicator.binder
        }

        override fun removeListener(listener: IWatchListener) {
            listeners.remove(listener)
        }

        override fun resetSequenceNumbers() {
            enqueueCommand(
                WatchDeviceInfoCommand((mtu - BLE_L2CAP_ATT_HEADER_SIZE).toShort())
            )
        }

        override fun analyzeResponse(response: WatchResponse, expectedResponseType: WatchResponseType): WatchResponseAnalysisResult {
            when (expectedResponseType) {
                WatchResponseType.SetMessage -> {
                    return if (response is WatchSetMessageCommand.Response) {
                        if (response.status == 0.toByte()) {
                            WatchResponseAnalysisResult.Ok
                        } else {
                            WatchResponseAnalysisResult.Err
                        }
                    } else {
                        WatchResponseAnalysisResult.Mismatch
                    }
                }

                WatchResponseType.ChangeAlarm -> {
                    return if (response is WatchChangeAlarmCommand.Response) {
                        if (response.status == 0.toByte()) {
                            WatchResponseAnalysisResult.Ok
                        } else {
                            WatchResponseAnalysisResult.Err
                        }
                    } else {
                        WatchResponseAnalysisResult.Mismatch
                    }
                }

                WatchResponseType.SetStepGoal -> {
                    return if (response is WatchSetStepGoalCommand.Response) {
                        if (response.status == 0.toByte()) {
                            WatchResponseAnalysisResult.Ok
                        } else {
                            WatchResponseAnalysisResult.Err
                        }
                    } else {
                        WatchResponseAnalysisResult.Mismatch
                    }
                }

                WatchResponseType.Unbind -> {
                    return if (response is WatchUnbindCommand.Response) {
                        if (response.status == 0.toByte()) {
                            WatchResponseAnalysisResult.Ok
                        } else {
                            WatchResponseAnalysisResult.Err
                        }
                    } else {
                        WatchResponseAnalysisResult.Mismatch
                    }
                }

                WatchResponseType.Bind -> {
                    return if (response is WatchBindCommand.Response) {
                        if (response.status == 0.toByte()) {
                            WatchResponseAnalysisResult.Ok
                        } else {
                            WatchResponseAnalysisResult.Err
                        }
                    } else {
                        WatchResponseAnalysisResult.Mismatch
                    }
                }

                WatchResponseType.SetProfile -> {
                    return if (response is WatchSetProfileCommand.Response) {
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
                    return if (response is WatchGetAlarmCommand.Response) { // Err... not that great since the real data comes with a big response.
                        WatchResponseAnalysisResult.Ok
                    } else {
                        WatchResponseAnalysisResult.Mismatch
                    }
                }

                WatchResponseType.SetDndSettings -> { // dummy
                    return if (response is WatchGetBatteryStateCommand.Response) {
                        WatchResponseAnalysisResult.Ok
                    } else {
                        WatchResponseAnalysisResult.Mismatch
                    }
                }

                WatchResponseType.SetRegularReminder -> { // dummy
                    return if (response is WatchGetBatteryStateCommand.Response) {
                        WatchResponseAnalysisResult.Ok
                    } else {
                        WatchResponseAnalysisResult.Mismatch
                    }
                }

                WatchResponseType.SetWatchWearingArm -> { // dummy
                    return if (response is WatchGetBatteryStateCommand.Response) {
                        WatchResponseAnalysisResult.Ok
                    } else {
                        WatchResponseAnalysisResult.Mismatch
                    }
                }

                WatchResponseType.SetSkinColor -> { // dummy
                    return if (response is WatchGetBatteryStateCommand.Response) {
                        WatchResponseAnalysisResult.Ok
                    } else {
                        WatchResponseAnalysisResult.Mismatch
                    }
                }

                WatchResponseType.SetWatchScheduleEnabled -> { // dummy
                    return if (response is WatchGetBatteryStateCommand.Response) {
                        WatchResponseAnalysisResult.Ok
                    } else {
                        WatchResponseAnalysisResult.Mismatch
                    }
                }

                WatchResponseType.SetWatchTimeLayout -> { // dummy
                    return if (response is WatchGetBatteryStateCommand.Response) {
                        WatchResponseAnalysisResult.Ok
                    } else {
                        WatchResponseAnalysisResult.Mismatch
                    }
                }

                WatchResponseType.GetWatchDials -> {
                    return if (response is WatchGetWatchFaceCommand.Response) {
                        WatchResponseAnalysisResult.Ok
                    } else {
                        WatchResponseAnalysisResult.Mismatch
                    }
                }

                WatchResponseType.ChangeWatchDial -> { // dummy
                    return if (response is WatchGetBatteryStateCommand.Response) { // FIXME!!!!
                        WatchResponseAnalysisResult.Ok
                    } else {
                        WatchResponseAnalysisResult.Mismatch
                    }
                }

                WatchResponseType.GetFileList -> {
                    return if (response is WatchGetBatteryStateCommand.Response) { // dummy
                        WatchResponseAnalysisResult.Ok
                    } else {
                        WatchResponseAnalysisResult.Mismatch
                    }
                }

                WatchResponseType.SetWeather -> {
                    return if (response is WatchGetBatteryStateCommand.Response) { // dummy
                        WatchResponseAnalysisResult.Ok
                    } else {
                        WatchResponseAnalysisResult.Mismatch
                    }
                }

                WatchResponseType.SetHeartMonitoring -> {
                    return if (response is WatchGetBatteryStateCommand.Response) { // dummy
                        WatchResponseAnalysisResult.Ok
                    } else {
                        WatchResponseAnalysisResult.Mismatch
                    }
                }

                WatchResponseType.SetTemperatureMonitoring -> {
                    return if (response is WatchGetBatteryStateCommand.Response) { // dummy
                        WatchResponseAnalysisResult.Ok
                    } else {
                        WatchResponseAnalysisResult.Mismatch
                    }
                }

                WatchResponseType.SetAccidentMonitoringEnabled -> {
                    return if (response is WatchGetBatteryStateCommand.Response) { // dummy
                        WatchResponseAnalysisResult.Ok
                    } else {
                        WatchResponseAnalysisResult.Mismatch
                    }
                }
                WatchResponseType.SetSportMode -> {
                    return if (response is WatchGetBatteryStateCommand.Response) { // dummy
                        WatchResponseAnalysisResult.Ok
                    } else {
                        WatchResponseAnalysisResult.Mismatch
                    }
                }
            }
        }

        override fun getFileCount() = enqueueCommand(WatchGetBatteryStateCommand()) // dummy request
        override fun getFileList() = enqueueCommand(WatchGetBatteryStateCommand()) // dummy request
        override fun startWatchFaceDownload(length: UInt, dialPlateId: Int, blockNumber: Short, version: Short, crc: UShort) = enqueueCommand(WatchGetBatteryStateCommand()) // dummy request
        override fun sendWatchFaceDownloadChunk(chunk: ByteArray) = enqueueCommand(WatchGetBatteryStateCommand()) // dummy request
        override fun nextWatchFaceDownloadChunkMeta(deltaOffset: Int, packetCount: UShort, crc: UShort)  = enqueueCommand(WatchGetBatteryStateCommand()) // dummy request
        override fun stopWatchFaceDownload(length: UInt) = enqueueCommand(WatchGetBatteryStateCommand()) // dummy request

        override fun setWatchWearingArm(arm: WatchWearingArm) = enqueueCommand(WatchGetBatteryStateCommand()) // dummy request
        override fun setDndSettings(mode: Byte, startTimeHour: Byte, startTimeMin: Byte, endTimeHour: Byte, endTimeMin: Byte) = enqueueCommand(WatchGetBatteryStateCommand()) // dummy request
        override fun setWatchTimeLayout(watchTimePosition: WatchTimePosition, rgb565Color: UShort) = enqueueCommand(WatchGetBatteryStateCommand()) // dummy request
        override fun getGDeviceInfo() {
        }

        override fun getMainTheme() = enqueueCommand(WatchGetBatteryStateCommand()) // dummy request
        override fun setMainTheme(index: Byte) = enqueueCommand(WatchGetBatteryStateCommand()) // dummy request

        // Delete history methods for sync acknowledgment - dummies for med protocol
        override fun deleteBloodHistory() = enqueueCommand(WatchGetBatteryStateCommand()) // dummy
        override fun deleteSleepHistory() = enqueueCommand(WatchGetBatteryStateCommand()) // dummy
        override fun deleteTemperatureHistory() = enqueueCommand(WatchGetBatteryStateCommand()) // dummy
        override fun deleteSportHistory() = enqueueCommand(WatchGetBatteryStateCommand()) // dummy
        override fun deleteAllHistory() = enqueueCommand(WatchGetBatteryStateCommand()) // dummy
        override fun deleteSportModeHistory() = enqueueCommand(WatchGetBatteryStateCommand()) // dummy
        override fun deleteComprehensiveHistory() = enqueueCommand(WatchGetBatteryStateCommand()) // dummy
        override fun deleteHeartHistory() = enqueueCommand(WatchGetBatteryStateCommand()) // dummy
        
        // Additional history data collection methods - dummies for med protocol
        override fun getAllHistoryData() = enqueueCommand(WatchGetBatteryStateCommand()) // dummy
        override fun getHeartHistoryData() = enqueueCommand(WatchGetBatteryStateCommand()) // dummy
        override fun getSportModeHistoryData() = enqueueCommand(WatchGetBatteryStateCommand()) // dummy
        override fun getBloodOxygenHistoryData() = enqueueCommand(WatchGetBatteryStateCommand()) // dummy
        override fun getComprehensiveHistoryData() = enqueueCommand(WatchGetBatteryStateCommand()) // dummy
        
        // Additional delete methods - dummies for med protocol
        override fun deleteBloodOxygenHistory() = enqueueCommand(WatchGetBatteryStateCommand()) // dummy
    }

    override val binder = WatchCommunicationServiceBinder()
}