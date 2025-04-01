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
import com.upang.fitness_club_management_system.model.ConfirmEmailRequest
import com.upang.fitness_club_management_system.model.ConfirmEmailResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgotPasswordEmailVerification : AppCompatActivity() {
    private lateinit var btnConfirmEmail: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgot_password_email_verification)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val otp1 = findViewById<EditText>(R.id.etOTP1)
        val otp2 = findViewById<EditText>(R.id.etOTP2)
        val otp3 = findViewById<EditText>(R.id.etOTP3)
        val otp4 = findViewById<EditText>(R.id.etOTP4)

        btnConfirmEmail = findViewById(R.id.btnConfirmEmail)

        val bundle = intent.extras

        val email = bundle?.getString("email").toString()

        btnConfirmEmail.setOnClickListener {
            val code = otp1.text.toString().trim() + otp2.text.toString().trim() + otp3.text.toString().trim() + otp4.text.toString().trim()
            Log.d("EMAIL CONFIRM", "Email: ${email} Code: ${code}")
            confirmEmail(email,code)
        }


    }
    private fun confirmEmail(email: String, code: String){
        val api = RetrofitClient.instance.create(Api::class.java)
        val confirmEmailRequest = ConfirmEmailRequest(email,code)

        api.ConfirmEmail(confirmEmailRequest).enqueue(object : Callback<ConfirmEmailResponse> {
            override fun onResponse(call: Call<ConfirmEmailResponse>, response: Response<ConfirmEmailResponse>) {
                if (response.isSuccessful){
                    val intent = Intent(this@ForgotPasswordEmailVerification, CreateNewPassword::class.java).apply {
                        putExtra("email", email)
                    }
                    startActivity(intent)
                    finish()
                } else {
                    Log.e("EMAIL CONFIRM", "Error: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ConfirmEmailResponse>, t: Throwable) {
                Log.e("EMAIL CONFIRM", "Error: ${t.message}")
            }
        })
    }
}