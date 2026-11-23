package com.example.mykmp.presentation

import com.example.mykmp.domain.model.Product
import com.example.mykmp.presentation.action.ProductListAction
import com.example.mykmp.presentation.reducer.ProductListReducer
import com.example.mykmp.presentation.state.ProductListUiState
import kotlin.test.Test
import kotlin.test.assertEquals

class ProductListReducerTest {
    private val products = listOf(Product(id = "1", name = "医療機器A", price = 120))

    @Test
    fun loadStarted_setsLoadingAndClearsError() {
        val state = ProductListUiState(errorMessage = "前のエラー")

        val next = ProductListReducer.reduce(state, ProductListAction.LoadStarted)

        assertEquals(ProductListUiState(isLoading = true, errorMessage = null), next)
    }

    @Test
    fun loadSucceeded_setsProducts() {
        val state = ProductListUiState(isLoading = true)

        val next = ProductListReducer.reduce(state, ProductListAction.LoadSucceeded(products))

        assertEquals(ProductListUiState(isLoading = false, products = products), next)
    }

    @Test
    fun loadFailed_setsErrorMessage() {
        val state = ProductListUiState(isLoading = true)

        val next = ProductListReducer.reduce(state, ProductListAction.LoadFailed("失敗した"))

        assertEquals(ProductListUiState(isLoading = false, errorMessage = "失敗した"), next)
    }
}
