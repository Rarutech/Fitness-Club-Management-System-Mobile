package com.upang.fitness_club_management_system.adapter


import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.upang.fitness_club_management_system.R
import com.upang.fitness_club_management_system.Reschedule
import com.upang.fitness_club_management_system.TraineeAppointments
import com.upang.fitness_club_management_system.api.Api
import com.upang.fitness_club_management_system.api.RetrofitClient
import com.upang.fitness_club_management_system.model.CancelScheduleResponse
import com.upang.fitness_club_management_system.model.TraineeRequestResponse
import retrofit2.Call
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale

class TraineeAppointmentAdapter(private val appointments: List<TraineeRequestResponse>) :
    RecyclerView.Adapter<TraineeAppointmentAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val trainerName: TextView = itemView.findViewById(R.id.trainerName)
        val trainingPlan: TextView = itemView.findViewById(R.id.trainingPlan)
        val eventDate: TextView = itemView.findViewById(R.id.assignmentDate)
        val status: TextView = itemView.findViewById(R.id.status)
        val eventTime: TextView = itemView.findViewById(R.id.eventTime)
        val btnReschedule: Button = itemView.findViewById(R.id.btnReschedule)
        val btnCancel: Button = itemView.findViewById(R.id.btnCancel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_booked_trainers, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appointment = appointments[position]
        holder.trainerName.text = appointment.trainer_name
        holder.trainingPlan.text = appointment.description
        holder.status.text = appointment.status
        holder.eventDate.text = formatDate(appointment.date_of_training)
        holder.eventTime.text = "${formatTime(appointment.time_start)} - ${formatTime(appointment.time_end)}"

        if (holder.status.text == "pending") {
            holder.btnReschedule.visibility = View.VISIBLE
            holder.btnCancel.visibility = View.VISIBLE
        } else {
            holder.btnReschedule.visibility = View.GONE
            holder.btnCancel.visibility = View.GONE
        }

        holder.btnReschedule.setOnClickListener {
            val sharedPreferences = holder.itemView.context.getSharedPreferences("user", MODE_PRIVATE)
            val editor = sharedPreferences.edit()

            editor.putString("request_id", appointment.request_id.toString())
            editor.apply()


            val intent = Intent(holder.itemView.context, Reschedule::class.java)
            intent.putExtra("request_id", appointment.request_id.toString())
            holder.itemView.context.startActivity(intent)
        }
        holder.btnCancel.setOnClickListener {
            val api = RetrofitClient.instance.create(Api::class.java)
            api.cancelSchedule(appointment.request_id).enqueue(object : retrofit2.Callback<CancelScheduleResponse> {
                override fun onResponse(
                    call: Call<CancelScheduleResponse>,
                    response: Response<CancelScheduleResponse>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(holder.itemView.context, "Schedule Cancelled", Toast.LENGTH_SHORT).show()

                        val activity = holder.itemView.context as? TraineeAppointments
                        activity?.finish()
                        activity?.startActivity(activity.intent)
                    } else {
                        Toast.makeText(holder.itemView.context, "Failed to cancel", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<CancelScheduleResponse>, t: Throwable) {
                    Toast.makeText(holder.itemView.context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

    }


    override fun getItemCount(): Int {
        return appointments.size
    }

    private fun formatDate(dateString: String): String {
        return try {
            val inputDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val date = inputDateFormat.parse(dateString)
            outputDateFormat.format(date!!)
        } catch (e: Exception) {
            dateString
        }
    }

    private fun formatTime(timeString: String): String {
        return try {
            val inputTimeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val outputTimeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val time = inputTimeFormat.parse(timeString)
            outputTimeFormat.format(time!!)
        } catch (e: Exception) {
            timeString
        }
    }
}
