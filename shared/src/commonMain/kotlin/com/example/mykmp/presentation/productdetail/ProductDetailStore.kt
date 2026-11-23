package com.example.mykmp.presentation.productdetail

import com.example.mykmp.domain.usecase.GetProductDetailUseCase
import com.example.mykmp.domain.usecase.ObserveFavoriteIdsUseCase
import com.example.mykmp.domain.usecase.ToggleFavoriteUseCase
import com.example.mykmp.presentation.mvi.Store
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * 商品詳細画面の Store。Android（ViewModel）と iOS（SwiftUI）の両方から使う。
 *
 * @param productId 表示する商品の ID
 * @param scope Android では viewModelScope、iOS では storeScope() を渡す。
 */
class ProductDetailStore internal constructor(
    productId: String,
    getProductDetail: GetProductDetailUseCase,
    observeFavoriteIds: ObserveFavoriteIdsUseCase,
    toggleFavorite: ToggleFavoriteUseCase,
    scope: CoroutineScope,
) {
    private val store = Store(
        initialState = ProductDetailUiState(),
        processor = ProductDetailProcessor(productId, getProductDetail, observeFavoriteIds, toggleFavorite),
        reducer = ProductDetailReducer,
        scope = scope,
    )

    val state: StateFlow<ProductDetailUiState> get() = store.state
    val sideEffect: Flow<ProductDetailSideEffect> get() = store.sideEffect

    fun dispatch(intent: ProductDetailIntent) = store.dispatch(intent)

    /** iOS など scope を自分で管理している場合に、画面を閉じるとき呼ぶ */
    fun close() = store.close()
}
