package com.upang.fitness_club_management_system.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.upang.fitness_club_management_system.R
import com.upang.fitness_club_management_system.model.Product
import com.upang.fitness_club_management_system.api.RetrofitClient

class ShopAdapter(private val productList: List<Product>, private val onItemClick: (Product) -> Unit) : RecyclerView.Adapter<ShopAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productName: TextView = itemView.findViewById(R.id.productName)
        private val productImage: ImageView = itemView.findViewById(R.id.productImage)
        private val productPrice: TextView = itemView.findViewById(R.id.productPrice)


        fun bind(product: Product) {
            productName.text = product.product_name
            productPrice.text = "₱${product.price.toDouble().toInt()}"

            // Construct full image URL using RetrofitClient
            val imageUrl = RetrofitClient.getBaseImageUrl()+ "storage/products/" + product.product_image

            // Load image using Glide
            Glide.with(itemView.context)
                .load(imageUrl)
                .into(productImage)

            // Handle click event
            itemView.setOnClickListener {
                onItemClick(product)
            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_shop, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(productList[position])
    }

    override fun getItemCount(): Int = productList.size
}
