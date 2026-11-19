
package com.example.agricultural

import android.content.Context

class SessionManager(context: Context) {

    private val preferences = context.getSharedPreferences(
        "AgriFarmSession",
        Context.MODE_PRIVATE
    )

    fun saveLogin(userId: Int) {
        preferences.edit()
            .putInt("USER_ID", userId)
            .putBoolean("IS_LOGGED_IN", true)
            .apply()
    }

    fun isLoggedIn(): Boolean {
        return preferences.getBoolean("IS_LOGGED_IN", false)
    }

    fun getUserId(): Int {
        return preferences.getInt("USER_ID", -1)
    }

    fun logout() {
        preferences.edit()
            .clear()
            .apply()
    }
}
