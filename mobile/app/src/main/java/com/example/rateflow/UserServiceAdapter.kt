package com.example.rateflow.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rateflow.R
import com.example.rateflow.model.UserService
import com.example.rateflow.network.RetrofitClient

class UserServiceAdapter(
    private var services: List<UserService>,
    private val onViewDetailsClick: (UserService) -> Unit
) : RecyclerView.Adapter<UserServiceAdapter.UserServiceViewHolder>() {

    companion object {
        private const val TAG = "UserServiceAdapter"
    }

    class UserServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivServiceImage: ImageView = itemView.findViewById(R.id.ivServiceImage)
        val tvServiceName: TextView = itemView.findViewById(R.id.tvServiceName)
        val tvServiceCategory: TextView = itemView.findViewById(R.id.tvServiceCategory)
        val tvServiceDescription: TextView = itemView.findViewById(R.id.tvServiceDescription)
        val btnViewDetails: Button = itemView.findViewById(R.id.btnViewDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserServiceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_service, parent, false)
        Log.d(TAG, "onCreateViewHolder: Created new view holder")
        return UserServiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserServiceViewHolder, position: Int) {
        val service = services[position]

        Log.d(TAG, "onBindViewHolder: Binding service at position $position - ${service.serviceName}")

        holder.tvServiceName.text = service.serviceName
        holder.tvServiceCategory.text = service.serviceCategory
        holder.tvServiceDescription.text = service.serviceDescription



        // Load image with error handling
        val imageUrl = "${RetrofitClient.getBaseUrl()}api/services/${service.serviceId}/image"
        Log.d(TAG, "onBindViewHolder: Loading image from URL: $imageUrl")

        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(R.drawable.ic_service_placeholder)
            .error(R.drawable.ic_service_placeholder)
            .timeout(10000)
            .centerCrop()
            .into(holder.ivServiceImage)

        holder.btnViewDetails.setOnClickListener {
            Log.d(TAG, "View details button clicked for service: ${service.serviceName} (ID: ${service.serviceId})")
            onViewDetailsClick(service)
        }
    }

    override fun getItemCount(): Int {
        val count = services.size
        Log.d(TAG, "getItemCount: Returning $count items")
        return count
    }

    fun updateServices(newServices: List<UserService>) {
        Log.d(TAG, "updateServices: Updating from ${services.size} to ${newServices.size} services")
        services = newServices
        notifyDataSetChanged()
    }
}