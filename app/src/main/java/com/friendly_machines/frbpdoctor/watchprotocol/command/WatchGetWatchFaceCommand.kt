package com.friendly_machines.frbpdoctor.watchprotocol.command

import com.friendly_machines.frbpdoctor.watchprotocol.WatchOperation

class WatchGetWatchFaceCommand : WatchCommand(WatchOperation.GetWatchFace, ByteArray(0))