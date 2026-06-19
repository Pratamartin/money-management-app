package com.pratatec.moneymgtapp.wear.presentation.login

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pratatec.moneymgtapp.wear.data.local.WearTokenStorage
import com.pratatec.moneymgtapp.wear.data.remote.api.WearAuthApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed class LoginEvent {
    object Success : LoginEvent()
}

class LoginViewModel(app: Application) : AndroidViewModel(app) {

    private val tokenStorage = WearTokenStorage(app)
    private val authApi = WearAuthApi()

    var uiState by mutableStateOf(LoginUiState())
        private set

    private val _events = Channel<LoginEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun updateEmail(v: String) { uiState = uiState.copy(email = v, error = null) }
    fun updatePassword(v: String) { uiState = uiState.copy(password = v, error = null) }

    fun login() {
        if (uiState.isLoading) return
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            authApi.login(uiState.email.trim(), uiState.password)
                .onSuccess { response ->
                    tokenStorage.save(response.access, response.refresh)
                    _events.send(LoginEvent.Success)
                }
                .onFailure {
                    uiState = uiState.copy(isLoading = false, error = "Email ou senha inválidos.")
                }
        }
    }
}

class LoginViewModelFactory(private val app: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
        LoginViewModel(app) as T
}
