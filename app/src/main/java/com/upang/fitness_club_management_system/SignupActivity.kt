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
import com.upang.fitness_club_management_system.model.SendConfirmEmailRequest
import com.upang.fitness_club_management_system.model.SendConfirmEmailResponse
import com.upang.fitness_club_management_system.model.SignUpRequest
import com.upang.fitness_club_management_system.model.SignUpResponse
import retrofit2.Callback
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
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val etPassword=findViewById<EditText>(R.id.etPassword)
        val btnSignUp=findViewById<Button>(R.id.btnSignUp)

        btnSignUp.setOnClickListener{

            val intent = Intent(this, ConfirmEmailActivity::class.java)
            val email = etEmail.text.toString().trim()
            val fullname = etFullname.text.toString().trim()
            val password = etPassword.text.toString().trim()

            val confirmPassword = etConfirmPassword.text.toString().trim()
            if (fullname.isEmpty() || password.isEmpty() || email.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "All fields is required", Toast.LENGTH_SHORT).show()
            } else if (confirmPassword != password){
                Toast.makeText(this, "Password does not match", Toast.LENGTH_SHORT).show()
            }
            else {
                sendEmailCode(email, fullname, password)
            }
        }
    }
    private fun sendEmailCode(email: String, fullname: String, password: String){
        val api = RetrofitClient.instance.create(Api::class.java)
        val sendConfirmEmailRequest = SendConfirmEmailRequest(email)

        api.GetEmailCode(sendConfirmEmailRequest).enqueue(object : Callback<SendConfirmEmailResponse>{
            override fun onResponse(call: retrofit2.Call<SendConfirmEmailResponse>, response: Response<SendConfirmEmailResponse>) {
                if (response.isSuccessful) {
                    val bundle = Bundle()

                    bundle.putString("email",email)
                    bundle.putString("fullname",fullname)
                    bundle.putString("password",password)
                    val intent = Intent(this@SignupActivity,ConfirmEmailActivity::class.java)
                    intent.putExtras(bundle)
                    startActivity(intent)
                    finish()
                }
                else {
                    Log.e("EMAIL CODE","Error: ${response.message()}")
                }
            }

            override fun onFailure(call: retrofit2.Call<SendConfirmEmailResponse>, t: Throwable) {
                Log.e("EMAIL CODE", "Error ${t.message}")
            }
        })
    }
}



