package com.upang.fitness_club_management_system.api

import com.upang.fitness_club_management_system.model.AuthRequest
import com.upang.fitness_club_management_system.model.AuthResponse
import com.upang.fitness_club_management_system.model.BookTrainerRequest
import com.upang.fitness_club_management_system.model.BookTrainerResponse
import com.upang.fitness_club_management_system.model.ConfirmEmailRequest
import com.upang.fitness_club_management_system.model.ConfirmEmailResponse
import com.upang.fitness_club_management_system.model.Event
import com.upang.fitness_club_management_system.model.FetchInventoryResponse
import com.upang.fitness_club_management_system.model.FetchTrainerProfileResponse
import com.upang.fitness_club_management_system.model.HighlightResponse
import com.upang.fitness_club_management_system.model.LoginRequest
import com.upang.fitness_club_management_system.model.LoginResponse
import com.upang.fitness_club_management_system.model.SendConfirmEmailRequest
import com.upang.fitness_club_management_system.model.SendConfirmEmailResponse
import com.upang.fitness_club_management_system.model.SignUpRequest
import com.upang.fitness_club_management_system.model.SignUpResponse
import com.upang.fitness_club_management_system.model.OrderRequest
import com.upang.fitness_club_management_system.model.OrderResponse
import com.upang.fitness_club_management_system.model.TrainerRequestResponse
import com.upang.fitness_club_management_system.model.postHighlightResponse
import com.upang.fitness_club_management_system.model.FetchOrdersResponse
import com.upang.fitness_club_management_system.model.FetchTrainersResponse
import com.upang.fitness_club_management_system.model.PaymentIntentResponse
import com.upang.fitness_club_management_system.model.UpdateProfileResponse
import com.upang.fitness_club_management_system.model.UpdateTrainerProfileRequest
import com.upang.fitness_club_management_system.model.UpdateTrainerProfileResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
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
    @GET("Api/fetchTrainerAssignment.php")
    fun getEvents(@Query("date") date: String): Call<List<Event>>

    @Headers("Content-Type: application/json")
    @GET("Api/fetchTrainerAssignment.php")
    fun getAllEvents(): Call<List<Event>>

    @Headers("Content-Type: application/json")
    @GET("Api/fetchInventory.php")
    fun fetchInventory(): Call<FetchInventoryResponse>

    @GET("Api/fetchInventory.php")
    fun fetchProduct(@Query("id") productId: Int): Call<FetchInventoryResponse>

    @Headers("Content-Type: application/json")
    @POST("Api/sendOrder.php")
    fun sendOrder(@Body orderRequest: OrderRequest): Call<OrderResponse>

    @GET("Api/fetchTrainerRequest.php")
    fun fetchTrainerRequest(@Query("email") email: String): Call<List<TrainerRequestResponse>>

    @GET("Api/fetchTrainerProfile.php")
    fun fetchTrainerProfile(@Query("email") email: String): Call<FetchTrainerProfileResponse>

    @Headers("Content-Type: application/json")
    @GET("Api/fetchOrders.php")
    fun fetchOrders(@Query("email") email: String): Call<FetchOrdersResponse>

    @Headers("Content-Type: application/json")
    @POST("Api/updateTrainerProfile.php")
    fun updateTrainerProfile(@Body updateTrainerProfileRequest: UpdateTrainerProfileRequest): Call<UpdateTrainerProfileResponse>

    @Multipart
    @POST("Api/updateProfilePic.php")
    fun updateProfilePic(
        @Part("email") email: RequestBody,
        @Part profile_picture: MultipartBody.Part
    ): Call<UpdateProfileResponse>
    @Headers("Content-Type: application/json")
    @GET("Api/fetchTrainerProfile.php")
    fun fetchAllTrainers(): Call<FetchTrainersResponse>

    @Headers("Content-Type: application/json")
    @POST("Api/trainerRequest.php")
    fun requestTrainer(@Body request: BookTrainerRequest): Call<BookTrainerResponse>

    @POST("Api/create_payment_intent.php")
    fun createPaymentIntent(
        @Body request: Int
    ): Call<PaymentIntentResponse>

}
