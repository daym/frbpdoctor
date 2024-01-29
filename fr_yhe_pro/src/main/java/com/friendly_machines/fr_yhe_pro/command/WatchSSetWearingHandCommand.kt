package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_pro.WatchOperation

// hand: 0 left; 1 right
class WatchSSetWearingHandCommand(val hand: Byte) : WatchCommand(WatchOperation.SSetWearingHand, byteArrayOf(hand))