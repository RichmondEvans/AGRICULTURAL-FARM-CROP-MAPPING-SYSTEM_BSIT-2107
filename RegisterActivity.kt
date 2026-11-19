
package com.example.agricultural

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.agricultural.databinding.ActivityRegisterBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    private val userDao by lazy {
        AppDatabase.get(this).userDao()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            registerUser()
        }

        binding.tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun registerUser() {

        val fullName = binding.etFullName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirmPassword =
            binding.etConfirmPassword.text.toString()

        // CHECK EMPTY FIELDS

        if (
            fullName.isEmpty() ||
            email.isEmpty() ||
            password.isEmpty() ||
            confirmPassword.isEmpty()
        ) {
            showMessage("Please complete all fields")
            return
        }

        // CHECK NAME

        if (fullName.length < 2) {
            showMessage("Please enter a valid full name")
            return
        }

        // CHECK EMAIL

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showMessage("Please enter a valid email address")
            return
        }

        // CHECK PASSWORD LENGTH

        if (password.length < 8) {
            showMessage("Password must be at least 8 characters")
            return
        }

        // CHECK PASSWORD MATCH

        if (password != confirmPassword) {
            showMessage("Passwords do not match")
            return
        }

        binding.btnRegister.isEnabled = false

        lifecycleScope.launch {

            try {

                val result = withContext(Dispatchers.IO) {

                    // CHECK EXISTING EMAIL

                    val existingUser = userDao.getByEmail(email)

                    if (existingUser != null) {
                        return@withContext -1L
                    }

                    // HASH PASSWORD

                    val hashedPassword = PasswordHasher.hash(password)

                    // SAVE USER

                    userDao.register(
                        fullName = fullName,
                        email = email,
                        passwordHash = hashedPassword
                    )
                }

                when {
                    result > 0 -> {

                        Toast.makeText(
                            this@RegisterActivity,
                            "Registration successful! Please login.",
                            Toast.LENGTH_LONG
                        ).show()

                        val intent = Intent(
                            this@RegisterActivity,
                            LoginActivity::class.java
                        )

                        intent.flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                    Intent.FLAG_ACTIVITY_SINGLE_TOP

                        startActivity(intent)
                        finish()
                    }

                    result == -1L -> {
                        showMessage("Email is already registered")
                    }

                    else -> {
                        showMessage("Registration failed. Please try again.")
                    }
                }

            } catch (e: Exception) {

                showMessage("Registration error: ${e.message}")

            } finally {

                binding.btnRegister.isEnabled = true
            }
        }
    }

    private fun showMessage(message: String) {

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }
}
