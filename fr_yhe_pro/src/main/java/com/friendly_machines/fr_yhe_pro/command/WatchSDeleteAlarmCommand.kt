package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_pro.WatchOperation

// SAlarm is used in multiple commands
class WatchSDeleteAlarmCommand(x: Byte, y: Byte) : WatchCommand(WatchOperation.SAlarm, byteArrayOf(2.toByte(), x, y))