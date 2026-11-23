package com.example.mykmp.data.model

import kotlinx.serialization.Serializable

/**
 * GET https://dummyjson.com/products/{id} のレスポンス。
 * 商品によって無い項目があるので、必須でないものは null / 空リストを既定値にしている。
 */
@Serializable
internal data class ProductDetailDto(
    val id: Int,
    val title: String,
    val description: String = "",
    /** カテゴリの slug（例: "beauty"）。表示名は categories API から取る */
    val category: String,
    val price: Double,
    val discountPercentage: Double = 0.0,
    val rating: Double = 0.0,
    val stock: Int = 0,
    val brand: String? = null,
    val availabilityStatus: String? = null,
    val warrantyInformation: String? = null,
    val shippingInformation: String? = null,
    val returnPolicy: String? = null,
    val images: List<String> = emptyList(),
    val reviews: List<ReviewDto> = emptyList(),
)

@Serializable
internal data class ReviewDto(
    val rating: Int,
    val comment: String,
    val reviewerName: String,
    /** ISO 8601 形式の日時（例: "2024-05-23T08:56:21.618Z"） */
    val date: String,
)
