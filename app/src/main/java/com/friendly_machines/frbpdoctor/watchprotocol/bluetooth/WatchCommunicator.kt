package com.friendly_machines.frbpdoctor.watchprotocol.bluetooth

import android.util.Log
import android.util.Pair
import com.friendly_machines.frbpdoctor.logger.Logger
import com.friendly_machines.frbpdoctor.watchprotocol.Crc16
import com.friendly_machines.frbpdoctor.watchprotocol.command.WatchCommand
import com.friendly_machines.frbpdoctor.watchprotocol.bluetooth.WatchCharacteristic.Companion.bigNotificationCharacteristic
import com.friendly_machines.frbpdoctor.watchprotocol.bluetooth.WatchCharacteristic.Companion.bigWritingPortCharacteristic
import com.friendly_machines.frbpdoctor.watchprotocol.bluetooth.WatchCharacteristic.Companion.notificationCharacteristic
import com.friendly_machines.frbpdoctor.watchprotocol.bluetooth.WatchCharacteristic.Companion.writingPortCharacteristic
import com.friendly_machines.frbpdoctor.watchprotocol.notification.WatchRawResponse
import com.friendly_machines.frbpdoctor.watchprotocol.notification.WatchResponse
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
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class WatchCommunicator {
    private var bleDisposables = CompositeDisposable()
    private var mtu: Int = 23 // FIXME default mtu is maybe even 20 or so
    private var maxPayloadSize: Int = 23 - 7 - 4

    private var connecting: Boolean = false
    private lateinit var keyDigest: ByteArray
    private var connection: RxBleConnection? = null

    companion object {
        const val TAG: String = "WatchCommunicator"
        val cipher: Cipher = Cipher.getInstance("AES/CBC/NoPadding")
        fun encodeWatchString(input: String): ByteArray {
            val inputChars = input.toCharArray()
            val buf = ByteBuffer.allocate(inputChars.size * 2).order(ByteOrder.BIG_ENDIAN)
            inputChars.forEach {
                buf.putChar(it)
            }
            return buf.array()
        }
    }

    private fun setupSender(commandQueue: PublishSubject<WatchCommand>) {
        // TODO .onBackpressureBuffer().flatMap(bytesAndFilter -> {}, 1/*serialized communication*/)
        bleDisposables.add(commandQueue.subscribe({
            sendAll(it)
        }, {
            Log.e(TAG, "setupSender")
        }))
    }

    private fun setupNotifications(characteristicUuid: UUID) {
        try {
            val notificationObservable: Observable<ByteArray> = connection!!.setupNotification(characteristicUuid).flatMap { it }

            // FIXME also discoverServices ("andThen")

            val disposable = notificationObservable
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({ data -> onNotificationReceived(characteristicUuid, data) }, { throwable ->
                    run {
                        Log.e(TAG, "Notification error: $throwable")
                    }
                })

            bleDisposables.add(disposable)
        } catch (e: BleCharacteristicNotFoundException) {
            Log.e(TAG, "Characteristic not found: $characteristicUuid")
            stopSelf()
        } catch (e: BleConflictingNotificationAlreadySetException) {
            Log.e(
                TAG, "Conflicting notification already set for characteristic: $characteristicUuid"
            )
            stopSelf()
        } catch (e: BleGattException) {
            Log.e(TAG, "Gatt error: $e")/*if (e.type == BleGattOperationType.NOTIFICATION) {
                Log.e(TAG, "Notification setup error for characteristic: $characteristicUuid")
            }*/
            stopSelf()
        }
    }

    // decode var length integer; result: (decoded value, length of raw data)
    private fun decodeVariableLengthInteger(buf: ByteBuffer): Pair<Int, Int> {
        var result = 0
        var basis = 1
        for (i in 0 until 4) {
            val chunk = buf.get().toInt()
            result += (chunk and 0x7F) * basis
            basis *= 0x80
            if ((chunk and 0x80) == 0) { // EOF
                return Pair(result, i + 1)
            }
        }
        return Pair(result, 4)
    }

    private fun encodeVariableLengthInteger(input: Int): ByteArray { // protobuf and/or MIDI
        var input = input
        val buf = ByteBuffer.allocate(
            if (input < 0x80) 1
            else if (input < 0x4000) 2
            else if (input < 0x20_0000) 3
            else 4
        ) // less than 7, 14, 21 bits of payloads need differing output length
        do {
            var chunk = input % 0x80 // take lowest 7 bits
            input /= 128 // shift by 7 bits
            if (input > 0) { // still more bits left
                chunk = chunk or 0x80 // set top bit as a "continue" flag
            }
            buf.put(chunk.toByte())
        } while (input > 0)
        return buf.array()
    }

    private fun decryptMessage(wrap: ByteBuffer): ByteArray {
        val encryptionMode = wrap.get() // if != 0, encrypted; usually: 1
        val contents: ByteArray = if (encryptionMode.toInt() != 0) {
            assert(encryptionMode.toInt() == 1)
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
                throw RuntimeException(e)
            } catch (e: NoSuchPaddingException) {
                throw RuntimeException(e)
            } catch (e: InvalidAlgorithmParameterException) {
                throw RuntimeException(e)
            } catch (e: IllegalBlockSizeException) {
                throw RuntimeException(e)
            } catch (e: BadPaddingException) {
                throw RuntimeException(e)
            } catch (e: InvalidKeyException) {
                Log.e(TAG, e.toString())
                throw RuntimeException(e)
            }
        } else {
            val remaining = wrap.remaining()
            val rawContents = ByteArray(remaining)
            wrap.get(rawContents)

            rawContents
        }
        return contents
    }

    private fun decodeMessage(wrap: ByteBuffer): WatchRawResponse {
        val buf = ByteBuffer.wrap(decryptMessage(wrap)).order(ByteOrder.BIG_ENDIAN)
        val serialNumber = buf.int // generated by watch
        val ackedSerialNumber = buf.int // That's the serial number of the package that we had sent to the watch
        val command = buf.short
        val length = buf.short.toInt()
        if (buf.hasRemaining()) {
            val rawContents = ByteArray(length)
            buf.get(rawContents)
            val lengthUntilCrcField = buf.position()
            buf.rewind()
            val everythingButCrc = ByteArray(lengthUntilCrcField)
            buf.get(everythingButCrc)
            val oldCrc = buf.short
            val newCrc = Crc16.crc16(everythingButCrc)
            if (oldCrc == newCrc) {
                Log.i(TAG, "decode crc ok")
            } else {
                Log.e(TAG, "decode crc mistake")
            }
            return WatchRawResponse(
                serialNumber, ackedSerialNumber, command, rawContents
            )
        }
        // FIXME log here, or throw exception?
        return WatchRawResponse(
            serialNumber, ackedSerialNumber, command, ByteArray(0)
        )
    }

    private fun decode3(wrap: ByteBuffer): WatchRawResponse {
        // FIXME check
        val contents: ByteArray = decryptMessage(wrap)
        // OK from here
        val buf = ByteBuffer.wrap(contents).order(ByteOrder.BIG_ENDIAN)
        val sn = buf.int
        buf.get()
        val command = buf.short
        val length = buf.short.toInt()
        // Note: length == 0: "we are done"
        // FIXME RAW_BP_DATA is special
        if (buf.hasRemaining()) {
            val bArr6 = ByteArray(length) // contents
            buf.get(bArr6)
            val oldCrc = buf.short
            buf.rewind()
            val bufBeforeCrc = ByteArray(length + 12) // I have no idea
            buf.get(bufBeforeCrc)
            val newCrc = Crc16.crc16(bufBeforeCrc)

            if (oldCrc == newCrc) {
                Log.i(TAG, "decode3 crc ok")
            } else {
                Log.e(TAG, "decode3 crc mistake")
            }
            return WatchRawResponse(
                sn, sn, command, bArr6
            )
        }
        return WatchRawResponse(
            sn, sn, command, ByteArray(0)
        )
    }

    private var listeners = HashSet<WatchListener>()

    private var sendingSequenceNumber = AtomicInteger(1) // verified; our first sn after the reset packet needs to be with sn > 0

    private fun onNotificationReceived(characteristicUuid: UUID, input: ByteArray) {
        Log.d(TAG, "Notification received: ${characteristicUuid}: ${input.contentToString()}")
        Logger.log("Notification received: ${characteristicUuid}: ${input.contentToString()}")
        val buf = ByteBuffer.wrap(input).order(ByteOrder.BIG_ENDIAN)
        val packetIndexRaw = decodeVariableLengthInteger(buf)

        // skip packetIndex field
        buf.position(packetIndexRaw.second)
        val packetIndex = packetIndexRaw.first
        if (packetIndex == 0) { // first packet has a total length
            val position = buf.position()
            val messageLengthRaw = decodeVariableLengthInteger(buf) // total length of the message, not just the packet
            val messageLengthRawLength = messageLengthRaw.second // how much space the number itself takes
            buf.position(messageLengthRawLength.toInt() + position)
            val protocolVersion = buf.get() / 16 // rest is reserved
            assert(protocolVersion == 4)
            Log.e(TAG, "watch protocol version $protocolVersion")
            // note: big: messageLengthRaw - 17 is the total payload len
        } else {
            Log.e(TAG, "watch protocol buffering not implemented")
        }
        // TODO wrap.position(((Number) s).intValue()); (resume)
        if (notificationCharacteristic == characteristicUuid) {
            val result = decodeMessage(buf)
            if (result.command.toInt() == 0) { // resets sequence numbers
                sendingSequenceNumber.set(1) // verified.
                // Note: sn == 0, ack_sn == 1--8
            }

            val response = WatchResponse.parse(
                result.command, ByteBuffer.wrap(result.arguments).order(
                    ByteOrder.BIG_ENDIAN
                )
            )
            Logger.log("-> decoded: $response")
            listeners.forEach {
                when (result.command) {
                    else -> it.onWatchResponse(response)
                }
            }

        } else if (bigNotificationCharacteristic == characteristicUuid) {
            val result = decodeBigMessage(buf)

            listeners.forEach {
                when (result.command) {
                    else -> it.onBigWatchRawResponse(rawResponse = result)
                }
            }
        }
    }

    private fun decodeBigMessage(wrap: ByteBuffer): WatchRawResponse {
        val buf = ByteBuffer.wrap(decryptMessage(wrap)).order(ByteOrder.BIG_ENDIAN)
        val sn = buf.int // sn generated by watch
        buf.get() // TODO
        val command = buf.short
        val length = buf.short.toInt()
        val result = ByteArray(length)
        buf.get(result)
        // padding
        //val r = buf.remaining()
        //Log.e(TAG, "remaining $r")
        return WatchRawResponse(sn, sn, command, result)
    }

    /**
     * If packetIndex == 0, it's the first packet. Otherwise, packetIndex > 0 is requires.
     * totalMessageLen is only used if packetIndex == 0
     */
    private fun encodePacket(
        packetIndex: Int,
        packetBody: ByteArray,
        totalMessageLength: Int,
    ): ByteArray {
        val rawPacketIndex = encodeVariableLengthInteger(packetIndex)
        return if (packetIndex == 0) { // first chunk
            val rawTotalMessageLength = encodeVariableLengthInteger(totalMessageLength)
            val buf = ByteBuffer.allocate(rawPacketIndex.size + rawTotalMessageLength.size + 1 + packetBody.size).order(ByteOrder.BIG_ENDIAN)
            buf.put(rawPacketIndex)
            buf.put(rawTotalMessageLength)
            buf.put((4 * 16).toByte()) // FIXME 1
            buf.put(packetBody)
            buf.array()
        } else {
            val buf = ByteBuffer.allocate(rawPacketIndex.size + packetBody.size).order(ByteOrder.BIG_ENDIAN)
            buf.put(rawPacketIndex)
            buf.put(packetBody)
            buf.array()
        }
    }

    /** Take the given client message and send it via channel 3, if necessary split it into different bluetooth packets */
    private fun sendInternal3(
        sendingSequenceNumber: Int, type: Byte, // FIXME 1:ota; otherwise:font
        body: ByteArray /* command body */, connection: RxBleConnection
    ) {
        val rawMessage = encodeInternal3(body, sendingSequenceNumber, type)
        Logger.log("gonna write to watch (big) ${rawMessage.contentToString()}")
        val encryptedMessage = encryptMessage(rawMessage)
        val totalMessageSize = encryptedMessage.size
        val buf = ByteBuffer.wrap(encryptedMessage).order(ByteOrder.BIG_ENDIAN)
        var packetIndex = 0
        while (buf.hasRemaining()) {
            val chunkSize = buf.remaining().coerceAtMost(maxPayloadSize)
            val packetPayload = ByteArray(chunkSize)
            buf.get(packetPayload)
            assert(packetIndex == 0)
            val chunk = encodePacket(packetIndex, packetPayload, totalMessageSize)
            bleDisposables.add(connection.writeCharacteristic(bigWritingPortCharacteristic, chunk).observeOn(AndroidSchedulers.mainThread()).subscribe({ _: ByteArray? ->
                Log.d(
                    TAG, "Write characteristic successful"
                )
            }) { throwable: Throwable ->
                Log.e(
                    TAG, "Write characteristic error: $throwable"
                )
                stopSelf()
            })

            packetIndex += 1
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
            instance.init(1, SecretKeySpec(keyDigest, "AES"), ivParameterSpec)
            instance.doFinal(paddedPlainText)
        } catch (unused: Exception) {
            Log.e(TAG, "encryption error")
            ByteArray(0)
        }
        return ByteBuffer.allocate(1 + iv.size + cipherText.size).order(ByteOrder.BIG_ENDIAN).put(1.toByte()/*encryption mode*/).put(iv).put(cipherText).array()
    }

    /** Encode the given body into a watch message body */
    private fun encodeMessage(
        body: ByteArray, sendingSequenceNumber: Int, command: Short
    ): ByteArray {
        var buffer = ByteBuffer.allocate(4 + 4 + 2 + 2 + body.size).order(ByteOrder.BIG_ENDIAN)
        buffer.putInt(sendingSequenceNumber)
        buffer.putInt(0) // junk
        buffer.putShort(command)
        buffer.putShort(body.size.toShort())
        buffer.put(body)
        val rawBuffer0 = buffer.array()
        val crc = Crc16.crc16(rawBuffer0)
        buffer = ByteBuffer.allocate(buffer.limit() + 2)
        buffer.put(rawBuffer0)
        buffer.putShort(crc)
        return buffer.array()
    }

    // TODO: check
    private fun encodeInternal3(
        body: ByteArray, sendingSequenceNumber: Int, type: Byte
    ): ByteArray {
        val buf = ByteBuffer.allocate(4 + 1 + 2 + 2 + body.size).order(ByteOrder.BIG_ENDIAN)
        buf.putInt(sendingSequenceNumber)
        buf.put(type)
        buf.putShort(0) // junk
        buf.putShort(body.size.toShort())
        buf.put(body)

        // Put CRC

        val buf2 = ByteBuffer.allocate(buf.limit() + 2).order(ByteOrder.BIG_ENDIAN)
        buf2.put(buf.array())
        buf2.putShort(Crc16.crc16(buf.array()))
        return buf2.array()
    }

    /** Take the given client message and send it, if necessary splitting it into different bluetooth packets */
    private fun sendInternal(
        sendingSequenceNumber: Int,
        command: Short,
        body: ByteArray, /* command body */
    ) {
        val contents = encodeMessage(body, sendingSequenceNumber, command)
        Logger.log("gonna write to watch (small) ${contents.contentToString()}")
        val totalMessage = encryptMessage(contents)
        val totalMessageSize = totalMessage.size
        val buf = ByteBuffer.wrap(totalMessage).order(ByteOrder.BIG_ENDIAN)
        var packetIndex = 0
        while (buf.hasRemaining()) {
            val chunkSize = buf.remaining().coerceAtMost(maxPayloadSize)
            val chunk = ByteArray(chunkSize)
            buf.get(chunk)
            assert(packetIndex == 0)
            val rawPacket = encodePacket(packetIndex, chunk, totalMessageSize)
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
                stopSelf()
            })

            packetIndex += 1
        }
    }

    /** Connect to bleDevice and start sending commandQueue entries as needed. Also register for notifications and call listeners as necessary. */
    fun start(bleDevice: RxBleDevice, commandQueue: PublishSubject<WatchCommand>) {
        assert(!this.connecting)
        this.connecting = true
        bleDisposables.clear()
        bleDisposables.add(
            bleDevice.establishConnection(false) // TODO timeout less than 30 s
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({ connection ->
                    run {
                        Log.d(TAG, "Connection established")

                        this.connection = connection
                        setupNotifications(bigNotificationCharacteristic)
                        setupNotifications(notificationCharacteristic)
                        setupSender(commandQueue = commandQueue)

                        val disposable = connection.requestMtu(256).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({ mtu ->
                            run {
                                // mtu: 251
                                this.mtu = mtu
                                this.maxPayloadSize = ((mtu - 7) - 4).coerceAtMost(240)

                                listeners.forEach {
                                    it.onMtuResponse(mtu)
                                }
                            }
                        }, { throwable ->
                            run {
                                Log.e(TAG, "MTU request failed: $throwable")
                                stopSelf()
                            }
                        })

                        bleDisposables.add(disposable)
                    }
                }, { throwable ->
                    run {
                        Log.e(TAG, "Connection error: $throwable")
                        stopSelf()
                    }
                })
        )
    }

    fun setKeyDigest(keyDigest: ByteArray) {
        this.keyDigest = keyDigest
    }

    /** Send command immediately */
    private fun sendAll(
        command: WatchCommand
    ) {
        val sendingSequenceNumber = this.sendingSequenceNumber.getAndAdd(1)
        sendInternal(
            sendingSequenceNumber,
            command.code,
            command.arguments,
        )
    }

    fun onDestroy() {
        bleDisposables.clear()
    }

    private fun stopSelf() {
        // FIXME
        TODO("Not yet implemented")
    }

    fun removeListener(listener: WatchListener) {
        this.listeners.remove(listener)
    }

    fun addListener(listener: WatchListener): WatchCommunicator {
        this.listeners.add(listener)
        return this
    }
}