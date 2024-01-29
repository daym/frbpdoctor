package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_pro.WatchOperation

class WatchCGetFileCountCommand : WatchCommand(WatchOperation.CGetFileCount, byteArrayOf(1))
