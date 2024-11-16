package com.example.diamellitusapp.Adaptor

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.format.DateTimeFormatter
import com.example.diamellitusapp.R
import com.example.diamellitusapp.Data.model.TaskDataClass
import com.example.diamellitusapp.UpdateTask
import com.example.diamellitusapp.Utils.AlarmReciving

class TaskAdaptor(

    private val context: Context,
    private var taskList:  MutableList<TaskDataClass> // Changed to List since it's more typical
) : RecyclerView.Adapter<TaskAdaptor.TaskViewHolder>() {

    // ViewHolder class to represent each task item
   class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val taskCategory: TextView = view.findViewById(R.id.textViewCategory) // Updated to match your XML
        val taskDesc: TextView = view.findViewById(R.id.textViewDescription)
        val taskDueDate: TextView = view.findViewById(R.id.textViewDate)
        val taskDueTime: TextView = view.findViewById(R.id.textViewTime)
        val updateButton: FloatingActionButton = view.findViewById(R.id.update)
        val deleteButton: FloatingActionButton = view.findViewById(R.id.delete)
        val cancelAlarmButton: FloatingActionButton = view.findViewById(R.id.cancel_alarmicon)
        //val cancelAlarmButton: FloatingActionButton = view.findViewById(R.id.cancelalarmicon)

    }



    // Inflates the task item layout and returns the ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.activity_task_layout, parent, false) // Assumes you have task_item_layout.xml
        return TaskViewHolder(view)
    }

    // Binds data to the views in the ViewHolder
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]

        // Format the due date and time
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        val formattedDate = task.dueDate?.format(dateFormatter) ?: "No Date"
        val formattedTime = task.dueTime?.format(timeFormatter) ?: "No Time"

        holder.taskCategory.text = task.category
        holder.taskDesc.text = task.desc
        holder.taskDueDate.text = "$formattedDate"
        holder.taskDueTime.text = "$formattedTime"
        holder.updateButton.setOnClickListener {
            updateTask(position)
        }
        holder.deleteButton.setOnClickListener {
            deleteTask(position)
        }
        // Set click listener for cancel alarm button
        holder.cancelAlarmButton.setOnClickListener {
            cancelReminder(task, position)
        }

        // Set click listener for each item
        holder.itemView.setOnClickListener {

        }
    }

    // Returns the number of tasks in the list
    override fun getItemCount(): Int {
        return taskList.size
    }



    @SuppressLint("NotifyDataSetChanged")
    fun reloadData() {
        val sharedPreferences = context.getSharedPreferences("DiaMellitus", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("DiaMellitus", null)
        val type = object : TypeToken<MutableList<TaskDataClass>>() {}.type
        taskList= if (json != null) gson.fromJson(json, type) else mutableListOf()
        notifyDataSetChanged()
    }

    private fun deleteTask(position: Int) {
        taskList.removeAt(position)
        notifyItemRemoved(position)

        val sharedPreferences = context.getSharedPreferences("DiaMellitus", Context.MODE_PRIVATE)
        val gson = Gson()
        val updatedJson = gson.toJson( taskList)
        val editor = sharedPreferences.edit()
        editor.putString("DiaMellitus", updatedJson)
        editor.apply()

        notifyItemRangeChanged(position,  taskList.size)
    }
    private fun updateTask(position: Int) {
        val dietTasks = taskList[position]
        val intent = Intent(context, UpdateTask::class.java)
        intent.putExtra("position", position)
        intent.putExtra("taskCategory", dietTasks.category)
        intent.putExtra("taskDesc", dietTasks.desc)
        intent.putExtra("taskDate", dietTasks.dueDate)
        intent.putExtra("taskTime", dietTasks.dueTime)
        context.startActivity(intent)

    }

    // Cancels the reminder (alarm) and displays a Toast message
    private fun cancelReminder(task: TaskDataClass, position: Int) {
        // Create an intent to cancel the alarm
        val alarmIntent = Intent(context, AlarmReciving::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            position, // Use position as requestCode to uniquely identify the alarm
            alarmIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Cancel the alarm with AlarmManager
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)

        // Display a toast message indicating that the reminder was canceled
        Toast.makeText(context, "Reminder Canceled", Toast.LENGTH_SHORT).show()
    }









}