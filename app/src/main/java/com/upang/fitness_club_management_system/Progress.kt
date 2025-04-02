package com.upang.fitness_club_management_system

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CalendarView
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.transition.Visibility
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.upang.fitness_club_management_system.api.Api
import com.upang.fitness_club_management_system.api.RetrofitClient
import com.upang.fitness_club_management_system.helper.PreferenceManager
import com.upang.fitness_club_management_system.model.TraineeProgressDateResponse
import com.upang.fitness_club_management_system.model.TraineeProgressResponse
import com.upang.fitness_club_management_system.model.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class Progress : AppCompatActivity() {
    private lateinit var tvTotalDays: TextView
    private lateinit var tvTotalWorkoutHours: TextView
    private lateinit var tvThisWeekDays: TextView
    private lateinit var tvThisWeekDates: TextView
    private lateinit var tvYear: TextView
    private lateinit var calendarView: CalendarView
    private var selectedDate: String = ""
    private val handler = Handler()
    private val delay: Long = 5000
    private val runnable = object : Runnable {
        override fun run() {
            Utils.getNotifications(this@Progress)
            handler.postDelayed(this, delay)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_progress)
        Utils.getNotifications(this)

        val btnAccount = findViewById<ImageButton>(R.id.btnAccount)
        btnAccount.setOnClickListener {
            val intent = Intent(this@Progress,Account::class.java)
            startActivity(intent)
        }

        // Initialize Views
        tvTotalDays = findViewById(R.id.tvTotalDays)
        tvTotalWorkoutHours = findViewById(R.id.tvTotalHours)
        tvThisWeekDays = findViewById(R.id.tvThisWeek)
        tvThisWeekDates = findViewById(R.id.tvWeek)
        tvYear = findViewById(R.id.tvYear)
        calendarView = findViewById(R.id.imageCalendarBG) // Ensure this is a CalendarView

        tvThisWeekDates.text = getCurrentWeekRange()
        tvYear.text = getCurrentYear()

        // Get selected date from CalendarView
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
            fetchProgressDate(selectedDate)
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.actionProgress

        bottomNavigationView.setOnItemSelectedListener { item ->
            val targetActivity = when (item.itemId) {
                R.id.actionHome -> Trainee_Home::class.java
                R.id.actionProgress -> Progress::class.java
                R.id.actionClasses -> BookClass::class.java
                R.id.actionShop -> Shop::class.java
                else -> null
            }

            if (targetActivity != null && targetActivity != javaClass) {
                startActivity(Intent(this, targetActivity))
                finish()
            }
            true
        }

        fetchProgress()
    }

    private fun fetchProgress() {
        val preferenceManager = PreferenceManager(this)
        val api = RetrofitClient.instance.create(Api::class.java)
        val email = preferenceManager.getEmail() ?: return

        api.TraineeProgress(email).enqueue(object : Callback<TraineeProgressResponse> {
            override fun onResponse(
                call: Call<TraineeProgressResponse>,
                response: Response<TraineeProgressResponse>
            ) {
                if (response.isSuccessful) {
                    val progress = response.body()
                    if (progress != null) {
                        tvTotalDays.text = progress.total_days.toString()
                        tvTotalWorkoutHours.text = progress.total_workout_hours.toString()
                        tvThisWeekDays.text = progress.this_week_days.toString()
                    }
                } else {
                    Toast.makeText(this@Progress, "Failed to load progress", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<TraineeProgressResponse>, t: Throwable) {
                Toast.makeText(this@Progress, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchProgressDate(date: String) {
        val preferenceManager = PreferenceManager(this)
        val email = preferenceManager.getEmail() ?: return
        val api = RetrofitClient.instance.create(Api::class.java)

        Log.d("fetchProgressDate", "Fetching progress for date: $date and email: $email")

        api.TraineeProgressDate(email, date).enqueue(object : Callback<TraineeProgressDateResponse> {
            override fun onResponse(
                call: Call<TraineeProgressDateResponse>,
                response: Response<TraineeProgressDateResponse>
            ) {
                if (response.isSuccessful) {
                    val progress = response.body()
                    if (progress != null) {
                        Log.d("fetchProgressDate", "Response received: $progress")

                        val checkInTime = progress.checkin_time
                        val checkOutTime = progress.checkout_time

                        val llDate: LinearLayout = findViewById(R.id.llDate)

                        if (checkInTime.isNullOrEmpty() || checkOutTime.isNullOrEmpty()) {
                            Log.d("fetchProgressDate", "Check-in or Check-out time is null, hiding layout.")
                            llDate.visibility = View.GONE
                            return
                        }

                        llDate.visibility = View.VISIBLE

                        val formattedCheckIn = formatTime(checkInTime)
                        val formattedCheckOut = formatTime(checkOutTime)

                        findViewById<TextView>(R.id.checkInTime).text = formattedCheckIn
                        findViewById<TextView>(R.id.checkOutTime).text = formattedCheckOut
                    } else {
                        Log.d("fetchProgressDate", "Response is empty or null")
                        Toast.makeText(this@Progress, "No records found for $date", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.d("fetchProgressDate", "API call failed with response code: ${response.code()}")
                    Toast.makeText(this@Progress, "No records found for $date", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<TraineeProgressDateResponse>, t: Throwable) {
                Log.e("fetchProgressDate", "API call failed: ${t.message}", t)
                Toast.makeText(this@Progress, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun formatTime(time: String): String {
        return try {
            val inputFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val date = inputFormat.parse(time)
            date?.let { outputFormat.format(it) } ?: time
        } catch (e: Exception) {
            Log.e("formatTime", "Error formatting time: ${e.message}", e)
            time // Return original time if there's an error
        }
    }

    private fun getCurrentWeekRange(): String {
        val calendar = Calendar.getInstance()

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val startDate = calendar.time

        calendar.add(Calendar.DATE, 6)
        val endDate = calendar.time

        val dateFormat = SimpleDateFormat("MMMM dd", Locale.getDefault())
        return "${dateFormat.format(startDate)} - ${dateFormat.format(endDate)}"
    }

    private fun getCurrentYear(): String {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.YEAR).toString()
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
