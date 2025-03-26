package com.upang.fitness_club_management_system

import android.app.TimePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.upang.fitness_club_management_system.api.Api
import com.upang.fitness_club_management_system.api.RetrofitClient
import com.upang.fitness_club_management_system.helper.PreferenceManager
import com.upang.fitness_club_management_system.model.BookTrainerRequest
import com.upang.fitness_club_management_system.model.BookTrainerResponse
import com.upang.fitness_club_management_system.model.FetchTrainerProfileResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class BookTrainerClass : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var calendarView: CalendarView
    private lateinit var selectStartTimeButton: Button
    private lateinit var selectEndTimeButton: Button
    private lateinit var selectedStartTimeText: TextView
    private lateinit var selectedEndTimeText: TextView
    private lateinit var etDescription: EditText
    private lateinit var btnBookTrainer: Button
    private var selectedDate: String = ""
    private var selectedStartTime: String = ""
    private var selectedEndTime: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_book_trainer_class)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupListeners()
        fetchUserProfile()
    }

    private fun initViews() {
        btnBookTrainer = findViewById(R.id.btnBookClass)
        etDescription = findViewById(R.id.etDescription)
        selectStartTimeButton = findViewById(R.id.selectStartTimeButton)
        selectEndTimeButton = findViewById(R.id.selectEndTimeButton)
        selectedStartTimeText = findViewById(R.id.selectedStartTimeText)
        selectedEndTimeText = findViewById(R.id.selectedEndTimeText)
        calendarView = findViewById(R.id.calendarView)
        sharedPreferences = getSharedPreferences("TrainerPrefs", Context.MODE_PRIVATE)
    }

    private fun setupListeners() {
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth)
        }

        btnBookTrainer.setOnClickListener { bookTrainer() }
        selectStartTimeButton.setOnClickListener { showTimePicker(true) }
        selectEndTimeButton.setOnClickListener { showTimePicker(false) }
    }

    private fun showTimePicker(isStartTime: Boolean) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            val amPm = if (selectedHour < 12) "AM" else "PM"
            val hourFormatted = if (selectedHour % 12 == 0) 12 else selectedHour % 12
            val time = String.format(Locale.getDefault(), "%02d:%02d %s", hourFormatted, selectedMinute, amPm)
            if (isStartTime) {
                selectedStartTime = time
                selectedStartTimeText.text = "Start Time: $selectedStartTime"
            } else {
                selectedEndTime = time
                selectedEndTimeText.text = "End Time: $selectedEndTime"
            }
        }, hour, minute, false)

        timePickerDialog.show()
    }

    private fun fetchUserProfile() {
        val api = RetrofitClient.instance.create(Api::class.java)
        val trainerEmail = sharedPreferences.getString("selected_trainer_email", null)

        if (trainerEmail == null) {
            Log.e("TrainerAccount", "Email is null")
            Toast.makeText(this, "Failed to fetch profile: Email is null", Toast.LENGTH_SHORT).show()
            return
        }

        api.fetchTrainerProfile(trainerEmail).enqueue(object : Callback<FetchTrainerProfileResponse> {
            override fun onResponse(call: Call<FetchTrainerProfileResponse>, response: Response<FetchTrainerProfileResponse>) {
                if (response.isSuccessful) {
                    val profile = response.body()
                    if (profile != null) {
                        findViewById<TextView>(R.id.tvName).text = profile.fullname
                        val profileImageView = findViewById<ImageView>(R.id.ivProfile)
                        Glide.with(this@BookTrainerClass)
                            .load(RetrofitClient.getBaseImageUrl() + "storage/profiles/" + profile.profile_picture)
                            .into(profileImageView)
                    } else {
                        Log.e("TrainerAccount", "Profile is null")
                        Toast.makeText(this@BookTrainerClass, "Failed to fetch profile: Profile is null", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("TrainerAccount", "Error: ${response.code()} - ${response.message()}")
                    Toast.makeText(this@BookTrainerClass, "Failed to fetch profile: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<FetchTrainerProfileResponse>, t: Throwable) {
                Log.e("TrainerAccount", "Network request failed: ${t.message}", t)
                Toast.makeText(this@BookTrainerClass, "Failed to fetch profile: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun bookTrainer() {
        val preferenceManager = PreferenceManager(this)
        val userEmail = preferenceManager.getEmail()
        val trainerEmail = sharedPreferences.getString("selected_trainer_email", null) ?: return
        val description = etDescription.text.toString()

        if (selectedDate.isEmpty() || selectedStartTime.isEmpty() || selectedEndTime.isEmpty() || description.isEmpty() || userEmail.isNullOrEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val api = RetrofitClient.instance.create(Api::class.java)
        val request = BookTrainerRequest(userEmail, trainerEmail, selectedDate, selectedStartTime, selectedEndTime, description)
        Log.e("BookTrainerClass","${userEmail}, ${trainerEmail}, ${selectedDate}, ${selectedStartTime}, ${selectedEndTime}, ${description}")
        api.requestTrainer(request).enqueue(object : Callback<BookTrainerResponse> {
            override fun onResponse(call: Call<BookTrainerResponse>, response: Response<BookTrainerResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.status == "success") {
                        Toast.makeText(this@BookTrainerClass, "Trainer booked successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@BookTrainerClass, "Booking failed: ${body?.message ?: "Unknown error"}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@BookTrainerClass, "Booking failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BookTrainerResponse>, t: Throwable) {
                Toast.makeText(this@BookTrainerClass, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("BookTrainerClass", "Error: ${t.message}", t)
            }
        })
    }
}
