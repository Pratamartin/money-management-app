package com.pratatec.moneymgtapp.wear.data.local

import android.content.Context
import android.content.SharedPreferences

class WearPinStorage(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("wear_pin", Context.MODE_PRIVATE)

    fun savePin(hash: String) {
        prefs.edit()
            .putString(KEY_HASH, hash)
            .putInt(KEY_ATTEMPTS, 0)
            .apply()
    }

    fun getPinHash(): String? = prefs.getString(KEY_HASH, null)
    fun hasPin(): Boolean = getPinHash() != null

    fun getAttempts(): Int = prefs.getInt(KEY_ATTEMPTS, 0)

    fun incrementAttempts() {
        prefs.edit().putInt(KEY_ATTEMPTS, getAttempts() + 1).apply()
    }

    fun resetAttempts() {
        prefs.edit().putInt(KEY_ATTEMPTS, 0).apply()
    }

    fun isLocked(): Boolean = getAttempts() >= MAX_ATTEMPTS

    companion object {
        private const val KEY_HASH = "pin_hash"
        private const val KEY_ATTEMPTS = "attempts"
        const val MAX_ATTEMPTS = 5
    }
}
