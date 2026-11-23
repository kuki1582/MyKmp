package com.example.mykmp.domain.usecase

import com.example.mykmp.domain.common.DomainResult
import com.example.mykmp.domain.model.Product
import com.example.mykmp.domain.repository.ProductRepository

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


