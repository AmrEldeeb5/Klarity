package com.example.sentio.di

import com.example.sentio.data.local.datasource.FolderLocalDataSource
import com.example.sentio.data.local.datasource.NoteLocalDataSource
import com.example.sentio.data.local.datasource.SqlDelightFolderDataSource
import com.example.sentio.data.local.datasource.SqlDelightNoteDataSource
import com.example.sentio.data.local.datasource.SqlDelightTagDataSource
import com.example.sentio.data.local.datasource.TagLocalDataSource
import com.example.sentio.data.repositories.FolderRepositoryImpl
import com.example.sentio.data.repositories.NoteRepositoryImpl
import com.example.sentio.data.repositories.TagRepositoryImpl
import com.example.sentio.data.util.DefaultDispatcherProvider
import com.example.sentio.data.util.DispatcherProvider
import com.example.sentio.db.SentioDatabase
import com.example.sentio.domain.repositories.FolderRepository
import com.example.sentio.domain.repositories.NoteRepository
import com.example.sentio.domain.repositories.TagRepository
import com.example.sentio.domain.usecase.CreateNoteUseCase
import com.example.sentio.domain.usecase.DeleteNoteUseCase
import com.example.sentio.domain.usecase.SearchNotesUseCase
import com.example.sentio.domain.usecase.UpdateNoteUseCase
import com.example.sentio.ui.viewmodels.EditorViewModel
import com.example.sentio.ui.viewmodels.HomeViewModel
import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
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
 * Database module - database instance and data sources.
 */
val databaseModule = module {
    single { SentioDatabase(driver = get()) }

    // Data Sources
    singleOf(::SqlDelightNoteDataSource) { bind<NoteLocalDataSource>() }
    singleOf(::SqlDelightFolderDataSource) { bind<FolderLocalDataSource>() }
    singleOf(::SqlDelightTagDataSource) { bind<TagLocalDataSource>() }
}

/**
 * Repository module - repository implementations.
 */
val repositoryModule = module {
    singleOf(::NoteRepositoryImpl) { bind<NoteRepository>() }
    singleOf(::FolderRepositoryImpl) { bind<FolderRepository>() }
    singleOf(::TagRepositoryImpl) { bind<TagRepository>() }
}

/**
 * Domain module - use cases.
 */
val domainModule = module {
    factoryOf(::CreateNoteUseCase)
    factoryOf(::UpdateNoteUseCase)
    factoryOf(::DeleteNoteUseCase)
    factoryOf(::SearchNotesUseCase)
}

/**
 * ViewModel module - all ViewModels.
 */
val viewModelModule = module {
    viewModelOf(::HomeViewModel)
    viewModelOf(::EditorViewModel)
}

/**
 * Main application module that includes all other modules.
 */
val appModule = module {
    includes(
        platformModule(),
        coreModule,
        databaseModule,
        repositoryModule,
        domainModule,
        viewModelModule
    )
}
