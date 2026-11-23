package com.example.mykmp.data

import com.example.mykmp.data.datasource.remote.ProductRemoteDataSource
import com.example.mykmp.data.datasource.remote.createHttpClient
import com.example.mykmp.data.repository.ProductRepositoryImpl
import com.example.mykmp.domain.common.DomainResult
import com.example.mykmp.domain.model.Product
import com.example.mykmp.domain.usecase.GetProductsUseCase
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ProductRepositoryImplTest {

    // DummyJSON の実際のレスポンスと同じ形（select=title,price 指定時）
    private val json = """
        {
          "products": [
            { "id": 1, "title": "Essence Mascara Lash Princess", "price": 9.99 },
            { "id": 2, "title": "Eyeshadow Palette with Mirror", "price": 19.99 }
          ],
          "total": 194, "skip": 0, "limit": 2
        }
    """.trimIndent()

    private fun repository(engine: MockEngine) =
        ProductRepositoryImpl(ProductRemoteDataSource(createHttpClient(engine)))

    @Test
    fun fetchProducts_parsesAndMapsResponse() = runTest {
        var requestedUrl = ""
        val engine = MockEngine { request ->
            requestedUrl = request.url.toString()
            respond(json, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }

        val products = repository(engine).fetchProducts()

        assertEquals("https://dummyjson.com/products?limit=30&select=title%2Cprice", requestedUrl)
        assertEquals(
            listOf(
                Product(id = "1", name = "Essence Mascara Lash Princess", price = 10),
                Product(id = "2", name = "Eyeshadow Palette with Mirror", price = 20),
            ),
            products,
        )
    }

    @Test
    fun useCase_returnsError_whenServerFails() = runTest {
        val engine = MockEngine { respond("", HttpStatusCode.InternalServerError) }

        val result = GetProductsUseCase(repository(engine))()

        assertIs<DomainResult.Error>(result)
    }
}
