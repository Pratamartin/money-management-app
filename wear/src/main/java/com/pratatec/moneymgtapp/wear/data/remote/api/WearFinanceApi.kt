package com.pratatec.moneymgtapp.wear.data.remote.api

import com.pratatec.moneymgtapp.wear.data.remote.dto.CategoriaResponse
import com.pratatec.moneymgtapp.wear.data.remote.dto.GastoDiarioRequest
import com.pratatec.moneymgtapp.wear.data.remote.dto.PeriodoResponse
import com.pratatec.moneymgtapp.wear.data.remote.dto.ResumoResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json

class WearFinanceApi(private val client: HttpClient) {

    private val jsonParser = Json { ignoreUnknownKeys = true }

    suspend fun getPeriodos(): Result<List<PeriodoResponse>> = runCatching {
        val raw = client.get("${WearKtorClient.BASE_URL}periodos/").bodyAsText()
        jsonParser.decodeFromString(raw)
    }

    suspend fun getResumo(periodoId: Int): Result<ResumoResponse> = runCatching {
        val raw = client.get("${WearKtorClient.BASE_URL}periodos/$periodoId/resumo/").bodyAsText()
        jsonParser.decodeFromString(raw)
    }

    suspend fun getCategorias(): Result<List<CategoriaResponse>> = runCatching {
        client.get("${WearKtorClient.BASE_URL}categorias/").body()
    }

    suspend fun createGasto(periodoId: Int, request: GastoDiarioRequest): Result<Unit> = runCatching {
        client.post("${WearKtorClient.BASE_URL}periodos/$periodoId/gastos-diarios/") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }
}
