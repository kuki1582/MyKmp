package com.example.mykmp.data.repository

import com.example.mykmp.data.datasource.remote.ProductRemoteDataSource
import com.example.mykmp.data.mapper.toDomain
import com.example.mykmp.domain.model.Product
import com.example.mykmp.domain.repository.ProductRepository

/**
 * 商品の Repository。どの DataSource から取るかを決め、mapper でドメインモデルに変換する。
 * いまはリモートだけ。あとで local（データベース）を追加し、キャッシュなどをここで組み合わせる。
 */
internal class ProductRepositoryImpl(
    private val remoteDataSource: ProductRemoteDataSource,
) : ProductRepository {
    override suspend fun fetchProducts(): List<Product> =
        remoteDataSource.getProducts().products.map { it.toDomain() }
}
