# Klarity

> A local-first notes & tasks workspace with an agentic AI assistant — built with Kotlin Multiplatform & Compose.

Klarity is a personal workspace for notes and tasks that runs natively on **Android** and **Desktop (JVM)** from one shared Kotlin codebase. Your data lives on your device (SQLDelight), and **Lou** — the built-in AI assistant — can search it, answer grounded questions, and act on it for you with your confirmation.

---

## Features

### Notebook
- Markdown notes with a **slash (`/`) command menu** for headings, lists, quotes, code, dividers, and to-dos
- Interactive **to-do checkboxes** you can tick straight from the rendered view
- **Folders**, pinning, and per-note status
- Instant search across titles, content, and tags

### Tasks
- A Kanban **Board** (Backlog, In Progress, In Review, Done)
- **List**, **Calendar**, and **Timeline** views of the same tasks
- Priorities, due dates, and tags
- A collapsible **Archived** section to restore removed tasks

### Lou — the AI assistant
Lou is a pluggable, multi-provider assistant grounded in your workspace:

- **Bring your own model** — Anthropic (Claude), OpenAI, or any OpenAI-compatible endpoint (Groq, OpenRouter, Ollama, LM Studio, and more). Without a key, Lou answers from local search only.
- **Grounded answers (RAG)** — keyword-ranked retrieval over your notes & tasks, with cited sources and streamed, token-by-token replies.
- **Agentic actions** — ask Lou to create, edit, complete, move, archive, or pin notes & tasks and it proposes each change as an **Approve / Cancel** card. Nothing is written without your confirmation.
- **Recoverable by design** — Lou never hard-deletes; "delete" archives the item so you can restore it.
- **Multi-step & self-serve** — Lou continues after you approve, and can `search_workspace` to find items it needs to act on.
- **Tunable** — pick a response style (temperature) per model, or switch Lou to read-only.

---

## Tech Stack

| | Technology |
|---|---|
| **Language** | Kotlin 2.2.10 |
| **UI** | Compose Multiplatform 1.11.1 · Material 3 Expressive |
| **Platforms** | Android · Desktop (JVM) |
| **Database** | SQLDelight 2.0.2 |
| **DI** | Koin 4.0.2 |
| **Networking (AI)** | Ktor 3.0.3 |
| **Dates** | kotlinx-datetime |
| **Build** | AGP 9.1.0 · Gradle |

---

## Getting Started

### Prerequisites
- JDK 17+
- Android Studio (latest) with the Kotlin Multiplatform plugin

### Run on Desktop
```bash
./gradlew :composeApp:run
```

### Run on Android
```bash
./gradlew :composeApp:installDebug
```

### Tests
```bash
./gradlew :composeApp:jvmTest
```

---

## Setting up Lou

1. Open **Settings** (gear icon, bottom of the sidebar).
2. Pick a **Provider** and paste an **API key** (Claude, OpenAI, or any OpenAI-compatible base URL such as Groq).
3. Optionally **Load models** to pick one, choose a **Response style**, and toggle **Let Lou take actions**.

Your key is stored locally on the device and sent only to the provider you choose.

---

## Architecture

Klarity follows an MVVM + clean-architecture split, with repositories backed directly by SQLDelight (no separate data-source layer):

```
presentation/   Compose UI · WorkspaceViewModel · theme
domain/         models · repository interfaces · use cases
data/           repository impls (SQLDelight) · ai/ (Lou) · mappers
di/             Koin modules
```

The AI layer lives in `data/ai/` — `AiService` (Ktor, streaming + tool-calling), `WorkspaceRetrieval` (RAG ranking), `AiTools`/`AiActions` (the agentic toolset), and `ChatHistory`.

See [ARCHITECTURE.md](ARCHITECTURE.md) for the full breakdown.

---

## License

MIT License — see [LICENSE](LICENSE).
