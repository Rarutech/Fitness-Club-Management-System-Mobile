package com.upang.fitness_club_management_system.adapter

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.upang.fitness_club_management_system.BookClassDetails
import com.upang.fitness_club_management_system.R
import com.upang.fitness_club_management_system.api.Api
import com.upang.fitness_club_management_system.model.Profile
import com.upang.fitness_club_management_system.api.RetrofitClient
import com.upang.fitness_club_management_system.model.TrainerReviewResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TrainersAdapter(
    private val context: Context,
    private val trainers: List<Profile>
) : RecyclerView.Adapter<TrainersAdapter.TrainerViewHolder>() {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("TrainerPrefs", Context.MODE_PRIVATE)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrainerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_trainers, parent, false)
        return TrainerViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrainerViewHolder, position: Int) {
        val trainer = trainers[position]
        holder.bind(trainer)
        holder.itemView.setOnClickListener {
            saveSelectedTrainerEmail(trainer.email)
            val intent = Intent(context, BookClassDetails::class.java)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = trainers.size

    private fun saveSelectedTrainerEmail(email: String) {
        val editor = sharedPreferences.edit()
        editor.putString("selected_trainer_email", email)
        editor.apply()
    }

    inner class TrainerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivTrainerProfile: ImageView = itemView.findViewById(R.id.ivTrainerProfile)
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvRole: TextView = itemView.findViewById(R.id.tvRole)
        private val tvRating: TextView = itemView.findViewById(R.id.tvRating)
        private val tvPriceRate: TextView = itemView.findViewById(R.id.tvPricePerPlan)
        fun bind(trainer: Profile) {
            tvName.text = trainer.fullname
            tvRole.text = trainer.role
            tvRating.text = "Rating: ${trainer.total_ratings}"
            tvPriceRate.text = "Stating rate: ₱300 - ₱800"

            val profilePictureUrl = RetrofitClient.getBaseImageUrl() +"storage/profiles/"+ trainer.profile_picture
            Glide.with(context).load(profilePictureUrl).into(ivTrainerProfile)
        }
    }
}
