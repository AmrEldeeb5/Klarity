# 🌐 Ktor Setup Guide for Sentio

## Current Status

✅ **Ktor 3.0.0 is already configured** in your project!

### Configured Dependencies

```toml
ktor = "3.0.0"

# Core
ktor-client-core
ktor-client-content-negotiation
ktor-serialization-kotlinx-json
ktor-client-logging

# Platform-specific engines
ktor-client-cio (Desktop/JVM)
ktor-client-okhttp (Android)
```

---

## 🎯 Use Cases in Sentio

### Phase 3: AI Integration
- OpenAI API calls (GPT-4, embeddings)
- Streaming responses for chat
- Vector database API (Chroma/Qdrant)

### Phase 4: Knowledge Graph
- External API integrations
- Web scraping for note enrichment
- Link preview generation

### Future: Collaboration
- WebSocket for real-time sync
- Cloud storage API
- User authentication

---

## 🚀 Quick Start

### 1. Create Ktor Client

Create a shared HTTP client in the data layer:

```kotlin
// data/remote/KtorClient.kt
package com.example.sentio.data.remote

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object KtorClient {
    val client = HttpClient {
        // JSON serialization
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }

        // Logging
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }

        // Timeout configuration
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 30_000
        }

        // Default request configuration
        defaultRequest {
            url("https://api.example.com/")
        }
    }
}
```

### 2. Create API Service

Example for OpenAI integration:

```kotlin
// data/remote/ai/OpenAIService.kt
package com.example.sentio.data.remote.ai

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

class OpenAIService(
    private val client: HttpClient,
    private val apiKey: String
) {
    private val baseUrl = "https://api.openai.com/v1"

    suspend fun generateEmbedding(text: String): Result<List<Float>> {
        return try {
            val response = client.post("$baseUrl/embeddings") {
                header("Authorization", "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(EmbeddingRequest(
                    model = "text-embedding-3-small",
                    input = text
                ))
            }
            
            val result = response.body<EmbeddingResponse>()
            Result.success(result.data.first().embedding)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun chat(messages: List<ChatMessage>): Result<String> {
        return try {
            val response = client.post("$baseUrl/chat/completions") {
                header("Authorization", "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(ChatRequest(
                    model = "gpt-4",
                    messages = messages
                ))
            }
            
            val result = response.body<ChatResponse>()
            Result.success(result.choices.first().message.content)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

@Serializable
data class EmbeddingRequest(
    val model: String,
    val input: String
)

@Serializable
data class EmbeddingResponse(
    val data: List<EmbeddingData>
)

@Serializable
data class EmbeddingData(
    val embedding: List<Float>
)

@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>
)

@Serializable
data class ChatMessage(
    val role: String,
    val content: String
)

@Serializable
data class ChatResponse(
    val choices: List<ChatChoice>
)

@Serializable
data class ChatChoice(
    val message: ChatMessage
)
```

### 3. Add to Koin Module

```kotlin
// di/AppModule.kt
val appModule = module {
    // Ktor Client
    single { KtorClient.client }
    
    // API Services
    single { OpenAIService(get(), apiKey = "your-api-key") }
    
    // Repositories
    single { AIRepository(get()) }
}
```

---

## 📚 Common Patterns

