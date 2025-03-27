package com.upang.fitness_club_management_system

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.upang.fitness_club_management_system.adapter.TraineeAppointmentAdapter
import com.upang.fitness_club_management_system.api.Api
import com.upang.fitness_club_management_system.api.RetrofitClient
import com.upang.fitness_club_management_system.helper.PreferenceManager
import com.upang.fitness_club_management_system.model.TraineeRequestApiResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TraineeAppointments : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TraineeAppointmentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_trainee_appointments)

        recyclerView = findViewById(R.id.rvTrainers)
        recyclerView.layoutManager = LinearLayoutManager(this)

        fetchAppointments()
    }

    private fun fetchAppointments() {
        val api = RetrofitClient.instance.create(Api::class.java)
        val preferenceManager = PreferenceManager(this)
        val email = preferenceManager.getEmail()

        if (email == null) {
            return
        }

        api.fetchTraineeRequest(email).enqueue(object : Callback<TraineeRequestApiResponse> {
            override fun onResponse(
                call: Call<TraineeRequestApiResponse>,
                response: Response<TraineeRequestApiResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val appointments = response.body()!!.requests
                    adapter = TraineeAppointmentAdapter(appointments)
                    recyclerView.adapter = adapter
                } else {
                    Toast.makeText(this@TraineeAppointments, "Failed to fetch data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<TraineeRequestApiResponse>, t: Throwable) {
                Log.e("API_ERROR", t.message.toString())
                Toast.makeText(this@TraineeAppointments, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
