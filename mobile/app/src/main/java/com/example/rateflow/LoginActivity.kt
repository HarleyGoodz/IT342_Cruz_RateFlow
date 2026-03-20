package com.example.rateflow

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import android.util.Patterns
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.login_page)

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val txtRegister = findViewById<TextView>(R.id.txtRegister)

        val editTextUsername = findViewById<TextInputEditText>(R.id.editTextUsername)
        val editTextPassword = findViewById<TextInputEditText>(R.id.editTextPassword)

        val usernameInputLayout = findViewById<TextInputLayout>(R.id.usernameInputLayout)
        val passwordInputLayout = findViewById<TextInputLayout>(R.id.passwordInputLayout)

        btnLogin.setOnClickListener {

            val email = editTextUsername.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            usernameInputLayout.error = null
            passwordInputLayout.error = null

            when {

                email.isEmpty() -> {
                    usernameInputLayout.error = "Email is required"
                }

                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    usernameInputLayout.error = "Enter a valid email address"
                }

                password.isEmpty() -> {
                    passwordInputLayout.error = "Password is required"
                }

                password.length < 8 -> {
                    passwordInputLayout.error = "Password must be at least 8 characters"
                }

                else -> {

                    val loginRequest = LoginRequest(
                        email = email,   // temporarily using username field to carry email
                        password = password
                    )

                    RetrofitClient.instance.loginUser(loginRequest)
                        .enqueue(object : Callback<User> {

                            override fun onResponse(call: Call<User>, response: Response<User>) {

                                if (response.isSuccessful) {

                                    Toast.makeText(
                                        this@LoginActivity,
                                        "Login successful!",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    val intent = Intent(this@LoginActivity, DashboardActivity::class.java)
                                    startActivity(intent)
                                    finish()

                                } else {
                                    Toast.makeText(
                                        this@LoginActivity,
                                        "Invalid email or password",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            override fun onFailure(call: Call<User>, t: Throwable) {

                                Toast.makeText(
                                    this@LoginActivity,
                                    "Server error: ${t.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        })
                }
            }
        }

        txtRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}