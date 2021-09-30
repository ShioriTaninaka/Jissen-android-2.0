package com.newit.school_guide.feat.firebase

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.newit.school_guide.core.api.network
import com.newit.school_guide.core.common.CommonSharedPreferences
import com.newit.school_guide.core.common.Constants
import com.newit.school_guide.core.common.Logger
import com.newit.school_guide.core.common.NotificationUtils
import com.newit.school_guide.feat.model.MessageEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus


class FirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)

        var mapData = p0.data
        var notification = p0.notification

//        Logger.d("onMessageReceived:" + mapData["type"])
//        for (key in mapData.keys) {
//            Logger.d("type_push " + key + ":" + mapData.get(key))
//        }
        var pushId :String?=null
        if(mapData.isNotEmpty()){
            pushId = mapData["id"]
        }
        if (notification != null) {
            var notificationUtils = NotificationUtils(applicationContext)
            notificationUtils.sendNotification(
                notification.title ?: "titleDefault",
                notification.body ?: "bodyDefault",
                pushId
            )
//            val intent = Intent("msg") //action: "msg"
//            intent.setPackage(packageName)
//            intent.putExtra("message", "")
//            applicationContext.sendBroadcast(intent)
            EventBus.getDefault().post(MessageEvent())
        }
    }

    override fun onNewToken(token: String) {
        Logger.d("Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        CommonSharedPreferences.getInstance().putBoolean(Constants.IS_NEW_TOKEN,true)
        CommonSharedPreferences.getInstance().putString(Constants.FCM_TOKEN,token)
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val res = network().postToken(token)
                if (res.statusCode == 1) {
                    Logger.d("post_token success")
                    CommonSharedPreferences.getInstance().putBoolean(Constants.IS_NEW_TOKEN,false)
                }
            } catch (e: Exception) {
                Logger.d("post_token fails :" + e.message)
            }

        }
    }
}