package com.friendly_machines.fr_yhe_api.commondata
// FIXME Maybe

/**
 * Push message types for watch notifications.
 * These correspond to different app notification types that can be pushed to the watch.
 * FIXME: There's also a MessageTypeMed, but it has fewer and differently-valued variants.
 */
enum class PushMessageType(val value: Byte) {
    PHONE(0),          // Phone calls
    SMS(1),            // SMS messages  
    EMAIL(2),          // Email
    WECHAT(3),         // WeChat
    QQ(4),             // QQ messaging
    SINA_WEIBO(5),     // Sina Weibo
    FACEBOOK(6),       // Facebook
    TWITTER(7),        // Twitter
    MESSENGER(8),      // Facebook Messenger
    WHATSAPP(9),       // WhatsApp
    LINKEDIN(10),      // LinkedIn
    INSTAGRAM(11),     // Instagram
    SKYPE(12),         // Skype
    LINE(13),          // Line messenger
    SNAPCHAT(14),      // Snapchat
    TELEGRAM(15),      // Telegram
    ZOOM(16),          // Zoom
    TIKTOK(17),        // TikTok
    KAKAOTALK(18),     // KakaoTalk
    VIBER(19),         // Viber
    OTHER(20)          // Other apps
}