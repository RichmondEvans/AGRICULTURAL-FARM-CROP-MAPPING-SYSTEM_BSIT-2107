package com.example.agricultural


abstract class FarmScopedActivity : BaseActivity() {

    protected var farmId: Int = -1
        private set

    protected fun resolveFarmId(): Boolean {
        farmId = intent.getIntExtra(EXTRA_FARM_ID, -1)

        if (farmId <= 0) {
            toast("Invalid Farm ID", long = true)
            finish()
            return false
        }

        return true
    }

    protected fun hasValidFarm(): Boolean = farmId > 0

    companion object {
        const val EXTRA_FARM_ID = "farm_id"
    }
}
