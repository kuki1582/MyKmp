package com.example.mykmp

import android.app.Application
import android.content.pm.ApplicationInfo
import com.example.mykmp.di.KmpSdk
import com.example.mykmp.di.PlatformContext

class MyKmpApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        KmpSdk.initialize(PlatformContext(this))
        // デバッグビルドのときだけ、Store の Intent / Action / SideEffect を Logcat に出す
        if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) {
            KmpSdk.enableLogging()
        }
    }
}
