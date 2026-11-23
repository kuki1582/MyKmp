package com.example.mykmp.domain.repository

import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    /** お気に入りの商品 ID を流し続ける */
    fun observeFavoriteIds(): Flow<Set<String>>

    /** お気に入りの登録・解除を切り替える */
    suspend fun toggleFavorite(productId: String)
}
