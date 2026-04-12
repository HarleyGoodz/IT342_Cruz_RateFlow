package com.example.rateflow.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rateflow.R
import com.example.rateflow.model.Service

class ServiceAdapter(
    private val services: List<Service>
) : RecyclerView.Adapter<ServiceAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val tvName: TextView =
            view.findViewById(R.id.tvServiceName)

        val tvCategory: TextView =
            view.findViewById(R.id.tvServiceCategory)

        val tvDescription: TextView =
            view.findViewById(R.id.tvServiceDescription)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        val service = services[position]

        holder.tvName.text =
            service.serviceName

        holder.tvCategory.text =
            service.serviceCategory

        holder.tvDescription.text =
            service.serviceDescription
    }

    override fun getItemCount(): Int {
        return services.size
    }
}