package com.example.rateflow

import android.app.Activity
import android.app.Dialog
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.rateflow.network.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class AddServiceActivity : AppCompatActivity() {

    private lateinit var etServiceName: EditText
    private lateinit var etDescription: EditText
    private lateinit var btnCreateService: Button
    private lateinit var layoutImagePicker: LinearLayout
    private lateinit var ivServiceImage: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvImageError: TextView
    private lateinit var tvServiceNameError: TextView
    private lateinit var tvCategoryError: TextView
    private lateinit var tvDescriptionError: TextView
    private lateinit var layoutCategory: LinearLayout
    private lateinit var tvCategoryPlaceholder: TextView

    private var selectedImageUri: Uri? = null
    private var selectedImageFile: File? = null
    private var selectedCategory: String? = null

    private val categories = arrayOf(
        "Food & Hospitality",
        "Medical & Health",
        "Retail & Commercial",
        "Personal & Lifestyle"
    )

    private val categoryIcons = mapOf(
        "Food & Hospitality" to "🍽️",
        "Medical & Health" to "🏥",
        "Retail & Commercial" to "🛍️",
        "Personal & Lifestyle" to "💆"
    )

    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_service)

        initializeViews()
        setupCategorySelector()
        setupClickListeners()
    }

    private fun initializeViews() {
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        etServiceName = findViewById(R.id.etServiceName)
        etDescription = findViewById(R.id.etDescription)
        btnCreateService = findViewById(R.id.btnCreateService)
        layoutImagePicker = findViewById(R.id.layoutImagePicker)
        ivServiceImage = findViewById(R.id.ivServiceImage)
        progressBar = findViewById(R.id.progressBar)
        tvImageError = findViewById(R.id.tvImageError)
        tvServiceNameError = findViewById(R.id.tvServiceNameError)
        tvCategoryError = findViewById(R.id.tvCategoryError)
        tvDescriptionError = findViewById(R.id.tvDescriptionError)
        layoutCategory = findViewById(R.id.layoutCategory)
        tvCategoryPlaceholder = findViewById(R.id.tvCategoryPlaceholder)

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupCategorySelector() {
        layoutCategory.setOnClickListener {
            showCategoryDialog()
        }
    }

    private fun showCategoryDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_category)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val title = dialog.findViewById<TextView>(R.id.tvDialogTitle)
        val rvCategories = dialog.findViewById<LinearLayout>(R.id.rvCategories)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)

        // Populate categories
        for (category in categories) {
            val categoryView = layoutInflater.inflate(R.layout.item_category_option, null)
            val icon = categoryView.findViewById<TextView>(R.id.tvCategoryIcon)
            val name = categoryView.findViewById<TextView>(R.id.tvCategoryName)
            val card = categoryView.findViewById<CardView>(R.id.cardCategory)

            icon.text = categoryIcons[category] ?: "📋"
            name.text = category

            card.setOnClickListener {
                selectedCategory = category
                tvCategoryPlaceholder.text = selectedCategory
                tvCategoryPlaceholder.setTextColor(resources.getColor(android.R.color.white))
                tvCategoryError.visibility = View.GONE
                dialog.dismiss()
            }

            rvCategories.addView(categoryView)
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun setupClickListeners() {
        layoutImagePicker.setOnClickListener {
            openImagePicker()
        }

        btnCreateService.setOnClickListener {
            createService()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            ivServiceImage.setImageURI(selectedImageUri)
            ivServiceImage.scaleType = ImageView.ScaleType.CENTER_CROP
            tvImageError.visibility = View.GONE

            // Convert URI to File for all Android versions
            selectedImageFile = uriToFile(selectedImageUri!!)
        }
    }

    private fun uriToFile(uri: Uri): File? {
        return try {
            val contentResolver: ContentResolver = contentResolver
            val fileName = "service_image_${System.currentTimeMillis()}.jpg"
            val tempFile = File(cacheDir, fileName)

            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        // Validate Service Name
        val serviceName = etServiceName.text.toString().trim()
        if (serviceName.isEmpty()) {
            tvServiceNameError.visibility = View.VISIBLE
            isValid = false
        } else {
            tvServiceNameError.visibility = View.GONE
        }

        // Validate Category
        if (selectedCategory.isNullOrEmpty()) {
            tvCategoryError.visibility = View.VISIBLE
            isValid = false
        } else {
            tvCategoryError.visibility = View.GONE
        }

        // Validate Description
        val description = etDescription.text.toString().trim()
        if (description.isEmpty()) {
            tvDescriptionError.visibility = View.VISIBLE
            isValid = false
        } else {
            tvDescriptionError.visibility = View.GONE
        }

        // Validate Image
        if (selectedImageFile == null || !selectedImageFile!!.exists()) {
            tvImageError.visibility = View.VISIBLE
            isValid = false
        } else {
            tvImageError.visibility = View.GONE
        }

        return isValid
    }

    private fun createService() {
        if (!validateInputs()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val serviceName = etServiceName.text.toString().trim()
        val category = selectedCategory!!
        val description = etDescription.text.toString().trim()

        // Get logged in user info from SharedPreferences
        val sharedPref = getSharedPreferences("RateFlowPrefs", MODE_PRIVATE)
        val createdBy = sharedPref.getString("userEmail", "Admin") ?: "Admin"

        // Show progress
        progressBar.visibility = View.VISIBLE
        btnCreateService.isEnabled = false

        // Create Multipart request
        val mediaTypePlain = "text/plain".toMediaTypeOrNull()
        val mediaTypeImage = "image/*".toMediaTypeOrNull()

        val serviceNamePart = RequestBody.create(mediaTypePlain, serviceName)
        val categoryPart = RequestBody.create(mediaTypePlain, category)
        val descriptionPart = RequestBody.create(mediaTypePlain, description)
        val createdByPart = RequestBody.create(mediaTypePlain, createdBy)

        // Prepare image file
        val requestFile = RequestBody.create(mediaTypeImage, selectedImageFile!!)
        val imagePart = MultipartBody.Part.createFormData("image", selectedImageFile!!.name, requestFile)

        // Make API call
        RetrofitClient.serviceApi.createService(
            serviceNamePart,
            categoryPart,
            descriptionPart,
            createdByPart,
            imagePart
        ).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                progressBar.visibility = View.GONE
                btnCreateService.isEnabled = true

                if (response.isSuccessful) {
                    Toast.makeText(
                        this@AddServiceActivity,
                        "Service created successfully!",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Return to dashboard
                    val intent = Intent(this@AddServiceActivity, AdminDashboardActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(
                        this@AddServiceActivity,
                        "Failed to create service: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                progressBar.visibility = View.GONE
                btnCreateService.isEnabled = true
                Toast.makeText(
                    this@AddServiceActivity,
                    "Error: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }
}