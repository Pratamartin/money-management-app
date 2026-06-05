package com.pratatec.moneymgtapp.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class GastoDiarioRequest(
    val data: String,
    val valor: String,
    val categoria_id: Int,
    val descricao: String = "",
)
