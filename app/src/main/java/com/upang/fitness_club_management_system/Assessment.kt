package com.upang.fitness_club_management_system

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Assessment : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_assessment)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val imgBtnRec = findViewById<android.widget.ImageButton>(R.id.imgBtnRec)
        val imgBtnRec2 = findViewById<android.widget.ImageButton>(R.id.imgBtnRec2)
        val imgBtnTrainee = findViewById<android.widget.ImageButton>(R.id.imgBtnTrainee)
        val imgBtnTrainer = findViewById<android.widget.ImageButton>(R.id.imgBtnTrainer)
        val btnContinueAssessment = findViewById<android.widget.Button>(R.id.btnContinueAssessment)


    }
}