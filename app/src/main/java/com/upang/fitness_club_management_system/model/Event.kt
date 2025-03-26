package com.upang.fitness_club_management_system.model

import com.google.gson.annotations.SerializedName

data class Event(
    @SerializedName("user_name") val user_name: String,
    @SerializedName("assignment_date") val assignment_date: String,
    @SerializedName("start_time") val start_time: String,
    @SerializedName("end_time") val end_time: String,
    @SerializedName("status") val status: String? = null,

    val assignment_id: Int? = null,
    val request_id: Int? = null,
    val user_email: String? = null,
    val trainer_name: String? = null,
    val trainer_email: String? = null

)
