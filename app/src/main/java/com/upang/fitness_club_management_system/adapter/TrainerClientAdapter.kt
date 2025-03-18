package com.upang.fitness_club_management_system.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.upang.fitness_club_management_system.R
import com.upang.fitness_club_management_system.model.TrainerRequestResponse
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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_client, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val request = trainerRequests[position]

        // Format date for better readability
        val formattedDate = formatDate(request.date_of_training)
        holder.traineeName.text = request.user_name.trim { it <= ' ' }
        holder.trainingPlan.text = request.description.trim { it <= ' ' }
        holder.eventDate.text = formattedDate

        // Check if the training date has passed
        if (isDatePassed(request.date_of_training)) {
            holder.status.text = "Completed"
        } else {
            holder.status.text = request.status.trim { it <= ' ' }
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
}