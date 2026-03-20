package com.example.rateflow

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.profile_page)

        val btnLogout = findViewById<MaterialButton>(R.id.btnLogout)
        val btnBack = findViewById<ImageView>(R.id.btnBack)

        val txtProfileName = findViewById<TextView>(R.id.txtProfileName)
        val txtProfileEmail = findViewById<TextView>(R.id.txtProfileEmail)
        val userAvatarLarge = findViewById<TextView>(R.id.userAvatarLarge)

        // 🔥 Fetch current logged-in user
        RetrofitClient.instance.getCurrentUser()
            .enqueue(object : Callback<User> {

                override fun onResponse(call: Call<User>, response: Response<User>) {

                    if (response.isSuccessful && response.body() != null) {

                        val user = response.body()!!

                        // Set username
                        txtProfileName.text = user.username

                        // Set email
                        txtProfileEmail.text = user.email

                        // Set avatar initials (first letter of username)
                        userAvatarLarge.text = user.username.first().uppercase()

                    } else {
                        Toast.makeText(
                            this@ProfileActivity,
                            "Session expired. Please login again.",
                            Toast.LENGTH_LONG
                        ).show()

                        startActivity(Intent(this@ProfileActivity, LoginActivity::class.java))
                        finish()
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    Toast.makeText(
                        this@ProfileActivity,
                        "Server error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })

        // 🔙 Back Button
        btnBack.setOnClickListener {
            finish()
        }

        btnLogout.setOnClickListener {

            RetrofitClient.instance.logout()
                .enqueue(object : Callback<String> {

                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        startActivity(Intent(this@ProfileActivity, LoginActivity::class.java))
                        finish()
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                        startActivity(Intent(this@ProfileActivity, LoginActivity::class.java))
                        finish()
                    }
                })
        }
    }
}