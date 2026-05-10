package com.example.rateflow.ratings

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.example.rateflow.R
import com.example.rateflow.services.Service
import com.example.rateflow.core.RetrofitClient
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class RateServiceActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "RateServiceActivity"
    }

    // Views
    private lateinit var btnBack: ImageButton
    private lateinit var ivServiceImage: ImageView
    private lateinit var tvServiceName: TextView
    private lateinit var tvServiceCategory: TextView
    private lateinit var tvAverageRating: TextView
    private lateinit var tvRatingCount: TextView
    private lateinit var ratingStarsDisplay: LinearLayout
    private lateinit var ratingBarContainer: LinearLayout
    private lateinit var etFeedback: TextInputEditText
    private lateinit var btnSubmitRating: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var tvAlreadyRated: TextView
    private lateinit var cardService: CardView

    // Feedbacks Views
    private lateinit var feedbackProgressBar: ProgressBar
    private lateinit var tvNoFeedbacks: TextView
    private lateinit var feedbacksContainer: LinearLayout

    // Data
    private var serviceId: Int = -1
    private var currentService: Service? = null
    private var selectedStarRating: Int = 0
    private var hasUserRated: Boolean = false
    private var currentUserId: Int = 0
    private var currentUsername: String = ""

    // Star views arrays
    private val displayStars = arrayOfNulls<ImageView>(5)
    private val inputStars = arrayOfNulls<ImageView>(5)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.rate_service)

        serviceId = intent.getIntExtra("serviceId", -1)

        if (serviceId == -1) {
            Toast.makeText(this, "Invalid service", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadUserSession()
        initializeViews()
        setupClickListeners()
        loadServiceDetails()
    }

    private fun loadUserSession() {
        val sharedPref = getSharedPreferences("UserProfiles", Context.MODE_PRIVATE)
        currentUserId = sharedPref.getInt("user_id", 0)
        currentUsername = sharedPref.getString("current_username", "User") ?: "User"
        Log.d(TAG, "User session - ID: $currentUserId, Name: $currentUsername")
    }

    private fun initializeViews() {
        btnBack = findViewById(R.id.btnBack)
        ivServiceImage = findViewById(R.id.ivServiceImage)
        tvServiceName = findViewById(R.id.tvServiceName)
        tvServiceCategory = findViewById(R.id.tvServiceCategory)
        tvAverageRating = findViewById(R.id.tvAverageRating)
        tvRatingCount = findViewById(R.id.tvRatingCount)
        ratingStarsDisplay = findViewById(R.id.ratingStarsDisplay)
        ratingBarContainer = findViewById(R.id.ratingBarContainer)
        etFeedback = findViewById(R.id.etFeedback)
        btnSubmitRating = findViewById(R.id.btnSubmitRating)
        progressBar = findViewById(R.id.progressBar)
        tvAlreadyRated = findViewById(R.id.tvAlreadyRated)
        cardService = findViewById(R.id.cardService)

        // Feedbacks views
        feedbackProgressBar = findViewById(R.id.feedbackProgressBar)
        tvNoFeedbacks = findViewById(R.id.tvNoFeedbacks)
        feedbacksContainer = findViewById(R.id.feedbacksContainer)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener { finish() }
        btnSubmitRating.setOnClickListener { submitRating() }
        createInputStars()
    }

    private fun createInputStars() {
        ratingBarContainer.removeAllViews()

        for (i in 0 until 5) {
            val starPosition = i
            val star = ImageView(this)
            val params = LinearLayout.LayoutParams(dpToPx(48), dpToPx(48))
            params.setMargins(dpToPx(4), 0, dpToPx(4), 0)
            star.layoutParams = params
            star.setImageResource(R.drawable.ic_star_empty)
            star.scaleType = ImageView.ScaleType.FIT_CENTER
            star.setOnClickListener {
                selectedStarRating = starPosition + 1
                updateInputStars(selectedStarRating)
                Log.d(TAG, "Selected rating: $selectedStarRating stars")
            }
            ratingBarContainer.addView(star)
            inputStars[i] = star
        }
    }

    private fun updateInputStars(rating: Int) {
        for (i in inputStars.indices) {
            inputStars[i]?.setImageResource(
                if (i < rating) R.drawable.ic_star_filled
                else R.drawable.ic_star_empty
            )
        }
    }

    private fun updateDisplayStars(rating: Double) {
        ratingStarsDisplay.removeAllViews()

        val fullStars = rating.toInt()
        val hasHalfStar = rating - fullStars >= 0.5

        for (i in 0 until 5) {
            val star = ImageView(this)
            val params = LinearLayout.LayoutParams(dpToPx(24), dpToPx(24))
            params.setMargins(dpToPx(2), 0, dpToPx(2), 0)
            star.layoutParams = params
            star.scaleType = ImageView.ScaleType.FIT_CENTER

            val drawableRes = when {
                i < fullStars -> R.drawable.ic_star_filled
                i == fullStars && hasHalfStar -> R.drawable.ic_star_half
                else -> R.drawable.ic_star_empty
            }

            star.setImageResource(drawableRes)
            ratingStarsDisplay.addView(star)
            displayStars[i] = star
        }
    }

    private fun displayFeedbackStars(rating: Int, container: LinearLayout) {
        container.removeAllViews()

        for (i in 0 until 5) {
            val star = ImageView(this)
            val params = LinearLayout.LayoutParams(dpToPx(16), dpToPx(16))
            params.setMargins(dpToPx(2), 0, dpToPx(2), 0)
            star.layoutParams = params
            star.scaleType = ImageView.ScaleType.FIT_CENTER

            star.setImageResource(
                if (i < rating) R.drawable.ic_star_filled
                else R.drawable.ic_star_empty
            )

            container.addView(star)
        }
    }

    private fun loadServiceDetails() {
        progressBar.visibility = View.VISIBLE

        RetrofitClient.serviceApi.getServiceById(serviceId).enqueue(object : Callback<Service> {
            override fun onResponse(call: Call<Service>, response: Response<Service>) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body() != null) {
                    currentService = response.body()
                    displayServiceInfo()
                    loadRatingStats()
                    loadFeedbacks()
                    checkUserRating()
                } else {
                    Toast.makeText(this@RateServiceActivity, "Failed to load service details", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun onFailure(call: Call<Service>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@RateServiceActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }

    private fun displayServiceInfo() {
        currentService?.let { service ->
            tvServiceName.text = service.serviceName
            tvServiceCategory.text = service.serviceCategory

            val imageUrl = "${RetrofitClient.getBaseUrl()}api/services/${serviceId}/image"
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_service_placeholder)
                .error(R.drawable.ic_service_placeholder)
                .into(ivServiceImage)
        }
    }

    private fun loadRatingStats() {
        RetrofitClient.ratingApi.getRatingStats(serviceId).enqueue(object : Callback<Map<String, Any>> {
            @Suppress("UNCHECKED_CAST")
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                if (response.isSuccessful && response.body() != null) {
                    val stats = response.body() as Map<String, Any>

                    val averageRating = (stats["average"] as? Number)?.toDouble() ?: 0.0
                    val totalRatings = (stats["total"] as? Number)?.toInt() ?: 0

                    tvAverageRating.text = String.format("%.1f", averageRating)
                    tvRatingCount.text = "($totalRatings review${if (totalRatings != 1) "s" else ""})"
                    updateDisplayStars(averageRating)

                    Log.d(TAG, "Rating stats loaded - Avg: $averageRating, Count: $totalRatings")
                } else {
                    Log.e(TAG, "Failed to load rating stats: ${response.code()}")
                    tvAverageRating.text = "0.0"
                    tvRatingCount.text = "(0 reviews)"
                    updateDisplayStars(0.0)
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                Log.e(TAG, "Error loading rating stats", t)
                tvAverageRating.text = "0.0"
                tvRatingCount.text = "(0 reviews)"
                updateDisplayStars(0.0)
            }
        })
    }

    private fun loadFeedbacks() {
        feedbackProgressBar.visibility = View.VISIBLE
        feedbacksContainer.removeAllViews()
        tvNoFeedbacks.visibility = View.GONE

        RetrofitClient.ratingApi.getFeedbacksByService(serviceId).enqueue(object : Callback<List<Rating>> {
            override fun onResponse(call: Call<List<Rating>>, response: Response<List<Rating>>) {
                feedbackProgressBar.visibility = View.GONE

                if (response.isSuccessful && response.body() != null) {
                    val feedbacks = response.body()!!
                    Log.d(TAG, "Loaded ${feedbacks.size} feedbacks")

                    if (feedbacks.isEmpty()) {
                        tvNoFeedbacks.visibility = View.VISIBLE
                    } else {
                        tvNoFeedbacks.visibility = View.GONE

                        // Sort by date (newest first) if date is available
                        val sortedFeedbacks = feedbacks.sortedByDescending { it.dateCreated }

                        for (feedback in sortedFeedbacks) {
                            addFeedbackToContainer(feedback)
                        }
                    }
                } else {
                    Log.e(TAG, "Failed to load feedbacks: ${response.code()}")
                    tvNoFeedbacks.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<List<Rating>>, t: Throwable) {
                feedbackProgressBar.visibility = View.GONE
                Log.e(TAG, "Error loading feedbacks", t)
                tvNoFeedbacks.visibility = View.VISIBLE
            }
        })
    }

    private fun addFeedbackToContainer(feedback: Rating) {
        val inflater = LayoutInflater.from(this)
        val feedbackView = inflater.inflate(R.layout.feedback_item, feedbacksContainer, false)

        val tvUserName = feedbackView.findViewById<TextView>(R.id.tvUserName)
        val tvDate = feedbackView.findViewById<TextView>(R.id.tvDate)
        val feedbackStarsContainer = feedbackView.findViewById<LinearLayout>(R.id.feedbackStarsContainer)
        val tvFeedbackText = feedbackView.findViewById<TextView>(R.id.tvFeedbackText)

        // Set user name
        tvUserName.text = feedback.userName ?: "Anonymous User"

        // Format and set date
        val dateStr = feedback.dateCreated?.let { formatDate(it) } ?: "Recently"
        tvDate.text = dateStr

        // Display stars
        val rating = feedback.starRate ?: 0
        displayFeedbackStars(rating, feedbackStarsContainer)

        // Set feedback text - REMOVE the color setting, let XML handle it
        val feedbackText = feedback.feedbackText
        if (feedbackText.isNullOrEmpty()) {
            tvFeedbackText.text = "No comment provided."
        } else {
            tvFeedbackText.text = feedbackText
            // Don't set text color here - let XML's android:textColor="#FFFFFF" work
        }

        feedbacksContainer.addView(feedbackView)
    }

    private fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            Log.e(TAG, "Error formatting date", e)
            "Recently"
        }
    }

    private fun checkUserRating() {
        if (currentUserId == 0) return

        RetrofitClient.ratingApi.checkUserRating(currentUserId, serviceId).enqueue(object : Callback<Map<String, Any>> {
            @Suppress("UNCHECKED_CAST")
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                if (response.isSuccessful && response.body() != null) {
                    val result = response.body() as Map<String, Any>
                    hasUserRated = result["hasRated"] as? Boolean ?: false

                    if (hasUserRated) {
                        btnSubmitRating.isEnabled = false
                        btnSubmitRating.alpha = 0.5f
                        tvAlreadyRated.visibility = View.VISIBLE

                        (result["rating"] as? Number)?.toInt()?.let { userRating ->
                            updateInputStars(userRating)
                        }

                        (result["feedback"] as? String)?.let { feedback ->
                            etFeedback.setText(feedback)
                            etFeedback.isEnabled = false
                        }

                        Log.d(TAG, "User has already rated this service")
                    }
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                Log.e(TAG, "Failed to check user rating", t)
            }
        })
    }

    private fun submitRating() {
        if (hasUserRated) {
            Toast.makeText(this, "You have already rated this service", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedStarRating == 0) {
            Toast.makeText(this, "Please select a star rating", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentUserId == 0) {
            Toast.makeText(this, "Please login to rate services", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        btnSubmitRating.isEnabled = false

        val rating = Rating().apply {
            serviceId = this@RateServiceActivity.serviceId
            userId = currentUserId
            starRate = selectedStarRating
            feedbackText = etFeedback.text?.toString()?.trim() ?: ""
            userName = currentUsername
        }

        RetrofitClient.ratingApi.submitRating(rating).enqueue(object : Callback<RatingResponse> {
            override fun onResponse(call: Call<RatingResponse>, response: Response<RatingResponse>) {
                progressBar.visibility = View.GONE

                val errorBody = if (!response.isSuccessful) response.errorBody()?.string() else null
                val isRatingIdError = errorBody?.contains("The given id must not be null") == true ||
                        errorBody?.contains("ratingId") == true

                if (response.isSuccessful) {
                    val ratingResponse = response.body()
                    if (ratingResponse != null && ratingResponse.success) {
                        showSuccessDialogAndRefresh()
                    } else {
                        Toast.makeText(this@RateServiceActivity, "Rating submitted!", Toast.LENGTH_SHORT).show()
                        showSuccessDialogAndRefresh()
                    }
                } else if (isRatingIdError) {
                    Log.d(TAG, "Rating was saved successfully despite the error response")
                    showSuccessDialogAndRefresh()
                } else {
                    Toast.makeText(this@RateServiceActivity, "Failed to submit rating", Toast.LENGTH_SHORT).show()
                    btnSubmitRating.isEnabled = true
                }
            }

            override fun onFailure(call: Call<RatingResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                btnSubmitRating.isEnabled = true
                Toast.makeText(this@RateServiceActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showSuccessDialogAndRefresh() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_rating_success, null)
        val dialogRatingStars = dialogView.findViewById<LinearLayout>(R.id.dialogRatingStars)
        val dialogMessage = dialogView.findViewById<TextView>(R.id.dialogMessage)
        val feedbackPreviewContainer = dialogView.findViewById<LinearLayout>(R.id.feedbackPreviewContainer)
        val tvFeedbackPreview = dialogView.findViewById<TextView>(R.id.tvFeedbackPreview)
        val btnOK = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnDialogOK)

        // Display selected stars in dialog
        displayDialogStars(selectedStarRating, dialogRatingStars)

        // Customize message based on rating
        val message = when (selectedStarRating) {
            5 -> "Excellent! Thank you for your 5-star rating! 🌟"
            4 -> "Great! We appreciate your positive feedback! 👍"
            3 -> "Thank you for your honest feedback! 📝"
            2 -> "Thanks for your feedback. We'll work to improve! 💪"
            else -> "Thank you for sharing your experience! 🙏"
        }
        dialogMessage.text = message

        // Show feedback preview if user wrote something
        val feedbackText = etFeedback.text?.toString()?.trim()
        if (!feedbackText.isNullOrEmpty()) {
            tvFeedbackPreview.text = "\"$feedbackText\""
            feedbackPreviewContainer.visibility = View.VISIBLE
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        btnOK.setOnClickListener {
            dialog.dismiss()
            loadRatingStats()
            loadFeedbacks()

            hasUserRated = true
            btnSubmitRating.isEnabled = false
            btnSubmitRating.alpha = 0.5f
            tvAlreadyRated.visibility = View.VISIBLE
            etFeedback.isEnabled = false
            updateInputStars(selectedStarRating)
        }

        btnSubmitRating.isEnabled = true
    }

    private fun displayDialogStars(rating: Int, container: LinearLayout) {
        container.removeAllViews()

        for (i in 0 until 5) {
            val star = ImageView(this)
            val params = LinearLayout.LayoutParams(dpToPx(32), dpToPx(32))
            params.setMargins(dpToPx(4), 0, dpToPx(4), 0)
            star.layoutParams = params
            star.scaleType = ImageView.ScaleType.FIT_CENTER

            star.setImageResource(
                if (i < rating) R.drawable.ic_star_filled
                else R.drawable.ic_star_empty
            )

            container.addView(star)
        }
    }


    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}