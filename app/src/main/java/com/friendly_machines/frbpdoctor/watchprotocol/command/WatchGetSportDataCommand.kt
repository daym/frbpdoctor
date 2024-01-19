package com.friendly_machines.frbpdoctor.watchprotocol.command

import com.friendly_machines.frbpdoctor.watchprotocol.WatchOperation

class WatchGetSportDataCommand : WatchCommand(WatchOperation.GetSportData, ByteArray(0)) // (big)