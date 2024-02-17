package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_pro.WatchOperation

class WatchAPushCallStateCommand(state: Byte) : WatchCommand(WatchOperation.APushCallState, byteArrayOf(state))