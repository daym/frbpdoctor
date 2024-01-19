package com.friendly_machines.frbpdoctor.watchprotocol.command

import com.friendly_machines.frbpdoctor.watchprotocol.WatchOperation

// Example: id=91; voltage=4072
class WatchGetBatteryStateCommand : WatchCommand(WatchOperation.GetBatteryState, ByteArray(0))