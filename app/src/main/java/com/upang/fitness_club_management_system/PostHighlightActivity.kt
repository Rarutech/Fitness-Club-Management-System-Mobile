package com.upang.fitness_club_management_system

import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.upang.fitness_club_management_system.adapter.ImageAdapter
import com.upang.fitness_club_management_system.adapter.SelectedImageAdapter
import com.upang.fitness_club_management_system.api.Api
import com.upang.fitness_club_management_system.api.RetrofitClient
import com.upang.fitness_club_management_system.helper.PreferenceManager
import com.upang.fitness_club_management_system.model.Utils
import com.upang.fitness_club_management_system.model.postHighlightResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class PostHighlightActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var selectedImageAdapter: SelectedImageAdapter
    private val selectedImages = ArrayList<Uri>()
    private lateinit var progressDialog: ProgressDialog
    private val handler = Handler()
    private val delay: Long = 5000
    private val runnable = object : Runnable {
        override fun run() {
            Utils.getNotifications(this@PostHighlightActivity)
            handler.postDelayed(this, delay)
        }
    }
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            if (uris.isNotEmpty()) {
                selectedImages.addAll(uris)
                selectedImageAdapter.notifyDataSetChanged()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_highlight)
        Utils.getNotifications(this)


        val btnGetImage = findViewById<ImageButton>(R.id.btnGetImage)
        val btnPost = findViewById<TextView>(R.id.btnPost)
        val etCaption = findViewById<EditText>(R.id.etCaption)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        selectedImageAdapter = SelectedImageAdapter(selectedImages)
        recyclerView.adapter = selectedImageAdapter

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Uploading... Please wait")
        progressDialog.setCancelable(false)

        val toolbar: Toolbar = findViewById(R.id.toolbarPostHighlight)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        toolbar.setNavigationOnClickListener {
            val intent = Intent(this@PostHighlightActivity, TrainerHomeActivity::class.java)
            startActivity(intent)
        }

        btnGetImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        btnPost.setOnClickListener {
            val preferenceManager = PreferenceManager(this)
            val userEmail = preferenceManager.getEmail()

            if (userEmail.isNullOrEmpty()) {
                Toast.makeText(this, "Error: No email found. Please log in again.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            uploadPost(userEmail, etCaption.text.toString(), selectedImages)
        }
    }

    private fun prepareFileParts(uriList: List<Uri>): List<MultipartBody.Part> {
        val parts = mutableListOf<MultipartBody.Part>()
        for (uri in uriList) {
            val filePath = getRealPathFromURI(uri)
            if (filePath.isNotEmpty()) {
                val file = File(filePath)
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())

                val part = MultipartBody.Part.createFormData("image_urls[]", file.name, requestFile)
                parts.add(part)
            }
        }
        return parts
    }

    private fun getRealPathFromURI(uri: Uri): String {
        val contentResolver: ContentResolver = contentResolver
        val fileName: String = getFileName(uri)
        val file = File(cacheDir, fileName)

        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return file.absolutePath
    }

    private fun getFileName(uri: Uri): String {
        var name = "temp_file"
        val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    name = it.getString(index)
                }
            }
        }
        return name
    }

    private fun uploadPost(userEmail: String, caption: String, imageUris: List<Uri>) {
        if (imageUris.isEmpty()) {
            Toast.makeText(this, "Please select at least one image", Toast.LENGTH_SHORT).show()
            return
        }

        val btnPost = findViewById<TextView>(R.id.btnPost)
        // Show progress dialog and disable button to prevent spamming
        progressDialog.show()
        btnPost.isEnabled = false

        val userEmailBody = userEmail.toRequestBody("text/plain".toMediaTypeOrNull())
        val captionBody = caption.toRequestBody("text/plain".toMediaTypeOrNull())
        val imageParts = prepareFileParts(imageUris)

        val api = RetrofitClient.instance.create(Api::class.java)
        val call = api.uploadPost(userEmailBody, captionBody, imageParts)

        call.enqueue(object : Callback<postHighlightResponse> {
            override fun onResponse(call: Call<postHighlightResponse>, response: Response<postHighlightResponse>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        if (responseBody.success) {
                            Toast.makeText(this@PostHighlightActivity, responseBody.message, Toast.LENGTH_SHORT).show()
                            progressDialog.dismiss()
                            btnPost.isEnabled = true
                            val intent = Intent(this@PostHighlightActivity, TrainerHomeActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@PostHighlightActivity, "Failed: ${responseBody.message}", Toast.LENGTH_SHORT).show()
                            progressDialog.dismiss()
                            btnPost.isEnabled = true
                        }
                    } else {
                        progressDialog.dismiss()
                        btnPost.isEnabled = true
                        Toast.makeText(this@PostHighlightActivity, "Response body is null", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    progressDialog.dismiss()
                    btnPost.isEnabled = true
                    Toast.makeText(this@PostHighlightActivity, "Server error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<postHighlightResponse>, t: Throwable) {
                progressDialog.dismiss()
                btnPost.isEnabled = true
                Toast.makeText(this@PostHighlightActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    override fun onStart() {
        super.onStart()
        // Start checking membership status when the activity is visible
        handler.post(runnable)
    }

    override fun onStop() {
        super.onStop()
        // Stop checking membership status when the activity is not visible
        handler.removeCallbacks(runnable)
    }
}
