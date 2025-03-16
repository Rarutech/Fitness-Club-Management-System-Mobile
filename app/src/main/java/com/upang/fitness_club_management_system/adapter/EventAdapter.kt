package com.upang.fitness_club_management_system.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.upang.fitness_club_management_system.R
import com.upang.fitness_club_management_system.model.Event
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventAdapter(private val eventList: List<Event>) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {
    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val traineeName: TextView = view.findViewById(R.id.trineeName)
        val assignmentDate: TextView = view.findViewById(R.id.assignmentDate)
        val eventTime: TextView = view.findViewById(R.id.eventTime)
        val status: TextView = view.findViewById(R.id.status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventAdapter.EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventAdapter.EventViewHolder, position: Int) {
        val event = eventList[position]
        holder.traineeName.text = "${event.trainer_name}"

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

        val currentDate = Date() // Current date and time
        val eventDateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val eventStartDateTime = eventDateTimeFormat.parse("${event.assignment_date} ${event.start_time}")
        val eventEndDateTime = eventDateTimeFormat.parse("${event.assignment_date} ${event.end_time}")
        val status = when {
            currentDate.after(eventEndDateTime) -> "Ended"
            currentDate.before(eventStartDateTime) -> "Incoming"
            else -> "Ongoing"
        }

        holder.assignmentDate.text = formattedDate
        holder.eventTime.text = "${formattedStartTime} - ${formattedEndTime}"
        holder.status.text = status
    }

    override fun getItemCount(): Int {
        return eventList.size
    }
}