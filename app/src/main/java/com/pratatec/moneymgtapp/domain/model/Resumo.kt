package com.pratatec.moneymgtapp.domain.model

data class Resumo(
    val saldoCarteira: Double,
    val saldoDisponivelMes: Double,
    val totalGastoMes: Double,
    val limiteHoje: Double,
)
