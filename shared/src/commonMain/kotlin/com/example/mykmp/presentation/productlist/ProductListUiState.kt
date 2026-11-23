package com.example.mykmp.presentation.productlist

data class ProductListUiState(
    /** API から取り直している最中か */
    val isLoading: Boolean = false,
    val items: List<ProductListItem> = emptyList(),
    val favoriteCount: Int = 0,
    val errorMessage: String? = null,
)
