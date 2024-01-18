package com.friendly_machines.frbpdoctor.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import com.friendly_machines.frbpdoctor.WatchCommunicationServiceClientShorthand
import com.friendly_machines.frbpdoctor.watchprotocol.bluetooth.WatchListener
import com.friendly_machines.frbpdoctor.watchprotocol.notification.WatchResponse
import com.friendly_machines.frbpdoctor.watchprotocol.notification.big.MessageType

// TODO service dependency on WatchCommunicationService
class NotificationListener : NotificationListenerService() {
    companion object {
        private const val TAG = "NotificationListener"
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        Log.i(TAG, "Notification Posted: " + sbn.packageName)
        WatchCommunicationServiceClientShorthand.bindExecOneCommandUnbind(this, WatchResponse.SetMessage(0)) { binder ->
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
            if (notification.visibility != Notification.VISIBILITY_SECRET) {
                binder.setMessage(
                    when (notification.category) {
                        Notification.CATEGORY_CALL -> MessageType.NewCall
                        Notification.CATEGORY_MISSED_CALL -> MessageType.MissedCall
                        Notification.CATEGORY_NAVIGATION -> MessageType.QQ
                        Notification.CATEGORY_ALARM -> MessageType.QQ
                        Notification.CATEGORY_REMINDER -> MessageType.QQ
                        Notification.CATEGORY_STOPWATCH -> MessageType.QQ
                        Notification.CATEGORY_EVENT -> MessageType.Facebook
                        Notification.CATEGORY_PROGRESS -> MessageType.QQ // TODO filter out
                        Notification.CATEGORY_MESSAGE -> MessageType.Messenger
                        Notification.CATEGORY_EMAIL -> MessageType.Messenger
                        Notification.CATEGORY_PROMO -> MessageType.Facebook // TODO filter out
                        Notification.CATEGORY_RECOMMENDATION -> MessageType.Facebook // TODO filter out
                        Notification.CATEGORY_STATUS -> MessageType.QQ // TODO maybe filter out
                        Notification.CATEGORY_SOCIAL -> MessageType.Facebook
                        Notification.CATEGORY_WORKOUT -> MessageType.Instagram
                        Notification.CATEGORY_LOCATION_SHARING -> MessageType.QQ
                        Notification.CATEGORY_SERVICE -> MessageType.QQ // TODO filter out since it could cause an endless loop (we ourselves use a service to send out to the watch)
                        Notification.CATEGORY_ERROR -> MessageType.QQ // TODO filter out since it could cause an endless loop (we ourselves use a service to send out to the watch)
                        Notification.CATEGORY_TRANSPORT -> MessageType.QQ
                        //Notification.CATEGORY_SYSTEM -> reserved
                        else -> MessageType.Messenger
                    }, time.toInt(), title, text
                )
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        Log.d(TAG, "Notification Removed: " + sbn.packageName)
    }
}