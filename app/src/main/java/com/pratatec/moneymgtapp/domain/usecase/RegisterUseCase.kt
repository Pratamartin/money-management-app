package com.pratatec.moneymgtapp.domain.usecase

import com.pratatec.moneymgtapp.domain.repository.AuthRepository

class RegisterUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(
        email: String,
        username: String,
        nome: String,
        password: String,
    ) = repository.register(email, username, nome, password)
}
