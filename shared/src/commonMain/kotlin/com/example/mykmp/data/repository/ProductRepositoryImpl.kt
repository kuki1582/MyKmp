package com.example.mykmp.data.repository

import com.example.mykmp.data.datasource.remote.ProductRemoteDataSource
import com.example.mykmp.data.mapper.toDomain
import com.example.mykmp.domain.model.Product
import com.example.mykmp.domain.repository.ProductRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/**
 * 商品の Repository。どの DataSource から取るかを決め、mapper でドメインモデルに変換する。
 *
 * 2 つの API の結果を一部ずつ使って 1 つの Product を作っている。
 * - products API   → id / title / price / category(slug)
 * - categories API → slug / name（カテゴリの表示名）
 */
internal class ProductRepositoryImpl(
    private val remoteDataSource: ProductRemoteDataSource,
) : ProductRepository {

    override suspend fun fetchProducts(): List<Product> = coroutineScope {
        // 2 つの API は互いに依存しないので並行に呼ぶ。どちらかが失敗すればもう片方もキャンセルされる
        val productsDeferred = async { remoteDataSource.getProducts() }
        val categoriesDeferred = async { remoteDataSource.getCategories() }

        val categoryNames = categoriesDeferred.await().associate { it.slug to it.name }
        productsDeferred.await().products.map { it.toDomain(categoryNames) }
    }
}
