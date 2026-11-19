
package com.example.agricultural

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordHasher {

    private const val ITERATIONS = 210000
    private const val KEY_LENGTH = 256
    private const val ALGORITHM = "PBKDF2WithHmacSHA1"

    fun hash(password: String): String {

        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)

        val hash = generateHash(password, salt, ITERATIONS)

        val encodedSalt = Base64.encodeToString(
            salt,
            Base64.NO_WRAP
        )

        val encodedHash = Base64.encodeToString(
            hash,
            Base64.NO_WRAP
        )

        return "pbkdf2_sha1\$$ITERATIONS\$$encodedSalt\$$encodedHash"
    }

    fun verify(password: String, storedHash: String): Boolean {

        return try {

            val parts = storedHash.split("$")

            if (parts.size != 4 || parts[0] != "pbkdf2_sha1") {
                return false
            }

            val iterations = parts[1].toInt()

            if (iterations !in 100000..1000000) {
                return false
            }

            val salt = Base64.decode(parts[2], Base64.NO_WRAP)
            val expectedHash = Base64.decode(parts[3], Base64.NO_WRAP)

            val actualHash = generateHash(
                password,
                salt,
                iterations
            )

            MessageDigest.isEqual(expectedHash, actualHash)

        } catch (e: Exception) {
            false
        }
    }

    private fun generateHash(
        password: String,
        salt: ByteArray,
        iterations: Int
    ): ByteArray {

        val spec = PBEKeySpec(
            password.toCharArray(),
            salt,
            iterations,
            KEY_LENGTH
        )

        return try {
            SecretKeyFactory.getInstance(ALGORITHM)
                .generateSecret(spec)
                .encoded
        } finally {
            spec.clearPassword()
        }
    }
}
