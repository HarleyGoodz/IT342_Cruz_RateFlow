package com.example.rateflow.auth

import android.os.Bundle
import android.text.InputFilter
import android.util.Patterns
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.rateflow.R
import com.example.rateflow.core.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var imgLogo: ImageView
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvSignIn: TextView
    private lateinit var btnTogglePassword: ImageButton
    private lateinit var btnToggleConfirmPassword: ImageButton

    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.register)

        etUsername = findViewById(R.id.etUsername)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvSignIn = findViewById(R.id.tvSignIn)
        imgLogo = findViewById(R.id.imgLogo)
        btnTogglePassword = findViewById(R.id.btnTogglePassword)
        btnToggleConfirmPassword = findViewById(R.id.btnToggleConfirmPassword)

        disableEnterKey(etUsername)
        disableEnterKey(etEmail)
        disableEnterKey(etPassword)
        disableEnterKey(etConfirmPassword)

        val filters = arrayOf(emojiFilter)
        etUsername.filters = filters
        etEmail.filters = filters
        etPassword.filters = filters
        etConfirmPassword.filters = filters

        setupPasswordToggles()

        btnRegister.setOnClickListener {
            registerUser()
        }

        tvSignIn.setOnClickListener {
            finish()
        }

        loadAnimatedLogo()
    }

    private fun loadAnimatedLogo() {
        // Load and animate the GIF using Glide
        Glide.with(this)
            .asGif()
            .load(R.drawable.starlogo) // Your GIF in drawable folder
            .into(imgLogo)
    }

    private fun setupPasswordToggles() {
        // Toggle for Password field
        btnTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible

            if (isPasswordVisible) {
                // Show password
                etPassword.inputType = android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                btnTogglePassword.setImageResource(R.drawable.ic_eye_open)
            } else {
                // Hide password
                etPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                btnTogglePassword.setImageResource(R.drawable.ic_eye_off)
            }
            // Move cursor to the end of text
            etPassword.setSelection(etPassword.text.length)
        }

        // Toggle for Confirm Password field
        btnToggleConfirmPassword.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible

            if (isConfirmPasswordVisible) {
                // Show confirm password
                etConfirmPassword.inputType = android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                btnToggleConfirmPassword.setImageResource(R.drawable.ic_eye_open)
            } else {
                // Hide confirm password
                etConfirmPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                btnToggleConfirmPassword.setImageResource(R.drawable.ic_eye_off)
            }
            // Move cursor to the end of text
            etConfirmPassword.setSelection(etConfirmPassword.text.length)
        }
    }

    private fun registerUser() {

        val username = etUsername.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

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

        val request = RegisterRequest(username, email, password)

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
                    } else {
                        // Handle specific error codes
                        when (response.code()) {
                            409 -> Toast.makeText(
                                this@RegisterActivity,
                                "Email or username already exists",
                                Toast.LENGTH_SHORT
                            ).show()
                            400 -> Toast.makeText(
                                this@RegisterActivity,
                                "Invalid input data",
                                Toast.LENGTH_SHORT
                            ).show()
                            else -> Toast.makeText(
                                this@RegisterActivity,
                                "Registration failed: ${response.code()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                override fun onFailure(
                    call: Call<RegisterResponse>,
                    t: Throwable
                ) {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Cannot connect to server: ${t.message}",
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

    private val emojiFilter = InputFilter { source, start, end, _, _, _ ->
        for (i in start until end) {
            val type = Character.getType(source[i])
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