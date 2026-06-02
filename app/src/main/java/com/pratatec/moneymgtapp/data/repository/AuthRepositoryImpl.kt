package com.pratatec.moneymgtapp.data.repository

import com.pratatec.moneymgtapp.data.local.TokenStorage
import com.pratatec.moneymgtapp.data.remote.api.AuthApi
import com.pratatec.moneymgtapp.data.remote.dto.LoginRequest
import com.pratatec.moneymgtapp.data.remote.dto.LoginResponse
import com.pratatec.moneymgtapp.data.remote.dto.RegisterRequest
import com.pratatec.moneymgtapp.data.remote.dto.RegisterResponse
import com.pratatec.moneymgtapp.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val api: AuthApi,
    private val tokenStorage: TokenStorage,
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<LoginResponse> {
        return api.login(LoginRequest(email, password)).onSuccess { response ->
            tokenStorage.save(response.access, response.refresh)
        }
    }

    override suspend fun register(
        email: String,
        username: String,
        nome: String,
        password: String,
    ): Result<RegisterResponse> {
        return api.register(RegisterRequest(email, username, nome, password))
    }

    override suspend fun hasValidSession(): Boolean = tokenStorage.hasTokens()

    override suspend fun clearTokens() = tokenStorage.clear()
}
