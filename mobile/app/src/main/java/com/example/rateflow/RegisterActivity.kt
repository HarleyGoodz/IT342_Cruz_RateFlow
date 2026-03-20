package com.example.rateflow

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.button.MaterialButton
import android.widget.TextView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_page)

        val btnRegister = findViewById<MaterialButton>(R.id.btnRegister)
        val txtLogin = findViewById<TextView>(R.id.txtLogin)


        val editTextEmail = findViewById<TextInputEditText>(R.id.editTextEmail)
        val editTextUsername = findViewById<TextInputEditText>(R.id.editTextUsername)
        val editTextPassword = findViewById<TextInputEditText>(R.id.editTextPassword)
        val editTextConfirmPassword = findViewById<TextInputEditText>(R.id.editTextConfirmPassword)

        val emailInputLayout = findViewById<TextInputLayout>(R.id.emailInputLayout)
        val usernameInputLayout = findViewById<TextInputLayout>(R.id.usernameInputLayout)
        val passwordInputLayout = findViewById<TextInputLayout>(R.id.passwordInputLayout)
        val confirmPasswordInputLayout = findViewById<TextInputLayout>(R.id.confirmPasswordInputLayout)

        txtLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        btnRegister.setOnClickListener {

            val email = editTextEmail.text.toString().trim()
            val username = editTextUsername.text.toString().trim()
            val password = editTextPassword.text.toString().trim()
            val confirmPassword = editTextConfirmPassword.text.toString().trim()

            emailInputLayout.error = null
            usernameInputLayout.error = null
            passwordInputLayout.error = null
            confirmPasswordInputLayout.error = null

            when {

                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    emailInputLayout.error = "Invalid email address"
                }
                username.length < 4 -> {
                    usernameInputLayout.error = "Username must be at least 4 characters"
                }
                password.length < 8 -> {
                    passwordInputLayout.error = "Password must be at least 8 characters"
                }
                password != confirmPassword -> {
                    confirmPasswordInputLayout.error = "Passwords do not match"
                }
                else -> {

                    val user = User(
                        username = username,
                        email = email,
                        password = password
                    )

                    RetrofitClient.instance.registerUser(user)
                        .enqueue(object : Callback<User> {

                            override fun onResponse(call: Call<User>, response: Response<User>) {
                                if (response.isSuccessful) {
                                    Toast.makeText(
                                        this@RegisterActivity,
                                        "Registration successful!",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                                    finish()
                                } else {
                                    Toast.makeText(
                                        this@RegisterActivity,
                                        "Registration failed",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            override fun onFailure(call: Call<User>, t: Throwable) {
                                Toast.makeText(
                                    this@RegisterActivity,
                                    "Server error: ${t.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        })
                }
            }
        }
    }
}
