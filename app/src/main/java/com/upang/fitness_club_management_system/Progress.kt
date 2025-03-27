package com.upang.fitness_club_management_system

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class Progress : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_progress)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.actionHome

        when (javaClass) {
            Trainee_Home::class.java -> bottomNavigationView.selectedItemId = R.id.actionHome
            Progress::class.java -> bottomNavigationView.selectedItemId = R.id.actionProgress
            BookClass::class.java -> bottomNavigationView.selectedItemId = R.id.actionClasses
            Shop::class.java -> bottomNavigationView.selectedItemId = R.id.actionShop
        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            val targetActivity = when (item.itemId) {
                R.id.actionHome -> Trainee_Home::class.java
                R.id.actionProgress -> Progress::class.java
                R.id.actionClasses -> BookClass::class.java
                R.id.actionShop -> Shop::class.java
                else -> null
            }

            if (targetActivity != null && targetActivity != javaClass) {
                startActivity(Intent(this, targetActivity))
                finish()
            }
            true
        }

    }
}