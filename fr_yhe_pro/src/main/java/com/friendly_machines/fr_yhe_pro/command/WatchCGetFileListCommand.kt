package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchCGetFileListCommand(x: Short, y: Short) : WatchCommand(WatchOperation.CGetFileList, run {
    val buf = ByteBuffer.allocate(2 + 2)
    buf.putShort(x)
    buf.putShort(y)
    buf.array()
})