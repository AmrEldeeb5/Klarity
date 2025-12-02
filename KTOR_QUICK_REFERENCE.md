# ⚡ Ktor Quick Reference

## Setup (Already Done ✅)

```toml
ktor = "3.0.0"
ktor-client-core
ktor-client-content-negotiation
ktor-serialization-kotlinx-json
ktor-client-logging
ktor-client-cio (Desktop)
```

---

## Basic Client

```kotlin
val client = HttpClient {
    install(ContentNegotiation) {
        json()
    }
    install(Logging) {
        level = LogLevel.INFO
    }
}
```

---

## Common Requests

### GET
```kotlin
val data: MyData = client.get("https://api.example.com/data")
```

### POST
```kotlin
val result: Response = client.post("https://api.example.com/items") {
    contentType(ContentType.Application.Json)
    setBody(myItem)
}
```

### PUT
```kotlin
client.put("https://api.example.com/items/1") {
    setBody(updatedItem)
}
```

### DELETE
```kotlin
client.delete("https://api.example.com/items/1")
```

---

## With Headers

```kotlin
client.get("https://api.example.com/protected") {
    header("Authorization", "Bearer $token")
    header("X-Custom", "value")
}
```

---

## Error Handling

```kotlin
suspend fun safeRequest(): Result<Data> {
    return try {
        val data: Data = client.get("url")
        Result.success(data)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

---

## Streaming

```kotlin
suspend fun stream(): Flow<String> = flow {
    client.preparePost("url") {
        setBody(request)
    }.execute { response ->
        val channel = response.bodyAsChannel()
        while (!channel.isClosedForRead) {
            val line = channel.readUTF8Line() ?: break
            emit(line)
        }
    }
}
```

---

## Koin Integration

```kotlin
// In AppModule
single { 
    HttpClient {
        install(ContentNegotiation) { json() }
    }
}

single { MyApiService(get()) }
```

---

## OpenAI Example

```kotlin
class OpenAIService(private val client: HttpClient) {
    suspend fun chat(prompt: String): String {
        val response = client.post("https://api.openai.com/v1/chat/completions") {
            header("Authorization", "Bearer $apiKey")
            contentType(ContentType.Application.Json)
            setBody(ChatRequest(
                model = "gpt-4",
                messages = listOf(
                    Message("user", prompt)
                )
            ))
        }
        return response.body<ChatResponse>().choices.first().message.content
    }
}
```

---

## Timeout

```kotlin
install(HttpTimeout) {
    requestTimeoutMillis = 30_000
    connectTimeoutMillis = 10_000
}
```

---

## Retry

```kotlin
install(HttpRequestRetry) {
    retryOnServerErrors(maxRetries = 3)
    exponentialDelay()
}
```

---

## Response as Text

```kotlin
val html: String = client.get("https://example.com").bodyAsText()
```

---

## Query Parameters

```kotlin
client.get("https://api.example.com/search") {
    parameter("q", "kotlin")
    parameter("limit", 10)
}
```

---

## Form Data

```kotlin
client.post("https://api.example.com/form") {
    setBody(FormDataContent(Parameters.build {
        append("key", "value")
    }))
}
```

---

## File Upload

```kotlin
client.post("https://api.example.com/upload") {
    setBody(MultiPartFormDataContent(
        formData {
            append("file", file.readBytes(), Headers.build {
                append(HttpHeaders.ContentType, "image/png")
                append(HttpHeaders.ContentDisposition, "filename=image.png")
            })
        }
    ))
}
```

---

## WebSocket (Future)

```kotlin
client.webSocket("wss://api.example.com/ws") {
    send("Hello")
    for (frame in incoming) {
        when (frame) {
            is Frame.Text -> println(frame.readText())
        }
    }
}
```

---

**Quick and easy Ktor reference! 🚀**
