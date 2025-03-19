package com.friendly_machines.frbpdoctor.ui.customization

import com.friendly_machines.fr_yhe_api.watchprotocol.IWatchBinder
import com.friendly_machines.fr_yhe_api.watchprotocol.IWatchListener
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.Crc16
import com.friendly_machines.fr_yhe_pro.command.WatchWControlDownloadCommand
import com.friendly_machines.fr_yhe_pro.command.WatchWNextDownloadChunkCommand
import com.friendly_machines.fr_yhe_pro.command.WatchWNextDownloadChunkMetaCommand
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

// TODO: Progress private val _progress = MutableStateFlow<DownloadProgress>()
//val progress = _progress.asStateFlow()
//
//// Then in the download function:
//_progress.value = DownloadProgress("Sending chunk $chunkIndex")

// YHE Pro
class WatchFaceController(val binder: IWatchBinder) : IWatchListener {
    private val responseChannel = Channel<WatchResponse>()
    override fun onWatchResponse(response: WatchResponse) {
        // ONLY handle responses of class 9 (W); those are very few
        if (response is WatchWControlDownloadCommand.Response || response is WatchWNextDownloadChunkCommand.Response || response is WatchWNextDownloadChunkMetaCommand.Response) {
            responseChannel.trySend(response) // FIXME: TRY? How about when it doesn't work, block someone?
        }
    }
    private fun setProgress(text: String) {
        // FIXME
    }
    suspend fun listWatchFaces(): WatchResponse {
        binder.getWatchDial()
        return responseChannel.receive()
    }
    suspend fun selectWatchFace(dialPlateId: Int): WatchResponse {
        binder.selectWatchFace(dialPlateId)
//       FIXME! withTimeout(5000) {
//            val response = responseChannel.receive()
//            // ... handle response
//        }
        return responseChannel.receive()
    }
    private suspend fun startWatchFaceDownload(length: UInt, dialPlateId: Int, blockNumber: Short, version: Short, crc: UShort): WatchResponse {
        binder.startWatchFaceDownload(length, dialPlateId, blockNumber, version, crc)
        return responseChannel.receive()
    }
    private suspend fun stopWatchFaceDownload(length: UInt): WatchResponse {
        binder.stopWatchFaceDownload(length)
        return responseChannel.receive()
    }
    private suspend fun nextWatchFaceDownloadChunkMeta(deltaOffset: Int, packetCount: UShort, crc: UShort): WatchResponse {
        binder.nextWatchFaceDownloadChunkMeta(deltaOffset, packetCount, crc)
        return responseChannel.receive()
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
                ++packageCount;
                delay(10) // Small delay of 10ms between chunks
            }
            val verifyResponse = nextWatchFaceDownloadChunkMeta(metaChunkSize, packageCount, Crc16.crc16(chunk.toByteArray()).toUShort())
            if (verifyResponse !is WatchWNextDownloadChunkMetaCommand.Response || verifyResponse.status != 0U.toByte()) {
                throw Exception("Watch did not accept chunk $chunkIndex")
            }
        }
        val stopResponse = stopWatchFaceDownload(body.size.toUInt())
        if (stopResponse !is WatchWControlDownloadCommand.Response || stopResponse.ok != 0U.toByte() || stopResponse.errorCode != 0U.toByte()) {
            throw Exception("Stopping download failed")
        }
    }
}