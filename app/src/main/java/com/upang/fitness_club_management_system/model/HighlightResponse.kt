package com.upang.fitness_club_management_system.model

import com.google.gson.annotations.SerializedName

data class HighlightResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("highlights") val highlights: List<Highlight>?
)

data class Highlight(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("user_email") val userEmail: String,
    @SerializedName("caption") val caption: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("images") val images: List<String>
)

