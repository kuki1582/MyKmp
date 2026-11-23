package com.example.mykmp.presentation.productlist

import com.example.mykmp.domain.model.Product
import com.example.mykmp.domain.repository.FavoriteRepository
import com.example.mykmp.domain.repository.ProductRepository
import com.example.mykmp.domain.usecase.ObserveFavoriteIdsUseCase
import com.example.mykmp.domain.usecase.ObserveProductsUseCase
import com.example.mykmp.domain.usecase.RefreshProductsUseCase
import com.example.mykmp.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ProductListStoreTest {
    private val productA = Product(id = "1", name = "医療機器A", price = 120, categoryName = "医療")
    private val productB = Product(id = "2", name = "医療機器B", price = 3400, categoryName = "医療")

    private val productRepository = FakeProductRepository()
    private val favoriteRepository = FakeFavoriteRepository()

    private fun TestScope.store() = ProductListStore(
        observeProducts = ObserveProductsUseCase(productRepository),
        observeFavoriteIds = ObserveFavoriteIdsUseCase(favoriteRepository),
        refreshProducts = RefreshProductsUseCase(productRepository),
        toggleFavorite = ToggleFavoriteUseCase(favoriteRepository),
        // dispatch した処理がすぐ進むように Unconfined にし、テスト終了時に止まる backgroundScope にぶら下げる
        scope = CoroutineScope(backgroundScope.coroutineContext + UnconfinedTestDispatcher(testScheduler)),
    )

    @Test
    fun load_combinesProductsAndFavorites() = runTest {
        productRepository.onRefresh = { productRepository.products.value = listOf(productA, productB) }
        favoriteRepository.ids.value = setOf("2")
        val store = store()

        store.dispatch(ProductListIntent.Load)

        assertEquals(
            ProductListUiState(
                items = listOf(
                    ProductListItem(productA, isFavorite = false),
                    ProductListItem(productB, isFavorite = true),
                ),
                favoriteCount = 1,
            ),
            store.state.value,
        )
    }

    @Test
    fun toggleFavorite_updatesItemsThroughCombinedFlow() = runTest {
        productRepository.products.value = listOf(productA, productB)
        val store = store()
        store.dispatch(ProductListIntent.Load)

        store.dispatch(ProductListIntent.ToggleFavorite("1"))

        assertEquals(listOf(true, false), store.state.value.items.map { it.isFavorite })
        assertEquals(1, store.state.value.favoriteCount)
    }

    @Test
    fun refreshFailure_showsErrorAndKeepsCachedItems() = runTest {
        productRepository.products.value = listOf(productA)
        productRepository.onRefresh = { error("network") }
        val store = store()

        store.dispatch(ProductListIntent.Load)

        assertEquals("失敗した", store.state.value.errorMessage)
        assertEquals(listOf(productA), store.state.value.items.map { it.product })
    }

    @Test
    fun toggleFavoriteFailure_emitsShowMessageAndKeepsItems() = runTest {
        productRepository.products.value = listOf(productA)
        favoriteRepository.failToggle = true
        val store = store()
        store.dispatch(ProductListIntent.Load)

        store.dispatch(ProductListIntent.ToggleFavorite("1"))

        assertEquals(
            ProductListSideEffect.ShowMessage("お気に入りを変更できませんでした"),
            store.sideEffect.first(),
        )
        assertEquals(listOf(false), store.state.value.items.map { it.isFavorite })
    }

    @Test
    fun clickProduct_emitsNavigateSideEffect() = runTest {
        val store = store()

        store.dispatch(ProductListIntent.ClickProduct("1"))

        assertEquals(ProductListSideEffect.NavigateToDetail("1"), store.sideEffect.first())
    }
}

private class FakeProductRepository : ProductRepository {
    val products = MutableStateFlow<List<Product>>(emptyList())
    var onRefresh: () -> Unit = {}

    override fun observeProducts(): Flow<List<Product>> = products

    override suspend fun refreshProducts() = onRefresh()

    override suspend fun getProductDetail(id: String) = error("一覧のテストでは使わない")
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
