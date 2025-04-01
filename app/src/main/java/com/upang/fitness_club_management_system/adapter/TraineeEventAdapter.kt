package com.upang.fitness_club_management_system

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.upang.fitness_club_management_system.model.TraineeEvent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TraineeEventAdapter(eventList: List<TraineeEvent>) : RecyclerView.Adapter<TraineeEventAdapter.TraineeEventViewHolder>() {

    private val filteredEvents = eventList.filter { event ->
        val eventDateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentDate = Date()
        val eventStartDateTime = eventDateTimeFormat.parse("${event.assignment_date} ${event.start_time}")
        val eventEndDateTime = eventDateTimeFormat.parse("${event.assignment_date} ${event.end_time}")
        val status = when {
            eventEndDateTime != null && currentDate.after(eventEndDateTime) -> "Ended"
            eventStartDateTime != null && currentDate.before(eventStartDateTime) -> "Incoming"
            else -> "Ongoing"
        }
        status != "Ended"
    }

    class TraineeEventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val traineeName: TextView = view.findViewById(R.id.trineeName)
        val assignmentDate: TextView = view.findViewById(R.id.assignmentDate)
        val eventTime: TextView = view.findViewById(R.id.eventTime)
        val status: TextView = view.findViewById(R.id.status)
        val noSched: LinearLayout = view.findViewById(R.id.rowNoSchedule)
        val traineeNameRow: LinearLayout = view.findViewById(R.id.nameRow)
        val assignmentDateRow: LinearLayout = view.findViewById(R.id.dateRow)
        val eventTimeRow: LinearLayout = view.findViewById(R.id.timeRow)
        val statusRow: LinearLayout = view.findViewById(R.id.statusRow)
        val lineName: View = view.findViewById(R.id.lNameRow)
        val lineDate: View = view.findViewById(R.id.lDateRow)
        val lineTime: View = view.findViewById(R.id.lTimeRow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TraineeEventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return TraineeEventViewHolder(view)
    }

    override fun onBindViewHolder(holder: TraineeEventViewHolder, position: Int) {
        if (filteredEvents.isEmpty()) {
            // Display "No Upcoming Schedule"
            holder.noSched.visibility = View.VISIBLE

            // Hide other event details
            holder.traineeNameRow.visibility = View.GONE
            holder.assignmentDateRow.visibility = View.GONE
            holder.eventTimeRow.visibility = View.GONE
            holder.statusRow.visibility = View.GONE
            holder.lineDate.visibility = View.GONE
            holder.lineTime.visibility = View.GONE
            holder.lineName.visibility = View.GONE
        } else {
            val event = filteredEvents.first() // Only take the first event

            // Show event details
            holder.noSched.visibility = View.GONE
            holder.traineeNameRow.visibility = View.VISIBLE
            holder.assignmentDateRow.visibility = View.VISIBLE
            holder.eventTimeRow.visibility = View.VISIBLE
            holder.statusRow.visibility = View.VISIBLE
            holder.lineDate.visibility = View.VISIBLE
            holder.lineTime.visibility = View.VISIBLE
            holder.lineName.visibility = View.VISIBLE

            holder.traineeName.text = event.trainer_name

            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
            val date: Date? = inputFormat.parse(event.assignment_date)
            val formattedDate = date?.let { outputFormat.format(it) } ?: event.assignment_date

            val timeInputFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val timeOutputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val startTime: Date? = timeInputFormat.parse(event.start_time)
            val endTime: Date? = timeInputFormat.parse(event.end_time)

            val formattedStartTime = startTime?.let { timeOutputFormat.format(it) } ?: event.start_time
            val formattedEndTime = endTime?.let { timeOutputFormat.format(it) } ?: event.end_time

            val eventDateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val eventStartDateTime = eventDateTimeFormat.parse("${event.assignment_date} ${event.start_time}")
            val eventEndDateTime = eventDateTimeFormat.parse("${event.assignment_date} ${event.end_time}")
            val currentDate = Date()
            val status = when {
                eventEndDateTime != null && currentDate.after(eventEndDateTime) -> "Ended"
                eventStartDateTime != null && currentDate.before(eventStartDateTime) -> "Incoming"
                else -> "Ongoing"
            }

            holder.assignmentDate.text = formattedDate
            holder.eventTime.text = "$formattedStartTime - $formattedEndTime"
            holder.status.text = status
        }
    }

    override fun getItemCount(): Int {
        return if (filteredEvents.isNotEmpty()) 1 else 1 // Always show one item (either event or "No Schedule")
    }
}
