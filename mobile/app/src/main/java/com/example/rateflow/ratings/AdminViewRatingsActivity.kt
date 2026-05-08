package com.example.rateflow.ratings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rateflow.R
import com.example.rateflow.services.Service
import com.example.rateflow.core.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class AdminViewRatingsActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "AdminViewRatings"
    }

    // Views
    private lateinit var btnBack: TextView
    private lateinit var ivServiceImage: ImageView
    private lateinit var tvServiceName: TextView
    private lateinit var tvServiceCategory: TextView
    private lateinit var tvAverageRating: TextView
    private lateinit var tvRatingCount: TextView
    private lateinit var averageStarsContainer: LinearLayout
    private lateinit var recyclerFeedbacks: RecyclerView
    private lateinit var tvFeedbackCount: TextView
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var progressBar: ProgressBar

    // Data
    private var serviceId: Int = -1
    private var currentService: Service? = null
    private var feedbacksList = mutableListOf<Rating>()
    private lateinit var feedbackAdapter: FeedbackAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_view_ratings)

        serviceId = intent.getIntExtra("serviceId", -1)

        if (serviceId == -1) {
            Toast.makeText(this, "Invalid service", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initializeViews()
        setupClickListeners()
        setupRecyclerView()
        loadServiceDetails()
        loadFeedbacks()
    }

    private fun initializeViews() {
        btnBack = findViewById(R.id.btnBack)
        ivServiceImage = findViewById(R.id.ivServiceImage)
        tvServiceName = findViewById(R.id.tvServiceName)
        tvServiceCategory = findViewById(R.id.tvServiceCategory)
        tvAverageRating = findViewById(R.id.tvAverageRating)
        tvRatingCount = findViewById(R.id.tvRatingCount)
        averageStarsContainer = findViewById(R.id.averageStarsContainer)
        recyclerFeedbacks = findViewById(R.id.recyclerFeedbacks)
        tvFeedbackCount = findViewById(R.id.tvFeedbackCount)
        layoutEmpty = findViewById(R.id.layoutEmpty)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        recyclerFeedbacks.layoutManager = LinearLayoutManager(this)
        feedbackAdapter = FeedbackAdapter(feedbacksList) { ratingId ->
            confirmDeleteFeedback(ratingId)
        }
        recyclerFeedbacks.adapter = feedbackAdapter
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
                } else {
                    Toast.makeText(this@AdminViewRatingsActivity, "Failed to load service details", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Service>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@AdminViewRatingsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
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
                    updateAverageStars(averageRating)

                    Log.d(TAG, "Rating stats loaded - Avg: $averageRating, Count: $totalRatings")
                } else {
                    Log.e(TAG, "Failed to load rating stats: ${response.code()}")
                    tvAverageRating.text = "0.0"
                    tvRatingCount.text = "(0 reviews)"
                    updateAverageStars(0.0)
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                Log.e(TAG, "Error loading rating stats", t)
                tvAverageRating.text = "0.0"
                tvRatingCount.text = "(0 reviews)"
                updateAverageStars(0.0)
            }
        })
    }

    private fun updateAverageStars(rating: Double) {
        averageStarsContainer.removeAllViews()

        val fullStars = rating.toInt()
        val hasHalfStar = rating - fullStars >= 0.5

        for (i in 0 until 5) {
            val star = ImageView(this)
            val params = LinearLayout.LayoutParams(
                dpToPx(20),
                dpToPx(20)
            )
            params.setMargins(dpToPx(2), 0, dpToPx(2), 0)
            star.layoutParams = params
            star.scaleType = ImageView.ScaleType.FIT_CENTER

            val drawableRes = when {
                i < fullStars -> R.drawable.ic_star_filled
                i == fullStars && hasHalfStar -> R.drawable.ic_star_half
                else -> R.drawable.ic_star_empty
            }

            star.setImageResource(drawableRes)
            averageStarsContainer.addView(star)
        }
    }

    private fun loadFeedbacks() {
        progressBar.visibility = View.VISIBLE
        layoutEmpty.visibility = View.GONE

        RetrofitClient.ratingApi.getFeedbacksByService(serviceId).enqueue(object : Callback<List<Rating>> {
            override fun onResponse(call: Call<List<Rating>>, response: Response<List<Rating>>) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body() != null) {
                    feedbacksList.clear()
                    feedbacksList.addAll(response.body()!!)

                    // Sort by date (newest first)
                    feedbacksList.sortByDescending { it.dateCreated }

                    feedbackAdapter.updateFeedbacks(feedbacksList)
                    updateFeedbackCount(feedbacksList.size)

                    if (feedbacksList.isEmpty()) {
                        layoutEmpty.visibility = View.VISIBLE
                    } else {
                        layoutEmpty.visibility = View.GONE
                    }

                    Log.d(TAG, "Loaded ${feedbacksList.size} feedbacks")
                } else {
                    Log.e(TAG, "Failed to load feedbacks: ${response.code()}")
                    layoutEmpty.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<List<Rating>>, t: Throwable) {
                progressBar.visibility = View.GONE
                Log.e(TAG, "Error loading feedbacks", t)
                layoutEmpty.visibility = View.VISIBLE
            }
        })
    }

    private fun updateFeedbackCount(count: Int) {
        tvFeedbackCount.text = "$count item${if (count != 1) "s" else ""}"
    }

    private fun confirmDeleteFeedback(ratingId: Int) {
        AlertDialog.Builder(this)
            .setTitle("Delete Feedback")
            .setMessage("Are you sure you want to delete this feedback? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteFeedback(ratingId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteFeedback(ratingId: Int) {
        progressBar.visibility = View.VISIBLE

        RetrofitClient.ratingApi.deleteRating(ratingId).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    Toast.makeText(this@AdminViewRatingsActivity, "Feedback deleted successfully", Toast.LENGTH_SHORT).show()

                    // Remove from list and update UI
                    val position = feedbacksList.indexOfFirst { it.ratingId == ratingId }
                    if (position != -1) {
                        feedbacksList.removeAt(position)
                        feedbackAdapter.updateFeedbacks(feedbacksList)
                        updateFeedbackCount(feedbacksList.size)

                        if (feedbacksList.isEmpty()) {
                            layoutEmpty.visibility = View.VISIBLE
                        }
                    }

                    // Refresh rating stats
                    loadRatingStats()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Failed to delete feedback"
                    Toast.makeText(this@AdminViewRatingsActivity, errorMsg, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@AdminViewRatingsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayFeedbackStars(rating: Int, container: LinearLayout) {
        container.removeAllViews()

        for (i in 0 until 5) {
            val star = ImageView(this)
            val params = LinearLayout.LayoutParams(
                dpToPx(16),
                dpToPx(16)
            )
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

    private fun formatDate(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return "Recently"

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

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    // Inner Adapter Class
    inner class FeedbackAdapter(
        private var feedbacks: List<Rating>,
        private val onDeleteClick: (Int) -> Unit
    ) : RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedbackViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.admin_feedback_item, parent, false)
            return FeedbackViewHolder(view)
        }

        override fun onBindViewHolder(holder: FeedbackViewHolder, position: Int) {
            val feedback = feedbacks[position]
            holder.bind(feedback)
        }

        override fun getItemCount(): Int = feedbacks.size

        fun updateFeedbacks(newFeedbacks: List<Rating>) {
            feedbacks = newFeedbacks
            notifyDataSetChanged()
        }

        inner class FeedbackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
            private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
            private val feedbackStarsContainer: LinearLayout = itemView.findViewById(R.id.feedbackStarsContainer)
            private val tvFeedbackText: TextView = itemView.findViewById(R.id.tvFeedbackText)
            private val btnDeleteFeedback: ImageButton = itemView.findViewById(R.id.btnDeleteFeedback)

            fun bind(feedback: Rating) {
                tvUserName.text = feedback.userName ?: "Anonymous User"
                tvDate.text = formatDate(feedback.dateCreated)
                tvFeedbackText.text = feedback.feedbackText?.takeIf { it.isNotBlank() } ?: "No comment provided."

                val rating = feedback.starRate ?: 0
                displayFeedbackStars(rating, feedbackStarsContainer)

                btnDeleteFeedback.setOnClickListener {
                    feedback.ratingId?.let { ratingId ->
                        onDeleteClick(ratingId)
                    }
                }
            }
        }
    }
}