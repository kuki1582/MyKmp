package com.example.mykmp.ui.productdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mykmp.di.KmpSdk
import com.example.mykmp.presentation.productdetail.ProductDetailIntent
import com.example.mykmp.presentation.productdetail.ProductDetailSideEffect
import com.example.mykmp.presentation.productdetail.ProductDetailStore
import com.example.mykmp.presentation.productdetail.ProductDetailUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * shared の [ProductDetailStore] を画面回転などで作り直さないために持っておくだけの ViewModel。
 * Navigation の画面（NavBackStackEntry）ごとに作られ、戻るボタンで画面を閉じると破棄される。
 * 最初の読み込みは Store を作った時点で自動で始まる（Processor.bootstrap）。
 */
class ProductDetailViewModel(productId: String) : ViewModel() {
    private val store: ProductDetailStore = KmpSdk.createProductDetailStore(productId, viewModelScope)

    val state: StateFlow<ProductDetailUiState> = store.state
    val sideEffect: Flow<ProductDetailSideEffect> = store.sideEffect

    fun dispatch(intent: ProductDetailIntent) = store.dispatch(intent)
}
