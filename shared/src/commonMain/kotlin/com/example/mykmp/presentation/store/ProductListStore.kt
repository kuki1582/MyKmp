package com.example.mykmp.presentation.store

import com.example.mykmp.domain.usecase.GetProductsUseCase
import com.example.mykmp.presentation.intent.ProductListIntent
import com.example.mykmp.presentation.processor.ProductListProcessor
import com.example.mykmp.presentation.reducer.ProductListReducer
import com.example.mykmp.presentation.sideEffect.ProductListSideEffect
import com.example.mykmp.presentation.state.ProductListUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * 商品一覧画面の Store。Android（ViewModel）と iOS（SwiftUI）の両方から使う。
 *
 * 型を具体的に書いて公開しているのは、Swift 側で SKIE が
 * StateFlow<ProductListUiState> をそのまま AsyncSequence として扱えるようにするため。
 *
 * @param scope Android では viewModelScope、iOS では storeScope() を渡す。
 */
class ProductListStore internal constructor(
    getProducts: GetProductsUseCase,
    scope: CoroutineScope,
) {
    private val store = Store(
        initialState = ProductListUiState(),
        processor = ProductListProcessor(getProducts),
        reducer = ProductListReducer,
        scope = scope,
    )

    val state: StateFlow<ProductListUiState> get() = store.state
    val sideEffect: Flow<ProductListSideEffect> get() = store.sideEffect

    fun dispatch(intent: ProductListIntent) = store.dispatch(intent)

    /** iOS など scope を自分で管理している場合に、画面を閉じるとき呼ぶ */
    fun close() = store.close()
}
