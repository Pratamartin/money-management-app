package com.pratatec.moneymgtapp.wear.domain.model

data class Resumo(
    val saldoCarteira: Double,
    val saldoDisponivelMes: Double,
    val totalGastoMes: Double,
    val limiteHoje: Double,
)

data class Categoria(val id: Int, val nome: String, val tipo: String)

data class Periodo(val id: Int, val mes: Int, val ano: Int)
