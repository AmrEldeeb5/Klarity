package com.example.sentio.di

import com.example.sentio.data.local.DatabaseDriverFactory
import org.koin.dsl.module

actual fun platformModule() = module {
    single { DatabaseDriverFactory().createDriver() }
}
