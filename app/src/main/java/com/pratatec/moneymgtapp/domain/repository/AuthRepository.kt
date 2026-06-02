package com.pratatec.moneymgtapp.domain.repository

import com.pratatec.moneymgtapp.data.remote.dto.LoginResponse
import com.pratatec.moneymgtapp.data.remote.dto.RegisterResponse

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<LoginResponse>
    suspend fun register(email: String, username: String, nome: String, password: String): Result<RegisterResponse>
    suspend fun hasValidSession(): Boolean
    suspend fun clearTokens()
}
