package com.upang.fitness_club_management_system

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
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

class Trainee_Home : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var highlightAdapter: HighlightAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_trainee_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val btnAccount = findViewById<ImageButton>(R.id.btnAccount)
        btnAccount.setOnClickListener {
            val intent = Intent(this,Account::class.java)
            startActivity(intent)
        }
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.actionHome

        when (javaClass) {
            Trainee_Home::class.java -> bottomNavigationView.selectedItemId = R.id.actionHome
            Progress::class.java -> bottomNavigationView.selectedItemId = R.id.actionProgress
            BookClass::class.java -> bottomNavigationView.selectedItemId = R.id.actionClasses
            Shop::class.java -> bottomNavigationView.selectedItemId = R.id.actionShop
        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            val targetActivity = when (item.itemId) {
                R.id.actionHome -> Trainee_Home::class.java
                R.id.actionProgress -> Progress::class.java
                R.id.actionClasses -> BookClass::class.java
                R.id.actionShop -> Shop::class.java
                else -> null
            }

            if (targetActivity != null && targetActivity != javaClass) {
                startActivity(Intent(this, targetActivity))
                finish()
            }
            true
        }

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