package com.upang.fitness_club_management_system.api

import com.upang.fitness_club_management_system.model.AuthRequest
import com.upang.fitness_club_management_system.model.AuthResponse
import com.upang.fitness_club_management_system.model.ConfirmEmailRequest
import com.upang.fitness_club_management_system.model.ConfirmEmailResponse
import com.upang.fitness_club_management_system.model.Event
import com.upang.fitness_club_management_system.model.HighlightResponse
import com.upang.fitness_club_management_system.model.LoginRequest
import com.upang.fitness_club_management_system.model.LoginResponse
import com.upang.fitness_club_management_system.model.SendConfirmEmailRequest
import com.upang.fitness_club_management_system.model.SendConfirmEmailResponse
import com.upang.fitness_club_management_system.model.SignUpRequest
import com.upang.fitness_club_management_system.model.SignUpResponse
import com.upang.fitness_club_management_system.model.postHighlightResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface Api {
    @Headers("Content-Type: application/json")
    @POST("Api/Login.php")  // Adjust based on your actual API URL
    fun loginUser(@Body request: LoginRequest): Call<LoginResponse>

    @Headers("Content-Type: application/json")
    @POST("Api/authentication.php")
    fun Authenticate(@Body request: AuthRequest): Call<AuthResponse>

    @Headers("Content-Type: application/json")
    @POST("Controller/users.php")
    fun SignUp(@Body request: SignUpRequest): Call<SignUpResponse>

    @GET("Api/getHighlights.php")
    fun GetHighlights(): Call<HighlightResponse>

    @Multipart
    @POST("Api/postHighlights.php")

    fun uploadPost(
        @Part("user_email") userEmail: RequestBody,
        @Part("caption") caption: RequestBody,
        @Part image_urls: List<MultipartBody.Part>
    ): Call<postHighlightResponse>

    @Headers("Content-Type: application/json")
    @POST("Api/emailVerificationCode.php")
    fun GetEmailCode(@Body request: SendConfirmEmailRequest): Call<SendConfirmEmailResponse>

    @Headers("Content-Type: application/json")
    @POST("Controller/emailConfirmation.php")
    fun ConfirmEmail(@Body request: ConfirmEmailRequest): Call<ConfirmEmailResponse>

    @Headers("Content-Type: application/json")
    @GET("API/fetchTrainerAssignment.php")
    fun getEvents(@Query("date") date: String): Call<List<Event>>

}