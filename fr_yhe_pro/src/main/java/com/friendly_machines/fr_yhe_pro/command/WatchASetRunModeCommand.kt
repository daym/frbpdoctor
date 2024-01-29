package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_pro.WatchOperation

class WatchASetRunModeCommand(val key: Byte, val value: Byte) : WatchCommand(WatchOperation.ASetRunMode, byteArrayOf(key, value))