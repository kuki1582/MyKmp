package com.example.mykmp

import com.example.mykmp.di.KmpSdk
import com.example.mykmp.domain.usecase.GetProductsUseCase

object IosUseCaseProvider{
    fun initialize(){
        KmpSdk.initialize()
    }
    fun provideGetProductsUseCase(): GetProductsUseCase{
        return KmpSdk.getProductsUseCase()
    }
}
