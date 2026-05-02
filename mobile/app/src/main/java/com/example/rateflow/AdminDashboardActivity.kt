package com.example.rateflow

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rateflow.adapter.ServiceAdapter
import com.example.rateflow.model.Service
import com.example.rateflow.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminDashboardActivity : AppCompatActivity() {

    // Toolbar buttons
    private lateinit var btnMenu: ImageButton
    private lateinit var btnAddService: ImageButton
    private lateinit var btnNotification: ImageButton
    private lateinit var btnProfile: ImageButton

    // RecyclerView
    private lateinit var recyclerServices: RecyclerView
    private lateinit var serviceAdapter: ServiceAdapter
    private var servicesList = mutableListOf<Service>()
    private var filteredServicesList = mutableListOf<Service>()

    // Quick actions
    private lateinit var btnViewAll: View

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

    companion object {
        private const val EDIT_SERVICE_REQUEST = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        initializeViews()
        setupRecyclerView()
        setupClickListeners()
        setupSearchListener()
        loadServices()
    }

    private fun initializeViews() {
        btnMenu = findViewById(R.id.btnMenu)
        btnAddService = findViewById(R.id.btnAddService)
        btnNotification = findViewById(R.id.btnNotification)
        btnProfile = findViewById(R.id.btnProfile)
        btnViewAll = findViewById(R.id.btnViewAll)
        btnFilter = findViewById(R.id.btnFilter)
        etSearch = findViewById(R.id.etSearch)
        tvServiceCount = findViewById(R.id.tvServiceCount)
        recyclerServices = findViewById(R.id.recyclerServices)

        // Filter indicator views
        layoutFilterIndicator = findViewById(R.id.layoutFilterIndicator)
        tvActiveFilter = findViewById(R.id.tvActiveFilter)
        btnClearFilterIndicator = findViewById(R.id.btnClearFilterIndicator)

        // Set clear filter click listener
        btnClearFilterIndicator.setOnClickListener {
            clearAllFilters()
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
            onCardClick = { service ->  // NEW: Handle card click to view ratings
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
        // Better approach - show a dialog or use a proper ProgressBar in layout
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
                        Toast.makeText(this@AdminDashboardActivity,
                            "Service deleted successfully", Toast.LENGTH_SHORT).show()
                        loadServices()
                    } else {
                        Toast.makeText(this@AdminDashboardActivity,
                            "Failed to delete service: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    progressDialog.dismiss()
                    Toast.makeText(this@AdminDashboardActivity,
                        "Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EDIT_SERVICE_REQUEST && resultCode == RESULT_OK) {
            // Refresh the services list
            loadServices()
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
            Toast.makeText(this, "Menu clicked", Toast.LENGTH_SHORT).show()
        }

        btnAddService.setOnClickListener {
            val intent = Intent(this, AddServiceActivity::class.java)
            startActivity(intent)
        }

        btnNotification.setOnClickListener {
            Toast.makeText(this, "Notifications clicked", Toast.LENGTH_SHORT).show()
        }

        btnProfile.setOnClickListener {
            Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show()
        }

        btnViewAll.setOnClickListener {
            clearAllFilters()
            Toast.makeText(this, "Showing all services", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(this, "Filtered: Food & Hospitality", Toast.LENGTH_SHORT).show()
        }

        btnMedicalHealth.setOnClickListener {
            currentCategoryFilter = "Medical & Health"
            applyFilters()
            updateFilterIndicator()
            dialog.dismiss()
            Toast.makeText(this, "Filtered: Medical & Health", Toast.LENGTH_SHORT).show()
        }

        btnRetailCommercial.setOnClickListener {
            currentCategoryFilter = "Retail & Commercial"
            applyFilters()
            updateFilterIndicator()
            dialog.dismiss()
            Toast.makeText(this, "Filtered: Retail & Commercial", Toast.LENGTH_SHORT).show()
        }

        btnPersonalLifestyle.setOnClickListener {
            currentCategoryFilter = "Personal & Lifestyle"
            applyFilters()
            updateFilterIndicator()
            dialog.dismiss()
            Toast.makeText(this, "Filtered: Personal & Lifestyle", Toast.LENGTH_SHORT).show()
        }

        btnClearFilter.setOnClickListener {
            clearAllFilters()
            dialog.dismiss()
            Toast.makeText(this, "Filter cleared", Toast.LENGTH_SHORT).show()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun applyFilters() {
        var filtered = servicesList.toList()

        // Apply category filter
        if (!currentCategoryFilter.isNullOrEmpty()) {
            filtered = filtered.filter { it.serviceCategory == currentCategoryFilter }
        }

        // Apply search filter
        if (currentSearchQuery.isNotEmpty()) {
            filtered = filtered.filter { service ->
                service.serviceName.contains(currentSearchQuery, ignoreCase = true) ||
                        service.serviceDescription.contains(currentSearchQuery, ignoreCase = true) ||
                        service.serviceCategory.contains(currentSearchQuery, ignoreCase = true)
            }
        }

        // Update the filtered list
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
                    Toast.makeText(
                        this@AdminDashboardActivity,
                        "Failed to load services: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<Service>>, t: Throwable) {
                Toast.makeText(
                    this@AdminDashboardActivity,
                    "Error: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
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