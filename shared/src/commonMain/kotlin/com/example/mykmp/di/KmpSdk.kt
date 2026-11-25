package com.example.mykmp.di

import com.example.mykmp.data.datasource.local.FavoriteLocalDataSource
import com.example.mykmp.data.datasource.local.ProductLocalDataSource
import com.example.mykmp.data.datasource.local.SqlDelightFavoriteLocalDataSource
import com.example.mykmp.data.datasource.local.SqlDelightProductLocalDataSource
import com.example.mykmp.data.datasource.local.db.AppDatabase
import com.example.mykmp.data.datasource.remote.ProductRemoteDataSource
import com.example.mykmp.data.datasource.remote.createHttpClient
import com.example.mykmp.data.repository.FavoriteRepositoryImpl
import com.example.mykmp.data.repository.ProductRepositoryImpl
import com.example.mykmp.domain.repository.ProductRepository
import com.example.mykmp.domain.repository.FavoriteRepository
import com.example.mykmp.domain.usecase.GetProductDetailUseCase
import com.example.mykmp.domain.usecase.ObserveFavoriteIdsUseCase
import com.example.mykmp.domain.usecase.ObserveProductsUseCase
import com.example.mykmp.domain.usecase.RefreshProductsUseCase
import com.example.mykmp.domain.usecase.ToggleFavoriteUseCase
import com.example.mykmp.presentation.mvi.PrintStoreLogger
import com.example.mykmp.presentation.mvi.StoreLogger
import com.example.mykmp.presentation.mvi.storeScope
import com.example.mykmp.presentation.productdetail.ProductDetailStore
import com.example.mykmp.presentation.productlist.ProductListStore
import kotlinx.coroutines.CoroutineScope
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.dsl.koinApplication
import org.koin.dsl.module

internal const val DATABASE_NAME = "mykmp.db"

// OS固有のモジュール（HttpClientEngine / SqlDriver / IO 用 Dispatcher を提供する）
internal expect fun platformModule(context: PlatformContext): Module

internal val sharedModule = module{
    single { createHttpClient(get()) }
    single { ProductRemoteDataSource(get()) }
    single { AppDatabase(get()) }
    single<ProductLocalDataSource> { SqlDelightProductLocalDataSource(get(), get()) }
    single<FavoriteLocalDataSource> { SqlDelightFavoriteLocalDataSource(get(), get()) }
    single<ProductRepository>{ ProductRepositoryImpl(get(), get()) }
    single<FavoriteRepository> { FavoriteRepositoryImpl(get()) }
    factory { ObserveProductsUseCase(get()) }
    factory { RefreshProductsUseCase(get()) }
    factory { ObserveFavoriteIdsUseCase(get()) }
    factory { ToggleFavoriteUseCase(get()) }
    factory { GetProductDetailUseCase(get()) }
}

// android、iosから利用するSDKのエンドポイント
object KmpSdk{
    private var koinApp: KoinApplication? = null

    /** Store のログの出力先。null ならログを出さない */
    private var storeLogger: StoreLogger? = null

    /**
     * アプリ起動時に一度だけ呼ぶ初期化メソッド
     * - Android: `KmpSdk.initialize(PlatformContext(this))`（Application から）
     * - iOS: `KmpSdk.shared.initialize(context: PlatformContext())`
     */
    fun initialize(context: PlatformContext){
        if(koinApp == null){
            koinApp = koinApplication{
                modules(platformModule(context),sharedModule)
            }
        }
    }

    /**
     * Store を通る Intent / Action / SideEffect をコンソールに出す。デバッグビルドでだけ呼ぶこと。
     * これ以降に作った Store からログが出る。
     * - Android: Application で `if (デバッグビルド) KmpSdk.enableLogging()`
     * - iOS: `#if DEBUG KmpSdk.shared.enableLogging() #endif`
     */
    fun enableLogging(){
        storeLogger = PrintStoreLogger
    }

    // 内部でkoinインスタンスを参照するためのプロパティ
    internal val koin
        get() = checkNotNull(koinApp){ "kmp.initialize()が呼ばれていません"}.koin

    /** 商品一覧の Store を作る。Android は ViewModel の viewModelScope を渡す */
    fun createProductListStore(scope: CoroutineScope): ProductListStore =
        ProductListStore(
            observeProducts = koin.get(),
            observeFavoriteIds = koin.get(),
            refreshProducts = koin.get(),
            toggleFavorite = koin.get(),
            scope = scope,
            logger = storeLogger,
        )

    /** 商品一覧の Store を作る（iOS 用）。不要になったら [ProductListStore.close] を呼ぶこと */
    fun createProductListStore(): ProductListStore = createProductListStore(storeScope())

    /** 商品詳細の Store を作る。Android は ViewModel の viewModelScope を渡す */
    fun createProductDetailStore(productId: String, scope: CoroutineScope): ProductDetailStore =
        ProductDetailStore(
            productId = productId,
            getProductDetail = koin.get(),
            observeFavoriteIds = koin.get(),
            toggleFavorite = koin.get(),
            scope = scope,
            logger = storeLogger,
        )

    /** 商品詳細の Store を作る（iOS 用）。不要になったら [ProductDetailStore.close] を呼ぶこと */
    fun createProductDetailStore(productId: String): ProductDetailStore =
        createProductDetailStore(productId, storeScope())
}
