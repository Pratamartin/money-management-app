package com.pratatec.moneymgtapp.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CategoriaResponse(
    val id: Int,
    val nome: String,
    val tipo: String,
)
