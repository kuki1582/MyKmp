package com.example.mykmp.presentation.productlist

import com.example.mykmp.presentation.mvi.Reducer

internal object ProductListReducer : Reducer<ProductListUiState, ProductListAction> {
    override fun reduce(state: ProductListUiState, action: ProductListAction): ProductListUiState =
        when (action) {
            ProductListAction.LoadStarted ->
                state.copy(isLoading = true, errorMessage = null)

            is ProductListAction.LoadSucceeded ->
                state.copy(isLoading = false, products = action.products)

            is ProductListAction.LoadFailed ->
                state.copy(isLoading = false, errorMessage = action.message)
        }
}
