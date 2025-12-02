# ⚡ Tech Stack Quick Reference

## All Technologies at a Glance

---

## 🎨 UI & Design

```kotlin
// Compose Multiplatform 1.7.0
@Composable
fun MyScreen() {
    MaterialTheme {
        Surface { /* content */ }
    }
}

// Material 3 Theme
SentioTheme {
    Text(
        text = "Hello",
        color = SentioColors.AccentPrimary,
        style = MaterialTheme.typography.bodyLarge
    )
}
```

**Docs**: [THEME_MIGRATION_COMPLETE.md](THEME_MIGRATION_COMPLETE.md)

---

## 💾 Database

```kotlin
// SQLDelight 2.0.1
val notes = database.noteQueries
    .selectAll()
    .asFlow()
    .mapToList(Dispatchers.IO)

// Insert
noteQueries.insert(id, title, content, ...)

// Query
val note = noteQueries.selectById(id).executeAsOneOrNull()
```

**Location**: `~/.sentio/sentio.db`

---

## 🔌 Dependency Injection

```kotlin
// Koin 4.0.0
val appModule = module {
    single { MyRepository(get()) }
    viewModel { MyViewModel(get()) }
}

// In composable
@Composable
fun MyScreen(
    viewModel: MyViewModel = koinViewModel()
) { }
```

**Docs**: [KOIN_VIEWMODEL_IMPROVEMENTS.md](KOIN_VIEWMODEL_IMPROVEMENTS.md)

---

## 🌐 HTTP Client

```kotlin
// Ktor 3.0.0
val client = HttpClient {
    install(ContentNegotiation) { json() }
}

// GET
val data: MyData = client.get("https://api.example.com/data")

// POST
val result = client.post("url") {
    setBody(myData)
}
```

**Docs**: [KTOR_SETUP_GUIDE.md](KTOR_SETUP_GUIDE.md), [KTOR_QUICK_REFERENCE.md](KTOR_QUICK_REFERENCE.md)

---

## 🔄 Serialization

```kotlin
// Kotlinx Serialization 1.7.3
@Serializable
data class Note(
    val id: String,
    val title: String
)

// Serialize
val json = Json.encodeToString(note)

// Deserialize
val note = Json.decodeFromString<Note>(json)
```

**Docs**: [SERIALIZATION_GUIDE.md](SERIALIZATION_GUIDE.md)

---

## 🧭 Navigation

```kotlin
// Navigation Compose 2.8.0-alpha10
@Serializable
data class EditorRoute(val noteId: String)

NavHost(navController, startDestination = HomeRoute) {
    composable<HomeRoute> { HomeScreen() }
    composable<EditorRoute> { EditorScreen() }
}

// Navigate
navController.navigate(EditorRoute("123"))
```

---

## 🔄 State Management

```kotlin
// Kotlin Flow + StateFlow
class MyViewModel : ViewModel() {
    private val _state = MutableStateFlow(MyState())
    val state: StateFlow<MyState> = _state.asStateFlow()
    
    fun updateState() {
        _state.value = _state.value.copy(/* changes */)
    }
}

// In composable
val state by viewModel.state.collectAsState()
```

---

## ⏰ Coroutines

```kotlin
// Kotlinx Coroutines 1.9.0
viewModelScope.launch {
    // Background work
    withContext(Dispatchers.IO) {
        database.query()
    }
    
    // Update UI
    withContext(Dispatchers.Main) {
        _state.value = newState
    }
}

// Flow
flow {
    emit(data)
}.flowOn(Dispatchers.IO)
```

---

## 📝 Markdown

```kotlin
// CommonMark 0.22.0
val parser = Parser.builder().build()
val renderer = HtmlRenderer.builder().build()

val document = parser.parse(markdownText)
val html = renderer.render(document)
```

---

## 🎨 Colors

```kotlin
// Sentio Theme
SentioColors.BgPrimary        // #0A1612
SentioColors.AccentPrimary    // #3DD68C
SentioColors.AccentAI         // #667EEA
SentioColors.TextPrimary      // #E0E6E3
```

---

## 📊 Common Patterns

### Repository Pattern
```kotlin
interface NoteRepository {
    fun getAllNotes(): Flow<List<Note>>
    suspend fun createNote(note: Note): Result<Note>
}

class SqlDelightNoteRepository(
    private val database: SentioDatabase
) : NoteRepository {
    override fun getAllNotes() = 
        database.noteQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
}
```

### Use Case Pattern
```kotlin
class CreateNoteUseCase(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(title: String): Result<Note> {
        val note = Note(
            id = uuid4().toString(),
            title = title,
            content = "",
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now()
        )
        return repository.createNote(note)
    }
}
```

### ViewModel Pattern
```kotlin
class HomeViewModel(
    private val noteRepository: NoteRepository,
    private val createNoteUseCase: CreateNoteUseCase
) : ViewModel() {
    val notes = noteRepository.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    fun createNote(title: String) {
        viewModelScope.launch {
            createNoteUseCase(title)
        }
    }
}
```

---

## 🔧 Build Commands

```bash
# Run app
./gradlew :composeApp:run

# Build
./gradlew :composeApp:build

# Clean
./gradlew clean

# Generate SQLDelight
./gradlew :composeApp:generateCommonMainSentioInterface

# Create installer
./gradlew :composeApp:packageDistributionForCurrentOS
```

---

## 📁 Project Structure

```
composeApp/src/
├── commonMain/
│   ├── kotlin/
│   │   ├── domain/      # Models, repos, use cases
│   │   ├── data/        # Implementations
│   │   ├── ui/          # Screens, ViewModels
│   │   └── di/          # Koin modules
│   └── sqldelight/      # SQL schemas
└── jvmMain/
    └── kotlin/
        ├── Main.kt
        ├── data/local/  # Platform driver
        └── di/          # Platform module
```

---

## 🎯 Version Summary

| Technology | Version | Status |
|-----------|---------|--------|
| Kotlin | 2.0.21 | ✅ |
| Compose | 1.7.0 | ✅ |
| SQLDelight | 2.0.1 | ✅ |
| Koin | 4.0.0 | ✅ |
| Ktor | 3.0.0 | ✅ |
| Navigation | 2.8.0-alpha10 | ✅ |
| Serialization | 1.7.3 | ✅ |
| Coroutines | 1.9.0 | ✅ |

---

## 📚 Full Documentation

- **Architecture**: [ARCHITECTURE.md](ARCHITECTURE.md)
- **Tech Stack**: [TECH_STACK_SUMMARY.md](TECH_STACK_SUMMARY.md)
- **File Structure**: [FILE_STRUCTURE.md](FILE_STRUCTURE.md)
- **All Docs**: [DOCS_INDEX.md](DOCS_INDEX.md)

---

**Everything you need in one place! 🚀**
