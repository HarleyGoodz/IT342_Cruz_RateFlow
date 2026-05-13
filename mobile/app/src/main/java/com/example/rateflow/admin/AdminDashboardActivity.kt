package com.example.rateflow.admin

import android.app.Dialog
import android.app.ProgressDialog
import com.bumptech.glide.Glide
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rateflow.services.AddServiceActivity
import com.example.rateflow.notifications.AdminNotificationsActivity
import com.example.rateflow.ratings.AdminViewRatingsActivity
import com.example.rateflow.services.EditServiceActivity
import com.example.rateflow.R
import com.example.rateflow.profile.UserProfileActivity
import com.example.rateflow.services.ServiceAdapter
import com.example.rateflow.services.Service
import com.example.rateflow.core.RetrofitClient
import com.example.rateflow.utils.CustomNotification
import com.example.rateflow.utils.NotificationType
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminDashboardActivity : AppCompatActivity() {

    // Toolbar buttons
    private lateinit var btnMenu: ImageButton
    private lateinit var btnAddService: ImageButton
    private lateinit var btnNotification: ImageButton
    private lateinit var btnProfile: ImageButton

    private lateinit var badgeAdminNotificationCount: TextView
    private val adminNotificationHandler = Handler(Looper.getMainLooper())
    private lateinit var adminNotificationRunnable: Runnable

    // Drawer layout
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: LinearLayout
    private lateinit var btnServices: LinearLayout
    private lateinit var btnAccessControls: LinearLayout

    // RecyclerView
    private lateinit var recyclerServices: RecyclerView
    private lateinit var serviceAdapter: ServiceAdapter
    private var servicesList = mutableListOf<Service>()
    private var filteredServicesList = mutableListOf<Service>()

    private lateinit var imgLogo: ImageView

    // Filter + Search
    private lateinit var btnFilter: TextView
    private lateinit var etSearch: EditText
    private lateinit var tvServiceCount: TextView

    // Filter Indicator Views
    private lateinit var layoutFilterIndicator: LinearLayout
    private lateinit var tvActiveFilter: TextView
    private lateinit var btnClearFilterIndicator: TextView

    // Filter variables
    private var currentCategoryFilter: String? = null
    private var currentSearchQuery: String = ""

    // Custom Notification
    private lateinit var customNotification: CustomNotification

    companion object {
        private const val EDIT_SERVICE_REQUEST = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        // Initialize custom notification
        customNotification = CustomNotification(this)

        initializeViews()
        loadAnimatedLogo()
        setupDrawer()
        setupRecyclerView()
        setupClickListeners()
        setupSearchListener()
        setupAdminNotificationBadge()
        loadServices()

        // Show welcome notification if coming from login
        showWelcomeNotificationIfNeeded()
    }

    private fun showWelcomeNotificationIfNeeded() {
        val shouldShow = intent.getBooleanExtra("show_welcome_notification", false)
        val username = intent.getStringExtra("username") ?: "Admin"

        if (shouldShow) {
            Handler(Looper.getMainLooper()).postDelayed({
                customNotification.show(
                    message = "Welcome $username!",
                    title = "Login Successful",
                    type = NotificationType.SUCCESS,
                    duration = 3000
                )
            }, 500)
        }
    }

    private fun loadAnimatedLogo() {
        Glide.with(this)
            .asGif()
            .load(R.drawable.starlogo)
            .into(imgLogo)
    }

    private fun initializeViews() {
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        btnServices = findViewById(R.id.btnServices)
        btnAccessControls = findViewById(R.id.btnAccessControls)
        imgLogo = findViewById(R.id.imgLogo)

        btnMenu = findViewById(R.id.btnMenu)
        btnAddService = findViewById(R.id.btnAddService)
        btnNotification = findViewById(R.id.btnNotification)
        btnProfile = findViewById(R.id.btnProfile)
        btnFilter = findViewById(R.id.btnFilter)
        etSearch = findViewById(R.id.etSearch)
        tvServiceCount = findViewById(R.id.tvServiceCount)
        recyclerServices = findViewById(R.id.recyclerServices)

        layoutFilterIndicator = findViewById(R.id.layoutFilterIndicator)
        tvActiveFilter = findViewById(R.id.tvActiveFilter)
        btnClearFilterIndicator = findViewById(R.id.btnClearFilterIndicator)

        btnClearFilterIndicator.setOnClickListener {
            clearAllFilters()
        }
    }

    private fun setupAdminNotificationBadge() {
        badgeAdminNotificationCount = findViewById(R.id.badgeAdminNotificationCount)

        val btnNotification = findViewById<ImageButton>(R.id.btnNotification)
        btnNotification.setOnClickListener {
            val intent = Intent(this, AdminNotificationsActivity::class.java)
            startActivity(intent)
        }

        startAdminBadgeUpdates()
    }

    private fun startAdminBadgeUpdates() {
        adminNotificationRunnable = Runnable {
            updateAdminNotificationBadge()
            adminNotificationHandler.postDelayed(adminNotificationRunnable, 30000)
        }
        adminNotificationHandler.post(adminNotificationRunnable)
    }

    private fun updateAdminNotificationBadge() {
        RetrofitClient.adminNotificationApi.getAdminUnreadCount().enqueue(object : retrofit2.Callback<Map<String, Long>> {
            override fun onResponse(call: retrofit2.Call<Map<String, Long>>, response: retrofit2.Response<Map<String, Long>>) {
                if (response.isSuccessful && response.body() != null) {
                    val count = response.body()?.get("count") ?: 0
                    if (count > 0) {
                        badgeAdminNotificationCount.visibility = View.VISIBLE
                        val countText = if (count > 99) "99+" else count.toString()
                        badgeAdminNotificationCount.text = countText
                    } else {
                        badgeAdminNotificationCount.visibility = View.GONE
                    }
                }
            }

            override fun onFailure(call: retrofit2.Call<Map<String, Long>>, t: Throwable) {
                // Handle error silently
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::adminNotificationRunnable.isInitialized) {
            adminNotificationHandler.removeCallbacks(adminNotificationRunnable)
        }
    }

    private fun setupDrawer() {
        btnServices.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            customNotification.show(
                message = "Services",
                title = "Navigation",
                type = NotificationType.INFO,
                duration = 1500
            )
        }

        btnAccessControls.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            val intent = Intent(this, AccessControlActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        val gridLayoutManager = GridLayoutManager(this, 2)
        recyclerServices.layoutManager = gridLayoutManager

        serviceAdapter = ServiceAdapter(
            services = filteredServicesList,
            onEditClick = { service ->
                val intent = Intent(this, EditServiceActivity::class.java)
                intent.putExtra("service", service)
                startActivityForResult(intent, EDIT_SERVICE_REQUEST)
            },
            onDeleteClick = { service ->
                showDeleteConfirmationDialog(service)
            },
            onCardClick = { service ->
                navigateToRatingsView(service)
            }
        )
        recyclerServices.adapter = serviceAdapter
    }

    private fun navigateToRatingsView(service: Service) {
        val intent = Intent(this, AdminViewRatingsActivity::class.java)
        intent.putExtra("serviceId", service.serviceId)
        intent.putExtra("serviceName", service.serviceName)
        intent.putExtra("serviceCategory", service.serviceCategory)
        startActivity(intent)
    }

    private fun showDeleteConfirmationDialog(service: Service) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_delete_confirmation)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val tvMessage = dialog.findViewById<TextView>(R.id.tvDeleteMessage)
        val btnConfirm = dialog.findViewById<Button>(R.id.btnConfirmDelete)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancelDelete)

        tvMessage.text = "Are you sure you want to delete \"${service.serviceName}\"? This will also delete all ratings associated with this service."

        btnConfirm.setOnClickListener {
            dialog.dismiss()
            deleteService(service)
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun deleteService(service: Service) {
        val progressDialog = ProgressDialog(this).apply {
            setMessage("Deleting service...")
            setCancelable(false)
            show()
        }

        RetrofitClient.serviceApi.deleteService(service.serviceId)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    progressDialog.dismiss()
                    if (response.isSuccessful) {
                        customNotification.show(
                            message = "Service deleted successfully",
                            title = "Success",
                            type = NotificationType.SUCCESS,
                            duration = 2000
                        )
                        loadServices()
                    } else {
                        customNotification.show(
                            message = "Failed to delete service: ${response.code()}",
                            title = "Error",
                            type = NotificationType.ERROR,
                            duration = 2000
                        )
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    progressDialog.dismiss()
                    customNotification.show(
                        message = "Error: ${t.message}",
                        title = "Connection Error",
                        type = NotificationType.ERROR,
                        duration = 3000
                    )
                }
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EDIT_SERVICE_REQUEST && resultCode == RESULT_OK) {
            loadServices()
            customNotification.show(
                message = "Service updated successfully",
                title = "Success",
                type = NotificationType.SUCCESS,
                duration = 2000
            )
        }
    }

    private fun setupSearchListener() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s.toString().trim()
                applyFilters()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupClickListeners() {
        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        btnAddService.setOnClickListener {
            val intent = Intent(this, AddServiceActivity::class.java)
            startActivity(intent)
        }

        btnProfile.setOnClickListener {
            val intent = Intent(this, UserProfileActivity::class.java)
            startActivity(intent)
        }

        btnFilter.setOnClickListener {
            showFilterDialog()
        }
    }

    private fun clearAllFilters() {
        currentCategoryFilter = null
        currentSearchQuery = ""
        etSearch.setText("")
        applyFilters()
        updateFilterIndicator()
        customNotification.show(
            message = "All filters cleared",
            title = "Filter Reset",
            type = NotificationType.INFO,
            duration = 1500
        )
    }

    private fun updateFilterIndicator() {
        if (!currentCategoryFilter.isNullOrEmpty()) {
            layoutFilterIndicator.visibility = View.VISIBLE
            tvActiveFilter.text = currentCategoryFilter
        } else if (currentSearchQuery.isNotEmpty()) {
            layoutFilterIndicator.visibility = View.VISIBLE
            tvActiveFilter.text = "Search: \"$currentSearchQuery\""
        } else {
            layoutFilterIndicator.visibility = View.GONE
            tvActiveFilter.text = ""
        }
    }

    private fun showFilterDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_filter)
        dialog.window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnFoodHospitality = dialog.findViewById<Button>(R.id.btnFoodHospitality)
        val btnMedicalHealth = dialog.findViewById<Button>(R.id.btnMedicalHealth)
        val btnRetailCommercial = dialog.findViewById<Button>(R.id.btnRetailCommercial)
        val btnPersonalLifestyle = dialog.findViewById<Button>(R.id.btnPersonalLifestyle)
        val btnClearFilter = dialog.findViewById<Button>(R.id.btnClearFilter)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)

        btnFoodHospitality.setOnClickListener {
            currentCategoryFilter = "Food & Hospitality"
            applyFilters()
            updateFilterIndicator()
            dialog.dismiss()
            customNotification.show(
                message = "Filtered: Food & Hospitality",
                title = "Filter Applied",
                type = NotificationType.INFO,
                duration = 1500
            )
        }

        btnMedicalHealth.setOnClickListener {
            currentCategoryFilter = "Medical & Health"
            applyFilters()
            updateFilterIndicator()
            dialog.dismiss()
            customNotification.show(
                message = "Filtered: Medical & Health",
                title = "Filter Applied",
                type = NotificationType.INFO,
                duration = 1500
            )
        }

        btnRetailCommercial.setOnClickListener {
            currentCategoryFilter = "Retail & Commercial"
            applyFilters()
            updateFilterIndicator()
            dialog.dismiss()
            customNotification.show(
                message = "Filtered: Retail & Commercial",
                title = "Filter Applied",
                type = NotificationType.INFO,
                duration = 1500
            )
        }

        btnPersonalLifestyle.setOnClickListener {
            currentCategoryFilter = "Personal & Lifestyle"
            applyFilters()
            updateFilterIndicator()
            dialog.dismiss()
            customNotification.show(
                message = "Filtered: Personal & Lifestyle",
                title = "Filter Applied",
                type = NotificationType.INFO,
                duration = 1500
            )
        }

        btnClearFilter.setOnClickListener {
            clearAllFilters()
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun applyFilters() {
        var filtered = servicesList.toList()

        if (!currentCategoryFilter.isNullOrEmpty()) {
            filtered = filtered.filter { it.serviceCategory == currentCategoryFilter }
        }

        if (currentSearchQuery.isNotEmpty()) {
            filtered = filtered.filter { service ->
                service.serviceName.contains(currentSearchQuery, ignoreCase = true) ||
                        service.serviceDescription.contains(currentSearchQuery, ignoreCase = true) ||
                        service.serviceCategory.contains(currentSearchQuery, ignoreCase = true)
            }
        }

        filteredServicesList.clear()
        filteredServicesList.addAll(filtered)
        serviceAdapter.updateServices(filteredServicesList)
        updateServiceCount(filteredServicesList.size)
        updateFilterIndicator()
    }

    private fun loadServices() {
        RetrofitClient.serviceApi.getAllServices().enqueue(object : Callback<List<Service>> {
            override fun onResponse(
                call: Call<List<Service>>,
                response: Response<List<Service>>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { services ->
                        servicesList.clear()
                        servicesList.addAll(services)

                        filteredServicesList.clear()
                        filteredServicesList.addAll(services)
                        serviceAdapter.updateServices(filteredServicesList)
                        updateServiceCount(services.size)
                    }
                } else {
                    customNotification.show(
                        message = "Failed to load services: ${response.code()}",
                        title = "Error",
                        type = NotificationType.ERROR,
                        duration = 2000
                    )
                }
            }

            override fun onFailure(call: Call<List<Service>>, t: Throwable) {
                customNotification.show(
                    message = "Error: ${t.message}",
                    title = "Connection Error",
                    type = NotificationType.ERROR,
                    duration = 3000
                )
            }
        })
    }

    private fun updateServiceCount(count: Int) {
        if (currentCategoryFilter != null || currentSearchQuery.isNotEmpty()) {
            tvServiceCount.text = "$count result${if (count != 1) "s" else ""}"
        } else {
            tvServiceCount.text = "$count service${if (count != 1) "s" else ""}"
        }
    }
}