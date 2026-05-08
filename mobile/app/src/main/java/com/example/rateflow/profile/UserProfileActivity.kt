package com.example.rateflow.profile

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.rateflow.R
import com.example.rateflow.auth.GoogleSignInHelper
import com.example.rateflow.auth.LoginActivity
import com.example.rateflow.auth.User
import com.example.rateflow.dashboard.UserDashboardActivity
import com.example.rateflow.core.RetrofitClient
import com.example.rateflow.ratings.MyRatingsActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserProfileActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "UserProfileActivity"
    }

    // Views
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationDrawer: LinearLayout
    private lateinit var btnMenu: TextView
    private lateinit var tvUsername: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvRole: TextView
    private lateinit var tvRoleBadge: TextView
    private lateinit var btnEditUsername: TextView
    private lateinit var btnLogout: Button
    private lateinit var drawerUserName: TextView
    private lateinit var drawerUserEmail: TextView

    // Session
    private lateinit var sharedPreferences: SharedPreferences
    private var currentUserEmail: String = ""
    private var currentUsername: String = ""
    private var currentUserRole: String = ""

    private lateinit var googleSignInHelper: GoogleSignInHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_profile)

        Log.d(TAG, "onCreate: User Profile started")

        initializeViews()
        setupClickListeners()
        loadUserSession()
        loadDrawerNavigation()
        setupRoleBadge()

        googleSignInHelper = GoogleSignInHelper(this)
    }

    private fun initializeViews() {
        try {
            drawerLayout = findViewById(R.id.drawerLayout)
            navigationDrawer = findViewById(R.id.navigationDrawer)
            btnMenu = findViewById(R.id.btnMenu)
            tvUsername = findViewById(R.id.tvUsername)
            tvEmail = findViewById(R.id.tvEmail)
            tvRole = findViewById(R.id.tvRole)
            tvRoleBadge = findViewById(R.id.tvRoleBadge)
            btnEditUsername = findViewById(R.id.btnEditUsername)
            btnLogout = findViewById(R.id.btnLogout)
            drawerUserName = findViewById(R.id.drawerUserName)
            drawerUserEmail = findViewById(R.id.drawerUserEmail)

            Log.d(TAG, "initializeViews: All views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "initializeViews: Error initializing views", e)
            throw e
        }
    }

    private fun setupClickListeners() {
        btnMenu.setOnClickListener {
            Log.d(TAG, "Back button clicked - finishing activity")
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        btnEditUsername.setOnClickListener {
            Log.d(TAG, "Edit username button clicked")
            showEditUsernameDialog()
        }

        btnLogout.setOnClickListener {
            Log.d(TAG, "Logout button clicked")
            showLogoutConfirmation()
        }
    }

    private fun setupRoleBadge() {
        when (currentUserRole.uppercase()) {
            "ADMIN" -> {
                tvRoleBadge.text = "ADMIN"
                tvRoleBadge.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark))
                tvRoleBadge.visibility = View.VISIBLE
            }
            "USER" -> {
                tvRoleBadge.visibility = View.GONE
            }
            else -> {
                tvRoleBadge.visibility = View.GONE
            }
        }
    }

    private fun loadUserSession() {
        sharedPreferences = getSharedPreferences("UserProfiles", Context.MODE_PRIVATE)

        // Get current logged-in user email
        currentUserEmail = sharedPreferences.getString("current_user", "") ?: ""

        // Get username
        currentUsername = if (currentUserEmail.isNotEmpty()) {
            sharedPreferences.getString("${currentUserEmail}_username", null) ?:
            sharedPreferences.getString("current_username", "User") ?: "User"
        } else {
            sharedPreferences.getString("current_username", "User") ?: "User"
        }

        // Get user role
        currentUserRole = sharedPreferences.getString("user_role", "USER") ?: "USER"

        Log.d(TAG, "loadUserSession: Email: '$currentUserEmail', Username: '$currentUsername', Role: '$currentUserRole'")

        // Display user info
        if (currentUserEmail.isEmpty()) {
            Log.w(TAG, "No user email found in session")
            tvUsername.text = "Guest User"
            tvEmail.text = "Not logged in"
            tvRole.text = "GUEST"
            btnEditUsername.isEnabled = false
            btnEditUsername.alpha = 0.5f
        } else {
            tvUsername.text = currentUsername
            tvEmail.text = currentUserEmail
            tvRole.text = currentUserRole.uppercase()
        }
    }

    private fun showEditUsernameDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_username, null)
        val etNewUsername = dialogView.findViewById<EditText>(R.id.etNewUsername)
        etNewUsername.setText(currentUsername)
        etNewUsername.selectAll()

        val dialog = AlertDialog.Builder(this)
            .setTitle("Edit Username")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newUsername = etNewUsername.text.toString().trim()
                if (newUsername.isNotEmpty() && newUsername != currentUsername) {
                    if (newUsername.length >= 3 && newUsername.length <= 20) {
                        updateUsername(newUsername)
                    } else {
                        Toast.makeText(this, "Username must be 3-20 characters", Toast.LENGTH_SHORT).show()
                    }
                } else if (newUsername.isEmpty()) {
                    Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun updateUsername(newUsername: String) {
        Log.d(TAG, "Updating username to: $newUsername")

        btnEditUsername.isEnabled = false
        btnEditUsername.text = "⏳"

        val request = UpdateProfileRequest(newUsername)

        RetrofitClient.instance.updateProfile(request).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                btnEditUsername.isEnabled = true
                btnEditUsername.text = "✏️"

                Log.d(TAG, "Response code: ${response.code()}")

                if (response.isSuccessful) {
                    val updatedUser = response.body()
                    if (updatedUser != null) {
                        Log.d(TAG, "Username updated successfully to: ${updatedUser.username}")

                        // Update SharedPreferences
                        val editor = sharedPreferences.edit()
                        editor.putString("${currentUserEmail}_username", updatedUser.username)
                        editor.putString("current_username", updatedUser.username)
                        editor.apply()

                        // Update UI
                        currentUsername = updatedUser.username ?: newUsername
                        tvUsername.text = currentUsername
                        drawerUserName.text = currentUsername

                        Toast.makeText(this@UserProfileActivity, "Username updated successfully!", Toast.LENGTH_SHORT).show()
                        sendUsernameUpdateBroadcast(currentUsername)
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Update failed: ${response.code()} - $errorBody")

                    val errorMessage = when (response.code()) {
                        401 -> "Session expired. Please login again."
                        400 -> "Invalid username format or already taken"
                        409 -> "Username already taken"
                        else -> "Failed to update username"
                    }
                    Toast.makeText(this@UserProfileActivity, errorMessage, Toast.LENGTH_SHORT).show()

                    if (response.code() == 401) {
                        performLogout()
                    }
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                btnEditUsername.isEnabled = true
                btnEditUsername.text = "✏️"
                Log.e(TAG, "Network error", t)
                Toast.makeText(this@UserProfileActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun sendUsernameUpdateBroadcast(newUsername: String) {
        val intent = Intent("USERNAME_UPDATED")
        intent.putExtra("new_username", newUsername)
        intent.putExtra("user_email", currentUserEmail)
        sendBroadcast(intent)
        Log.d(TAG, "sendUsernameUpdateBroadcast: Broadcast sent for username update")
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                performLogout()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun performLogout() {
        Log.d(TAG, "performLogout: Logging out user")

        // Show loading state
        btnLogout.isEnabled = false
        btnLogout.text = "Logging out..."
        googleSignInHelper.signOut()

        // Call backend logout to invalidate server session
        RetrofitClient.instance.logout().enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                Log.d(TAG, "Server logout successful: ${response.code()}")
                clearLocalSessionAndNavigate()
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e(TAG, "Server logout failed: ${t.message}")
                // Still logout locally even if server call fails
                clearLocalSessionAndNavigate()
            }
        })
    }

    private fun clearLocalSessionAndNavigate() {
        // Clear SharedPreferences
        val editor = sharedPreferences.edit()
        editor.clear()  // Clear all instead of just specific keys
        editor.apply()

        // Clear cookies from Retrofit client
        RetrofitClient.clearCookies()

        // Reset button state
        btnLogout.isEnabled = true
        btnLogout.text = "Logout"

        // Navigate to Login Activity
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
    }

    private fun loadDrawerNavigation() {
        drawerUserName.text = currentUsername
        drawerUserEmail.text = currentUserEmail

        val navServices = findViewById<TextView>(R.id.navServices)
        val navMyRatings = findViewById<TextView>(R.id.navMyRatings)


        navServices.setOnClickListener {
            Log.d(TAG, "Drawer - Services clicked")
            drawerLayout.closeDrawer(Gravity.START)
            val intent = Intent(this, UserDashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        navMyRatings.setOnClickListener {
            Log.d(TAG, "Drawer - My Ratings clicked")
            drawerLayout.closeDrawer(Gravity.START)
            val intent = Intent(this, MyRatingsActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserSession()
        setupRoleBadge()
    }
}
