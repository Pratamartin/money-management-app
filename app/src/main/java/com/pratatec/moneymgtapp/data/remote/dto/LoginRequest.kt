package com.pratatec.moneymgtapp.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val email: String, val password: String)
