package com.pratatec.moneymgtapp.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val username: String,
    val nome: String,
    val password: String,
)
