package com.example.mykmp.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.example.mykmp.data.datasource.local.db.AppDatabase
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

internal actual fun platformModule(context: PlatformContext) = module {
    single<HttpClientEngine> { OkHttp.create() }
    single<SqlDriver> { AndroidSqliteDriver(AppDatabase.Schema, context, DATABASE_NAME) }
    single<CoroutineDispatcher> { Dispatchers.IO }
}
