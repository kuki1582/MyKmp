package com.example.mykmp.domain.repository

import com.example.mykmp.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    /** 保存済みの商品を流し続ける。更新されるたびに新しい一覧が流れる */
    fun observeProducts(): Flow<List<Product>>

    /** 最新の商品を取得して保存する。失敗したら例外を投げる */
    suspend fun refreshProducts()
}
