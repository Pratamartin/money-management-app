package com.pratatec.moneymgtapp.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class GastoDiarioResponse(
    val id: Int,
    val data: String,
    val valor: String,
    val descricao: String,
    val categoria: CategoriaResponse,
)
