package com.example.kmp.domain.usecase

import com.example.kmp.domain.common.DomainResult
import com.example.kmp.domain.model.Product
import com.example.kmp.domain.repository.ProductRepository

class GetProductsUseCase(
    private val repository: ProductRepository
){
    suspend operator fun invoke(): DomainResult<List<Product>> {
        return try{
            val products = repository.fetchProducts()
            DomainResult.Success(products)
        }catch(e: Exception){
            DomainResult.Error("失敗した")
        }
    }
}


