package com.example.rateflow

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import com.bumptech.glide.Glide
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.rateflow.model.User
import com.example.rateflow.network.GoogleLoginRequest
import com.example.rateflow.network.GoogleLoginResponse
import com.example.rateflow.network.LoginRequest
import com.example.rateflow.network.LoginResponse
import com.example.rateflow.network.RetrofitClient
import com.google.android.gms.common.SignInButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "LoginActivity"
        private const val RC_SIGN_IN = 1001
    }

    private lateinit var imgLogo: ImageView
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView
    private lateinit var btnGoogleSignIn: SignInButton
    private lateinit var googleSignInHelper: GoogleSignInHelper
    private lateinit var tvForgotPassword: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        initializeViews()
        setupClickListeners()
        loadAnimatedLogo()
        googleSignInHelper = GoogleSignInHelper(this)
        handleDeepLink(intent)
    }

    private fun initializeViews() {
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegister = findViewById(R.id.tvSignUp)
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)
        imgLogo = findViewById(R.id.imgLogo)
    }

    private fun loadAnimatedLogo() {
        // Load and animate the GIF using Glide
        Glide.with(this)
            .asGif()
            .load(R.drawable.starlogo) // Your GIF in drawable folder
            .into(imgLogo)
    }

    private fun setupClickListeners() {
        btnLogin.setOnClickListener {
            loginUser()
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        btnGoogleSignIn.setOnClickListener {
            startActivityForResult(googleSignInHelper.getSignInIntent(), RC_SIGN_IN)
        }
        tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        intent?.data?.let { uri ->
            // Check if this is a password reset deep link
            val token = uri.getQueryParameter("token")
            if (token != null && uri.path?.contains("reset-password") == true) {
                // Navigate to ResetPasswordActivity
                val resetIntent = Intent(this, ResetPasswordActivity::class.java)
                resetIntent.putExtra("TOKEN", token)
                startActivity(resetIntent)
            }
        }
    }

    private fun saveUserData(user: User) {
        val sharedPref = getSharedPreferences("UserProfiles", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        editor.putString("current_user", user.email)
        editor.putString("${user.email}_username", user.username)
        editor.putString("current_username", user.username)
        editor.putString("user_role", user.role ?: "USER")
        editor.putInt("user_id", user.id ?: 0)
        editor.apply()

        // Verify save
        val savedEmail = sharedPref.getString("current_user", "NOT_FOUND")
        val savedUsername = sharedPref.getString("current_username", "NOT_FOUND")
        Log.d(TAG, "Saved to SharedPreferences - Email: $savedEmail, Username: $savedUsername")
    }

    private fun showCreateAccountDialog(email: String, displayName: String) {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Account Not Found")
            .setMessage("The Google account ($email) is not registered.")
    }

    private fun navigateToDashboard(role: String) {
        val intent = if (role == "ADMIN") {
            Intent(this, AdminDashboardActivity::class.java)
        } else {
            Intent(this, UserDashboardActivity::class.java)
        }
        startActivity(intent)
        finish()
    }
    private fun authenticateWithBackend(idToken: String, email: String?, displayName: String) {
        // First, try to login with existing account
        val request = GoogleLoginRequest(idToken)

        RetrofitClient.instance.googleLogin(request).enqueue(object : Callback<GoogleLoginResponse> {
            override fun onResponse(call: Call<GoogleLoginResponse>, response: Response<GoogleLoginResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    // User exists in database - login successful
                    val userData = response.body()?.user
                    if (userData != null) {
                        saveUserData(userData)
                        Toast.makeText(this@LoginActivity, "Welcome back, ${userData.username}!", Toast.LENGTH_SHORT).show()
                        navigateToDashboard(userData.role ?: "USER")
                    }
                } else if (response.code() == 403) {
                    // User not registered - show dialog to create account
                    val errorBody = response.body()
                    val errorEmail = errorBody?.email ?: email ?: ""

                    showCreateAccountDialog(errorEmail, displayName)
                } else {
                    Toast.makeText(this@LoginActivity, "Authentication failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GoogleLoginResponse>, t: Throwable) {
                Log.e(TAG, "Google auth network error", t)
                Toast.makeText(this@LoginActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val result = googleSignInHelper.handleSignInResult(data)
            when (result) {
                is GoogleSignInHelper.GoogleSignInResult.Success -> {
                    val account = result.account
                    val idToken = account.idToken
                    val email = account.email
                    val displayName = account.displayName ?: email?.split("@")?.get(0) ?: "User"

                    if (idToken != null) {
                        authenticateWithBackend(idToken, email, displayName)
                    } else {
                        Toast.makeText(this, "Failed to get authentication token", Toast.LENGTH_SHORT).show()
                    }
                }
                is GoogleSignInHelper.GoogleSignInResult.Error -> {
                    Log.e(TAG, "Google Sign-In failed", result.exception)
                    Toast.makeText(this, "Google Sign-In failed: ${result.exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
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