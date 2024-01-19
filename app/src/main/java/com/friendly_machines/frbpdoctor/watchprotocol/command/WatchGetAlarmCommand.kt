package com.friendly_machines.frbpdoctor.watchprotocol.command

import com.friendly_machines.frbpdoctor.watchprotocol.WatchOperation

class WatchGetAlarmCommand : WatchCommand(WatchOperation.GetAlarm, ByteArray(0)) // (big)