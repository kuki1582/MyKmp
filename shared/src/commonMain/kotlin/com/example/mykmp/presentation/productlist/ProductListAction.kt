package com.example.mykmp.presentation.productlist

/** 商品一覧画面で「実際に起きたこと」。Store の内部だけで使う。 */
internal sealed interface ProductListAction {
    data object RefreshStarted : ProductListAction
    data object RefreshSucceeded : ProductListAction
    data class RefreshFailed(val message: String) : ProductListAction

    /** 一覧かお気に入りが変わった */
    data class ItemsUpdated(
        val items: List<ProductListItem>,
        val favoriteCount: Int,
    ) : ProductListAction
}
