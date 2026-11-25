package com.example.mykmp.ui.productlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mykmp.di.KmpSdk
import com.example.mykmp.presentation.productlist.ProductListIntent
import com.example.mykmp.presentation.productlist.ProductListSideEffect
import com.example.mykmp.presentation.productlist.ProductListUiState
import com.example.mykmp.presentation.productlist.ProductListStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * shared の [ProductListStore] を画面回転などで作り直さないために持っておくだけの ViewModel。
 * ロジックは Store（Processor / Reducer）側にあるので、ここには書かない。
 * 最初の読み込みは Store を作った時点で自動で始まる（Processor.bootstrap）。
 * viewModelScope は ViewModel 破棄時に自動で止まるので、store.close() は呼ばなくてよい。
 */
class ProductListViewModel : ViewModel() {
    private val store: ProductListStore = KmpSdk.createProductListStore(viewModelScope)

    val state: StateFlow<ProductListUiState> = store.state
    val sideEffect: Flow<ProductListSideEffect> = store.sideEffect

    fun dispatch(intent: ProductListIntent) = store.dispatch(intent)
}
