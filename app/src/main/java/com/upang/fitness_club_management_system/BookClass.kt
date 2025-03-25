package com.upang.fitness_club_management_system

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

        val recyclerView = findViewById<RecyclerView>(R.id.rvTrainers)
        recyclerView.layoutManager = LinearLayoutManager(this)

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
