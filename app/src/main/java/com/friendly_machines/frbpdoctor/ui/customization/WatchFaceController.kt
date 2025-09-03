package com.friendly_machines.frbpdoctor.ui.customization

import com.friendly_machines.fr_yhe_api.watchprotocol.IWatchBinder
import com.friendly_machines.fr_yhe_api.watchprotocol.IWatchListener
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.Crc16
import com.friendly_machines.fr_yhe_pro.command.WatchWControlDownloadCommand
import com.friendly_machines.fr_yhe_pro.command.WatchWDeleteWatchDialCommand
import com.friendly_machines.fr_yhe_pro.command.WatchWGetWatchDialInfoCommand
import com.friendly_machines.fr_yhe_pro.command.WatchWNextDownloadChunkCommand
import com.friendly_machines.fr_yhe_pro.command.WatchWNextDownloadChunkMetaCommand
import com.friendly_machines.fr_yhe_pro.command.WatchWSetCurrentWatchDialCommand
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlin.reflect.KClass

// TODO: Progress private val _progress = MutableStateFlow<DownloadProgress>()
//val progress = _progress.asStateFlow()
//
//// Then in the download function:
//_progress.value = DownloadProgress("Sending chunk $chunkIndex")

// YHE Pro; it's important that there's only one instance live of this because otherwise we could misinterpret responses meant for others as responses meant for us.
class WatchFaceController(val binder: IWatchBinder, val progresser: (String) -> Unit) : IWatchListener {
    private val responseChannel = Channel<WatchResponse>()
    override fun onWatchResponse(response: WatchResponse) {
        // ONLY handle responses of class 9 (W); those are very few
        if (response is WatchWControlDownloadCommand.Response || response is WatchWDeleteWatchDialCommand.Response || response is WatchWGetWatchDialInfoCommand.Response || response is WatchWNextDownloadChunkCommand.Response || response is WatchWNextDownloadChunkMetaCommand.Response || response is WatchWSetCurrentWatchDialCommand.Response) {
            responseChannel.trySend(response) // FIXME: TRY? How about when it doesn't work, block someone?
        }
    }

    private suspend fun <T : WatchResponse> receive(clazz: KClass<T>): WatchResponse {
        while (true) {
            val result = responseChannel.receive()
            if (result.javaClass == clazz) {
                return result
            }
        }
    }

    private fun setProgress(text: String) {
        progresser(text)
    }

    suspend fun listWatchFaces(): WatchResponse {
        setProgress("listWatchFaces")
        binder.getWatchDial()
        return receive(WatchWGetWatchDialInfoCommand.Response::class)
    }

    suspend fun selectWatchFace(dialPlateId: Int): WatchResponse {
        setProgress("selectWatchFace $dialPlateId")
        binder.selectWatchFace(dialPlateId)
//       FIXME! withTimeout(5000) {
//            val response = receive()
//            // ... handle response
//        }
        return receive(WatchWSetCurrentWatchDialCommand.Response::class)
    }

    private suspend fun startWatchFaceDownload(length: UInt, dialPlateId: Int, blockNumber: Short, version: Short, crc: UShort): WatchResponse {
        setProgress("startWatchFaceDownload $length #dialPlateId $blockNumber $version $crc")
        binder.startWatchFaceDownload(length, dialPlateId, blockNumber, version, crc)
        return receive(WatchWControlDownloadCommand.Response::class)
    }

    private suspend fun stopWatchFaceDownload(length: UInt): WatchResponse {
        setProgress("stopWatchFaceDownload $length")
        binder.stopWatchFaceDownload(length)
        return receive(WatchWControlDownloadCommand.Response::class)
    }

    private suspend fun nextWatchFaceDownloadChunkMeta(deltaOffset: Int, packetCount: UShort, crc: UShort): WatchResponse {
        setProgress("nextWatchFaceDownloadChunkMeta $deltaOffset $packetCount $crc")
        binder.nextWatchFaceDownloadChunkMeta(deltaOffset, packetCount, crc)
        return receive(WatchWNextDownloadChunkCommand.Response::class)
    }

    /** Give the specified watchface BODY to the watch */
    suspend fun downloadWatchface(mtu: Byte, dialPlateId: Int, blockNumber: Short, version: Short, body: ByteArray) = coroutineScope {
        val metaChunkSize = 4096 // Byte
        val startResponse = startWatchFaceDownload(body.size.toUInt(), dialPlateId, blockNumber, version, Crc16.crc16(body).toUShort())
        if (startResponse !is WatchWControlDownloadCommand.Response || startResponse.ok != 1U.toByte() || startResponse.errorCode != 0U.toByte()) {
            throw Exception("Starting download failed")
        }
        body.iterator().asSequence().chunked(metaChunkSize).forEachIndexed { chunkIndex, chunk ->
            // Send MTU chunks
            // TEST: Package should be 182 B with the 6 byte header (command code; size; checksum); package should literally say 0901 b600 *; the b6 is important
            var packageCount: UShort = 0U
            chunk.chunked(mtu - 6).forEach { packageChunk ->
                // (no response will come)
                binder.sendWatchFaceDownloadChunk(packageChunk.toByteArray())
                ++packageCount
                delay(10) // Small delay of 10ms between chunks
            }
            val verifyResponse = nextWatchFaceDownloadChunkMeta(metaChunkSize, packageCount, Crc16.crc16(chunk.toByteArray()).toUShort())
            if (verifyResponse !is WatchWNextDownloadChunkMetaCommand.Response || verifyResponse.status != 0U.toByte()) {
                setProgress("Watch did not accept chunk $chunkIndex")
                throw Exception("Watch did not accept chunk $chunkIndex")
            }
        }
        val stopResponse = stopWatchFaceDownload(body.size.toUInt())
        if (stopResponse !is WatchWControlDownloadCommand.Response || stopResponse.ok != 0U.toByte() || stopResponse.errorCode != 0U.toByte()) {
            setProgress("Stopping download failed")
            throw Exception("Stopping download failed")
        }
    }
}