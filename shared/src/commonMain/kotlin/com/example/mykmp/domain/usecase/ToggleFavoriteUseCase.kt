package com.example.mykmp.domain.usecase

import com.example.mykmp.domain.common.DomainResult
import com.example.mykmp.domain.repository.FavoriteRepository
import kotlin.coroutines.cancellation.CancellationException

/** お気に入りの登録・解除を切り替える。結果は ObserveFavoriteIdsUseCase の Flow に流れてくる */
class ToggleFavoriteUseCase(
    private val repository: FavoriteRepository,
) {
    suspend operator fun invoke(productId: String): DomainResult<Unit> =
        try {
            repository.toggleFavorite(productId)
            DomainResult.Success(Unit)
        } catch (e: CancellationException) {
            // 画面を閉じたときなどのキャンセルはエラー扱いせず、そのまま伝える
            throw e
        } catch (e: Exception) {
            DomainResult.Error("お気に入りを変更できませんでした")
        }
}
