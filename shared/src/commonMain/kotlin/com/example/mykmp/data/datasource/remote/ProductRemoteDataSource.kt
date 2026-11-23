package com.example.mykmp.data.datasource.remote

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
    suspend fun getProducts(): ProductsResponseDto =
        httpClient.get("$BASE_URL/products") {
            parameter("limit", 30)
            parameter("select", "title,price")
        }.body()

    companion object {
        const val BASE_URL = "https://dummyjson.com"
    }
}
