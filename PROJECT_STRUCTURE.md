# 📁 Sentio Project Structure

## Current Structure

```
sentio/
├── composeApp/
│   ├── src/
│   │   ├── commonMain/kotlin/com/example/sentio/
│   │   │   ├── domain/                    # Business logic layer
│   │   │   │   ├── models/                # Domain entities
│   │   │   │   │   ├── Note.kt            ✅ Created
│   │   │   │   │   ├── Tag.kt             ✅ Created
│   │   │   │   │   ├── Folder.kt          ✅ Created
│   │   │   │   │   ├── Snippet.kt         ✅ Created
│   │   │   │   │   ├── Attachment.kt      ✅ Created
│   │   │   │   │   ├── Link.kt            ✅ Created
│   │   │   │   │   └── SearchResult.kt    ✅ Created
│   │   │   │   ├── repositories/          # Repository interfaces
│   │   │   │   │   ├── NoteRepository.kt  ✅ Created
│   │   │   │   │   ├── TagRepository.kt   ✅ Created
│   │   │   │   │   └── FolderRepository.kt ✅ Created
│   │   │   │   └── usecases/              # Business operations
│   │   │   │       └── note/
│   │   │   │           ├── CreateNoteUseCase.kt  ✅ Created
│   │   │   │           ├── UpdateNoteUseCase.kt  ✅ Created
│   │   │   │           ├── DeleteNoteUseCase.kt  ✅ Created
│   │   │   │           └── SearchNotesUseCase.kt ✅ Created
│   │   │   │
│   │   │   ├── data/                      # Data layer
│   │   │   │   ├── repositories/          # Repository implementations
│   │   │   │   │   ├── SqlDelightNoteRepository.kt    ✅ Created
│   │   │   │   │   ├── SqlDelightTagRepository.kt     ✅ Created
│   │   │   │   │   └── SqlDelightFolderRepository.kt  ✅ Created
│   │   │   │   ├── local/                 # Local database
│   │   │   │   │   └── DatabaseDriverFactory.kt       ✅ Created
│   │   │   │   └── remote/                # External APIs (TODO: Phase 3)
│   │   │   │       └── ai/
│   │   │   │
│   │   │   ├── ui/                        # UI layer
│   │   │   │   ├── screens/               # Full screen composables
│   │   │   │   │   └── home/
│   │   │   │   │       └── HomeScreen.kt  ✅ Created
│   │   │   │   ├── components/            # Reusable components (TODO)
│   │   │   │   ├── theme/                 # Material 3 theme
│   │   │   │   │   ├── Theme.kt           ✅ Created
│   │   │   │   │   └── Typography.kt      ✅ Created
│   │   │   │   ├── navigation/            # Navigation (TODO: Phase 1)
│   │   │   │   └── viewmodels/            # State management (TODO: Phase 1)
│   │   │   │
│   │   │   ├── di/                        # Dependency injection
│   │   │   │   └── AppModule.kt           ✅ Created (Koin)
│   │   │   │
│   │   │   └── App.kt                     ✅ Created (root composable)
│   │   │
│   │   ├── jvmMain/kotlin/com/example/sentio/
│   │   │   ├── Main.kt                    ✅ Updated
│   │   │   ├── data/local/
│   │   │   │   └── DatabaseDriverFactory.kt  ✅ Created (JVM impl)
│   │   │   └── di/
│   │   │       └── PlatformModule.kt      ✅ Created
│   │   │
│   │   └── commonMain/sqldelight/         # SQLDelight schemas
│   │       └── com/example/sentio/db/
│   │           ├── Note.sq                ✅ Created
│   │           ├── Tag.sq                 ✅ Created
│   │           └── Folder.sq              ✅ Created
│   │   │
│   │   └── androidMain/                   # Android-specific (future)
│   │
│   └── build.gradle.kts                   ✅ Configured
│
├── gradle/
│   └── libs.versions.toml                 ✅ Configured
│
├── ROADMAP.md                             ✅ Created
├── ARCHITECTURE.md                        ✅ Created
└── PROJECT_STRUCTURE.md                   ✅ This file
```

