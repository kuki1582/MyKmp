package com.example.mykmp.presentation.productlist

sealed interface ProductListSideEffect {
    data class NavigateToDetail(val id: String): ProductListSideEffect
}
