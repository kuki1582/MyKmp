package com.example.mykmp.domain.usecase

import com.example.mykmp.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow

/** お気に入りの商品 ID を流し続ける */
class ObserveFavoriteIdsUseCase(
    private val repository: FavoriteRepository,
) {
    operator fun invoke(): Flow<Set<String>> = repository.observeFavoriteIds()
}
