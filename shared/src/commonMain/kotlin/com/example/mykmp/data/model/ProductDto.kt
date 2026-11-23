package com.example.mykmp.data.model

import kotlinx.serialization.Serializable

/** GET https://dummyjson.com/products のレスポンス */
@Serializable
internal data class ProductsResponseDto(
    val products: List<ProductDto>,
)

/** API の商品 1 件。使う項目だけ定義し、残りは無視する（Json の ignoreUnknownKeys） */
@Serializable
internal data class ProductDto(
    val id: Int,
    val title: String,
    val price: Double,
    /** カテゴリの slug（例: "beauty"）。表示名は categories API から取る */
    val category: String,
)
