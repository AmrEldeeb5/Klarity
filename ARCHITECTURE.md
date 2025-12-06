# Klarity Architecture Guide

This document explains the complete architecture of Klarity, from data layer to presentation layer, following **MVVM Clean Architecture** principles.

---

## 📐 Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                      PRESENTATION LAYER                         │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────┐  │
│  │   Screen    │◄─│  ViewModel  │◄─│  Sealed UI States       │  │
│  │ (Composable)│  │ (onEvent)   │  │  (Loading/Success/Error)│  │
│  └─────────────┘  └──────┬──────┘  └─────────────────────────┘  │
└──────────────────────────┼──────────────────────────────────────┘
                           │ Uses
┌──────────────────────────▼──────────────────────────────────────┐
│                        DOMAIN LAYER                              │
│  ┌─────────────┐  ┌─────────────────────┐                       │
│  │  Use Cases  │  │  Repository         │                       │
│  │             │──│  Interfaces         │                       │
│  └─────────────┘  └─────────────────────┘                       │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │                    Domain Models                             ││
│  │              (Note, Folder, Tag, Task)                       ││
│  └─────────────────────────────────────────────────────────────┘│
└──────────────────────────┬──────────────────────────────────────┘
                           │ Implements
┌──────────────────────────▼──────────────────────────────────────┐
│                         DATA LAYER                               │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  │
│  │  Repository     │──│   Data Source   │──│    Database     │  │
│  │  Implementation │  │   (SQLDelight)  │  │   (SQLDelight)  │  │
│  └────────┬────────┘  └─────────────────┘  └─────────────────┘  │
│           │                                                      │
│  ┌────────▼────────┐  ┌─────────────────┐                       │
│  │     Mappers     │  │     Entities    │                       │
│  │ (Entity↔Domain) │  │  (Data Layer)   │                       │
│  └─────────────────┘  └─────────────────┘                       │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🗄️ Data Layer

The data layer handles all data operations - database access, caching, and data transformation.

### 1. Entities (`data/entity/`)

Data layer entities represent the database schema. They're distinct from domain models to allow flexibility in how data is stored vs. how it's used.

```kotlin
// data/entity/NoteEntity.kt
data class NoteEntity(
    val id: String,
    val title: String,
    val content: String,
    val folderId: String?,
    val isPinned: Boolean,
    val isFavorite: Boolean,
    val tags: List<String>,
    val createdAt: Long,
    val updatedAt: Long
)
```

**Why separate entities?**
- Database schema can evolve independently of business logic
- Allows for database-specific optimizations (e.g., storing tags as JSON string)
- Clear boundary between data storage and domain concepts

### 2. Data Sources (`data/local/datasource/`)

Data sources abstract database operations. This allows swapping database implementations without changing repositories.

```kotlin
// Interface - defines WHAT operations are available
interface NoteLocalDataSource {
    fun getAllNotes(): Flow<List<NoteEntity>>
    fun getNoteById(id: String): NoteEntity?
    fun getPinnedNotes(): Flow<List<NoteEntity>>
    fun searchNotes(query: String): Flow<List<NoteEntity>>
    suspend fun insertNote(note: NoteEntity)
    suspend fun updateNote(note: NoteEntity)
    suspend fun deleteNote(id: String)
}

// Implementation - defines HOW using SQLDelight
class SqlDelightNoteDataSource(
    private val database: KlarityDatabase,
    private val dispatchers: DispatcherProvider
) : NoteLocalDataSource {
    
    private val queries = database.noteQueries
    
    override fun getAllNotes(): Flow<List<NoteEntity>> = 
        queries.selectAllNotes()
            .asFlow()
            .mapToList(dispatchers.io)
            .map { list -> list.map { it.toNoteEntity() } }
    
    override suspend fun insertNote(note: NoteEntity) {
        withContext(dispatchers.io) {
            queries.insertNote(
                id = note.id,
                title = note.title,
                content = note.content,
                // ... other fields
            )
        }
    }
}
```

**Key Points:**
- `Flow` for reactive data streams (auto-updates when DB changes)
- `suspend` for one-shot operations
- Injected `DispatcherProvider` for testability

### 3. Mappers (`data/mapper/`)

Mappers convert between data entities and domain models. This keeps conversion logic isolated and testable.

