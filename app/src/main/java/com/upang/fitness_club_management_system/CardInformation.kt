package com.upang.fitness_club_management_system

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.stripe.android.ApiResultCallback
import com.stripe.android.PaymentConfiguration
import com.stripe.android.PaymentIntentResult
import com.stripe.android.Stripe
import com.stripe.android.model.ConfirmPaymentIntentParams
import com.stripe.android.model.PaymentMethod
import com.stripe.android.model.StripeIntent
import com.upang.fitness_club_management_system.model.PaymentIntentResponse
import com.stripe.android.view.CardInputWidget
import com.upang.fitness_club_management_system.api.Api
import com.upang.fitness_club_management_system.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CardInformation : AppCompatActivity() {
    private lateinit var stripe: Stripe
    private lateinit var cardInputWidget: CardInputWidget
    private lateinit var payButton: Button
    private var clientSecret: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_card_information)

        PaymentConfiguration.init(
            applicationContext,
            "pk_test_51R7qAeBNSwOEu2mpYqg3LpokRdbt17nufCifDObthMiiOzuybNT8lnbWUJYdYHNr4gSs7QrafjN8ExeScD91FcLN002nD7PMvM"
        )
        stripe = Stripe(this, PaymentConfiguration.getInstance(this).publishableKey)

        cardInputWidget = findViewById(R.id.cardInputWidget)
        payButton = findViewById(R.id.payButton)

        createPaymentIntent(600)

        payButton.setOnClickListener {
            processPayment()
        }
    }

    private fun createPaymentIntent(amount: Int) {
        val api = RetrofitClient.instance.create(Api::class.java)
        val requestBody = hashMapOf("amount" to amount)

        Log.d("PaymentIntent", "Sending request to create payment intent with amount: $amount")

        api.createPaymentIntent(requestBody).enqueue(object : Callback<PaymentIntentResponse> {
            override fun onResponse(call: Call<PaymentIntentResponse>, response: Response<PaymentIntentResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        if (it.error == null) {
                            clientSecret = it.clientSecret
                            Log.d("PaymentIntent", "Client Secret received: $clientSecret")
                        } else {
                            Log.e("PaymentIntent", "Error from API: ${it.error}")
                            Toast.makeText(this@CardInformation, "Error: ${it.error}", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("PaymentIntent", "Failed to get client secret. Response code: ${response.code()}, Error: $errorBody")
                    Toast.makeText(this@CardInformation, "Failed to get client secret", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<PaymentIntentResponse>, t: Throwable) {
                Log.e("PaymentIntent", "API Call Failed: ${t.message}", t)
                Toast.makeText(this@CardInformation, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun processPayment() {
        val params = cardInputWidget.paymentMethodCreateParams

        if (params != null && clientSecret != null) {
            Log.d("PaymentProcess", "Creating payment method with provided card details")

            stripe.createPaymentMethod(params, callback = object :
                ApiResultCallback<PaymentMethod> {
                override fun onSuccess(paymentMethod: PaymentMethod) {
                    Log.d("PaymentProcess", "Payment method created successfully: ${paymentMethod.id}")
                    confirmPayment(paymentMethod.id!!)
                }

                override fun onError(e: Exception) {
                    Log.e("PaymentProcess", "Payment method error: ${e.message}", e)
                    Toast.makeText(this@CardInformation, "Payment method error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            })
        } else {
            Log.e("PaymentProcess", "Invalid card details or missing client secret")
            Toast.makeText(this, "Invalid card details", Toast.LENGTH_LONG).show()
        }
    }

    private fun confirmPayment(paymentMethodId: String) {
        Log.d("PaymentProcess", "Confirming payment with PaymentMethodId: $paymentMethodId and ClientSecret: $clientSecret")

        val params = ConfirmPaymentIntentParams.createWithPaymentMethodId(
            paymentMethodId, clientSecret!!
        )

        stripe.confirmPayment(this, params)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        stripe.onPaymentResult(requestCode, data, object : ApiResultCallback<PaymentIntentResult> {
            override fun onSuccess(result: PaymentIntentResult) {
                val paymentIntent = result.intent
                Log.d("PaymentProcess", "Payment successful: ${paymentIntent.status}")

                if (paymentIntent.status == StripeIntent.Status.Succeeded) {
                    Toast.makeText(this@CardInformation, "Payment Successful!", Toast.LENGTH_LONG).show()

                    val intent = Intent(this@CardInformation, Trainee_Home::class.java)
                    startActivity(intent)
                    finish()
                }
            }

            override fun onError(e: Exception) {
                Log.e("PaymentProcess", "Payment failed: ${e.message}", e)
                Toast.makeText(this@CardInformation, "Payment failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}
