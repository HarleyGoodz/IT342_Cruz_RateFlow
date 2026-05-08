package com.example.rateflow.notifications

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rateflow.R
import com.example.rateflow.core.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminNotificationsActivity : AppCompatActivity() {

    private lateinit var recyclerAdminNotifications: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var layoutEmpty: View
    private lateinit var btnBack: View
    private lateinit var btnClearAll: View
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
            deleteAdminNotification(notification.id, position)
        }
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener { finish() }
        btnClearAll.setOnClickListener { clearAllAdminNotifications() }
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
                    Toast.makeText(this@AdminNotificationsActivity,
                        "Admin notification deleted", Toast.LENGTH_SHORT).show()
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
}