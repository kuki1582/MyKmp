package com.example.mykmp.data.datasource.remote

import com.example.mykmp.data.model.CategoryDto
import com.example.mykmp.data.model.ProductDetailDto
import com.example.mykmp.data.model.ProductsResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/**
 * 商品の取得元（リモート）。DummyJSON の API を Ktor Client で呼ぶ。
 * HTTP の呼び出しと JSON → DTO の変換だけを担当し、ドメインモデルへの変換は Repository で行う。
 * https://dummyjson.com/docs/products
 */
internal class ProductRemoteDataSource(
    private val httpClient: HttpClient,
) {
    /** 商品一覧。使うのは title / price / category だけ */
    suspend fun getProducts(): ProductsResponseDto =
        httpClient.get("$BASE_URL/products") {
            parameter("limit", 30)
            parameter("select", "title,price,category")
        }.body()

    /** 商品 1 件の詳細。全項目を取る */
    suspend fun getProduct(id: String): ProductDetailDto =
        httpClient.get("$BASE_URL/products/$id").body()

    /** カテゴリ一覧。使うのは slug / name だけ */
    suspend fun getCategories(): List<CategoryDto> =
        httpClient.get("$BASE_URL/products/categories").body()

    companion object {
        const val BASE_URL = "https://dummyjson.com"
    }
}
