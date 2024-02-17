package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_pro.WatchOperation

class WatchASetRunModeCommand(key: Byte, value: Byte) : WatchCommand(WatchOperation.ASetRunMode, byteArrayOf(key, value))