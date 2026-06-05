package com.pratatec.moneymgtapp.domain.model

data class GastoDoDia(
    val data: String,
    val gastos: List<GastoDiario>,
    val totalDia: Double,
)
