package com.upang.fitness_club_management_system.model

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Part

data class PostHighlightRequest(
    @SerializedName("user_email") val user_email: RequestBody,
    @SerializedName("caption") val caption: RequestBody,
    @SerializedName("image_urls[]") val image_urls: List<MultipartBody.Part>
)


