package com.example.citycatch.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.citycatch.EntryPointActivity
import com.example.citycatch.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MessagingService: FirebaseMessagingService() {

    private val channelId = "Notifications"
    private val channelName = "com.example.citycatch"

    override fun onMessageReceived(message: RemoteMessage) {
        Log.i("TAG NOTIFY", "Received")
        if(message.notification != null){ generateNotification() }
    }

    private fun generateNotification(){
        Log.i("TAG NOTIFY", "Notification")
        val intent= Intent(this, EntryPointActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendinIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.logo)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendinIntent)

        builder.setContentTitle("CityCatch: New Place")
            .setContentText("A new Place has been added, be the first one to take a Photo!!")

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        notificationManager.notify(0, builder.build())
    }

}