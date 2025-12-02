# 📁 Sentio File Structure

## Project Root
```
sentio/
├── composeApp/              # Main application module
├── gradle/                  # Gradle wrapper and dependencies
├── .gradle/                 # Gradle cache (ignored)
├── .idea/                   # IntelliJ IDEA settings (ignored)
├── build/                   # Build outputs (ignored)
├── build.gradle.kts         # Root build configuration
├── settings.gradle.kts      # Project settings
├── gradle.properties        # Gradle properties
└── Documentation files (see below)
```

## Documentation Files

| File | Purpose |
|------|---------|
| `README.md` | Project overview and quick info |
| `QUICK_START.md` | How to run and use the app |
| `ROADMAP.md` | 10-week development plan |
| `ARCHITECTURE.md` | Technical architecture details |
| `PROJECT_STRUCTURE.md` | Detailed code organization |
| `GETTING_STARTED.md` | Developer onboarding guide |
| `NEXT_STEPS.md` | Immediate development tasks |
| `IMPLEMENTATION_SUMMARY.md` | What's been built so far |
| `FILE_STRUCTURE.md` | This file |

## Source Code Structure

```
composeApp/src/
│
├── commonMain/              # Shared code (all platforms)
│   ├── kotlin/com/example/sentio/
│   │   │
│   │   ├── domain/          # Business logic (pure Kotlin)
│   │   │   ├── models/      # Data classes
│   │   │   │   ├── Note.kt
│   │   │   │   ├── Tag.kt
│   │   │   │   ├── Folder.kt
│   │   │   │   ├── Snippet.kt
│   │   │   │   ├── Attachment.kt
│   │   │   │   ├── Link.kt
│   │   │   │   └── SearchResult.kt
│   │   │   │
│   │   │   ├── repositories/  # Interfaces
│   │   │   │   ├── NoteRepository.kt
│   │   │   │   ├── TagRepository.kt
│   │   │   │   └── FolderRepository.kt
│   │   │   │
│   │   │   └── usecases/      # Business operations
│   │   │       └── note/
│   │   │           ├── CreateNoteUseCase.kt
│   │   │           ├── UpdateNoteUseCase.kt
│   │   │           ├── DeleteNoteUseCase.kt
│   │   │           └── SearchNotesUseCase.kt
│   │   │
│   │   ├── data/            # Data access layer
│   │   │   ├── local/
│   │   │   │   └── DatabaseDriverFactory.kt (expect)
│   │   │   │
│   │   │   └── repositories/  # Implementations
│   │   │       ├── SqlDelightNoteRepository.kt
│   │   │       ├── SqlDelightTagRepository.kt
│   │   │       └── SqlDelightFolderRepository.kt
│   │   │
│   │   ├── ui/              # User interface
│   │   │   ├── screens/
│   │   │   │   ├── home/
│   │   │   │   │   └── HomeScreen.kt
│   │   │   │   └── editor/
│   │   │   │       └── EditorScreen.kt
│   │   │   │
│   │   │   ├── viewmodels/
│   │   │   │   ├── HomeViewModel.kt
│   │   │   │   └── EditorViewModel.kt
│   │   │   │
│   │   │   ├── navigation/
│   │   │   │   ├── Screen.kt
│   │   │   │   └── SentioNavigation.kt
│   │   │   │
│   │   │   └── theme/
│   │   │       ├── Theme.kt
│   │   │       └── Typography.kt
│   │   │
│   │   ├── di/              # Dependency injection
│   │   │   └── AppModule.kt
│   │   │
│   │   └── App.kt           # Root composable
│   │
│   └── sqldelight/          # Database schemas
│       └── com/example/sentio/db/
│           ├── Note.sq
│           ├── Tag.sq
│           └── Folder.sq
│
├── jvmMain/                 # Desktop-specific code
│   └── kotlin/com/example/sentio/
│       ├── Main.kt          # Entry point
│       │
│       ├── data/local/
│       │   └── DatabaseDriverFactory.kt (actual)
│       │
│       └── di/
│           └── PlatformModule.kt
│
└── androidMain/             # Android-specific (future)
    └── kotlin/...
```

## Generated Code (Don't Edit)

```
composeApp/build/
└── generated/
    └── sqldelight/
        └── code/
            └── SentioDatabase/
                └── commonMain/
                    └── com/example/sentio/db/
                        ├── Note.kt
                        ├── NoteQueries.kt
                        ├── Tag.kt
                        ├── TagQueries.kt
                        ├── Folder.kt
                        ├── FolderQueries.kt
                        └── SentioDatabase.kt
```

## Configuration Files

```
gradle/
└── libs.versions.toml       # Dependency versions

composeApp/
└── build.gradle.kts         # Module build config
```

## Key Directories Explained

### `domain/`
Pure business logic, no platform dependencies. Contains:
- **models**: Immutable data classes
- **repositories**: Interfaces (dependency inversion)
- **usecases**: Single-responsibility operations

### `data/`
Data access implementations. Contains:
- **local**: Database drivers
- **repositories**: Concrete implementations
- **remote**: API clients (future)

### `ui/`
Compose UI layer. Contains:
- **screens**: Full-screen composables
- **viewmodels**: State management
- **navigation**: Screen routing
- **theme**: Material 3 styling

### `di/`
Dependency injection setup using Koin

### `sqldelight/`
SQL schema definitions (`.sq` files)

## File Naming Conventions

| Type | Convention | Example |
|------|-----------|---------|
| Domain Model | `PascalCase.kt` | `Note.kt` |
| Use Case | `VerbNounUseCase.kt` | `CreateNoteUseCase.kt` |
| Repository Interface | `NounRepository.kt` | `NoteRepository.kt` |
| Repository Impl | `TechNounRepository.kt` | `SqlDelightNoteRepository.kt` |
| Screen | `NounScreen.kt` | `HomeScreen.kt` |
| ViewModel | `NounViewModel.kt` | `HomeViewModel.kt` |
| SQL Schema | `Noun.sq` | `Note.sq` |

## Important Files to Know

### Entry Points
- `Main.kt` - Desktop app entry point
- `App.kt` - Root Compose UI

### Configuration
- `AppModule.kt` - Koin DI setup
- `Theme.kt` - Material 3 colors

### Core Logic
- `NoteRepository.kt` - Note operations interface
- `SqlDelightNoteRepository.kt` - Database implementation
- `HomeViewModel.kt` - Home screen state
- `EditorViewModel.kt` - Editor state

### Database
- `Note.sq` - Note table schema
- `DatabaseDriverFactory.kt` - Platform-specific driver

## Build Outputs

```
composeApp/build/
├── compose/
│   └── binaries/
│       └── main/
│           ├── app/         # Runnable app
│           └── deb/         # Linux installer
│               msi/         # Windows installer
│               dmg/         # macOS installer
└── reports/                 # Test and lint reports
```

## Ignored Files (.gitignore)

- `build/` - Build outputs
- `.gradle/` - Gradle cache
- `.idea/` - IDE settings
- `local.properties` - Local config
- `*.iml` - IntelliJ modules

## Total File Count

- **Domain Models**: 7 files
- **Repositories**: 3 interfaces + 3 implementations
- **Use Cases**: 4 files
- **ViewModels**: 2 files
- **Screens**: 2 files
- **SQL Schemas**: 3 files
- **DI Modules**: 2 files
- **Documentation**: 9 files

**Total Source Files**: ~35 Kotlin files + 3 SQL files

---

**Clean, organized, and ready for Phase 2! 🌿**
