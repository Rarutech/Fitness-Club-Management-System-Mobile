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
import com.upang.fitness_club_management_system.model.MakeTrainer

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
        val btn_trainee = findViewById<ImageButton>(R.id.imgBtnRec)
        val btn_Trainer = findViewById<ImageButton>(R.id.imgBtnRec2)

        btn_trainee.setOnClickListener {
            makeTrainee()
        }

        btn_Trainer.setOnClickListener {
            makeTrainer()
        }
    }
    private fun makeTrainee(){
        val email = PreferenceManager(this).getEmail() ?: return
        val api = RetrofitClient.instance.create(Api::class.java)

        api.makeMember(email).enqueue(object : retrofit2.Callback<MakeTrainer>{
            override fun onResponse(call: retrofit2.Call<MakeTrainer>, response: retrofit2.Response<MakeTrainer>) {
                if (response.isSuccessful) {
                    val makeTrainerResponse = response.body()
                    if (makeTrainerResponse != null) {

                        if (makeTrainerResponse.status == "success") {
                            val intent = Intent(this@Assessment, Trainee_Home::class.java)
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

            override fun onFailure(call: retrofit2.Call<MakeTrainer>, t: Throwable) {
                Toast.makeText(this@Assessment, "Failed to make trainer: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun makeTrainer(){
        val email = PreferenceManager(this).getEmail() ?: return
        val api = RetrofitClient.instance.create(Api::class.java)

        api.makeTrainer(email).enqueue(object : retrofit2.Callback<MakeTrainer>{
            override fun onResponse(call: retrofit2.Call<MakeTrainer>, response: retrofit2.Response<MakeTrainer>) {
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

            override fun onFailure(call: retrofit2.Call<MakeTrainer>, t: Throwable) {
                Toast.makeText(this@Assessment, "Failed to make trainer: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}