package com.upang.fitness_club_management_system

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.upang.fitness_club_management_system.adapter.HighlightAdapter
import com.upang.fitness_club_management_system.api.Api
import com.upang.fitness_club_management_system.api.RetrofitClient
import com.upang.fitness_club_management_system.model.HighlightResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class TrainerHomeActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var highlightAdapter: HighlightAdapter
    private lateinit var btnUpload: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_trainer_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.recyclerView)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        //Bottom Navigation
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        when (javaClass) {
            TrainerHomeActivity::class.java -> bottomNavigationView.selectedItemId = R.id.actionHomeTrainer
            TrainerScheduleActivity::class.java -> bottomNavigationView.selectedItemId = R.id.actionSchedule
            PostHighlightActivity::class.java -> bottomNavigationView.selectedItemId = R.id.actionPost
            TrainerClients::class.java -> bottomNavigationView.selectedItemId = R.id.actionClients
            Shop::class.java -> bottomNavigationView.selectedItemId = R.id.actionShopTrainer
        }

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



        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        fetchHighlights()
    }
    private fun fetchHighlights() {
        val api = RetrofitClient.instance.create(Api::class.java)

        api.GetHighlights().enqueue(object : Callback<HighlightResponse> {
            override fun onResponse(call: Call<HighlightResponse>, response: Response<HighlightResponse>) {
                if (response.isSuccessful) {
                    val highlights = response.body()?.highlights ?: emptyList()
                    highlightAdapter = HighlightAdapter(highlights)
                    recyclerView.adapter = highlightAdapter
                    Log.e("API", "Fetching Success")
                    Log.d("API","Response: ${response.body()}")
                } else {
                    Log.e("API", "Response not successful")
                }
            }

            override fun onFailure(call: Call<HighlightResponse>, t: Throwable) {
                Log.e("API", "Failed to fetch data: ${t.message}")
            }

        })
    }
}