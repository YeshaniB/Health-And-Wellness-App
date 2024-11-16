package com.example.diamellitusapp.Utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.app.PendingIntent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.diamellitusapp.R
import com.example.diamellitusapp.TaskList

class AlarmReciving:  BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
            // Check for POST_NOTIFICATIONS permission before displaying the notification
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted, create and display the notification
                createNotification(context)
            } else {
                // Handle the case where the permission is not granted (you could request it or notify the user)
                Toast.makeText(context, "Notification permission required", Toast.LENGTH_SHORT).show()
            }
    }

    @SuppressLint("MissingPermission")
    private fun createNotification(context: Context) {
        // Notification channel ID
        val channelId = "alarm_channel"

        // Set the sound for the notification
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        // Build the notification
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.baseline_alarm) // Use your app icon or a custom one
            .setContentTitle("It's Your Reminder")
            .setContentText("It's time to complete your task!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)  // Automatically remove the notification when clicked
            .setSound(alarmSound)  // Set alarm sound

        //open the TaskList
        val notificationIntent = Intent(context, TaskList::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        builder.setContentIntent(pendingIntent)

        // Display the notification
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(1, builder.build())
        }

}
