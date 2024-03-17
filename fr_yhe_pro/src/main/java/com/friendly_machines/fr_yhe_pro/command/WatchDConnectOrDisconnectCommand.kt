package com.friendly_machines.fr_yhe_pro.command
import com.friendly_machines.fr_yhe_pro.WatchOperation

//  These is the response we send to the watch to a watch-initiated indication.
class WatchDConnectOrDisconnectCommand(ok: Boolean) : WatchCommand(WatchOperation.DConnectOrDisconnect, byteArrayOf(if (ok) { 0 } else { 1 }))