package com.upang.fitness_club_management_system

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.upang.fitness_club_management_system.adapter.TrainerClientAdapter
import com.upang.fitness_club_management_system.api.Api
import com.upang.fitness_club_management_system.api.RetrofitClient
import com.upang.fitness_club_management_system.helper.PreferenceManager
import com.upang.fitness_club_management_system.model.FetchTrainerProfileResponse
import com.upang.fitness_club_management_system.model.TrainerFetchApiResponse
import com.upang.fitness_club_management_system.model.TrainerFetchData
import com.upang.fitness_club_management_system.model.TrainerRequestApiResponse
import com.upang.fitness_club_management_system.model.TrainerRequestResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale

class BookTrainerSuccess : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_book_trainer_success)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets

        }

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, BookClass::class.java)
            startActivity(intent)
        }
    }

    private fun fetchUserProfile() {
        val api = RetrofitClient.instance.create(Api::class.java)
        val trainerEmail = sharedPreferences.getString("selected_trainer_email", null)

        if (trainerEmail == null) {
            Log.e("BookTrainerSuccess", "Email is null")
            Toast.makeText(this, "Failed to fetch profile: Email is null", Toast.LENGTH_SHORT).show()
            return
        }

        api.fetchTrainerProfile(trainerEmail).enqueue(object :
            Callback<FetchTrainerProfileResponse> {
            override fun onResponse(call: Call<FetchTrainerProfileResponse>, response: Response<FetchTrainerProfileResponse>) {
                if (response.isSuccessful) {
                    val profile = response.body()
                    if (profile != null) {
                        findViewById<TextView>(R.id.tvName).text = profile.fullname
                        val profileImageView = findViewById<ImageView>(R.id.ivProfile)
                        Glide.with(this@BookTrainerSuccess)
                            .load(RetrofitClient.getBaseImageUrl() + "storage/profiles/" + profile.profile_picture)
                            .into(profileImageView)
                    } else {
                        Log.e("BookTrainerSuccess", "Profile is null")
                        Toast.makeText(this@BookTrainerSuccess, "Failed to fetch profile: Profile is null", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("BookTrainerSuccess", "Error: ${response.code()} - ${response.message()}")
                    Toast.makeText(this@BookTrainerSuccess, "Failed to fetch profile: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<FetchTrainerProfileResponse>, t: Throwable) {
                Log.e("BookTrainerSuccess", "Network request failed: ${t.message}", t)
                Toast.makeText(this@BookTrainerSuccess, "Failed to fetch profile: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchEvents() {
        val trainerEmail = sharedPreferences.getString("selected_trainer_email", null)

        if (trainerEmail == null) {
            Log.e("BookTrainerSuccess", "Trainer email is null")
            Toast.makeText(this, "Error: Trainer email is null", Toast.LENGTH_SHORT).show()
            return
        }

        val request_id = sharedPreferences.getString(trainerEmail, null)

        if (request_id == null) {
            Log.e("BookTrainerSuccess", "Request ID is null")
            Toast.makeText(this, "Error: No request ID found for this trainer", Toast.LENGTH_SHORT).show()
            return
        }

        val api = RetrofitClient.instance.create(Api::class.java)
        api.fetchTrainerRequestId(request_id).enqueue(object : Callback<TrainerFetchApiResponse> {
            override fun onResponse(
                call: Call<TrainerFetchApiResponse>,
                response: Response<TrainerFetchApiResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val request = response.body()?.request

                    Log.d("BookTrainerSuccess", "Request fetched: $request")

                    val formattedStartTime = formatTime(request?.time_start)
                    val formattedEndTime = formatTime(request?.time_end)

                    findViewById<TextView>(R.id.tvName).text = request?.date_of_training ?: "No date"
                    findViewById<TextView>(R.id.tvTime).text = "$formattedStartTime - $formattedEndTime"
                    findViewById<TextView>(R.id.tvDescription).text = request?.description ?: "No description"
                } else {
                    Toast.makeText(applicationContext, "Failed to load data", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<TrainerFetchApiResponse>, t: Throwable) {
                Log.e("BookTrainerSuccess", "API call failed: ${t.message}")
                Toast.makeText(applicationContext, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })

    }

    private fun formatTime(time: String?): String {
        if (time.isNullOrEmpty()) return "No time"

        return try {
            val inputFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("h:mm a", Locale.getDefault()) // Converts to "8:00 AM"
            val date = inputFormat.parse(time)
            outputFormat.format(date ?: return "Invalid time")
        } catch (e: Exception) {
            "Invalid time"
        }
    }
}