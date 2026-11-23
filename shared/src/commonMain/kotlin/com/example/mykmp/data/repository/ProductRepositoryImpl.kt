package com.example.mykmp.data.repository

import com.example.mykmp.domain.model.Product
import com.example.mykmp.domain.repository.ProductRepository
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
