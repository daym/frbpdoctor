package com.friendly_machines.frbpdoctor.watchprotocol.command

import com.friendly_machines.frbpdoctor.watchprotocol.WatchOperation

open class WatchCommand(val operation: WatchOperation, val arguments: ByteArray)