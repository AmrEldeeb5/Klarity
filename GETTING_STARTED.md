# 🌿 Getting Started with Sentio Development

## Welcome!

You've just set up the foundation for Sentio's Cognitive Layer. Here's everything you need to know to start developing.

---

## 📚 Documentation Overview

| Document | Purpose |
|----------|---------|
| `README.md` | Project overview and quick start |
| `ROADMAP.md` | 10-week development plan with milestones |
| `ARCHITECTURE.md` | Technical architecture and design patterns |
| `PROJECT_STRUCTURE.md` | Detailed file organization |
| `NEXT_STEPS.md` | Immediate tasks for Phase 1 |
| `GETTING_STARTED.md` | This file - your development guide |

---

## 🎯 What's Been Built

### ✅ Complete
- Domain models (Note, Tag, Folder, Snippet, etc.)
- Repository interfaces
- Use cases for note operations
- Basic UI with Material 3 theme
- Home screen with sidebar layout
- Dependency injection setup
- In-memory data storage (temporary)

### 🚧 Next Up
- SQLDelight database integration
- Note list UI with real data
- Markdown editor
- Navigation system
- Search functionality

---

## 🏃 Quick Start

### 1. Run the App

```bash
./gradlew :composeApp:run
```

You should see a window with:
- Sidebar on the left (Sentio logo, navigation items)
- Main content area with "Welcome to Sentio"
- "Create Your First Note" button (not functional yet)

### 2. Explore the Code

**Start here**:
```
composeApp/src/commonMain/kotlin/com/example/sentio/
├── App.kt                    # Entry point
├── ui/screens/home/HomeScreen.kt  # Main UI
└── domain/models/Note.kt     # Core data model
```

**Key files to understand**:
- `domain/models/` - Your data structures
- `domain/repositories/` - Data access interfaces
- `domain/usecases/` - Business logic operations
- `ui/theme/` - Colors and typography

### 3. Make Your First Change

Try changing the theme color:

```kotlin
// ui/theme/Theme.kt
private val SentioPrimary = Color(0xFF2E7D32) // Change this!
```

Run the app again to see your change.

---

## 🛠️ Development Workflow

### Daily Workflow

1. **Pull latest changes** (if working with others)
2. **Run the app** to see current state
3. **Make changes** to one component at a time
4. **Test immediately** - don't accumulate untested code
5. **Commit frequently** with clear messages

### Recommended Order of Development

Follow `NEXT_STEPS.md` for the optimal sequence:

1. **Week 1**: Database + Data Layer
   - Set up SQLDelight
   - Implement real repositories
   - Test CRUD operations

2. **Week 2**: UI + Features
   - Build note list
   - Create editor screen
   - Add navigation
   - Implement search

### Testing Your Changes

```bash
# Quick check (compiles but doesn't run)
./gradlew :composeApp:check

# Full build
./gradlew :composeApp:build

# Run app
./gradlew :composeApp:run

# Clean build (if things get weird)
./gradlew clean build
```

---

## 🎨 UI Development Tips

### Compose Basics

```kotlin
// Simple composable
@Composable
fun MyComponent() {
    Text("Hello Sentio")
}

// With state
@Composable
fun Counter() {
    var count by remember { mutableStateOf(0) }
    Button(onClick = { count++ }) {
        Text("Count: $count")
    }
}

// With ViewModel
@Composable
fun NoteList(viewModel: HomeViewModel) {
    val notes by viewModel.notes.collectAsState()
    LazyColumn {
        items(notes) { note ->
            NoteListItem(note)
        }
    }
}
```

### Material 3 Components

Use these for consistency:
- `Button`, `TextButton`, `IconButton`
- `TextField`, `OutlinedTextField`
- `Card`, `Surface`
- `LazyColumn`, `LazyRow`
- `Scaffold`, `TopAppBar`

### Theme Colors

Access theme colors:
```kotlin
MaterialTheme.colorScheme.primary
MaterialTheme.colorScheme.background
MaterialTheme.colorScheme.surface
```

---

## 💾 Database Development

### SQLDelight Workflow

1. **Write SQL schema** in `.sq` files
2. **Run Gradle** to generate Kotlin code
3. **Use generated code** in repositories

Example:
```kotlin
// After defining Note.sq
val database = SentioDatabase(driver)
val noteQueries = database.noteQueries

// Insert
noteQueries.insert(id, title, content, ...)

// Query
val notes = noteQueries.selectAll().executeAsList()

// With Flow
val notesFlow = noteQueries.selectAll()
    .asFlow()
    .mapToList(Dispatchers.IO)
```

### Database Location

Desktop: `~/.sentio/sentio.db`

---

## 🐛 Debugging Tips

### Common Issues

**"Unresolved reference"**
- Run `./gradlew clean build`
- Sync Gradle in IDE
- Check imports

**"Database locked"**
- Close all connections
- Use transactions properly
- Check for unclosed cursors

**"Compose recomposition issues"**
- Use `remember` for state
- Use `derivedStateOf` for computed values
- Check for infinite loops

### Logging

```kotlin
// Add to any file
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

fun myFunction() {
    logger.info { "Something happened" }
    logger.error { "Error: $message" }
}
```

---

## 📦 Adding Dependencies

### 1. Update `gradle/libs.versions.toml`

```toml
[versions]
myLibrary = "1.0.0"

[libraries]
my-library = { module = "com.example:library", version.ref = "myLibrary" }
```

### 2. Add to `composeApp/build.gradle.kts`

```kotlin
commonMain.dependencies {
    implementation(libs.my.library)
}
```

### 3. Sync Gradle

```bash
./gradlew --refresh-dependencies
```

---

## 🎯 Phase 1 Goals (Weeks 1-2)

By the end of Phase 1, you should be able to:

- ✅ Create a new note
- ✅ Edit note content with markdown
- ✅ See live markdown preview
- ✅ Organize notes in folders
- ✅ Add tags to notes
- ✅ Search across all notes
- ✅ Notes persist after closing app

**Success = You can use Sentio for your own note-taking!**

---

## 🚀 Beyond Phase 1

Once Phase 1 is complete, you'll move to:

**Phase 2**: Smart features (snippets, rich content, advanced search)  
**Phase 3**: AI integration (RAG, semantic search, chat)  
**Phase 4**: Knowledge graph visualization  
**Phase 5**: Polish and performance

See `ROADMAP.md` for details.

---

## 📖 Learning Resources

### Compose Multiplatform
- [Official Docs](https://www.jetbrains.com/lp/compose-multiplatform/)
- [Compose Tutorial](https://developer.android.com/jetpack/compose/tutorial)

### Kotlin
- [Kotlin Docs](https://kotlinlang.org/docs/home.html)
- [Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)

### SQLDelight
- [SQLDelight Docs](https://cashapp.github.io/sqldelight/)
- [Multiplatform Setup](https://cashapp.github.io/sqldelight/2.0.1/multiplatform_sqlite/)

### Architecture
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Repository Pattern](https://developer.android.com/topic/architecture/data-layer)

---

## 💡 Pro Tips

1. **Start small** - Get one feature working before moving to the next
2. **Test frequently** - Run the app after every change
3. **Read the docs** - All documentation is in this repo
4. **Use the REPL** - Kotlin REPL is great for testing logic
5. **Commit often** - Small commits are easier to debug
6. **Ask for help** - Check GitHub issues or Stack Overflow

---

## 🎉 You're Ready!

Open `NEXT_STEPS.md` and start with Step 1: Set Up SQLDelight Database.

Happy coding! 🌿
