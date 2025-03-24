package com.upang.fitness_club_management_system

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.upang.fitness_club_management_system.adapter.OrdersAdapter
import com.upang.fitness_club_management_system.api.Api
import com.upang.fitness_club_management_system.api.RetrofitClient
import com.upang.fitness_club_management_system.helper.PreferenceManager
import com.upang.fitness_club_management_system.model.FetchOrdersResponse
import com.upang.fitness_club_management_system.model.FetchTrainerProfileResponse
import com.upang.fitness_club_management_system.model.Order
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.content.Intent
import android.widget.ImageButton

class TrainerAccount : AppCompatActivity() {
    private lateinit var ordersAdapter: OrdersAdapter
    private lateinit var progressBar: ProgressBar
    private var dataLoadCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_trainer_account)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        progressBar = findViewById(R.id.progressBar)

        findViewById<ImageButton>(R.id.btnSettings).setOnClickListener {
            val intent = Intent(this, Settings::class.java)
            startActivity(intent)
        }

        showLoader()
        fetchUserProfile()
        fetchOrders()
    }

    private fun showLoader() {
        progressBar.visibility = View.VISIBLE
    }

    private fun hideLoader() {
        dataLoadCount++
        if (dataLoadCount == 2) {
            progressBar.visibility = View.GONE
        }
    }

    private fun fetchUserProfile() {
        val preferenceManager = PreferenceManager(this)
        val api = RetrofitClient.instance.create(Api::class.java)
        val email = preferenceManager.getEmail()

        if (email == null) {
            Log.e("TrainerAccount", "Email is null")
            Toast.makeText(this, "Failed to fetch profile: Email is null", Toast.LENGTH_SHORT).show()
            hideLoader()
            return
        }

        api.fetchTrainerProfile(email).enqueue(object : Callback<FetchTrainerProfileResponse> {
            override fun onResponse(
                call: Call<FetchTrainerProfileResponse>,
                response: Response<FetchTrainerProfileResponse>
            ) {
                if (response.isSuccessful) {
                    val profile = response.body()
                    if (profile != null) {
                        findViewById<TextView>(R.id.tvName).text = profile.fullname
                        findViewById<TextView>(R.id.tvEmail).text = profile.email
                        findViewById<TextView>(R.id.tvAboutMe).text = profile.about
                        val profileImageView = findViewById<ImageView>(R.id.imageViewAvatar1)
                        Glide.with(this@TrainerAccount)
                            .load(RetrofitClient.getBaseImageUrl() + "storage/profiles/" + profile.profile_picture)
                            .into(profileImageView)
                    } else {
                        Log.e("TrainerAccount", "Profile is null")
                        Toast.makeText(this@TrainerAccount, "Failed to fetch profile: Profile is null", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("TrainerAccount", "Error: ${response.code()} - ${response.message()}")
                    Toast.makeText(this@TrainerAccount, "Failed to fetch profile: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
                hideLoader()
            }

            override fun onFailure(call: Call<FetchTrainerProfileResponse>, t: Throwable) {
                Log.e("TrainerAccount", "Network request failed: ${t.message}", t)
                Toast.makeText(this@TrainerAccount, "Failed to fetch profile: ${t.message}", Toast.LENGTH_SHORT).show()
                hideLoader()
            }
        })
    }

    private fun fetchOrders() {
        val preferenceManager = PreferenceManager(this)
        val api = RetrofitClient.instance.create(Api::class.java)
        val email = preferenceManager.getEmail()

        if (email == null) {
            Log.e("TrainerAccount", "Email is null")
            Toast.makeText(this, "Failed to fetch orders: Email is null", Toast.LENGTH_SHORT).show()
            hideLoader()
            return
        }

        api.fetchOrders(email).enqueue(object : Callback<FetchOrdersResponse> {
            override fun onResponse(
                call: Call<FetchOrdersResponse>,
                response: Response<FetchOrdersResponse>
            ) {
                if (response.isSuccessful) {
                    val ordersResponse = response.body()
                    if (ordersResponse != null) {
                        setupRecyclerView(ordersResponse.orders)
                    } else {
                        Log.e("TrainerAccount", "Orders response is null")
                        Toast.makeText(this@TrainerAccount, "Failed to fetch orders: Response is null", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("TrainerAccount", "Error: ${response.code()} - ${response.message()}")
                    Toast.makeText(this@TrainerAccount, "Failed to fetch orders: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
                hideLoader()
            }

            override fun onFailure(call: Call<FetchOrdersResponse>, t: Throwable) {
                Log.e("TrainerAccount", "Network request failed: ${t.message}", t)
                Toast.makeText(this@TrainerAccount, "Failed to fetch orders: ${t.message}", Toast.LENGTH_SHORT).show()
                hideLoader()
            }
        })
    }

    private fun setupRecyclerView(orders: List<Order>) {
        val recyclerView = findViewById<RecyclerView>(R.id.rvOrders)
        recyclerView.layoutManager = LinearLayoutManager(this)
        ordersAdapter = OrdersAdapter(orders)
        recyclerView.adapter = ordersAdapter
    }
}