package com.example.rateflow.dashboard

import android.content.Context
import com.bumptech.glide.Glide
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.content.BroadcastReceiver
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.drawerlayout.widget.DrawerLayout
import android.view.Gravity
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.rateflow.core.GridSpacingItemDecoration
import com.example.rateflow.ratings.MyRatingsActivity
import com.example.rateflow.R
import com.example.rateflow.ratings.RateServiceActivity
import com.example.rateflow.notifications.UserNotificationActivity
import com.example.rateflow.profile.UserProfileActivity
import com.example.rateflow.profile.UserServiceAdapter
import com.example.rateflow.notifications.Notification
import com.example.rateflow.services.Service
import com.example.rateflow.profile.UserService
import com.example.rateflow.core.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserDashboardActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "UserDashboard"
    }

    // Views
    private lateinit var imgLogo: ImageView
    private lateinit var tvWelcome: TextView
    private lateinit var btnMenu: TextView
    private lateinit var btnNotification: ImageButton
    private lateinit var btnProfile: ImageButton
    private lateinit var recyclerServices: RecyclerView
    private lateinit var tvServiceCount: TextView
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var etSearch: EditText
    private lateinit var btnFilter: TextView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationDrawer: LinearLayout
    private lateinit var drawerUserName: TextView
    private lateinit var drawerUserEmail: TextView
    private lateinit var badgeNotification: TextView

    // Adapter and Lists
    private lateinit var serviceAdapter: UserServiceAdapter
    private var allServicesList = mutableListOf<UserService>()
    private var filteredServicesList = mutableListOf<UserService>()
    private var notificationCount = 0
    private var selectedCategory: String = "All"

    // Session
    private lateinit var sharedPreferences: SharedPreferences
    private var currentUserEmail: String = ""
    private var currentUsername: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_dashboard)

        Log.d(TAG, "onCreate: User Dashboard started")

        initializeViews()
        loadAnimatedLogo()
        setupRecyclerView()
        setupClickListeners()
        setupSearchListener()
        loadUserSession()
        loadAllServices()
        loadNotificationCount()
    }

    private fun loadAnimatedLogo() {
        // Load and animate the GIF using Glide
        Glide.with(this)
            .asGif()
            .load(R.drawable.starlogo) // Your GIF in drawable folder
            .into(imgLogo)
    }

    private val usernameUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "USERNAME_UPDATED") {
                val newUsername = intent.getStringExtra("new_username") ?: return
                val userEmail = intent.getStringExtra("user_email") ?: return

                if (userEmail == currentUserEmail) {
                    currentUsername = newUsername
                    tvWelcome.text = "Welcome $currentUsername!"
                    drawerUserName.text = currentUsername

                    // Update shared preferences
                    val editor = sharedPreferences.edit()
                    editor.putString("current_username", newUsername)
                    editor.putString("${currentUserEmail}_username", newUsername)
                    editor.apply()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter("USERNAME_UPDATED")

        // For Android 14+ (API 34+), you need to specify the flag
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(usernameUpdateReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(usernameUpdateReceiver, filter)
        }

        // Refresh services
        Log.d(TAG, "onResume: Refreshing services list")
        loadAllServices()

        loadNotificationCount()

    }

    override fun onPause() {
        super.onPause()
        try {
            unregisterReceiver(usernameUpdateReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receiver", e)
        }
    }

    private fun initializeViews() {
        try {
            imgLogo = findViewById(R.id.imgLogo)
            tvWelcome = findViewById(R.id.tvWelcome)
            btnMenu = findViewById(R.id.btnMenu)
            btnNotification = findViewById(R.id.btnNotification)
            badgeNotification = findViewById(R.id.badgeNotification)
            btnProfile = findViewById(R.id.btnProfile)
            recyclerServices = findViewById(R.id.recyclerUserServices)
            tvServiceCount = findViewById(R.id.tvServiceCount)
            layoutEmpty = findViewById(R.id.layoutEmpty)
            progressBar = findViewById(R.id.progressBar)
            etSearch = findViewById(R.id.etSearch)
            btnFilter = findViewById(R.id.btnFilter)

            drawerLayout = findViewById(R.id.drawerLayout)
            navigationDrawer = findViewById(R.id.navigationDrawer)
            drawerUserName = findViewById(R.id.drawerUserName)
            drawerUserEmail = findViewById(R.id.drawerUserEmail)

            Log.d(TAG, "initializeViews: All views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "initializeViews: Error initializing views", e)
            throw e
        }
    }

    private fun updateNotificationBadge(count: Int) {
        notificationCount = count
        if (count > 0) {
            badgeNotification.text = if (count > 99) "99+" else count.toString()
            badgeNotification.visibility = View.VISIBLE
            Log.d(TAG, "updateNotificationBadge: Showing badge with count $count")
        } else {
            badgeNotification.visibility = View.GONE
            Log.d(TAG, "updateNotificationBadge: Hiding badge (no notifications)")
        }
    }

    private fun loadNotificationCount() {
        if (currentUserEmail.isEmpty()) {
            Log.d(TAG, "No user logged in, skipping notification count")
            updateNotificationBadge(0)
            return
        }

        Log.d(TAG, "Loading notification count from: http://localhost:8080/api/user-notifications")

        // Call the same endpoint as React - gets ALL notifications
        RetrofitClient.notificationApi.getUserNotifications().enqueue(object : Callback<List<Notification>> {
            override fun onResponse(call: Call<List<Notification>>, response: Response<List<Notification>>) {
                if (response.isSuccessful && response.body() != null) {
                    // Just like React: setNotificationCount(data.length)
                    val count = response.body()?.size ?: 0
                    updateNotificationBadge(count)
                    Log.d(TAG, "Notification count loaded: $count")

                    // Cache the count for offline use
                    sharedPreferences.edit()
                        .putInt("${currentUserEmail}_notification_count", count)
                        .apply()
                } else {
                    Log.e(TAG, "Failed to load notifications: ${response.code()}")
                    // Use cached count if available
                    val cachedCount = sharedPreferences.getInt("${currentUserEmail}_notification_count", 0)
                    updateNotificationBadge(cachedCount)
                }
            }

            override fun onFailure(call: Call<List<Notification>>, t: Throwable) {
                Log.e(TAG, "Network error loading notifications", t)
                // Use cached count if available
                val cachedCount = sharedPreferences.getInt("${currentUserEmail}_notification_count", 0)
                updateNotificationBadge(cachedCount)
            }
        })
    }

    private fun setupRecyclerView() {

        val gridLayoutManager = GridLayoutManager(this, 2)
        recyclerServices.layoutManager = gridLayoutManager


        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.grid_spacing)
        recyclerServices.addItemDecoration(GridSpacingItemDecoration(2, spacingInPixels, true))

        serviceAdapter = UserServiceAdapter(
            services = filteredServicesList,
            onViewDetailsClick = { service ->
                Log.d(TAG, "View details clicked for service: ${service.serviceName} (ID: ${service.serviceId})")
                viewServiceDetails(service)
            }
        )
        recyclerServices.adapter = serviceAdapter

        Log.d(TAG, "setupRecyclerView: RecyclerView configured with GridLayoutManager (2 columns)")
    }

    private fun setupClickListeners() {
        btnMenu.setOnClickListener {
            Log.d(TAG, "Menu button clicked - opening drawer")
            drawerLayout.openDrawer(Gravity.START)
        }

        btnNotification.setOnClickListener {
            Log.d(TAG, "Notification button clicked - Opening User Notifications")
            val intent = Intent(this, UserNotificationActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        btnProfile.setOnClickListener {
            Log.d(TAG, "Profile button clicked - Navigating to Profile Page")
            // ✅ Navigate to UserProfileActivity
            val intent = Intent(this, UserProfileActivity::class.java)
            startActivity(intent)
            // Optional: Add animation
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        btnFilter.setOnClickListener {
            Log.d(TAG, "Filter button clicked")
            showFilterDialog()
        }

        setupDrawerNavigation()
    }

    private fun setupDrawerNavigation() {
        val navServices = findViewById<TextView>(R.id.navServices)
        val navMyRatings = findViewById<TextView>(R.id.navMyRatings)

        navServices.setOnClickListener {
            Log.d(TAG, "Drawer - Services clicked")
            drawerLayout.closeDrawer(Gravity.START)
            // Already on services page, just refresh
            loadAllServices()
        }

        navMyRatings.setOnClickListener {
            Log.d(TAG, "Drawer - My Ratings clicked")
            drawerLayout.closeDrawer(Gravity.START)
            val intent = Intent(this, MyRatingsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showFilterDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_filter, null)

        // Get buttons from your custom layout
        val btnFoodHospitality = dialogView.findViewById<Button>(R.id.btnFoodHospitality)
        val btnMedicalHealth = dialogView.findViewById<Button>(R.id.btnMedicalHealth)
        val btnRetailCommercial = dialogView.findViewById<Button>(R.id.btnRetailCommercial)
        val btnPersonalLifestyle = dialogView.findViewById<Button>(R.id.btnPersonalLifestyle)
        val btnClearFilter = dialogView.findViewById<Button>(R.id.btnClearFilter)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

        // Track current selected category
        var tempSelectedCategory: String? = selectedCategory

        // Function to update button appearances (highlight selected)
        fun updateButtonSelection(category: String?) {
            // Reset all buttons background
            val defaultBg = ContextCompat.getDrawable(this, R.drawable.bg_filter_option)
            val selectedBg = ContextCompat.getDrawable(this, R.drawable.bg_filter_option_selected)

            btnFoodHospitality.background = defaultBg
            btnMedicalHealth.background = defaultBg
            btnRetailCommercial.background = defaultBg
            btnPersonalLifestyle.background = defaultBg

            // Highlight selected button
            when (category) {
                "Food & Hospitality" -> btnFoodHospitality.background = selectedBg
                "Medical & Health" -> btnMedicalHealth.background = selectedBg
                "Retail & Commercial" -> btnRetailCommercial.background = selectedBg
                "Personal & Lifestyle" -> btnPersonalLifestyle.background = selectedBg
            }
        }

        // Set initial selection
        updateButtonSelection(selectedCategory)

        // Button click listeners
        btnFoodHospitality.setOnClickListener {
            tempSelectedCategory = "Food & Hospitality"
            updateButtonSelection(tempSelectedCategory)
        }

        btnMedicalHealth.setOnClickListener {
            tempSelectedCategory = "Medical & Health"
            updateButtonSelection(tempSelectedCategory)
        }

        btnRetailCommercial.setOnClickListener {
            tempSelectedCategory = "Retail & Commercial"
            updateButtonSelection(tempSelectedCategory)
        }

        btnPersonalLifestyle.setOnClickListener {
            tempSelectedCategory = "Personal & Lifestyle"
            updateButtonSelection(tempSelectedCategory)
        }

        btnClearFilter.setOnClickListener {
            tempSelectedCategory = "All"
            updateButtonSelection("All")
        }

        // Create and show dialog
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Handle confirm - apply filter when any button is clicked (since no apply button)
        // Since your layout doesn't have an Apply button, we'll apply immediately
        // But if you want to keep the current behavior, we need to add listeners

        // Option 1: Apply filter immediately when a category is selected
        // Just set the value and dismiss

        // Option 2: Add an Apply button (recommended)
        // For now, let's use immediate application
        val applyFilter = {
            if (tempSelectedCategory != selectedCategory) {
                selectedCategory = tempSelectedCategory ?: "All"
                filterServices(etSearch.text.toString().trim(), selectedCategory)
                updateFilterButtonState()
            }
            dialog.dismiss()
        }

        // Apply filter when category is selected (immediate)
        btnFoodHospitality.setOnClickListener {
            tempSelectedCategory = "Food & Hospitality"
            updateButtonSelection(tempSelectedCategory)
            applyFilter()
        }

        btnMedicalHealth.setOnClickListener {
            tempSelectedCategory = "Medical & Health"
            updateButtonSelection(tempSelectedCategory)
            applyFilter()
        }

        btnRetailCommercial.setOnClickListener {
            tempSelectedCategory = "Retail & Commercial"
            updateButtonSelection(tempSelectedCategory)
            applyFilter()
        }

        btnPersonalLifestyle.setOnClickListener {
            tempSelectedCategory = "Personal & Lifestyle"
            updateButtonSelection(tempSelectedCategory)
            applyFilter()
        }

        btnClearFilter.setOnClickListener {
            tempSelectedCategory = "All"
            updateButtonSelection("All")
            applyFilter()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun updateFilterButtonState() {
        if (selectedCategory != "All") {
            btnFilter.text = "Filter: ${selectedCategory} ✓"
            btnFilter.setBackgroundResource(R.drawable.bg_filter_apply)
        } else {
            btnFilter.text = "Filter"
            btnFilter.setBackgroundResource(R.drawable.bg_filter_button)
        }
    }

    private fun setupSearchListener() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                Log.d(TAG, "Search query changed: '$query'")
                filterServices(query, null)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadUserSession() {
        sharedPreferences = getSharedPreferences("UserProfiles", Context.MODE_PRIVATE)

        // Get the current logged-in user email
        currentUserEmail = sharedPreferences.getString("current_user", "") ?: ""

        // Get username from multiple possible locations
        currentUsername = if (currentUserEmail.isNotEmpty()) {
            sharedPreferences.getString("${currentUserEmail}_username", null) ?:
            sharedPreferences.getString("current_username", "User") ?: "User"
        } else {
            sharedPreferences.getString("current_username", "User") ?: "User"
        }

        val userEmail = if (currentUserEmail.isNotEmpty()) currentUserEmail else "guest@example.com"

        Log.d(TAG, "loadUserSession: Email: '$currentUserEmail', Username: '$currentUsername'")

        if (currentUserEmail.isEmpty()) {
            Log.w(TAG, "No user email found in session, but showing all services anyway")
            tvWelcome.text = "Welcome Guest!"
            drawerUserName.text = "Guest User"
            drawerUserEmail.text = "Please login"
        } else {
            tvWelcome.text = "Welcome $currentUsername!"
            drawerUserName.text = currentUsername
            drawerUserEmail.text = currentUserEmail
        }
    }

    private fun loadAllServices() {
        progressBar.visibility = View.VISIBLE
        layoutEmpty.visibility = View.GONE

        Log.d(TAG, "loadAllServices: Fetching all services from server")

        RetrofitClient.serviceApi.getAllServices().enqueue(object : Callback<List<Service>> {
            override fun onResponse(call: Call<List<Service>>, response: Response<List<Service>>) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    response.body()?.let { services ->
                        Log.d(TAG, "loadAllServices: Successfully fetched ${services.size} total services")

                        allServicesList.clear()

                        // Convert Service to UserService and add ALL services
                        services.forEach { service ->
                            Log.d(TAG, "  Service: ${service.serviceName}, Created by: '${service.createdBy}'")
                            allServicesList.add(
                                UserService(
                                    serviceId = service.serviceId,
                                    imageUrl = service.imageUrl,
                                    serviceName = service.serviceName,
                                    serviceCategory = service.serviceCategory,
                                    serviceDescription = service.serviceDescription,
                                    createdBy = service.createdBy,
                                    createdAt = null
                                )
                            )
                        }

                        // Show all services without filtering by creator
                        filteredServicesList.clear()
                        filteredServicesList.addAll(allServicesList)
                        serviceAdapter.updateServices(filteredServicesList)
                        updateServiceCount(filteredServicesList.size)

                        if (allServicesList.isEmpty()) {
                            Log.d(TAG, "No services found at all")
                            layoutEmpty.visibility = View.VISIBLE
                        } else {
                            Log.d(TAG, "Displaying all ${allServicesList.size} services")
                            layoutEmpty.visibility = View.GONE
                        }
                    } ?: run {
                        Log.e(TAG, "Response body is null")
                        layoutEmpty.visibility = View.VISIBLE
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e(TAG, "Failed with code ${response.code()}. Error: $errorBody")
                    Toast.makeText(this@UserDashboardActivity, "Failed to load services", Toast.LENGTH_SHORT).show()
                    layoutEmpty.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<List<Service>>, t: Throwable) {
                progressBar.visibility = View.GONE
                Log.e(TAG, "Network failure", t)
                Toast.makeText(this@UserDashboardActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                layoutEmpty.visibility = View.VISIBLE
            }
        })
    }

    private fun filterServices(query: String, category: String?) {
        Log.d(TAG, "filterServices: Query='$query', Category='$category'")

        var filtered = allServicesList.toList()

        // Apply category filter
        if (category != null && category != "All") {
            filtered = filtered.filter { it.serviceCategory == category }
            Log.d(TAG, "After category filter: ${filtered.size} services")
        }

        // Apply search filter
        if (query.isNotEmpty()) {
            filtered = filtered.filter { service ->
                service.serviceName.contains(query, ignoreCase = true) ||
                        service.serviceCategory.contains(query, ignoreCase = true) ||
                        service.serviceDescription.contains(query, ignoreCase = true)
            }
            Log.d(TAG, "After search filter: ${filtered.size} services")
        }

        filteredServicesList.clear()
        filteredServicesList.addAll(filtered)
        serviceAdapter.updateServices(filteredServicesList)
        updateServiceCount(filteredServicesList.size)

        if (filteredServicesList.isEmpty() && allServicesList.isNotEmpty()) {
            layoutEmpty.visibility = View.VISIBLE
        } else {
            layoutEmpty.visibility = View.GONE
        }
    }

    private fun updateServiceCount(count: Int) {
        val message = when (count) {
            0 -> "No services available"
            1 -> "1 service available"
            else -> "$count services available"
        }
        tvServiceCount.text = message
        Log.d(TAG, "updateServiceCount: $message")
    }

    private fun viewServiceDetails(service: UserService) {
        val intent = Intent(this, RateServiceActivity::class.java)
        intent.putExtra("serviceId", service.serviceId)
        startActivity(intent)
    }
}