---

## Next Steps (Phase 1 - Foundation)

### 1. Database Setup (Week 1)
- [ ] Add SQLDelight dependency
- [ ] Create database schema (.sq files)
- [ ] Generate database entities
- [ ] Implement DAOs
- [ ] Create real repository implementations
- [ ] Add database migrations support

### 2. UI Components (Week 1-2)
- [ ] Create NoteListItem component
- [ ] Create NoteEditor component
- [ ] Create FolderTree component
- [ ] Create TagChip component
- [ ] Create SearchBar component
- [ ] Create EmptyState component

### 3. Screens (Week 2)
- [ ] Complete HomeScreen with note list
- [ ] Create EditorScreen
- [ ] Create SearchScreen
- [ ] Create SettingsScreen

### 4. ViewModels (Week 2)
- [ ] HomeViewModel
- [ ] EditorViewModel
- [ ] SearchViewModel
- [ ] SettingsViewModel

### 5. Navigation (Week 2)
- [ ] Set up navigation graph
- [ ] Implement screen transitions
- [ ] Add keyboard shortcuts

---

## File Naming Conventions

### Domain Layer
- Models: `PascalCase.kt` (e.g., `Note.kt`)
- Use Cases: `VerbNounUseCase.kt` (e.g., `CreateNoteUseCase.kt`)
- Repositories: `NounRepository.kt` (interface)

### Data Layer
- Entities: `NounEntity.kt` (e.g., `NoteEntity.kt`)
- DAOs: `NounDao.kt` (e.g., `NoteDao.kt`)
- Repositories: `NounRepositoryImpl.kt` (implementation)
- Mappers: `NounMapper.kt` (e.g., `NoteMapper.kt`)

### UI Layer
- Screens: `NounScreen.kt` (e.g., `HomeScreen.kt`)
- Components: `NounComponent.kt` or descriptive name
- ViewModels: `NounViewModel.kt` (e.g., `HomeViewModel.kt`)

---

## Package Organization Rules

1. **Domain layer** is platform-agnostic (pure Kotlin)
2. **Data layer** can have platform-specific implementations
3. **UI layer** uses Compose Multiplatform (shared across platforms)
4. **Platform-specific code** goes in `jvmMain`, `androidMain`, etc.

---

## Dependencies Status

### Already Configured ✅
- Compose Multiplatform
- Material 3
- Kotlin Coroutines
- Kotlinx Serialization
- Kotlinx DateTime
- Ktor (for future AI integration)
- CommonMark (markdown parsing)
- UUID generation

### Added in Phase 1 ✅
- SQLDelight (local database)
- Koin (dependency injection)
- Navigation Compose

### To Add in Phase 2
- DataStore (preferences)

### To Add in Phase 3
- OpenAI SDK or Ktor client for AI
- Vector database client (Chroma/Qdrant)

---

## Testing Structure (Future)

```
composeApp/src/
├── commonTest/kotlin/com/example/sentio/
│   ├── domain/
│   │   ├── usecases/
│   │   └── models/
│   ├── data/
│   │   └── repositories/
│   └── ui/
│       └── viewmodels/
└── jvmTest/kotlin/
    └── ui/
        └── screens/
```

---

## Build Outputs

- **Desktop**: `build/compose/binaries/main/app/`
- **Installers**: `build/compose/binaries/main/`
  - `.dmg` (macOS)
  - `.msi` (Windows)
  - `.deb` (Linux)

---

## Git Ignore Patterns

Already configured in `.gitignore`:
- Build outputs
- IDE files
- Gradle cache
- Local properties

---

## Documentation

- `ROADMAP.md` - Development phases and milestones
- `ARCHITECTURE.md` - Technical architecture and design patterns
- `PROJECT_STRUCTURE.md` - This file (project organization)
- `README.md` - Project overview and setup instructions

---

## Quick Commands

```bash
# Run desktop app
./gradlew :composeApp:run

# Build desktop installer
./gradlew :composeApp:packageDistributionForCurrentOS

# Run tests
./gradlew :composeApp:test

# Clean build
./gradlew clean
```
