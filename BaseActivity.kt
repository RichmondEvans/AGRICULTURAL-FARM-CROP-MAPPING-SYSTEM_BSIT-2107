package com.example.agricultural

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * ABSTRACTION + INHERITANCE
 *
 * Parent of the screens that need the database, toasts and safe
 * coroutine launching, so these are written once instead of in
 * every Activity.
 */
abstract class BaseActivity : AppCompatActivity() {

    protected val database: AppDatabase by lazy { AppDatabase.get(this) }

    protected fun toast(message: String, long: Boolean = false) {
        Toast.makeText(
            this,
            message,
            if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        ).show()
    }

    /** Launches work on the lifecycle scope and shows any error as a toast. */
    protected fun launchSafely(
        errorPrefix: String,
        block: suspend CoroutineScope.() -> Unit
    ): Job = lifecycleScope.launch {
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            toast("$errorPrefix: ${e.message}", long = true)
        }
    }
}
