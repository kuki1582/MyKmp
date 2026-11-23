package com.example.mykmp.data

import com.example.mykmp.data.datasource.remote.ProductRemoteDataSource
import com.example.mykmp.data.datasource.remote.createHttpClient
import com.example.mykmp.data.repository.ProductRepositoryImpl
import com.example.mykmp.domain.common.DomainResult
import com.example.mykmp.domain.model.Product
import com.example.mykmp.domain.usecase.GetProductsUseCase
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
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

    private fun repository(engine: MockEngine) =
        ProductRepositoryImpl(ProductRemoteDataSource(createHttpClient(engine)))

    @Test
    fun fetchProducts_combinesProductsAndCategories() = runTest {
        val requested = mutableListOf<String>()
        val engine = MockEngine { request ->
            requested += request.url.toString()
            when (request.url.encodedPath) {
                "/products" -> respond(productsJson, HttpStatusCode.OK, jsonHeaders)
                "/products/categories" -> respond(categoriesJson, HttpStatusCode.OK, jsonHeaders)
                else -> respondError(HttpStatusCode.NotFound)
            }
        }

        val products = repository(engine).fetchProducts()

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
            products,
        )
    }

    @Test
    fun useCase_returnsError_whenCategoriesApiFails() = runTest {
        val engine = MockEngine { request ->
            when (request.url.encodedPath) {
                "/products" -> respond(productsJson, HttpStatusCode.OK, jsonHeaders)
                else -> respondError(HttpStatusCode.InternalServerError)
            }
        }

        val result = GetProductsUseCase(repository(engine))()

        assertIs<DomainResult.Error>(result)
    }
}
