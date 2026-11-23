package com.example.mykmp.presentation.productdetail

sealed interface ProductDetailIntent {
    /** 画面の表示開始。お気に入りの監視を始め、詳細を取得する。Store を作った直後に 1 回だけ送る */
    data object Load : ProductDetailIntent

    /** 詳細を取り直す（再読み込みボタン） */
    data object Retry : ProductDetailIntent

    data object ToggleFavorite : ProductDetailIntent
}
