# 🌿 Sentio

> A modern, local-first developer workspace built with Kotlin Multiplatform

Sentio combines everything developers need in one seamless tool:

- 📝 **Smart Notebook** — Markdown notes with code syntax highlighting
- 🧩 **Snippet Manager** — Save and organize reusable code snippets
- 📋 **Task Flow** — Kanban boards, timers, and productivity tracking
- 🤖 **AI Assistant** — Semantic search and RAG-powered knowledge retrieval
- 🔄 **Local-First** — Your data stays on your device, with optional cloud sync
- 📱 **Cross-Platform** — Native Android & Desktop apps from shared code

---

## 🏗️ Architecture

Sentio follows **MVVM Clean Architecture** with strict layer separation:

```
┌─────────────────────────────────────────────────────────┐
│  PRESENTATION        UI ← State ← ViewModel ← Events   │
├─────────────────────────────────────────────────────────┤
│  DOMAIN              UseCases ← Repository Interfaces  │
├─────────────────────────────────────────────────────────┤
│  DATA                Repositories → DataSources → DB   │
└─────────────────────────────────────────────────────────┘
```

### Project Structure

```
composeApp/src/commonMain/kotlin/com/example/sentio/
│
├── presentation/          # UI Layer
│   ├── screen/           # Composable screens (Home, Editor)
│   ├── viewmodel/        # ViewModels with event-based pattern
│   ├── state/            # Sealed UI states & events
│   ├── navigation/       # Type-safe navigation routes
│   └── theme/            # Material 3 theming
│
├── domain/               # Business Logic
│   ├── models/           # Domain models (Note, Folder, Tag)
│   ├── repositories/     # Repository interfaces
│   └── usecase/          # Single-purpose use cases
│
├── data/                 # Data Layer
│   ├── local/datasource/ # SQLDelight data sources
│   ├── mapper/           # SQLDelight Entity ↔ Domain mappers
│   ├── repositories/     # Repository implementations
│   └── util/             # DispatcherProvider, utilities
│
├── db/                   # SQLDelight generated entities
│
└── di/                   # Koin dependency injection
```

### Key Patterns

| Pattern | Implementation |
|---------|----------------|
| **Sealed UI States** | `Idle`, `Loading`, `Success`, `Error`, `Empty` |
| **Event-Driven VMs** | Single `onEvent()` entry point |
| **Effects Channel** | One-time events via Kotlin Channels |
| **Data Sources** | Abstract DB operations from repositories |
| **Mapper Layer** | Clean Entity ↔ Domain conversion |

📖 See [ARCHITECTURE.md](ARCHITECTURE.md) for detailed documentation.

---

## 🛠️ Tech Stack

| | Technology |
|---|---|
| **Language** | Kotlin 2.0.21 |
| **UI** | Compose Multiplatform 1.7.0 |
| **Platforms** | Android, Desktop (JVM) |
| **Database** | SQLDelight 2.0.1 |
| **DI** | Koin 4.0 |
| **Navigation** | Navigation Compose 2.8.0-alpha10 |
| **Async** | Coroutines & Flow |

---

## 🚀 Quick Start

### Prerequisites
- JDK 17+
- Android Studio Ladybug+
- Kotlin Multiplatform plugin

### Run Desktop
```bash
./gradlew :composeApp:run
```

### Run Android
```bash
./gradlew :composeApp:installDebug
```

---

## 📄 License

MIT License — see [LICENSE](LICENSE) for details.

