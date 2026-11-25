package com.example.mykmp.presentation.productdetail

/**
 * ユーザーの操作。画面を開いたときの処理（お気に入りの監視の開始と詳細の取得）は
 * Intent ではなく Processor.bootstrap() で Store が自動で行う。
 */
sealed interface ProductDetailIntent {
    /** 詳細を取り直す（再読み込みボタン） */
    data object Retry : ProductDetailIntent

    data object ToggleFavorite : ProductDetailIntent
}
