package com.example.rateflow.notifications

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rateflow.R
import com.example.rateflow.core.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class UserNotificationActivity : AppCompatActivity() {

    private lateinit var recyclerNotifications: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var btnBack: ImageButton
    private lateinit var btnClearAll: TextView

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var notificationAdapter: NotificationAdapter
    private var notificationsList = mutableListOf<Notification>()

    private var currentUserEmail: String = ""
    private var currentUserId: Int = -1

    companion object {
        private const val TAG = "UserNotificationActivity"
        private const val PREFS_NAME = "UserProfiles"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.usernotification)

        initializeViews()
        setupRecyclerView()
        setupClickListeners()
        loadUserSession()
        loadNotificationsFromServer()
    }

    private fun initializeViews() {
        recyclerNotifications = findViewById(R.id.recyclerNotifications)
        progressBar = findViewById(R.id.progressBar)
        layoutEmpty = findViewById(R.id.layoutEmpty)
        btnBack = findViewById(R.id.btnBack)
        btnClearAll = findViewById(R.id.btnClearAll)
    }

    private fun setupRecyclerView() {
        recyclerNotifications.layoutManager = LinearLayoutManager(this)
        notificationAdapter = NotificationAdapter(notificationsList) { notification ->
            showDeleteConfirmation(notification)
        }
        recyclerNotifications.adapter = notificationAdapter
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        btnClearAll.setOnClickListener {
            if (notificationsList.isNotEmpty()) {
                showClearAllConfirmation()
            } else {
                Toast.makeText(this, "No notifications to clear", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadUserSession() {
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        currentUserEmail = sharedPreferences.getString("current_user", "") ?: ""
        currentUserId = sharedPreferences.getInt("current_user_id", -1)

        Log.d(TAG, "User session - Email: $currentUserEmail, ID: $currentUserId")

        if (currentUserEmail.isEmpty()) {
            Toast.makeText(this, "Please login to view notifications", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun loadNotificationsFromServer() {
        progressBar.visibility = View.VISIBLE
        layoutEmpty.visibility = View.GONE

        Log.d(TAG, "Fetching user notifications from ${RetrofitClient.getBaseUrl()}api/user-notifications")

        RetrofitClient.notificationApi.getUserNotifications().enqueue(object : Callback<List<Notification>> {
            override fun onResponse(call: Call<List<Notification>>, response: Response<List<Notification>>) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    response.body()?.let { notifications ->
                        Log.d(TAG, "Successfully fetched ${notifications.size} user notifications")

                        notificationsList.clear()
                        notificationsList.addAll(notifications)
                        notificationAdapter.updateNotifications(notificationsList)

                        if (notificationsList.isEmpty()) {
                            layoutEmpty.visibility = View.VISIBLE
                            Log.d(TAG, "No user notifications found")
                        } else {
                            layoutEmpty.visibility = View.GONE
                            Log.d(TAG, "Displaying ${notificationsList.size} user notifications")
                        }
                    } ?: run {
                        Log.e(TAG, "Response body is null")
                        layoutEmpty.visibility = View.VISIBLE
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e(TAG, "Failed to load notifications: ${response.code()} - $errorBody")
                    Toast.makeText(this@UserNotificationActivity, "Failed to load notifications", Toast.LENGTH_SHORT).show()
                    layoutEmpty.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<List<Notification>>, t: Throwable) {
                progressBar.visibility = View.GONE
                Log.e(TAG, "Network failure", t)
                Toast.makeText(this@UserNotificationActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                layoutEmpty.visibility = View.VISIBLE
            }
        })
    }

    private fun deleteNotification(notification: Notification) {
        progressBar.visibility = View.VISIBLE

        Log.d(TAG, "Deleting notification with ID: ${notification.id}")

        RetrofitClient.notificationApi.deleteUserNotification(notification.id).enqueue(object : Callback<Map<String, Boolean>> {
            override fun onResponse(call: Call<Map<String, Boolean>>, response: Response<Map<String, Boolean>>) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful() && response.body()?.get("success") == true) {
                    notificationsList.remove(notification)
                    notificationAdapter.updateNotifications(notificationsList)
                    Toast.makeText(this@UserNotificationActivity, "Notification deleted", Toast.LENGTH_SHORT).show()

                    if (notificationsList.isEmpty()) {
                        layoutEmpty.visibility = View.VISIBLE
                    }
                    Log.d(TAG, "Notification deleted successfully")
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e(TAG, "Failed to delete: ${response.code()} - $errorBody")
                    Toast.makeText(this@UserNotificationActivity, "Failed to delete notification", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Map<String, Boolean>>, t: Throwable) {
                progressBar.visibility = View.GONE
                Log.e(TAG, "Delete network failure", t)
                Toast.makeText(this@UserNotificationActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun clearAllNotifications() {
        progressBar.visibility = View.VISIBLE

        Log.d(TAG, "Clearing all user notifications")

        RetrofitClient.notificationApi.clearAllUserNotifications().enqueue(object : Callback<Map<String, Boolean>> {
            override fun onResponse(call: Call<Map<String, Boolean>>, response: Response<Map<String, Boolean>>) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful() && response.body()?.get("success") == true) {
                    notificationsList.clear()
                    notificationAdapter.updateNotifications(notificationsList)
                    layoutEmpty.visibility = View.VISIBLE
                    Toast.makeText(this@UserNotificationActivity, "All notifications cleared", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "All notifications cleared successfully")
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e(TAG, "Failed to clear all: ${response.code()} - $errorBody")
                    Toast.makeText(this@UserNotificationActivity, "Failed to clear notifications", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Map<String, Boolean>>, t: Throwable) {
                progressBar.visibility = View.GONE
                Log.e(TAG, "Clear all network failure", t)
                Toast.makeText(this@UserNotificationActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showDeleteConfirmation(notification: Notification) {
        AlertDialog.Builder(this)
            .setTitle("Delete Notification")
            .setMessage("Are you sure you want to delete this notification?")
            .setPositiveButton("Delete") { _, _ ->
                deleteNotification(notification)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showClearAllConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Clear All Notifications")
            .setMessage("Are you sure you want to clear all notifications? This action cannot be undone.")
            .setPositiveButton("Clear All") { _, _ ->
                clearAllNotifications()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun formatDateString(dateString: String): String {
        return try {
            val formats = listOf(
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss.SSS"
            )

            var date: Date? = null
            for (format in formats) {
                try {
                    val sdf = SimpleDateFormat(format, Locale.getDefault())
                    date = sdf.parse(dateString)
                    if (date != null) break
                } catch (e: ParseException) {
                    continue
                }
            }

            if (date != null) {
                val now = Date()
                val diff = now.time - date.time

                when {
                    diff < 60000 -> "Just now"
                    diff < 3600000 -> "${diff / 60000} minutes ago"
                    diff < 86400000 -> "${diff / 3600000} hours ago"
                    diff < 604800000 -> "${diff / 86400000} days ago"
                    else -> {
                        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        outputFormat.format(date)
                    }
                }
            } else {
                dateString
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error formatting date: $dateString", e)
            dateString
        }
    }

    // Adapter for Notifications
    inner class NotificationAdapter(
        private var notifications: List<Notification>,
        private val onDeleteClick: (Notification) -> Unit
    ) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.notification_user_item, parent, false)
            return NotificationViewHolder(view)
        }

        override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
            val notification = notifications[position]
            holder.bind(notification, onDeleteClick)
        }

        override fun getItemCount(): Int = notifications.size

        fun updateNotifications(newNotifications: List<Notification>) {
            notifications = newNotifications
            notifyDataSetChanged()
        }

        inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
            private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
            private val tvType: TextView = itemView.findViewById(R.id.tvType)
            private val tvDetails: TextView = itemView.findViewById(R.id.tvDetails)
            private val ivNotificationIcon: ImageView = itemView.findViewById(R.id.ivNotificationIcon)
            private val viewUnreadIndicator: View = itemView.findViewById(R.id.viewUnreadIndicator)
            private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

            fun bind(notification: Notification, onDeleteClick: (Notification) -> Unit) {
                tvMessage.text = notification.message
                tvTime.text = formatDateString(notification.createdAt)
                tvType.text = formatNotificationType(notification.type)

                val detailsText = buildString {
                    if (!notification.actorName.isNullOrEmpty()) {
                        append("By: ${notification.actorName}")
                        if (!notification.details.isNullOrEmpty()) {
                            append("\n")
                        }
                    }
                    if (!notification.details.isNullOrEmpty()) {
                        append(notification.details)
                    }
                }

                if (detailsText.isNotEmpty()) {
                    tvDetails.text = detailsText
                    tvDetails.visibility = View.VISIBLE
                } else {
                    tvDetails.visibility = View.GONE
                }

                setNotificationIcon(notification.type)

                // Hide unread indicator for now (you can implement read/unread later)
                viewUnreadIndicator.visibility = View.GONE

                btnDelete.setOnClickListener {
                    onDeleteClick(notification)
                }

                itemView.setOnClickListener {
                    if (tvDetails.visibility == View.GONE) {
                        tvDetails.visibility = View.VISIBLE
                    } else {
                        tvDetails.visibility = View.GONE
                    }
                }
            }

            private fun formatNotificationType(type: String): String {
                return when (type) {
                    "USERNAME_CHANGE" -> "Account Update"
                    "SERVICE_RATING" -> "New Rating"
                    "FEEDBACK_DELETED" -> "Feedback"
                    "ROLE_GRANTED" -> "Promotion"
                    "ROLE_DEMOTED" -> "Role Change"
                    else -> "Notification"
                }
            }

            private fun setNotificationIcon(type: String) {
                val iconRes = when (type) {
                    "USERNAME_CHANGE" -> android.R.drawable.ic_menu_edit
                    "SERVICE_RATING" -> android.R.drawable.btn_star_big_on
                    "FEEDBACK_DELETED" -> android.R.drawable.ic_menu_delete
                    "ROLE_GRANTED" -> android.R.drawable.ic_menu_manage
                    "ROLE_DEMOTED" -> android.R.drawable.ic_menu_revert
                    else -> android.R.drawable.ic_dialog_info
                }
                ivNotificationIcon.setImageResource(iconRes)
            }
        }
    }
}