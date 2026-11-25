package com.example.mykmp.presentation.mvi

/**
 * Store を通る Intent / Action / SideEffect を記録する。
 * すべての画面の Action は Store を通るので、ここに 1 つ渡すだけで全画面の動きを追える。
 */
fun interface StoreLogger {
    fun log(message: String)
}

/** 標準出力に出すロガー（Android では Logcat の System.out、iOS では Xcode のコンソールに出る） */
internal val PrintStoreLogger = StoreLogger { println(it) }
