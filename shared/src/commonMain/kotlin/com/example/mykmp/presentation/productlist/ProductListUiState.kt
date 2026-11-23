package com.example.mykmp.presentation.productlist

import com.example.mykmp.domain.model.Product

data class ProductListUiState(
    val isLoading: Boolean = false,
    val products: List<Product> = emptyList(),
    val errorMessage: String? = null,
)
