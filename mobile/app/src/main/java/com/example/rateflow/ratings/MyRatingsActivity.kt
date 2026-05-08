package com.example.rateflow.ratings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rateflow.R
import com.example.rateflow.core.RetrofitClient
import com.example.rateflow.services.Service
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyRatingsActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MyRatingsActivity"
    }

    private lateinit var btnBack: TextView
    private lateinit var recyclerRatings: RecyclerView
    private lateinit var tvTotalRatings: TextView
    private lateinit var tvAverageRating: TextView
    private lateinit var averageStarsContainer: LinearLayout
    private lateinit var tvRatingCount: TextView
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var progressBar: ProgressBar

    private lateinit var ratingsAdapter: MyRatingsAdapter
    private var ratingsList = mutableListOf<Rating>()
    private var currentUserId: Int = 0
    private var currentUsername: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.myratings)

        initializeViews()
        setupClickListeners()
        loadUserSession()
        loadUserRatings()
    }

    private fun initializeViews() {
        btnBack = findViewById(R.id.btnBack)
        recyclerRatings = findViewById(R.id.recyclerMyRatings)
        tvTotalRatings = findViewById(R.id.tvTotalRatings)
        tvAverageRating = findViewById(R.id.tvAverageRating)
        averageStarsContainer = findViewById(R.id.averageStarsContainer)
        tvRatingCount = findViewById(R.id.tvRatingCount)
        layoutEmpty = findViewById(R.id.layoutEmpty)
        progressBar = findViewById(R.id.progressBar)

        recyclerRatings.layoutManager = LinearLayoutManager(this)
        ratingsAdapter = MyRatingsAdapter(ratingsList, onDeleteClick = { rating, position ->
            showDeleteConfirmationDialog(rating, position)
        })
        recyclerRatings.adapter = ratingsAdapter
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun navigateToRateService(serviceId: Int) {
        Log.d(TAG, "navigateToRateService called with serviceId: $serviceId")
        val intent = Intent(this, RateServiceActivity::class.java).apply {
            putExtra("serviceId", serviceId)
        }
        startActivity(intent)
    }

    private fun loadUserSession() {
        val sharedPref = getSharedPreferences("UserProfiles", Context.MODE_PRIVATE)
        currentUserId = sharedPref.getInt("user_id", 0)
        currentUsername = sharedPref.getString("current_username", "User") ?: "User"
        Log.d(TAG, "User session - ID: $currentUserId, Name: $currentUsername")
    }

    private fun loadUserRatings() {
        if (currentUserId == 0) {
            Log.e(TAG, "User ID not found")
            layoutEmpty.visibility = View.VISIBLE
            return
        }

        progressBar.visibility = View.VISIBLE
        layoutEmpty.visibility = View.GONE

        RetrofitClient.ratingApi.getUserRatings(currentUserId).enqueue(object : Callback<List<Rating>> {
            override fun onResponse(call: Call<List<Rating>>, response: Response<List<Rating>>) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body() != null) {
                    ratingsList.clear()
                    ratingsList.addAll(response.body()!!)
                    ratingsAdapter.updateRatings(ratingsList)

                    updateStats(ratingsList)

                    if (ratingsList.isEmpty()) {
                        layoutEmpty.visibility = View.VISIBLE
                    } else {
                        layoutEmpty.visibility = View.GONE
                    }

                    Log.d(TAG, "Loaded ${ratingsList.size} ratings")
                } else {
                    Log.e(TAG, "Failed to load ratings: ${response.code()}")
                    layoutEmpty.visibility = View.VISIBLE
                    Toast.makeText(this@MyRatingsActivity, "Failed to load ratings", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Rating>>, t: Throwable) {
                progressBar.visibility = View.GONE
                Log.e(TAG, "Error loading ratings", t)
                layoutEmpty.visibility = View.VISIBLE
                Toast.makeText(this@MyRatingsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateStats(ratings: List<Rating>) {
        if (ratings.isEmpty()) {
            tvTotalRatings.text = "0"
            tvAverageRating.text = "0.0"
            displayAverageStars(0.0)
            tvRatingCount.text = "0 items"
            return
        }

        val total = ratings.size
        val sum = ratings.sumOf { it.starRate ?: 0 }
        val average = if (total > 0) sum.toDouble() / total else 0.0

        tvTotalRatings.text = total.toString()
        tvAverageRating.text = String.format("%.1f", average)
        displayAverageStars(average)
        tvRatingCount.text = "$total item${if (total != 1) "s" else ""}"
    }

    private fun displayAverageStars(rating: Double) {
        averageStarsContainer.removeAllViews()

        val fullStars = rating.toInt()
        val hasHalfStar = rating - fullStars >= 0.5

        for (i in 0 until 5) {
            val star = ImageView(this)
            val params = LinearLayout.LayoutParams(dpToPx(16), dpToPx(16))
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

    private fun showDeleteConfirmationDialog(rating: Rating, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Delete Rating")
            .setMessage("Are you sure you want to delete your rating for this service?")
            .setPositiveButton("Delete") { _, _ ->
                deleteRating(rating, position)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteRating(rating: Rating, position: Int) {
        progressBar.visibility = View.VISIBLE

        RetrofitClient.ratingApi.deleteRating(rating.ratingId!!).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!
                    if (result["success"] == true) {
                        ratingsList.removeAt(position)
                        ratingsAdapter.updateRatings(ratingsList)
                        updateStats(ratingsList)

                        Toast.makeText(this@MyRatingsActivity, "Rating deleted successfully", Toast.LENGTH_SHORT).show()

                        if (ratingsList.isEmpty()) {
                            layoutEmpty.visibility = View.VISIBLE
                        }
                    } else {
                        Toast.makeText(this@MyRatingsActivity, "Failed to delete rating", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@MyRatingsActivity, "Failed to delete rating: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@MyRatingsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    // Adapter inner class
    inner class MyRatingsAdapter(
        private var ratings: List<Rating>,
        private val onDeleteClick: (Rating, Int) -> Unit
    ) : RecyclerView.Adapter<MyRatingsAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val cardRoot: CardView = itemView.findViewById(R.id.cardRoot)
            val tvServiceName: TextView = itemView.findViewById(R.id.tvServiceName)
            val tvServiceCategory: TextView = itemView.findViewById(R.id.tvServiceCategory)
            val ratingStarsContainer: LinearLayout = itemView.findViewById(R.id.ratingStarsContainer)
            val tvRatingDate: TextView = itemView.findViewById(R.id.tvRatingDate)
            val tvFeedbackText: TextView = itemView.findViewById(R.id.tvFeedbackText)
            val btnDeleteRating: TextView = itemView.findViewById(R.id.btnDeleteRating)

            init {
                // Make the entire card clickable
                cardRoot.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val rating = ratings[position]
                        rating.serviceId?.let { serviceId ->
                            navigateToRateService(serviceId)
                        }
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_my_rating, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val rating = ratings[position]

            // Load service details from API
            rating.serviceId?.let { serviceId ->
                loadServiceDetails(serviceId, holder, rating)
            }

            // Display rating stars
            displayRatingStars(holder.ratingStarsContainer, rating.starRate ?: 0)

            // Display date
            holder.tvRatingDate.text = rating.dateCreated?.let { formatDate(it) } ?: "Recently"

            // Display feedback text
            val feedbackText = rating.feedbackText
            if (feedbackText.isNullOrEmpty()) {
                holder.tvFeedbackText.visibility = View.GONE
            } else {
                holder.tvFeedbackText.visibility = View.VISIBLE
                holder.tvFeedbackText.text = feedbackText
            }

            // Delete button click - This stops the event from bubbling up to the card click
            holder.btnDeleteRating.setOnClickListener {
                onDeleteClick(rating, position)
            }
        }

        private fun loadServiceDetails(serviceId: Int, holder: ViewHolder, rating: Rating) {
            RetrofitClient.serviceApi.getServiceById(serviceId).enqueue(object : Callback<Service> {
                override fun onResponse(call: Call<Service>, response: Response<Service>) {
                    if (response.isSuccessful && response.body() != null) {
                        val service = response.body()!!
                        holder.tvServiceName.text = service.serviceName
                        holder.tvServiceCategory.text = service.serviceCategory
                    } else {
                        holder.tvServiceName.text = "Service #$serviceId"
                        holder.tvServiceCategory.text = "Unknown Category"
                    }
                }

                override fun onFailure(call: Call<Service>, t: Throwable) {
                    holder.tvServiceName.text = "Service #$serviceId"
                    holder.tvServiceCategory.text = "Unknown Category"
                }
            })
        }

        private fun displayRatingStars(container: LinearLayout, rating: Int) {
            container.removeAllViews()

            for (i in 0 until 5) {
                val star = ImageView(container.context)
                val params = LinearLayout.LayoutParams(dpToPx(18), dpToPx(18))
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

        private fun formatDate(dateString: String): String {
            return try {
                val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
                val outputFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                val date = inputFormat.parse(dateString)
                "Rated on: ${outputFormat.format(date ?: java.util.Date())}"
            } catch (e: Exception) {
                "Recently"
            }
        }

        override fun getItemCount(): Int = ratings.size

        fun updateRatings(newRatings: List<Rating>) {
            ratings = newRatings
            notifyDataSetChanged()
        }

        private fun dpToPx(dp: Int): Int {
            return (dp * resources.displayMetrics.density).toInt()
        }
    }
}