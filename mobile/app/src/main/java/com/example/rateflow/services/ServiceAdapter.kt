package com.example.rateflow.services

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rateflow.R
import com.example.rateflow.core.RetrofitClient

class ServiceAdapter(
    private var services: List<Service>,
    private val onEditClick: ((Service) -> Unit)? = null,
    private val onDeleteClick: ((Service) -> Unit)? = null,
    private val onCardClick: ((Service) -> Unit)? = null
) : RecyclerView.Adapter<ServiceAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardService: View = view
        val ivServiceImage: ImageView = view.findViewById(R.id.ivServiceImage)
        val tvServiceName: TextView = view.findViewById(R.id.tvServiceName)
        val tvServiceCategory: TextView = view.findViewById(R.id.tvServiceCategory)
        val tvServiceDescription: TextView = view.findViewById(R.id.tvServiceDescription)
        val tvCreatedBy: TextView = view.findViewById(R.id.tvCreatedBy)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val service = services[position]

        holder.tvServiceName.text = service.serviceName
        holder.tvServiceCategory.text = service.serviceCategory
        holder.tvServiceDescription.text = service.serviceDescription
        holder.tvCreatedBy.text = "Created by: ${service.createdBy}"

        // Load image
        val imageUrl = "${RetrofitClient.getBaseUrl()}api/services/${service.serviceId}/image"
        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(R.drawable.ic_service_placeholder)
            .error(R.drawable.ic_service_placeholder)
            .centerCrop()
            .into(holder.ivServiceImage)

        holder.btnEdit.setOnClickListener {
            onEditClick?.invoke(service)
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick?.invoke(service)
        }

        holder.cardService.setOnClickListener {
            onCardClick?.invoke(service)
        }
    }

    override fun getItemCount() = services.size

    fun updateServices(newServices: List<Service>) {
        services = newServices
        notifyDataSetChanged()
    }
}