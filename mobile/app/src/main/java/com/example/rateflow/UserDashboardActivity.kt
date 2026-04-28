package com.example.rateflow

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rateflow.adapter.UserServiceAdapter
import com.example.rateflow.model.Service
import com.example.rateflow.model.UserService
import com.example.rateflow.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserDashboardActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "UserDashboard"
    }

    // Views
    private lateinit var tvWelcome: TextView
    private lateinit var btnMenu: TextView
    private lateinit var btnNotification: TextView
    private lateinit var btnProfile: TextView
    private lateinit var recyclerServices: RecyclerView
    private lateinit var tvServiceCount: TextView
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var etSearch: EditText
    private lateinit var btnFilter: TextView

    // Adapter and Lists
    private lateinit var serviceAdapter: UserServiceAdapter
    private var allServicesList = mutableListOf<UserService>()
    private var filteredServicesList = mutableListOf<UserService>()

    // Session
    private lateinit var sharedPreferences: SharedPreferences
    private var currentUserEmail: String = ""
    private var currentUsername: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_dashboard)

        Log.d(TAG, "onCreate: User Dashboard started")

        initializeViews()
        setupRecyclerView()
        setupClickListeners()
        setupSearchListener()
        loadUserSession()
        loadAllServices()
    }

    private fun initializeViews() {
        try {
            tvWelcome = findViewById(R.id.tvWelcome)
            btnMenu = findViewById(R.id.btnMenu)
            btnNotification = findViewById(R.id.btnNotification)
            btnProfile = findViewById(R.id.btnProfile)
            recyclerServices = findViewById(R.id.recyclerUserServices)
            tvServiceCount = findViewById(R.id.tvServiceCount)
            layoutEmpty = findViewById(R.id.layoutEmpty)
            progressBar = findViewById(R.id.progressBar)
            etSearch = findViewById(R.id.etSearch)
            btnFilter = findViewById(R.id.btnFilter)

            Log.d(TAG, "initializeViews: All views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "initializeViews: Error initializing views", e)
            throw e
        }
    }

    private fun setupRecyclerView() {
        // Use GridLayoutManager with 2 columns for 2x2 grid
        val gridLayoutManager = GridLayoutManager(this, 2)
        recyclerServices.layoutManager = gridLayoutManager

        // Optional: Add spacing between grid items
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
            Log.d(TAG, "Menu button clicked")
            Toast.makeText(this, "Menu clicked", Toast.LENGTH_SHORT).show()
        }

        btnNotification.setOnClickListener {
            Log.d(TAG, "Notification button clicked")
            Toast.makeText(this, "Notifications clicked", Toast.LENGTH_SHORT).show()
        }

        btnProfile.setOnClickListener {
            Log.d(TAG, "Profile button clicked")
            Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show()
        }

        btnFilter.setOnClickListener {
            Log.d(TAG, "Filter button clicked")
            showFilterDialog()
        }
    }

    private fun showFilterDialog() {
        // Simple filter dialog - can be expanded
        val options = arrayOf("All", "Food & Hospitality", "Medical & Health", "Retail & Commercial", "Personal & Lifestyle")
        AlertDialog.Builder(this)
            .setTitle("Filter by Category")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        Log.d(TAG, "Filter: Showing all services")
                        filterServices("", null)
                    }
                    1 -> {
                        Log.d(TAG, "Filter: Food & Hospitality")
                        filterServices("", "Food & Hospitality")
                    }
                    2 -> {
                        Log.d(TAG, "Filter: Medical & Health")
                        filterServices("", "Medical & Health")
                    }
                    3 -> {
                        Log.d(TAG, "Filter: Retail & Commercial")
                        filterServices("", "Retail & Commercial")
                    }
                    4 -> {
                        Log.d(TAG, "Filter: Personal & Lifestyle")
                        filterServices("", "Personal & Lifestyle")
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
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

        Log.d(TAG, "loadUserSession: Email: '$currentUserEmail', Username: '$currentUsername'")

        if (currentUserEmail.isEmpty()) {
            Log.w(TAG, "No user email found in session, but showing all services anyway")
            tvWelcome.text = "Welcome Guest!"
        } else {
            tvWelcome.text = "Welcome $currentUsername!"
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
        Log.d(TAG, "Viewing details for service: ${service.serviceName}")
        Toast.makeText(this, "Viewing: ${service.serviceName}\nCreated by: ${service.createdBy}", Toast.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: Refreshing services list")
        loadAllServices()
    }
}