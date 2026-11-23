package com.example.mykmp.presentation.processor

import kotlinx.coroutines.flow.Flow

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
}
