package com.upang.fitness_club_management_system

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.upang.fitness_club_management_system.helper.PreferenceManager
import com.upang.fitness_club_management_system.model.Utils

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        saveEssen()
        checkAuthenticationAfterDelay()

    }
    private fun saveEssen() {
        val token = intent.getStringExtra("token")
        val email = intent.getStringExtra("email")
        val role = intent.getStringExtra("role")

        val preferenceManager = PreferenceManager(this)
        token?.let { preferenceManager.saveToken(it) }
        email?.let { preferenceManager.saveEmail(it) }
        role?.let { preferenceManager.saveRole(it)}
    }

    private fun checkAuthenticationAfterDelay() {
        Handler(Looper.getMainLooper()).postDelayed({
            Utils.checkAuthentication(this)
        }, 200)

        val preferenceManager = PreferenceManager(this)
        val role = preferenceManager.getRole().toString()

        if (role == "trainer") {
            Log.d("Role", "Current Role: ${role}")
            val intent  = Intent(this, TrainerHomeActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Log.d("Role","Role: ${role}")
            val intent  = Intent(this, Trainee_Home::class.java)
            startActivity(intent)
            finish()
        }
    }

}