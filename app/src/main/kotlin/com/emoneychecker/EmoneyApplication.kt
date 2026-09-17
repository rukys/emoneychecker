package com.emoneychecker

import android.app.Application
import com.emoneychecker.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class EmoneyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@EmoneyApplication)
            modules(appModule)
        }
    }
}
