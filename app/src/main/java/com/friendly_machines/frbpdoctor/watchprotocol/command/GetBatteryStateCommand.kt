package com.friendly_machines.frbpdoctor.watchprotocol.command

// Example: id=91; voltage=4072
class GetBatteryStateCommand : WatchCommand(42, ByteArray(0))