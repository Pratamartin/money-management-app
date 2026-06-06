package com.pratatec.moneymgtapp.data.remote.api

import com.pratatec.moneymgtapp.data.remote.dto.CategoriaResponse
import com.pratatec.moneymgtapp.data.remote.dto.GastoDiarioRequest
import com.pratatec.moneymgtapp.data.remote.dto.GastoDiarioResponse
import com.pratatec.moneymgtapp.data.remote.dto.PeriodoRequest
import com.pratatec.moneymgtapp.data.remote.dto.PeriodoResponse
import com.pratatec.moneymgtapp.data.remote.dto.ResumoResponse
import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class FinanceApi(private val client: HttpClient) {

    private val jsonParser = Json { ignoreUnknownKeys = true }

    // ── Categorias ─────────────────────────────────────────────────────────

    suspend fun getCategorias(): Result<List<CategoriaResponse>> = runCatching {
        client.get("${KtorClient.BASE_URL}categorias/").body()
    }

    suspend fun createCategoria(nome: String): Result<CategoriaResponse> = runCatching {
        client.post("${KtorClient.BASE_URL}categorias/") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("nome" to nome))
        }.body()
    }

    suspend fun deleteCategoria(id: Int): Result<Unit> = runCatching {
        val response = client.delete("${KtorClient.BASE_URL}categorias/$id/")
        if (!response.status.isSuccess()) throw Exception("HTTP ${response.status.value}")
    }

    // ── Períodos ────────────────────────────────────────────────────────────

    suspend fun getPeriodos(): Result<List<PeriodoResponse>> = runCatching {
        val raw = client.get("${KtorClient.BASE_URL}periodos/").bodyAsText()
        Log.d("FinanceApi", "getPeriodos → $raw")
        jsonParser.decodeFromString(raw)
    }

    suspend fun createPeriodo(request: PeriodoRequest): Result<PeriodoResponse> = runCatching {
        val raw = client.post("${KtorClient.BASE_URL}periodos/") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.bodyAsText()
        Log.d("FinanceApi", "createPeriodo → $raw")
        jsonParser.decodeFromString(raw)
    }

    suspend fun updatePeriodo(
        periodoId: Int,
        saldoCarteira: String? = null,
        saldoDisponivelMes: String? = null,
    ): Result<PeriodoResponse> = runCatching {
        val raw = client.patch("${KtorClient.BASE_URL}periodos/$periodoId/") {
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject {
                saldoCarteira?.let { put("saldo_carteira", it) }
                saldoDisponivelMes?.let { put("saldo_disponivel_mes", it) }
            })
        }.bodyAsText()
        jsonParser.decodeFromString<PeriodoResponse>(raw)
    }

    suspend fun getResumo(periodoId: Int): Result<ResumoResponse> = runCatching {
        val response = client.get("${KtorClient.BASE_URL}periodos/$periodoId/resumo/")
        val raw = response.bodyAsText()
        Log.d("FinanceApi", "getResumo[$periodoId] status=${response.status.value} → $raw")
        jsonParser.decodeFromString<ResumoResponse>(raw)
    }

    // ── Gastos Diários ──────────────────────────────────────────────────────

    suspend fun getGastosDiarios(periodoId: Int): Result<List<GastoDiarioResponse>> = runCatching {
        client.get("${KtorClient.BASE_URL}periodos/$periodoId/gastos-diarios/").body()
    }

    suspend fun createGasto(periodoId: Int, request: GastoDiarioRequest): Result<GastoDiarioResponse> = runCatching {
        Log.d("FinanceApi", "createGasto[$periodoId] sending: data=${request.data} valor=${request.valor} cat=${request.categoria_id}")
        val response = client.post("${KtorClient.BASE_URL}periodos/$periodoId/gastos-diarios/") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        val raw = response.bodyAsText()
        Log.d("FinanceApi", "createGasto[$periodoId] status=${response.status.value} → $raw")
        jsonParser.decodeFromString<GastoDiarioResponse>(raw)
    }

    suspend fun updateGasto(
        periodoId: Int,
        gastoId: Int,
        valor: String? = null,
        categoriaId: Int? = null,
        descricao: String? = null,
    ): Result<GastoDiarioResponse> = runCatching {
        client.patch("${KtorClient.BASE_URL}periodos/$periodoId/gastos-diarios/$gastoId/") {
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject {
                valor?.let { put("valor", it) }
                categoriaId?.let { put("categoria_id", it) }
                descricao?.let { put("descricao", it) }
            })
        }.body()
    }

    suspend fun deleteGasto(periodoId: Int, gastoId: Int): Result<Unit> = runCatching {
        val response = client.delete("${KtorClient.BASE_URL}periodos/$periodoId/gastos-diarios/$gastoId/")
        if (!response.status.isSuccess()) throw Exception("HTTP ${response.status.value}")
    }
}
