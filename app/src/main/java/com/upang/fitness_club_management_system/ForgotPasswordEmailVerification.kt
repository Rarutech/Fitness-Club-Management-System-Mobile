package com.upang.fitness_club_management_system

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.upang.fitness_club_management_system.api.Api
import com.upang.fitness_club_management_system.api.RetrofitClient
import com.upang.fitness_club_management_system.model.ConfirmEmailRequest
import com.upang.fitness_club_management_system.model.ConfirmEmailResponse
import com.upang.fitness_club_management_system.model.SendConfirmEmailRequest
import com.upang.fitness_club_management_system.model.SendConfirmEmailResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgotPasswordEmailVerification : AppCompatActivity() {
    private lateinit var btnConfirmEmail: Button
    private lateinit var progressDialog: ProgressDialog
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
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Resending code...")
        progressDialog.setCancelable(false)

        val tvResendCode = findViewById<TextView>(R.id.tvResendCode)
        tvResendCode.setOnClickListener {
            val bundle = intent.extras
            val email = bundle?.getString("email").toString()
            val fullname = bundle?.getString("fullname").toString()
            val password = bundle?.getString("password").toString()
            progressDialog.show()
            sendEmailCode(email,fullname,password)
        }

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
    private fun sendEmailCode(email: String, fullname: String, password: String) {
        val api = RetrofitClient.instance.create(Api::class.java)
        val request = SendConfirmEmailRequest(email)
        progressDialog.show()
        api.GetEmailCode(request).enqueue(object : Callback<SendConfirmEmailResponse> {
            override fun onResponse(call: Call<SendConfirmEmailResponse>, response: Response<SendConfirmEmailResponse>) {
                if (response.isSuccessful) {
                    val intent = Intent(this@ForgotPasswordEmailVerification, ConfirmEmailActivity::class.java).apply {
                        putExtra("email", email)
                        putExtra("fullname", fullname)
                        putExtra("password", password)
                    }
                    progressDialog.dismiss()
                    startActivity(intent)
                    finish()
                } else {
                    progressDialog.dismiss()
                    Log.e("EMAIL CODE", "Error: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<SendConfirmEmailResponse>, t: Throwable) {
                progressDialog.dismiss()
                Log.e("EMAIL CODE", "Error: ${t.message}")
            }
        })
    }
}