```kotlin
// data/mapper/NoteMapper.kt
object NoteMapper {
    
    // Entity → Domain Model
    fun NoteEntity.toDomainModel(): Note = Note(
        id = id,
        title = title,
        content = content,
        folderId = folderId,
        isPinned = isPinned,
        isFavorite = isFavorite,
        tags = tags,
        createdAt = Instant.fromEpochMilliseconds(createdAt),
        updatedAt = Instant.fromEpochMilliseconds(updatedAt)
    )
    
    // Domain Model → Entity
    fun Note.toEntity(): NoteEntity = NoteEntity(
        id = id,
        title = title,
        content = content,
        folderId = folderId,
        isPinned = isPinned,
        isFavorite = isFavorite,
        tags = tags,
        createdAt = createdAt.toEpochMilliseconds(),
        updatedAt = updatedAt.toEpochMilliseconds()
    )
}
```

**Why mappers?**
- Domain uses `Instant` for dates, database stores `Long` timestamps
- Tags might be stored as JSON in database but as `List<String>` in domain
- Single place to change if data format changes

### 4. Repositories (`data/repositories/`)

Repositories implement domain interfaces, orchestrating data sources and applying business logic.

```kotlin
// data/repositories/NoteRepositoryImpl.kt
class NoteRepositoryImpl(
    private val noteDataSource: NoteLocalDataSource,
    private val dispatchers: DispatcherProvider
) : NoteRepository {
    
    override fun getAllNotes(): Flow<List<Note>> =
        noteDataSource.getAllNotes()
            .map { entities -> entities.map { it.toDomainModel() } }
            .flowOn(dispatchers.io)
    
    override suspend fun getNoteById(id: String): Note? =
        withContext(dispatchers.io) {
            noteDataSource.getNoteById(id)?.toDomainModel()
        }
    
    override suspend fun createNote(note: Note): Note {
        val now = Clock.System.now()
        val newNote = note.copy(
            id = if (note.id.isBlank()) generateId() else note.id,
            createdAt = now,
            updatedAt = now
        )
        noteDataSource.insertNote(newNote.toEntity())
        return newNote
    }
}
```

### 5. Dispatcher Provider (`data/util/`)

Injectable dispatchers for testability - tests can use `TestDispatcher`.

```kotlin
// Interface
interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
}

// Production implementation
class DefaultDispatcherProvider : DispatcherProvider {
    override val main: CoroutineDispatcher = Dispatchers.Main
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val default: CoroutineDispatcher = Dispatchers.Default
}
```

---

## 🎯 Domain Layer

The domain layer contains business logic, independent of any framework or database.

### 1. Domain Models (`domain/models/`)

Pure Kotlin data classes representing business entities.

```kotlin
// domain/models/Note.kt
data class Note(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val folderId: String? = null,
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val tags: List<String> = emptyList(),
    val createdAt: Instant = Clock.System.now(),
    val updatedAt: Instant = Clock.System.now()
) {
    // Business logic methods
    fun preview(maxLength: Int = 100): String =
        content.take(maxLength).let { 
            if (content.length > maxLength) "$it..." else it 
        }
    
    fun hasTag(tag: String): Boolean = 
        tags.any { it.equals(tag, ignoreCase = true) }
}
```

### 2. Repository Interfaces (`domain/repositories/`)

Contracts that the data layer must fulfill.

```kotlin
// domain/repositories/NoteRepository.kt
interface NoteRepository {
    fun getAllNotes(): Flow<List<Note>>
    fun getPinnedNotes(): Flow<List<Note>>
    suspend fun getNoteById(id: String): Note?
    suspend fun createNote(note: Note): Note
    suspend fun updateNote(note: Note)
    suspend fun deleteNote(id: String)
    fun searchNotes(query: String): Flow<List<Note>>
}
```

### 3. Use Cases (`domain/usecase/`)

Single-responsibility classes for business operations. Each use case does ONE thing.

```kotlin
// domain/usecase/CreateNoteUseCase.kt
class CreateNoteUseCase(
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(
        title: String = "Untitled Note",
        content: String = "",
        folderId: String? = null
    ): Result<Note> = runCatching {
        val note = Note(
            title = title,
            content = content,
            folderId = folderId
        )
        noteRepository.createNote(note)
    }
}

// domain/usecase/SearchNotesUseCase.kt
class SearchNotesUseCase(
    private val noteRepository: NoteRepository
) {
    operator fun invoke(query: String): Flow<List<Note>> =
        noteRepository.searchNotes(query)
}
```

