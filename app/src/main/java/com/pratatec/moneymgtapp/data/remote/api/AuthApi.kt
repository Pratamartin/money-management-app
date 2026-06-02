package com.pratatec.moneymgtapp.data.remote.api

import com.pratatec.moneymgtapp.data.remote.dto.LoginRequest
import com.pratatec.moneymgtapp.data.remote.dto.LoginResponse
import com.pratatec.moneymgtapp.data.remote.dto.RegisterRequest
import com.pratatec.moneymgtapp.data.remote.dto.RegisterResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AuthApi(private val client: HttpClient) {

    suspend fun login(request: LoginRequest): Result<LoginResponse> = runCatching {
        client.post("${KtorClient.BASE_URL}auth/login/") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body<LoginResponse>()
    }

    suspend fun register(request: RegisterRequest): Result<RegisterResponse> = runCatching {
        client.post("${KtorClient.BASE_URL}auth/register/") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body<RegisterResponse>()
    }
}
