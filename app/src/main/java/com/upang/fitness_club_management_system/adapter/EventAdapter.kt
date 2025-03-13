package com.upang.fitness_club_management_system.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.upang.fitness_club_management_system.R
import com.upang.fitness_club_management_system.model.Event

class EventAdapter(private val eventList: List<Event>) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {
    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val traineeName: TextView = view.findViewById(R.id.trineeName)
        val assignmentDate: TextView = view.findViewById(R.id.assignmentDate)
        val eventTime: TextView = view.findViewById(R.id.eventTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventAdapter.EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventAdapter.EventViewHolder, position: Int) {
        val event = eventList[position]
        holder.traineeName.text = "Trainer: ${event.trainer_name}"
        holder.assignmentDate.text = "Date: ${event.assignment_date}"
        holder.eventTime.text = "Time: ${event.start_time} - ${event.end_time}"
    }

    override fun getItemCount(): Int {
        return eventList.size
    }
}