package com.example.mykmp.presentation.productdetail

sealed interface ProductDetailSideEffect {
    /** 1 回だけ表示するメッセージ（Android: トースト / iOS: アラート） */
    data class ShowMessage(val message: String) : ProductDetailSideEffect
}
