package com.upang.fitness_club_management_system

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputLayout
import com.upang.fitness_club_management_system.api.Api
import com.upang.fitness_club_management_system.api.RetrofitClient
import com.upang.fitness_club_management_system.model.SendConfirmEmailRequest
import com.upang.fitness_club_management_system.model.SendConfirmEmailResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignupActivity : AppCompatActivity() {
    private var isPasswordVisible = false
    private lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etFullname = findViewById<EditText>(R.id.etFullname)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)
        val btnShowPass1 = findViewById<TextInputLayout>(R.id.btnShowPass1)
        val btnShowPass2 = findViewById<TextInputLayout>(R.id.btnShowPass2)

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Singing in...")
        progressDialog.setCancelable(false)

        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginPage::class.java))
            finish()
        }

        btnShowPass1.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
            } else {
                etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            }
            etPassword.setSelection(etPassword.text?.length ?: 0)
        }

        btnShowPass2.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
            } else {
                etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            }
            etPassword.setSelection(etPassword.text?.length ?: 0)
        }

        btnSignUp.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val fullname = etFullname.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            Log.e("SignUpActivity", "email: $email")

            when {
                fullname.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() ->
                    Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()

                !isValidEmail(email) ->
                    Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()

                password != confirmPassword ->
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()

                else -> sendEmailCode(email, fullname, password)
            }
        }
    }

    private fun sendEmailCode(email: String, fullname: String, password: String) {
        val api = RetrofitClient.instance.create(Api::class.java)
        val request = SendConfirmEmailRequest(email)
        progressDialog.show()
        api.GetEmailCode(request).enqueue(object : Callback<SendConfirmEmailResponse> {
            override fun onResponse(call: Call<SendConfirmEmailResponse>, response: Response<SendConfirmEmailResponse>) {
                if (response.isSuccessful) {
                    val intent = Intent(this@SignupActivity, ConfirmEmailActivity::class.java).apply {
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
    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailPattern.toRegex())
    }
}