package com.example.mykmp.domain.model

/** 商品の詳細 */
data class ProductDetail(
    val id: String,
    val name: String,
    val description: String,
    val categoryName: String,
    val price: Int,
    /** 割引率（%） */
    val discountPercentage: Double,
    /** 評価（0〜5） */
    val rating: Double,
    val stock: Int,
    val brand: String?,
    /** 在庫状況（例: "In Stock" / "Low Stock"） */
    val availabilityStatus: String?,
    val warrantyInformation: String?,
    val shippingInformation: String?,
    val returnPolicy: String?,
    val imageUrls: List<String>,
    val reviews: List<Review>,
)

data class Review(
    /** 評価（1〜5） */
    val rating: Int,
    val comment: String,
    val reviewerName: String,
    /** 投稿日（例: "2024-05-23"） */
    val date: String,
)
