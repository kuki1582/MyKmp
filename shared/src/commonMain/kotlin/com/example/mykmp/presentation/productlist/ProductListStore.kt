package com.example.mykmp.presentation.productlist

import com.example.mykmp.domain.usecase.ObserveFavoriteIdsUseCase
import com.example.mykmp.domain.usecase.ObserveProductsUseCase
import com.example.mykmp.domain.usecase.RefreshProductsUseCase
import com.example.mykmp.domain.usecase.ToggleFavoriteUseCase
import com.example.mykmp.presentation.mvi.Store
import com.example.mykmp.presentation.mvi.StoreLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * 商品一覧画面の Store。Android（ViewModel）と iOS（SwiftUI）の両方から使う。
 *
 * 型を具体的に書いて公開しているのは、Swift 側で SKIE が
 * StateFlow<ProductListUiState> をそのまま AsyncSequence として扱えるようにするため。
 *
 * 作った直後に一覧とお気に入りの監視・商品の取得が自動で始まる（UI から何か送る必要はない）。
 *
 * @param scope Android では viewModelScope、iOS では storeScope() を渡す。
 */
class ProductListStore internal constructor(
    observeProducts: ObserveProductsUseCase,
    observeFavoriteIds: ObserveFavoriteIdsUseCase,
    refreshProducts: RefreshProductsUseCase,
    toggleFavorite: ToggleFavoriteUseCase,
    scope: CoroutineScope,
    logger: StoreLogger? = null,
) {
    private val store = Store(
        name = "ProductList",
        initialState = ProductListUiState(),
        processor = ProductListProcessor(observeProducts, observeFavoriteIds, refreshProducts, toggleFavorite),
        reducer = ProductListReducer,
        scope = scope,
        logger = logger,
    )

    val state: StateFlow<ProductListUiState> get() = store.state
    val sideEffect: Flow<ProductListSideEffect> get() = store.sideEffect

    fun dispatch(intent: ProductListIntent) = store.dispatch(intent)

    /** iOS など scope を自分で管理している場合に、画面を閉じるとき呼ぶ */
    fun close() = store.close()
}
