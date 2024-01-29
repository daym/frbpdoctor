package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_pro.WatchOperation

class WatchSSetScheduleSwitchCommand(val enabled: Boolean) : WatchCommand(WatchOperation.SSetScheduleSwitch, byteArrayOf(if (enabled) 1.toByte() else 0.toByte()))