package com.pratatec.moneymgtapp.domain.model

data class GastoDiario(
    val id: Int,
    val data: String,
    val valor: Double,
    val descricao: String,
    val categoria: Categoria,
)
