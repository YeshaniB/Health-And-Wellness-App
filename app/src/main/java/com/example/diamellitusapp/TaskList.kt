package com.example.diamellitusapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diamellitusapp.Adaptor.TaskAdaptor
import com.google.android.material.floatingactionbutton.FloatingActionButton

class TaskList : AppCompatActivity() {
    //private lateinit var setAlarmIcon: FloatingActionButton
    //private lateinit var alarmManager: AlarmManager
    private lateinit var adaptor: TaskAdaptor
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_task_list)


        val fab: FloatingActionButton = findViewById(R.id.fab1)
        val icon1: ImageView = findViewById(R.id.Home)
        val icon2: ImageView = findViewById(R.id.Addtask)
        val icon3: ImageView = findViewById(R.id.setTimer)

        //Navigation to Page
        fab.setOnClickListener {
            val intent = Intent(this, AddTask::class.java)
            startActivity(intent)
        }
        // Set the OnClickListener to navigate to the AddTask activity
         icon1.setOnClickListener {
            val intent = Intent(this, TaskList::class.java)
            startActivity(intent)
        }
        // Set the OnClickListener to navigate to the AddTask activity
        icon2.setOnClickListener {
            val intent = Intent(this, AddTask::class.java)
            startActivity(intent)
        }
        // Set the OnClickListener to navigate to the Timer activity
        icon3.setOnClickListener {
            val intent = Intent(this, Timer::class.java)
            startActivity(intent)
        }

        // Initialize RecyclerView and Adapter
        val recyclerView = findViewById<RecyclerView>(R.id.taskRecycleview1)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize the adapter and set it to RecyclerView
        adaptor = TaskAdaptor(this, mutableListOf())
        recyclerView.adapter = adaptor

        // Load data
        adaptor.reloadData()
    }

    override fun onResume() {
        super.onResume()
        // Reload data when returning to this activity
        adaptor.reloadData()
        }

}



