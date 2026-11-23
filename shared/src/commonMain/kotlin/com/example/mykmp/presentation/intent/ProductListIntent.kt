package com.example.mykmp.presentation.intent

sealed interface ProductListIntent {
    data object Load: ProductListIntent
    data class ClickProduct(val id: String): ProductListIntent
}
