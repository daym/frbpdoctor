package com.friendly_machines.fr_yhe_api.watchprotocol

enum class WatchProfileSex(val code: Byte) {
    Female(0),
    Male(1);

    companion object {
        fun parse(code: Byte): WatchProfileSex? {
            return when (code) {
                0.toByte() -> Female
                1.toByte() -> Male
                else -> null
            }
        }
    }
}