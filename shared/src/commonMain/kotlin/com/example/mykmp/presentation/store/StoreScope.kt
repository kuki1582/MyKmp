package com.example.mykmp.presentation.store

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * 自分で寿命を管理する Store（iOS など）用の CoroutineScope を作る。
 *
 * Android の viewModelScope と同じ構成にしている。
 * - SupervisorJob: 1 つの dispatch が例外で落ちても、ほかの処理や以降の Intent を止めない
 * - Dispatchers.Main.immediate: State 更新をメインスレッドで行う。すでにメインスレッドにいるときは
 *   キューに積まずその場で実行する
 *
 * この scope で作った Store は、不要になったら必ず close() すること。
 */
internal fun storeScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
