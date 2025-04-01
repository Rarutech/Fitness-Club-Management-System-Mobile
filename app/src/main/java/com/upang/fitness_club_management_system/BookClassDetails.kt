package com.upang.fitness_club_management_system

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.content.SharedPreferences
import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.upang.fitness_club_management_system.api.ReviewsAdapter
import com.upang.fitness_club_management_system.model.TrainerReviewResponse

class BookClassDetails : AppCompatActivity() {
    private lateinit var progressBar: ProgressBar
    private lateinit var btnBookClass: Button
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var rvReviews: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_book_class_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this@BookClassDetails, BookClass::class.java)
            startActivity(intent)
        }
        btnBookClass = findViewById(R.id.btnBookClass)
        btnBookClass.setOnClickListener{
            val intent = Intent(this, BookTrainerClass::class.java)
            startActivity(intent)
        }

        progressBar = findViewById(R.id.progressBar)
        sharedPreferences = getSharedPreferences("TrainerPrefs", Context.MODE_PRIVATE)

        rvReviews = findViewById(R.id.rvReviews)
        rvReviews.layoutManager = LinearLayoutManager(this)
        fetchReviews()
        fetchUserProfile()
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
            override fun onResponse(
                call: Call<FetchTrainerProfileResponse>,
                response: Response<FetchTrainerProfileResponse>
            ) {
                if (response.isSuccessful) {
                    val profile = response.body()
                    if (profile != null) {
                        findViewById<TextView>(R.id.tvName).text = profile.fullname
                        findViewById<TextView>(R.id.tvAbout).text = profile.about
                        val profileImageView = findViewById<ImageView>(R.id.ivProfile)
                        Glide.with(this@BookClassDetails)
                            .load(RetrofitClient.getBaseImageUrl() + "storage/profiles/" + profile.profile_picture)
                            .into(profileImageView)
                    } else {
                        Log.e("TrainerAccount", "Profile is null")
                        Toast.makeText(this@BookClassDetails, "Failed to fetch profile: Profile is null", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("TrainerAccount", "Error: ${response.code()} - ${response.message()}")
                    Toast.makeText(this@BookClassDetails, "Failed to fetch profile: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<FetchTrainerProfileResponse>, t: Throwable) {
                Log.e("TrainerAccount", "Network request failed: ${t.message}", t)
                Toast.makeText(this@BookClassDetails, "Failed to fetch profile: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchReviews() {
        val api = RetrofitClient.instance.create(Api::class.java)
        val trainerEmail = sharedPreferences.getString("selected_trainer_email", null) ?: return
        Log.e("BookClassDetails", "Email: $trainerEmail")

        api.fetchTrainerReviews(trainerEmail).enqueue(object : Callback<TrainerReviewResponse> {
            override fun onResponse(
                call: Call<TrainerReviewResponse>,
                response: Response<TrainerReviewResponse>
            ) {
                if (response.isSuccessful) {
                    val reviewsResponse = response.body()
                    if (reviewsResponse != null && reviewsResponse.success) {
                        val reviews = reviewsResponse.reviews

                        rvReviews.adapter = ReviewsAdapter(reviews)
                        rvReviews.visibility = View.VISIBLE

                        val averageRating = if (reviews.isNotEmpty()) {
                            String.format("%.1f", reviews[0].average_rating.toDouble()).toFloat()
                        } else {
                            0.0f
                        }

                        findViewById<TextView>(R.id.tvRating).text = "⭐ $averageRating"
                    } else {
                        Toast.makeText(this@BookClassDetails, "No reviews available", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@BookClassDetails, "Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<TrainerReviewResponse>, t: Throwable) {
                Toast.makeText(this@BookClassDetails, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("BookClassDetails", "Fetch reviews failed: ${t.message}")
            }
        })
    }

}
