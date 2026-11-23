package com.example.mykmp.domain.repository

import com.example.mykmp.domain.model.Product

interface ProductRepository {
    suspend fun fetchProducts(): List<Product>
}
