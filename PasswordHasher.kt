package com.example.agricultural

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

// Helper class to safely encrypt and check user passwords
object PasswordHasher {

    private const val ITERATIONS = 210000
    private const val KEY_LENGTH = 256
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"

    // Encrypts the password with a random salt before saving to the database
    fun hash(password: String): String {
        val salt = ByteArray(16).apply {
            SecureRandom().nextBytes(this)
        }

        val hash = createHash(password, salt, ITERATIONS)
        val saltText = Base64.encodeToString(salt, Base64.NO_WRAP)
        val hashText = Base64.encodeToString(hash, Base64.NO_WRAP)

        return "pbkdf2_sha256\$$ITERATIONS\$$saltText\$$hashText"
    }

    // Checks if the password entered during login matches the saved password
    fun verify(password: String, savedHash: String): Boolean {
        return try {
            val parts = savedHash.split("$")
            if (parts.size != 4) return false

            val (algo, iterText, saltText, hashText) = parts
            if (algo != "pbkdf2_sha256") return false

            val iterations = iterText.toInt()
            val salt = Base64.decode(saltText, Base64.NO_WRAP)
            val oldHash = Base64.decode(hashText, Base64.NO_WRAP)
            val newHash = createHash(password, salt, iterations)

            MessageDigest.isEqual(oldHash, newHash)
        } catch (_: Exception) {
            false
        }
    }

    private fun createHash(password: String, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, KEY_LENGTH)
        return try {
            SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }
}