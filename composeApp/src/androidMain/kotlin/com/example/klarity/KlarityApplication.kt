package com.example.klarity

import android.app.Application
import android.content.Context
import com.example.klarity.di.appModule
import org.koin.core.context.startKoin
import org.koin.dsl.module

/**
 * Android Application entry point. Starts Koin before the first Activity so the UI can resolve
 * repositories / ViewModels. The Android SQLDelight driver pulls a [Context] from Koin, so we
 * register the application context here (avoids needing the koin-android module).
 */
class KlarityApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            modules(
                module { single<Context> { this@KlarityApplication } },
                appModule,
            )
        }
    }
}
