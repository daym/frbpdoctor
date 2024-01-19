package com.friendly_machines.frbpdoctor.watchprotocol.command

import com.friendly_machines.frbpdoctor.watchprotocol.WatchOperation

class WatchGetBpDataCommand : WatchCommand(WatchOperation.GetBpData, ByteArray(0)) // (big)