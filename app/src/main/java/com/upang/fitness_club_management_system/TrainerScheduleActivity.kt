package com.upang.fitness_club_management_system

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.upang.fitness_club_management_system.adapter.EventAdapter
import com.upang.fitness_club_management_system.api.Api
import com.upang.fitness_club_management_system.api.RetrofitClient
import com.upang.fitness_club_management_system.model.Event
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Objects

class TrainerScheduleActivity : AppCompatActivity() {
    private lateinit var calendarView: MaterialCalendarView
    private lateinit var recyclerView: RecyclerView
    private lateinit var eventAdapter: EventAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_trainer_schedule)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.trainer_schedule)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        calendarView = findViewById(R.id.calendarView)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        calendarView.setOnDateChangedListener { widget, date, selected ->
            val selectedDate = "${date.year}-${date.month + 1}-${date.day}"
            fetchEvents(selectedDate)
        }
    }
    private fun fetchEvents(date: String) {
        val api = RetrofitClient.instance.create(Api::class.java)
        val call: Call<List<Event>> = api.getEvents(date)
        call.enqueue(object : Callback<List<Event>> {
            override fun onResponse(call: Call<List<Event>>, response: Response<List<Event>>) {
                if (response.isSuccessful && response.body() != null) {
                    eventAdapter = EventAdapter(response.body()!!)
                    recyclerView.adapter = eventAdapter
                } else {
                    Toast.makeText(this@TrainerScheduleActivity, "No events found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Event>>, t: Throwable) {
                Log.e("API_ERROR", t.message ?: "Unknown error")
                Toast.makeText(this@TrainerScheduleActivity, "Error fetching events", Toast.LENGTH_SHORT).show()
            }
        })
    }
}