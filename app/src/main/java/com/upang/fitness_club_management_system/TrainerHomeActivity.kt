package com.upang.fitness_club_management_system

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.upang.fitness_club_management_system.adapter.EventAdapter
import com.upang.fitness_club_management_system.adapter.HighlightAdapter
import com.upang.fitness_club_management_system.api.Api
import com.upang.fitness_club_management_system.api.RetrofitClient
import com.upang.fitness_club_management_system.helper.EventDecorator
import com.upang.fitness_club_management_system.helper.NotificationWorker
import com.upang.fitness_club_management_system.helper.PreferenceManager
import com.upang.fitness_club_management_system.helper.TodayDecorator
import com.upang.fitness_club_management_system.model.Event
import com.upang.fitness_club_management_system.model.HighlightResponse
import com.upang.fitness_club_management_system.model.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.HashSet
import java.util.concurrent.TimeUnit


class TrainerHomeActivity : AppCompatActivity() {
    private lateinit var eventAdapter: EventAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var rvEvents: RecyclerView
    private lateinit var highlightAdapter: HighlightAdapter
    private val handler = Handler()
    private val delay: Long = 5000
    private val runnable = object : Runnable {
        override fun run() {
            Utils.getNotifications(this@TrainerHomeActivity)
            handler.postDelayed(this, delay)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_trainer_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.recyclerView)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        Utils.checkAuthentication(this)
        startNotificationWorker()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        bottomNavigationView.selectedItemId = R.id.actionHomeTrainer

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



        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        rvEvents = findViewById(R.id.rvEvents)
        rvEvents.layoutManager = LinearLayoutManager(this)
        fetchEvents()
        fetchHighlights()
    }

    private fun fetchHighlights() {
        val api = RetrofitClient.instance.create(Api::class.java)

        api.GetHighlights().enqueue(object : Callback<HighlightResponse> {
            override fun onResponse(call: Call<HighlightResponse>, response: Response<HighlightResponse>) {
                if (response.isSuccessful) {
                    val highlights = response.body()?.highlights ?: emptyList()
                    highlightAdapter = HighlightAdapter(highlights)
                    recyclerView.adapter = highlightAdapter
                    Log.e("API", "Fetching Success")
                    Log.d("API","Response: ${response.body()}")
                } else {
                    Log.e("API", "Response not successful")
                }
            }

            override fun onFailure(call: Call<HighlightResponse>, t: Throwable) {
                Log.e("API", "Failed to fetch data: ${t.message}")
            }

        })
    }

    private fun fetchEvents() {
        val preferenceManager = PreferenceManager(this)
        val api = RetrofitClient.instance.create(Api::class.java)
        val email = preferenceManager.getEmail() ?: return
        val call: Call<List<Event>> = api.getAllEvents(email)

        call.enqueue(object : Callback<List<Event>> {
            override fun onResponse(call: Call<List<Event>>, response: Response<List<Event>>) {
                if (response.isSuccessful && response.body() != null) {
                    eventAdapter = EventAdapter(response.body()!!)
                    rvEvents.adapter = eventAdapter


                } else {
                    Toast.makeText(this@TrainerHomeActivity, "No events found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Event>>, t: Throwable) {
                Log.e("API_ERROR", t.message ?: "Unknown error")
                Toast.makeText(this@TrainerHomeActivity, "Error fetching events", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            }
        }
    }

    private fun startNotificationWorker() {
        val notificationWorkRequest = PeriodicWorkRequestBuilder<NotificationWorker>(15, TimeUnit.MINUTES)
            .build()

        // Enqueue the periodic work request
        WorkManager.getInstance(applicationContext).enqueue(notificationWorkRequest)

        Log.d("LoginActivity", "NotificationWorker enqueued after login.")
    }

    override fun onStart() {
        super.onStart()
        // Start checking membership status when the activity is visible
        handler.post(runnable)
    }

    override fun onStop() {
        super.onStop()
        // Stop checking membership status when the activity is not visible
        handler.removeCallbacks(runnable)
    }
}
