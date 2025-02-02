package com.upang.fitness_club_management_system

import android.content.Intent
import android.os.Bundle
import android.provider.Telephony.Mms.Intents
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.upang.fitness_club_management_system.api.Api
import com.upang.fitness_club_management_system.api.RetrofitClient
import com.upang.fitness_club_management_system.model.LoginRequest
import com.upang.fitness_club_management_system.model.LoginResponse
import okhttp3.Callback
import retrofit2.Call
import retrofit2.Response

class LoginPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val button = findViewById<Button>(R.id.button)

        button.setOnClickListener{
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()){
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Email and Password Required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        val api = RetrofitClient.instance.create(Api::class.java)
        val loginRequest = LoginRequest(email, password)

        api.loginUser(loginRequest).enqueue(object : retrofit2.Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val result = response.body()
                    result?.let {
                        if (it.token != null) {
                            Log.d("Login", "Success: ${it.message}, Token: ${it.token}")

                            val intent = Intent(this@LoginPage, MainActivity::class.java)
                            intent.putExtra("Token", it.token)
                            startActivity(intent)
                            finish()

                            Toast.makeText(applicationContext, "Login Successful!", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.d("Login", "Failed: ${it.message}")
                            Toast.makeText(applicationContext, "Login Failed: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(applicationContext, "Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e("Login", "Error: ${t.message}")
                Toast.makeText(applicationContext, "Login request failed", Toast.LENGTH_SHORT).show()
            }
        })
    }

}


