# 🌿 Sentio — The Unified Developer Operating System

Sentio is a smart developer notebook with AI-powered memory, knowledge graphs, and seamless workflow integration. Built with Compose Multiplatform for desktop.

## 🎯 Vision

Sentio combines four mega-systems into one unified platform:

1. **Cognitive Layer** (Current Focus) - Smart notebook with AI memory and RAG
2. **Execution Layer** (Future) - Tasks, timers, and project orchestration
3. **Systems Intelligence Layer** (Future) - API monitoring and backend analysis
4. **Collaboration Layer** (Future) - Team workspaces and real-time sync

## 🚀 Current Status: Phase 1 - Complete ✅

We've built the **Cognitive Layer** foundation - a powerful note-taking system with:

- ✅ Domain models (Note, Tag, Folder, Snippet)
- ✅ Clean architecture (Domain, Data, UI layers)
- ✅ Material 3 UI with search and editor
- ✅ SQLDelight database with persistent storage
- ✅ Koin dependency injection
- ✅ Navigation Compose
- ✅ ViewModels with reactive state
- ✅ Full-text search
- 🚧 Markdown rendering (next - Phase 2)
- 🚧 Folder/Tag management UI (next - Phase 2)

## 🏗️ Architecture

Sentio follows clean architecture principles:

```
UI Layer (Compose) → Domain Layer (Use Cases) → Data Layer (Repositories)
```

See [ARCHITECTURE.md](ARCHITECTURE.md) for detailed design documentation.

## 📋 Roadmap

See [ROADMAP.md](ROADMAP.md) for the complete development plan.

**Phase 1** (Weeks 1-2): Basic note-taking with local storage  
**Phase 2** (Weeks 3-4): Smart features (search, snippets, rich content)  
**Phase 3** (Weeks 5-6): AI integration (RAG, semantic search)  
**Phase 4** (Weeks 7-8): Knowledge graph visualization  
**Phase 5** (Weeks 9-10): Polish and performance

## 🛠️ Tech Stack

- **UI**: Compose Multiplatform + Material 3
- **Language**: Kotlin
- **Database**: SQLDelight (planned)
- **Async**: Kotlin Coroutines + Flow
- **Markdown**: CommonMark
- **AI**: OpenAI API / Local LLMs (planned)
- **Build**: Gradle with Kotlin DSL

## 🚦 Getting Started

### Prerequisites

- JDK 11 or higher
- Gradle 8.x (included via wrapper)

### Run the Desktop App

```bash
./gradlew :composeApp:run
```

On Windows:
```cmd
gradlew.bat :composeApp:run
```

### Build Desktop Installer

```bash
./gradlew :composeApp:packageDistributionForCurrentOS
```

This creates platform-specific installers:
- `.dmg` for macOS
- `.msi` for Windows
- `.deb` for Linux

### Run Tests

```bash
./gradlew :composeApp:test
```

## 📁 Project Structure

```
sentio/
├── composeApp/src/
│   ├── commonMain/
│   │   ├── kotlin/          # Shared Kotlin code
│   │   │   ├── domain/      # Business logic
│   │   │   ├── data/        # Data layer (SQLDelight)
│   │   │   ├── ui/          # Compose UI
│   │   │   └── di/          # Koin DI
│   │   └── sqldelight/      # Database schemas
│   └── jvmMain/kotlin/      # Desktop-specific code
└── Documentation/           # See below
```

See [FILE_STRUCTURE.md](FILE_STRUCTURE.md) for complete file tree.

## 🎨 Design Philosophy

- **Local-first**: Your data stays on your machine
- **Keyboard-driven**: Fast navigation with shortcuts
- **AI-enhanced**: Smart features without being intrusive
- **Developer-focused**: Built by developers, for developers
- **Clean code**: Maintainable, testable, extensible

## 📚 Documentation

**Start here**: [DOCS_INDEX.md](DOCS_INDEX.md) - Complete documentation index

| Document | Description |
|----------|-------------|
| [QUICK_START.md](QUICK_START.md) | How to run and use the app |
| [PHASE_1_COMPLETE.md](PHASE_1_COMPLETE.md) | ✅ Phase 1 completion summary |
| [ROADMAP.md](ROADMAP.md) | 10-week development plan |
| [ARCHITECTURE.md](ARCHITECTURE.md) | Technical architecture |
| [FILE_STRUCTURE.md](FILE_STRUCTURE.md) | Complete file tree |
| [GETTING_STARTED.md](GETTING_STARTED.md) | Developer guide |
| [NEXT_STEPS.md](NEXT_STEPS.md) | Upcoming tasks (Phase 2) |

## 🤝 Contributing

This is currently a personal project in early development. Contributions will be welcome once the foundation is stable.

## 📄 License

See [LICENSE](LICENSE) file for details.

## 🔮 Future Plans

- Multi-platform support (Web, Android, iOS)
- Cloud sync with end-to-end encryption
- Plugin system for extensibility
- Integration with Execution Layer (tasks, timers)
- Team collaboration features

---

**Built with ❤️ using Kotlin and Compose Multiplatform**
