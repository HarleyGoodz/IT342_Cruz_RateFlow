package com.example.rateflow.notifications

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rateflow.R
import com.example.rateflow.core.RetrofitClient
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminNotificationsActivity : AppCompatActivity() {

    private lateinit var recyclerAdminNotifications: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var layoutEmpty: View
    private lateinit var btnBack: View
    private lateinit var btnClearAll: TextView
    private lateinit var adminNotificationAdapter: AdminNotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_notifications)

        initViews()
        setupRecyclerView()
        setupClickListeners()
        loadAdminNotifications()
    }

    private fun initViews() {
        recyclerAdminNotifications = findViewById(R.id.recyclerAdminNotifications)
        progressBar = findViewById(R.id.progressBar)
        layoutEmpty = findViewById(R.id.layoutEmpty)
        btnBack = findViewById(R.id.btnBack)
        btnClearAll = findViewById(R.id.btnClearAll)
    }

    private fun setupRecyclerView() {
        adminNotificationAdapter = AdminNotificationAdapter()
        recyclerAdminNotifications.layoutManager = LinearLayoutManager(this)
        recyclerAdminNotifications.adapter = adminNotificationAdapter

        adminNotificationAdapter.onAdminNotificationDeleteListener = { notification, position ->
            showDeleteConfirmationDialog(notification, position)
        }
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener { finish() }
        btnClearAll.setOnClickListener {
            if (adminNotificationAdapter.itemCount > 0) {
                showClearAllConfirmationDialog()
            } else {
                Toast.makeText(this, "No notifications to clear", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDeleteConfirmationDialog(notification: AdminNotification, position: Int) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_delete_admin_notification, null)

        // Get views
        val tvPreviewTitle = dialogView.findViewById<TextView>(R.id.tvPreviewTitle)
        val tvPreviewMessage = dialogView.findViewById<TextView>(R.id.tvPreviewMessage)
        val tvPreviewTime = dialogView.findViewById<TextView>(R.id.tvPreviewTime)
        val btnCancel = dialogView.findViewById<MaterialButton>(R.id.btnCancelDelete)
        val btnConfirm = dialogView.findViewById<MaterialButton>(R.id.btnConfirmDelete)

        // Set notification preview data
        tvPreviewTitle.text = notification.title
        tvPreviewMessage.text = notification.message
        tvPreviewTime.text = formatTimestamp(notification.createdAt)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.show()

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            dialog.dismiss()
            deleteAdminNotification(notification.id, position)
        }
    }

    private fun showClearAllConfirmationDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_clear_admin_notifications, null)

        // Get views
        val tvNotificationCount = dialogView.findViewById<TextView>(R.id.tvNotificationCount)
        val btnCancel = dialogView.findViewById<MaterialButton>(R.id.btnCancelClear)
        val btnConfirm = dialogView.findViewById<MaterialButton>(R.id.btnConfirmClear)

        // Set notification count
        val count = adminNotificationAdapter.itemCount
        tvNotificationCount.text = "$count notification${if (count > 1) "s" else ""} will be deleted"

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            dialog.dismiss()
            clearAllAdminNotifications()
        }
    }

    private fun showDeleteSuccessDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_delete_success_simple, null)
        val btnOK = dialogView.findViewById<MaterialButton>(R.id.btnDialogOK)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        btnOK.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun loadAdminNotifications() {
        progressBar.visibility = View.VISIBLE

        RetrofitClient.adminNotificationApi.getAdminNotifications().enqueue(object : Callback<List<AdminNotification>> {
            override fun onResponse(call: Call<List<AdminNotification>>, response: Response<List<AdminNotification>>) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body() != null) {
                    val notifications = response.body()!!
                    adminNotificationAdapter.setAdminNotifications(notifications)

                    if (notifications.isEmpty()) {
                        layoutEmpty.visibility = View.VISIBLE
                        recyclerAdminNotifications.visibility = View.GONE
                        btnClearAll.visibility = View.GONE
                    } else {
                        layoutEmpty.visibility = View.GONE
                        recyclerAdminNotifications.visibility = View.VISIBLE
                        btnClearAll.visibility = View.VISIBLE
                    }
                } else {
                    Toast.makeText(this@AdminNotificationsActivity,
                        "Failed to load admin notifications", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<AdminNotification>>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@AdminNotificationsActivity,
                    "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deleteAdminNotification(notificationId: Int, position: Int) {
        progressBar.visibility = View.VISIBLE

        RetrofitClient.adminNotificationApi.deleteAdminNotification(notificationId).enqueue(object : Callback<Map<String, Boolean>> {
            override fun onResponse(call: Call<Map<String, Boolean>>, response: Response<Map<String, Boolean>>) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body()?.get("success") == true) {
                    showDeleteSuccessDialog()
                    loadAdminNotifications() // Reload to refresh the list
                } else {
                    Toast.makeText(this@AdminNotificationsActivity,
                        "Failed to delete admin notification", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Map<String, Boolean>>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@AdminNotificationsActivity,
                    "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun clearAllAdminNotifications() {
        progressBar.visibility = View.VISIBLE

        RetrofitClient.adminNotificationApi.clearAllAdminNotifications().enqueue(object : Callback<Map<String, Boolean>> {
            override fun onResponse(call: Call<Map<String, Boolean>>, response: Response<Map<String, Boolean>>) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body()?.get("success") == true) {
                    Toast.makeText(this@AdminNotificationsActivity,
                        "All admin notifications cleared", Toast.LENGTH_SHORT).show()
                    loadAdminNotifications() // Reload to show empty state
                } else {
                    Toast.makeText(this@AdminNotificationsActivity,
                        "Failed to clear admin notifications", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Map<String, Boolean>>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@AdminNotificationsActivity,
                    "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun formatTimestamp(timestamp: String?): String {
        if (timestamp == null) return "Just now"

        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
            val notificationDate = sdf.parse(timestamp)
            val now = java.util.Date()

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
                    val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                    dateFormat.format(notificationDate)
                }
            }
        } catch (e: Exception) {
            timestamp
        }
    }
}