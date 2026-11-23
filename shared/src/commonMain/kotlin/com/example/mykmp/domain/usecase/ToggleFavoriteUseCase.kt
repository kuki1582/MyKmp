package com.example.mykmp.domain.usecase

import com.example.mykmp.domain.repository.FavoriteRepository

/** お気に入りの登録・解除を切り替える */
class ToggleFavoriteUseCase(
    private val repository: FavoriteRepository,
) {
    suspend operator fun invoke(productId: String) = repository.toggleFavorite(productId)
}
