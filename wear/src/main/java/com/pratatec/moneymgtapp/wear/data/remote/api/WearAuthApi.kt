package com.pratatec.moneymgtapp.wear.data.remote.api

import com.pratatec.moneymgtapp.wear.data.remote.dto.LoginRequest
import com.pratatec.moneymgtapp.wear.data.remote.dto.LoginResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class WearAuthApi {
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        expectSuccess = false
    }

    suspend fun login(email: String, password: String): Result<LoginResponse> = runCatching {
        client.post("${WearKtorClient.BASE_URL}auth/login/") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email, password))
        }.body<LoginResponse>()
    }
}
