package com.example.klarity.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klarity.presentation.theme.DevbookTheme
import com.example.klarity.presentation.theme.MonoFamily

/**
 * A small, dependency-free Markdown renderer for the assistant's replies — so AI answers read like
 * any modern chat (headings, bold/italic, inline code, fenced code blocks, bullet/numbered lists,
 * block quotes and links) instead of one flat paragraph.
 *
 * It deliberately covers only the subset an LLM actually emits; anything it doesn't recognise is
 * rendered verbatim, so unusual input degrades to plain text rather than disappearing.
 */
@Composable
fun MarkdownText(
    markdown: String,
    color: Color,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 15.sp,
    lineHeight: TextUnit = 24.sp,
) {
    val c = DevbookTheme.colors
    val blocks = remember(markdown) { parseMarkdownBlocks(markdown) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        blocks.forEach { block ->
            when (block) {
                is MdBlock.Heading -> {
                    val (hSize, hLine, weight) = when (block.level) {
                        1 -> Triple(21.sp, 28.sp, FontWeight.Bold)
                        2 -> Triple(18.sp, 25.sp, FontWeight.SemiBold)
                        else -> Triple(16.sp, 22.sp, FontWeight.SemiBold)
                    }
                    Text(
                        buildAnnotatedString { appendInline(block.text, c.p, c.sHigh) },
                        color = color, fontSize = hSize, lineHeight = hLine, fontWeight = weight,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }

                is MdBlock.Paragraph -> Text(
                    buildAnnotatedString { appendInline(block.text, c.p, c.sHigh) },
                    color = color, fontSize = fontSize, lineHeight = lineHeight,
                )

                is MdBlock.Bullet -> MdListRow(marker = "•", indent = block.indent, markerColor = c.p) {
                    Text(
                        buildAnnotatedString { appendInline(block.text, c.p, c.sHigh) },
                        color = color, fontSize = fontSize, lineHeight = lineHeight,
                    )
                }

                is MdBlock.Numbered -> MdListRow(marker = "${block.number}.", indent = block.indent, markerColor = c.onv) {
                    Text(
                        buildAnnotatedString { appendInline(block.text, c.p, c.sHigh) },
                        color = color, fontSize = fontSize, lineHeight = lineHeight,
                    )
                }

                is MdBlock.Quote -> Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                    Box(Modifier.width(3.dp).fillMaxHeight().clip(RoundedCornerShape(2.dp)).background(c.outline))
                    Box(Modifier.padding(start = 12.dp)) {
                        Text(
                            buildAnnotatedString { appendInline(block.text, c.p, c.sHigh) },
                            color = c.onv, fontSize = fontSize, lineHeight = lineHeight,
                        )
                    }
                }

                is MdBlock.Code -> CodeBlock(block.text, block.lang)
                MdBlock.Rule -> HorizontalDivider(color = c.outlinev)
            }
        }
    }
}

@Composable
private fun MdListRow(marker: String, indent: Int, markerColor: Color, content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = (indent.coerceIn(0, 4) * 16).dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(marker, color = markerColor, fontSize = 15.sp, lineHeight = 24.sp, modifier = Modifier.widthIn(min = 16.dp))
        Box(Modifier.weight(1f)) { content() }
    }
}

@Composable
private fun CodeBlock(code: String, lang: String?) {
    val c = DevbookTheme.colors
    val clipboard = LocalClipboardManager.current
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = c.sLow),
        border = BorderStroke(1.dp, c.outlinev),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(lang ?: "code", color = c.onv, fontSize = 11.sp, modifier = Modifier.weight(1f))
            TextButton(
                onClick = { clipboard.setText(AnnotatedString(code)) },
                colors = ButtonDefaults.textButtonColors(contentColor = c.onv),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            ) {
                MsIcon(DbIcons.contentCopy, 14.dp, c.onv)
                Spacer(Modifier.width(4.dp))
                Text("Copy", fontSize = 11.sp)
            }
        }
        HorizontalDivider(color = c.outlinev)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Text(code, color = c.on, fontFamily = MonoFamily, fontSize = 13.sp, lineHeight = 19.sp)
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════════
// BLOCK PARSING
// ════════════════════════════════════════════════════════════════════════════════

private sealed interface MdBlock {
    data class Heading(val level: Int, val text: String) : MdBlock
    data class Paragraph(val text: String) : MdBlock
    data class Bullet(val text: String, val indent: Int) : MdBlock
    data class Numbered(val number: Int, val text: String, val indent: Int) : MdBlock
    data class Quote(val text: String) : MdBlock
    data class Code(val text: String, val lang: String?) : MdBlock
    data object Rule : MdBlock
}

private val OrderedRegex = Regex("""^(\d{1,3})[.)]\s+(.*)$""")

