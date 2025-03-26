package com.upang.fitness_club_management_system

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.stripe.android.PaymentConfiguration
import com.stripe.android.Stripe
import com.stripe.android.model.ConfirmPaymentIntentParams
import com.upang.fitness_club_management_system.api.Api
import com.upang.fitness_club_management_system.api.RetrofitClient
import com.upang.fitness_club_management_system.model.PaymentIntentResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MembershipFee : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_membership_fee)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Stripe with your publishable key
        PaymentConfiguration.init(
            applicationContext,
            "pk_test_51QuQqmFJOmXFu2MhKBNEmzockLi7VsMXC14Rpt3hWOvmgDvdNDjdX0bpagWbRWck2Zkq2CeLO8TXWGP3AriLSh8A00SqPRHZR6"
        )

        // Call payment intent function with actual amount (5000 cents = $50.00)
        createPaymentIntent(5000)
    }

    private fun createPaymentIntent(amount: Int) {
        val api = RetrofitClient.instance.create(Api::class.java)

        api.createPaymentIntent(amount).enqueue(object : Callback<PaymentIntentResponse> {
            override fun onResponse(call: Call<PaymentIntentResponse>, response: Response<PaymentIntentResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        if (it.error == null) {
                            startPaymentFlow(it.clientSecret)
                        } else {
                            Toast.makeText(this@MembershipFee, "Error: ${it.error}", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toast.makeText(this@MembershipFee, "Failed to get client secret", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<PaymentIntentResponse>, t: Throwable) {
                Toast.makeText(this@MembershipFee, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun startPaymentFlow(clientSecret: String) {
        val stripe = Stripe(this, PaymentConfiguration.getInstance(this).publishableKey)
        val paymentIntentParams = ConfirmPaymentIntentParams.create(clientSecret)

        stripe.confirmPayment(this, paymentIntentParams)
    }
}
