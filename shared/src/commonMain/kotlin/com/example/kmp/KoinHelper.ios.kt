package com.example.kmp

import com.example.kmp.di.KmpSdk
import com.example.kmp.domain.usecase.GetProductsUseCase

object IosUseCaseProvider{
    fun initialize(){
        KmpSdk.initialize()
    }
    fun provideGetProductsUseCase(): GetProductsUseCase{
        return KmpSdk.getProductsUseCase()
    }
}
