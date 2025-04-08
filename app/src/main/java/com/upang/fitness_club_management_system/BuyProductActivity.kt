package com.upang.fitness_club_management_system

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isNotEmpty
import com.bumptech.glide.Glide
import com.stripe.android.ApiResultCallback
import com.stripe.android.PaymentConfiguration
import com.stripe.android.PaymentIntentResult
import com.stripe.android.Stripe
import com.stripe.android.model.ConfirmPaymentIntentParams
import com.stripe.android.model.PaymentMethod
import com.stripe.android.model.StripeIntent
import com.stripe.android.view.CardInputWidget
import com.upang.fitness_club_management_system.api.Api
import com.upang.fitness_club_management_system.api.RetrofitClient
import com.upang.fitness_club_management_system.helper.PreferenceManager
import com.upang.fitness_club_management_system.model.FetchInventoryResponse
import com.upang.fitness_club_management_system.model.OrderRequest
import com.upang.fitness_club_management_system.model.OrderResponse
import com.upang.fitness_club_management_system.model.PaymentIntentResponse
import com.upang.fitness_club_management_system.model.Product
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BuyProductActivity : AppCompatActivity() {
    private lateinit var stripe: Stripe
    private lateinit var productImage: ImageView
    private lateinit var productName: TextView
    private lateinit var productPrice: TextView
    private lateinit var productStock: TextView
    private lateinit var btnPurchase: TextView
    private lateinit var quantity: EditText
    private lateinit var payOnline: RadioButton
    private lateinit var etCard: CardInputWidget
    private var selectedProduct: Product? = null
    private var clientSecret: String? = null
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_buy_product)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        PaymentConfiguration.init(
            applicationContext,
            "pk_test_51R7qAeBNSwOEu2mpYqg3LpokRdbt17nufCifDObthMiiOzuybNT8lnbWUJYdYHNr4gSs7QrafjN8ExeScD91FcLN002nD7PMvM"
        )

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Buying product...")
        progressDialog.setCancelable(false)

        productImage = findViewById(R.id.productImage)
        productName = findViewById(R.id.productName)
        productPrice = findViewById(R.id.productPrice)
        productStock = findViewById(R.id.productStock)
        btnPurchase = findViewById(R.id.btnPurchase)
        payOnline = findViewById(R.id.payOnline)
        quantity = findViewById(R.id.etQuantity)
        etCard = findViewById(R.id.etCard)

        val sharedPreferences = getSharedPreferences("shop_prefs", Context.MODE_PRIVATE)
        val productId = sharedPreferences.getInt("selected_product_id", -1)

        Log.d("PRODUCT_DETAILS", "Retrieved Product ID: $productId")

        if (productId != -1) {
            fetchProductDetails(productId)
        } else {
            Log.e("PRODUCT_DETAILS", "Invalid product ID")
            Toast.makeText(this, "Invalid product ID", Toast.LENGTH_SHORT).show()
            finish()
        }

        payOnline.setOnClickListener {
            if (payOnline.isChecked) {
                etCard.visibility = View.VISIBLE
            }else {
                etCard.visibility = View.GONE
            }
        }


        val toolbar: Toolbar = findViewById(R.id.toolbarBuyProducts)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            val preferenceManager = PreferenceManager(this)
            val role = preferenceManager.getRole()
            if (role != null) {
                if (role == "trainer") {
                    val intent = Intent(this@BuyProductActivity, TrainerShop::class.java)
                    startActivity(intent)
                } else {
                    val intent = Intent(this@BuyProductActivity, Shop::class.java)
                    startActivity(intent)
                }
            } else{
                val intent = Intent(this@BuyProductActivity, Shop::class.java)
                startActivity(intent)
            }
        }


        btnPurchase.setOnClickListener {
            val quantityText = quantity.text.toString().trim()

            if (quantityText.isEmpty()) {
                Toast.makeText(this@BuyProductActivity, "Add a Quantity", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val _quantity = quantityText.toIntOrNull()

            if (_quantity == null || _quantity <= 0) {
                Toast.makeText(this@BuyProductActivity, "Enter a valid quantity", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val totalPrice = selectedProduct?.price?.toDouble()?.times(_quantity!!) ?: 0.0
            purchaseItemOnline(totalPrice.toInt())
            intent.putExtra("quantity", _quantity)
        }


    }

    private fun fetchProductDetails(productId: Int) {
        val apiService = RetrofitClient.instance.create(Api::class.java)
        apiService.fetchProduct(productId).enqueue(object : Callback<FetchInventoryResponse> {
            override fun onResponse(call: Call<FetchInventoryResponse>, response: Response<FetchInventoryResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    Log.d("API_RESPONSE", "Response: ${response.body().toString()}") // Log the response
                    val productList = response.body()!!.data
                    if (productList.isNotEmpty()) {
                        selectedProduct = productList[0]
                        displayProductDetails(selectedProduct!!)
                    } else {
                        Toast.makeText(this@BuyProductActivity, "Product not found", Toast.LENGTH_SHORT).show()
                        finish() // Close the activity if the product does not exist
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("API_RESPONSE", "Error - HTTP ${response.code()}: $errorBody")
                    Toast.makeText(this@BuyProductActivity, "Failed to load product details", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<FetchInventoryResponse>, t: Throwable) {
                Log.e("PRODUCT_DETAILS", "Network error: ${t.message}", t)
                Toast.makeText(this@BuyProductActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayProductDetails(product: Product) {
        productName.text = product.product_name
        productPrice.text = "₱${product.price.toDouble().toInt()}"
        productStock.text = "Stock: ${product.stock_quantity}"

        // Load the product image using Glide
        val imageUrl = "https://glider-above-hopefully.ngrok-free.app/PumpingIronGym/storage/products/${product.product_image}"
        Glide.with(this)
            .load(imageUrl)
            .into(productImage)
    }

    private fun purchaseItemOnline(amount: Int) {
        stripe = Stripe(this, PaymentConfiguration.getInstance(this).publishableKey)
        createPaymentIntent(amount)

        if (etCard.isNotEmpty()) {
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
                            Toast.makeText(this@BuyProductActivity, "Error: ${it.error}", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("PaymentIntent", "Failed to get client secret. Response code: ${response.code()}, Error: $errorBody")
                    Toast.makeText(this@BuyProductActivity, "Failed to get client secret", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<PaymentIntentResponse>, t: Throwable) {
                Log.e("PaymentIntent", "API Call Failed: ${t.message}", t)
                Toast.makeText(this@BuyProductActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun processPayment() {
        val params = etCard.paymentMethodCreateParams

        if (params != null && clientSecret != null) {
            Log.d("PaymentProcess", "Creating payment method with provided card details")
            progressDialog.show()
            stripe.createPaymentMethod(params, callback = object :
                ApiResultCallback<PaymentMethod> {
                override fun onSuccess(paymentMethod: PaymentMethod) {
                    Log.d("PaymentProcess", "Payment method created successfully: ${paymentMethod.id}")
                    confirmPayment(paymentMethod.id!!)
                }

                override fun onError(e: Exception) {
                    progressDialog.dismiss()
                    Log.e("PaymentProcess", "Payment method error: ${e.message}", e)
                    Toast.makeText(this@BuyProductActivity, "Payment method error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            })
        } else {
            progressDialog.dismiss()
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
                    val quantity = intent.getIntExtra("quantity", 0)

                    purchaseProduct(quantity)
                    Toast.makeText(this@BuyProductActivity, "Payment Successful!", Toast.LENGTH_LONG).show()
                }
            }

            override fun onError(e: Exception) {
                progressDialog.dismiss()
                Log.e("PaymentProcess", "Payment failed: ${e.message}", e)
                Toast.makeText(this@BuyProductActivity, "Payment failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun purchaseProduct(quantity: Int){
        val preferenceManager = PreferenceManager(this@BuyProductActivity)
        val email = preferenceManager.getEmail()

        if (email == null || quantity == 0) {
            return
        }

        val productName = selectedProduct!!.product_name
        val orderRequest = OrderRequest(email, productName, quantity)

        val api = RetrofitClient.instance.create(Api::class.java)
        api.sendOrder(orderRequest).enqueue(object : Callback<OrderResponse> {
            override fun onResponse(call: Call<OrderResponse>, response: Response<OrderResponse>) {
                if (response.isSuccessful) {
                    progressDialog.dismiss()
                    Toast.makeText(this@BuyProductActivity, "Product Purchased", Toast.LENGTH_SHORT).show()
                    val preferenceManager = PreferenceManager(this@BuyProductActivity)
                    val role = preferenceManager.getRole()
                    if (role != null) {
                        if (role == "trainer") {
                            val intent = Intent(this@BuyProductActivity, TrainerAccount::class.java)
                            startActivity(intent)
                        } else {
                            val intent = Intent(this@BuyProductActivity, Account::class.java)
                            startActivity(intent)
                        }
                    }
                } else {
                    progressDialog.dismiss()
                    Log.e("ORDER_RESPONSE", "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<OrderResponse>, t: Throwable) {
                Log.d("ORDER_RESPONSE", "Parameters: email${email}, product name: ${productName}, quantity: ${quantity}")
                Log.e("ORDER_RESPONSE", "Network error: ${t.message}", t)
                progressDialog.dismiss()
                Toast.makeText(this@BuyProductActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}