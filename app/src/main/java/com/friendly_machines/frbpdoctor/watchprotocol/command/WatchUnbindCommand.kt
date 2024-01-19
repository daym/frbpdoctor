package com.friendly_machines.frbpdoctor.watchprotocol.command

import com.friendly_machines.frbpdoctor.watchprotocol.WatchOperation

class WatchUnbindCommand : WatchCommand(WatchOperation.Unbind, ByteArray(0))