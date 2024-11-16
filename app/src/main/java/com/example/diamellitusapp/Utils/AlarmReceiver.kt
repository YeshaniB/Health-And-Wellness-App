package com.example.diamellitusapp.Utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.diamellitusapp.R

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        var mediaPlayer: MediaPlayer? = null
    }
    private val CHANNEL_ID = "alarm_channel"

    override fun onReceive(context: Context, intent: Intent) {
        // Create a notification to alert the user
        createNotificationChannel(context)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_alarm) // Replace with your own icon
            .setContentTitle("Alarm")
            .setContentText("It's time for your reminder!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            //notify(1, builder.build())
        }

        // Optionally, play a sound
        playAlarmSound(context)
    }

    /**
     * Creates a notification channel for devices running Android O and above.
     *
     * @param context The context.
     */
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Alarm Channel"
            val descriptionText = "Channel for Alarm Manager"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Plays an alarm sound when the alarm is triggered.
     *
     * @param context The context.
     */
    private fun playAlarmSound(context: Context) {
        try {
            val mediaPlayer = MediaPlayer.create(context, R.raw.alarm_sound) // Place your alarm sound in res/raw/alarm_sound.mp3
            //------------------------------mediaPlayer.isLooping = true
            mediaPlayer.isLooping = true
            mediaPlayer.start()

            // Stop the sound after some time (e.g., 10 seconds)
            // You can implement a more robust solution as needed
            android.os.Handler().postDelayed({
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                    mediaPlayer.release()
                }
            }, 60000) // Stops the sound after 60,000 milliseconds (60 seconds)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopAlarmSound() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }
}