**Why use cases?**
- Single Responsibility: Each class does one thing
- Reusable: Multiple ViewModels can use the same use case
- Testable: Easy to unit test business logic
- `operator fun invoke()` allows calling like a function: `createNoteUseCase(title)`

---

## 🖼️ Presentation Layer

The presentation layer handles UI state and user interactions.

### 1. Sealed UI States (`presentation/state/`)

Type-safe state management using sealed classes/interfaces.

```kotlin
// Generic base state
sealed interface UiState<out T> {
    data object Idle : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String, val retryAction: (() -> Unit)? = null) : UiState<Nothing>
    data object Empty : UiState<Nothing>
}

// Screen-specific states
sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(
        val notes: List<Note>,
        val searchQuery: String = "",
        val isSearching: Boolean = false
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
    data object Empty : HomeUiState
}

// UI Events (user actions)
sealed interface HomeUiEvent {
    data class SearchQueryChanged(val query: String) : HomeUiEvent
    data object ClearSearch : HomeUiEvent
    data class NoteClicked(val noteId: String) : HomeUiEvent
    data class DeleteNote(val noteId: String) : HomeUiEvent
    data object CreateNote : HomeUiEvent
    data object Refresh : HomeUiEvent
}

// One-time effects (navigation, snackbars)
sealed interface HomeUiEffect {
    data class NavigateToEditor(val noteId: String) : HomeUiEffect
    data class ShowError(val message: String) : HomeUiEffect
    data class ShowSnackbar(val message: String) : HomeUiEffect
}
```

**Why sealed classes?**
- **Exhaustive `when`**: Compiler ensures all states are handled
- **Type-safe**: Can't accidentally pass wrong state type
- **Self-documenting**: States are explicit, not boolean flags
- **Immutable**: States can only change through defined transitions

### 2. ViewModels (`presentation/viewmodel/`)

ViewModels use the event-based pattern for unidirectional data flow.

```kotlin
class HomeViewModel(
    private val noteRepository: NoteRepository,
    private val createNoteUseCase: CreateNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val searchNotesUseCase: SearchNotesUseCase
) : ViewModel() {

    // UI State - observed by the UI
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Search query - separate flow for debouncing
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Effects channel - for one-time events
    private val _effects = Channel<HomeUiEffect>(Channel.BUFFERED)
    val effects: Flow<HomeUiEffect> = _effects.receiveAsFlow()

    // Reactive notes list
    val notes: StateFlow<List<Note>> = searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                noteRepository.getAllNotes()
            } else {
                searchNotesUseCase(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Single entry point for all UI events
    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.SearchQueryChanged -> updateSearchQuery(event.query)
            is HomeUiEvent.ClearSearch -> clearSearch()
            is HomeUiEvent.NoteClicked -> navigateToNote(event.noteId)
            is HomeUiEvent.DeleteNote -> deleteNote(event.noteId)
            is HomeUiEvent.CreateNote -> createNote()
            is HomeUiEvent.Refresh -> refresh()
        }
    }

    private fun createNote() {
        viewModelScope.launch {
            createNoteUseCase(title = "Untitled Note")
                .onSuccess { note ->
                    _effects.send(HomeUiEffect.NavigateToEditor(note.id))
                }
                .onFailure { error ->
                    _effects.send(HomeUiEffect.ShowError(error.message ?: "Failed"))
                }
        }
    }
    
    // ... other private methods
}
```

**Event-Based Pattern Benefits:**
- **Single entry point**: All actions go through `onEvent()`
- **Traceable**: Easy to log/debug all events
- **Testable**: Send events, verify state changes
- **Unidirectional**: Events → ViewModel → State → UI

### 3. Screens (`presentation/screen/`)

Composable functions that observe state and send events.

