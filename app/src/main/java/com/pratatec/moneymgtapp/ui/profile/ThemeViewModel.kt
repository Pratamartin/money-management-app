package com.pratatec.moneymgtapp.ui.profile

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.pratatec.moneymgtapp.ui.theme.AppTheme

class ThemeViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    var appTheme: AppTheme by mutableStateOf(
        AppTheme.valueOf(prefs.getString("app_theme", AppTheme.DARK.name) ?: AppTheme.DARK.name)
    )
        private set

    fun setTheme(theme: AppTheme) {
        appTheme = theme
        prefs.edit().putString("app_theme", theme.name).apply()
    }
}
