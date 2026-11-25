package com.example.mykmp.presentation.productdetail

import com.example.mykmp.domain.model.ProductDetail

/** 商品詳細画面で「実際に起きたこと」。Store の内部だけで使う。 */
internal sealed interface ProductDetailAction {
    data object LoadStarted : ProductDetailAction
    data class LoadSucceeded(val detail: ProductDetail) : ProductDetailAction {
        // ログが長くなりすぎないよう、id と名前だけ出す
        override fun toString() = "LoadSucceeded(id=${detail.id}, name=${detail.name})"
    }
    data class LoadFailed(val message: String) : ProductDetailAction

    /** この商品のお気に入り状態が変わった */
    data class FavoriteChanged(val isFavorite: Boolean) : ProductDetailAction
}
