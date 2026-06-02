package com.pratatec.moneymgtapp.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterResponse(
    val id: Int,
    val email: String,
    val username: String,
    val nome: String,
)
