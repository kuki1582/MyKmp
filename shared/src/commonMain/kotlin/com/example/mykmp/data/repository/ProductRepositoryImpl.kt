package com.example.mykmp.data.repository

import com.example.mykmp.data.datasource.remote.ProductDto
import com.example.mykmp.data.datasource.remote.ProductRemoteDataSource
import com.example.mykmp.domain.model.Product
import com.example.mykmp.domain.repository.ProductRepository
import kotlin.math.roundToInt

/**
 * 商品の Repository。どの DataSource から取るかを決め、DTO をドメインモデルに変換する。
 * いまはリモートだけ。あとで local（データベース）を追加し、キャッシュなどをここで組み合わせる。
 */
internal class ProductRepositoryImpl(
    private val remoteDataSource: ProductRemoteDataSource,
) : ProductRepository {
    override suspend fun fetchProducts(): List<Product> =
        remoteDataSource.getProducts().products.map { it.toDomain() }
}

/** API の形（DTO）をアプリの形（ドメインモデル）に変換する */
internal fun ProductDto.toDomain(): Product = Product(
    id = id.toString(),
    name = title,
    // DummyJSON の価格はドル（小数）。いまの Product.price は Int なので四捨五入している
    price = price.roundToInt(),
)
