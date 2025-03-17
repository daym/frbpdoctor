package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_pro.WatchOperation

class WatchAShutdown(x: Byte/*3*/): WatchCommand(WatchOperation.AShutdown, byteArrayOf(x))