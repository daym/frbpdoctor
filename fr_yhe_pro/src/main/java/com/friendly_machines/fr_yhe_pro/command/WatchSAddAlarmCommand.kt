package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_pro.WatchOperation

// SAlarm is used in multiple commands
// x and b can be 0 (and usually are).
class WatchSAddAlarmCommand(val x: Byte, val y: Byte, val z: Byte, val a: Byte, val b: Byte) : WatchCommand(WatchOperation.SAlarm, byteArrayOf(1.toByte(), x, y, z, a, b))