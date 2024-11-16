package com.example.diamellitusapp

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.ImageView
import com.example.diamellitusapp.Data.model.TaskDataClass
import com.example.diamellitusapp.Utils.AlarmReciving
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class AddTask : AppCompatActivity() {
    private lateinit var setAlarmIcon: FloatingActionButton
    private lateinit var dueTime: EditText
    private lateinit var taskDueDate: EditText


    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_task)

        val home: ImageView = findViewById(R.id.Home)
        val task: ImageView = findViewById(R.id.Addtask)
        val timer: ImageView = findViewById(R.id.setTimer)

        //Navigation Button Onclick Listerner
        home.setOnClickListener {
            val intent = Intent(this, TaskList::class.java)
            startActivity(intent)
        }

        task.setOnClickListener {
            val intent = Intent(this, AddTask::class.java)
            startActivity(intent)
        }

        timer.setOnClickListener {
            val intent = Intent(this, Timer::class.java)
            startActivity(intent)
        }


        val taskCategory = findViewById<Spinner>(R.id.spinnerCategory)
        val taskDesc = findViewById<EditText>(R.id.editTextTaskDescription)
        taskDueDate = findViewById(R.id.editTextSelectDate)
        dueTime = findViewById(R.id.editTextSelectTime)
        setAlarmIcon = findViewById(R.id.setAlarmIconF)
        val addButton = findViewById<Button>(R.id.buttonSave)

        // Set OnClickListener for DatePickerDialog
        taskDueDate.setOnClickListener {
            showDatePickerDialog(taskDueDate)
        }

        // Set OnClickListener for TimePickerDialog
        dueTime.setOnClickListener {
            showTimePickerDialog(dueTime)
        }

        setAlarmIcon.setOnClickListener {
            // Combine selected date and time
            val selectedDate = taskDueDate.text.toString()
            val selectedTime = dueTime.text.toString()

            //Validation
            if (selectedDate.isEmpty() || selectedTime.isEmpty()) {
                Toast.makeText(this, "Please select both date and time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val calendar = Calendar.getInstance()
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val fullDateTime = "$selectedDate $selectedTime"

            try {
                val date = sdf.parse(fullDateTime) // Parse date and time
                date?.let {
                    calendar.time = it
                    setAlarm(calendar.timeInMillis)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error parsing date/time", Toast.LENGTH_SHORT).show()
            }
        }

        addButton.setOnClickListener {
            val category = taskCategory.selectedItem.toString()
            val description = taskDesc.text.toString()
            val duedate = taskDueDate.text.toString()
            val duetime = dueTime.text.toString()

            if (description.isEmpty() || duedate.isEmpty() || duetime.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val sharedPreferences = getSharedPreferences("DiaMellitus", Context.MODE_PRIVATE)
            val gson = Gson()
            val json = sharedPreferences.getString("DiaMellitus", null)
            val type = object : TypeToken<MutableList<TaskDataClass>>() {}.type
            val taskListList: MutableList<TaskDataClass> =
                if (json != null) gson.fromJson(json, type) else mutableListOf()

            taskListList.add(TaskDataClass(category, description, duedate, duetime))

            val updatedJson = gson.toJson(taskListList)
            val editor = sharedPreferences.edit()
            editor.putString("DiaMellitus", updatedJson)
            editor.apply()

            Toast.makeText(this, "Task Added", Toast.LENGTH_SHORT).show()


            // Schedule notification
            /* val adaptor = TaskAdaptor(context, taskList)
            adaptor.scheduleNotification(task, task.dueDate!!, task.dueTime!!)
*/

            finish()

            val btnNavigate2: Button = findViewById(R.id.buttonSave)
            btnNavigate2.setOnClickListener {
                val intent = Intent(this, TaskList::class.java)
                startActivity(intent)
            }
        }
    }

        private fun setAlarm(timeInMillis: Long) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            val intent =
                Intent(this, AlarmReciving::class.java)  // Intent to trigger alarm receiver

            val pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Check Android version for exact alarm scheduling
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {  // Android 12+
                // Handle exact alarm permission for Android 12+
                if (alarmManager.canScheduleExactAlarms()) {
                    // Schedule the exact alarm
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        timeInMillis,
                        pendingIntent
                    )
                    Toast.makeText(this, "Reminder set for: ${dueTime.text}", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    // Redirect user to settings to enable exact alarm permission
                    Toast.makeText(
                        this,
                        "Please enable exact alarms in the settings",
                        Toast.LENGTH_LONG
                    ).show()
                    val intent =
                        Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    startActivity(intent)

                }
            } else {
                // Schedule the alarm for devices below Android 12 (no permission required)
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    timeInMillis,
                    pendingIntent
                )
                Toast.makeText(this, "Reminder set for:  ${dueTime.text}", Toast.LENGTH_SHORT).show()
            }
        }


    }


// Function to show DatePickerDialog
private fun showDatePickerDialog(editText: EditText) {
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(
        editText.context,
        { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            editText.setText(selectedDate)
        },
        year,
        month,
        day
    )
    datePickerDialog.show()
}

    private fun showTimePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            editText.context,
            { _, selectedHour, selectedMinute ->
                val selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                editText.setText(selectedTime)
            },
            hour,
            minute,
            true // Set to false if you want AM/PM format
        )
        timePickerDialog.show()
    }



