# 🔄 Kotlinx Serialization Guide for Sentio

## Current Status

✅ **Kotlinx Serialization 1.7.3 is already configured!**

### Configured Dependencies

```toml
kotlinxSerializationJson = "1.7.3"
kotlinx-serialization-json

# Plugin
kotlinSerialization = "org.jetbrains.kotlin.plugin.serialization"
```

---

## 🎯 Use Cases in Sentio

### Current (Phase 1)
- Navigation routes (type-safe)
- API responses (Ktor integration)
- Data transfer objects

### Future (Phase 2+)
- Export/Import notes (JSON)
- Settings persistence
- API communication
- WebSocket messages

---

## 🚀 Quick Start

### 1. Basic Serialization

```kotlin
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
data class Note(
    val id: String,
    val title: String,
    val content: String
)

// Serialize to JSON
val note = Note("1", "My Note", "Content")
val json = Json.encodeToString(note)
// {"id":"1","title":"My Note","content":"Content"}

// Deserialize from JSON
val noteFromJson = Json.decodeFromString<Note>(json)
```

### 2. Custom JSON Configuration

```kotlin
val json = Json {
    prettyPrint = true           // Format with indentation
    isLenient = true             // Accept non-standard JSON
    ignoreUnknownKeys = true     // Ignore extra fields
    coerceInputValues = true     // Handle null as default
    encodeDefaults = true        // Include default values
    explicitNulls = false        // Omit null fields
}
```

---

## 📚 Common Patterns

### Basic Data Class

```kotlin
@Serializable
data class User(
    val id: String,
    val name: String,
    val email: String
)
```

### Optional Fields

```kotlin
@Serializable
data class Note(
    val id: String,
    val title: String,
    val content: String = "",           // Default value
    val tags: List<String> = emptyList(), // Default empty list
    val metadata: Map<String, String>? = null // Nullable
)
```

### Custom Field Names

```kotlin
@Serializable
data class ApiResponse(
    @SerialName("user_id")
    val userId: String,
    
    @SerialName("created_at")
    val createdAt: String
)
```

### Nested Objects

```kotlin
@Serializable
data class NoteWithAuthor(
    val id: String,
    val title: String,
    val author: Author
)

@Serializable
data class Author(
    val name: String,
    val email: String
)
```

### Lists and Collections

```kotlin
@Serializable
data class NotesResponse(
    val notes: List<Note>,
    val total: Int,
    val page: Int
)

// Serialize list directly
val notes = listOf(note1, note2, note3)
val json = Json.encodeToString(notes)
```

### Maps

```kotlin
@Serializable
data class Settings(
    val preferences: Map<String, String>,
    val flags: Map<String, Boolean>
)
```

---

## 🎨 Sentio-Specific Examples

### 1. Export Notes to JSON

```kotlin
// domain/usecases/export/ExportNotesUseCase.kt
class ExportNotesUseCase(
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(): Result<String> {
        return try {
            val notes = noteRepository.getAllNotes().first()
            val exportData = NotesExport(
                version = "1.0",
                exportedAt = Clock.System.now().toString(),
                notes = notes.map { it.toExportFormat() }
            )
            val json = Json {
                prettyPrint = true
                encodeDefaults = true
            }.encodeToString(exportData)
            Result.success(json)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

@Serializable
data class NotesExport(
    val version: String,
    val exportedAt: String,
    val notes: List<NoteExport>
)

@Serializable
data class NoteExport(
    val id: String,
    val title: String,
    val content: String,
    val tags: List<String>,
    val createdAt: String,
    val updatedAt: String
)

fun Note.toExportFormat() = NoteExport(
    id = id,
    title = title,
    content = content,
    tags = tags.map { it.name },
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString()
)
```

### 2. Import Notes from JSON

