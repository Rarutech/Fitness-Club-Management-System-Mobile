package com.upang.fitness_club_management_system

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton


class MembershipFee : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_membership_fee)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val imageBackButton = findViewById<ImageButton>(R.id.imageBackButton)
        imageBackButton.setOnClickListener {
            val intent = Intent(this, MembershipBenefits::class.java)
            startActivity(intent)
            finish()
        }

        val btnRadio1 = findViewById<RadioButton>(R.id.btnRadio1)
        val btnRadio2 = findViewById<RadioButton>(R.id.btnRadio2)
        val btnPayNow = findViewById<MaterialButton>(R.id.btnPayNow)
        val btnPayAtCounter = findViewById<MaterialButton>(R.id.btnPayAtCounter)
        val btnContinueMembership = findViewById<Button>(R.id.btnContinueMembership)

        // When "Pay Now" button is clicked, select btnRadio1
        btnPayNow.setOnClickListener {
            btnRadio1.isChecked = true
            btnRadio2.isChecked = false
        }

        // When "Pay at Counter" button is clicked, select btnRadio2
        btnPayAtCounter.setOnClickListener {
            btnRadio2.isChecked = true
            btnRadio1.isChecked = false
        }

        // Continue button navigates to respective activity based on selected radio button
        btnContinueMembership.setOnClickListener {
            when {
                btnRadio1.isChecked -> {
                    val intent = Intent(this, CardInformation::class.java)
                    startActivity(intent)
                    finish()
                }
                btnRadio2.isChecked -> {
                    val intent = Intent(this, QRCodeMembershipFee::class.java)
                    startActivity(intent)
                    finish()
                }
                else -> {
                    Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
