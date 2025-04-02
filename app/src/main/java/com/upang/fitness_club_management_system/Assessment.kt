package com.upang.fitness_club_management_system

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.upang.fitness_club_management_system.api.Api
import com.upang.fitness_club_management_system.api.RetrofitClient
import com.upang.fitness_club_management_system.helper.PreferenceManager
import com.upang.fitness_club_management_system.model.makeMemberRequest
import com.upang.fitness_club_management_system.model.makeTrainerResponse
import retrofit2.Call
import retrofit2.Response

class Assessment : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_assessment)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val btnTrainee = findViewById<ImageButton>(R.id.imgBtnRec)
        val btnTrainer = findViewById<ImageButton>(R.id.imgBtnRec2)

        btnTrainee.setOnClickListener {
            makeMember()
        }

        btnTrainer.setOnClickListener {
            makeTrainer()
        }

    }

    private fun makeTrainer(){
        val preferenceManager = PreferenceManager(this)
        val email = preferenceManager.getEmail().toString()
        val api = RetrofitClient.instance.create(Api::class.java)

        api.makeTrainer(email).enqueue(object : retrofit2.Callback<makeTrainerResponse> {
            override fun onResponse(call: Call<makeTrainerResponse>, response: Response<makeTrainerResponse>) {
                if (response.isSuccessful) {
                    val makeTrainerResponse = response.body()
                    if (makeTrainerResponse != null) {
                        if (makeTrainerResponse.status == "success") {
                            val intent = Intent(this@Assessment, TrainerHomeActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            val errorMessage = makeTrainerResponse.message
                            Toast.makeText(this@Assessment, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    val errorMessage = response.errorBody()?.string()
                    Toast.makeText(this@Assessment, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<makeTrainerResponse>, t: Throwable) {
                Toast.makeText(this@Assessment, "Failed to make trainer: ${t.message}", Toast.LENGTH_SHORT).show()
            }

        })

    }

    private fun makeMember(){
        val preferenceManager = PreferenceManager(this)
        val email = preferenceManager.getEmail().toString()
        val api = RetrofitClient.instance.create(Api::class.java)

        api.makeMember(email).enqueue(object : retrofit2.Callback<makeMemberRequest> {
            override fun onResponse(call: Call<makeMemberRequest>, response: Response<makeMemberRequest>) {
                if (response.isSuccessful) {
                    val makeMemberResponse = response.body()
                    if (makeMemberResponse != null) {
                        if (makeMemberResponse.status == "success") {
                            val intent = Intent(this@Assessment, Trainee_Home::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            val errorMessage = makeMemberResponse.message
                            Toast.makeText(this@Assessment, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    val errorMessage = response.errorBody()?.string()
                    Toast.makeText(this@Assessment, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<makeMemberRequest>, t: Throwable) {
                Toast.makeText(this@Assessment, "Failed to make member: ${t.message}", Toast.LENGTH_SHORT).show()
            }

        })
    }
}