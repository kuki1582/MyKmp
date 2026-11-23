package com.example.mykmp

import android.app.Application
import com.example.mykmp.di.KmpSdk

class MyKmpApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        KmpSdk.initialize()
    }
}
