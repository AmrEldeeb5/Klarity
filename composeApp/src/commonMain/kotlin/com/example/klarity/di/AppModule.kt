package com.example.klarity.di

import app.cash.sqldelight.db.SqlDriver
import com.example.klarity.data.ai.AiService
import com.example.klarity.data.repositories.SqlDelightFolderRepository
import com.example.klarity.data.repositories.SqlDelightNoteRepository
import com.example.klarity.data.repositories.SqlDelightSettingsRepository
import com.example.klarity.data.repositories.SqlDelightTagRepository
import com.example.klarity.data.repositories.SqlDelightTaskRepository
import com.example.klarity.data.util.DefaultDispatcherProvider
import com.example.klarity.data.util.DispatcherProvider
import com.example.klarity.db.KlarityDatabase
import com.example.klarity.domain.repositories.FolderRepository
import com.example.klarity.domain.repositories.NoteRepository
import com.example.klarity.domain.repositories.SettingsRepository
import com.example.klarity.domain.repositories.TagRepository
import com.example.klarity.domain.repositories.TaskRepository
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import com.example.klarity.domain.usecase.CreateNoteUseCase
import com.example.klarity.domain.usecase.DeleteNoteUseCase
import com.example.klarity.domain.usecase.NoteUseCases
import com.example.klarity.domain.usecase.SearchNotesUseCase
import com.example.klarity.domain.usecase.UpdateNoteUseCase
import com.example.klarity.presentation.WorkspaceViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Platform-specific module for dependencies that vary by platform (e.g., database driver).
 */
expect fun platformModule(): org.koin.core.module.Module

/**
 * Core utilities module - dispatchers, etc.
 */
val coreModule = module {
    singleOf(::DefaultDispatcherProvider) { bind<DispatcherProvider>() }
}

/**
 * Database module - database instance.
 */
val databaseModule = module {
    single {
        val driver = get<SqlDriver>()
        // Ensure the key/value settings table exists even on databases created before it was added
        // to the schema (this project ships no .sqm migrations, so Schema.migrate is a no-op).
        driver.execute(
            identifier = null,
            sql = "CREATE TABLE IF NOT EXISTS AppSetting(key TEXT NOT NULL PRIMARY KEY, value TEXT NOT NULL);",
            parameters = 0,
        )
        KlarityDatabase(driver = driver)
    }
}

/**
 * Network module - HTTP client and AI service.
 */
val networkModule = module {
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; isLenient = true })
            }
        }
    }
    single { AiService(get()) }
}

/**
 * Repository module - repository implementations.
 * Repositories use SQLDelight directly - no DataSource layer needed.
 */
val repositoryModule = module {
    single<NoteRepository> { SqlDelightNoteRepository(get(), get()) }
    single<FolderRepository> { SqlDelightFolderRepository(get(), get()) }
    single<TagRepository> { SqlDelightTagRepository(get(), get()) }
    single<TaskRepository> { SqlDelightTaskRepository(get(), get()) }
    single<SettingsRepository> { SqlDelightSettingsRepository(get(), get()) }
}

/**
 * Domain module - use cases.
 */
val domainModule = module {
    // Individual use cases
    factoryOf(::CreateNoteUseCase)
    factoryOf(::UpdateNoteUseCase)
    factoryOf(::DeleteNoteUseCase)
    factoryOf(::SearchNotesUseCase)

    // Use case containers for ViewModels
    factory { NoteUseCases(get(), get(), get(), get()) }
}

/**
 * ViewModel module — the Devbook screens share a single [WorkspaceViewModel].
 */
val viewModelModule = module {
    viewModelOf(::WorkspaceViewModel)
}

/**
 * Main application module that includes all other modules.
 */
val appModule = module {
    includes(
        platformModule(),
        coreModule,
        databaseModule,
        networkModule,
        repositoryModule,
        domainModule,
        viewModelModule
    )
}
