package com.example.mykmp

import android.app.Application
import com.example.mykmp.di.KmpSdk
import com.example.mykmp.di.PlatformContext

class MyKmpApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        KmpSdk.initialize(PlatformContext(this))
    }
}
