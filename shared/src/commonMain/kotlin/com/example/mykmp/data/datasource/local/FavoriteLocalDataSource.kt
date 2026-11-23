package com.example.mykmp.data.datasource.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.mykmp.data.datasource.local.db.AppDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/** お気に入りの保存先（ローカル DB） */
internal interface FavoriteLocalDataSource {
    /** お気に入りの商品 ID。登録・解除のたびに新しい集合が流れてくる */
    fun observeFavoriteIds(): Flow<Set<String>>

    /** 登録済みなら解除、未登録なら登録する */
    suspend fun toggle(productId: String)
}

internal class SqlDelightFavoriteLocalDataSource(
    database: AppDatabase,
    private val ioDispatcher: CoroutineDispatcher,
) : FavoriteLocalDataSource {

    private val queries = database.favoriteQueries

    override fun observeFavoriteIds(): Flow<Set<String>> =
        queries.selectAllIds().asFlow().mapToList(ioDispatcher).map { it.toSet() }

    override suspend fun toggle(productId: String) = withContext(ioDispatcher) {
        queries.transaction {
            if (queries.exists(productId).executeAsOne()) {
                queries.delete(productId)
            } else {
                queries.insert(productId)
            }
        }
    }
}
