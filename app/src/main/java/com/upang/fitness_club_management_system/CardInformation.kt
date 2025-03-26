package com.upang.fitness_club_management_system

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.stripe.android.ApiResultCallback
import com.stripe.android.PaymentConfiguration
import com.stripe.android.Stripe
import com.stripe.android.model.ConfirmPaymentIntentParams
import com.stripe.android.model.PaymentMethod
import com.stripe.android.view.CardInputWidget
import com.upang.fitness_club_management_system.api.Api
import com.upang.fitness_club_management_system.api.RetrofitClient
import com.upang.fitness_club_management_system.model.PaymentIntentResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CardInformation : AppCompatActivity() {
    private lateinit var stripe: Stripe
    private lateinit var cardInputWidget: CardInputWidget
    private lateinit var payButton: Button
    private var clientSecret: String? = null  // Store client secret

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_membership_fee)

        // Initialize Stripe
        PaymentConfiguration.init(
            applicationContext,
            "pk_test_51QuQqmFJOmXFu2MhKBNEmzockLi7VsMXC14Rpt3hWOvmgDvdNDjdX0bpagWbRWck2Zkq2CeLO8TXWGP3AriLSh8A00SqPRHZR6"
        )
        stripe = Stripe(this, PaymentConfiguration.getInstance(this).publishableKey)

        // Initialize UI components
        cardInputWidget = findViewById(R.id.cardInputWidget)
        payButton = findViewById(R.id.payButton)

        // Get payment intent from server
        createPaymentIntent(5000) // Amount in cents ($50.00)

        // Handle pay button click
        payButton.setOnClickListener {
            processPayment()
        }
    }

    private fun createPaymentIntent(amount: Int) {
        val api = RetrofitClient.instance.create(Api::class.java)
        api.createPaymentIntent(amount).enqueue(object : Callback<PaymentIntentResponse> {
            override fun onResponse(call: Call<PaymentIntentResponse>, response: Response<PaymentIntentResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        if (it.error == null) {
                            clientSecret = it.clientSecret // Store client secret
                        } else {
                            Toast.makeText(this@CardInformation, "Error: ${it.error}", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toast.makeText(this@CardInformation, "Failed to get client secret", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<PaymentIntentResponse>, t: Throwable) {
                Toast.makeText(this@CardInformation, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun processPayment() {
        val params = cardInputWidget.paymentMethodCreateParams
        if (params != null && clientSecret != null) {
            stripe.createPaymentMethod(params, callback = object :
                ApiResultCallback<PaymentMethod> {
                override fun onSuccess(paymentMethod: PaymentMethod) {
                    confirmPayment(paymentMethod.id!!)
                }

                override fun onError(e: Exception) {
                    Toast.makeText(this@CardInformation, "Payment method error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            })
        } else {
            Toast.makeText(this, "Invalid card details", Toast.LENGTH_LONG).show()
        }
    }

    private fun confirmPayment(paymentMethodId: String) {
        val params = ConfirmPaymentIntentParams.createWithPaymentMethodId(
            paymentMethodId, clientSecret!!
        )
        stripe.confirmPayment(this, params)
    }
}
