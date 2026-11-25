package com.example.mykmp.presentation.productlist

/**
 * ユーザーの操作。画面を開いたときの処理（監視の開始と最初の取得）は
 * Intent ではなく Processor.bootstrap() で Store が自動で行う。
 */
sealed interface ProductListIntent {
    /** 最新の商品を取り直す（再読み込みボタン） */
    data object Refresh : ProductListIntent

    data class ClickProduct(val id: String) : ProductListIntent

    data class ToggleFavorite(val id: String) : ProductListIntent
}
