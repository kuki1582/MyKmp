package com.example.mykmp.data.mapper

import com.example.mykmp.data.model.ProductDto
import com.example.mykmp.domain.model.Product
import kotlin.math.roundToInt

/**
 * API の形（ProductDto）をアプリの形（Product）に変換する。
 * domain 層は data 層（DTO）を知らないようにしたいので、変換は data 層に置く。
 */
internal fun ProductDto.toDomain(): Product = Product(
    id = id.toString(),
    name = title,
    // DummyJSON の価格はドル（小数）。いまの Product.price は Int なので四捨五入している
    price = price.roundToInt(),
)
