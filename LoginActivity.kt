
package com.example.agricultural

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.agricultural.databinding.ActivityLoginBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val userDao by lazy {
        AppDatabase.get(this).userDao()
    }

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)

        if (sessionManager.isLoggedIn()) {
            openHome()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            loginUser()
        }

        binding.tvRegister.setOnClickListener {
            startActivity(
                Intent(this, RegisterActivity::class.java)
            )
        }
    }

    private fun loginUser() {

        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(
                this,
                "Please enter your email and password",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        binding.btnLogin.isEnabled = false

        lifecycleScope.launch {

            try {
                val user = withContext(Dispatchers.IO) {
                    userDao.getByEmail(email)
                }

                if (user != null &&
                    PasswordHasher.verify(
                        password,
                        user.passwordHash
                    )
                ) {

                    sessionManager.saveLogin(user.id)

                    Toast.makeText(
                        this@LoginActivity,
                        "Welcome, ${user.fullName}!",
                        Toast.LENGTH_SHORT
                    ).show()

                    openHome()

                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        "Invalid email or password",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Toast.makeText(
                    this@LoginActivity,
                    "Login error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                binding.btnLogin.isEnabled = true
            }
        }
    }

    private fun openHome() {

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        startActivity(intent)
        finish()
    }
}
