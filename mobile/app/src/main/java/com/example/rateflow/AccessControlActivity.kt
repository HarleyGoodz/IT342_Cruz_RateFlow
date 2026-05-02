package com.example.rateflow

import android.app.ProgressDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rateflow.model.User
import com.example.rateflow.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AccessControlActivity : AppCompatActivity() {

    private lateinit var recyclerUsers: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private lateinit var progressDialog: ProgressDialog
    private lateinit var btnBack: ImageButton
    private lateinit var tvTitle: TextView
    private lateinit var etSearch: EditText
    private lateinit var tvUserCount: TextView

    private var usersList = mutableListOf<User>()
    private var filteredUsersList = mutableListOf<User>()
    private var currentUserId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_access_control)

        initializeViews()
        setupRecyclerView()
        setupClickListeners()
        setupSearchListener()
        loadUsers()
    }

    private fun initializeViews() {
        recyclerUsers = findViewById(R.id.recyclerUsers)
        btnBack = findViewById(R.id.btnBack)
        tvTitle = findViewById(R.id.tvTitle)
        etSearch = findViewById(R.id.etSearch)
        tvUserCount = findViewById(R.id.tvUserCount)

        progressDialog = ProgressDialog(this).apply {
            setMessage("Loading users...")
            setCancelable(false)
        }
    }

    private fun setupRecyclerView() {
        recyclerUsers.layoutManager = LinearLayoutManager(this)
        userAdapter = UserAdapter(
            users = filteredUsersList,
            currentUserId = getCurrentUserId(),
            onRoleChange = { user, newRole ->
                showRoleChangeConfirmation(user, newRole)
            }
        )
        recyclerUsers.adapter = userAdapter
    }

    private fun getCurrentUserId(): Int? {
        // Get current user ID from SharedPreferences or session
        val sharedPref = getSharedPreferences("RateFlowPrefs", Context.MODE_PRIVATE)
        return sharedPref.getInt("userId", -1).takeIf { it != -1 }
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupSearchListener() {
        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterUsers(s.toString())
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun filterUsers(query: String) {
        if (query.isEmpty()) {
            filteredUsersList.clear()
            filteredUsersList.addAll(usersList)
        } else {
            val filtered = usersList.filter { user ->
                user.username?.contains(query, ignoreCase = true) == true ||
                        user.email?.contains(query, ignoreCase = true) == true
            }
            filteredUsersList.clear()
            filteredUsersList.addAll(filtered)
        }
        userAdapter.updateUsers(filteredUsersList)
        updateUserCount()
    }

    private fun loadUsers() {
        progressDialog.show()

        RetrofitClient.userManagementApi.getUsers().enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                progressDialog.dismiss()

                if (response.isSuccessful) {
                    response.body()?.let { users ->
                        usersList.clear()
                        usersList.addAll(users)
                        filteredUsersList.clear()
                        filteredUsersList.addAll(users)
                        userAdapter.updateUsers(filteredUsersList)
                        updateUserCount()
                    }
                } else {
                    showError("Failed to load users: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                progressDialog.dismiss()
                showError("Error: ${t.message}")
            }
        })
    }

    private fun updateUserCount() {
        tvUserCount.text = "${filteredUsersList.size} user${if (filteredUsersList.size != 1) "s" else ""}"
    }

    private fun showRoleChangeConfirmation(user: User, newRole: String) {
        val isPromoting = newRole.equals("ADMIN", ignoreCase = true)
        val message = if (isPromoting) {
            "Are you sure you want to make ${user.username} an admin?"
        } else {
            "Are you sure you want to remove admin access from ${user.username}?"
        }

        AlertDialog.Builder(this)
            .setTitle("Confirm Role Change")
            .setMessage(message)
            .setPositiveButton("Yes") { _, _ ->
                changeUserRole(user, newRole)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun changeUserRole(user: User, newRole: String) {
        progressDialog.setMessage("Updating user role...")
        progressDialog.show()

        val call = if (newRole.equals("ADMIN", ignoreCase = true)) {
            RetrofitClient.userManagementApi.grantAdminAccess(user.id!!)
        } else {
            RetrofitClient.userManagementApi.removeAdminAccess(user.id!!)
        }

        call.enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                progressDialog.dismiss()

                if (response.isSuccessful) {
                    Toast.makeText(
                        this@AccessControlActivity,
                        "User role updated successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadUsers() // Refresh the list
                } else {
                    showError("Failed to update user role: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                progressDialog.dismiss()
                showError("Error: ${t.message}")
            }
        })
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}

// User Adapter Class
class UserAdapter(
    private var users: List<User>,
    private val currentUserId: Int?,
    private val onRoleChange: (User, String) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size

    fun updateUsers(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        private val tvEmail: TextView = itemView.findViewById(R.id.tvEmail)
        private val tvRole: TextView = itemView.findViewById(R.id.tvRole)
        private val btnChangeRole: Button = itemView.findViewById(R.id.btnChangeRole)

        fun bind(user: User) {
            tvUsername.text = user.username ?: "No username"
            tvEmail.text = user.email ?: "No email"
            tvRole.text = user.role ?: "USER"

            // Set role text color based on role
            when (user.role?.uppercase()) {
                "ADMIN" -> {
                    tvRole.setTextColor(0xFF00BFFF.toInt())
                    btnChangeRole.text = "Demote to User"
                }
                else -> {
                    tvRole.setTextColor(0xFFFFFFFF.toInt())
                    btnChangeRole.text = "Make Admin"
                }
            }

            // Disable role change for current user (can't change own role)
            btnChangeRole.isEnabled = currentUserId != user.id

            btnChangeRole.setOnClickListener {
                val newRole = if (user.role.equals("ADMIN", ignoreCase = true)) "USER" else "ADMIN"
                onRoleChange(user, newRole)
            }
        }
    }
}

// Extension function for ApiService to add user management endpoints
// Add these functions to your RetrofitClient or create a new interface