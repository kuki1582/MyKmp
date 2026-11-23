package com.example.mykmp.domain.usecase

import com.example.mykmp.domain.model.Product
import com.example.mykmp.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow

/** 商品一覧を流し続ける */
class ObserveProductsUseCase(
    private val repository: ProductRepository,
) {
    operator fun invoke(): Flow<List<Product>> = repository.observeProducts()
}
