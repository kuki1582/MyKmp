package com.example.mykmp.presentation.productlist

import com.example.mykmp.presentation.mvi.Reducer

internal object ProductListReducer : Reducer<ProductListUiState, ProductListAction> {
    override fun reduce(state: ProductListUiState, action: ProductListAction): ProductListUiState =
        when (action) {
            ProductListAction.RefreshStarted ->
                state.copy(isLoading = true, errorMessage = null)

            ProductListAction.RefreshSucceeded ->
                state.copy(isLoading = false)

            is ProductListAction.RefreshFailed ->
                state.copy(isLoading = false, errorMessage = action.message)

            is ProductListAction.ItemsUpdated ->
                state.copy(items = action.items, favoriteCount = action.favoriteCount)
        }
}
