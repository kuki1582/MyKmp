package com.example.mykmp.presentation.reducer

/** 今の State と Action から次の State を作る。副作用（通信・ログ・時刻取得など）を持たない純粋関数にすること。 */
internal fun interface Reducer<State, in Action> {
    fun reduce(state: State, action: Action): State
}
