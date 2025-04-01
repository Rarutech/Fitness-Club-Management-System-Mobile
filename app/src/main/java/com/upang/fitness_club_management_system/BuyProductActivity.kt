package com.upang.fitness_club_management_system

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.bumptech.glide.Glide
import com.stripe.android.PaymentConfiguration
import com.stripe.android.Stripe
import com.stripe.android.view.CardInputWidget
import com.upang.fitness_club_management_system.api.Api
import com.upang.fitness_club_management_system.api.RetrofitClient
import com.upang.fitness_club_management_system.helper.PreferenceManager
import com.upang.fitness_club_management_system.model.FetchInventoryResponse
import com.upang.fitness_club_management_system.model.OrderRequest
import com.upang.fitness_club_management_system.model.OrderResponse
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
    private lateinit var payOffline: RadioButton
    private lateinit var payOnline: RadioButton
    private lateinit var etCard: CardInputWidget
    private var selectedProduct: Product? = null
    private var clientSecret: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_buy_product)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        productImage = findViewById(R.id.productImage)
        productName = findViewById(R.id.productName)
        productPrice = findViewById(R.id.productPrice)
        productStock = findViewById(R.id.productStock)
        btnPurchase = findViewById(R.id.btnPurchase)
        payOffline = findViewById(R.id.payOffline)
        payOnline = findViewById(R.id.payOnline)
        quantity = findViewById(R.id.etQuantity)


        payOnline.setOnClickListener {
            payOffline.isChecked = false
        }

        payOffline.setOnClickListener {
            payOnline.isChecked = false
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
            if (!payOnline.isChecked && !payOffline.isChecked) {
                Toast.makeText(this@BuyProductActivity, "Please select a payment method", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (payOnline.isChecked){

            }
            purchaseItemOffline(_quantity)
        }


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

    private fun purchaseItemOffline(quantity: Int) {
        if (selectedProduct == null) {
            return
        }

        // Create the OrderRequest object
        val preferenceManager = PreferenceManager(this)
        val email = preferenceManager.getEmail()
        if (email == null) {
            return
        }
        val productName = selectedProduct!!.product_name

        val orderRequest = OrderRequest(email, productName, quantity)

        val api = RetrofitClient.instance.create(Api::class.java)
        api.sendOrder(orderRequest).enqueue(object : Callback<OrderResponse> {
            override fun onResponse(call: Call<OrderResponse>, response: Response<OrderResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@BuyProductActivity, "Product Purchased", Toast.LENGTH_SHORT).show()

                    val preferenceManager = PreferenceManager(this@BuyProductActivity)
                    val role = preferenceManager.getRole()
                    if (role != null) {
                        if (role == "trainer") {
                            val intent = Intent(this@BuyProductActivity, TrainerAccount::class.java)
                            startActivity(intent)
                        }
                    } else{
                        val intent = Intent(this@BuyProductActivity, Account::class.java)
                        startActivity(intent)
                    }
                } else {
                    Log.e("ORDER_RESPONSE", "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<OrderResponse>, t: Throwable) {
                Log.d("ORDER_RESPONSE", "Parameters: email${email}, product name: ${productName}, quantity: ${quantity}")
                Log.e("ORDER_RESPONSE", "Network error: ${t.message}", t)
                Toast.makeText(this@BuyProductActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

//    private fun purchaseItemOnline() {
//        PaymentConfiguration.init(
//            applicationContext,
//            "pk_test_51R7qAeBNSwOEu2mpYqg3LpokRdbt17nufCifDObthMiiOzuybNT8lnbWUJYdYHNr4gSs7QrafjN8ExeScD91FcLN002nD7PMvM"
//        )
//        stripe = Stripe(this, PaymentConfiguration.getInstance(this).publishableKey)
//
//        etCard = findViewById(R.id.etCard)
//        payButton = findViewById(R.id.payButton)
//
//        createPaymentIntent(600)
//
//        payButton.setOnClickListener {
//            processPayment()
//        }
//    }
}