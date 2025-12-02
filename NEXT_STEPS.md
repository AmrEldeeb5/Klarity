# 🚀 Next Steps — Phase 1 Implementation

## What We've Built So Far ✅

### Domain Layer (Complete)
- ✅ Core models: Note, Tag, Folder, Snippet, Attachment, Link
- ✅ Repository interfaces: NoteRepository, TagRepository, FolderRepository
- ✅ Use cases: CreateNote, UpdateNote, DeleteNote, SearchNotes

### Data Layer (Temporary)
- ✅ In-memory repository implementations (for testing)
- ⚠️ Need to replace with SQLDelight

### UI Layer (Basic)
- ✅ Material 3 theme with Sentio branding
- ✅ Basic HomeScreen with sidebar layout
- ✅ App.kt root composable

### Infrastructure
- ✅ Dependency injection container (AppContainer)
- ✅ Project documentation (ROADMAP, ARCHITECTURE, PROJECT_STRUCTURE)

---

## Immediate Next Steps (This Week)

### 1. Set Up SQLDelight Database

**Goal**: Replace in-memory storage with persistent local database

**Tasks**:
```bash
# Add SQLDelight plugin to build.gradle.kts
plugins {
    id("app.cash.sqldelight") version "2.0.1"
}

# Add dependencies
dependencies {
    implementation("app.cash.sqldelight:sqlite-driver:2.0.1")
    implementation("app.cash.sqldelight:coroutines-extensions:2.0.1")
}
```

**Create schema files**:
- `composeApp/src/commonMain/sqldelight/com/example/sentio/db/Note.sq`
- `composeApp/src/commonMain/sqldelight/com/example/sentio/db/Tag.sq`
- `composeApp/src/commonMain/sqldelight/com/example/sentio/db/Folder.sq`

**Example schema** (Note.sq):
```sql
CREATE TABLE Note (
    id TEXT PRIMARY KEY NOT NULL,
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    folderId TEXT,
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL,
    isPinned INTEGER AS Boolean DEFAULT 0,
    isFavorite INTEGER AS Boolean DEFAULT 0,
    FOREIGN KEY (folderId) REFERENCES Folder(id) ON DELETE SET NULL
);

CREATE INDEX note_folderId ON Note(folderId);
CREATE INDEX note_createdAt ON Note(createdAt);

selectAll:
SELECT * FROM Note ORDER BY updatedAt DESC;

selectById:
SELECT * FROM Note WHERE id = ?;

selectByFolder:
SELECT * FROM Note WHERE folderId = ? ORDER BY updatedAt DESC;

insert:
INSERT INTO Note(id, title, content, folderId, createdAt, updatedAt, isPinned, isFavorite)
VALUES (?, ?, ?, ?, ?, ?, ?, ?);

update:
UPDATE Note SET title = ?, content = ?, folderId = ?, updatedAt = ?, isPinned = ?, isFavorite = ?
WHERE id = ?;

delete:
DELETE FROM Note WHERE id = ?;

search:
SELECT * FROM Note 
WHERE title LIKE '%' || ? || '%' OR content LIKE '%' || ? || '%'
ORDER BY updatedAt DESC;
```

**Implement real repositories**:
- Create `SqlDelightNoteRepository.kt`
- Create `SqlDelightTagRepository.kt`
- Create `SqlDelightFolderRepository.kt`

**Update AppContainer** to use real repositories instead of in-memory ones.

---

### 2. Build Note List UI

**Goal**: Display notes in the main content area

**Create components**:
- `ui/components/NoteListItem.kt` - Single note card
- `ui/components/NoteList.kt` - Scrollable list of notes
- `ui/components/EmptyState.kt` - "No notes yet" placeholder

**Create ViewModel**:
```kotlin
// ui/viewmodels/HomeViewModel.kt
class HomeViewModel(
    private val noteRepository: NoteRepository,
    private val createNoteUseCase: CreateNoteUseCase
) : ViewModel() {
    val notes: StateFlow<List<Note>> = noteRepository.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    fun createNote(title: String) {
        viewModelScope.launch {
            createNoteUseCase(title)
        }
    }
}
```

**Update HomeScreen** to use ViewModel and display notes.

---

### 3. Build Note Editor

**Goal**: Create/edit notes with markdown support

