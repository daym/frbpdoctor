package com.friendly_machines.fr_yhe_api.commondata

enum class MessageType(val code: Byte) {
    PhoneCall(0),
    SMS(1),
    Email(2),
    WeChat(3),
    QQ(4),
    SinaWeibo(5),
    Facebook(6),
    Twitter(7),
    Messenger(8),
    WhatsApp(9),
    LinkedIn(10),
    Instagram(11),
    Skype(12),
    Line(13),
    Snapchat(14),
    Telegram(15),
    Zoom(16),
    TikTok(17),
    KakaoTalk(18),
    Viber(19),
    Other(20);

    companion object {
        fun parse(code: Byte): MessageType? {
            return values().find { it.code == code }
        }
    }
}