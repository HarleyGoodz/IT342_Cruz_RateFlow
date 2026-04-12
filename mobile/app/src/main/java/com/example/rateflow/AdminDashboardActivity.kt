package com.example.rateflow

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.GridLayoutManager

import com.example.rateflow.adapter.ServiceAdapter
import com.example.rateflow.model.Service
import com.example.rateflow.network.RetrofitClient

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminDashboardActivity : AppCompatActivity() {

    // Toolbar buttons
    private lateinit var btnMenu: ImageButton
    private lateinit var btnNotification: ImageButton
    private lateinit var btnProfile: ImageButton

    // RecyclerView
    private lateinit var recyclerServices: RecyclerView
    private lateinit var serviceAdapter: ServiceAdapter

    // Quick actions
    private lateinit var btnViewAll: View

    // Filter + Search
    private lateinit var btnFilter: TextView
    private lateinit var etSearch: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_admin_dashboard)

        initializeViews()
        setupRecyclerView()
        setupClickListeners()

        loadServices()
    }

    private fun initializeViews() {

        btnMenu =
            findViewById(R.id.btnMenu)

        btnNotification =
            findViewById(R.id.btnNotification)

        btnProfile =
            findViewById(R.id.btnProfile)

        btnViewAll =
            findViewById(R.id.btnViewAll)

        btnFilter =
            findViewById(R.id.btnFilter)

        etSearch =
            findViewById(R.id.etSearch)

        recyclerServices =
            findViewById(R.id.recyclerServices)
    }

    private fun setupRecyclerView() {

        recyclerServices.layoutManager =
            GridLayoutManager(this, 2)
    }

    private fun setupClickListeners() {

        btnMenu.setOnClickListener {

            Toast.makeText(
                this,
                "Menu clicked",
                Toast.LENGTH_SHORT
            ).show()
        }

        btnNotification.setOnClickListener {

            Toast.makeText(
                this,
                "Notifications clicked",
                Toast.LENGTH_SHORT
            ).show()
        }

        btnProfile.setOnClickListener {

            Toast.makeText(
                this,
                "Profile clicked",
                Toast.LENGTH_SHORT
            ).show()
        }

        btnViewAll.setOnClickListener {

            Toast.makeText(
                this,
                "View All clicked",
                Toast.LENGTH_SHORT
            ).show()
        }

        btnFilter.setOnClickListener {

            Toast.makeText(
                this,
                "Filter clicked",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun loadServices() {

        RetrofitClient.serviceApi
            .getAllServices()
            .enqueue(object : Callback<List<Service>> {

                override fun onResponse(
                    call: Call<List<Service>>,
                    response: Response<List<Service>>
                ) {

                    if (response.isSuccessful) {

                        val services =
                            response.body()

                        if (services != null) {

                            serviceAdapter =
                                ServiceAdapter(services)

                            recyclerServices.adapter =
                                serviceAdapter
                        }

                    } else {

                        Toast.makeText(
                            this@AdminDashboardActivity,
                            "Failed to load services",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(
                    call: Call<List<Service>>,
                    t: Throwable
                ) {

                    Toast.makeText(
                        this@AdminDashboardActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
}