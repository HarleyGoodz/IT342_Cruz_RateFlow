package com.example.rateflow.notifications

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rateflow.R
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class AdminNotificationAdapter : RecyclerView.Adapter<AdminNotificationAdapter.AdminNotificationViewHolder>() {

    private var adminNotifications = mutableListOf<AdminNotification>()
    var onAdminNotificationDeleteListener: ((AdminNotification, Int) -> Unit)? = null

    fun setAdminNotifications(notifications: List<AdminNotification>) {
        this.adminNotifications = notifications.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminNotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_notification, parent, false)
        return AdminNotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdminNotificationViewHolder, position: Int) {
        val notification = adminNotifications[position]

        holder.tvTitle.text = notification.title
        holder.tvMessage.text = notification.message

        // Format timestamp
        val timestamp = formatTimestamp(notification.createdAt)
        holder.tvTimestamp.text = timestamp

        // Set icon based on notification type
        setNotificationIcon(holder.ivIcon, notification.type)

        // Handle delete button click
        holder.btnDelete.setOnClickListener {
            onAdminNotificationDeleteListener?.invoke(notification, position)
        }
    }

    private fun setNotificationIcon(icon: ImageView, type: String?) {
        when (type?.lowercase(Locale.getDefault())) {
            "user_registration" -> icon.setImageResource(R.drawable.ic_profile_default)
            "service_created" -> icon.setImageResource(R.drawable.ic_notifications)
            "service_updated" -> icon.setImageResource(R.drawable.ic_edit)
            "service_deleted" -> icon.setImageResource(R.drawable.ic_delete)
            else -> icon.setImageResource(R.drawable.ic_notifications)
        }
    }

    private fun formatTimestamp(timestamp: String?): String {
        if (timestamp == null) return "Just now"

        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val notificationDate = sdf.parse(timestamp)
            val now = Date()

            if (notificationDate == null) return timestamp

            val diff = now.time - notificationDate.time
            val diffMinutes = diff / (60 * 1000)
            val diffHours = diff / (60 * 60 * 1000)
            val diffDays = diff / (24 * 60 * 60 * 1000)

            when {
                diffMinutes < 1 -> "Just now"
                diffMinutes < 60 -> "$diffMinutes minute${if (diffMinutes > 1) "s" else ""} ago"
                diffHours < 24 -> "$diffHours hour${if (diffHours > 1) "s" else ""} ago"
                diffDays < 7 -> "$diffDays day${if (diffDays > 1) "s" else ""} ago"
                else -> {
                    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    dateFormat.format(notificationDate)
                }
            }
        } catch (e: ParseException) {
            timestamp
        }
    }

    override fun getItemCount(): Int = adminNotifications.size

    inner class AdminNotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivIcon: ImageView = itemView.findViewById(R.id.ivIcon)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }
}