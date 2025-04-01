package com.upang.fitness_club_management_system

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
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
import com.upang.fitness_club_management_system.model.FetchTraineeProfileResponse
import com.upang.fitness_club_management_system.model.FetchTrainerProfileResponse
import com.upang.fitness_club_management_system.model.Order
import com.upang.fitness_club_management_system.model.getMembershipResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale

class Account : AppCompatActivity() {
    private lateinit var ordersAdapter: OrdersAdapter
    private lateinit var progressBar: ProgressBar
    private var dataLoadCount = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_account)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val toolbar: Toolbar = findViewById(R.id.toolbar2)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, Trainee_Home::class.java)
            startActivity(intent)
            finish()
        }
        progressBar = findViewById(R.id.progressBar)

        findViewById<ImageButton>(R.id.btnQrCode).setOnClickListener {
            val intent = Intent(this, QRCodeScan::class.java)
            startActivity(intent)
        }

        findViewById<ImageButton>(R.id.btnSettings).setOnClickListener {
            val intent = Intent(this, Settings::class.java)
            startActivity(intent)
            finish()
        }

        showLoader()
        fetchUserProfile()
        fetchOrders()
        fetchMembership()
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

        api.fetchUserProfile(email).enqueue(object : Callback<FetchTraineeProfileResponse> {
            override fun onResponse(
                call: Call<FetchTraineeProfileResponse>,
                response: Response<FetchTraineeProfileResponse>
            ) {
                if (response.isSuccessful) {
                    val profile = response.body()
                    if (profile != null) {
                        findViewById<TextView>(R.id.tvName).text = profile.fullname
                        findViewById<TextView>(R.id.tvEmail).text = profile.email
                        val profileImageView = findViewById<ImageView>(R.id.imageViewAvatar1)
                        Glide.with(this@Account)
                            .load(RetrofitClient.getBaseImageUrl() + "storage/profiles/" + profile.profile_picture)
                            .into(profileImageView)
                    } else {
                        Log.e("TrainerAccount", "Profile is null")
                        Toast.makeText(this@Account, "Failed to fetch profile: Profile is null", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("TrainerAccount", "Error: ${response.code()} - ${response.message()}")
                    Toast.makeText(this@Account, "Failed to fetch profile: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
                hideLoader()
            }

            override fun onFailure(call: Call<FetchTraineeProfileResponse>, t: Throwable) {
                Log.e("TrainerAccount", "Network request failed: ${t.message}", t)
                Toast.makeText(this@Account, "Failed to fetch profile: ${t.message}", Toast.LENGTH_SHORT).show()
                hideLoader()
            }
        })
    }

    private fun fetchMembership() {
        val preferenceManager = PreferenceManager(this)
        val api = RetrofitClient.instance.create(Api::class.java)
        val email = preferenceManager.getEmail() ?: return

        api.getMembership(email).enqueue(object : retrofit2.Callback<getMembershipResponse> {
            override fun onResponse(call: Call<getMembershipResponse>, response: Response<getMembershipResponse>) {
                if (response.isSuccessful) {
                    val membership = response.body() ?: return
                    if (membership.success) {
                        val formattedMembershipEnd = formatDate(membership.membership_end)
                        val formattedNextPaymentDate = formatDate(membership.next_payment_date)

                        findViewById<TextView>(R.id.tvMembershipEnd).text = formattedMembershipEnd
                        findViewById<TextView>(R.id.tvNextPaymentDate).text = formattedNextPaymentDate
                        findViewById<TextView>(R.id.tvStatus).text = membership.status
                    } else {
                        Toast.makeText(this@Account, "Failed to get membership details", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<getMembershipResponse>, t: Throwable) {
                Toast.makeText(this@Account, "Error ${t.message}", Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(this@Account, "Failed to fetch orders: Response is null", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("TrainerAccount", "Error: ${response.code()} - ${response.message()}")
                    Toast.makeText(this@Account, "Failed to fetch orders: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
                hideLoader()
            }

            override fun onFailure(call: Call<FetchOrdersResponse>, t: Throwable) {
                Log.e("TrainerAccount", "Network request failed: ${t.message}", t)
                Toast.makeText(this@Account, "Failed to fetch orders: ${t.message}", Toast.LENGTH_SHORT).show()
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

    private fun formatDate(dateStr: String?): String {
        if (dateStr.isNullOrEmpty()) return "N/A"

        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // Assuming API returns YYYY-MM-DD
            val outputFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()) // Example: January 01, 2025
            val date = inputFormat.parse(dateStr)
            outputFormat.format(date ?: return "Invalid date")
        } catch (e: Exception) {
            "Invalid date"
        }
    }
}