package com.upang.fitness_club_management_system.model

import okhttp3.MultipartBody

data class UpdateProfileRequest(
    val email: String,
    val profile_picture: MultipartBody.Part
)