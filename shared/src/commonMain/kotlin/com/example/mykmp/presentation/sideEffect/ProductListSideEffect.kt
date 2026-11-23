package com.example.mykmp.presentation.sideEffect

sealed interface ProductListSideEffect {
    data class NavigateToDetail(val id: String): ProductListSideEffect
}
