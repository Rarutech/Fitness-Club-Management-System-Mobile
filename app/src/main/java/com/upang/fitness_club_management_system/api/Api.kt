package com.upang.fitness_club_management_system.api

import com.upang.fitness_club_management_system.model.AuthRequest
import com.upang.fitness_club_management_system.model.AuthResponse
import com.upang.fitness_club_management_system.model.LoginRequest
import com.upang.fitness_club_management_system.model.LoginResponse
import com.upang.fitness_club_management_system.model.SignUpRequest
import com.upang.fitness_club_management_system.model.SignUpResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface Api {
    @Headers("Content-Type: application/json")
    @POST("login.php")  // Adjust based on your actual API URL
    fun loginUser(@Body request: LoginRequest): Call<LoginResponse>

    @Headers("Content-Type: application/json")
    @POST("Authentication.php")
    fun Authenticate(@Body request: AuthRequest): Call<AuthResponse>

    @Headers("Content-Type: application/json")
    @POST("users.php")
    fun SignUp(@Body request: SignUpRequest): Call<SignUpResponse>
}