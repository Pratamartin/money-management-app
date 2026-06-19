package com.pratatec.moneymgtapp.wear.data.remote.api

import com.pratatec.moneymgtapp.wear.BuildConfig
import com.pratatec.moneymgtapp.wear.data.local.WearTokenStorage
import com.pratatec.moneymgtapp.wear.data.remote.dto.TokenRefreshResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.encodedPath
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object WearKtorClient {
    const val BASE_URL = BuildConfig.BASE_URL

    fun create(tokenStorage: WearTokenStorage): HttpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(Auth) {
            bearer {
                loadTokens {
                    val access = tokenStorage.getAccess() ?: return@loadTokens null
                    val refresh = tokenStorage.getRefresh() ?: return@loadTokens null
                    BearerTokens(access, refresh)
                }
                refreshTokens {
                    val oldRefresh = tokenStorage.getRefresh() ?: return@refreshTokens null
                    val response = client.post("${BASE_URL}auth/token/refresh/") {
                        contentType(ContentType.Application.Json)
                        setBody(mapOf("refresh" to oldRefresh))
                        markAsRefreshTokenRequest()
                    }
                    if (!response.status.isSuccess()) {
                        tokenStorage.clear()
                        return@refreshTokens null
                    }
                    val body = response.body<TokenRefreshResponse>()
                    val newRefresh = body.refresh ?: oldRefresh
                    tokenStorage.save(body.access, newRefresh)
                    BearerTokens(body.access, newRefresh)
                }
                sendWithoutRequest { request ->
                    !request.url.encodedPath.startsWith("/auth/")
                }
            }
        }
        expectSuccess = false
    }
}
