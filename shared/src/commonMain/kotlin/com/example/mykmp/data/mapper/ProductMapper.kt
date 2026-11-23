package com.example.mykmp.data.mapper

import com.example.mykmp.data.datasource.local.db.ProductEntity
import com.example.mykmp.data.model.ProductDetailDto
import com.example.mykmp.data.model.ProductDto
import com.example.mykmp.data.model.ReviewDto
import com.example.mykmp.domain.model.Product
import com.example.mykmp.domain.model.ProductDetail
import com.example.mykmp.domain.model.Review
import kotlin.math.roundToInt

/*
 * data 層のモデル ⇔ ドメインモデル（Product）の変換。
 * domain 層は data 層（DTO / DB の Entity）を知らないようにしたいので、変換は data 層に置く。
 */

/**
 * API の形（ProductDto）→ Product
 *
 * @param categoryNames categories API から作った「slug → 表示名」の対応表。
 *   見つからない slug はそのまま表示名として使う。
 */
internal fun ProductDto.toDomain(categoryNames: Map<String, String>): Product = Product(
    id = id.toString(),
    name = title,
    // DummyJSON の価格はドル（小数）。いまの Product.price は Int なので四捨五入している
    price = price.roundToInt(),
    categoryName = categoryNames[category] ?: category,
)

/** DB の形（ProductEntity）→ Product */
internal fun ProductEntity.toDomain(): Product = Product(
    id = id,
    name = name,
    price = price.toInt(),
    categoryName = categoryName,
)

/** Product → DB の形（ProductEntity） */
internal fun Product.toEntity(): ProductEntity = ProductEntity(
    id = id,
    name = name,
    price = price.toLong(),
    categoryName = categoryName,
)

/**
 * 詳細 API の形（ProductDetailDto）→ ProductDetail
 *
 * @param categoryNames categories API から作った「slug → 表示名」の対応表
 */
internal fun ProductDetailDto.toDomain(categoryNames: Map<String, String>): ProductDetail = ProductDetail(
    id = id.toString(),
    name = title,
    description = description,
    categoryName = categoryNames[category] ?: category,
    // 一覧と同じく、ドルを四捨五入して Int にしている
    price = price.roundToInt(),
    discountPercentage = discountPercentage,
    rating = rating,
    stock = stock,
    brand = brand,
    availabilityStatus = availabilityStatus,
    warrantyInformation = warrantyInformation,
    shippingInformation = shippingInformation,
    returnPolicy = returnPolicy,
    imageUrls = images,
    reviews = reviews.map { it.toDomain() },
)

internal fun ReviewDto.toDomain(): Review = Review(
    rating = rating,
    comment = comment,
    reviewerName = reviewerName,
    // "2024-05-23T08:56:21.618Z" → "2024-05-23"（表示は日付だけでよいので先頭 10 文字）
    date = date.take(10),
)
