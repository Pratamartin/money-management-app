package com.pratatec.moneymgtapp.domain.model

data class Periodo(
    val id: Int,
    val mes: Int,
    val ano: Int,
    val saldoCarteira: Double,
    val saldoDisponivelMes: Double,
)
