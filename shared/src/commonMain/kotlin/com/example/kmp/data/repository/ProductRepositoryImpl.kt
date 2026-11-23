package com.example.kmp.data.repository

import com.example.kmp.domain.model.Product
import com.example.kmp.domain.repository.ProductRepository
import io.ktor.client.HttpClient

internal class ProductRepositoryImpl(
    private val httpClient: HttpClient
): ProductRepository{
    override suspend fun fetchProducts(): List<Product> {
        return listOf(
            Product(id = "1","医療機器A",120)
        )
    }
}
