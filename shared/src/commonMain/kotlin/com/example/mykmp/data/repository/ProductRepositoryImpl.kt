package com.example.mykmp.data.repository

import com.example.mykmp.data.datasource.local.ProductLocalDataSource
import com.example.mykmp.data.datasource.remote.ProductRemoteDataSource
import com.example.mykmp.data.mapper.toDomain
import com.example.mykmp.data.mapper.toEntity
import com.example.mykmp.domain.model.Product
import com.example.mykmp.domain.repository.ProductRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 商品の Repository。API（remote）と DB（local）の両方を使う。
 *
 * 画面に出すデータは常に DB から流す（DB を唯一の情報源にする）。
 * - [observeProducts]：DB の商品を Flow で流す。DB が書き換わると自動で新しい一覧が流れる
 * - [refreshProducts]：API から取得して DB に保存する。失敗しても DB の前回の結果は表示されたまま
 */
internal class ProductRepositoryImpl(
    private val remoteDataSource: ProductRemoteDataSource,
    private val localDataSource: ProductLocalDataSource,
) : ProductRepository {

    override fun observeProducts(): Flow<List<Product>> =
        localDataSource.observeProducts().map { entities -> entities.map { it.toDomain() } }

    override suspend fun refreshProducts() {
        val products = fetchFromRemote()
        localDataSource.replaceProducts(products.map { it.toEntity() })
    }

    /**
     * 2 つの API の結果を一部ずつ使って Product を作る。
     * - products API   → id / title / price / category(slug)
     * - categories API → slug / name（カテゴリの表示名）
     */
    private suspend fun fetchFromRemote(): List<Product> = coroutineScope {
        // 2 つの API は互いに依存しないので並行に呼ぶ。どちらかが失敗すればもう片方もキャンセルされる
        val productsDeferred = async { remoteDataSource.getProducts() }
        val categoriesDeferred = async { remoteDataSource.getCategories() }

        val categoryNames = categoriesDeferred.await().associate { it.slug to it.name }
        productsDeferred.await().products.map { it.toDomain(categoryNames) }
    }
}
