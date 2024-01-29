package com.friendly_machines.fr_yhe_api.commondata

enum class MessageTypeMed(val code: Byte) {
    Wechat(1),
    Qq(2),
    Sms(3),
    NewCall(4),
    HungUpCall(5),
    MissedCall(7),
    Facebook(12),
    Instagram(13),
    Line(14),
    Messenger(15),
    Snapchat(16),
    Twitter(17),
    Viber(18),
    Whatsapp(19);
    companion object {
        fun parse(code: Byte): MessageTypeMed? {
            return values().find { it.code == code }
        }
    }
}