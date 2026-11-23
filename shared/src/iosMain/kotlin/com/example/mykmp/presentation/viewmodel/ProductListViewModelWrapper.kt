package com.example.mykmp.presentation.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mykmp.di.KmpSdk
import com.example.mykmp.presentation.intent.ProductListIntent
import com.example.mykmp.presentation.sideEffect.ProductListSideEffect
import com.example.mykmp.presentation.state.ProductListUiState
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Swift から ProductListViewModel を扱うためのラッパー。
 * StateFlow / Flow は Swift から直接 collect しづらいので、コールバックに変換して渡す。
 * 画面を閉じるときは必ず [dispose] を呼ぶこと。
 */
class ProductListViewModelWrapper {
    private val storeOwner = object : ViewModelStoreOwner {
        override val viewModelStore = ViewModelStore()
    }

    private val viewModel: ProductListViewModel = ViewModelProvider.create(
        owner = storeOwner,
        factory = viewModelFactory {
            initializer { ProductListViewModel(KmpSdk.getProductsUseCase()) }
        },
    )[ProductListViewModel::class]

    private val scope = MainScope()

    val currentState: ProductListUiState
        get() = viewModel.state.value

    fun observeState(onChange: (ProductListUiState) -> Unit) {
        scope.launch { viewModel.state.collect { onChange(it) } }
    }

    fun observeSideEffect(onEffect: (ProductListSideEffect) -> Unit) {
        scope.launch { viewModel.sideEffect.collect { onEffect(it) } }
    }

    fun onIntent(intent: ProductListIntent) {
        viewModel.onIntent(intent)
    }

    fun dispose() {
        scope.cancel()
        storeOwner.viewModelStore.clear()
    }
}
