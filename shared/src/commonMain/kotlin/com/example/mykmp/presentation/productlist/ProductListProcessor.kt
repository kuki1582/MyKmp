package com.example.mykmp.presentation.productlist

import com.example.mykmp.domain.common.DomainResult
import com.example.mykmp.domain.usecase.ObserveFavoriteIdsUseCase
import com.example.mykmp.domain.usecase.ObserveProductsUseCase
import com.example.mykmp.domain.usecase.RefreshProductsUseCase
import com.example.mykmp.domain.usecase.ToggleFavoriteUseCase
import com.example.mykmp.presentation.mvi.Processor
import com.example.mykmp.presentation.mvi.ProcessorOutput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private typealias Output = ProcessorOutput<ProductListAction, ProductListSideEffect>

internal class ProductListProcessor(
    private val observeProducts: ObserveProductsUseCase,
    private val observeFavoriteIds: ObserveFavoriteIdsUseCase,
    private val refreshProducts: RefreshProductsUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
) : Processor<ProductListIntent, ProductListAction, ProductListSideEffect> {

    override fun process(intent: ProductListIntent): Flow<Output> =
        when (intent) {
            // 監視（終わらない Flow）と取得（1 回で終わる処理）を同時に動かすので channelFlow を使う
            ProductListIntent.Load -> channelFlow {
                launch { items().collect { send(it) } }
                refresh { send(it) }
            }

            ProductListIntent.Refresh -> flow { refresh { emit(it) } }

            is ProductListIntent.ClickProduct ->
                flow { emit(ProcessorOutput.SideEffect(ProductListSideEffect.NavigateToDetail(intent.id))) }

            // DB を書き換えるだけ。結果は items() の Flow を通って自動で画面に反映される
            is ProductListIntent.ToggleFavorite -> flow { toggleFavorite(intent.id) }
        }

    /**
     * 2 つの UseCase の Flow を combine して 1 つの一覧にする。
     * どちらかの Flow に新しい値が来るたびに、両方の最新値で作り直される。
     */
    private fun items(): Flow<Output> =
        combine(observeProducts(), observeFavoriteIds()) { products, favoriteIds ->
            products.map { ProductListItem(product = it, isFavorite = it.id in favoriteIds) }
        }
            .distinctUntilChanged()
            // map で一覧から画面用の値（お気に入り件数）を計算し、Action に変換する
            .map { items ->
                ProcessorOutput.Action(
                    ProductListAction.ItemsUpdated(
                        items = items,
                        favoriteCount = items.count { it.isFavorite },
                    ),
                )
            }

    private suspend fun refresh(send: suspend (Output) -> Unit) {
        send(ProcessorOutput.Action(ProductListAction.RefreshStarted))
        when (val result = refreshProducts()) {
            is DomainResult.Success -> send(ProcessorOutput.Action(ProductListAction.RefreshSucceeded))
            is DomainResult.Error -> send(ProcessorOutput.Action(ProductListAction.RefreshFailed(result.message)))
        }
    }
}
