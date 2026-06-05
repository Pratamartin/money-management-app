package com.pratatec.moneymgtapp.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PeriodoRequest(
    val mes: Int,
    val ano: Int,
    val saldo_carteira: String,
    val saldo_disponivel_mes: String,
)
