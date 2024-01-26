package com.friendly_machines.frbpdoctor.watchprotocol.bluetooth

import android.util.Log
import com.friendly_machines.frbpdoctor.watchprotocol.WatchMessageDecodingException
import com.friendly_machines.frbpdoctor.watchprotocol.WatchMessageEncodingException
import com.friendly_machines.frbpdoctor.watchprotocol.command.WatchCommand
import com.friendly_machines.frbpdoctor.watchprotocol.bluetooth.WatchCharacteristic.bigNotificationCharacteristic
import com.friendly_machines.frbpdoctor.watchprotocol.bluetooth.WatchCharacteristic.bigWritingPortCharacteristic
import com.friendly_machines.frbpdoctor.watchprotocol.bluetooth.WatchCharacteristic.decodeBigMessage
import com.friendly_machines.frbpdoctor.watchprotocol.bluetooth.WatchCharacteristic.decodeMessage
import com.friendly_machines.frbpdoctor.watchprotocol.bluetooth.WatchCharacteristic.decodeVariableLengthInteger
import com.friendly_machines.frbpdoctor.watchprotocol.bluetooth.WatchCharacteristic.encodeFirstPacket
import com.friendly_machines.frbpdoctor.watchprotocol.bluetooth.WatchCharacteristic.encodeInternal3
import com.friendly_machines.frbpdoctor.watchprotocol.bluetooth.WatchCharacteristic.encodeMessage
import com.friendly_machines.frbpdoctor.watchprotocol.bluetooth.WatchCharacteristic.encodeMiddlePacket
import com.friendly_machines.frbpdoctor.watchprotocol.bluetooth.WatchCharacteristic.notificationCharacteristic
import com.friendly_machines.frbpdoctor.watchprotocol.bluetooth.WatchCharacteristic.writingPortCharacteristic
import com.friendly_machines.frbpdoctor.watchprotocol.notification.WatchResponse
import com.polidea.rxandroidble3.RxBleConnection
import com.polidea.rxandroidble3.RxBleDevice
import com.polidea.rxandroidble3.exceptions.BleCharacteristicNotFoundException
import com.polidea.rxandroidble3.exceptions.BleConflictingNotificationAlreadySetException
import com.polidea.rxandroidble3.exceptions.BleGattException
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.Subject
import java.nio.BufferUnderflowException
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

/**
 * We only ever see the Data Channel Payload. The Preamble, Access Address, Data Channel PDU header and BLE CRC are done by RxAndroidBle internally and do not count to the MTU.
 *
 * For Bluetooth LE, the Data Channel Payload hos these parts:
 *   1. Logical Link Control and Adaption Protocol (L2CAP) Header (4 B): Length (2 B) and channel ID (2 B)
 *   2. Attribute Protocol (ATT) Header (3 B): Opcode (1 B) and attribute handle (2 B)
 *   3. The actual ATT payload (up to 244 B)
 */
private const val B = 1
private const val BLE_L2CAP_HEADER_SIZE: Int = 4*B
// ATT_MTU counts starting from here.
private const val BLE_ATT_HEADER_SIZE: Int = 3*B
internal const val BLE_L2CAP_ATT_HEADER_SIZE: Int = BLE_L2CAP_HEADER_SIZE + BLE_ATT_HEADER_SIZE

// Our packets usually have this maximal overhead--except for the first packet (which has 6 B). TODO nicer?
private const val ENCODED_PACKET_MAJORITY_HEADER_SIZE: Int = 4*B

class WatchCommunicator {
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

    private fun setupSender(commandQueue: Subject<WatchCommand>) {
        // TODO .onBackpressureBuffer().flatMap(bytesAndFilter -> {}, 1/*serialized communication*/)
        bleDisposables.add(commandQueue.subscribe({
            sendAll(it)
        }, {
            Log.e(TAG, "setupSender")
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

    private var listeners = HashSet<WatchListener>()

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

        val response = WatchResponse.parse(
            result.command, ByteBuffer.wrap(result.arguments)
        )
        Log.d(TAG, "-> decoded: $response")
        listeners.forEach {
            when (result.command) {
                else -> it.onWatchResponse(response)
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
    fun start(bleDevice: RxBleDevice, commandQueue: Subject<WatchCommand>) {
        assert(!this.connecting)
        this.connecting = true
        bleDisposables.clear()
        bleDisposables.add(
            bleDevice.establishConnection(false) // TODO timeout less than 30 s
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({ connection ->
                    run {
                        Log.d(TAG, "Connection established")
                        this.connection = connection
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
                        Log.e(TAG, "Connection error: $throwable")
                        notifyListenersOfException(throwable)
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
            command.operation.code,
            command.arguments,
        )
    }

    fun onDestroy() {
        bleDisposables.clear()
    }

    fun removeListener(listener: WatchListener) {
        this.listeners.remove(listener)
    }

    fun addListener(listener: WatchListener): WatchCommunicator {
        this.listeners.add(listener)
        return this
    }

    private fun notifyListenersOfException(exception: Throwable) {
        this.listeners.forEach {
            it.onException(exception)
        }
    }
}