package com.example.rateflow

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.dashboard_page)

        val userAvatar = findViewById<TextView>(R.id.userAvatar)
        val txtUserName = findViewById<TextView>(R.id.txtUserName)

        userAvatar.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        // 🔥 Fetch logged-in user from backend
        RetrofitClient.instance.getCurrentUser()
            .enqueue(object : Callback<User> {

                override fun onResponse(call: Call<User>, response: Response<User>) {

                    if (response.isSuccessful && response.body() != null) {

                        val user = response.body()!!

                        // Set username in welcome card
                        txtUserName.text = user.username

                        // Set first letter in avatar
                        userAvatar.text = user.username.first().uppercase()

                        NotificationHelper.show(
                            this@DashboardActivity,
                            "Welcome back, ${user.username}!",
                            NotificationHelper.Type.SUCCESS
                        )

                    } else {
                        NotificationHelper.show(
                            this@DashboardActivity,
                            "Session expired. Please login again.",
                            NotificationHelper.Type.WARNING
                        )


                        startActivity(Intent(this@DashboardActivity, LoginActivity::class.java))
                        finish()
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    NotificationHelper.show(
                        this@DashboardActivity,
                        "Server error: ${t.message}",
                        NotificationHelper.Type.ERROR
                    )
                }
            })
    }
}