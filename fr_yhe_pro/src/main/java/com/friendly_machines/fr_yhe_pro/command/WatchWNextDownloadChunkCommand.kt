package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_pro.WatchOperation

class WatchWNextDownloadChunkCommand(chunk: ByteArray) : WatchCommand(WatchOperation.WNextDownloadChunk, chunk) {
    // No Response class - chunks don't get responses from watch
}
