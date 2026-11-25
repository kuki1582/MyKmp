package com.example.mykmp.presentation.productdetail

import com.example.mykmp.domain.usecase.GetProductDetailUseCase
import com.example.mykmp.domain.usecase.ObserveFavoriteIdsUseCase
import com.example.mykmp.domain.usecase.ToggleFavoriteUseCase
import com.example.mykmp.presentation.mvi.Store
import com.example.mykmp.presentation.mvi.StoreLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * 商品詳細画面の Store。Android（ViewModel）と iOS（SwiftUI）の両方から使う。
 *
 * 作った直後にお気に入りの監視・詳細の取得が自動で始まる（UI から何か送る必要はない）。
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
    logger: StoreLogger? = null,
) {
    private val store = Store(
        name = "ProductDetail($productId)",
        initialState = ProductDetailUiState(),
        processor = ProductDetailProcessor(productId, getProductDetail, observeFavoriteIds, toggleFavorite),
        reducer = ProductDetailReducer,
        scope = scope,
        logger = logger,
    )

    val state: StateFlow<ProductDetailUiState> get() = store.state
    val sideEffect: Flow<ProductDetailSideEffect> get() = store.sideEffect

    fun dispatch(intent: ProductDetailIntent) = store.dispatch(intent)

    /** iOS など scope を自分で管理している場合に、画面を閉じるとき呼ぶ */
    fun close() = store.close()
}
