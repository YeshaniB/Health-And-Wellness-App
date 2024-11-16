package com.example.diamellitusapp

import android.Manifest
import android.content.pm.PackageManager
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar
import android.app.TimePickerDialog
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.diamellitusapp.Utils.AlarmReceiver

class SetReminder : AppCompatActivity() {

    private lateinit var selectTimeTextView: TextView
    private lateinit var selectTimeButton: Button
    private lateinit var setAlarmButton: Button
    private lateinit var cancelAlarmButton: Button
    private lateinit var stopAlarmbtn: Button

    private var alarmTimeInMillis: Long = 0 // Stores the selected alarm time in milliseconds

    // Launcher for requesting the POST_NOTIFICATIONS permission
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
            // You can proceed with sending notifications or setting alarms that rely on notifications
        } else {
            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
            // Inform the user that notifications won't be available
        }
    }





    private val selectedHour = 0
    private val selectedMinute = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_reminder)

        // Initialize Views
        selectTimeTextView = findViewById(R.id.selecttime)
        selectTimeButton = findViewById(R.id.selecttimebtn)
        setAlarmButton = findViewById(R.id.setalarmbtn)
        cancelAlarmButton = findViewById(R.id.cancelalarmbtn)
        stopAlarmbtn = findViewById(R.id.stopAlarmbtn)

        // Set OnClickListener for Select Time Button
        selectTimeButton.setOnClickListener {
            showTimePicker()
        }

        // Set OnClickListener for Set Alarm Button
        /*setAlarmButton.setOnClickListener {
            if (alarmTimeInMillis == 0L) {
                Toast.makeText(this, "Please select a time first", Toast.LENGTH_SHORT).show()
            } else {
                setAlarm(alarmTimeInMillis)
            }
        }*/

        setAlarmButton.setOnClickListener {
            if (alarmTimeInMillis == 0L) {
                Toast.makeText(this, "Please select a time first", Toast.LENGTH_SHORT).show()
            } else {
                // Check and request notification permission before setting the alarm
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API level 33+
                    when {
                        ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED -> {
                            // Permission is already granted
                            setAlarm(alarmTimeInMillis)
                        }
                        ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) -> {
                            // In a real app, you should explain to the user why you need the permission
                            Toast.makeText(
                                this,
                                "Notifications are needed to remind you at the set time.",
                                Toast.LENGTH_LONG
                            ).show()
                            // Request permission
                            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        else -> {
                            // Directly ask for the permission
                            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                } else {
                    // For Android versions below 13, no need to request permission
                    setAlarm(alarmTimeInMillis)
                }
            }
        }


        // Set OnClickListener for Cancel Alarm Button
        cancelAlarmButton.setOnClickListener {
            cancelAlarm()
        }

        stopAlarmbtn.setOnClickListener {
            AlarmReceiver().stopAlarmSound()
            Toast.makeText(this, "Alarm stopped", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Displays a TimePickerDialog for the user to select a time.
     * After selecting, updates the TextView and stores the alarm time in milliseconds.
     */
    private fun showTimePicker() {
        val currentTime = Calendar.getInstance()
        val hour = currentTime.get(Calendar.HOUR_OF_DAY)
        val minute = currentTime.get(Calendar.MINUTE)

        val timePicker = TimePickerDialog(this@SetReminder,
            { _, selectedHour, selectedMinute ->
                // Set selected time in TextView
                val period = if (selectedHour >= 12) "PM" else "AM"
                val formattedHour = if (selectedHour == 0 || selectedHour == 12) 12 else selectedHour % 12
                val formattedMinute = String.format("%02d", selectedMinute)
                val formattedTime = "$formattedHour : $formattedMinute $period"
                selectTimeTextView.text = formattedTime

                // Calculate the time in milliseconds for the alarm
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                calendar.set(Calendar.MINUTE, selectedMinute)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                // If the selected time is before the current time, set it for the next day
                if (calendar.timeInMillis <= System.currentTimeMillis()) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }

                alarmTimeInMillis = calendar.timeInMillis

                Toast.makeText(this, "Alarm set for $formattedTime", Toast.LENGTH_SHORT).show()
            },
            hour,
            minute,
            false // Set to false for 12-hour format
        )

        timePicker.show()
    }

    /**
     * Schedules an alarm using AlarmManager to trigger at the specified time.
     *
     * @param timeInMillis The time in milliseconds when the alarm should trigger.
     */
    private fun setAlarm(timeInMillis: Long) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(this, AlarmReceiver::class.java)

        // PendingIntent flags
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, pendingIntentFlags)

        // Schedule the alarm

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {  // Android 12+
            // Handle exact alarm permission for Android 12+
            if (alarmManager.canScheduleExactAlarms()) {
                // Schedule the exact alarm
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    timeInMillis,
                    pendingIntent
                )
                Toast.makeText(this, "Alarm set for: " + selectTimeTextView.text, Toast.LENGTH_SHORT).show()
            } else {
                // Redirect user to settings to enable exact alarm permission
                Toast.makeText(this, "Please enable exact alarms in the settings", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            }
        } else {
            // Schedule the alarm for devices below Android 12 (no permission required)
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                timeInMillis,
                pendingIntent
            )
            Toast.makeText(this, "Alarm set for: " + selectTimeTextView.text, Toast.LENGTH_SHORT).show()
            }

        Toast.makeText(this, "Alarm is set!", Toast.LENGTH_SHORT).show()
    }

    /**
     * Cancels the previously set alarm.
     */
    private fun cancelAlarm() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(this, AlarmReceiver::class.java)

        // PendingIntent flags
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, pendingIntentFlags)

        // Cancel the alarm
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()

        Toast.makeText(this, "Alarm is canceled", Toast.LENGTH_SHORT).show()
    }
}
