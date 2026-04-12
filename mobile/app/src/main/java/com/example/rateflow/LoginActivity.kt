package com.example.rateflow

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputFilter
import android.util.Patterns
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.rateflow.network.LoginRequest
import com.example.rateflow.network.LoginResponse
import com.example.rateflow.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvSignUp: TextView
    private lateinit var tvForgotPassword: TextView

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.login)

        sharedPreferences =
            getSharedPreferences("UserProfiles", Context.MODE_PRIVATE)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvSignUp = findViewById(R.id.tvSignUp)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)

        disableEnterKey(etEmail)
        disableEnterKey(etPassword)

        etEmail.filters = arrayOf(emojiFilter)
        etPassword.filters = arrayOf(emojiFilter)

        // LOGIN BUTTON
        btnLogin.setOnClickListener {
            loginUser()
        }

        // SIGN UP
        tvSignUp.setOnClickListener {

            val intent = Intent(
                this,
                RegisterActivity::class.java
            )

            startActivity(intent)
        }

        // FORGOT PASSWORD
        tvForgotPassword.setOnClickListener {

            Toast.makeText(
                this,
                "Forgot password feature coming soon",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun loginUser() {

        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {

            Toast.makeText(
                this,
                "Please enter email and password",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

            Toast.makeText(
                this,
                "Invalid email format",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val request = LoginRequest(
            email,
            password
        )

        RetrofitClient.instance
            .loginUser(request)
            .enqueue(object : Callback<LoginResponse> {

                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {

                    if (response.isSuccessful) {

                        Toast.makeText(
                            this@LoginActivity,
                            "Login successful",
                            Toast.LENGTH_SHORT
                        ).show()

                        val intent = Intent(this@LoginActivity, DashboardActivity::class.java)
                        startActivity(intent)
                        finish()

                    } else {

                        val errorBody =
                            response.errorBody()?.string()

                        Toast.makeText(
                            this@LoginActivity,
                            "Server error: $errorBody",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(
                    call: Call<LoginResponse>,
                    t: Throwable
                ) {

                    t.printStackTrace()

                    Toast.makeText(
                        this@LoginActivity,
                        "Error: " + t.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun disableEnterKey(editText: EditText) {

        editText.setOnEditorActionListener { _, actionId, _ ->

            if (
                actionId == EditorInfo.IME_ACTION_DONE ||
                actionId == EditorInfo.IME_ACTION_NEXT
            ) {
                return@setOnEditorActionListener true
            }

            false
        }
    }

    private val emojiFilter =
        InputFilter { source, start, end, _, _, _ ->

            for (i in start until end) {

                val type =
                    Character.getType(source[i])

                if (
                    type == Character.SURROGATE.toInt() ||
                    type == Character.OTHER_SYMBOL.toInt()
                ) {
                    return@InputFilter ""
                }
            }

            null
        }
}