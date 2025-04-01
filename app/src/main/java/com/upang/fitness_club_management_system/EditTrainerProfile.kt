package com.upang.fitness_club_management_system

import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
import com.upang.fitness_club_management_system.api.Api
import com.upang.fitness_club_management_system.api.RetrofitClient
import com.upang.fitness_club_management_system.helper.PreferenceManager
import com.upang.fitness_club_management_system.model.FetchTrainerProfileResponse
import com.upang.fitness_club_management_system.model.UpdateProfileResponse
import com.upang.fitness_club_management_system.model.UpdateTrainerProfileRequest
import com.upang.fitness_club_management_system.model.UpdateTrainerProfileResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class EditTrainerProfile : AppCompatActivity() {
    private lateinit var progressBar: ProgressBar
    private lateinit var etFullname: TextInputEditText
    private lateinit var etAboutMe: TextInputEditText
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
        setContentView(R.layout.activity_edit_trainer_profile)

        val toolbar: Toolbar = findViewById(R.id.toolbarEditProfile)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        progressBar = findViewById(R.id.progressBar)
        tvSaveBtn = findViewById(R.id.tvSaveBtn)
        ivBtn = findViewById(R.id.ivBtn)
        etFullname = findViewById(R.id.etFullName)

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Uploading... Please wait")
        progressDialog.setCancelable(false)


        fetchUserProfile()
        showLoader()

        ivBtn.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
        tvSaveBtn.setOnClickListener {
            updateProfile()
            if (selectedImageUri != null){
                updatePicture()
            }
        }
        val toolbartrainer = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarEditTrainerProfile)
        setSupportActionBar(toolbartrainer)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            startActivity(Intent(this, Settings::class.java))
            finish() // Close current activity
        }

    }

    private fun updateProfile() {
        val preferenceManager = PreferenceManager(this)
        val api = RetrofitClient.instance.create(Api::class.java)
        val email = preferenceManager.getEmail()
        var fullname = etFullname.text.toString()
        var aboutMe = etAboutMe.text.toString()

        if (email.isNullOrEmpty()) {
            return
        }
        if (fullname.isEmpty()) {
            fullname = preferenceManager.getFullName() ?: ""
        }

        if (aboutMe.isEmpty()) {
            aboutMe = preferenceManager.getAboutMe() ?: ""
        }

        Log.d("EditTrainerProfile", "Updating profile with email: $email, fullname: $fullname, aboutMe: $aboutMe")

        val updateRequest = UpdateTrainerProfileRequest(email, fullname, aboutMe)
        api.updateTrainerProfile(updateRequest).enqueue(object : Callback<UpdateTrainerProfileResponse> {
            override fun onResponse(
                call: Call<UpdateTrainerProfileResponse>,
                response: Response<UpdateTrainerProfileResponse>
            ) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    Toast.makeText(this@EditTrainerProfile, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@EditTrainerProfile, TrainerAccount::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@EditTrainerProfile, "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UpdateTrainerProfileResponse>, t: Throwable) {
                Toast.makeText(this@EditTrainerProfile, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this@EditTrainerProfile, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@EditTrainerProfile, TrainerAccount::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Log.e("EditTrainerProfile", "Upload failed: ${response.code()} - ${response.message()}")
                    Toast.makeText(this@EditTrainerProfile, "Failed to update profile picture", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UpdateProfileResponse>, t: Throwable) {
                progressDialog.dismiss()
                Log.e("EditTrainerProfile", "Error: ${t.message}", t)
                Toast.makeText(this@EditTrainerProfile, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
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

    private fun showLoader() {
        progressBar.visibility = View.VISIBLE
    }

    private fun hideLoader() {
        progressBar.visibility = View.GONE
    }

    private fun fetchUserProfile() {
        val preferenceManager = PreferenceManager(this)
        val api = RetrofitClient.instance.create(Api::class.java)
        val email = preferenceManager.getEmail() ?: return

        api.fetchTrainerProfile(email).enqueue(object : Callback<FetchTrainerProfileResponse> {
            override fun onResponse(
                call: Call<FetchTrainerProfileResponse>,
                response: Response<FetchTrainerProfileResponse>
            ) {
                if (response.isSuccessful) {
                    val profile = response.body()
                    if (profile != null) {
                        val preferenceManager = PreferenceManager(this@EditTrainerProfile)
                        val currentName = profile.fullname
                        val currentAboutMe = profile.about
                        preferenceManager.saveFullName(currentName)
                        preferenceManager.saveAboutMe(currentAboutMe)
                        val profileImageView = findViewById<ImageView>(R.id.ivBtn)
                        Glide.with(this@EditTrainerProfile)
                            .load(RetrofitClient.getBaseImageUrl() + "storage/profiles/" + profile.profile_picture)
                            .into(profileImageView)
                    } else {
                        Log.e("TrainerAccount", "Profile is null")
                        Toast.makeText(this@EditTrainerProfile, "Failed to fetch profile: Profile is null", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("TrainerAccount", "Error: ${response.code()} - ${response.message()}")
                    Toast.makeText(this@EditTrainerProfile, "Failed to fetch profile: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
                hideLoader()
            }

            override fun onFailure(call: Call<FetchTrainerProfileResponse>, t: Throwable) {
                Log.e("TrainerAccount", "Network request failed: ${t.message}", t)
                Toast.makeText(this@EditTrainerProfile, "Failed to fetch profile: ${t.message}", Toast.LENGTH_SHORT).show()
                hideLoader()
            }
        })
    }
}
