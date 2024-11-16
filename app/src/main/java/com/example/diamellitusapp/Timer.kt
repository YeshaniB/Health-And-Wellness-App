package com.example.diamellitusapp

import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.media.MediaPlayer
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class Timer : AppCompatActivity() {

    companion object {
        var mediaPlayer: MediaPlayer? = null
    }

    private lateinit var timerLeftText: TextView
    private lateinit var btnPlayPause: AppCompatButton
    private lateinit var addTimerButton: ImageButton
    private lateinit var resetTimerButton: ImageButton
    private lateinit var progressBar: ProgressBar

    private var timeLeftInMillis: Long = 0L
    private var timerLengthInMillis: Long = 60000L
    private var timer: CountDownTimer? = null
    private var isTimerRunning = false
    private var isTimerPaused = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContentView(R.layout.activity_timer)


        val home: ImageView = findViewById(R.id.Home)
        val task: ImageView = findViewById(R.id.Addtask)
        val timer: ImageView = findViewById(R.id.setTimer)

        home.setOnClickListener {
            val intent = Intent(this, TaskList::class.java)
            startActivity(intent)
        }
        // Set the OnClickListener to navigate to the new activity
        task.setOnClickListener {
            val intent = Intent(this, AddTask::class.java)
            startActivity(intent)
        }

        timer.setOnClickListener {
            val intent = Intent(this, Timer::class.java)
            startActivity(intent)
        }
        // Initialize views
        timerLeftText = findViewById(R.id.timerleft)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        addTimerButton = findViewById(R.id.addtimer)
        resetTimerButton = findViewById(R.id.resetTimer)
        progressBar = findViewById(R.id.progressBar)

        // Initialize progress bar
       progressBar.max = 100
       progressBar.progress = 0 // Initially full

        // Initialize MediaPlayer with alarm sound
        mediaPlayer = MediaPlayer.create(this, R.raw.time_alarm) // Replace 'alarm_sound' with your actual file name

        // Set OnClickListener for adding time (e.g., 1 minute)
        addTimerButton.setOnClickListener {
            //addTime(60000) // Add 1 minute (60,000 ms)
            showTimePickerDialog()
        }

        // Set OnClickListener for the Play/Pause button
        btnPlayPause.setOnClickListener {
            if (isTimerRunning) {
                pauseTimer()
            } else {
                startTimer()
            }
        }
        // Set OnClickListener for the Reset button
        resetTimerButton.setOnClickListener {
            resetTimer()
        }
    }

    private fun showTimePickerDialog() {
        // Create NumberPickers for hours, minutes, and seconds
        val hourPicker = NumberPicker(this).apply {
            minValue = 0
            maxValue = 23 // Assuming a 24-hour format
            value = 0 // Default hour
        }

        val minutePicker = NumberPicker(this).apply {
            minValue = 0
            maxValue = 59 // Minutes range
            value = 0 // Default minute
        }

        val secondPicker = NumberPicker(this).apply {
            minValue = 0
            maxValue = 59 // Seconds range
            value = 0 // Default second
        }

        // Create a layout to hold the NumberPickers
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(hourPicker)
            addView(minutePicker)
            addView(secondPicker)
        }

        // Build the AlertDialog
        val dialog = AlertDialog.Builder(this)
            .setTitle("Set Timer (Hours, Minutes, and Seconds)")
            .setView(layout)
            .setPositiveButton("OK") { _, _ ->
                // Convert selected time to milliseconds and update the timer
                val selectedHours = hourPicker.value
                val selectedMinutes = minutePicker.value
                val selectedSeconds = secondPicker.value

                val totalTimeInMillis = (selectedHours * 3600 + selectedMinutes * 60 + selectedSeconds) * 1000L
                addTime(totalTimeInMillis) // Update the timer with total time in milliseconds
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    // Method to add time (in milliseconds)
    private fun addTime(timeToAdd: Long) {
        timerLengthInMillis += timeToAdd
        timeLeftInMillis += timeToAdd
        updateTimerText()
        if (isTimerRunning) {
            updateProgressBar()
        }




    }

    override fun onDestroy() {
        super.onDestroy()
        // Release MediaPlayer when activity is destroyed
        mediaPlayer?.release()
        mediaPlayer = null // Set to null to prevent memory leaks
    }
    // Method to start the timer
    private fun startTimer() {
       val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        if (timeLeftInMillis <= 0) return
        timer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateTimerText()

                // Directly update progress bar with remaining time
                //progressBar.progress = ((timerLengthInMillis - timeLeftInMillis).toInt())
                // Update progress bar only every 5 seconds to avoid too many UI updates
                if ((millisUntilFinished / 1000) % 5 == 0L) {
                    updateProgressBar()
                }
            }

            override fun onFinish() {
                //resetTimer()
//                timeLeftInMillis = 0L
//                updateTimerText()
//                progressBar.progress = progressBar.max // Set progress to max (fill the ring)
//                btnPlayPause.text = "Start"
//                isTimerRunning = false
//                isTimerPaused = false
                progressBar.progress = progressBar.max

                // Optionally change progress drawable to indicate completion
                //progressBar.progressDrawable = getDrawable(R.drawable.grey_progressbas_bg)

                resetTimer()
                // Play the alarm sound if mediaPlayer is initialized
                mediaPlayer?.let {
                    if (!it.isPlaying) {
                        it.start() // Start playing the alarm sound
                    }
                }
            }
        }.start()
        isTimerRunning = true
        isTimerPaused = false
        btnPlayPause.text = "Pause"
    }


    // Method to pause the timer
    private fun pauseTimer() {
        timer?.cancel()
        isTimerRunning = false
        isTimerPaused = true
        btnPlayPause.text = "Start"
    }

    // Method to reset the timer
    private fun resetTimer() {
        timer?.cancel()
        timeLeftInMillis = 0L
        timerLengthInMillis = 0L
        isTimerRunning = false
        isTimerPaused = false
        updateTimerText()
        // Set progress bar to 0 to show it's empty
        progressBar.progress = 0
        btnPlayPause.text = "Start"
    }

    // Method to update the text of the timer
    private fun updateTimerText() {
        val totalSeconds = (timeLeftInMillis / 1000) //n
        val hours = totalSeconds / 3600 //n
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        //val timeFormatted = String.format("%02d:%02d", minutes, seconds)
        val timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds) //n

        timerLeftText.text = timeFormatted
    }

    private fun updateProgressBar() {
        if (isTimerRunning && timerLengthInMillis > 0) {
            // Calculate progress as a percentage of the total time
            val progressPercentage = (timerLengthInMillis - timeLeftInMillis).toFloat() / timerLengthInMillis.toFloat() * progressBar.max

            // Use ObjectAnimator to animate the progress change smoothly
            val progressAnimator = ObjectAnimator.ofInt(progressBar, "progress", progressBar.progress, progressPercentage.toInt())
            progressAnimator.duration = 500 // Smooth update duration
            progressAnimator.start()
        }
    }




}

