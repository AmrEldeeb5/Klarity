package com.example.sentio.di

import com.example.sentio.data.repositories.SqlDelightFolderRepository
import com.example.sentio.data.repositories.SqlDelightNoteRepository
import com.example.sentio.data.repositories.SqlDelightTagRepository
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
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

expect fun platformModule(): org.koin.core.module.Module

val appModule = module {
    includes(platformModule())

    // Database
    single {
        SentioDatabase(
            driver = get()
        )
    }

    // Repositories
    singleOf(::SqlDelightNoteRepository) bind NoteRepository::class
    singleOf(::SqlDelightTagRepository) bind TagRepository::class
    singleOf(::SqlDelightFolderRepository) bind FolderRepository::class

    // Use Cases
    singleOf(::CreateNoteUseCase)
    singleOf(::UpdateNoteUseCase)
    singleOf(::DeleteNoteUseCase)
    singleOf(::SearchNotesUseCase)

    // ViewModels
    viewModel { HomeViewModel(get(), get(), get(), get()) }
    viewModel { EditorViewModel(get(), get()) }
}
