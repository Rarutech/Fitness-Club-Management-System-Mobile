package com.upang.fitness_club_management_system.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.upang.fitness_club_management_system.R
import com.upang.fitness_club_management_system.model.Highlight


class HighlightAdapter(private val highlightList: List<Highlight>) :
    RecyclerView.Adapter<HighlightAdapter.HighlightViewHolder>() {

    class HighlightViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCaption: TextView = itemView.findViewById(R.id.tvCaption)
        val rvImages: RecyclerView = itemView.findViewById(R.id.rvImages)
        val tvName: TextView = itemView.findViewById(R.id.tvName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HighlightViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_highlight, parent, false)
        return HighlightViewHolder(view)
    }

    override fun onBindViewHolder(holder: HighlightViewHolder, position: Int) {
        val highlight = highlightList[position]
        holder.tvCaption.text = highlight.caption
        holder.tvName.text = highlight.name
        holder.rvImages.layoutManager = LinearLayoutManager(holder.itemView.context, LinearLayoutManager.HORIZONTAL, false)
        holder.rvImages.adapter = ImageAdapter(highlight.images)
    }

    override fun getItemCount(): Int = highlightList.size
}