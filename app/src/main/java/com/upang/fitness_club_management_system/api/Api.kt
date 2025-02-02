package com.upang.fitness_club_management_system.api

import com.upang.fitness_club_management_system.model.LoginRequest
import com.upang.fitness_club_management_system.model.LoginResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface Api {
    @Headers("Content-Type: application/json")
    @POST("login.php")  // Adjust based on your actual API URL
    fun loginUser(@Body request: LoginRequest): Call<LoginResponse>
}