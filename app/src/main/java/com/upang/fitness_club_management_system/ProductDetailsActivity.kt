package com.upang.fitness_club_management_system

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.upang.fitness_club_management_system.api.Api
import com.upang.fitness_club_management_system.api.RetrofitClient
import com.upang.fitness_club_management_system.model.FetchInventoryResponse
import com.upang.fitness_club_management_system.model.Product
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProductDetailsActivity : AppCompatActivity() {
    private lateinit var productImage: ImageView
    private lateinit var productName: TextView
    private lateinit var productDescription: TextView
    private lateinit var productPrice: TextView
    private lateinit var productStock: TextView
    private lateinit var btnBuyNow: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_details)

        productImage = findViewById(R.id.productImage)
        productName = findViewById(R.id.productName)
        productDescription = findViewById(R.id.productDescription)
        productPrice = findViewById(R.id.productPrice)
        productStock = findViewById(R.id.productStock)
        btnBuyNow = findViewById(R.id.btnBuyNow)

        val toolbar: Toolbar = findViewById(R.id.toolbarProductDetails)

        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        toolbar.setNavigationOnClickListener {
            val intent = Intent(this@ProductDetailsActivity, Shop::class.java)
            startActivity(intent)
        }

        btnBuyNow.setOnClickListener {
            val intent = Intent(this@ProductDetailsActivity,BuyProductActivity::class.java)
            startActivity(intent)
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
                        displayProductDetails(productList[0])
                    } else {
                        Toast.makeText(this@ProductDetailsActivity, "Product not found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("API_RESPONSE", "Error - HTTP ${response.code()}: $errorBody")
                    Toast.makeText(this@ProductDetailsActivity, "Failed to load product details", Toast.LENGTH_SHORT).show()
                }
            }


            override fun onFailure(call: Call<FetchInventoryResponse>, t: Throwable) {
                Log.e("PRODUCT_DETAILS", "Network error: ${t.message}", t)
                Toast.makeText(this@ProductDetailsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayProductDetails(product: Product) {
        productName.text = product.product_name
        productDescription.text = product.description
        productPrice.text = "₱${product.price.toDouble().toInt()}"
        productStock.text = "Stock: ${product.stock_quantity}"

        val imageUrl = "https://glider-above-hopefully.ngrok-free.app/PumpingIronGym/storage/products/${product.product_image}"

        Glide.with(this)
            .load(imageUrl)
            .into(productImage)
    }
}
