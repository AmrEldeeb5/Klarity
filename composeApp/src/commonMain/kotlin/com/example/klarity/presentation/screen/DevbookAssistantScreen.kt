package com.example.klarity.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klarity.domain.models.Note
import com.example.klarity.presentation.ChatMessage
import com.example.klarity.presentation.DevbookScreen
import com.example.klarity.presentation.WorkspaceViewModel
import com.example.klarity.presentation.components.DbIcons
import com.example.klarity.presentation.components.MsIcon
import com.example.klarity.presentation.theme.DevbookTheme

@Composable
fun DevbookAssistantScreen(vm: WorkspaceViewModel, navigate: (DevbookScreen) -> Unit) {
    val c = DevbookTheme.colors
    val chat by vm.chat.collectAsState()
    var input by remember { mutableStateOf("") }
    val scroll = rememberScrollState()

    LaunchedEffect(chat.size) { scroll.animateScrollTo(scroll.maxValue) }

    val openNote: (Note) -> Unit = { vm.selectNote(it.id); navigate(DevbookScreen.NOTEBOOK) }
    val send: () -> Unit = {
        val q = input.trim()
        if (q.isNotEmpty()) {
            vm.ask(q)
            input = ""
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
        Column(
            modifier = Modifier.align(Alignment.TopCenter).widthIn(max = 840.dp).fillMaxSize().padding(start = 40.dp, end = 40.dp, top = 32.dp, bottom = 24.dp),
        ) {
            // Thread
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(scroll).padding(bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp),
            ) {
                // Static intro is always shown first.
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    AiAvatar()
                    Text(
                        "Hi — I can search your notes and tasks. Ask me anything about your workspace.",
                        color = c.on, fontSize = 15.sp, lineHeight = 26.sp, modifier = Modifier.padding(top = 5.dp),
                    )
                }
                chat.forEach { msg ->
                    if (msg.fromUser) UserMessage(msg.text) else AssistantMessage(msg, openNote)
                }
            }

            // Suggested prompts
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                SuggestChip(DbIcons.summarize, "Summarize my workspace") { vm.summarizeWorkspace() }
                SuggestChip(DbIcons.checklist, "What's open?") { vm.summarizeOpenTasks() }
            }
            Spacer(Modifier.height(12.dp))

            // Composer
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(c.sCont).border(1.dp, c.outlinev, RoundedCornerShape(28.dp)).padding(start = 18.dp, top = 8.dp, end = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                MsIcon(DbIcons.attachFile, 24.dp, c.onv, modifier = Modifier.padding(bottom = 8.dp))
                Box(modifier = Modifier.weight(1f).padding(vertical = 11.dp), contentAlignment = Alignment.CenterStart) {
                    if (input.isEmpty()) Text("Ask about your workspace…", color = c.onv, fontSize = 15.sp)
                    BasicTextField(
                        value = input,
                        onValueChange = { input = it },
                        textStyle = TextStyle(color = c.on, fontSize = 15.sp),
                        cursorBrush = SolidColor(c.p),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { send() }),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Box(
                    modifier = Modifier.padding(bottom = 5.dp).height(34.dp).clip(RoundedCornerShape(17.dp)).background(c.sHigh).padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        MsIcon(DbIcons.autoAwesome, 16.dp, c.p)
                        Text("Local", color = c.onv, fontSize = 12.5.sp)
                    }
                }
                Box(
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(c.p).clickable { send() },
                    contentAlignment = Alignment.Center,
                ) {
                    MsIcon(DbIcons.arrowUpward, 24.dp, c.op)
                }
            }
            Spacer(Modifier.height(10.dp))
            Text("Answers are grounded in your own notes & tasks — local search, no external API.", color = c.onv, fontSize = 11.5.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun UserMessage(text: String) {
    val c = DevbookTheme.colors
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Box(
            modifier = Modifier.widthIn(max = 520.dp).clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomEnd = 6.dp, bottomStart = 20.dp)).background(c.secc).padding(horizontal = 18.dp, vertical = 13.dp),
        ) {
            Text(text, color = c.onsecc, fontSize = 15.sp, lineHeight = 24.sp)
        }
    }
}

@Composable
private fun AssistantMessage(msg: ChatMessage, openNote: (Note) -> Unit) {
    val c = DevbookTheme.colors
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        AiAvatar()
        Column {
            Text(msg.text, color = c.on, fontSize = 15.sp, lineHeight = 26.sp)
            if (msg.sources.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Sources", color = c.onv, fontSize = 12.sp)
                    msg.sources.forEach { note ->
                        SourceChip(note.title.ifBlank { "Untitled note" }) { openNote(note) }
                    }
                }
            }
        }
    }
}

@Composable
private fun AiAvatar() {
    val c = DevbookTheme.colors
    Box(modifier = Modifier.size(34.dp).clip(RoundedCornerShape(11.dp)).background(c.p), contentAlignment = Alignment.Center) {
        MsIcon(DbIcons.autoAwesome, 20.dp, c.op)
    }
}

@Composable
private fun SourceChip(label: String, onClick: () -> Unit) {
    val c = DevbookTheme.colors
    Row(
        modifier = Modifier.height(28.dp).clip(RoundedCornerShape(8.dp)).background(c.pc).clickable { onClick() }.padding(horizontal = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        MsIcon(DbIcons.description, 15.dp, c.opc)
        Text(label, color = c.opc, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SuggestChip(icon: ImageVector, label: String, onClick: () -> Unit) {
    val c = DevbookTheme.colors
    Row(
        modifier = Modifier.height(34.dp).clip(RoundedCornerShape(18.dp)).background(c.sLow).border(1.dp, c.outline, RoundedCornerShape(18.dp)).clickable { onClick() }.padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        MsIcon(icon, 17.dp, c.p)
        Text(label, color = c.p, fontSize = 13.sp)
    }
}
