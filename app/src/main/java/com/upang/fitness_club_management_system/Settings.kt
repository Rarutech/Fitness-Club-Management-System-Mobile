package com.upang.fitness_club_management_system

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.appcompat.widget.Toolbar
import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.upang.fitness_club_management_system.api.Api
import com.upang.fitness_club_management_system.api.RetrofitClient
import com.upang.fitness_club_management_system.helper.PreferenceManager
import com.upang.fitness_club_management_system.model.LogoutResponse
import com.upang.fitness_club_management_system.model.Utils
import retrofit2.Call
import retrofit2.Response

class Settings : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbarSettings)
        toolbar.setNavigationOnClickListener {
            val preferenceManager = PreferenceManager(this)
            val role = preferenceManager.getRole()
            if (role == "trainer") {
                val intent = Intent(this, TrainerAccount::class.java)
                startActivity(intent)
                return@setNavigationOnClickListener
            } else {
                val intent = Intent(this, Account::class.java)
                startActivity(intent)
                return@setNavigationOnClickListener
            }
        }

        val editProfile = findViewById<TextView>(R.id.tvEditProfile)
        editProfile.setOnClickListener {
            val preferenceManager = PreferenceManager(this)
            val role = preferenceManager.getRole()
            if (role == "trainer") {
                val intent = Intent(this, EditTrainerProfile::class.java)
                startActivity(intent)
                return@setOnClickListener
            } else if (role == "member") {
                val intent = Intent(this, EditProfile::class.java)
                startActivity(intent)
                return@setOnClickListener
            }
        }

        val changePassword = findViewById<TextView>(R.id.tvChangePass)
        changePassword.setOnClickListener {
            val intent = Intent(this, ChangePassword::class.java)
            startActivity(intent)
        }
        val logout = findViewById<TextView>(R.id.tvLogout)
        logout.setOnClickListener {
            val ConfirmPassword = findViewById<LinearLayout>(R.id.ConfirmLogout)

            ConfirmPassword.visibility = View.VISIBLE

        }

        val btnYes = findViewById<TextView>(R.id.btnYes)
        btnYes.setOnClickListener {
            logout()
        }

        val btnNo = findViewById<TextView>(R.id.btnNo)
        btnNo.setOnClickListener {
            val ConfirmPassword = findViewById<LinearLayout>(R.id.ConfirmLogout)
            ConfirmPassword.visibility = View.GONE
        }
        val tvAboutUs = findViewById<TextView>(R.id.tvAboutUs)
        tvAboutUs.setOnClickListener {
            val intent = Intent(this, AboutUs::class.java)
            startActivity(intent)
        }
        val privacyPolicy = findViewById<TextView>(R.id.PrivacyPolicy)
        privacyPolicy.setOnClickListener {
            val intent = Intent(this, PrivacyPolicy::class.java)
            startActivity(intent)
        }
    }
    private fun logout(){
        val preferenceManager = PreferenceManager(this)
        val apiService = RetrofitClient.instance.create(Api::class.java)
        val email = preferenceManager.getEmail() ?: return

        apiService.logout(email).enqueue(object  : retrofit2.Callback<LogoutResponse>{
            override fun onResponse(
                call: Call<LogoutResponse>,
                response: Response<LogoutResponse>
            ) {
                if(response.isSuccessful){
                    val preferenceManager = PreferenceManager(this@Settings)
                    preferenceManager.clear()
                    Utils.checkAuthentication(this@Settings)
                }

            }

            override fun onFailure(call: Call<LogoutResponse>, t: Throwable) {
                TODO("Not yet implemented")
            }
        })

    }
}