```kotlin
// domain/usecases/import/ImportNotesUseCase.kt
class ImportNotesUseCase(
    private val noteRepository: NoteRepository,
    private val tagRepository: TagRepository
) {
    suspend operator fun invoke(jsonString: String): Result<Int> {
        return try {
            val exportData = Json.decodeFromString<NotesExport>(jsonString)
            
            var importedCount = 0
            exportData.notes.forEach { noteExport ->
                // Create tags if they don't exist
                val tags = noteExport.tags.map { tagName ->
                    tagRepository.getOrCreateTag(tagName)
                }
                
                // Create note
                val note = Note(
                    id = uuid4().toString(),
                    title = noteExport.title,
                    content = noteExport.content,
                    folderId = null,
                    tags = tags,
                    createdAt = Instant.parse(noteExport.createdAt),
                    updatedAt = Instant.parse(noteExport.updatedAt)
                )
                
                noteRepository.createNote(note)
                importedCount++
            }
            
            Result.success(importedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### 3. Settings Persistence

```kotlin
// data/local/SettingsManager.kt
class SettingsManager(private val context: Context) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    private val settingsFile = File(context.filesDir, "settings.json")
    
    fun saveSettings(settings: AppSettings) {
        val jsonString = json.encodeToString(settings)
        settingsFile.writeText(jsonString)
    }
    
    fun loadSettings(): AppSettings {
        return if (settingsFile.exists()) {
            val jsonString = settingsFile.readText()
            json.decodeFromString(jsonString)
        } else {
            AppSettings() // Default settings
        }
    }
}

@Serializable
data class AppSettings(
    val theme: String = "dark",
    val fontSize: Int = 16,
    val autoSave: Boolean = true,
    val aiEnabled: Boolean = false,
    val apiKey: String? = null
)
```

### 4. API Request/Response (with Ktor)

```kotlin
// data/remote/ai/OpenAIModels.kt
@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Float = 0.7f,
    @SerialName("max_tokens")
    val maxTokens: Int? = null
)

@Serializable
data class ChatMessage(
    val role: String,
    val content: String
)

@Serializable
data class ChatResponse(
    val id: String,
    val choices: List<ChatChoice>,
    val usage: Usage
)

@Serializable
data class ChatChoice(
    val index: Int,
    val message: ChatMessage,
    @SerialName("finish_reason")
    val finishReason: String
)

@Serializable
data class Usage(
    @SerialName("prompt_tokens")
    val promptTokens: Int,
    @SerialName("completion_tokens")
    val completionTokens: Int,
    @SerialName("total_tokens")
    val totalTokens: Int
)

// Usage with Ktor
suspend fun chat(request: ChatRequest): ChatResponse {
    return client.post("https://api.openai.com/v1/chat/completions") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()
}
```

---

## 🔧 Advanced Features

### Custom Serializers

```kotlin
@Serializable
data class Note(
    val id: String,
    val title: String,
    @Serializable(with = InstantSerializer::class)
    val createdAt: Instant
)

object InstantSerializer : KSerializer<Instant> {
    override val descriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)
    
    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString())
    }
    
    override fun deserialize(decoder: Decoder): Instant {
        return Instant.parse(decoder.decodeString())
    }
}
```

### Polymorphic Serialization

```kotlin
@Serializable
sealed class Content {
    @Serializable
    @SerialName("text")
    data class Text(val text: String) : Content()
    
    @Serializable
    @SerialName("image")
    data class Image(val url: String, val caption: String) : Content()
    
    @Serializable
    @SerialName("code")
    data class Code(val code: String, val language: String) : Content()
}

@Serializable
data class RichNote(
    val id: String,
    val title: String,
    val contents: List<Content>
)
```

### Transient Fields

```kotlin
@Serializable
data class Note(
    val id: String,
    val title: String,
    val content: String,
    @Transient
    val isSelected: Boolean = false // Not serialized
)
```

### Default Values

```kotlin
@Serializable
data class Settings(
    val theme: String = "dark",
    val fontSize: Int = 16,
    val autoSave: Boolean = true
)

// When deserializing, missing fields use defaults
val json = """{"theme":"light"}"""
val settings = Json.decodeFromString<Settings>(json)
// settings.fontSize == 16 (default)
// settings.autoSave == true (default)
```

---

## 🎯 Best Practices

### 1. Use Data Classes

```kotlin
// ✅ Good
@Serializable
data class Note(val id: String, val title: String)

// ❌ Avoid
@Serializable
class Note(val id: String, val title: String)
```

### 2. Provide Defaults

```kotlin
// ✅ Good - Handles missing fields gracefully
@Serializable
data class Note(
    val id: String,
    val title: String,
    val content: String = ""
)

// ❌ Risky - Fails if field is missing
@Serializable
data class Note(
    val id: String,
    val title: String,
    val content: String
)
```

### 3. Use SerialName for API Fields

```kotlin
// ✅ Good - Kotlin naming + API compatibility
@Serializable
data class User(
    @SerialName("user_id")
    val userId: String,
    @SerialName("created_at")
    val createdAt: String
)
```

### 4. Handle Nullability Properly

```kotlin
@Serializable
data class Note(
    val id: String,
    val title: String,
    val description: String? = null,  // Optional field
    val tags: List<String> = emptyList() // Empty list instead of null
)
```

### 5. Version Your Data

```kotlin
@Serializable
data class NotesExport(
    val version: String = "1.0",
    val notes: List<Note>
)

// When importing, check version
fun import(json: String) {
    val export = Json.decodeFromString<NotesExport>(json)
    when (export.version) {
        "1.0" -> importV1(export)
        "2.0" -> importV2(export)
        else -> throw UnsupportedVersionException()
    }
}
```

---

## 🔍 Error Handling

### Safe Deserialization

```kotlin
fun safeDeserialize(json: String): Result<Note> {
    return try {
        val note = Json.decodeFromString<Note>(json)
        Result.success(note)
    } catch (e: SerializationException) {
        Result.failure(Exception("Invalid JSON format: ${e.message}"))
    } catch (e: IllegalArgumentException) {
        Result.failure(Exception("Invalid data: ${e.message}"))
    }
}
```

### Validation After Deserialization

```kotlin
@Serializable
data class Note(
    val id: String,
    val title: String,
    val content: String
) {
    init {
        require(id.isNotBlank()) { "ID cannot be blank" }
        require(title.isNotBlank()) { "Title cannot be blank" }
    }
}
```

---

## 🧪 Testing

### Test Serialization

```kotlin
@Test
fun `test note serialization`() {
    val note = Note("1", "Test", "Content")
    val json = Json.encodeToString(note)
    val decoded = Json.decodeFromString<Note>(json)
    
    assertEquals(note, decoded)
}

@Test
fun `test handles missing optional fields`() {
    val json = """{"id":"1","title":"Test"}"""
    val note = Json.decodeFromString<Note>(json)
    
    assertEquals("", note.content) // Default value
}
```

---

## 📊 Performance Tips

### 1. Reuse Json Instance

```kotlin
// ✅ Good - Single instance
object JsonConfig {
    val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
}

// ❌ Avoid - New instance each time
fun serialize(note: Note): String {
    return Json { prettyPrint = true }.encodeToString(note)
}
```

### 2. Use Streaming for Large Data

```kotlin
// For very large JSON files
fun streamLargeJson(file: File): Flow<Note> = flow {
    file.bufferedReader().use { reader ->
        // Process line by line
        reader.lineSequence().forEach { line ->
            val note = Json.decodeFromString<Note>(line)
            emit(note)
        }
    }
}
```

---

## 🔗 Integration with Other Libraries

### With Ktor

```kotlin
// Already configured!
install(ContentNegotiation) {
    json(Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    })
}

// Automatic serialization/deserialization
val response: MyData = client.get("url")
client.post("url") { setBody(myData) }
```

### With DataStore (Future)

```kotlin
val settingsDataStore = DataStore(
    serializer = SettingsSerializer,
    produceFile = { File("settings.json") }
)

object SettingsSerializer : Serializer<AppSettings> {
    override val defaultValue = AppSettings()
    
    override suspend fun readFrom(input: InputStream): AppSettings {
        return Json.decodeFromString(input.readBytes().decodeToString())
    }
    
    override suspend fun writeTo(t: AppSettings, output: OutputStream) {
        output.write(Json.encodeToString(t).encodeToByteArray())
    }
}
```

---

## 📚 Resources

### Official Documentation
- [Kotlinx Serialization Guide](https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/serialization-guide.md)
- [JSON Serialization](https://github.com/Kotlin/kotlinx.serialization/blob/master/formats/README.md#json)

### Sentio Use Cases
- Export/Import notes
- API communication (OpenAI, etc.)
- Settings persistence
- Navigation routes

---

## ✅ Quick Reference

```kotlin
// Serialize
val json = Json.encodeToString(myObject)

// Deserialize
val obj = Json.decodeFromString<MyClass>(json)

// Custom config
val json = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
}

// Lists
val list = listOf(obj1, obj2)
val json = Json.encodeToString(list)

// Maps
val map = mapOf("key" to "value")
val json = Json.encodeToString(map)

// Nullable
val obj: MyClass? = Json.decodeFromString(json)

// Safe
val result = runCatching {
    Json.decodeFromString<MyClass>(json)
}
```

---

**Kotlinx Serialization is ready to power Sentio's data handling! 🚀**

*Guide created: December 2, 2025*
