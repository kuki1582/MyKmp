package com.example.mykmp.di

import android.content.Context

/**
 * Android では Context を包んで渡す。
 * Context は abstract クラスなので typealias にはできない（expect class と modality が合わない）。
 */
actual class PlatformContext(context: Context) {
    /** Activity などを保持し続けないよう、アプリ全体の Context だけを持つ */
    val applicationContext: Context = context.applicationContext
}