```kotlin
@Composable
fun HomeScreen(
    onNoteClick: (String) -> Unit,
    onCreateNote: () -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    // Observe state
    val notes by viewModel.notes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    
    // Handle one-time effects
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is HomeUiEffect.NavigateToEditor -> onNoteClick(effect.noteId)
                is HomeUiEffect.ShowError -> { /* show snackbar */ }
                is HomeUiEffect.ShowSnackbar -> { /* show snackbar */ }
            }
        }
    }

    // UI that sends events
    MainContent(
        notes = notes,
        searchQuery = searchQuery,
        onSearchQueryChange = { 
            viewModel.onEvent(HomeUiEvent.SearchQueryChanged(it)) 
        },
        onNoteClick = { noteId ->
            viewModel.onEvent(HomeUiEvent.NoteClicked(noteId))
        },
        onCreateNote = {
            viewModel.onEvent(HomeUiEvent.CreateNote)
        }
    )
}
```

### 4. Navigation (`presentation/navigation/`)

Type-safe navigation using sealed classes.

```kotlin
// Type-safe routes
@Serializable
sealed interface Screen {
    @Serializable
    data object Home : Screen
    
    @Serializable
    data class Editor(val noteId: String) : Screen
    
    @Serializable
    data object Settings : Screen
}

// NavHost setup
@Composable
fun KlarityNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home
    ) {
        composable<Screen.Home> {
            HomeScreen(
                onNoteClick = { noteId ->
                    navController.navigate(Screen.Editor(noteId))
                },
                onCreateNote = {
                    navController.navigate(Screen.Editor("new"))
                }
            )
        }

        composable<Screen.Editor> { backStackEntry ->
            val editor: Screen.Editor = backStackEntry.toRoute()
            EditorScreen(
                noteId = editor.noteId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
```

---

## 💉 Dependency Injection (`di/`)

Koin modules organize dependencies by layer.

```kotlin
// Core utilities
val coreModule = module {
    singleOf(::DefaultDispatcherProvider) { bind<DispatcherProvider>() }
}

// Database & data sources
val databaseModule = module {
    single { KlarityDatabase(driver = get()) }
    singleOf(::SqlDelightNoteDataSource) { bind<NoteLocalDataSource>() }
    singleOf(::SqlDelightFolderDataSource) { bind<FolderLocalDataSource>() }
}

// Repositories
val repositoryModule = module {
    singleOf(::NoteRepositoryImpl) { bind<NoteRepository>() }
    singleOf(::FolderRepositoryImpl) { bind<FolderRepository>() }
}

// Use cases
val domainModule = module {
    factoryOf(::CreateNoteUseCase)
    factoryOf(::UpdateNoteUseCase)
    factoryOf(::DeleteNoteUseCase)
    factoryOf(::SearchNotesUseCase)
}

// ViewModels
val viewModelModule = module {
    viewModelOf(::HomeViewModel)
    viewModelOf(::EditorViewModel)
}

// Combined app module
val appModule = module {
    includes(
        platformModule(),  // Platform-specific (DB driver)
        coreModule,
        databaseModule,
        repositoryModule,
        domainModule,
        viewModelModule
    )
}
```

**Scope Explanation:**
- `single` / `singleOf`: One instance for entire app (singletons)
- `factory` / `factoryOf`: New instance every time (use cases)
- `viewModelOf`: Scoped to Android/Compose lifecycle

---

## 🔄 Data Flow Example

Here's how data flows when a user creates a note:

```
1. USER ACTION
   └─► User taps "Create Note" button

2. UI LAYER (Screen)
   └─► HomeScreen calls: viewModel.onEvent(HomeUiEvent.CreateNote)

3. VIEWMODEL
   └─► onEvent() receives CreateNote event
   └─► Calls createNoteUseCase(title = "Untitled Note")

4. USE CASE (Domain)
   └─► CreateNoteUseCase.invoke() creates Note domain model
   └─► Calls noteRepository.createNote(note)

5. REPOSITORY (Data)
   └─► NoteRepositoryImpl.createNote()
   └─► Generates ID, sets timestamps
   └─► Converts to entity: note.toEntity()
   └─► Calls noteDataSource.insertNote(entity)

6. DATA SOURCE (Data)
   └─► SqlDelightNoteDataSource.insertNote()
   └─► Executes SQL: queries.insertNote(...)

7. RESPONSE FLOWS BACK UP
   └─► DataSource completes
   └─► Repository returns new Note
   └─► UseCase returns Result.success(note)
   └─► ViewModel sends effect: NavigateToEditor(note.id)
   └─► Screen observes effect, navigates to editor
```

