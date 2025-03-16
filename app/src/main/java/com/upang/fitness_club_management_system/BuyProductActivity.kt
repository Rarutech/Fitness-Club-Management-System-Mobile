package com.upang.fitness_club_management_system

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
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
    private lateinit var productImage: ImageView
    private lateinit var productName: TextView
    private lateinit var productPrice: TextView
    private lateinit var productStock: TextView
    private lateinit var btnPurchase: TextView
    private lateinit var quantity: EditText
    private var selectedProduct: Product? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_buy_product)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize views
        productImage = findViewById(R.id.productImage)
        productName = findViewById(R.id.productName)
        productPrice = findViewById(R.id.productPrice)
        productStock = findViewById(R.id.productStock)
        btnPurchase = findViewById(R.id.btnPurchase)
        quantity = findViewById(R.id.etQuantity)
        // Set up the toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this@BuyProductActivity, Shop::class.java)
            startActivity(intent)
        }

        // Set up the purchase button click listener
        btnPurchase.setOnClickListener {
            val quantityText = quantity.text.toString().trim()

            if (quantityText.isEmpty()) {
                Toast.makeText(this@BuyProductActivity, "Add a Quantity", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Stop execution if quantity is empty
            }

            val _quantity = quantityText.toIntOrNull() // Convert safely to Int

            if (_quantity == null || _quantity <= 0) {
                Toast.makeText(this@BuyProductActivity, "Enter a valid quantity", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            purchaseItem(_quantity)
        }

        // Retrieve the selected product ID from shared preferences
        val sharedPreferences = getSharedPreferences("shop_prefs", Context.MODE_PRIVATE)
        val productId = sharedPreferences.getInt("selected_product_id", -1)

        Log.d("PRODUCT_DETAILS", "Retrieved Product ID: $productId")

        if (productId != -1) {
            fetchProductDetails(productId) // Fetch product details from the API
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

    private fun purchaseItem(quantity: Int) {
        if (selectedProduct == null) {
            Toast.makeText(this, "Product details not available", Toast.LENGTH_SHORT).show()
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
                Log.d("ORDER_RESPONSE", "Parameters: email${email}, product name: ${productName}, quantity: ${quantity}")
                Log.d("ORDER_RESPONSE", "Raw: ${response.raw()}")
                Log.d("ORDER_RESPONSE", "Body: ${response.body()}")
                Log.d("ORDER_RESPONSE", "ErrorBody: ${response.errorBody()?.string()}")

                if (response.isSuccessful) {
                    Log.d("ORDER_RESPONSE", "Success: ${response.body()}")
                    val intent = Intent(this@BuyProductActivity, Shop::class.java)
                    startActivity(intent)
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
}