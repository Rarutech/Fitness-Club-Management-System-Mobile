package com.upang.fitness_club_management_system.api

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.upang.fitness_club_management_system.R
import com.upang.fitness_club_management_system.model.TrainerReview

class ReviewsAdapter(private val reviews: List<TrainerReview>) :
    RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivProfile: ImageView = view.findViewById(R.id.ivProfile)
        val tvRating: TextView = view.findViewById(R.id.tvRating)
        val tvComment: TextView = view.findViewById(R.id.tvComment)
        val tvName: TextView = view.findViewById(R.id.tvName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reviews, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]

        // Set text values
        holder.tvRating.text = "⭐${review.rating}"
        holder.tvComment.text = review.comment
        holder.tvName.text = "- ${review.user_name}"

        // Load profile image (assumes a valid URL)
        val imageUrl = RetrofitClient.getBaseImageUrl() + "storage/profiles/" + review.user_picture
        Glide.with(holder.itemView.context).load(imageUrl).into(holder.ivProfile)
    }

    override fun getItemCount(): Int = reviews.size
}
