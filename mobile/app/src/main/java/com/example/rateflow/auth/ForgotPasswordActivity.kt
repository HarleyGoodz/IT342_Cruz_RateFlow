package com.example.rateflow.auth

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.rateflow.R
import com.example.rateflow.core.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var btnSendResetLink: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvBackToLogin: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        etEmail = findViewById(R.id.etEmail)
        btnSendResetLink = findViewById(R.id.btnSendResetLink)
        progressBar = findViewById(R.id.progressBar)
        tvBackToLogin = findViewById(R.id.ivBack)
    }

    private fun setupClickListeners() {
        btnSendResetLink.setOnClickListener {
            sendResetLink()
        }

        tvBackToLogin.setOnClickListener {
            finish()
        }
    }

    private fun sendResetLink() {
        val email = etEmail.text.toString().trim()

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            return
        }

        // Show progress
        btnSendResetLink.isEnabled = false
        progressBar.visibility = View.VISIBLE

        val request = mapOf("email" to email)

        RetrofitClient.instance.forgotPassword(request).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                btnSendResetLink.isEnabled = true
                progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!
                    val success = result["success"] as? Boolean ?: false
                    val message = result["message"] as? String ?: ""

                    if (success) {
                        // Show success dialog
                        AlertDialog.Builder(this@ForgotPasswordActivity)
                            .setTitle("Reset Link Sent")
                            .setMessage("$message\n\nPlease check your email for the password reset link.")
                            .setPositiveButton("OK") { _, _ ->
                                finish() // Return to login screen
                            }
                            .setCancelable(false)
                            .show()
                    } else {
                        Toast.makeText(this@ForgotPasswordActivity, message, Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this@ForgotPasswordActivity, "Failed to send reset link. Please try again.", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                btnSendResetLink.isEnabled = true
                progressBar.visibility = View.GONE
                Toast.makeText(this@ForgotPasswordActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}