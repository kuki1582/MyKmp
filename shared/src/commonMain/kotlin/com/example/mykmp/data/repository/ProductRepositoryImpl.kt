package com.example.mykmp.data.repository

import com.example.mykmp.data.datasource.local.ProductLocalDataSource
import com.example.mykmp.data.datasource.remote.ProductRemoteDataSource
import com.example.mykmp.data.mapper.toDomain
import com.example.mykmp.data.mapper.toEntity
import com.example.mykmp.domain.model.Product
import com.example.mykmp.domain.repository.ProductRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.cancellation.CancellationException

/**
 * 商品の Repository。API（remote）と DB（local）の両方を使う。
 *
 * 1. API から取得できたら DB に保存して、その結果を返す
 * 2. API が失敗したら、DB に保存してある前回の結果を返す（オフラインでも表示できる）
 * 3. DB も空なら、API のエラーをそのまま投げる（UseCase で DomainResult.Error になる）
 */
internal class ProductRepositoryImpl(
    private val remoteDataSource: ProductRemoteDataSource,
    private val localDataSource: ProductLocalDataSource,
) : ProductRepository {

    override suspend fun fetchProducts(): List<Product> =
        try {
            val products = fetchFromRemote()
            localDataSource.replaceProducts(products.map { it.toEntity() })
            products
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val cached = localDataSource.getProducts()
            if (cached.isEmpty()) throw e
            cached.map { it.toDomain() }
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
