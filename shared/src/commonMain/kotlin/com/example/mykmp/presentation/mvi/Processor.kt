package com.example.mykmp.presentation.mvi

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * Processor が出す結果。
 * State を変えたいときは [Action]、1 回きりのイベントを UI に届けたいときは [SideEffect] を出す。
 */
internal sealed interface ProcessorOutput<out Action, out SideEffect> {
    data class Action<out A>(val action: A) : ProcessorOutput<A, Nothing>
    data class SideEffect<out E>(val sideEffect: E) : ProcessorOutput<Nothing, E>
}

/**
 * Intent を受け取り、UseCase 呼び出しなどの非同期処理をして結果を [ProcessorOutput] として流す。
 * 1 つの Intent から複数の Action を出してよい（例: LoadStarted → LoadSucceeded）。
 */
internal fun interface Processor<in Intent, out Action, out SideEffect> {
    fun process(intent: Intent): Flow<ProcessorOutput<Action, SideEffect>>

    /**
     * Store を作った直後に 1 回だけ動く処理（Bootstrapper）。
     * 監視の開始や最初の読み込みなど、UI から Intent を送らなくても始めたい処理を書く。
     * Store 自身が 1 回だけ呼ぶので、送り忘れや二重実行が起きない。
     */
    fun bootstrap(): Flow<ProcessorOutput<Action, SideEffect>> = emptyFlow()
}
