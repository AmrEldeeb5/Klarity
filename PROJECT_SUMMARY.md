# 🌿 Sentio - Project Summary

## One-Page Overview

### What is Sentio?
A **smart developer notebook** with AI-powered memory, knowledge graphs, and workflow integration. Built with Compose Multiplatform for desktop.

### Current Status
**Phase 1 Complete** ✅ - Fully functional note-taking app with persistent storage

---

## Quick Stats

| Metric | Value |
|--------|-------|
| **Status** | Phase 1 Complete |
| **Lines of Code** | ~2,500 |
| **Files** | 35 Kotlin + 3 SQL |
| **Documentation** | 10 markdown files |
| **Tech Stack** | Kotlin, Compose, SQLDelight, Koin |
| **Platform** | Desktop (JVM) |
| **Database** | SQLite (local) |

---

## Features

### ✅ Working Now
- Create, edit, delete notes
- Full-text search
- Pin and favorite notes
- Auto-save
- Persistent storage
- Material 3 UI
- Split-view editor

### 🚧 Coming Soon (Phase 2)
- Markdown rendering
- Syntax highlighting
- Folder management
- Tag management
- Image embedding
- Keyboard shortcuts

### 🔮 Future (Phase 3+)
- AI chat ("Ask Sentio")
- Semantic search
- Knowledge graph
- Team collaboration

---

## Architecture

```
┌─────────────────────────────────────┐
│     UI Layer (Compose)              │
│  HomeScreen, EditorScreen           │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│   Domain Layer (Business Logic)     │
│  Models, Use Cases, Repositories    │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│    Data Layer (SQLDelight)          │
│  Database, Queries, Persistence     │
└─────────────────────────────────────┘
```

**Pattern**: Clean Architecture + MVVM + Repository Pattern

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| **UI** | Compose Multiplatform + Material 3 |
| **State** | Kotlin Flow + StateFlow |
| **DI** | Koin 4.0 |
| **Database** | SQLDelight 2.0 |
| **Navigation** | Navigation Compose |
| **Language** | Kotlin 2.0 |
| **Build** | Gradle 8.14 |

---

## File Structure

```
sentio/
├── composeApp/src/
│   ├── commonMain/
│   │   ├── kotlin/
│   │   │   ├── domain/      # 7 models, 3 repos, 4 use cases
│   │   │   ├── data/        # 3 repo implementations
│   │   │   ├── ui/          # 2 screens, 2 viewmodels
│   │   │   └── di/          # Koin setup
│   │   └── sqldelight/      # 3 SQL schemas
│   └── jvmMain/kotlin/      # Desktop-specific
└── docs/                    # 10 markdown files
```

---

## How to Use

### Run
```bash
./gradlew :composeApp:run
```

### Create Note
1. Click + button
2. Type content
3. Auto-saves

### Search
Type in search bar → instant results

### Data Location
`~/.sentio/sentio.db`

---

## Documentation

| For... | Read... |
|--------|---------|
| **Users** | [QUICK_START.md](QUICK_START.md) |
| **Developers** | [GETTING_STARTED.md](GETTING_STARTED.md) |
| **Architects** | [ARCHITECTURE.md](ARCHITECTURE.md) |
| **Contributors** | [NEXT_STEPS.md](NEXT_STEPS.md) |
| **Overview** | [DOCS_INDEX.md](DOCS_INDEX.md) |

---

## Roadmap

### Phase 1 (Weeks 1-2) ✅ COMPLETE
Foundation: Notes, search, persistence

### Phase 2 (Weeks 3-4) 🚧 NEXT
Smart features: Markdown, snippets, rich content

### Phase 3 (Weeks 5-6) 📅 PLANNED
AI integration: RAG, semantic search, chat

### Phase 4 (Weeks 7-8) 📅 PLANNED
Knowledge graph: Visualization, linking

### Phase 5 (Weeks 9-10) 📅 PLANNED
Polish: Performance, UX, export

---

## Key Decisions

### Why Compose Multiplatform?
- Single codebase for desktop/mobile
- Modern declarative UI
- Great developer experience

### Why SQLDelight?
- Type-safe SQL
- Compile-time verification
- Multiplatform support

### Why Koin?
- Lightweight DI
- Kotlin-first
- Easy to test

### Why Local-First?
- Privacy
- Speed
- Offline-capable
- User owns data

---

## Success Metrics

### Phase 1 Goals
- [x] Persistent storage
- [x] CRUD operations
- [x] Search functionality
- [x] Clean architecture
- [x] Documentation

### Phase 2 Goals
- [ ] Markdown rendering
- [ ] Folder/tag UI
- [ ] Rich content
- [ ] Keyboard shortcuts
- [ ] Export features

---

## Team

**Current**: Solo developer project  
**Future**: Open to contributors after Phase 2

---

## Links

- **Repository**: (Add GitHub link)
- **Issues**: (Add issue tracker)
- **Discussions**: (Add discussion forum)
- **Releases**: (Add releases page)

---

## License

See [LICENSE](LICENSE) file

---

## Contact

- **Project**: Sentio
- **Version**: 1.0.0 (Phase 1)
- **Status**: Active Development
- **Platform**: Desktop (JVM)

---

**Built with ❤️ using Kotlin and Compose Multiplatform**

*Last updated: December 2, 2025*
