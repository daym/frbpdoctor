package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_pro.WatchOperation

class WatchGGetUserConfigCommand : WatchCommand(WatchOperation.GGetUserConfig, "CF".toByteArray(charset = Charsets.US_ASCII))