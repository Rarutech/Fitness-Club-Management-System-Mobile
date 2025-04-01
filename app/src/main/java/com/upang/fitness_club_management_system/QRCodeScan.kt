package com.upang.fitness_club_management_system

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.BarcodeView
import com.upang.fitness_club_management_system.api.Api
import com.upang.fitness_club_management_system.api.RetrofitClient
import com.upang.fitness_club_management_system.helper.PreferenceManager
import com.upang.fitness_club_management_system.model.CheckInResponse
import com.upang.fitness_club_management_system.model.CheckOutResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class QRCodeScan : AppCompatActivity() {
    private lateinit var barcodeView: BarcodeView
    private lateinit var txtResult: TextView
    private var scannedData: String? = null
    private lateinit var sharedPreferences: android.content.SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_qrcode_scan)

        val toolbar: Toolbar = findViewById(R.id.toolbar2)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            val preferenceManager = PreferenceManager(this)
            val role = preferenceManager.getRole()
            if (role == "trainer") {
                val intent = Intent(this, TrainerAccount::class.java)
                startActivity(intent)
                return@setNavigationOnClickListener
            } else if (role == "member") {
                val intent = Intent(this, Account::class.java)
                startActivity(intent)
                return@setNavigationOnClickListener
            }
        }

        barcodeView = findViewById(R.id.barcode_scanner)
        txtResult = findViewById(R.id.txtResult)
        txtResult.text = "Scan A QR Code"

        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
        } else {
            startScanner()
        }
    }

    private fun startScanner() {
        barcodeView.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {
                Log.d("QRCodeScan", "QR Code Detected") // Log when a QR code is detected
                if (result != null) {
                    Log.d("QRCodeScan", "Scanned Data: ${result.text}") // Log the scanned result

                    if (scannedData != result.text) {
                        scannedData = result.text
                        txtResult.text = "Scan successful!"

                        val storedCheckInId = getCheckInId()
                        Log.d("QRCodeScan", "Stored CheckInId: $storedCheckInId") // Log check-in status

                        if (storedCheckInId == -1) {
                            Log.d("QRCodeScan", "Calling checkIn()") // Log check-in attempt
                            checkIn(scannedData!!)
                        } else {
                            Log.d("QRCodeScan", "Calling checkOut()") // Log check-out attempt
                            checkOut(storedCheckInId, scannedData!!)
                        }
                    }
                }
            }
        })
        barcodeView.resume()
    }


    private fun checkIn(scannedString: String) {
        val preferenceManager = PreferenceManager(this)
        val email = preferenceManager.getEmail() ?: return
        val api = RetrofitClient.instance.create(Api::class.java)

        api.checkIn(scannedString, email).enqueue(object : Callback<CheckInResponse> {
            override fun onResponse(call: Call<CheckInResponse>, response: Response<CheckInResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val checkInId = response.body()?.id
                    if (checkInId != null) {
                        saveCheckInId(checkInId)
                        Toast.makeText(this@QRCodeScan, "Check-in successful!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@QRCodeScan, Trainee_Home::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@QRCodeScan, "Check-in ID missing!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@QRCodeScan, "Check-in failed!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CheckInResponse>, t: Throwable) {
                Toast.makeText(this@QRCodeScan, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("QRCodeScan", "Check-in failed: ${t.message}")
            }
        })
    }

    private fun checkOut(checkInId: Int, scannedString: String) {
        val api = RetrofitClient.instance.create(Api::class.java)

        api.checkOut(checkInId, scannedString).enqueue(object : Callback<CheckOutResponse> {
            override fun onResponse(call: Call<CheckOutResponse>, response: Response<CheckOutResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    removeCheckInId()
                    Toast.makeText(this@QRCodeScan, "Check-Out successful!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@QRCodeScan, Trainee_Home::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@QRCodeScan, "Check-Out Failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CheckOutResponse>, t: Throwable) {
                Toast.makeText(this@QRCodeScan, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("QRCodeScan", "Check-Out failed: ${t.message}")
            }
        })
    }

    private fun saveCheckInId(id: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt("CHECKIN_ID", id)
        editor.apply()
    }

    private fun getCheckInId(): Int {
        return sharedPreferences.getInt("CHECKIN_ID", -1)
    }

    private fun removeCheckInId() {
        val editor = sharedPreferences.edit()
        editor.remove("CHECKIN_ID")
        editor.apply()
    }

    override fun onResume() {
        super.onResume()
        barcodeView.resume()
    }

    override fun onPause() {
        super.onPause()
        barcodeView.pause()
    }
}
