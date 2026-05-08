package com.example.rateflow.services

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rateflow.ratings.AdminViewRatingsActivity
import com.example.rateflow.R
import com.example.rateflow.core.RetrofitClient

class AdminServiceAdapter(
    private var services: List<ServiceWithStats>,
    private val context: Context
) : RecyclerView.Adapter<AdminServiceAdapter.ServiceViewHolder>() {

    companion object {
        private const val TAG = "AdminServiceAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.admin_service_item, parent, false)
        return ServiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val serviceWithStats = services[position]
        holder.bind(serviceWithStats)
    }

    override fun getItemCount(): Int = services.size

    fun updateServices(newServices: List<ServiceWithStats>) {
        services = newServices
        notifyDataSetChanged()
    }

    inner class ServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardService: CardView = itemView.findViewById(R.id.cardService)
        private val ivServiceImage: ImageView = itemView.findViewById(R.id.ivServiceImage)
        private val tvServiceName: TextView = itemView.findViewById(R.id.tvServiceName)
        private val tvServiceCategory: TextView = itemView.findViewById(R.id.tvServiceCategory)
        private val tvRatingValue: TextView = itemView.findViewById(R.id.tvRatingValue)
        private val ratingStarsContainer: LinearLayout = itemView.findViewById(R.id.ratingStarsContainer)
        private val tvRatingCount: TextView = itemView.findViewById(R.id.tvRatingCount)
        private val tvLatestFeedback: TextView = itemView.findViewById(R.id.tvLatestFeedback)

        fun bind(serviceWithStats: ServiceWithStats) {
            val service = serviceWithStats.service

            // Set service details
            tvServiceName.text = service.serviceName
            tvServiceCategory.text = service.serviceCategory

            // Set rating stats
            val averageRating = serviceWithStats.averageRating ?: 0.0
            val totalRatings = serviceWithStats.totalRatings ?: 0

            tvRatingValue.text = String.format("%.1f", averageRating)
            tvRatingCount.text = "($totalRatings)"

            // Display stars
            displayStars(averageRating, ratingStarsContainer)

            // Set latest feedback preview
            val latestFeedback = serviceWithStats.latestFeedback
            if (!latestFeedback.isNullOrEmpty()) {
                tvLatestFeedback.text = "Latest: \"${takeIf { latestFeedback.length > 40 }}\""
                tvLatestFeedback.visibility = View.VISIBLE
            } else {
                tvLatestFeedback.text = "No feedbacks yet"
                tvLatestFeedback.visibility = View.VISIBLE
            }

            // Load service image
            val imageUrl = "${RetrofitClient.getBaseUrl()}api/services/${service.serviceId}/image"
            Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_service_placeholder)
                .error(R.drawable.ic_service_placeholder)
                .into(ivServiceImage)

            // Set click listener for the entire card
            cardService.setOnClickListener {
                val intent = Intent(context, AdminViewRatingsActivity::class.java)
                intent.putExtra("serviceId", service.serviceId)
                intent.putExtra("serviceName", service.serviceName)
                intent.putExtra("serviceCategory", service.serviceCategory)
                context.startActivity(intent)
            }
        }

        private fun displayStars(rating: Double, container: LinearLayout) {
            container.removeAllViews()

            val fullStars = rating.toInt()
            val hasHalfStar = rating - fullStars >= 0.5

            for (i in 0 until 5) {
                val star = ImageView(context)
                val params = LinearLayout.LayoutParams(
                    dpToPx(16),
                    dpToPx(16)
                )
                params.setMargins(dpToPx(2), 0, dpToPx(2), 0)
                star.layoutParams = params
                star.scaleType = ImageView.ScaleType.FIT_CENTER

                val drawableRes = when {
                    i < fullStars -> R.drawable.ic_star_filled
                    i == fullStars && hasHalfStar -> R.drawable.ic_star_half
                    else -> R.drawable.ic_star_empty
                }

                star.setImageResource(drawableRes)
                container.addView(star)
            }
        }

        private fun dpToPx(dp: Int): Int {
            return (dp * context.resources.displayMetrics.density).toInt()
        }
    }
}