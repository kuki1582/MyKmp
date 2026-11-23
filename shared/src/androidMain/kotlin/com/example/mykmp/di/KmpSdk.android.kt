package com.example.mykmp.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.dsl.module

internal actual val platformModule = module {
    single { HttpClient(OkHttp) }
}
