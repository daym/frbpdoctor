package com.friendly_machines.frbpdoctor.watchprotocol.command

import com.friendly_machines.frbpdoctor.watchprotocol.WatchOperation

class WatchGetStepDataCommand : WatchCommand(WatchOperation.GetStepData, ByteArray(0)) // (big)