package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_pro.WatchOperation

class WatchGGetRealBloodOxygenCommand : WatchCommand(WatchOperation.GGetRealBloodOxygen, "IS".toByteArray(Charsets.US_ASCII))