package com.upang.fitness_club_management_system

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.upang.fitness_club_management_system.api.Api
import com.upang.fitness_club_management_system.api.RetrofitClient
import com.upang.fitness_club_management_system.helper.PreferenceManager
import com.upang.fitness_club_management_system.model.ForgotPasswordResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateNewPassword : AppCompatActivity() {
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_new_password)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnShowPass2CNP = findViewById<TextInputLayout>(R.id.btnShowPass2CNP)
        val btnShowPass3CNP = findViewById<TextInputLayout>(R.id.btnShowPass3CNP)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val btnChangePass = findViewById<Button>(R.id.btnChangePass)
        val btnBack = findViewById<ImageButton>(R.id.imageBackButton)

        btnShowPass2CNP.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            togglePasswordVisibility(etPassword, isPasswordVisible)
        }

        btnShowPass3CNP.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            togglePasswordVisibility(etConfirmPassword, isPasswordVisible)
        }

        val bundle = intent.extras
        var email = bundle?.getString("email").orEmpty()

        if (email.isEmpty()) {
            val preferenceManager = PreferenceManager(this)
            email = preferenceManager.getEmail() ?: return
        }

        btnChangePass.setOnClickListener {
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (password == confirmPassword && email.isNotEmpty()) {
                forgotPassword(email, password)
            } else {
                Toast.makeText(this, "Password does not match or email is missing", Toast.LENGTH_SHORT).show()
            }
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun togglePasswordVisibility(editText: TextInputEditText, isVisible: Boolean) {
        editText.transformationMethod =
            if (isVisible) HideReturnsTransformationMethod.getInstance()
            else PasswordTransformationMethod.getInstance()
        editText.setSelection(editText.text?.length ?: 0)
    }

    private fun forgotPassword(email: String, password: String) {
        val api = RetrofitClient.instance.create(Api::class.java)
        api.forgotPassword(email, password).enqueue(object : Callback<ForgotPasswordResponse> {
            override fun onResponse(
                call: Call<ForgotPasswordResponse>,
                response: Response<ForgotPasswordResponse>
            ) {
                if (response.isSuccessful) {
                    val forgotPasswordResponse = response.body()
                    if (forgotPasswordResponse != null && forgotPasswordResponse.success) {
                        Toast.makeText(this@CreateNewPassword, "Password Changed", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@CreateNewPassword, LoginPage::class.java))
                        finish()
                    } else {
                        Log.e("CreateNewPassword", "email: $email, password: $password")
                        Toast.makeText(
                            this@CreateNewPassword,
                            forgotPasswordResponse?.message ?: "Failed to change password",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(this@CreateNewPassword, "Failed to change password", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ForgotPasswordResponse>, t: Throwable) {
                Toast.makeText(this@CreateNewPassword, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
