package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_pro.WatchOperation

class WatchGGetDeviceInfoCommand : WatchCommand(WatchOperation.GGetDeviceInfo, "GC".toByteArray(Charsets.US_ASCII))
