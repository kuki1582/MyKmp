package com.example.mykmp.presentation.productdetail

import com.example.mykmp.domain.model.Product
import com.example.mykmp.domain.model.ProductDetail
import com.example.mykmp.domain.repository.FavoriteRepository
import com.example.mykmp.domain.repository.ProductRepository
import com.example.mykmp.domain.usecase.GetProductDetailUseCase
import com.example.mykmp.domain.usecase.ObserveFavoriteIdsUseCase
import com.example.mykmp.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ProductDetailStoreTest {
    private val detail = ProductDetail(
        id = "1", name = "医療機器A", description = "説明", categoryName = "医療", price = 120,
        discountPercentage = 10.0, rating = 4.5, stock = 3, brand = "ブランド", availabilityStatus = "In Stock",
        warrantyInformation = null, shippingInformation = null, returnPolicy = null,
        imageUrls = listOf("https://example.com/1.png"), reviews = emptyList(),
    )

    private val productRepository = FakeProductRepository()
    private val favoriteRepository = FakeFavoriteRepository()

    private fun TestScope.store(productId: String = "1") = ProductDetailStore(
        productId = productId,
        getProductDetail = GetProductDetailUseCase(productRepository),
        observeFavoriteIds = ObserveFavoriteIdsUseCase(favoriteRepository),
        toggleFavorite = ToggleFavoriteUseCase(favoriteRepository),
        scope = CoroutineScope(backgroundScope.coroutineContext + UnconfinedTestDispatcher(testScheduler)),
    )

    @Test
    fun load_showsDetailAndFavoriteState() = runTest {
        productRepository.detail = { detail }
        favoriteRepository.ids.value = setOf("1")
        val store = store()

        store.dispatch(ProductDetailIntent.Load)

        assertEquals(ProductDetailUiState(detail = detail, isFavorite = true), store.state.value)
    }

    @Test
    fun loadFailure_showsErrorAndRetryRecovers() = runTest {
        productRepository.detail = { error("network") }
        val store = store()
        store.dispatch(ProductDetailIntent.Load)
        assertEquals("商品の詳細を取得できませんでした", store.state.value.errorMessage)

        productRepository.detail = { detail }
        store.dispatch(ProductDetailIntent.Retry)

        assertEquals(ProductDetailUiState(detail = detail), store.state.value)
    }

    @Test
    fun toggleFavorite_updatesOnlyThisProduct() = runTest {
        productRepository.detail = { detail }
        val store = store()
        store.dispatch(ProductDetailIntent.Load)

        store.dispatch(ProductDetailIntent.ToggleFavorite)

        assertEquals(true, store.state.value.isFavorite)
        assertEquals(setOf("1"), favoriteRepository.ids.value)
    }

    @Test
    fun toggleFavoriteFailure_emitsShowMessage() = runTest {
        productRepository.detail = { detail }
        favoriteRepository.failToggle = true
        val store = store()
        store.dispatch(ProductDetailIntent.Load)

        store.dispatch(ProductDetailIntent.ToggleFavorite)

        assertEquals(
            ProductDetailSideEffect.ShowMessage("お気に入りを変更できませんでした"),
            store.sideEffect.first(),
        )
        assertEquals(false, store.state.value.isFavorite)
    }
}

private class FakeProductRepository : ProductRepository {
    var detail: () -> ProductDetail = { error("not set") }

    override fun observeProducts(): Flow<List<Product>> = emptyFlow()

    override suspend fun refreshProducts() = Unit

    override suspend fun getProductDetail(id: String): ProductDetail = detail()
}

private class FakeFavoriteRepository : FavoriteRepository {
    val ids = MutableStateFlow<Set<String>>(emptySet())
    var failToggle = false

    override fun observeFavoriteIds(): Flow<Set<String>> = ids

    override suspend fun toggleFavorite(productId: String) {
        if (failToggle) error("db error")
        ids.value = if (productId in ids.value) ids.value - productId else ids.value + productId
    }
}
