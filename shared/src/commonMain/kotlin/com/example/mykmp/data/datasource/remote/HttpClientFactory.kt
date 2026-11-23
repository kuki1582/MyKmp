package com.example.mykmp.data.datasource.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * 共通設定の HttpClient を作る。エンジン（Android: OkHttp / iOS: Darwin）だけを OS ごとに差し替える。
 */
internal fun createHttpClient(engine: HttpClientEngine): HttpClient = HttpClient(engine) {
    // 2xx 以外はエラー（例外）にする。Repository / UseCase 側で DomainResult.Error に変換される
    expectSuccess = true

    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true // API 側に項目が増えても壊れないように
            },
        )
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 15_000
    }
    install(Logging) {
        level = LogLevel.INFO
    }
}
