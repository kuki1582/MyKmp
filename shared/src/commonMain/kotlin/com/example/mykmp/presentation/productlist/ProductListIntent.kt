package com.example.mykmp.presentation.productlist

sealed interface ProductListIntent {
    /** 画面の表示開始。一覧とお気に入りの監視を始め、最新の商品を取得する。Store を作った直後に 1 回だけ送る */
    data object Load : ProductListIntent

    /** 最新の商品を取り直す（再読み込みボタン） */
    data object Refresh : ProductListIntent

    data class ClickProduct(val id: String) : ProductListIntent

    data class ToggleFavorite(val id: String) : ProductListIntent
}
