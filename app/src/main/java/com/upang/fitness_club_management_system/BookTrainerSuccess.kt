package com.upang.fitness_club_management_system

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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
import com.upang.fitness_club_management_system.model.TrainerRequestResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
        initViews()
        fetchUserProfile()
    }
    private fun initViews() {
        sharedPreferences = getSharedPreferences("TrainerPrefs", Context.MODE_PRIVATE)
    }
    private fun fetchUserProfile() {
        val api = RetrofitClient.instance.create(Api::class.java)
        val trainerEmail = sharedPreferences.getString("selected_trainer_email", null)

        if (trainerEmail == null) {
            Log.e("TrainerAccount", "Email is null")
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
                        Log.e("TrainerAccount", "Profile is null")
                        Toast.makeText(this@BookTrainerSuccess, "Failed to fetch profile: Profile is null", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("TrainerAccount", "Error: ${response.code()} - ${response.message()}")
                    Toast.makeText(this@BookTrainerSuccess, "Failed to fetch profile: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<FetchTrainerProfileResponse>, t: Throwable) {
                Log.e("TrainerAccount", "Network request failed: ${t.message}", t)
                Toast.makeText(this@BookTrainerSuccess, "Failed to fetch profile: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchEvents() {
        val preferenceManager = PreferenceManager(this)
        val email = preferenceManager.getEmail()
        if (email == null) {
            Toast.makeText(this, "Email not found. Please log in.", Toast.LENGTH_SHORT).show()
            return
        }

        val api = RetrofitClient.instance.create(Api::class.java)
        api.fetchTrainerRequest(email).enqueue(object : Callback<List<TrainerRequestResponse>> {
            override fun onResponse(
                call: Call<List<TrainerRequestResponse>>,
                response: Response<List<TrainerRequestResponse>>
            ) {
                if (response.isSuccessful && response.body() != null) {

                } else {
                    Toast.makeText(applicationContext, "Failed to load data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<TrainerRequestResponse>>, t: Throwable) {
                Log.e("TrainerClients", "API call failed: ${t.message}")
                Toast.makeText(applicationContext, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}