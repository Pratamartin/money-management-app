package com.pratatec.moneymgtapp.ui.profile

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pratatec.moneymgtapp.sync.WearTokenSync
import com.pratatec.moneymgtapp.sync.hashPin
import kotlinx.coroutines.launch

data class PinSheetState(
    val visible: Boolean = false,
    val pin: String = "",
    val confirmPin: String = "",
    val isSaving: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
)

class PinViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences("wear_pin_prefs", Context.MODE_PRIVATE)

    var sheet by mutableStateOf(PinSheetState())
        private set

    val hasPin: Boolean get() = prefs.getString("pin_hash", null) != null

    fun openSheet() { sheet = PinSheetState(visible = true) }
    fun closeSheet() { sheet = PinSheetState() }

    fun updatePin(value: String) {
        if (value.length <= 4 && value.all { it.isDigit() })
            sheet = sheet.copy(pin = value, error = null)
    }

    fun updateConfirmPin(value: String) {
        if (value.length <= 4 && value.all { it.isDigit() })
            sheet = sheet.copy(confirmPin = value, error = null)
    }

    fun save() {
        val pin = sheet.pin
        if (pin.length < 4) {
            sheet = sheet.copy(error = "O PIN deve ter 4 dígitos.")
            return
        }
        if (pin != sheet.confirmPin) {
            sheet = sheet.copy(error = "Os PINs não coincidem.")
            return
        }
        viewModelScope.launch {
            sheet = sheet.copy(isSaving = true)
            val hash = hashPin(pin)
            prefs.edit().putString("pin_hash", hash).apply()
            WearTokenSync.pushPin(getApplication(), hash)
            sheet = sheet.copy(isSaving = false, success = true)
        }
    }
}
