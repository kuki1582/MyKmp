package com.example.mykmp.presentation.mvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * MVI の汎用 Store。Android / iOS の仕組みには依存しない。
 *
 * UI ──Intent──▶ Processor ──Action──▶ Reducer ──State──▶ UI
 *                    └──SideEffect──▶ UI
 *
 * 画面ごとには [Processor] と [Reducer] を書いてこのクラスに渡すだけでよい。
 * Action は Store の内部だけで使うので、このクラスも internal にしている。
 * UI に公開する画面ごとの Store（例: ProductListStore）は、このクラスに処理を任せる。
 *
 * 作った直後に [Processor.bootstrap] を 1 回だけ動かす。
 *
 * @param name ログに出す名前（例: "ProductList"）
 * @param logger null ならログを出さない
 */
internal class Store<Intent : Any, Action : Any, State : Any, SideEffect : Any>(
    private val name: String,
    initialState: State,
    private val processor: Processor<Intent, Action, SideEffect>,
    private val reducer: Reducer<State, Action>,
    private val scope: CoroutineScope,
    private val logger: StoreLogger? = null,
) {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state.asStateFlow()

    // 受け取り手がいない間に出た SideEffect も取りこぼさないようにバッファしておく
    private val _sideEffect = Channel<SideEffect>(Channel.BUFFERED)
    val sideEffect: Flow<SideEffect> = _sideEffect.receiveAsFlow()

    init {
        log("Bootstrap")
        scope.launch { processor.bootstrap().collect(::handle) }
    }

    fun dispatch(intent: Intent) {
        log("Intent: $intent")
        scope.launch { processor.process(intent).collect(::handle) }
    }

    /** scope を止める。scope を自分で管理している場合（iOS など）は画面を閉じるときに呼ぶ。 */
    fun close() {
        log("Close")
        scope.cancel()
    }

    private suspend fun handle(output: ProcessorOutput<Action, SideEffect>) {
        when (output) {
            is ProcessorOutput.Action -> {
                log("Action: ${output.action}")
                _state.update { reducer.reduce(it, output.action) }
            }

            is ProcessorOutput.SideEffect -> {
                log("SideEffect: ${output.sideEffect}")
                _sideEffect.send(output.sideEffect)
            }
        }
    }

    private fun log(message: String) {
        logger?.log("[$name] $message")
    }
}
