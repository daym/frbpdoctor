package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_pro.WatchOperation

class WatchSSetUploadReminderCommand(val enabled: Boolean, val x: Byte) : WatchCommand(WatchOperation.SSetUploadReminder, byteArrayOf(if (enabled) 1.toByte() else 0.toByte(), x))