package com.upang.fitness_club_management_system

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.upang.fitness_club_management_system.adapter.TrainerClientAdapter
import com.upang.fitness_club_management_system.api.Api
import com.upang.fitness_club_management_system.api.RetrofitClient
import com.upang.fitness_club_management_system.helper.PreferenceManager
import com.upang.fitness_club_management_system.model.TrainerRequestApiResponse
import com.upang.fitness_club_management_system.model.TrainerRequestResponse
import com.upang.fitness_club_management_system.model.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class TrainerClients : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TrainerClientAdapter
    private lateinit var searchView: SearchView
    private var trainerRequests: MutableList<TrainerRequestResponse> = mutableListOf()
    private var filteredRequests: MutableList<TrainerRequestResponse> = mutableListOf()
    private val handler = Handler()
    private val delay: Long = 5000
    private val runnable = object : Runnable {
        override fun run() {
            Utils.getNotifications(this@TrainerClients)
            handler.postDelayed(this, delay)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trainer_clients)
        Utils.getNotifications(this)


        recyclerView = findViewById(R.id.clientRecyclerView)
        searchView = findViewById(R.id.searchView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        searchView.isFocusable = true
        searchView.isFocusableInTouchMode = true
        searchView.clearFocus()

        fetchEvents()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        bottomNavigationView.selectedItemId = R.id.actionClients

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

        // Implement SearchView Listener
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filter(newText.orEmpty())  // Filter on text change
                return true
            }
        })
    }

    private fun fetchEvents() {
        val preferenceManager = PreferenceManager(this)
        val email = preferenceManager.getEmail()

        if (email == null) {
            Toast.makeText(this, "Email not found. Please log in.", Toast.LENGTH_SHORT).show()
            Log.e("TrainerClients", "No email found in SharedPreferences")
            return
        }

        // Log the email being sent in the request
        Log.d("TrainerClients", "Fetching trainer requests for email: $email")

        val api = RetrofitClient.instance.create(Api::class.java)
        api.fetchTrainerRequest(email).enqueue(object : Callback<TrainerRequestApiResponse> {
            override fun onResponse(
                call: Call<TrainerRequestApiResponse>,
                response: Response<TrainerRequestApiResponse>
            ) {
                if (response.isSuccessful) {
                    val responseBody = response.body()

                    // Log full response body
                    Log.d("TrainerClients", "API Response: $responseBody")

                    if (responseBody != null) {
                        val fetchedRequests = responseBody.requests

                        // Log the number of received requests
                        Log.d("TrainerClients", "Number of requests received: ${fetchedRequests.size}")

                        trainerRequests.clear()
                        filteredRequests.clear()
                        trainerRequests.addAll(fetchedRequests)
                        filteredRequests.addAll(trainerRequests)

                        adapter = TrainerClientAdapter(filteredRequests)
                        recyclerView.adapter = adapter
                        adapter.notifyDataSetChanged()
                    } else {
                        Log.e("TrainerClients", "Response body is null")
                        Toast.makeText(applicationContext, "Failed to load data", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("TrainerClients", "Response failed: ${response.errorBody()?.string()}")
                    Toast.makeText(applicationContext, "Failed to load data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<TrainerRequestApiResponse>, t: Throwable) {
                Log.e("TrainerClients", "API call failed: ${t.message}")
                Toast.makeText(applicationContext, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }





    private fun filter(query: String) {
        filteredRequests.clear()
        if (query.isEmpty()) {
            filteredRequests.addAll(trainerRequests)
        } else {
            val lowerCaseQuery = query.lowercase(Locale.getDefault())
            for (request in trainerRequests) {
                if (request.user_name.lowercase(Locale.getDefault()).contains(lowerCaseQuery)) {
                    filteredRequests.add(request)
                }
            }
        }
        adapter.notifyDataSetChanged() // ✅ Ensure the adapter updates
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
