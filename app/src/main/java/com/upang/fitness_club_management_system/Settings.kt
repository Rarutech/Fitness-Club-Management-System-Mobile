package com.upang.fitness_club_management_system

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.appcompat.widget.Toolbar
import android.content.Intent
import android.widget.TextView
import com.upang.fitness_club_management_system.helper.PreferenceManager

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
            finish()
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
    }

}
