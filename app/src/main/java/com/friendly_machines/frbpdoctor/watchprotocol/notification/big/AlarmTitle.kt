package com.friendly_machines.frbpdoctor.watchprotocol.notification.big

enum class AlarmTitle(val code: Byte) {
    GetUp(0), WorkingDay(1), Anniversary(2), Meeting(3), TakeMedicine(4), Appointment(5);
    companion object {
        fun parse(code: Byte): AlarmTitle? {
            return AlarmTitle.values().find { it.code == code }
        }
    }
}