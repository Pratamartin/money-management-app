package com.pratatec.moneymgtapp.wear.presentation.pin

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import com.pratatec.moneymgtapp.wear.data.local.WearPinSession
import com.pratatec.moneymgtapp.wear.data.local.WearPinStorage
import java.security.MessageDigest

data class PinUiState(
    val digits: String = "",
    val attemptsLeft: Int = WearPinStorage.MAX_ATTEMPTS,
    val locked: Boolean = false,
    val error: Boolean = false,
)

class PinViewModel(app: Application) : AndroidViewModel(app) {

    private val storage = WearPinStorage(app)

    var uiState by mutableStateOf(
        PinUiState(
            attemptsLeft = WearPinStorage.MAX_ATTEMPTS - storage.getAttempts(),
            locked = storage.isLocked(),
        )
    )
        private set

    fun onDigit(d: String) {
        if (uiState.locked || uiState.digits.length >= 4) return
        val next = uiState.digits + d
        uiState = uiState.copy(digits = next, error = false)
        if (next.length == 4) validate(next)
    }

    fun onDelete() {
        if (uiState.digits.isEmpty()) return
        uiState = uiState.copy(digits = uiState.digits.dropLast(1), error = false)
    }

    private fun validate(pin: String) {
        val hash = sha256(pin)
        if (hash == storage.getPinHash()) {
            storage.resetAttempts()
            WearPinSession.unlocked = true
        } else {
            storage.incrementAttempts()
            val left = WearPinStorage.MAX_ATTEMPTS - storage.getAttempts()
            uiState = uiState.copy(
                digits = "",
                error = true,
                attemptsLeft = left,
                locked = storage.isLocked(),
            )
        }
    }

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(input.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}

class PinViewModelFactory(private val app: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
        PinViewModel(app) as T
}
