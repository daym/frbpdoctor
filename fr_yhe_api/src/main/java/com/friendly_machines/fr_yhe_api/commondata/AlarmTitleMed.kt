package com.friendly_machines.fr_yhe_api.commondata

enum class AlarmTitleMed(val code: Byte) {
    GetUp(0), WorkingDay(1), Anniversary(2), Meeting(3), TakeMedicine(4), Appointment(5);
    companion object {
        fun parse(code: Byte): AlarmTitleMed? {
            return AlarmTitleMed.values().find { it.code == code }
        }
    }
//    override fun toString(): String {
//        return displayText
//    }

}