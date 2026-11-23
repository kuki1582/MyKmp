package com.example.mykmp.presentation.productdetail

import com.example.mykmp.presentation.mvi.Reducer

internal object ProductDetailReducer : Reducer<ProductDetailUiState, ProductDetailAction> {
    override fun reduce(state: ProductDetailUiState, action: ProductDetailAction): ProductDetailUiState =
        when (action) {
            ProductDetailAction.LoadStarted ->
                state.copy(isLoading = true, errorMessage = null)

            is ProductDetailAction.LoadSucceeded ->
                state.copy(isLoading = false, detail = action.detail)

            is ProductDetailAction.LoadFailed ->
                state.copy(isLoading = false, errorMessage = action.message)

            is ProductDetailAction.FavoriteChanged ->
                state.copy(isFavorite = action.isFavorite)
        }
}
