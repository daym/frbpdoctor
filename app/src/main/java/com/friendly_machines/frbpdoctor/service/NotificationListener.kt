package com.friendly_machines.frbpdoctor.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.friendly_machines.frbpdoctor.WatchCommunicationServiceClientShorthand
import com.friendly_machines.frbpdoctor.watchprotocol.WatchMessageEncodingException
import com.friendly_machines.frbpdoctor.watchprotocol.notification.WatchResponse
import com.friendly_machines.frbpdoctor.watchprotocol.notification.big.MessageType

// Note: Service dependency on WatchCommunicationService
class NotificationListener : NotificationListenerService() {
    companion object {
        private const val TAG = "NotificationListener"
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        try {
            val notification = sbn.notification
            val time = notification.`when`
            var title = notification.extras.getString(Notification.EXTRA_TITLE)
            var text = notification.extras.getString(Notification.EXTRA_TEXT)
            if (title == null) {
                title = notification.tickerText.toString()
            }
            if (text == null) {
                text = notification.toString()
            }
            if (notification.category == Notification.CATEGORY_SERVICE || notification.category == Notification.CATEGORY_ERROR || notification.category == Notification.CATEGORY_PROGRESS || notification.category == Notification.CATEGORY_PROMO || notification.category == Notification.CATEGORY_RECOMMENDATION || notification.category == Notification.CATEGORY_STATUS) {
                return
            }
            if (notification.visibility != Notification.VISIBILITY_SECRET) {
                Log.i(TAG, "Notification Posted: " + sbn.packageName)
                WatchCommunicationServiceClientShorthand.bindExecOneCommandUnbind(this, WatchResponse.SetMessage(0)) { binder ->
                    try {
                        binder.setMessage(
                            when (notification.category) {
                                Notification.CATEGORY_CALL -> MessageType.NewCall
                                Notification.CATEGORY_MISSED_CALL -> MessageType.MissedCall
                                Notification.CATEGORY_NAVIGATION -> MessageType.Qq
                                Notification.CATEGORY_ALARM -> MessageType.Qq
                                Notification.CATEGORY_REMINDER -> MessageType.Qq
                                Notification.CATEGORY_STOPWATCH -> MessageType.Qq
                                Notification.CATEGORY_EVENT -> MessageType.Facebook
                                Notification.CATEGORY_MESSAGE -> MessageType.Messenger
                                Notification.CATEGORY_EMAIL -> MessageType.Messenger
                                Notification.CATEGORY_SOCIAL -> MessageType.Facebook
                                Notification.CATEGORY_WORKOUT -> MessageType.Instagram
                                Notification.CATEGORY_LOCATION_SHARING -> MessageType.Qq
                                Notification.CATEGORY_TRANSPORT -> MessageType.Qq
                                //Notification.CATEGORY_SYSTEM -> reserved
                                else -> MessageType.Messenger
                            }, time.toInt(), title, text)
                    } catch (e: WatchMessageEncodingException) {
                        binder.setMessage(MessageType.Line, time.toInt(),"message too long", "message too long")
                    }
                }
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Notification error: $e")
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
    }
}