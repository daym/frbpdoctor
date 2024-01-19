package com.friendly_machines.frbpdoctor.watchprotocol.command

import com.friendly_machines.frbpdoctor.watchprotocol.WatchOperation

class WatchCurrentStepCommand : WatchCommand(WatchOperation.CurrentStep, ByteArray(0)) // (big)