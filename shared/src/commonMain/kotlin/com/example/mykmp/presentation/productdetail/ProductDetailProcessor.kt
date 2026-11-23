package com.example.mykmp.presentation.productdetail

import com.example.mykmp.domain.common.DomainResult
import com.example.mykmp.domain.usecase.GetProductDetailUseCase
import com.example.mykmp.domain.usecase.ObserveFavoriteIdsUseCase
import com.example.mykmp.domain.usecase.ToggleFavoriteUseCase
import com.example.mykmp.presentation.mvi.Processor
import com.example.mykmp.presentation.mvi.ProcessorOutput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private typealias Output = ProcessorOutput<ProductDetailAction, ProductDetailSideEffect>

/**
 * @param productId この画面で表示する商品。Intent ではなく Store を作るときに決める
 */
internal class ProductDetailProcessor(
    private val productId: String,
    private val getProductDetail: GetProductDetailUseCase,
    private val observeFavoriteIds: ObserveFavoriteIdsUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
) : Processor<ProductDetailIntent, ProductDetailAction, ProductDetailSideEffect> {

    override fun process(intent: ProductDetailIntent): Flow<Output> =
        when (intent) {
            // お気に入りの監視（終わらない Flow）と詳細の取得を同時に動かすので channelFlow を使う
            ProductDetailIntent.Load -> channelFlow {
                launch { favoriteChanges().collect { send(it) } }
                load { send(it) }
            }

            ProductDetailIntent.Retry -> flow { load { emit(it) } }

            ProductDetailIntent.ToggleFavorite -> flow {
                when (val result = toggleFavorite(productId)) {
                    // 成功時は何も返さない。DB が変わり、監視中の favoriteChanges() から FavoriteChanged が届く
                    is DomainResult.Success -> Unit
                    is DomainResult.Error ->
                        emit(ProcessorOutput.SideEffect(ProductDetailSideEffect.ShowMessage(result.message)))
                }
            }
        }

    /**
     * お気に入り ID の集合を、この商品がお気に入りかどうかに map する。
     * 一覧画面と同じ Flow なので、一覧で切り替えてもこちらで切り替えても、両方の画面に反映される。
     */
    private fun favoriteChanges(): Flow<Output> =
        observeFavoriteIds()
            .map { favoriteIds -> productId in favoriteIds }
            .distinctUntilChanged()
            .map { ProcessorOutput.Action(ProductDetailAction.FavoriteChanged(it)) }

    private suspend fun load(send: suspend (Output) -> Unit) {
        send(ProcessorOutput.Action(ProductDetailAction.LoadStarted))
        when (val result = getProductDetail(productId)) {
            is DomainResult.Success -> send(ProcessorOutput.Action(ProductDetailAction.LoadSucceeded(result.data)))
            is DomainResult.Error -> send(ProcessorOutput.Action(ProductDetailAction.LoadFailed(result.message)))
        }
    }
}
