package com.upang.fitness_club_management_system

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.upang.fitness_club_management_system.adapter.TraineeAppointmentAdapter
import com.upang.fitness_club_management_system.api.Api
import com.upang.fitness_club_management_system.api.RetrofitClient
import com.upang.fitness_club_management_system.helper.PreferenceManager
import com.upang.fitness_club_management_system.model.TraineeRequestApiResponse
import com.upang.fitness_club_management_system.model.TraineeRequestResponse
import com.upang.fitness_club_management_system.model.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TraineeAppointments : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TraineeAppointmentAdapter
    private var allAppointments = listOf<TraineeRequestResponse>()
    private val handler = Handler()
    private val delay: Long = 5000
    private val runnable = object : Runnable {
        override fun run() {
            Utils.getNotifications(this@TraineeAppointments)
            handler.postDelayed(this, delay)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_trainee_appointments)

        recyclerView = findViewById(R.id.rvTrainers)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val dropdownButton: ImageButton = findViewById(R.id.btnFilter)
        dropdownButton.setOnClickListener { view -> showPopupMenu(view) }

        val searchView = findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { filterAppointmentsByQuery(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { filterAppointmentsByQuery(it) }
                return true
            }
        })

        adapter = TraineeAppointmentAdapter(emptyList())
        recyclerView.adapter = adapter

        fetchAppointments()
    }

    private fun fetchAppointments() {
        val api = RetrofitClient.instance.create(Api::class.java)
        val preferenceManager = PreferenceManager(this)
        val email = preferenceManager.getEmail() ?: return

        api.fetchTraineeRequest(email).enqueue(object : Callback<TraineeRequestApiResponse> {
            override fun onResponse(call: Call<TraineeRequestApiResponse>, response: Response<TraineeRequestApiResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    allAppointments = response.body()!!.requests
                    adapter.updateList(allAppointments) // Set all data initially
                } else {
                    Toast.makeText(this@TraineeAppointments, "Failed to fetch data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<TraineeRequestApiResponse>, t: Throwable) {
                Log.e("API_ERROR", t.message.toString())
                Toast.makeText(this@TraineeAppointments, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.dropdown_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            val selectedStatus = when (item.itemId) {
                R.id.all -> "All"
                R.id.approved -> "approved"
                R.id.pending -> "pending"
                R.id.completed -> "completed"
                R.id.rejected -> "rejected"
                else -> null
            }

            selectedStatus?.let {
                filterAppointmentsByStatus(it)
            }

            true
        }

        popupMenu.show()
    }

    private fun filterAppointmentsByStatus(status: String) {
        val filteredAppointments = if (status == "All") allAppointments else allAppointments.filter { it.status == status }

        if (filteredAppointments.isEmpty()) {
            findViewById<TextView>(R.id.tvNoBooking).visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            findViewById<TextView>(R.id.tvNoBooking).visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            adapter.updateList(filteredAppointments)
        }
    }

    private fun filterAppointmentsByQuery(query: String) {
        val filteredAppointments = allAppointments.filter {
            it.trainer_name.contains(query, ignoreCase = true) ||
                    it.status.contains(query, ignoreCase = true)
        }

        if (filteredAppointments.isEmpty()) {
            findViewById<TextView>(R.id.tvNoBooking).visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            findViewById<TextView>(R.id.tvNoBooking).visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            adapter.updateList(filteredAppointments)
        }
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
