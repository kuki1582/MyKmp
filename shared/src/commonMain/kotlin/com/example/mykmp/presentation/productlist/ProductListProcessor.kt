package com.example.mykmp.presentation.productlist

import com.example.mykmp.domain.common.DomainResult
import com.example.mykmp.domain.usecase.GetProductsUseCase
import com.example.mykmp.presentation.mvi.Processor
import com.example.mykmp.presentation.mvi.ProcessorOutput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class ProductListProcessor(
    private val getProducts: GetProductsUseCase,
) : Processor<ProductListIntent, ProductListAction, ProductListSideEffect> {

    override fun process(
        intent: ProductListIntent,
    ): Flow<ProcessorOutput<ProductListAction, ProductListSideEffect>> = flow {
        when (intent) {
            ProductListIntent.Load -> {
                emit(ProcessorOutput.Action(ProductListAction.LoadStarted))
                when (val result = getProducts()) {
                    is DomainResult.Success ->
                        emit(ProcessorOutput.Action(ProductListAction.LoadSucceeded(result.data)))

                    is DomainResult.Error ->
                        emit(ProcessorOutput.Action(ProductListAction.LoadFailed(result.message)))
                }
            }

            is ProductListIntent.ClickProduct ->
                emit(ProcessorOutput.SideEffect(ProductListSideEffect.NavigateToDetail(intent.id)))
        }
    }
}
