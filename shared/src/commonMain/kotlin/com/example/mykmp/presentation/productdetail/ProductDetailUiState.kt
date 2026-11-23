package com.example.mykmp.presentation.productdetail

import com.example.mykmp.domain.model.ProductDetail

data class ProductDetailUiState(
    val isLoading: Boolean = false,
    val detail: ProductDetail? = null,
    val isFavorite: Boolean = false,
    val errorMessage: String? = null,
)
