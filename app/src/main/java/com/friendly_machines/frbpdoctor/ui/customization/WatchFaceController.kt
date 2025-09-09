package com.friendly_machines.frbpdoctor.ui.customization

import android.util.Log
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
class WatchFaceController(val binder: IWatchBinder, val progresser: (Float, String) -> Unit) : IWatchListener {
    private val TAG: String = "WatchFaceController"
    private val responseChannel = Channel<WatchResponse>()
    override fun onWatchResponse(response: WatchResponse) {
        // ONLY handle responses of class 9 (W); those are very few
        if (response is WatchWControlDownloadCommand.Response || response is WatchWDeleteWatchDialCommand.Response || response is WatchWGetWatchDialInfoCommand.Response || response is WatchWNextDownloadChunkMetaCommand.Response || response is WatchWSetCurrentWatchDialCommand.Response) {

            val result = responseChannel.trySend(response)
            if (result.isFailure) {
                Log.e(TAG, "Failed to send ${response::class.simpleName}: ${result.exceptionOrNull()}")
                responseChannel.close()
            }

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

    private fun setProgress(percentage: Float, text: String) {
        progresser(percentage, text)
    }

    suspend fun listWatchFaces(): WatchResponse {
        setProgress(0f, "Getting watch face list...")
        binder.getWatchDial()
        return receive(WatchWGetWatchDialInfoCommand.Response::class)
    }

    suspend fun selectWatchFace(dialPlateId: Int): WatchResponse {
        setProgress(0f, "Activating watch face...")
        binder.selectWatchFace(dialPlateId)
        return receive(WatchWSetCurrentWatchDialCommand.Response::class)
    }

    suspend fun deleteWatchFace(dialPlateId: Int): WatchResponse {
        setProgress(0f, "Deleting watch face...")
        binder.deleteWatchDial(dialPlateId)
        return receive(WatchWDeleteWatchDialCommand.Response::class)
    }

    private suspend fun startWatchFaceDownload(length: UInt, dialPlateId: Int, blockNumber: Short, version: Short, crc: UShort): WatchResponse {
        setProgress(0f, "Initializing download...")
        binder.startWatchFaceDownload(length, dialPlateId, blockNumber, version, crc)
        return receive(WatchWControlDownloadCommand.Response::class)
    }

    private suspend fun stopWatchFaceDownload(length: UInt): WatchResponse {
        setProgress(0f, "Finalizing...")
        binder.stopWatchFaceDownload(length)
        return receive(WatchWControlDownloadCommand.Response::class)
    }

    private suspend fun nextWatchFaceDownloadChunkMeta(deltaOffset: Int, packetCount: UShort, crc: UShort): WatchResponse {
        // setProgress("nextWatchFaceDownloadChunkMeta $deltaOffset $packetCount $crc")
        binder.nextWatchFaceDownloadChunkMeta(deltaOffset, packetCount, crc)
        return receive(WatchWNextDownloadChunkMetaCommand.Response::class)
    }

    /** Give the specified watchface BODY to the watch */
    suspend fun downloadWatchface(mtu: Byte, dialPlateId: Int, blockNumber: Short, version: Short, body: ByteArray) = coroutineScope {
        val metaChunkSize = 4096 // Byte
        val totalSize = body.size
        
        setProgress(0f, "Starting download...")
        val startResponse = startWatchFaceDownload(body.size.toUInt(), dialPlateId, blockNumber, version, Crc16.crc16(body).toUShort())
        if (startResponse !is WatchWControlDownloadCommand.Response) {
            throw Exception("Starting download failed: wrong response type")
        } else if (startResponse.control != 1.toByte() || startResponse.errorCode != 0.toByte()) {
            throw Exception("Starting download failed: control=${startResponse.control}, error=${startResponse.errorCode}")
        }
        
        val chunks = body.iterator().asSequence().chunked(metaChunkSize)
        val totalChunks = chunks.count()
        var bytesTransferred = 0
        
        chunks.forEachIndexed { chunkIndex, chunk ->
            val overallProgress = (bytesTransferred.toFloat() / totalSize * 100f).toInt()            
            setProgress(overallProgress.toFloat(), "Sending chunk ${chunkIndex + 1} of $totalChunks...")
            
            // Send MTU chunks
            // MTU chunking: Protocol overhead (6 bytes) + ATT overhead (3 bytes) = 9 bytes total
            var packageCount: UShort = 0U
            chunk.chunked(mtu.toInt() - 9).forEach { packageChunk ->
                binder.sendWatchFaceDownloadChunk(packageChunk.toByteArray())
                ++packageCount
                delay(10) // Small delay of 10ms between chunks
            }
            
            // Use actual chunk size, not fixed metaChunkSize
            val actualChunkSize = chunk.size
            
            setProgress(overallProgress.toFloat(), "Verifying chunk ${chunkIndex + 1}...")
            val verifyResponse = nextWatchFaceDownloadChunkMeta(actualChunkSize, packageCount, Crc16.crc16(chunk.toByteArray()).toUShort())
            if (verifyResponse !is WatchWNextDownloadChunkMetaCommand.Response || verifyResponse.status != 0.toByte()) {
                throw Exception("Watch did not accept chunk ${chunkIndex + 1}")
            }
            
            bytesTransferred += actualChunkSize
            val updatedProgress = (bytesTransferred.toFloat() / totalSize * 100f).toInt()
            setProgress(updatedProgress.toFloat(), "Chunk ${chunkIndex + 1} verified")
        }
        
        setProgress(100f, "Finalizing download...")
        val stopResponse = stopWatchFaceDownload(body.size.toUInt())
        if (stopResponse !is WatchWControlDownloadCommand.Response) {
            throw Exception("Stopping download failed: wrong response type")
        } else if (stopResponse.control != 0.toByte() || stopResponse.errorCode != 0.toByte()) {
            throw Exception("Stopping download failed: control=${stopResponse.control}, error=${stopResponse.errorCode}")
        }
        
        setProgress(100f, "Download completed!")
    }
}