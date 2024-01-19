package com.friendly_machines.frbpdoctor.watchprotocol.command

import com.friendly_machines.frbpdoctor.watchprotocol.WatchOperation

class WatchGetDeviceConfigCommand : WatchCommand(WatchOperation.GetDeviceConfig, ByteArray(0)) // (big)