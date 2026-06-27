package com.example.klarity.data.ai

/**
 * Trims the conversation sent to the model so long threads don't overflow smaller models' context
 * windows (many open/local models cap at ~8k tokens). Pure and deterministic for easy testing.
 */
object ChatHistory {

    /**
     * Keeps at most [maxMessages] of the most recent turns, and ensures the window begins with a
     * user turn — some providers (notably Anthropic) reject a request whose first message is from
     * the assistant. The latest user message is always retained since it's at the tail.
     */
    fun window(messages: List<AiTurn>, maxMessages: Int): List<AiTurn> {
        val tail = if (messages.size > maxMessages) messages.takeLast(maxMessages) else messages
        val firstUser = tail.indexOfFirst { it.role == "user" }
        // firstUser <= 0 means it already starts with a user turn (or none exists) — leave it as-is.
        return if (firstUser <= 0) tail else tail.drop(firstUser)
    }
}
