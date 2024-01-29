package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_pro.WatchOperation

// SAlarm is used in multiple commands
class WatchSDeleteAlarmCommand(val x: Byte, val y: Byte) : WatchCommand(WatchOperation.SAlarm, byteArrayOf(2.toByte(), x, y))