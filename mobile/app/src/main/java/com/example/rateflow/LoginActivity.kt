package com.example.rateflow

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.rateflow.network.LoginRequest
import com.example.rateflow.network.LoginResponse
import com.example.rateflow.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "LoginActivity"
    }

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegister = findViewById(R.id.tvSignUp)
    }

    private fun setupClickListeners() {
        btnLogin.setOnClickListener {
            loginUser()
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun loginUser() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }


        btnLogin.isEnabled = false

        val request = LoginRequest(email, password)

        RetrofitClient.instance.loginUser(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {

                btnLogin.isEnabled = true

                if (response.isSuccessful) {
                    val user = response.body()

                    if (user != null) {
                        Log.d(TAG, "Login successful - Email: ${user.email}, Username: ${user.username}, Role: ${user.role}")

                        // Save to SharedPreferences
                        val sharedPref = getSharedPreferences("UserProfiles", Context.MODE_PRIVATE)
                        val editor = sharedPref.edit()

                        // Save current logged-in user email (this is the key)
                        editor.putString("current_user", user.email ?: email)

                        // Save username with email as key (important for dashboard)
                        val username = user.username ?: email?.split("@")?.get(0) ?: "User"
                        editor.putString("${user.email}_username", username)

                        // Also save username in a generic field for easy access
                        editor.putString("current_username", username)

                        // Save user role
                        editor.putString("user_role", user.role ?: "USER")

                        // Save user ID
                        editor.putInt("user_id", user.id ?: 0)

                        editor.apply()

                        // Verify save
                        val savedEmail = sharedPref.getString("current_user", "NOT_FOUND")
                        val savedUsername = sharedPref.getString("current_username", "NOT_FOUND")
                        Log.d(TAG, "Saved to SharedPreferences - Email: $savedEmail, Username: $savedUsername")

                        Toast.makeText(this@LoginActivity, "Login successful! Welcome $username", Toast.LENGTH_SHORT).show()

                        // Navigate based on role
                        if (user.role == "ADMIN") {
                            Log.d(TAG, "Redirecting to Admin Dashboard")
                            startActivity(Intent(this@LoginActivity, AdminDashboardActivity::class.java))
                        } else {
                            Log.d(TAG, "Redirecting to User Dashboard")
                            startActivity(Intent(this@LoginActivity, UserDashboardActivity::class.java))
                        }
                        finish()
                    } else {
                        Log.e(TAG, "Login response body is null")
                        Toast.makeText(this@LoginActivity, "Login failed: Invalid response", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e(TAG, "Login failed - Code: ${response.code()}")
                    when (response.code()) {
                        401 -> Toast.makeText(this@LoginActivity, "Invalid email or password", Toast.LENGTH_SHORT).show()
                        404 -> Toast.makeText(this@LoginActivity, "User not found", Toast.LENGTH_SHORT).show()
                        else -> Toast.makeText(this@LoginActivity, "Login failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                btnLogin.isEnabled = true
                Log.e(TAG, "Login network error", t)
                Toast.makeText(this@LoginActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}