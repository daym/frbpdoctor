package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_pro.WatchOperation

class WatchGGetMacAddressCommand : WatchCommand(WatchOperation.GGetMacAddress, ByteArray(0))