---

## 🧪 Testing Benefits

This architecture makes testing straightforward:

```kotlin
// Test ViewModel with fake repository
class HomeViewModelTest {
    private val fakeRepository = FakeNoteRepository()
    private val viewModel = HomeViewModel(
        noteRepository = fakeRepository,
        createNoteUseCase = CreateNoteUseCase(fakeRepository),
        // ...
    )
    
    @Test
    fun `create note sends navigation effect`() = runTest {
        // When
        viewModel.onEvent(HomeUiEvent.CreateNote)
        
        // Then
        val effect = viewModel.effects.first()
        assertIs<HomeUiEffect.NavigateToEditor>(effect)
    }
}

// Test UseCase with mock repository
class CreateNoteUseCaseTest {
    @Test
    fun `invoke creates note with generated id`() = runTest {
        val mockRepo = mockk<NoteRepository>()
        coEvery { mockRepo.createNote(any()) } answers { firstArg() }
        
        val useCase = CreateNoteUseCase(mockRepo)
        val result = useCase(title = "Test")
        
        assertTrue(result.isSuccess)
        assertEquals("Test", result.getOrNull()?.title)
    }
}
```

---

## 📁 Final Project Structure

```
composeApp/src/commonMain/kotlin/com/example/Klarity/
├── App.kt                          # Main composable entry point
├── Platform.kt                     # Expect declarations
│
├── domain/
│   ├── models/
│   │   ├── Note.kt
│   │   ├── Folder.kt
│   │   ├── Tag.kt
│   │   └── Task.kt
│   ├── repositories/
│   │   ├── NoteRepository.kt       # Interface
│   │   ├── FolderRepository.kt
│   │   └── TagRepository.kt
│   └── usecase/
│       ├── CreateNoteUseCase.kt
│       ├── UpdateNoteUseCase.kt
│       ├── DeleteNoteUseCase.kt
│       └── SearchNotesUseCase.kt
│
├── data/
│   ├── entity/
│   │   ├── NoteEntity.kt
│   │   ├── FolderEntity.kt
│   │   └── TaskEntity.kt
│   ├── local/
│   │   └── datasource/
│   │       ├── NoteLocalDataSource.kt
│   │       ├── SqlDelightNoteDataSource.kt
│   │       ├── FolderLocalDataSource.kt
│   │       ├── SqlDelightFolderDataSource.kt
│   │       ├── TagLocalDataSource.kt
│   │       └── SqlDelightTagDataSource.kt
│   ├── mapper/
│   │   ├── NoteMapper.kt
│   │   ├── FolderMapper.kt
│   │   └── TagMapper.kt
│   ├── repositories/
│   │   ├── NoteRepositoryImpl.kt
│   │   ├── FolderRepositoryImpl.kt
│   │   └── TagRepositoryImpl.kt
│   └── util/
│       ├── DispatcherProvider.kt
│       └── DefaultDispatcherProvider.kt
│
├── presentation/
│   ├── state/
│   │   ├── UiState.kt
│   │   ├── HomeUiState.kt
│   │   └── EditorUiState.kt
│   ├── viewmodel/
│   │   ├── HomeViewModel.kt
│   │   └── EditorViewModel.kt
│   ├── screen/
│   │   ├── home/
│   │   │   └── HomeScreen.kt
│   │   └── editor/
│   │       └── EditorScreen.kt
│   ├── navigation/
│   │   ├── Screen.kt
│   │   └── KlarityNavigation.kt
│   └── theme/
│       ├── Colors.kt
│       ├── Theme.kt
│       └── Typography.kt
│
├── di/
│   └── AppModule.kt
│
└── db/
    └── (SQLDelight generated files)
```

---

## 🎯 Key Takeaways

1. **Separation of Concerns**: Each layer has a single responsibility
2. **Dependency Rule**: Outer layers depend on inner layers, never reverse
3. **Abstraction**: Depend on interfaces, not implementations
4. **Testability**: Every component can be tested in isolation
5. **Unidirectional Data Flow**: State flows down, events flow up
6. **Type Safety**: Sealed classes ensure all cases are handled

This architecture scales well as the app grows and makes onboarding new developers easier since the patterns are consistent throughout the codebase.
