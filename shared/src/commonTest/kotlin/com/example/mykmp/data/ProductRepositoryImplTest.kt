package com.example.mykmp.data

import com.example.mykmp.data.datasource.local.ProductLocalDataSource
import com.example.mykmp.data.datasource.local.db.ProductEntity
import com.example.mykmp.data.datasource.remote.ProductRemoteDataSource
import com.example.mykmp.data.datasource.remote.createHttpClient
import com.example.mykmp.data.mapper.toDomain
import com.example.mykmp.data.mapper.toEntity
import com.example.mykmp.data.repository.ProductRepositoryImpl
import com.example.mykmp.domain.common.DomainResult
import com.example.mykmp.domain.model.Product
import com.example.mykmp.domain.model.Review
import com.example.mykmp.domain.usecase.RefreshProductsUseCase
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.time.Duration.Companion.minutes
import kotlin.time.TestTimeSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ProductRepositoryImplTest {

    // DummyJSON の実際のレスポンスと同じ形（select=title,price,category 指定時）
    private val productsJson = """
        {
          "products": [
            { "id": 1, "title": "Essence Mascara Lash Princess", "price": 9.99, "category": "beauty" },
            { "id": 6, "title": "Calvin Klein CK One", "price": 49.99, "category": "fragrances" }
          ],
          "total": 194, "skip": 0, "limit": 2
        }
    """.trimIndent()

    private val categoriesJson = """
        [
          { "slug": "beauty", "name": "Beauty", "url": "https://dummyjson.com/products/category/beauty" },
          { "slug": "fragrances", "name": "Fragrances", "url": "https://dummyjson.com/products/category/fragrances" }
        ]
    """.trimIndent()

    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

    private val local = FakeProductLocalDataSource()

    private fun repository(engine: MockEngine) =
        ProductRepositoryImpl(ProductRemoteDataSource(createHttpClient(engine)), local)

    private val failingEngine = MockEngine { respondError(HttpStatusCode.InternalServerError) }

    private val cachedProduct = Product(id = "99", name = "キャッシュ済みの商品", price = 5, categoryName = "Cache")

    @Test
    fun refreshProducts_combinesTwoApisAndSavesToLocal() = runTest {
        val requested = mutableListOf<String>()
        val engine = MockEngine { request ->
            requested += request.url.toString()
            when (request.url.encodedPath) {
                "/products" -> respond(productsJson, HttpStatusCode.OK, jsonHeaders)
                "/products/categories" -> respond(categoriesJson, HttpStatusCode.OK, jsonHeaders)
                else -> respondError(HttpStatusCode.NotFound)
            }
        }

        repository(engine).refreshProducts(force = true)

        assertEquals(
            setOf(
                "https://dummyjson.com/products?limit=30&select=title%2Cprice%2Ccategory",
                "https://dummyjson.com/products/categories",
            ),
            requested.toSet(),
        )
        assertEquals(
            listOf(
                Product(id = "1", name = "Essence Mascara Lash Princess", price = 10, categoryName = "Beauty"),
                Product(id = "6", name = "Calvin Klein CK One", price = 50, categoryName = "Fragrances"),
            ),
            local.products.value.map { it.toDomain() },
        )
    }

    @Test
    fun getProductDetail_combinesDetailAndCategoryName() = runTest {
        val detailJson = """
            {
              "id": 1, "title": "Essence Mascara Lash Princess", "description": "Popular mascara",
              "category": "beauty", "price": 9.99, "discountPercentage": 7.17, "rating": 4.94, "stock": 5,
              "brand": "Essence", "availabilityStatus": "Low Stock",
              "images": ["https://cdn.dummyjson.com/1.png"],
              "reviews": [{ "rating": 2, "comment": "Very unhappy!", "reviewerName": "John Doe", "date": "2024-05-23T08:56:21.618Z" }]
            }
        """.trimIndent()
        val engine = MockEngine { request ->
            when (request.url.encodedPath) {
                "/products/1" -> respond(detailJson, HttpStatusCode.OK, jsonHeaders)
                "/products/categories" -> respond(categoriesJson, HttpStatusCode.OK, jsonHeaders)
                else -> respondError(HttpStatusCode.NotFound)
            }
        }

        val detail = repository(engine).getProductDetail("1")

        assertEquals("Beauty", detail.categoryName)
        assertEquals(10, detail.price)
        assertEquals("Essence", detail.brand)
        assertEquals(listOf("https://cdn.dummyjson.com/1.png"), detail.imageUrls)
        assertEquals(Review(rating = 2, comment = "Very unhappy!", reviewerName = "John Doe", date = "2024-05-23"), detail.reviews.single())
    }

    @Test
    fun refreshProducts_skipsApiWhileCacheIsFresh_unlessForced() = runTest {
        var productsRequests = 0
        val engine = MockEngine { request ->
            when (request.url.encodedPath) {
                "/products" -> {
                    productsRequests++
                    respond(productsJson, HttpStatusCode.OK, jsonHeaders)
                }
                "/products/categories" -> respond(categoriesJson, HttpStatusCode.OK, jsonHeaders)
                else -> respondError(HttpStatusCode.NotFound)
            }
        }
        val timeSource = TestTimeSource()
        val repository = ProductRepositoryImpl(
            ProductRemoteDataSource(createHttpClient(engine)), local, timeSource, cacheDuration = 5.minutes,
        )

        repository.refreshProducts(force = false) // 初回：まだ取得していないので呼ぶ
        timeSource += 4.minutes
        repository.refreshProducts(force = false) // 5 分以内：呼ばない
        repository.refreshProducts(force = true)  // 強制：呼ぶ
        timeSource += 6.minutes
        repository.refreshProducts(force = false) // 前回の強制取得から 5 分を過ぎた：呼ぶ

        assertEquals(3, productsRequests)
    }

    @Test
    fun observeProducts_emitsProductsFromLocal() = runTest {
        local.products.value = listOf(cachedProduct.toEntity())

        assertEquals(listOf(cachedProduct), repository(failingEngine).observeProducts().first())
    }

    @Test
    fun refreshFailure_keepsCachedProducts() = runTest {
        local.products.value = listOf(cachedProduct.toEntity())

        val result = RefreshProductsUseCase(repository(failingEngine))(force = true)

        assertIs<DomainResult.Error>(result)
        assertEquals(listOf(cachedProduct.toEntity()), local.products.value)
    }

    @Test
    fun refresh_returnsError_whenCategoriesApiFails() = runTest {
        val engine = MockEngine { request ->
            when (request.url.encodedPath) {
                "/products" -> respond(productsJson, HttpStatusCode.OK, jsonHeaders)
                else -> respondError(HttpStatusCode.InternalServerError)
            }
        }

        val result = RefreshProductsUseCase(repository(engine))(force = true)

        assertIs<DomainResult.Error>(result)
    }
}

/** テスト用のローカル DB。メモリ上に保存するだけ */
private class FakeProductLocalDataSource : ProductLocalDataSource {
    val products = MutableStateFlow<List<ProductEntity>>(emptyList())

    override fun observeProducts(): Flow<List<ProductEntity>> = products

    override suspend fun replaceProducts(products: List<ProductEntity>) {
        this.products.value = products
    }
}
