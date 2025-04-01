package com.upang.fitness_club_management_system.model

import com.google.gson.annotations.SerializedName

data class PaymentIntentResult(
    @SerializedName("clientSecret") val clientSecret: String?,  // The client secret for confirming the payment
    @SerializedName("error") val error: String? // Any error message from the server

)
