package com.example.mykmp.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.example.mykmp.data.datasource.local.SqlDelightFavoriteLocalDataSource
import com.example.mykmp.data.datasource.local.SqlDelightProductLocalDataSource
import com.example.mykmp.data.datasource.local.db.AppDatabase
import com.example.mykmp.data.datasource.local.db.ProductEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** SQLDelight の実装を、JVM 上のインメモリ SQLite で確かめる */
class SqlDelightProductLocalDataSourceTest {

    private fun database(): AppDatabase {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        AppDatabase.Schema.create(driver)
        return AppDatabase(driver)
    }

    /** withContext の切り替え先も runTest と同じスケジューラにそろえる */
    private fun TestScope.dispatcher() = StandardTestDispatcher(testScheduler)

    private fun entity(id: String) = ProductEntity(id = id, name = "商品$id", price = 100, categoryName = "Beauty")

    @Test
    fun replaceProducts_replacesAllRowsAndSortsById() = runTest {
        val local = SqlDelightProductLocalDataSource(database(), dispatcher())
        local.replaceProducts(listOf(entity("1"), entity("2")))

        local.replaceProducts(listOf(entity("10"), entity("3")))

        assertEquals(listOf(entity("3"), entity("10")), local.observeProducts().first())
    }

    @Test
    fun toggle_addsThenRemovesFavorite() = runTest {
        val favorites = SqlDelightFavoriteLocalDataSource(database(), dispatcher())

        favorites.toggle("1")
        assertEquals(setOf("1"), favorites.observeFavoriteIds().first())

        favorites.toggle("1")
        assertEquals(emptySet(), favorites.observeFavoriteIds().first())
    }
}
