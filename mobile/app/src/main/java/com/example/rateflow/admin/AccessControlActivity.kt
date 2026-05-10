package com.example.rateflow.admin

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rateflow.R
import com.example.rateflow.auth.User
import com.example.rateflow.core.RetrofitClient
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
        val dialogView = layoutInflater.inflate(R.layout.dialog_role_confirmation, null)

        // Get views
        val tvDialogTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)
        val tvUserName = dialogView.findViewById<TextView>(R.id.tvUserName)
        val tvUserEmail = dialogView.findViewById<TextView>(R.id.tvUserEmail)
        val tvCurrentRole = dialogView.findViewById<TextView>(R.id.tvCurrentRole)
        val tvOldRoleBadge = dialogView.findViewById<TextView>(R.id.tvOldRoleBadge)
        val tvNewRoleBadge = dialogView.findViewById<TextView>(R.id.tvNewRoleBadge)
        val roleIcon = dialogView.findViewById<ImageView>(R.id.roleIcon)
        val iconBackground = dialogView.findViewById<View>(R.id.iconBackground)
        val btnCancel = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCancel)
        val btnConfirm = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnConfirm)

        val isPromoting = newRole.equals("ADMIN", ignoreCase = true)
        val currentRole = user.role ?: "USER"

        // Set user details
        tvUserName.text = user.username ?: "Unknown User"
        tvUserEmail.text = user.email ?: "No email"
        tvCurrentRole.text = currentRole.uppercase()

        // Set role badges
        tvOldRoleBadge.text = currentRole.uppercase()
        tvNewRoleBadge.text = newRole.uppercase()

        // Set badge colors
        if (isPromoting) {
            // Promoting from USER to ADMIN
            tvDialogTitle.text = "Grant Admin Access"
            tvCurrentRole.setTextColor(0xFFFFFFFF.toInt())

            // Set old role badge (USER - gray)
            tvOldRoleBadge.setBackgroundResource(R.drawable.bg_role_badge_user)
            tvOldRoleBadge.setTextColor(0xFFFFFFFF.toInt())

            // Set new role badge (ADMIN - blue)
            tvNewRoleBadge.setBackgroundResource(R.drawable.bg_role_badge_admin)
            tvNewRoleBadge.setTextColor(0xFFFFFFFF.toInt())

            // Update icon and background for admin promotion
            roleIcon.setImageResource(R.drawable.ic_admin_shield)
            roleIcon.setColorFilter(0xFF00BFFF.toInt(), android.graphics.PorterDuff.Mode.SRC_IN)
            (iconBackground.background as? android.graphics.drawable.GradientDrawable)?.setColor(0x1A00BFFF.toInt())

        } else {
            // Demoting from ADMIN to USER
            tvDialogTitle.text = "Demote"
            tvCurrentRole.setTextColor(0xFF00BFFF.toInt())

            // Set old role badge (ADMIN - blue)
            tvOldRoleBadge.setBackgroundResource(R.drawable.bg_role_badge_admin)
            tvOldRoleBadge.setTextColor(0xFFFFFFFF.toInt())

            // Set new role badge (USER - gray)
            tvNewRoleBadge.setBackgroundResource(R.drawable.bg_role_badge_user)
            tvNewRoleBadge.setTextColor(0xFFFFFFFF.toInt())

            // Update icon and background for demotion
            roleIcon.setImageResource(R.drawable.ic_demote)
            roleIcon.setColorFilter(0xFFFF5252.toInt(), android.graphics.PorterDuff.Mode.SRC_IN)
            (iconBackground.background as? android.graphics.drawable.GradientDrawable)?.setColor(0x1AFF5252.toInt())
        }

        // Set confirm button text based on action
        btnConfirm.text = if (isPromoting) "Promote" else "Demote"

        // KEEP THE BUTTON COLOR CONSISTENT - Use your app's primary blue theme
        // This ensures the button doesn't change color for both actions
        btnConfirm.backgroundTintList = android.content.res.ColorStateList.valueOf(0xFF00BFFF.toInt())

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            dialog.dismiss()
            changeUserRole(user, newRole)
        }
    }

    private fun showRoleChangeSuccessDialog(userName: String, isPromoted: Boolean) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_role_change_success, null)
        val tvSuccessMessage = dialogView.findViewById<TextView>(R.id.tvSuccessMessage)
        val btnOK = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnOK)

        val message = if (isPromoted) {
            "$userName has been granted admin access successfully!"
        } else {
            "Admin access has been removed from $userName successfully!"
        }
        tvSuccessMessage.text = message

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        btnOK.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun changeUserRole(user: User, newRole: String) {
        progressDialog.setMessage("Updating user role...")
        progressDialog.show()

        val isPromoting = newRole.equals("ADMIN", ignoreCase = true)

        val call = if (isPromoting) {
            RetrofitClient.userManagementApi.grantAdminAccess(user.id!!)
        } else {
            RetrofitClient.userManagementApi.removeAdminAccess(user.id!!)
        }

        call.enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                progressDialog.dismiss()

                if (response.isSuccessful) {
                    showRoleChangeSuccessDialog(user.username ?: "User", isPromoting)
                    loadUsers()
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

            when (user.role?.uppercase()) {
                "ADMIN" -> {
                    tvRole.setTextColor(0xFF00BFFF.toInt())
                    btnChangeRole.text = "Demote"
                }
                else -> {
                    tvRole.setTextColor(0xFFFFFFFF.toInt())
                    btnChangeRole.text = "Make Admin"
                }
            }

            btnChangeRole.isEnabled = currentUserId != user.id

            btnChangeRole.setOnClickListener {
                val newRole = if (user.role.equals("ADMIN", ignoreCase = true)) "USER" else "ADMIN"
                onRoleChange(user, newRole)
            }
        }
    }
}