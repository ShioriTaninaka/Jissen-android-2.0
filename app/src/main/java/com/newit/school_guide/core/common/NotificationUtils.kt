package com.newit.school_guide.core.common

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.newit.school_guide.MainActivity
import com.newit.school_guide.R
import com.newit.school_guide.core.base.BaseApplication
import com.newit.school_guide.feat.SplashActivity
import kotlin.random.Random

class NotificationUtils(private val mContext: Context) {

    fun sendNotification(title : String,messageBody: String,pushId : String?) {
        val intent = Intent(mContext, SplashActivity::class.java)
        intent.putExtra("isNotify",true)
//        intent.addFlags(  Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra("id",pushId)
        val pendingIntent = PendingIntent.getActivity(mContext, 0 /* Request code */, intent,
            PendingIntent.FLAG_UPDATE_CURRENT)

        val channelId = mContext.getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(mContext, channelId)
            .setSmallIcon(R.drawable.ic_notification)
//            .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher))
            .setColor(ContextCompat.getColor(mContext, R.color.colorPrimary))
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                "Channel school guide",
                NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(Random.nextInt(1000) /* ID of notification */, notificationBuilder.build())
    }
}