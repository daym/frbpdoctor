package com.friendly_machines.fr_yhe_pro.command
import com.friendly_machines.fr_yhe_pro.WatchOperation

//  These is the response we send to the watch to a watch-initiated indication.
class WatchDSleepReminderCommand(ok: Boolean) : WatchCommand(WatchOperation.DSleepReminder, byteArrayOf(if (ok) { 0 } else { 1 }))