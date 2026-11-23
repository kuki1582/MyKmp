package com.example.mykmp.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.example.mykmp.data.datasource.local.db.AppDatabase
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.dsl.module

@Suppress("UNUSED_PARAMETER") // iOS では context を使わない
internal actual fun platformModule(context: PlatformContext) = module {
    single<HttpClientEngine> { Darwin.create() }
    single<SqlDriver> { NativeSqliteDriver(AppDatabase.Schema, DATABASE_NAME) }
    single<CoroutineDispatcher> { Dispatchers.IO }
}
