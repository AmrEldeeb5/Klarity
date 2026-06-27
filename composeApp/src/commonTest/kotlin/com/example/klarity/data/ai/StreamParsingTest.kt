package com.example.klarity.data.ai

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class StreamParsingTest {

    // ── OpenAI ────────────────────────────────────────────────────────────────
    @Test
    fun `openAiDelta extracts the content token`() {
        val data = """{"id":"x","choices":[{"index":0,"delta":{"content":"Hello"},"finish_reason":null}]}"""
        assertEquals("Hello", StreamParsing.openAiDelta(data))
    }

    @Test
    fun `openAiDelta returns null for the DONE sentinel`() {
        assertNull(StreamParsing.openAiDelta("[DONE]"))
    }

    @Test
    fun `openAiDelta returns null for a role-only or empty delta`() {
        assertNull(StreamParsing.openAiDelta("""{"choices":[{"delta":{"role":"assistant"}}]}"""))
        assertNull(StreamParsing.openAiDelta("""{"choices":[{"delta":{"content":""}}]}"""))
    }

    @Test
    fun `openAiDelta returns null for malformed json`() {
        assertNull(StreamParsing.openAiDelta("not json"))
    }

    // ── Anthropic ───────────────────────────────────────────────────────────────
    @Test
    fun `anthropicDelta extracts text from a content_block_delta`() {
        val data = """{"type":"content_block_delta","index":0,"delta":{"type":"text_delta","text":"Hi"}}"""
        assertEquals("Hi", StreamParsing.anthropicDelta(data))
    }

    @Test
    fun `anthropicDelta ignores non-text events`() {
        assertNull(StreamParsing.anthropicDelta("""{"type":"message_start","message":{"id":"x"}}"""))
        assertNull(StreamParsing.anthropicDelta("""{"type":"ping"}"""))
        // A message_delta carries a stop_reason, not text.
        assertNull(StreamParsing.anthropicDelta("""{"type":"message_delta","delta":{"stop_reason":"end_turn"}}"""))
    }

    @Test
    fun `anthropicDelta returns null for malformed json`() {
        assertNull(StreamParsing.anthropicDelta("{bad"))
    }
}
