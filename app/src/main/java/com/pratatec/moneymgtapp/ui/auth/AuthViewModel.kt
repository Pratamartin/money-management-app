package com.pratatec.moneymgtapp.ui.auth

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pratatec.moneymgtapp.data.local.TokenStorage
import com.pratatec.moneymgtapp.data.remote.api.AuthApi
import com.pratatec.moneymgtapp.data.remote.api.KtorClient
import com.pratatec.moneymgtapp.data.repository.AuthRepositoryImpl
import com.pratatec.moneymgtapp.domain.repository.AuthRepository
import com.pratatec.moneymgtapp.domain.usecase.LoginUseCase
import com.pratatec.moneymgtapp.domain.usecase.RegisterUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)

data class RegisterUiState(
    val nome: String = "",
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val fieldErrors: Map<String, String> = emptyMap(),
    val error: String? = null,
)

sealed class AuthEvent {
    object NavigateToHome : AuthEvent()
    object NavigateToLogin : AuthEvent()
}

class AuthViewModel(app: Application) : AndroidViewModel(app) {

    private val tokenStorage = TokenStorage(app)
    private val httpClient = KtorClient.create(tokenStorage)
    private val authApi = AuthApi(httpClient)
    private val repository: AuthRepository = AuthRepositoryImpl(authApi, tokenStorage)
    private val loginUseCase = LoginUseCase(repository)
    private val registerUseCase = RegisterUseCase(repository)

    var loginState by mutableStateOf(LoginUiState())
        private set

    var registerState by mutableStateOf(RegisterUiState())
        private set

    private val _events = Channel<AuthEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // --- Login ---

    fun updateEmail(value: String) { loginState = loginState.copy(email = value, error = null) }
    fun updatePassword(value: String) { loginState = loginState.copy(password = value, error = null) }

    fun login() {
        if (loginState.isLoading) return
        viewModelScope.launch {
            loginState = loginState.copy(isLoading = true, error = null)
            loginUseCase(loginState.email, loginState.password)
                .onSuccess { _events.send(AuthEvent.NavigateToHome) }
                .onFailure { loginState = loginState.copy(error = "Email ou senha inválidos.") }
            loginState = loginState.copy(isLoading = false)
        }
    }

    // --- Register ---

    fun updateNome(value: String) { registerState = registerState.copy(nome = value) }
    fun updateUsername(value: String) { registerState = registerState.copy(username = value) }
    fun updateRegisterEmail(value: String) { registerState = registerState.copy(email = value) }
    fun updateRegisterPassword(value: String) { registerState = registerState.copy(password = value) }
    fun updateConfirmPassword(value: String) { registerState = registerState.copy(confirmPassword = value) }

    fun register() {
        if (registerState.isLoading) return
        val errors = validateRegister()
        if (errors.isNotEmpty()) {
            registerState = registerState.copy(fieldErrors = errors)
            return
        }
        viewModelScope.launch {
            registerState = registerState.copy(isLoading = true, error = null, fieldErrors = emptyMap())
            registerUseCase(
                email = registerState.email,
                username = registerState.username,
                nome = registerState.nome,
                password = registerState.password,
            ).onSuccess {
                _events.send(AuthEvent.NavigateToLogin)
            }.onFailure {
                registerState = registerState.copy(error = "Erro ao criar conta. Tente novamente.")
            }
            registerState = registerState.copy(isLoading = false)
        }
    }

    private fun validateRegister(): Map<String, String> {
        val s = registerState
        return buildMap {
            if (s.nome.trim().length < 2) put("nome", "Nome muito curto")
            if (s.username.trim().length < 3) put("username", "Mínimo 3 caracteres")
            if (s.username.contains(" ")) put("username", "Sem espaços")
            if (!s.email.contains("@")) put("email", "Email inválido")
            if (s.password.length < 8) put("password", "Mínimo 8 caracteres")
            if (s.password != s.confirmPassword) put("confirmPassword", "Senhas não coincidem")
        }
    }

    // --- Splash ---

    suspend fun hasValidSession(): Boolean = repository.hasValidSession()

    override fun onCleared() {
        super.onCleared()
        httpClient.close()
    }
}
