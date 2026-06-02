package com.pratatec.moneymgtapp.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(val access: String, val refresh: String)
