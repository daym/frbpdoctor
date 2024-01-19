package com.friendly_machines.frbpdoctor.watchprotocol.command

import com.friendly_machines.frbpdoctor.watchprotocol.WatchOperation

class WatchCurrentHeatCommand : WatchCommand(WatchOperation.CurrentHeat, ByteArray(0)) // (big)