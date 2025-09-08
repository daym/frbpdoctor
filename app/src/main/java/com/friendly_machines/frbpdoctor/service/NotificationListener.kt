package com.friendly_machines.frbpdoctor.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchMessageEncodingException
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponseType
import com.friendly_machines.frbpdoctor.WatchCommunicationClientShorthand

// Note: Service dependency on WatchCommunicationService
class NotificationListener : NotificationListenerService() {
    companion object {
        private const val TAG = "NotificationListener"
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
            if (notification.flags and Notification.FLAG_LOCAL_ONLY != 0) {
                return
            }
            if (notification.visibility != Notification.VISIBILITY_SECRET) {
                Log.i(TAG, "Notification Posted: " + sbn.packageName)
                WatchCommunicationClientShorthand.bindExecOneCommandUnbind(this, WatchResponseType.SetMessage) { binder ->
                    try {
                        // TODO what if the call was hung up by the user via the phone?
                        // TODO if there is a new call, keep the WatchCommunicationService alive for a while so we can send further messages (like hangup) to the watch.
                        binder.setMessage(
                            when (notification.category) {
                                Notification.CATEGORY_CALL -> com.friendly_machines.fr_yhe_api.commondata.MessageTypeMed.NewCall
                                Notification.CATEGORY_MISSED_CALL -> com.friendly_machines.fr_yhe_api.commondata.MessageTypeMed.MissedCall
                                Notification.CATEGORY_NAVIGATION -> com.friendly_machines.fr_yhe_api.commondata.MessageTypeMed.Qq
                                Notification.CATEGORY_ALARM -> com.friendly_machines.fr_yhe_api.commondata.MessageTypeMed.Qq
                                Notification.CATEGORY_REMINDER -> com.friendly_machines.fr_yhe_api.commondata.MessageTypeMed.Qq
                                Notification.CATEGORY_STOPWATCH -> com.friendly_machines.fr_yhe_api.commondata.MessageTypeMed.Qq
                                Notification.CATEGORY_EVENT -> com.friendly_machines.fr_yhe_api.commondata.MessageTypeMed.Facebook
                                Notification.CATEGORY_MESSAGE -> com.friendly_machines.fr_yhe_api.commondata.MessageTypeMed.Messenger
                                Notification.CATEGORY_EMAIL -> com.friendly_machines.fr_yhe_api.commondata.MessageTypeMed.Messenger
                                Notification.CATEGORY_SOCIAL -> com.friendly_machines.fr_yhe_api.commondata.MessageTypeMed.Facebook
                                Notification.CATEGORY_WORKOUT -> com.friendly_machines.fr_yhe_api.commondata.MessageTypeMed.Instagram
                                Notification.CATEGORY_LOCATION_SHARING -> com.friendly_machines.fr_yhe_api.commondata.MessageTypeMed.Qq
                                Notification.CATEGORY_TRANSPORT -> com.friendly_machines.fr_yhe_api.commondata.MessageTypeMed.Qq
                                //Notification.CATEGORY_SYSTEM -> reserved
                                else -> com.friendly_machines.fr_yhe_api.commondata.MessageTypeMed.Messenger
                            }, time.toInt(), title, text
                        )
                    } catch (e: WatchMessageEncodingException) {
                        binder.setMessage(com.friendly_machines.fr_yhe_api.commondata.MessageTypeMed.Line, time.toInt(), "message too long", "message too long")
                    }
                }
            }
        } catch (e: RuntimeException) {
            Log.e(TAG, "Notification error: $e")
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
    }
}