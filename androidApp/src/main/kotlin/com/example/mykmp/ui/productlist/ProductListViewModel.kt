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
 * viewModelScope は ViewModel 破棄時に自動で止まるので、store.close() は呼ばなくてよい。
 */
class ProductListViewModel : ViewModel() {
    private val store: ProductListStore = KmpSdk.createProductListStore(viewModelScope)

    val state: StateFlow<ProductListUiState> = store.state
    val sideEffect: Flow<ProductListSideEffect> = store.sideEffect

    init {
        // 監視を始める Load は 1 回だけ送る。画面回転で Composable が作り直されても ViewModel は残るので、ここで送る
        store.dispatch(ProductListIntent.Load)
    }

    fun dispatch(intent: ProductListIntent) = store.dispatch(intent)
}
