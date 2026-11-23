package com.example.mykmp.domain.usecase

import com.example.mykmp.domain.common.DomainResult
import com.example.mykmp.domain.model.ProductDetail
import com.example.mykmp.domain.repository.ProductRepository
import kotlin.coroutines.cancellation.CancellationException

/** 商品 1 件の詳細を取得する */
class GetProductDetailUseCase(
    private val repository: ProductRepository,
) {
    suspend operator fun invoke(id: String): DomainResult<ProductDetail> =
        try {
            DomainResult.Success(repository.getProductDetail(id))
        } catch (e: CancellationException) {
            // 画面を閉じたときなどのキャンセルはエラー扱いせず、そのまま伝える
            throw e
        } catch (e: Exception) {
            DomainResult.Error("商品の詳細を取得できませんでした")
        }
}
