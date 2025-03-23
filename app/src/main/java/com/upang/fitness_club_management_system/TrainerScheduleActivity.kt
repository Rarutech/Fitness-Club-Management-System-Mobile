package com.upang.fitness_club_management_system

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.upang.fitness_club_management_system.adapter.EventAdapter
import com.upang.fitness_club_management_system.api.Api
import com.upang.fitness_club_management_system.api.RetrofitClient
import com.upang.fitness_club_management_system.helper.EventDecorator
import com.upang.fitness_club_management_system.helper.TodayDecorator
import com.upang.fitness_club_management_system.model.Event
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.HashSet

class TrainerScheduleActivity : AppCompatActivity() {
    private lateinit var calendarView: MaterialCalendarView
    private lateinit var recyclerView: RecyclerView
    private lateinit var eventAdapter: EventAdapter
    private val todayDate = CalendarDay.today() // Get today's date

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_trainer_schedule)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.trainer_schedule)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Bottom Navigation
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        when (javaClass) {
            TrainerHomeActivity::class.java -> bottomNavigationView.selectedItemId = R.id.actionHomeTrainer
            TrainerScheduleActivity::class.java -> bottomNavigationView.selectedItemId = R.id.actionSchedule
            PostHighlightActivity::class.java -> bottomNavigationView.selectedItemId = R.id.actionPost
            TrainerClients::class.java -> bottomNavigationView.selectedItemId = R.id.actionClients
            TrainerShop::class.java -> bottomNavigationView.selectedItemId = R.id.actionShopTrainer
        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            val targetActivity = when (item.itemId) {
                R.id.actionHomeTrainer -> TrainerHomeActivity::class.java
                R.id.actionSchedule -> TrainerScheduleActivity::class.java
                R.id.actionPost -> PostHighlightActivity::class.java
                R.id.actionClients -> TrainerClients::class.java
                R.id.actionShopTrainer -> TrainerShop::class.java
                else -> null
            }

            if (targetActivity != null && targetActivity != javaClass) {
                startActivity(Intent(this, targetActivity))
                finish()
            }
            true
        }


        calendarView = findViewById(R.id.calendarView)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        fetchEvents() // Fetch all events initially

        calendarView.setOnDateChangedListener { _, date, _ ->
            val selectedDate = "${date.year}-${date.month + 1}-${date.day}"
            fetchEventsForDate(selectedDate) // Fetch events for the selected date
        }
    }

    private fun fetchEvents() {
        val api = RetrofitClient.instance.create(Api::class.java)
        val call: Call<List<Event>> = api.getAllEvents() // Adjust API method to fetch all events

        call.enqueue(object : Callback<List<Event>> {
            override fun onResponse(call: Call<List<Event>>, response: Response<List<Event>>) {
                if (response.isSuccessful && response.body() != null) {
                    val events = response.body()!!
                    val eventDates = HashSet<CalendarDay>()

                    for (event in events) {
                        val parts = event.assignment_date.split("-") // Format: YYYY-MM-DD
                        if (parts.size == 3) {
                            val year = parts[0].toInt()
                            val month = parts[1].toInt() - 1 // CalendarDay uses 0-based months
                            val day = parts[2].toInt()
                            eventDates.add(CalendarDay.from(year, month, day))
                        }
                    }

                    // Apply event markers & highlight today
                    calendarView.post {
                        calendarView.removeDecorators() // Clear old decorators
                        calendarView.addDecorator(EventDecorator(eventDates)) // Add event dots
                        calendarView.addDecorator(TodayDecorator(todayDate)) // Highlight today
                    }
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

    private fun fetchEventsForDate(date: String) {
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
