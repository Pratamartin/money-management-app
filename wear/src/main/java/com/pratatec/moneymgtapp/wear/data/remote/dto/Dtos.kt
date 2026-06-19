package com.pratatec.moneymgtapp.wear.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TokenRefreshResponse(val access: String, val refresh: String? = null)

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class LoginResponse(val access: String, val refresh: String)

@Serializable
data class ResumoResponse(
    val saldo_carteira: Double,
    val saldo_disponivel_mes: Double,
    val total_gasto_mes: Double,
    val limite_hoje: Double,
)

@Serializable
data class CategoriaResponse(val id: Int, val nome: String, val tipo: String)

@Serializable
data class GastoDiarioRequest(
    val data: String,
    val valor: String,
    val categoria_id: Int,
    val descricao: String,
)

@Serializable
data class PeriodoResponse(
    val id: Int,
    val mes: Int,
    val ano: Int,
    val saldo_carteira: String,
    val saldo_disponivel_mes: String,
)
