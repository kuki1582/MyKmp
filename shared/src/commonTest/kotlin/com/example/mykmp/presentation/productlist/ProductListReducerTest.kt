package com.example.mykmp.presentation.productlist

import com.example.mykmp.domain.model.Product
import kotlin.test.Test
import kotlin.test.assertEquals

class ProductListReducerTest {
    private val items = listOf(
        ProductListItem(Product(id = "1", name = "医療機器A", price = 120, categoryName = "医療"), isFavorite = true),
    )

    @Test
    fun refreshStarted_setsLoadingAndClearsError() {
        val state = ProductListUiState(errorMessage = "前のエラー")

        val next = ProductListReducer.reduce(state, ProductListAction.RefreshStarted)

        assertEquals(ProductListUiState(isLoading = true, errorMessage = null), next)
    }

    @Test
    fun refreshSucceeded_stopsLoading() {
        val next = ProductListReducer.reduce(ProductListUiState(isLoading = true), ProductListAction.RefreshSucceeded)

        assertEquals(ProductListUiState(isLoading = false), next)
    }

    @Test
    fun refreshFailed_setsErrorAndKeepsItems() {
        val state = ProductListUiState(isLoading = true, items = items, favoriteCount = 1)

        val next = ProductListReducer.reduce(state, ProductListAction.RefreshFailed("失敗した"))

        assertEquals(ProductListUiState(isLoading = false, items = items, favoriteCount = 1, errorMessage = "失敗した"), next)
    }

    @Test
    fun itemsUpdated_setsItemsAndFavoriteCount() {
        val next = ProductListReducer.reduce(ProductListUiState(), ProductListAction.ItemsUpdated(items, favoriteCount = 1))

        assertEquals(ProductListUiState(items = items, favoriteCount = 1), next)
    }
}
