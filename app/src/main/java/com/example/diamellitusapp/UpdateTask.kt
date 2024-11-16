package com.example.diamellitusapp

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.diamellitusapp.Data.model.TaskDataClass
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar

class UpdateTask : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_update_task)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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

        val position = intent.getIntExtra("position", -1)
        val prevCategory = intent.getStringExtra("taskCategory")
        val prevTaskDesc = intent.getStringExtra("taskDesc")
        val prevDate = intent.getStringExtra("taskDate")
        val prevTime = intent.getStringExtra("taskTime")

        val categoryInput = findViewById<Spinner>(R.id.updatespinnerCategory)
        val taskDescInput = findViewById<EditText>(R.id.updateTextTaskDescription)
        val dateInput = findViewById<EditText>(R.id.updateTextSelectDate)
        val timeInput = findViewById<EditText>(R.id.updateTextSelectTime)
        val updateBtn = findViewById<Button>(R.id.buttonUpdate)


//        }
        val types = resources.getStringArray(R.array.Category) // Ensure you have this array in strings.xml
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, types)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categoryInput.adapter = adapter

        // Set the Spinner selection based on oldType
        val spinnerPosition = adapter.getPosition(prevCategory)
        categoryInput.setSelection(spinnerPosition)
        


        //categoryInput.setText(prevCategory)
        taskDescInput.setText(prevTaskDesc)
        dateInput.setText(prevDate)
        timeInput.setText(prevTime)


        // Set up DatePickerDialog for the date input
        dateInput.setOnClickListener {
            showDatePickerDialog(dateInput)
        }

        // Set up TimePickerDialog for the time input
        timeInput.setOnClickListener {
            showTimePickerDialog(timeInput)
        }

        updateBtn.setOnClickListener {
            val newCategory = categoryInput.selectedItem.toString()
            val newDesc = taskDescInput.text.toString()
            val newTDate = dateInput.text.toString()
            val newTTime = timeInput.text.toString()



            val sharedPreferences = getSharedPreferences("DiaMellitus", MODE_PRIVATE)
            val gson = Gson()
            val json = sharedPreferences.getString("DiaMellitus", null)
            val type = object : TypeToken<MutableList<TaskDataClass>>() {}.type
            val taskList: MutableList<TaskDataClass> =
                if (json != null) gson.fromJson(json, type) else mutableListOf()

            if (position != -1 && position < taskList.size) {
                taskList[position] =
                    TaskDataClass(newCategory, newDesc, newTDate, newTTime)
            }

            val updatedJson = gson.toJson(taskList)
            val editor = sharedPreferences.edit()
            editor.putString("DiaMellitus", updatedJson)
            editor.apply()

            finish()

        }




        }





// Function to show DatePickerDialog
private fun showDatePickerDialog(editText: EditText) {
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(
        this,
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
        this,
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
}
