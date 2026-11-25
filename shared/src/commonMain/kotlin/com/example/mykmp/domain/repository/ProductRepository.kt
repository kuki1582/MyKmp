package com.example.mykmp.domain.repository

import com.example.mykmp.domain.model.Product
import com.example.mykmp.domain.model.ProductDetail
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    /** 保存済みの商品を流し続ける。更新されるたびに新しい一覧が流れる */
    fun observeProducts(): Flow<List<Product>>

    /**
     * 最新の商品を取得して保存する。失敗したら例外を投げる。
     *
     * @param force true なら必ず取得する。false なら、最近取得したばかりのときは何もしない
     */
    suspend fun refreshProducts(force: Boolean)

    /** 商品 1 件の詳細を取得する。失敗したら例外を投げる */
    suspend fun getProductDetail(id: String): ProductDetail
}
