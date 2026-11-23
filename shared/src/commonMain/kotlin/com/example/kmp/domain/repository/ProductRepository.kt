package com.example.kmp.domain.repository

import com.example.kmp.domain.model.Product

interface ProductRepository {
    suspend fun fetchProducts(): List<Product>
}
