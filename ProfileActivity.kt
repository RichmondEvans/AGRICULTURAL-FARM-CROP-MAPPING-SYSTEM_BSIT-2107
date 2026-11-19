
package com.example.agricultural

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.agricultural.databinding.ActivityProfileBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    private lateinit var sessionManager: SessionManager

    private val userDao by lazy {
        AppDatabase.get(this).userDao()
    }

    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        // CHECK LOGIN SESSION
        if (!sessionManager.isLoggedIn()) {
            openLogin()
            return
        }

        loadProfile()

        binding.btnSave.setOnClickListener {
            updateProfile()
        }

        binding.btnLogout.setOnClickListener {
            logoutUser()
        }

        binding.btnRecycleBin.setOnClickListener {
            startActivity(Intent(this, TrashActivity::class.java))
        }
    }

    // LOAD USER PROFILE

    private fun loadProfile() {

        val userId = sessionManager.getUserId()

        lifecycleScope.launch {

            try {

                val user = withContext(Dispatchers.IO) {
                    userDao.getById(userId)
                }

                if (user == null) {
                    Toast.makeText(
                        this@ProfileActivity,
                        "User account not found. Please login again.",
                        Toast.LENGTH_LONG
                    ).show()

                    sessionManager.logout()
                    openLogin()
                    return@launch
                }

                currentUser = user

                binding.tvProfileName.text = user.fullName
                binding.tvProfileEmail.text = user.email
                binding.tvCreatedAt.text =
                    "Member since: ${user.createdAt}"

                binding.etFullName.setText(user.fullName)
                binding.etEmail.setText(user.email)

            } catch (e: Exception) {

                Toast.makeText(
                    this@ProfileActivity,
                    "Unable to load profile: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // UPDATE PROFILE

    private fun updateProfile() {

        val user = currentUser ?: return

        val fullName = binding.etFullName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()

        if (fullName.length < 2) {
            showMessage("Please enter a valid full name")
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showMessage("Please enter a valid email address")
            return
        }

        binding.btnSave.isEnabled = false

        lifecycleScope.launch {

            try {

                val updated = withContext(Dispatchers.IO) {

                    // CHECK IF EMAIL BELONGS TO ANOTHER USER
                    val existingUser = userDao.getByEmail(email)

                    if (existingUser != null && existingUser.id != user.id) {
                        return@withContext false
                    }

                    userDao.updateProfile(
                        id = user.id,
                        fullName = fullName,
                        email = email
                    )
                }

                if (updated) {

                    currentUser = user.copy(
                        fullName = fullName,
                        email = email
                    )

                    binding.tvProfileName.text = fullName
                    binding.tvProfileEmail.text = email

                    showMessage("Profile updated successfully!")

                } else {
                    showMessage(
                        "Email is already used or no changes were saved."
                    )
                }

            } catch (e: Exception) {

                showMessage("Update failed: ${e.message}")

            } finally {

                binding.btnSave.isEnabled = true
            }
        }
    }

    // LOGOUT

    private fun logoutUser() {

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Logout") { _, _ ->

                sessionManager.logout()
                openLogin()
            }
            .show()
    }

    private fun openLogin() {

        val intent = Intent(
            this,
            LoginActivity::class.java
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        startActivity(intent)
        finish()
    }

    private fun showMessage(message: String) {

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }
}