private fun parseMarkdownBlocks(src: String): List<MdBlock> {
    val out = ArrayList<MdBlock>()
    val lines = src.replace("\r\n", "\n").replace("\r", "\n").split("\n")
    val para = StringBuilder()

    fun flush() {
        if (para.isNotBlank()) out.add(MdBlock.Paragraph(para.toString().trim()))
        para.setLength(0)
    }

    var i = 0
    while (i < lines.size) {
        val line = lines[i].trimEnd()
        val trimmed = line.trimStart()
        val indent = (line.length - trimmed.length) / 2

        when {
            trimmed.startsWith("```") -> {
                flush()
                val lang = trimmed.removePrefix("```").trim().ifBlank { null }
                val sb = StringBuilder()
                i++
                while (i < lines.size && !lines[i].trimStart().startsWith("```")) {
                    sb.append(lines[i]).append('\n')
                    i++
                }
                out.add(MdBlock.Code(sb.toString().trimEnd('\n'), lang))
            }

            trimmed.isEmpty() -> flush()

            isHorizontalRule(trimmed) -> {
                flush()
                out.add(MdBlock.Rule)
            }

            isHeading(trimmed) -> {
                flush()
                val level = trimmed.takeWhile { it == '#' }.length
                out.add(MdBlock.Heading(level.coerceIn(1, 3), trimmed.drop(level).trim()))
            }

            trimmed == ">" || trimmed.startsWith("> ") -> {
                flush()
                out.add(MdBlock.Quote(trimmed.removePrefix(">").trim()))
            }

            isBullet(trimmed) -> {
                flush()
                out.add(MdBlock.Bullet(trimmed.drop(2).trim(), indent))
            }

            OrderedRegex.matches(trimmed) -> {
                flush()
                val m = OrderedRegex.find(trimmed)!!
                out.add(MdBlock.Numbered(m.groupValues[1].toIntOrNull() ?: 1, m.groupValues[2].trim(), indent))
            }

            else -> {
                if (para.isNotEmpty()) para.append(' ')
                para.append(trimmed)
            }
        }
        i++
    }
    flush()
    return out
}

private fun isHeading(t: String): Boolean {
    val hashes = t.takeWhile { it == '#' }.length
    return hashes in 1..6 && t.length > hashes && t[hashes] == ' '
}

private fun isBullet(t: String): Boolean =
    t.startsWith("- ") || t.startsWith("* ") || t.startsWith("+ ")

private fun isHorizontalRule(t: String): Boolean {
    val s = t.replace(" ", "")
    return s.length >= 3 && (s.all { it == '-' } || s.all { it == '*' } || s.all { it == '_' })
}

// ════════════════════════════════════════════════════════════════════════════════
// INLINE PARSING  →  AnnotatedString
// ════════════════════════════════════════════════════════════════════════════════

/**
 * Appends [text] to the builder, translating inline Markdown: `**bold**`, `*italic*`, `***both***`,
 * `_underscored_`, `` `code` ``, `~~strike~~` and `[label](url)` links. Underscore emphasis is only
 * honoured at word boundaries so identifiers like `snake_case` survive untouched.
 */
private fun AnnotatedString.Builder.appendInline(text: String, linkColor: Color, codeBg: Color) {
    var i = 0
    val n = text.length
    while (i < n) {
        val ch = text[i]

        // Escaped character — emit the next char literally.
        if (ch == '\\' && i + 1 < n) {
            append(text[i + 1]); i += 2; continue
        }

        // Inline code `code`
        if (ch == '`') {
            val end = text.indexOf('`', i + 1)
            if (end > i) {
                withStyle(SpanStyle(fontFamily = MonoFamily, background = codeBg)) {
                    append(' ' + text.substring(i + 1, end) + ' ')
                }
                i = end + 1; continue
            }
        }

        // Bold + italic ***text***
        if (text.startsWith("***", i)) {
            val end = text.indexOf("***", i + 3)
            if (end > i + 2) {
                withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Italic)) {
                    appendInline(text.substring(i + 3, end), linkColor, codeBg)
                }
                i = end + 3; continue
            }
        }

        // Bold **text** / __text__
        if (text.startsWith("**", i) || (text.startsWith("__", i) && isBoundary(text, i - 1))) {
            val delim = text.substring(i, i + 2)
            val end = text.indexOf(delim, i + 2)
            if (end > i + 1) {
                withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                    appendInline(text.substring(i + 2, end), linkColor, codeBg)
                }
                i = end + 2; continue
            }
        }

        // Strikethrough ~~text~~
        if (text.startsWith("~~", i)) {
            val end = text.indexOf("~~", i + 2)
            if (end > i + 1) {
                withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                    appendInline(text.substring(i + 2, end), linkColor, codeBg)
                }
                i = end + 2; continue
            }
        }

        // Italic *text* / _text_
        if (ch == '*' || (ch == '_' && isBoundary(text, i - 1))) {
            val end = text.indexOf(ch, i + 1)
            if (end > i + 1 && text[i + 1] != ' ' && text[end - 1] != ' ') {
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    appendInline(text.substring(i + 1, end), linkColor, codeBg)
                }
                i = end + 1; continue
            }
        }

        // Link [label](url)
        if (ch == '[') {
            val close = text.indexOf(']', i + 1)
            if (close > i && close + 1 < n && text[close + 1] == '(') {
                val urlEnd = text.indexOf(')', close + 2)
                if (urlEnd > close) {
                    withStyle(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)) {
                        appendInline(text.substring(i + 1, close), linkColor, codeBg)
                    }
                    i = urlEnd + 1; continue
                }
            }
        }

        append(ch)
        i++
    }
}

/** True when the char at [idx] is a word boundary (start-of-string or a non-alphanumeric char). */
private fun isBoundary(text: String, idx: Int): Boolean = idx < 0 || !text[idx].isLetterOrDigit()
