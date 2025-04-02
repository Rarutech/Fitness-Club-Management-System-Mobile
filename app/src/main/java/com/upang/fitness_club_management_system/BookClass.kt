package com.upang.fitness_club_management_system

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.ImageButton
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.upang.fitness_club_management_system.adapter.TrainersAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.upang.fitness_club_management_system.api.Api
import com.upang.fitness_club_management_system.model.FetchTrainersResponse
import com.upang.fitness_club_management_system.api.RetrofitClient
import com.upang.fitness_club_management_system.model.Profile
import com.upang.fitness_club_management_system.model.Utils

class BookClass : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var trainerAdapter: TrainersAdapter
    private var allTrainers = listOf<Profile>()
    private val handler = Handler()
    private val delay: Long = 5000
    private val runnable = object : Runnable {
        override fun run() {
            Utils.getNotifications(this@BookClass)
            handler.postDelayed(this, delay)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_book_class)
        Utils.getNotifications(this)

        val btnAccount = findViewById<ImageButton>(R.id.btnAccount)
        btnAccount.setOnClickListener {
            val intent = Intent(this, TrainerAccount::class.java)
            startActivity(intent)
        }

        recyclerView = findViewById(R.id.rvTrainers)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val fabAppointment = findViewById<FloatingActionButton>(R.id.fabAppointment)
        fabAppointment.setOnClickListener{
            val intent = Intent(this,TraineeAppointments::class.java)
            startActivity(intent)
        }
        val searchView = findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { filterTrainersByName(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { filterTrainersByName(it) }
                return true
            }
        })

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.actionClasses

        when (javaClass) {
            Trainee_Home::class.java -> bottomNavigationView.selectedItemId = R.id.actionHome
            Progress::class.java -> bottomNavigationView.selectedItemId = R.id.actionProgress
            BookClass::class.java -> bottomNavigationView.selectedItemId = R.id.actionClasses
            Shop::class.java -> bottomNavigationView.selectedItemId = R.id.actionShop
        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            val targetActivity = when (item.itemId) {
                R.id.actionHome -> Trainee_Home::class.java
                R.id.actionProgress -> Progress::class.java
                R.id.actionClasses -> BookClass::class.java
                R.id.actionShop -> Shop::class.java
                else -> null
            }

            if (targetActivity != null && targetActivity != javaClass) {
                startActivity(Intent(this, targetActivity))
                finish()
            }
            true
        }

        fetchTrainers()
    }

    private fun fetchTrainers() {
        val api = RetrofitClient.instance.create(Api::class.java)
        api.fetchAllTrainers().enqueue(object : Callback<FetchTrainersResponse> {
            override fun onResponse(call: Call<FetchTrainersResponse>, response: Response<FetchTrainersResponse>) {
                if (response.isSuccessful) {
                    allTrainers = response.body()?.profiles ?: emptyList()
                    trainerAdapter = TrainersAdapter(this@BookClass, allTrainers)
                    recyclerView.adapter = trainerAdapter
                } else {
                    Toast.makeText(this@BookClass, "Failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<FetchTrainersResponse>, t: Throwable) {
                Toast.makeText(this@BookClass, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filterTrainersByName(query: String) {
        val filteredTrainers = allTrainers.filter {
            it.fullname.contains(query, ignoreCase = true)
        }
        trainerAdapter.updateList(filteredTrainers)
    }
    override fun onStart() {
        super.onStart()
        // Start checking membership status when the activity is visible
        handler.post(runnable)
    }

    override fun onStop() {
        super.onStop()
        // Stop checking membership status when the activity is not visible
        handler.removeCallbacks(runnable)
    }
}
