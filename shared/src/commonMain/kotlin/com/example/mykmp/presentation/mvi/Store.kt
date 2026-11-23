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
 */
internal class Store<Intent : Any, Action : Any, State : Any, SideEffect : Any>(
    initialState: State,
    private val processor: Processor<Intent, Action, SideEffect>,
    private val reducer: Reducer<State, Action>,
    private val scope: CoroutineScope,
) {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state.asStateFlow()

    // 受け取り手がいない間に出た SideEffect も取りこぼさないようにバッファしておく
    private val _sideEffect = Channel<SideEffect>(Channel.BUFFERED)
    val sideEffect: Flow<SideEffect> = _sideEffect.receiveAsFlow()

    fun dispatch(intent: Intent) {
        scope.launch {
            processor.process(intent).collect { output ->
                when (output) {
                    is ProcessorOutput.Action -> _state.update { reducer.reduce(it, output.action) }
                    is ProcessorOutput.SideEffect -> _sideEffect.send(output.sideEffect)
                }
            }
        }
    }

    /** scope を止める。scope を自分で管理している場合（iOS など）は画面を閉じるときに呼ぶ。 */
    fun close() {
        scope.cancel()
    }
}
