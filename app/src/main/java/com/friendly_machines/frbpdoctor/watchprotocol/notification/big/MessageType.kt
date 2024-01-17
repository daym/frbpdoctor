package com.friendly_machines.frbpdoctor.watchprotocol.notification.big

enum class MessageType(val code: Byte) {
    Wechat(1),
    QQ(2),
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
        fun parse(code: Byte): MessageType? {
            return values().find { it.code == code }
        }
    }
}