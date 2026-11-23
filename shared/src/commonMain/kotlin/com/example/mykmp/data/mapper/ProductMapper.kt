package com.example.mykmp.data.mapper

import com.example.mykmp.data.model.ProductDto
import com.example.mykmp.domain.model.Product
import kotlin.math.roundToInt

/**
 * API の形（ProductDto）をアプリの形（Product）に変換する。
 * domain 層は data 層（DTO）を知らないようにしたいので、変換は data 層に置く。
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
