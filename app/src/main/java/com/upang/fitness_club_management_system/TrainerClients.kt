package com.upang.fitness_club_management_system

import android.content.Intent
import android.os.Bundle
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
import com.upang.fitness_club_management_system.model.TrainerRequestResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TrainerClients : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TrainerClientAdapter
    private lateinit var searchView: SearchView
    private var trainerRequests: MutableList<TrainerRequestResponse> = mutableListOf()
    private var filteredRequests: MutableList<TrainerRequestResponse> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trainer_clients)

        recyclerView = findViewById(R.id.clientRecyclerView)
        searchView = findViewById(R.id.searchView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        fetchEvents()

        // Bottom Navigation Setup
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.actionClients

        bottomNavigationView.setOnItemSelectedListener { item ->
            val targetActivity = when (item.itemId) {
                R.id.actionHomeTrainer -> TrainerHomeActivity::class.java
                R.id.actionSchedule -> TrainerScheduleActivity::class.java
                R.id.actionPost -> PostHighlightActivity::class.java
                R.id.actionClients -> TrainerClients::class.java
                R.id.actionShopTrainer -> Shop::class.java
                else -> null
            }

            if (targetActivity != null && targetActivity != javaClass) {
                startActivity(Intent(this, targetActivity))
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }
            true
        }

        // *Implement SearchView Listener*
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false  // We don't need to handle submission explicitly
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
            return
        }

        val api = RetrofitClient.instance.create(Api::class.java)
        api.fetchTrainerRequest(email).enqueue(object : Callback<List<TrainerRequestResponse>> {
            override fun onResponse(
                call: Call<List<TrainerRequestResponse>>,
                response: Response<List<TrainerRequestResponse>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    trainerRequests = response.body()!!.toMutableList()
                    filteredRequests.addAll(trainerRequests)

                    adapter = TrainerClientAdapter(filteredRequests)
                    recyclerView.adapter = adapter
                    adapter.notifyDataSetChanged()
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

    private fun filter(query: String) {
        filteredRequests.clear()
        if (query.isEmpty()) {
            filteredRequests.addAll(trainerRequests)
        } else {
            val lowerCaseQuery = query.lowercase()
            for (request in trainerRequests) {
                if (request.user_name.lowercase().contains(lowerCaseQuery)) {
                    filteredRequests.add(request)
                }
            }
        }
        adapter.notifyDataSetChanged()
    }
}