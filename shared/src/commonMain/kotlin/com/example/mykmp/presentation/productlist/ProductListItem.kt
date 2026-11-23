package com.example.mykmp.presentation.productlist

import com.example.mykmp.domain.model.Product

/** 一覧の 1 行。商品と、それがお気に入りかどうかを組み合わせたもの */
data class ProductListItem(
    val product: Product,
    val isFavorite: Boolean,
)
