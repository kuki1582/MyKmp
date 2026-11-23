package com.example.mykmp.domain.usecase

import com.example.mykmp.domain.common.DomainResult
import com.example.mykmp.domain.repository.ProductRepository
import kotlin.coroutines.cancellation.CancellationException

/** 最新の商品を取得して保存する。結果は ObserveProductsUseCase の Flow に流れてくる */
class RefreshProductsUseCase(
    private val repository: ProductRepository,
) {
    suspend operator fun invoke(): DomainResult<Unit> =
        try {
            repository.refreshProducts()
            DomainResult.Success(Unit)
        } catch (e: CancellationException) {
            // 画面を閉じたときなどのキャンセルはエラー扱いせず、そのまま伝える
            throw e
        } catch (e: Exception) {
            DomainResult.Error("失敗した")
        }
}
