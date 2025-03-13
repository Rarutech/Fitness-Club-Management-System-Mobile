package com.upang.fitness_club_management_system.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.upang.fitness_club_management_system.R

class ImageAdapter(private val imageList: List<String>) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    private val BASE_URL = "https://glider-above-hopefully.ngrok-free.app/PumpingIronGym/Api/"

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageAdapter.ImageViewHolder, position: Int) {
        val imageUrl = BASE_URL + imageList[position]

        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .into(holder.imageView)
    }

    override fun getItemCount(): Int = imageList.size
}
