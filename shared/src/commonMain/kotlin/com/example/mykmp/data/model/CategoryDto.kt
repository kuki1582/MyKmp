package com.example.mykmp.data.model

import kotlinx.serialization.Serializable

/** GET https://dummyjson.com/products/categories の 1 件 */
@Serializable
internal data class CategoryDto(
    /** 商品側の category と同じ値（例: "beauty"） */
    val slug: String,
    /** 表示名（例: "Beauty"） */
    val name: String,
)
