package com.example.klarity.data.ai

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChatHistoryTest {

    private fun user(text: String) = AiTurn("user", text)
    private fun bot(text: String) = AiTurn("assistant", text)

    @Test
    fun `short conversations pass through unchanged`() {
        val msgs = listOf(user("hi"), bot("hello"), user("how are you"))
        assertEquals(msgs, ChatHistory.window(msgs, maxMessages = 12))
    }

    @Test
    fun `long conversations keep only the most recent turns`() {
        val msgs = (1..20).map { if (it % 2 == 1) user("u$it") else bot("b$it") }
        val windowed = ChatHistory.window(msgs, maxMessages = 6)
        assertEquals(6, windowed.size)
        assertEquals("u15", windowed.first().content) // turns 15..20 retained
        assertEquals("b20", windowed.last().content)
    }

    @Test
    fun `window always begins with a user turn`() {
        // 20 alternating turns (odd=user, even=bot). takeLast(5) = [b16, u17, b18, u19, b20],
        // which starts on an assistant turn — that leading b16 must be dropped.
        val msgs = (1..20).map { if (it % 2 == 1) user("u$it") else bot("b$it") }
        val windowed = ChatHistory.window(msgs, maxMessages = 5)
        assertTrue(windowed.first().role == "user", "first message should be from the user")
        assertEquals("u17", windowed.first().content)
        assertEquals(4, windowed.size)
    }

    @Test
    fun `the latest user message is always retained`() {
        val msgs = (1..20).map { if (it % 2 == 1) user("u$it") else bot("b$it") }
        val windowed = ChatHistory.window(msgs, maxMessages = 6)
        assertEquals("u19", windowed.dropLast(1).last { it.role == "user" }.content)
    }
}
