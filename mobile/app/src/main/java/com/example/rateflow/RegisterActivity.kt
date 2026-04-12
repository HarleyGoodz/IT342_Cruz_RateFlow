package com.example.rateflow

import android.content.Intent
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
import com.example.rateflow.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvSignIn: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.register)

        etUsername = findViewById(R.id.etUsername)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvSignIn = findViewById(R.id.tvSignIn)

        disableEnterKey(etUsername)
        disableEnterKey(etEmail)
        disableEnterKey(etPassword)
        disableEnterKey(etConfirmPassword)

        val filters = arrayOf(emojiFilter)
        etUsername.filters = filters
        etEmail.filters = filters
        etPassword.filters = filters
        etConfirmPassword.filters = filters

        btnRegister.setOnClickListener {
            registerUser()
        }

        tvSignIn.setOnClickListener {
            finish()
        }
    }

    private fun registerUser() {

        val username = etUsername.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword =
            etConfirmPassword.text.toString().trim()

        if (
            username.isEmpty() ||
            email.isEmpty() ||
            password.isEmpty() ||
            confirmPassword.isEmpty()
        ) {
            Toast.makeText(
                this,
                "Please fill in all fields",
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

        if (password.length < 6) {
            Toast.makeText(
                this,
                "Password must be at least 6 characters",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(
                this,
                "Passwords do not match",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val request =
            RegisterRequest(
                username,
                email,
                password
            )

        RetrofitClient.instance
            .registerUser(request)
            .enqueue(object : Callback<RegisterResponse> {

                override fun onResponse(
                    call: Call<RegisterResponse>,
                    response: Response<RegisterResponse>
                ) {

                    if (response.isSuccessful) {

                        Toast.makeText(
                            this@RegisterActivity,
                            "Registration successful",
                            Toast.LENGTH_SHORT
                        ).show()

                        finish()
                    }
                    else {

                        Toast.makeText(
                            this@RegisterActivity,
                            "Registration failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(
                    call: Call<RegisterResponse>,
                    t: Throwable
                ) {

                    Toast.makeText(
                        this@RegisterActivity,
                        "Cannot connect to server",
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