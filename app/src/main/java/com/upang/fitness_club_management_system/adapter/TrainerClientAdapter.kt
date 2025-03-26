package com.upang.fitness_club_management_system.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.upang.fitness_club_management_system.R
import com.upang.fitness_club_management_system.TrainerClients
import com.upang.fitness_club_management_system.api.Api
import com.upang.fitness_club_management_system.api.RetrofitClient
import com.upang.fitness_club_management_system.model.TraineePendingResponse
import com.upang.fitness_club_management_system.model.TrainerRequestResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TrainerClientAdapter(private val trainerRequests: List<TrainerRequestResponse>) :
    RecyclerView.Adapter<TrainerClientAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var traineeName: TextView = itemView.findViewById(R.id.trineeName)
        var trainingPlan: TextView = itemView.findViewById(R.id.trainingPlan)
        var eventDate: TextView = itemView.findViewById(R.id.eventDate)
        var status: TextView = itemView.findViewById(R.id.status)
        var btnAccept: Button = itemView.findViewById(R.id.btnAccept)
        var btnReject: Button = itemView.findViewById(R.id.btnReject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_client, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val request = trainerRequests[position]

        // Format date for better readability
        val formattedDate = formatDate(request.date_of_training)
        holder.traineeName.text = request.user_name.trim()
        holder.trainingPlan.text = request.description.trim()
        holder.eventDate.text = formattedDate
        val request_id = request.request_id

        // Trim and store the status value
        val statusValue = request.status.trim().lowercase(Locale.ROOT)

        Log.d("TrainerClientAdapter", "Request ID: $request_id, Status: '$statusValue'")

        if (isDatePassed(request.date_of_training)) {
            holder.status.text = "Completed"
            holder.btnAccept.visibility = View.GONE
            holder.btnReject.visibility = View.GONE
        } else {
            holder.status.text = request.status.trim()
            holder.btnAccept.visibility = View.GONE
            holder.btnReject.visibility = View.GONE

            if (statusValue == "pending") {
                Log.d("TrainerClientAdapter", "Showing buttons for Request ID: $request_id")
                holder.btnAccept.visibility = View.VISIBLE
                holder.btnReject.visibility = View.VISIBLE
            }

            holder.btnAccept.setOnClickListener {
                val jsonObject = JSONObject()
                jsonObject.put("request_id", request_id)
                val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
                val api = RetrofitClient.instance.create(Api::class.java)
                api.acceptTrainer(requestBody).enqueue(object : retrofit2.Callback<TraineePendingResponse> {
                    override fun onResponse(call: Call<TraineePendingResponse>, response: Response<TraineePendingResponse>) {
                        if (response.isSuccessful) {
                            if(response.body()?.status == "success") {
                                Toast.makeText(holder.itemView.context, "Client Accepted", Toast.LENGTH_SHORT).show()
                                Log.d("TrainerClientAdapter", "Client Accepted")
                                refreshActivity(holder.itemView.context)
                            } else {
                                Log.d("TrainerClientAdapter", "Failed to accept client: ${response.body()?.message}")
                                Toast.makeText(holder.itemView.context, "Failed to accept client", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Log.d("TrainerClientAdapter", "Failed to accept client")
                            Toast.makeText(holder.itemView.context, "Failed to accept client", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<TraineePendingResponse>, t: Throwable) {
                        Toast.makeText(holder.itemView.context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                        Log.e("TrainerClientAdapter", "Error: ${t.message}")
                    }
                })
            }

            holder.btnReject.setOnClickListener {
                val jsonObject = JSONObject()
                jsonObject.put("request_id", request_id)
                val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
                val api = RetrofitClient.instance.create(Api::class.java)
                api.rejectTrainer(requestBody).enqueue(object : retrofit2.Callback<TraineePendingResponse> {
                    override fun onResponse(call: Call<TraineePendingResponse>, response: Response<TraineePendingResponse>) {
                        if (response.isSuccessful) {
                            Toast.makeText(holder.itemView.context, "Client Rejected", Toast.LENGTH_SHORT).show()
                            refreshActivity(holder.itemView.context)
                        } else {
                            Toast.makeText(holder.itemView.context, "Failed to reject client", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<TraineePendingResponse>, t: Throwable) {
                        Toast.makeText(holder.itemView.context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                        Log.e("TrainerClientAdapter", "Error: ${t.message}")
                    }
                })
            }
        }
    }


    override fun getItemCount(): Int {
        return trainerRequests.size
    }

    private fun formatDate(dateString: String): String {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            return outputFormat.format(date)
        } catch (e: ParseException) {
            return dateString
        }
    }

    private fun isDatePassed(dateString: String): Boolean {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val trainingDate = inputFormat.parse(dateString)
            return trainingDate != null && trainingDate.before(Calendar.getInstance().time)
        } catch (e: ParseException) {
            return false
        }
    }

    private fun refreshActivity(context: Context) {
        val intent = Intent(context, TrainerClients::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
