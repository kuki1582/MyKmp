package com.example.mykmp.data.datasource.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.mykmp.data.datasource.local.db.AppDatabase
import com.example.mykmp.data.datasource.local.db.ProductEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * 商品の取得元（ローカル DB）。Repository からはこのインターフェースだけを見る。
 * テストでは偽物（Fake）に差し替えられる。
 */
internal interface ProductLocalDataSource {
    /** 保存済みの商品。DB が書き換わるたびに新しい一覧が流れてくる */
    fun observeProducts(): Flow<List<ProductEntity>>

    /** 保存済みの商品をすべて消して、渡した商品に入れ替える */
    suspend fun replaceProducts(products: List<ProductEntity>)
}

/** SQLDelight で実装したローカル DB */
internal class SqlDelightProductLocalDataSource(
    database: AppDatabase,
    private val ioDispatcher: CoroutineDispatcher,
) : ProductLocalDataSource {

    private val queries = database.productQueries

    override fun observeProducts(): Flow<List<ProductEntity>> =
        queries.selectAll().asFlow().mapToList(ioDispatcher)

    override suspend fun replaceProducts(products: List<ProductEntity>) = withContext(ioDispatcher) {
        // 途中で失敗したときに中途半端な状態にならないよう、1 つのトランザクションで入れ替える
        queries.transaction {
            queries.deleteAll()
            products.forEach { queries.insert(it) }
        }
    }
}
