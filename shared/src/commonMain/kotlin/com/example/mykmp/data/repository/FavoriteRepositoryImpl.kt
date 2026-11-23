package com.example.mykmp.data.repository

import com.example.mykmp.data.datasource.local.FavoriteLocalDataSource
import com.example.mykmp.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow

internal class FavoriteRepositoryImpl(
    private val localDataSource: FavoriteLocalDataSource,
) : FavoriteRepository {
    override fun observeFavoriteIds(): Flow<Set<String>> = localDataSource.observeFavoriteIds()

    override suspend fun toggleFavorite(productId: String) = localDataSource.toggle(productId)
}