### GET Request
```kotlin
suspend fun fetchData(): Result<MyData> {
    return try {
        val response: MyData = client.get("https://api.example.com/data")
        Result.success(response)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

### POST Request with Body
```kotlin
suspend fun createItem(item: Item): Result<Item> {
    return try {
        val response: Item = client.post("https://api.example.com/items") {
            contentType(ContentType.Application.Json)
            setBody(item)
        }
        Result.success(response)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

### Streaming Response
```kotlin
suspend fun streamChat(prompt: String): Flow<String> = flow {
    client.preparePost("https://api.example.com/stream") {
        contentType(ContentType.Application.Json)
        setBody(StreamRequest(prompt))
    }.execute { response ->
        val channel = response.bodyAsChannel()
        while (!channel.isClosedForRead) {
            val chunk = channel.readUTF8Line() ?: break
            emit(chunk)
        }
    }
}
```

### With Headers
```kotlin
suspend fun authenticatedRequest(): Result<Data> {
    return try {
        val response: Data = client.get("https://api.example.com/protected") {
            header("Authorization", "Bearer $token")
            header("X-Custom-Header", "value")
        }
        Result.success(response)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

---

## 🔧 Configuration Options

### Retry Logic
```kotlin
install(HttpRequestRetry) {
    retryOnServerErrors(maxRetries = 3)
    exponentialDelay()
}
```

### Custom Timeout
```kotlin
install(HttpTimeout) {
    requestTimeoutMillis = 60_000
    connectTimeoutMillis = 15_000
}
```

### Response Validation
```kotlin
install(ResponseObserver) {
    onResponse { response ->
        println("Response status: ${response.status}")
    }
}

expectSuccess = true // Throw exception on non-2xx responses
```

### Custom JSON Configuration
```kotlin
install(ContentNegotiation) {
    json(Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    })
}
```

---

## 🎨 Sentio-Specific Examples

### 1. AI Chat Feature (Phase 3)

```kotlin
// domain/usecases/ai/AskSentioUseCase.kt
class AskSentioUseCase(
    private val openAIService: OpenAIService,
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(question: String): Result<String> {
        // Get relevant notes (RAG)
        val relevantNotes = noteRepository.searchNotes(question)
            .first()
            .take(5)
        
        // Build context
        val context = relevantNotes.joinToString("\n\n") { 
            "${it.title}\n${it.content}" 
        }
        
        // Create messages
        val messages = listOf(
            ChatMessage("system", "You are Sentio, a helpful AI assistant. Use the following notes as context: $context"),
            ChatMessage("user", question)
        )
        
        // Get AI response
        return openAIService.chat(messages)
    }
}
```

### 2. Generate Embeddings (Phase 3)

```kotlin
// domain/usecases/ai/GenerateEmbeddingUseCase.kt
class GenerateEmbeddingUseCase(
    private val openAIService: OpenAIService
) {
    suspend operator fun invoke(note: Note): Result<List<Float>> {
        val text = "${note.title}\n${note.content}"
        return openAIService.generateEmbedding(text)
    }
}
```

### 3. Link Preview (Phase 2)

```kotlin
// data/remote/LinkPreviewService.kt
class LinkPreviewService(private val client: HttpClient) {
    suspend fun fetchPreview(url: String): Result<LinkPreview> {
        return try {
            val html: String = client.get(url).bodyAsText()
            val preview = parseHtml(html) // Extract title, description, image
            Result.success(preview)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

---

## 🔒 Security Best Practices

### 1. Store API Keys Securely
```kotlin
// Don't hardcode API keys!
// ❌ Bad
val apiKey = "sk-1234567890"

// ✅ Good - Use environment variables or secure storage
val apiKey = System.getenv("OPENAI_API_KEY") 
    ?: throw IllegalStateException("API key not found")
```

### 2. Use HTTPS
```kotlin
defaultRequest {
    url {
        protocol = URLProtocol.HTTPS
    }
}
```

### 3. Validate Responses
```kotlin
suspend fun safeRequest(): Result<Data> {
    return try {
        val response = client.get("https://api.example.com/data")
        if (response.status.isSuccess()) {
            Result.success(response.body())
        } else {
            Result.failure(Exception("Request failed: ${response.status}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

---

## 🧪 Testing with Ktor

### Mock Client for Tests
```kotlin
val mockClient = HttpClient(MockEngine) {
    engine {
        addHandler { request ->
            when (request.url.encodedPath) {
                "/api/data" -> respond(
                    content = """{"id": 1, "name": "Test"}""",
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
                else -> error("Unhandled ${request.url.encodedPath}")
            }
        }
    }
}
```

---

## 📊 Error Handling

### Comprehensive Error Handling
```kotlin
suspend fun robustRequest(): Result<Data> {
    return try {
        val response = client.get("https://api.example.com/data")
        Result.success(response.body())
    } catch (e: ClientRequestException) {
        // 4xx errors
        Result.failure(Exception("Client error: ${e.response.status}"))
    } catch (e: ServerResponseException) {
        // 5xx errors
        Result.failure(Exception("Server error: ${e.response.status}"))
    } catch (e: IOException) {
        // Network errors
        Result.failure(Exception("Network error: ${e.message}"))
    } catch (e: Exception) {
        // Other errors
        Result.failure(e)
    }
}
```

---

## 🚀 Performance Tips

### 1. Reuse Client Instance
```kotlin
// ✅ Good - Single instance
object KtorClient {
    val client = HttpClient { /* config */ }
}

// ❌ Bad - New instance each time
fun makeRequest() {
    val client = HttpClient { /* config */ }
    // ...
}
```

### 2. Use Connection Pooling
```kotlin
install(HttpTimeout) {
    // Ktor handles connection pooling automatically
}
```

### 3. Cancel Requests
```kotlin
val job = scope.launch {
    client.get("https://api.example.com/slow")
}

// Cancel if needed
job.cancel()
```

---

## 📚 Resources

### Official Documentation
- [Ktor Client](https://ktor.io/docs/client.html)
- [Ktor Multiplatform](https://ktor.io/docs/http-client-multiplatform.html)
- [Content Negotiation](https://ktor.io/docs/serialization-client.html)

### Sentio Integration
- Phase 3: AI features (OpenAI, embeddings)
- Phase 4: External APIs (link previews, web scraping)
- Future: Real-time sync (WebSocket)

---

## ✅ Next Steps

### For Phase 3 (AI Integration)
1. Create `OpenAIService.kt`
2. Add API key management
3. Implement embedding generation
4. Build RAG pipeline
5. Create "Ask Sentio" feature

### For Phase 2 (Rich Content)
1. Create `LinkPreviewService.kt`
2. Fetch and parse HTML
3. Extract metadata
4. Display in notes

---

**Ktor is ready to power Sentio's network features! 🚀**

*Guide created: December 2, 2025*
