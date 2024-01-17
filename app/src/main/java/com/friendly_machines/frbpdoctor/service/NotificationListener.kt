package com.friendly_machines.frbpdoctor.service

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.friendly_machines.frbpdoctor.watchprotocol.notification.big.MessageType

class NotificationListener : NotificationListenerService() {
    companion object {
        private const val TAG = "NotificationListener"
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        Log.i(TAG, "Notification Posted: " + sbn.packageName)
        val serviceConnection = object : ServiceConnection {
            //private var disconnector: WatchCommunicationService? = null
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val notification = sbn.notification
                val time = notification.`when`
                val binder = service as WatchCommunicationService.WatchCommunicationServiceBinder
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

            override fun onServiceDisconnected(name: ComponentName?) {
                //handler.removeCallbacksAndMessages(null);
                //disconnector!!.removeListener(this@MainActivity)
            }
        }
        val serviceIntent = Intent(this, WatchCommunicationService::class.java)
        if (!bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)) {
            Log.e(TAG, "Could not bind to WatchCommunicationService")
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        Log.i(TAG, "Notification Removed: " + sbn.packageName)
    }
}