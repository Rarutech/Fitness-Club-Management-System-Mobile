package com.upang.fitness_club_management_system

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.BarcodeView

class QRCodeScan : AppCompatActivity() {
    private lateinit var barcodeView: BarcodeView
    private lateinit var txtResult: TextView
    private var scannedData: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_qrcode_scan)
        barcodeView = findViewById(R.id.barcode_scanner)
        txtResult = findViewById(R.id.txtResult)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
        } else {
            startScanner()
        }
    }
    private fun startScanner() {
        barcodeView.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {
                if (result != null && scannedData != result.text) {
                    scannedData = result.text
                    txtResult.text = "Scanned: $scannedData"
                    Log.d("QRCodeScan","Scanned: ${scannedData}")
                }
            }
        })
        barcodeView.resume()
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