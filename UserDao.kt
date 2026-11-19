
package com.example.agricultural

import android.content.ContentValues
import android.database.sqlite.SQLiteConstraintException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

interface UserDataSource {
    fun register(
        fullName: String,
        email: String,
        passwordHash: String
    ): Long

    fun getByEmail(email: String): User?
    fun getById(id: Int): User?

    fun updateProfile(
        id: Int,
        fullName: String,
        email: String
    ): Boolean
}

class UserDao(
    private val dbHelper: AppDatabase
) : UserDataSource {

    override fun register(
        fullName: String,
        email: String,
        passwordHash: String
    ): Long {
        val values = ContentValues().apply {
            put("fullName", fullName.trim())
            put("email", email.trim())
            put("passwordHash", passwordHash)
            put(
                "createdAt",
                SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss",
                    Locale.getDefault()
                ).format(Date())
            )
        }

        return dbHelper.writableDatabase.insert(
            "users",
            null,
            values
        )
    }

    override fun getByEmail(email: String): User? {
        dbHelper.readableDatabase.query(
            "users",
            null,
            "email = ?",
            arrayOf(email.trim()),
            null,
            null,
            null
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                return readUser(cursor)
            }
        }

        return null
    }

    override fun getById(id: Int): User? {
        dbHelper.readableDatabase.query(
            "users",
            null,
            "id = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                return readUser(cursor)
            }
        }

        return null
    }

    override fun updateProfile(
        id: Int,
        fullName: String,
        email: String
    ): Boolean {
        val values = ContentValues().apply {
            put("fullName", fullName.trim())
            put("email", email.trim())
        }

        return try {
            dbHelper.writableDatabase.update(
                "users",
                values,
                "id = ?",
                arrayOf(id.toString())
            ) > 0
        } catch (e: SQLiteConstraintException) {
            false
        }
    }

    private fun readUser(
        cursor: android.database.Cursor
    ): User {
        return User(
            id = cursor.getInt(
                cursor.getColumnIndexOrThrow("id")
            ),
            fullName = cursor.getString(
                cursor.getColumnIndexOrThrow("fullName")
            ),
            email = cursor.getString(
                cursor.getColumnIndexOrThrow("email")
            ),
            passwordHash = cursor.getString(
                cursor.getColumnIndexOrThrow("passwordHash")
            ),
            createdAt = cursor.getString(
                cursor.getColumnIndexOrThrow("createdAt")
            )
        )
    }
}
