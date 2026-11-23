package com.example.mykmp.presentation.productlist

import com.example.mykmp.domain.model.Product

/** 商品一覧画面で「実際に起きたこと」。Store の内部だけで使う。 */
internal sealed interface ProductListAction {
    data object LoadStarted : ProductListAction
    data class LoadSucceeded(val products: List<Product>) : ProductListAction
    data class LoadFailed(val message: String) : ProductListAction
}
