package com.example.mykmp.data.mapper

import com.example.mykmp.data.datasource.local.db.ProductEntity
import com.example.mykmp.data.model.ProductDto
import com.example.mykmp.domain.model.Product
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
