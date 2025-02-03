package com.upang.fitness_club_management_system

import android.content.Intent
import android.os.Bundle
import android.telecom.Call
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
import com.upang.fitness_club_management_system.model.LoginResponse
import com.upang.fitness_club_management_system.model.SignUpRequest
import com.upang.fitness_club_management_system.model.SignUpResponse
import retrofit2.Response

class SignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val etFullname=findViewById<EditText>(R.id.etFullname)
        val etEmail=findViewById<EditText>(R.id.etEmail)
        val etPassword=findViewById<EditText>(R.id.etPassword)
        val btnSignUp=findViewById<Button>(R.id.btnSignUp)

        btnSignUp.setOnClickListener{
            val email = etEmail.text.toString().trim()
            val fullname = etFullname.text.toString().trim()
            val password = etPassword.text.toString().trim()
            SignUp(email,fullname,password)
        }
    }
        private fun SignUp(email:String,fullname:String,password:String){
            val api = RetrofitClient.instance.create(Api::class.java)
            val signUpRequest = SignUpRequest(fullname,email,password)

            api.SignUp(signUpRequest).enqueue(object : retrofit2.Callback<SignUpResponse>{
                override fun onResponse(call: retrofit2.Call<SignUpResponse>, response: Response<SignUpResponse>)
                {
                    if (response.isSuccessful){
                        val result = response.body()
                        Log.d("Signup", "Error:${response.message()} ")
                        if (result?.message=="User created successfully"){
                            Toast.makeText(applicationContext, "User created successfully", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@SignupActivity,LoginPage::class.java)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        Log.d("Signup","Error:${response.message()}")
                    }
                }
                override fun onFailure(call: retrofit2.Call<SignUpResponse>, t: Throwable) {
                    Log.d("Signup","Error:${t.message}")
                }
            })

        }
}



