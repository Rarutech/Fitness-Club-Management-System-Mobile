    package com.upang.fitness_club_management_system

    import android.content.Context
    import android.content.Intent
    import android.content.SharedPreferences
    import android.os.Bundle
    import android.util.Log
    import android.widget.SearchView
    import android.widget.Toast
    import androidx.appcompat.app.AppCompatActivity
    import androidx.core.view.ViewCompat
    import androidx.core.view.WindowInsetsCompat
    import androidx.recyclerview.widget.GridLayoutManager
    import androidx.recyclerview.widget.RecyclerView
    import com.google.android.material.bottomnavigation.BottomNavigationView
    import com.upang.fitness_club_management_system.adapter.ShopAdapter
    import com.upang.fitness_club_management_system.api.Api
    import com.upang.fitness_club_management_system.api.RetrofitClient
    import com.upang.fitness_club_management_system.model.FetchInventoryResponse
    import com.upang.fitness_club_management_system.model.Product
    import retrofit2.Call
    import retrofit2.Callback
    import retrofit2.Response

    class TrainerShop : AppCompatActivity() {

        private lateinit var shopRecyclerView: RecyclerView
        private lateinit var shopAdapter: ShopAdapter
        private lateinit var sharedPreferences: SharedPreferences
        private var productList: List<Product> = emptyList()
        private var filteredProductList: List<Product> = emptyList()

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_trainer_shop)
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }

            sharedPreferences = getSharedPreferences("shop_prefs", Context.MODE_PRIVATE)
            shopRecyclerView = findViewById(R.id.shopRecyclerView)
            shopRecyclerView.layoutManager = GridLayoutManager(this, 2)

            val searchView = findViewById<SearchView>(R.id.searchView)
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    // Not necessary to handle submit in this case
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    filterProducts(newText)
                    return true
                }
            })

            fetchInventory()
            setupBottomNavigationView()
        }

        private fun fetchInventory() {
            RetrofitClient.instance.create(Api::class.java).fetchInventory().enqueue(object : Callback<FetchInventoryResponse> {
                override fun onResponse(call: Call<FetchInventoryResponse>, response: Response<FetchInventoryResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        setupRecyclerView(response.body()!!.data)
                    } else {
                        showError(response.code(), response.errorBody()?.string())
                    }
                }

                override fun onFailure(call: Call<FetchInventoryResponse>, t: Throwable) {
                    Log.e("API_FAILURE", "Network request failed: ${t.message}", t)
                    Toast.makeText(this@TrainerShop, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }

        private fun setupRecyclerView(productList: List<Product>) {
            shopAdapter = ShopAdapter(productList) { selectedProduct ->
                saveProductId(selectedProduct.id)
                startActivity(Intent(this, ProductDetailsActivity::class.java))
            }
            shopRecyclerView.adapter = shopAdapter
        }

        private fun saveProductId(productId: Int) {
            sharedPreferences.edit().putInt("selected_product_id", productId).apply()
        }

        private fun setupBottomNavigationView() {
            val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
            bottomNavigationView.selectedItemId = R.id.actionShopTrainer

            bottomNavigationView.setOnItemSelectedListener { item ->
                val targetActivity = when (item.itemId) {
                    R.id.actionHomeTrainer -> TrainerHomeActivity::class.java
                    R.id.actionSchedule -> TrainerScheduleActivity::class.java
                    R.id.actionPost -> PostHighlightActivity::class.java
                    R.id.actionClients -> TrainerClients::class.java
                    R.id.actionShopTrainer -> TrainerShop::class.java
                    else -> null
                }

                if (targetActivity != null && targetActivity != javaClass) {
                    startActivity(Intent(this, targetActivity))
                    finish()
                }
                true
            }
        }

        private fun filterProducts(query: String?) {
            val filteredList = if (query.isNullOrEmpty()) {
                productList // If the query is empty, show the full list
            } else {
                productList.filter { it.product_name.contains(query, ignoreCase = true) }
            }
            shopAdapter = ShopAdapter(filteredList) { selectedProduct ->
                saveProductId(selectedProduct.id)
                startActivity(Intent(this, ProductDetailsActivity::class.java))
            }
            shopRecyclerView.adapter = shopAdapter
        }

        private fun showError(errorCode: Int, errorBody: String?) {
            val errorMessage = "Failed to load products (Error $errorCode: ${errorBody ?: "Unknown error"})"
            Log.e("API_ERROR", errorMessage)
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }
