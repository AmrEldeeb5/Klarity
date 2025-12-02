# 🌿 Sentio Cognitive Layer — Development Roadmap

## Vision
Build a smart developer notebook with AI-powered memory, vector search, and knowledge graph capabilities.

---

## Phase 1: Foundation (Weeks 1-2)
**Goal**: Basic note-taking with local storage

### Milestone 1.1: Core Data Layer
- [ ] Define domain models (Note, Tag, Folder, Snippet)
- [ ] Set up local database (SQLDelight or Room)
- [ ] Implement repository pattern
- [ ] Add basic CRUD operations

### Milestone 1.2: Basic UI
- [ ] Main window layout (sidebar + editor + preview)
- [ ] Note list view with search
- [ ] Markdown editor (basic text input)
- [ ] Folder tree navigation
- [ ] Tag management UI

### Milestone 1.3: Markdown Support
- [ ] Markdown rendering (CommonMark)
- [ ] Syntax highlighting for code blocks
- [ ] Live preview toggle
- [ ] Export to HTML/PDF

**Deliverable**: Working note-taking app with folders, tags, and markdown

---

## Phase 2: Smart Features (Weeks 3-4)
**Goal**: Add search, snippets, and basic intelligence

### Milestone 2.1: Advanced Search
- [ ] Full-text search across all notes
- [ ] Filter by tags, folders, date
- [ ] Search history
- [ ] Keyboard shortcuts (Cmd+K / Ctrl+K)

### Milestone 2.2: Code Snippets
- [ ] Snippet creation with language detection
- [ ] Syntax highlighting (20+ languages)
- [ ] Copy to clipboard
- [ ] Snippet templates
- [ ] Quick insert into notes

### Milestone 2.3: Rich Content
- [ ] Image embedding (drag & drop)
- [ ] File attachments
- [ ] Links between notes (wiki-style)
- [ ] Backlinks view

**Deliverable**: Feature-complete notebook with snippets and rich content

---

## Phase 3: AI Integration (Weeks 5-6)
**Goal**: Add AI memory and intelligent features

### Milestone 3.1: AI Backend Setup
- [ ] Integrate OpenAI API (or local LLM)
- [ ] Implement embedding generation
- [ ] Set up vector database (Chroma/Qdrant)
- [ ] Build RAG pipeline

### Milestone 3.2: AI Features
- [ ] "Ask Sentio" chat interface
- [ ] Semantic search (vector similarity)
- [ ] Auto-tagging suggestions
- [ ] Note summarization
- [ ] Related notes suggestions

### Milestone 3.3: AI Memory
- [ ] Conversation history
- [ ] Context-aware responses
- [ ] Memory persistence
- [ ] Privacy controls (local-first option)

**Deliverable**: AI-powered notebook with semantic search and chat

---

## Phase 4: Knowledge Graph (Weeks 7-8)
**Goal**: Visualize connections between notes

### Milestone 4.1: Graph Data Model
- [ ] Build relationship model (note-to-note links)
- [ ] Extract entities from notes (auto-detect)
- [ ] Track references and citations
- [ ] Bidirectional linking

### Milestone 4.2: Graph Visualization
- [ ] Interactive graph view (force-directed layout)
- [ ] Node filtering (by tag, type, date)
- [ ] Click to navigate
- [ ] Zoom and pan controls

### Milestone 4.3: Graph Intelligence
- [ ] Suggest connections
- [ ] Find knowledge gaps
- [ ] Cluster related topics
- [ ] Export graph data

**Deliverable**: Visual knowledge graph with intelligent suggestions

---

## Phase 5: Polish & Performance (Weeks 9-10)
**Goal**: Production-ready desktop app

### Milestone 5.1: Performance
- [ ] Lazy loading for large note collections
- [ ] Database indexing optimization
- [ ] Async operations for AI calls
- [ ] Memory usage optimization

### Milestone 5.2: UX Polish
- [ ] Dark/light theme
- [ ] Customizable shortcuts
- [ ] Settings panel
- [ ] Onboarding tutorial
- [ ] Keyboard-first navigation

### Milestone 5.3: Data Management
- [ ] Import from Notion, Obsidian, Markdown files
- [ ] Export entire workspace
- [ ] Backup and restore
- [ ] Sync preparation (local-first architecture)

**Deliverable**: Polished v1.0 ready for personal use

---

## Future Phases (Post-Launch)

### Phase 6: Execution Layer Integration
- Task creation from notes
- Link tasks to documentation
- Time tracking integration

### Phase 7: Multi-Platform
- Web version (Compose for Web)
- Mobile apps (Android/iOS)
- Cloud sync

### Phase 8: Collaboration
- Shared workspaces
- Real-time editing
- Team knowledge base

---

## Tech Stack

**Frontend**
- Compose Multiplatform (Desktop)
- Material 3 Design
- Kotlin Coroutines + Flow

**Data Layer**
- SQLDelight (local database)
- Kotlinx Serialization
- DataStore (preferences)

**AI/ML**
- OpenAI API (GPT-4, embeddings)
- Ktor (HTTP client)
- Local LLM option (Ollama)

**Markdown & Code**
- CommonMark (parsing)
- Highlight.js or similar (syntax highlighting)

**Graph**
- Custom force-directed layout
- Canvas API for rendering

---

## Success Metrics

**Phase 1-2**: Can replace basic note apps (Notion, Bear)
**Phase 3-4**: Unique AI features that competitors don't have
**Phase 5**: Daily driver for developers

---

## Risk Mitigation

**Risk**: AI costs too high
**Mitigation**: Support local LLMs (Ollama, LM Studio)

**Risk**: Performance issues with large note collections
**Mitigation**: Pagination, lazy loading, database indexing

**Risk**: Scope creep
**Mitigation**: Stick to Cognitive Layer only for v1.0
