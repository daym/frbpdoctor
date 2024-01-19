package com.friendly_machines.frbpdoctor.watchprotocol.command

import com.friendly_machines.frbpdoctor.watchprotocol.WatchOperation

class WatchGetHeatDataCommand : WatchCommand(WatchOperation.GetHeatData, ByteArray(0)) // (big)