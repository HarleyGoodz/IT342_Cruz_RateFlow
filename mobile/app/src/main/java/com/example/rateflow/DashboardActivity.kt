package com.example.rateflow

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DashboardActivity : AppCompatActivity() {

    private lateinit var tvWelcome: TextView
    private lateinit var btnMenu: TextView
    private lateinit var btnNotification: TextView
    private lateinit var btnProfile: TextView
    private lateinit var btnViewAll: Button
    private lateinit var etSearch: EditText

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.dashboard)

        sharedPreferences =
            getSharedPreferences("UserProfiles", Context.MODE_PRIVATE)

        tvWelcome = findViewById(R.id.tvWelcome)
        btnMenu = findViewById(R.id.btnMenu)
        btnNotification = findViewById(R.id.btnNotification)
        btnProfile = findViewById(R.id.btnProfile)
        btnViewAll = findViewById(R.id.btnViewAll)
        etSearch = findViewById(R.id.etSearch)

        loadUser()

        btnMenu.setOnClickListener {
            Toast.makeText(
                this,
                "Menu clicked",
                Toast.LENGTH_SHORT
            ).show()
        }

        btnNotification.setOnClickListener {
            Toast.makeText(
                this,
                "Notifications clicked",
                Toast.LENGTH_SHORT
            ).show()
        }

        btnProfile.setOnClickListener {
            Toast.makeText(
                this,
                "Profile clicked",
                Toast.LENGTH_SHORT
            ).show()
        }

        btnViewAll.setOnClickListener {
            Toast.makeText(
                this,
                "View All clicked",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun loadUser() {

        val email =
            sharedPreferences.getString("current_user", null)

        if (email != null) {

            val username =
                sharedPreferences.getString(
                    "${email}_username",
                    "User"
                )

            tvWelcome.text = "Welcome $username!"
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()

        Toast.makeText(
            this,
            "Logout first to exit",
            Toast.LENGTH_SHORT
        ).show()
    }
}