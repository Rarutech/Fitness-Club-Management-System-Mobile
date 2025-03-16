package com.upang.fitness_club_management_system

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.upang.fitness_club_management_system.adapter.ShopAdapter
import com.upang.fitness_club_management_system.api.Api
import com.upang.fitness_club_management_system.api.RetrofitClient
import com.upang.fitness_club_management_system.model.FetchInventoryResponse
import com.upang.fitness_club_management_system.model.Product
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Shop : AppCompatActivity() {
    private lateinit var shopRecyclerView: RecyclerView
    private lateinit var shopAdapter: ShopAdapter
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_shop)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sharedPreferences = getSharedPreferences("shop_prefs", Context.MODE_PRIVATE)

        shopRecyclerView = findViewById(R.id.shopRecyclerView)
        shopRecyclerView.layoutManager = GridLayoutManager(this, 2) // 2 items per row

        fetchInventory()
    }

    private fun fetchInventory() {
        val api = RetrofitClient.instance.create(Api::class.java)
        api.fetchInventory().enqueue(object : Callback<FetchInventoryResponse> {
            override fun onResponse(
                call: Call<FetchInventoryResponse>,
                response: Response<FetchInventoryResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val productList = response.body()!!.data
                    setupRecyclerView(productList)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    val errorCode = response.code()
                    Log.e("API_ERROR", "Error $errorCode: $errorBody")
                    Toast.makeText(this@Shop, "Failed to load products (Error $errorCode)", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<FetchInventoryResponse>, t: Throwable) {
                Log.e("API_FAILURE", "Network request failed: ${t.message}", t)
                Toast.makeText(this@Shop, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }



    private fun setupRecyclerView(productList: List<Product>) {
        shopAdapter = ShopAdapter(productList) { selectedProduct ->
            saveProductId(selectedProduct.id)
            val intent = Intent(this, ProductDetailsActivity::class.java)
            startActivity(intent)
        }
        shopRecyclerView.adapter = shopAdapter
    }

    private fun saveProductId(productId: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt("selected_product_id", productId)
        editor.apply()
    }
}
