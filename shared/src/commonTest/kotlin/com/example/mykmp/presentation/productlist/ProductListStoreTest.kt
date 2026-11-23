package com.example.mykmp.presentation.productlist

import com.example.mykmp.domain.model.Product
import com.example.mykmp.domain.repository.ProductRepository
import com.example.mykmp.domain.usecase.GetProductsUseCase
import com.example.mykmp.presentation.mvi.ProcessorOutput
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

class ProductListStoreTest {
    private val products = listOf(Product(id = "1", name = "医療機器A", price = 120, categoryName = "医療"))

    private fun useCase(result: () -> List<Product>) = GetProductsUseCase(
        object : ProductRepository {
            override suspend fun fetchProducts(): List<Product> = result()
        },
    )

    @Test
    fun processor_load_emitsStartedThenSucceeded() = runTest {
        val processor = ProductListProcessor(useCase { products })

        val outputs = processor.process(ProductListIntent.Load).toList()

        assertEquals(
            listOf(
                ProcessorOutput.Action(ProductListAction.LoadStarted),
                ProcessorOutput.Action(ProductListAction.LoadSucceeded(products)),
            ),
            outputs,
        )
    }

    @Test
    fun processor_load_emitsFailedOnError() = runTest {
        val processor = ProductListProcessor(useCase { error("network") })

        val outputs = processor.process(ProductListIntent.Load).toList()

        assertEquals(
            ProcessorOutput.Action(ProductListAction.LoadFailed("失敗した")),
            outputs.last(),
        )
    }

    @Test
    fun store_load_updatesState() = runTest {
        val store = ProductListStore(useCase { products }, storeScope())

        store.dispatch(ProductListIntent.Load)

        assertEquals(ProductListUiState(products = products), store.state.value)
    }

    @Test
    fun store_clickProduct_emitsNavigateSideEffect() = runTest {
        val store = ProductListStore(useCase { products }, storeScope())

        store.dispatch(ProductListIntent.ClickProduct("1"))

        assertEquals(ProductListSideEffect.NavigateToDetail("1"), store.sideEffect.first())
    }


    /** dispatch した処理がすぐ終わるように Unconfined にし、テスト終了時に止まる backgroundScope にぶら下げる */
    private fun TestScope.storeScope() =
        CoroutineScope(backgroundScope.coroutineContext + UnconfinedTestDispatcher(testScheduler))
}
