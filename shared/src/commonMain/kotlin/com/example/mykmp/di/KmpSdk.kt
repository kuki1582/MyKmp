package com.example.mykmp.di

import com.example.mykmp.data.repository.ProductRepositoryImpl
import com.example.mykmp.domain.repository.ProductRepository
import com.example.mykmp.domain.usecase.GetProductsUseCase
import com.example.mykmp.presentation.store.ProductListStore
import com.example.mykmp.presentation.store.storeScope
import kotlinx.coroutines.CoroutineScope
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.dsl.koinApplication
import org.koin.dsl.module

// OS固有のモジュール
internal expect val platformModule: Module

internal val sharedModule = module{
    single<ProductRepository>{ ProductRepositoryImpl(get()) }
    factory { GetProductsUseCase(get()) }
}

// android、iosから利用するSDKのエンドポイント
object KmpSdk{
    private var koinApp: KoinApplication? = null

    // アプリ起動時に一度だけ呼ぶ初期化メソッド
    fun initialize(){
        if(koinApp == null){
            koinApp = koinApplication{
                modules(platformModule,sharedModule)
            }
        }
    }

    // 内部でkoinインスタンスを参照するためのプロパティ
    internal val koin
        get() = checkNotNull(koinApp){ "kmp.initialize()が呼ばれていません"}.koin

    // android側から直接UseCaseを取得するメソッド
    fun getProductsUseCase(): GetProductsUseCase = koin.get()

    /** 商品一覧の Store を作る。Android は ViewModel の viewModelScope を渡す */
    fun createProductListStore(scope: CoroutineScope): ProductListStore =
        ProductListStore(getProductsUseCase(), scope)

    /** 商品一覧の Store を作る（iOS 用）。不要になったら [ProductListStore.close] を呼ぶこと */
    fun createProductListStore(): ProductListStore = createProductListStore(storeScope())
}
