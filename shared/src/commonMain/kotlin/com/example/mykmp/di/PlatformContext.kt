package com.example.mykmp.di

/**
 * OS ごとの初期化に必要なもの。
 * Android では Context（DB ファイルの作成に必要）を包み、iOS では何も持たない。
 */
expect class PlatformContext
