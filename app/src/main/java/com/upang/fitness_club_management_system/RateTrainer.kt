package com.upang.fitness_club_management_system

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.upang.fitness_club_management_system.api.Api
import com.upang.fitness_club_management_system.api.RetrofitClient
import com.upang.fitness_club_management_system.model.FetchTrainerProfileResponse
import com.upang.fitness_club_management_system.model.RateTrainerRequest
import com.upang.fitness_club_management_system.model.RateTrainerResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RateTrainer : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_rate_trainer)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }
        val btnPost: Button = findViewById(R.id.btnPost)
        btnPost.setOnClickListener {
            rateTrainer()
        }
        fetchUserProfile()
    }
    private fun fetchUserProfile() {
        val trainerEmail = intent.getStringExtra("trainer_email") ?: ""

        if (trainerEmail.isEmpty()) {
            Toast.makeText(this, "Trainer email is missing", Toast.LENGTH_SHORT).show()
            return
        }

        val api = RetrofitClient.instance.create(Api::class.java)

        api.fetchTrainerProfile(trainerEmail).enqueue(object : Callback<FetchTrainerProfileResponse> {
            override fun onResponse(call: Call<FetchTrainerProfileResponse>, response: Response<FetchTrainerProfileResponse>) {
                if (response.isSuccessful) {
                    val profile = response.body()
                    if (profile != null) {
                        findViewById<TextView>(R.id.tvName).text = "How was your training experience with ${profile.fullname}"
                        val profileImageView = findViewById<ImageView>(R.id.ivProfile)
                        Glide.with(this@RateTrainer)
                            .load(RetrofitClient.getBaseImageUrl() + "storage/profiles/" + profile.profile_picture)
                            .into(profileImageView)
                    } else {
                        Log.e("BookTrainerSuccess", "Profile is null")
                        Toast.makeText(this@RateTrainer, "Failed to fetch profile: Profile is null", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("BookTrainerSuccess", "Error: ${response.code()} - ${response.message()}")
                    Toast.makeText(this@RateTrainer, "Failed to fetch profile: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<FetchTrainerProfileResponse>, t: Throwable) {
                Log.e("BookTrainerSuccess", "Network request failed: ${t.message}", t)
                Toast.makeText(this@RateTrainer, "Failed to fetch profile: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun rateTrainer() {
        val userEmail = intent.getStringExtra("trainee_email") ?: ""
        val userName = intent.getStringExtra("trainee_name") ?: ""
        val trainerEmail = intent.getStringExtra("trainer_email") ?: ""
        val trainerName = intent.getStringExtra("trainer_name") ?: ""

        val ratingBar = findViewById<RatingBar>(R.id.ratingBar)
        val etComment = findViewById<EditText>(R.id.etComment)

        val rating = ratingBar.rating.toDouble()
        val comment = etComment.text.toString().trim()

        // Validate inputs
        if (rating < 1.0 || rating > 5.0) {
            Toast.makeText(this, "Please select a valid rating between 1 and 5", Toast.LENGTH_SHORT).show()
            Log.e("RateTrainer", "Invalid rating: $rating")
            return
        }

        if (comment.isEmpty()) {
            Toast.makeText(this, "Please enter a comment", Toast.LENGTH_SHORT).show()
            Log.e("RateTrainer", "Comment field is empty")
            return
        }

        val api = RetrofitClient.instance.create(Api::class.java)
        val rateTrainerRequest = RateTrainerRequest(userName, userEmail, trainerName, trainerEmail, rating, comment)

        api.rateTrainer(rateTrainerRequest).enqueue(object : Callback<RateTrainerResponse> {
            override fun onResponse(call: Call<RateTrainerResponse>, response: Response<RateTrainerResponse>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    Log.d("RateTrainer", "API Response Body: $responseBody")

                    if (responseBody?.success == true) {
                        Toast.makeText(this@RateTrainer, "Review submitted successfully!", Toast.LENGTH_SHORT).show()
                        Log.d("RateTrainer", "Review submission successful!")
                        finish() // Close activity after submission
                    } else {
                        Log.e("RateTrainer", "API Success False: ${responseBody?.message}")
                        Toast.makeText(this@RateTrainer, "Failed to submit review: ${responseBody?.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("RateTrainer", "Failed Response - Code: ${response.code()}, Message: ${response.message()}")
                    Toast.makeText(this@RateTrainer, "Failed to submit review", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RateTrainerResponse>, t: Throwable) {
                Log.e("RateTrainer", "Error submitting review: ${t.message}", t)
                Toast.makeText(this@RateTrainer, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


}