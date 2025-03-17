package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_pro.WatchOperation

class WatchAGetRealData(x: Byte, i: Byte /* 1...5 */, z: Byte): WatchCommand(WatchOperation.ARealData, byteArrayOf(x, i, z))