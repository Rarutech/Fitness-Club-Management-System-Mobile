package com.upang.fitness_club_management_system.api

import com.upang.fitness_club_management_system.ForgotPassword
import com.upang.fitness_club_management_system.model.AuthRequest
import com.upang.fitness_club_management_system.model.AuthResponse
import com.upang.fitness_club_management_system.model.BookTrainerRequest
import com.upang.fitness_club_management_system.model.BookTrainerResponse
import com.upang.fitness_club_management_system.model.CancelScheduleResponse
import com.upang.fitness_club_management_system.model.CheckInResponse
import com.upang.fitness_club_management_system.model.CheckOutResponse
import com.upang.fitness_club_management_system.model.ConfirmEmailRequest
import com.upang.fitness_club_management_system.model.ConfirmEmailResponse
import com.upang.fitness_club_management_system.model.EditBookTrainerRequest
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
import com.upang.fitness_club_management_system.model.postHighlightResponse
import com.upang.fitness_club_management_system.model.FetchOrdersResponse
import com.upang.fitness_club_management_system.model.FetchTraineeProfileResponse
import com.upang.fitness_club_management_system.model.FetchTrainersResponse
import com.upang.fitness_club_management_system.model.ForgotPasswordResponse
import com.upang.fitness_club_management_system.model.PaymentIntentResponse
import com.upang.fitness_club_management_system.model.RateTrainerRequest
import com.upang.fitness_club_management_system.model.RateTrainerResponse
import com.upang.fitness_club_management_system.model.TraineeEvent
import com.upang.fitness_club_management_system.model.TraineePendingResponse
import com.upang.fitness_club_management_system.model.TraineeProgressDateResponse
import com.upang.fitness_club_management_system.model.TraineeProgressResponse
import com.upang.fitness_club_management_system.model.TraineeRequestApiResponse
import com.upang.fitness_club_management_system.model.TrainerFetchApiResponse
import com.upang.fitness_club_management_system.model.TrainerRequestApiResponse
import com.upang.fitness_club_management_system.model.TrainerReviewResponse
import com.upang.fitness_club_management_system.model.UpdateProfileResponse
import com.upang.fitness_club_management_system.model.UpdateTrainerProfileRequest
import com.upang.fitness_club_management_system.model.UpdateTrainerProfileResponse
import com.upang.fitness_club_management_system.model.UpdateUserProfileResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
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
    fun getEvents(@Query("date") date: String,
                  @Query("email") email: String): Call<List<Event>>

    @Headers("Content-Type: application/json")
    @GET("Api/fetchTrainerAssignment.php")
    fun getAllEvents(@Query("email") email: String): Call<List<Event>>

    @Headers("Content-Type: application/json")
    @GET("Api/fetchUserAssignment.php")
    fun getAllTraineeEvents(@Query("email") email: String): Call<List<TraineeEvent>>


    @Headers("Content-Type: application/json")
    @GET("Api/fetchInventory.php")
    fun fetchInventory(): Call<FetchInventoryResponse>

    @GET("Api/fetchInventory.php")
    fun fetchProduct(@Query("id") productId: Int): Call<FetchInventoryResponse>

    @Headers("Content-Type: application/json")
    @POST("Api/sendOrder.php")
    fun sendOrder(@Body orderRequest: OrderRequest): Call<OrderResponse>

    //fetch Trainer Request via Email
    @GET("Api/fetchTrainerRequest.php")
    fun fetchTrainerRequest(@Query("email") email: String): Call<TrainerRequestApiResponse>

    //fetch Trainer Profile
    @GET("Api/fetchTrainerProfile.php")
    fun fetchTrainerProfile(@Query("email") email: String): Call<FetchTrainerProfileResponse>

    //Fetch Orders
    @Headers("Content-Type: application/json")
    @GET("Api/fetchOrders.php")
    fun fetchOrders(@Query("email") email: String): Call<FetchOrdersResponse>


    //Update Trainer Profile
    @Headers("Content-Type: application/json")
    @POST("Api/updateTrainerProfile.php")
    fun updateTrainerProfile(@Body updateTrainerProfileRequest: UpdateTrainerProfileRequest): Call<UpdateTrainerProfileResponse>


    //Update Profile Picture
    @Multipart
    @POST("Api/updateProfilePic.php")
    fun updateProfilePic(
        @Part("email") email: RequestBody,
        @Part profile_picture: MultipartBody.Part
    ): Call<UpdateProfileResponse>

    //Fetch All Profile
    @Headers("Content-Type: application/json")
    @GET("Api/fetchTrainerProfile.php")
    fun fetchAllTrainers(): Call<FetchTrainersResponse>

    //Request Trainer
    @Headers("Content-Type: application/json")
    @POST("Api/trainerRequest.php")
    fun requestTrainer(@Body request: BookTrainerRequest): Call<BookTrainerResponse>

    //Fetch Trainer Request by ID
    @GET("Api/fetchTrainerRequest.php")
    fun fetchTrainerRequestId(@Query("request_id") request_id: String): Call<TrainerFetchApiResponse>

    @GET("Api/fetchTraineeRequest.php")
    fun fetchTraineeRequest(@Query("email") email: String): Call<TraineeRequestApiResponse>

    //Accept or Reject Trainer Request
    @Headers("Content-Type: application/json")
    @POST("Api/acceptTraineeRequest.php")
    fun acceptTrainer(@Body request: RequestBody): Call<TraineePendingResponse>

    @Headers("Content-Type: application/json")
    @POST("Api/cancelTraineeRequest.php")
    fun rejectTrainer(@Body request: RequestBody): Call<TraineePendingResponse>

    //Cancel Schedule
    @FormUrlEncoded
    @POST("Api/cancelSchedule.php")
    fun cancelSchedule(@Field("request_id") requestId: Int): Call<CancelScheduleResponse>

    //Reschedule
    @Headers("Content-Type: application/json")
    @POST("Api/editTrainerRequest.php")
    fun editRequestTrainer(@Body request: EditBookTrainerRequest): Call<BookTrainerResponse>


    //Review Trainer
    @Headers("Content-Type: application/json")
    @POST("Api/rateTrainer.php")
    fun rateTrainer(@Body request: RateTrainerRequest): Call<RateTrainerResponse>

    @GET("Api/fetchAllReview.php")
    fun fetchTrainerReviews(@Query("trainer_email") email: String): Call<TrainerReviewResponse>

    @POST("Api/create_payment_intent.php")
    fun createPaymentIntent(@Body request: HashMap<String, Int>): Call<PaymentIntentResponse>

    @FormUrlEncoded
    @POST("Api/checkin.php")
    fun checkIn(@Field("scanned_string") scanned_string: String,
                @Field("user_email") user_email: String): Call<CheckInResponse>

    @FormUrlEncoded
    @POST("Api/checkout.php")
    fun checkOut(@Field("id") id: Int,
                 @Field("scanned_string") scanned_string: String): Call<CheckOutResponse>

    @GET("Api/TraineeProgress.php")
    fun TraineeProgress(@Query("email") email: String) : Call<TraineeProgressResponse>

    @GET("Api/TraineeProgressDate.php")
    fun TraineeProgressDate(@Query("email") email: String,
                            @Query("date") date: String) : Call<TraineeProgressDateResponse>

    @FormUrlEncoded
    @POST("Api/forgotPassword.php")
    fun forgotPassword(@Field("email") email: String,
                       @Field("new_password") new_password: String) : Call<ForgotPasswordResponse>

    @FormUrlEncoded
    @POST("Api/fetchUserProfile.php")
    fun fetchUserProfile(@Field("email") email: String) : Call<FetchTraineeProfileResponse>

    @FormUrlEncoded
    @POST("Api/updateUserProfile.php")
    fun updateProfile(@Field("email") email:String,
                      @Field("fullname") fullname:String) : Call<UpdateUserProfileResponse>


}
