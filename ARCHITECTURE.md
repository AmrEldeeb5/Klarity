# 🏗️ Sentio Architecture — Cognitive Layer

## Overview
Sentio follows a **clean architecture** pattern with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────┐
│                        UI Layer                              │
│  (Compose Multiplatform - Screens, Components, ViewModels)  │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                     Domain Layer                             │
│        (Business Logic, Use Cases, Domain Models)            │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                      Data Layer                              │
│    (Repositories, Data Sources, Database, Network, AI)       │
└─────────────────────────────────────────────────────────────┘
```

---

## Layer Details

### 1. UI Layer (`ui/`)
**Responsibility**: User interface and user interactions

**Structure**:
```
ui/
├── screens/           # Full screen composables
│   ├── home/
│   ├── editor/
│   ├── search/
│   └── settings/
├── components/        # Reusable UI components
│   ├── markdown/
│   ├── sidebar/
│   ├── editor/
│   └── common/
├── theme/            # Material 3 theme, colors, typography
├── navigation/       # Navigation logic
└── viewmodels/       # State management
```

**Key Principles**:
- Composables are pure functions of state
- ViewModels hold UI state and handle business logic calls
- No direct database or network access
- Use StateFlow/SharedFlow for reactive updates

---

### 2. Domain Layer (`domain/`)
**Responsibility**: Business logic and rules (platform-agnostic)

**Structure**:
```
domain/
├── models/           # Core domain entities
│   ├── Note.kt
│   ├── Tag.kt
│   ├── Folder.kt
│   ├── Snippet.kt
│   ├── Attachment.kt
│   └── Link.kt
├── usecases/         # Business operations
│   ├── note/
│   │   ├── CreateNoteUseCase.kt
│   │   ├── UpdateNoteUseCase.kt
│   │   ├── DeleteNoteUseCase.kt
│   │   └── SearchNotesUseCase.kt
│   ├── ai/
│   │   ├── AskSentioUseCase.kt
│   │   ├── GenerateEmbeddingUseCase.kt
│   │   └── SuggestTagsUseCase.kt
│   └── graph/
│       ├── BuildKnowledgeGraphUseCase.kt
│       └── FindRelatedNotesUseCase.kt
└── repositories/     # Repository interfaces (implemented in data layer)
    ├── NoteRepository.kt
    ├── TagRepository.kt
    ├── AIRepository.kt
    └── SearchRepository.kt
```

**Key Principles**:
- Pure Kotlin (no platform dependencies)
- Domain models are immutable data classes
- Use cases encapsulate single business operations
- Repositories are interfaces (dependency inversion)

---

### 3. Data Layer (`data/`)
**Responsibility**: Data access and external integrations

**Structure**:
```
data/
├── local/            # Local database
│   ├── database/
│   │   ├── SentioDatabase.kt
│   │   └── entities/
│   │       ├── NoteEntity.kt
│   │       ├── TagEntity.kt
│   │       └── FolderEntity.kt
│   ├── dao/
│   │   ├── NoteDao.kt
│   │   ├── TagDao.kt
│   │   └── FolderDao.kt
│   └── preferences/
│       └── UserPreferences.kt
├── remote/           # External APIs
│   ├── ai/
│   │   ├── OpenAIClient.kt
│   │   └── EmbeddingService.kt
│   └── sync/         # Future: cloud sync
├── repositories/     # Repository implementations
│   ├── NoteRepositoryImpl.kt
│   ├── TagRepositoryImpl.kt
│   └── AIRepositoryImpl.kt
└── mappers/          # Entity ↔ Domain model conversion
    ├── NoteMapper.kt
    └── TagMapper.kt
```

**Key Principles**:
- Database entities are separate from domain models
- Mappers convert between layers
- Repositories coordinate between local and remote sources
- Use Kotlin Flow for reactive data streams

---

## Data Flow Example

**User creates a note:**

```
1. User types in Editor (UI)
   ↓
2. EditorViewModel.saveNote() called
   ↓
3. CreateNoteUseCase.execute() (Domain)
   ↓
4. NoteRepository.createNote() (Data)
   ↓
5. NoteDao.insert() → SQLDelight (Database)
   ↓
6. Flow<List<Note>> emits updated list
   ↓
7. ViewModel collects and updates UI state
   ↓
8. UI recomposes with new note
```

---

## Key Technologies

### Database
**SQLDelight** (type-safe SQL for Kotlin Multiplatform)
- Compile-time SQL verification
- Generated type-safe Kotlin APIs
- Supports desktop (SQLite)

### State Management
**Kotlin Flow + StateFlow**
- Reactive data streams
- Lifecycle-aware
- Coroutine-based

### Dependency Injection
**Manual DI** (for simplicity in v1.0)
- Single `AppContainer` object
- Lazy initialization
- Easy to test

Future: Koin or Kotlin Inject

### AI Integration
**Ktor Client** + **OpenAI API**
- Async HTTP calls
- JSON serialization
- Streaming support for chat

---

## Module Structure

```
sentio/
├── composeApp/
│   └── src/
│       ├── commonMain/kotlin/com/example/sentio/
│       │   ├── domain/          # Business logic
│       │   ├── data/            # Data layer
│       │   ├── ui/              # UI layer
│       │   ├── di/              # Dependency injection
│       │   └── App.kt           # Root composable
│       ├── jvmMain/kotlin/      # Desktop-specific code
│       │   └── Main.kt
│       └── androidMain/kotlin/  # Android-specific (future)
```

---

## Design Patterns

### Repository Pattern
Abstracts data sources (local DB, remote API, cache)

### Use Case Pattern
Single responsibility for each business operation

### MVVM (Model-View-ViewModel)
- Model: Domain entities
- View: Composables
- ViewModel: State + business logic coordination

### Mapper Pattern
Converts between data entities and domain models

---

## Testing Strategy

### Unit Tests
- Domain layer (use cases, business logic)
- Mappers
- ViewModels (with test coroutines)

### Integration Tests
- Repository implementations
- Database operations

### UI Tests
- Compose UI testing
- Screenshot tests

---

## Performance Considerations

### Database
- Indexes on frequently queried fields (title, tags, date)
- Pagination for large note lists
- Background thread for writes

### AI Calls
- Debounce user input (avoid excessive API calls)
- Cache embeddings
- Show loading states

### UI
- Lazy loading for lists
- Virtual scrolling for large documents
- Offload heavy operations to background threads

---

## Security & Privacy

### Local-First
- All data stored locally by default
- No cloud dependency for core features

### AI Privacy
- Option to use local LLMs (Ollama)
- Clear indication when data leaves device
- User consent for AI features

### Data Encryption
- Future: Encrypt sensitive notes
- Secure storage for API keys

---

## Future Architecture Evolution

### Phase 2: Add Execution Layer
- New domain models (Task, Timer, Sprint)
- Shared repositories
- Cross-layer integrations

### Phase 3: Multi-Platform
- Shared business logic (commonMain)
- Platform-specific UI (expect/actual)
- Sync layer for cloud storage

### Phase 4: Collaboration
- WebSocket for real-time sync
- Conflict resolution
- User management
