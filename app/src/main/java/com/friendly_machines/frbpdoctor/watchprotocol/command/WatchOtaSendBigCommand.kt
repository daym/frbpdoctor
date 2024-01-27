package com.friendly_machines.frbpdoctor.watchprotocol.command

import com.friendly_machines.frbpdoctor.watchprotocol.WatchOperation

// See also sendInternal3
class WatchOtaSendBigCommand: WatchCommand(WatchOperation.OtaSendBig, ByteArray(0))