**Create EditorScreen**:
```kotlin
// ui/screens/editor/EditorScreen.kt
@Composable
fun EditorScreen(
    noteId: String?,
    onBack: () -> Unit
) {
    // Split view: Editor on left, Preview on right
    Row {
        MarkdownEditor(modifier = Modifier.weight(1f))
        MarkdownPreview(modifier = Modifier.weight(1f))
    }
}
```

**Create components**:
- `ui/components/editor/MarkdownEditor.kt` - Text input with toolbar
- `ui/components/editor/MarkdownPreview.kt` - Rendered markdown
- `ui/components/editor/EditorToolbar.kt` - Bold, italic, code, etc.

**Use CommonMark** for rendering:
```kotlin
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

val parser = Parser.builder().build()
val renderer = HtmlRenderer.builder().build()
val document = parser.parse(markdownText)
val html = renderer.render(document)
```

---

### 4. Add Navigation

**Goal**: Navigate between screens

**Create navigation**:
```kotlin
// ui/navigation/Navigation.kt
sealed class Screen {
    object Home : Screen()
    data class Editor(val noteId: String?) : Screen()
    object Search : Screen()
    object Settings : Screen()
}

@Composable
fun SentioNavigation() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    
    when (val screen = currentScreen) {
        Screen.Home -> HomeScreen(
            onNoteClick = { currentScreen = Screen.Editor(it.id) },
            onCreateNote = { currentScreen = Screen.Editor(null) }
        )
        is Screen.Editor -> EditorScreen(
            noteId = screen.noteId,
            onBack = { currentScreen = Screen.Home }
        )
        // ...
    }
}
```

**Update App.kt** to use navigation.

---

### 5. Add Search Functionality

**Goal**: Full-text search across all notes

**Update database schema** with FTS (Full-Text Search):
```sql
CREATE VIRTUAL TABLE NoteFts USING fts5(
    id UNINDEXED,
    title,
    content
);

-- Triggers to keep FTS in sync
CREATE TRIGGER note_ai AFTER INSERT ON Note BEGIN
    INSERT INTO NoteFts(id, title, content) VALUES (new.id, new.title, new.content);
END;

CREATE TRIGGER note_ad AFTER DELETE ON Note BEGIN
    DELETE FROM NoteFts WHERE id = old.id;
END;

CREATE TRIGGER note_au AFTER UPDATE ON Note BEGIN
    UPDATE NoteFts SET title = new.title, content = new.content WHERE id = new.id;
END;
```

**Create SearchScreen**:
```kotlin
@Composable
fun SearchScreen() {
    var query by remember { mutableStateOf("") }
    val results by viewModel.searchResults.collectAsState()
    
    Column {
        SearchBar(query = query, onQueryChange = { query = it })
        SearchResults(results = results)
    }
}
```

---

## Testing Your Progress

After each step, test:

1. **Database**: Create a note, close app, reopen → note should persist
2. **UI**: Click "Create Note" → should open editor
3. **Editor**: Type markdown → should render in preview
4. **Navigation**: Click note → should open editor with that note
5. **Search**: Type query → should filter notes

---

## Quick Commands

```bash
# Run app
./gradlew :composeApp:run

# Clean build
./gradlew clean build

# Check for errors
./gradlew :composeApp:check

# Generate SQLDelight code
./gradlew :composeApp:generateCommonMainSentioInterface
```

---

## Common Issues & Solutions

### SQLDelight not generating code
- Run `./gradlew clean`
- Check `.sq` file syntax
- Ensure plugin is applied correctly

### Compose preview not working
- Use `@Preview` annotation
- Run from IDE's preview panel

### Database locked error
- Close all database connections properly
- Use `use { }` blocks for transactions

---

## Resources

- [SQLDelight Docs](https://cashapp.github.io/sqldelight/)
- [Compose Multiplatform Docs](https://www.jetbrains.com/lp/compose-multiplatform/)
- [CommonMark Spec](https://commonmark.org/)
- [Material 3 Guidelines](https://m3.material.io/)

---

## Success Criteria for Phase 1

By end of Week 2, you should have:

- ✅ Persistent local database
- ✅ Create, read, update, delete notes
- ✅ Markdown editor with live preview
- ✅ Full-text search
- ✅ Folder organization
- ✅ Tag management
- ✅ Basic keyboard shortcuts

**Then you're ready for Phase 2: Smart Features!** 🎉
