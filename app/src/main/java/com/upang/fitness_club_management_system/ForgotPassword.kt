package com.upang.fitness_club_management_system

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.upang.fitness_club_management_system.api.Api
import com.upang.fitness_club_management_system.api.RetrofitClient
import com.upang.fitness_club_management_system.model.SendConfirmEmailRequest
import com.upang.fitness_club_management_system.model.SendConfirmEmailResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgotPassword : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgot_password)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val etEmail: EditText = findViewById(R.id.etEmail)
        val btnSend: Button = findViewById(R.id.btnSend)

        btnSend.setOnClickListener {
            val email = etEmail.text.toString().trim()
            sendEmailCode(email)
        }
    }
    private fun sendEmailCode(email: String) {
        val api = RetrofitClient.instance.create(Api::class.java)
        val request = SendConfirmEmailRequest(email)

        api.GetEmailCode(request).enqueue(object : Callback<SendConfirmEmailResponse> {
            override fun onResponse(call: Call<SendConfirmEmailResponse>, response: Response<SendConfirmEmailResponse>) {
                if (response.isSuccessful) {
                    val intent = Intent(this@ForgotPassword, ForgotPasswordEmailVerification::class.java).apply {
                        putExtra("email", email)
                    }
                    startActivity(intent)
                    finish()
                } else {
                    Log.e("EMAIL CODE", "Error: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<SendConfirmEmailResponse>, t: Throwable) {
                Log.e("EMAIL CODE", "Error: ${t.message}")
            }
        })
    }

}