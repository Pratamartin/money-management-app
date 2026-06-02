package com.pratatec.moneymgtapp.data.local

import android.content.Context
import android.content.SharedPreferences

class TokenStorage(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("auth_tokens", Context.MODE_PRIVATE)

    fun save(access: String, refresh: String) {
        prefs.edit()
            .putString(KEY_ACCESS, access)
            .putString(KEY_REFRESH, refresh)
            .apply()
    }

    fun getAccess(): String? = prefs.getString(KEY_ACCESS, null)
    fun getRefresh(): String? = prefs.getString(KEY_REFRESH, null)
    fun hasTokens(): Boolean = getAccess() != null
    fun clear() { prefs.edit().clear().apply() }

    companion object {
        private const val KEY_ACCESS = "access"
        private const val KEY_REFRESH = "refresh"
    }
}
