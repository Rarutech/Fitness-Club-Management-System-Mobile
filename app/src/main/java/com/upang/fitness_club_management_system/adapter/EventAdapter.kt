package com.upang.fitness_club_management_system.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.upang.fitness_club_management_system.R
import com.upang.fitness_club_management_system.model.Event
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventAdapter(eventList: List<Event>) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {
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

    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val traineeName: TextView = view.findViewById(R.id.trineeName)
        val assignmentDate: TextView = view.findViewById(R.id.assignmentDate)
        val eventTime: TextView = view.findViewById(R.id.eventTime)
        val status: TextView = view.findViewById(R.id.status)
        val noSched: LinearLayout = view.findViewById(R.id.rowNoSchedule)
        val traineeNameRow: LinearLayout = view.findViewById(R.id.nameRow)
        val assignmentDateRow: LinearLayout = view.findViewById(R.id.dateRow)
        val eventTimeRow: LinearLayout = view.findViewById(R.id.timeRow)
        val statusRow: LinearLayout = view.findViewById(R.id.statusRow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventAdapter.EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventAdapter.EventViewHolder, position: Int) {
        if (filteredEvents.isEmpty()) {
            holder.noSched.visibility = View.VISIBLE
            holder.traineeNameRow.visibility = View.GONE
            holder.assignmentDateRow.visibility = View.GONE
            holder.eventTimeRow.visibility = View.GONE
            holder.statusRow.visibility = View.GONE
        } else {
            val event = filteredEvents.first() // Get only the first event

            holder.noSched.visibility = View.GONE
            holder.traineeNameRow.visibility = View.VISIBLE
            holder.assignmentDateRow.visibility = View.VISIBLE
            holder.eventTimeRow.visibility = View.VISIBLE
            holder.statusRow.visibility = View.VISIBLE

            holder.traineeName.text = event.user_name

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
        return if (filteredEvents.isNotEmpty()) 1 else 1
    }
}
