package com.upang.fitness_club_management_system

import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
import com.upang.fitness_club_management_system.api.Api
import com.upang.fitness_club_management_system.api.RetrofitClient
import com.upang.fitness_club_management_system.helper.PreferenceManager
import com.upang.fitness_club_management_system.model.FetchTraineeProfileResponse
import com.upang.fitness_club_management_system.model.UpdateProfileResponse
import com.upang.fitness_club_management_system.model.UpdateUserProfileResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class EditProfile : AppCompatActivity() {
    private lateinit var progressBar: ProgressBar
    private lateinit var etFullname: TextInputEditText
    private lateinit var tvSaveBtn: TextView
    private lateinit var ivBtn: ImageView
    private var selectedImageUri: Uri? = null
    private lateinit var progressDialog: ProgressDialog

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            ivBtn.setImageURI(selectedImageUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val toolbar: Toolbar = findViewById(R.id.toolbarEditProfile)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            startActivity(Intent(this, Settings::class.java))
            finish()
        }
        progressBar = findViewById(R.id.progressBar)
        tvSaveBtn = findViewById(R.id.tvSaveBtn)
        ivBtn = findViewById(R.id.imageViewAvatar2)
        etFullname = findViewById(R.id.etFullName)

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Uploading... Please wait")
        progressDialog.setCancelable(false)

        ivBtn.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
        tvSaveBtn.setOnClickListener {
            updateProfile()
            if (selectedImageUri != null){
                updatePicture()
            }
        }
        fetchUserProfile()
    }
    private fun fetchUserProfile() {
        val preferenceManager = PreferenceManager(this)
        val api = RetrofitClient.instance.create(Api::class.java)
        val email = preferenceManager.getEmail()

        if (email == null) {
            Log.e("TrainerAccount", "Email is null")
            Toast.makeText(this, "Failed to fetch profile: Email is null", Toast.LENGTH_SHORT).show()
            return
        }

        api.fetchUserProfile(email).enqueue(object : Callback<FetchTraineeProfileResponse> {
            override fun onResponse(
                call: Call<FetchTraineeProfileResponse>,
                response: Response<FetchTraineeProfileResponse>
            ) {
                if (response.isSuccessful) {
                    val profile = response.body()
                    if (profile != null) {
                        val profileImageView = findViewById<ImageView>(R.id.imageViewAvatar2)
                        val currentName = profile.fullname
                        preferenceManager.saveFullName(currentName)
                        Glide.with(this@EditProfile)
                            .load(RetrofitClient.getBaseImageUrl() + "storage/profiles/" + profile.profile_picture)
                            .into(profileImageView)
                    } else {
                        Log.e("TrainerAccount", "Profile is null")
                        Toast.makeText(this@EditProfile, "Failed to fetch profile: Profile is null", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("TrainerAccount", "Error: ${response.code()} - ${response.message()}")
                    Toast.makeText(this@EditProfile, "Failed to fetch profile: ${response.message()}", Toast.LENGTH_SHORT).show()
                }

            }

            override fun onFailure(call: Call<FetchTraineeProfileResponse>, t: Throwable) {
                Log.e("TrainerAccount", "Network request failed: ${t.message}", t)
                Toast.makeText(this@EditProfile, "Failed to fetch profile: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updatePicture() {
        val preferenceManager = PreferenceManager(this)
        val api = RetrofitClient.instance.create(Api::class.java)
        val email = preferenceManager.getEmail()

        if (email.isNullOrEmpty()) {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
            return
        }

        val file = createFileFromUri(selectedImageUri!!)
        if (!file.exists() || file.length() == 0L) {
            Toast.makeText(this, "File does not exist or is empty", Toast.LENGTH_SHORT).show()
            return
        }

        val emailRequestBody = email.toRequestBody("text/plain".toMediaTypeOrNull())
        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("profile_picture", file.name, requestFile)
        Log.d("EditTrainerProfile", "Uploading file: ${file.name}, size: ${file.length()} bytes")

        progressDialog.show()
        api.updateProfilePic(emailRequestBody, body).enqueue(object : Callback<UpdateProfileResponse> {
            override fun onResponse(call: Call<UpdateProfileResponse>, response: Response<UpdateProfileResponse>) {
                progressDialog.dismiss()
                if (response.isSuccessful) {
                    Log.d("EditTrainerProfile", "Upload success: ${response.body()?.profile_picture}")
                    Toast.makeText(this@EditProfile, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@EditProfile, Account::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Log.e("EditTrainerProfile", "Upload failed: ${response.code()} - ${response.message()}")
                    Toast.makeText(this@EditProfile, "Failed to update profile picture", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UpdateProfileResponse>, t: Throwable) {
                progressDialog.dismiss()
                Log.e("EditTrainerProfile", "Error: ${t.message}", t)
                Toast.makeText(this@EditProfile, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun createFileFromUri(uri: Uri): File {
        val contentResolver: ContentResolver = contentResolver
        val fileName = getFileName(uri)
        val file = File(cacheDir, fileName)

        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            Log.d("EditTrainerProfile", "File successfully created: ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e("EditTrainerProfile", "Error creating file: ${e.message}")
        }

        return file
    }

    private fun getFileName(uri: Uri): String {
        var name = "temp_file"
        contentResolver.query(uri, null, null, null, null)?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    name = it.getString(index)
                }
            }
        }
        return name
    }

    private fun updateProfile() {
        val preferenceManager = PreferenceManager(this)
        val api = RetrofitClient.instance.create(Api::class.java)
        val email = preferenceManager.getEmail() ?: return
        var fullname = etFullname.text.toString()

        if (email.isNullOrEmpty()) {
            return
        }
        if (fullname.isEmpty()) {
            fullname = preferenceManager.getFullName() ?: ""
        }

        api.updateProfile(email,fullname).enqueue(object : retrofit2.Callback<UpdateUserProfileResponse>{
            override fun onResponse(
                call: Call<UpdateUserProfileResponse>,
                response: Response<UpdateUserProfileResponse>
            ) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    Toast.makeText(this@EditProfile, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@EditProfile, Account::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@EditProfile, "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UpdateUserProfileResponse>, t: Throwable) {
                Toast.makeText(this@EditProfile, "Error: ${t.message}", Toast.LENGTH_SHORT).show()

            }
        })
    }
}