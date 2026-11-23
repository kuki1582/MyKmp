package com.example.mykmp.presentation.productlist

sealed interface ProductListSideEffect {
    data class NavigateToDetail(val id: String) : ProductListSideEffect

    /** 1 回だけ表示するメッセージ（Android: トースト / iOS: アラート） */
    data class ShowMessage(val message: String) : ProductListSideEffect
}
