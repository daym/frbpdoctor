package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_pro.WatchOperation

// SAlarm is used in multiple commands
class WatchSGetAllAlarmsCommand : WatchCommand(WatchOperation.SAlarm, ByteArray(1))