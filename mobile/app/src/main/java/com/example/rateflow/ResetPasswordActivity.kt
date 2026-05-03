package com.example.rateflow

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.rateflow.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnResetPassword: Button
    private lateinit var progressBar: ProgressBar

    private var resetToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        // Get token from intent
        resetToken = intent.getStringExtra("TOKEN")

        if (resetToken.isNullOrEmpty()) {
            showErrorAndFinish("Invalid reset link. Please request a new password reset.")
            return
        }

        initializeViews()
        setupClickListeners()

        // Validate token before showing form
        validateToken()
    }

    private fun initializeViews() {
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnResetPassword = findViewById(R.id.btnResetPassword)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupClickListeners() {
        btnResetPassword.setOnClickListener {
            resetPassword()
        }
    }

    private fun validateToken() {
        progressBar.visibility = View.VISIBLE
        btnResetPassword.isEnabled = false

        RetrofitClient.instance.validateResetToken(resetToken!!).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!
                    val valid = result["valid"] as? Boolean ?: false

                    if (valid) {
                        btnResetPassword.isEnabled = true
                        Toast.makeText(this@ResetPasswordActivity, "Token verified. You can now reset your password.", Toast.LENGTH_SHORT).show()
                    } else {
                        val message = result["message"] as? String ?: "Invalid token"
                        showErrorAndFinish(message)
                    }
                } else {
                    showErrorAndFinish("Invalid or expired reset link. Please request a new one.")
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                progressBar.visibility = View.GONE
                showErrorAndFinish("Network error: ${t.message}")
            }
        })
    }

    private fun resetPassword() {
        val newPassword = etNewPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        if (newPassword.isEmpty()) {
            Toast.makeText(this, "Please enter a new password", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        btnResetPassword.isEnabled = false

        val request = mapOf(
            "token" to resetToken!!,
            "newPassword" to newPassword
        )

        RetrofitClient.instance.resetPassword(request).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                progressBar.visibility = View.GONE
                btnResetPassword.isEnabled = true

                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!
                    val success = result["success"] as? Boolean ?: false
                    val message = result["message"] as? String ?: ""

                    if (success) {
                        AlertDialog.Builder(this@ResetPasswordActivity)
                            .setTitle("Password Reset Successful")
                            .setMessage("$message\n\nYou can now login with your new password.")
                            .setPositiveButton("Go to Login") { _, _ ->
                                finish() // This will close the activity and return to login
                            }
                            .setCancelable(false)
                            .show()
                    } else {
                        Toast.makeText(this@ResetPasswordActivity, message, Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this@ResetPasswordActivity, "Failed to reset password. Please try again.", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                progressBar.visibility = View.GONE
                btnResetPassword.isEnabled = true
                Toast.makeText(this@ResetPasswordActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun showErrorAndFinish(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK") { _, _ ->
                finish() // Go back to login
            }
            .setCancelable(false)
            .show()
    }
}