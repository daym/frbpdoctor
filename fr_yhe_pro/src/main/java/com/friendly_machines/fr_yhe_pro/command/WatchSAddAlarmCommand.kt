package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_pro.WatchOperation

// SAlarm is used in multiple commands
// x and b can be 0 (and usually are).
class WatchSAddAlarmCommand(x: Byte, y: Byte, z: Byte, a: Byte, b: Byte) : WatchCommand(WatchOperation.SAlarm, byteArrayOf(1.toByte(), x, y, z, a, b))