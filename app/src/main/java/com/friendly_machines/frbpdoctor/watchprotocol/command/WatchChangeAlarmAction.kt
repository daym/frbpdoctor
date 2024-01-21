package com.friendly_machines.frbpdoctor.watchprotocol.command

enum class WatchChangeAlarmAction(val code: Byte) {
    Edit(0),
    Add(1);
}
