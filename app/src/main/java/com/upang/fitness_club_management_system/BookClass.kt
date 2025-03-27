package com.upang.fitness_club_management_system

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.upang.fitness_club_management_system.adapter.TrainersAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.upang.fitness_club_management_system.api.Api
import com.upang.fitness_club_management_system.model.FetchTrainersResponse
import com.upang.fitness_club_management_system.api.RetrofitClient

class BookClass : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_book_class)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val fabAppointment = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabAppointment)

        fabAppointment.setOnClickListener{
            val intent = Intent(this, TraineeAppointments::class.java)
            startActivity(intent)
        }

        val recyclerView = findViewById<RecyclerView>(R.id.rvTrainers)
        recyclerView.layoutManager = LinearLayoutManager(this)

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

        fetchTrainers(recyclerView)
    }

    private fun fetchTrainers(recyclerView: RecyclerView) {
        val api = RetrofitClient.instance.create(Api::class.java)
        api.fetchAllTrainers().enqueue(object : Callback<FetchTrainersResponse> {
            override fun onResponse(
                call: Call<FetchTrainersResponse>,
                response: Response<FetchTrainersResponse>
            ) {
                if (response.isSuccessful) {
                    val trainers = response.body()?.profiles ?: emptyList()
                    recyclerView.adapter = TrainersAdapter(this@BookClass, trainers)
                } else {
                    Toast.makeText(this@BookClass, "Failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<FetchTrainersResponse>, t: Throwable) {
                Toast.makeText(this@BookClass, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


}
