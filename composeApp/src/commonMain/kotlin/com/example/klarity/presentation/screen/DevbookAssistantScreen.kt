package com.example.klarity.presentation.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.klarity.presentation.components.ActionProposalCard
import com.example.klarity.presentation.components.DbIcons
import com.example.klarity.presentation.components.MarkdownText
import com.example.klarity.presentation.components.MsIcon
import com.example.klarity.presentation.components.MsIconButton
import com.example.klarity.presentation.theme.DevbookTheme
import com.example.klarity.presentation.theme.LocalWindowMetrics

@Composable
fun DevbookAssistantScreen(vm: WorkspaceViewModel, navigate: (DevbookScreen) -> Unit, onOpenSettings: () -> Unit) {
    val c = DevbookTheme.colors
    val m = LocalWindowMetrics.current
    val chat by vm.chat.collectAsState()
    val thinking by vm.thinking.collectAsState()
    val settings by vm.settings.collectAsState()
    var input by remember { mutableStateOf("") }
    val scroll = rememberScrollState()

    LaunchedEffect(chat.size, thinking) { scroll.animateScrollTo(scroll.maxValue) }

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
            modifier = Modifier.align(Alignment.TopCenter).widthIn(max = 840.dp).fillMaxSize().padding(start = m.screenPaddingH, end = m.screenPaddingH, top = m.screenPaddingTop, bottom = 24.dp),
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
                        "Hi, I'm Lou — I can search your notes and tasks. Ask me anything about your workspace.",
                        color = c.on, fontSize = 15.sp, lineHeight = 26.sp, modifier = Modifier.padding(top = 5.dp),
                    )
                }
                chat.forEach { msg ->
                    if (msg.fromUser) {
                        UserMessage(msg.text)
                    } else {
                        AssistantMessage(
                            msg = msg,
                            openNote = openNote,
                            onApprove = { actionId -> vm.approveAction(msg.id, actionId) },
                            onDecline = { actionId -> vm.declineAction(msg.id, actionId) },
                        )
                    }
                }
                if (thinking) ThinkingRow()
            }

            // Prompt to connect a key when AI isn't configured yet.
            if (!settings.enabled) {
                ConnectKeyBanner(providerLabel = settings.provider.label, onOpenSettings = onOpenSettings)
                Spacer(Modifier.height(10.dp))
            }

            // Suggested prompts — scroll horizontally on narrow screens instead of clipping.
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            ) {
                SuggestChip(DbIcons.summarize, "Summarize my workspace") { vm.summarizeWorkspace() }
                SuggestChip(DbIcons.checklist, "What's open?") { vm.summarizeOpenTasks() }
            }
            Spacer(Modifier.height(12.dp))

            // Composer — keep the borderless multiline BasicTextField; only the shell is now M3.
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = c.sCont,
                border = BorderStroke(1.dp, c.outlinev),
            ) {
                Row(
                    modifier = Modifier.padding(start = 12.dp, top = 8.dp, end = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // Decorative (attach isn't wired yet) — a plain icon, not an actionable button,
                    // so screen readers don't announce a control that does nothing.
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
                    AssistChip(
                        onClick = onOpenSettings,
                        modifier = Modifier.padding(bottom = 5.dp),
                        shape = RoundedCornerShape(17.dp),
                        label = { Text(if (settings.enabled) settings.provider.short else "Local", fontSize = 12.5.sp) },
                        leadingIcon = { MsIcon(DbIcons.autoAwesome, 16.dp, c.p) },
                        border = null,
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = c.sHigh,
                            labelColor = c.onv,
                            leadingIconContentColor = c.p,
                        ),
                    )
                    FilledIconButton(
                        onClick = send,
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = c.p, contentColor = c.op),
                    ) {
                        MsIcon(DbIcons.arrowUpward, 24.dp, c.op, contentDescription = "Send message")
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                if (settings.enabled) "Powered by ${settings.provider.label} (${settings.model}), grounded in your notes & tasks."
                else "Answers use local search of your notes & tasks. Add an API key in Settings for full AI.",
                color = c.onv, fontSize = 11.5.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ThinkingRow() {
    val c = DevbookTheme.colors
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        AiAvatar()
        Row(
            modifier = Modifier.padding(top = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Thinking…", color = c.onv, fontSize = 15.sp)
            LoadingIndicator(modifier = Modifier.size(22.dp), color = c.p)
        }
    }
}

@Composable
private fun ConnectKeyBanner(providerLabel: String, onOpenSettings: () -> Unit) {
    val c = DevbookTheme.colors
    Surface(
        onClick = onOpenSettings,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = c.pc,
        contentColor = c.opc,
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, top = 4.dp, end = 6.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MsIcon(DbIcons.vpnKey, 18.dp, c.opc)
            Text(
                "Add a $providerLabel API key for full AI answers.",
                color = c.opc, fontSize = 13.sp, modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onOpenSettings, colors = ButtonDefaults.textButtonColors(contentColor = c.opc)) {
                Text("Open Settings", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun UserMessage(text: String) {
    val c = DevbookTheme.colors
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Surface(
            modifier = Modifier.widthIn(max = 520.dp),
            color = c.secc,
            contentColor = c.onsecc,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomEnd = 6.dp, bottomStart = 20.dp),
        ) {
            Text(text, color = c.onsecc, fontSize = 15.sp, lineHeight = 24.sp, modifier = Modifier.padding(horizontal = 18.dp, vertical = 13.dp))
        }
    }
}

@Composable
private fun AssistantMessage(
    msg: ChatMessage,
    openNote: (Note) -> Unit,
    onApprove: (String) -> Unit,
    onDecline: (String) -> Unit,
) {
    val c = DevbookTheme.colors
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        AiAvatar()
        Column {
            MarkdownText(msg.text, color = c.on, fontSize = 15.sp, lineHeight = 26.sp)
            if (msg.actions.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Column(modifier = Modifier.widthIn(max = 460.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    msg.actions.forEach { action ->
                        ActionProposalCard(
                            action = action,
                            onApprove = { onApprove(action.id) },
                            onDecline = { onDecline(action.id) },
                        )
                    }
                }
            }
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
    AssistChip(
        onClick = onClick,
        label = { Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
        leadingIcon = { MsIcon(DbIcons.description, 15.dp, c.opc) },
        border = null,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = c.pc,
            labelColor = c.opc,
            leadingIconContentColor = c.opc,
        ),
    )
}

@Composable
private fun SuggestChip(icon: ImageVector, label: String, onClick: () -> Unit) {
    val c = DevbookTheme.colors
    AssistChip(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        label = { Text(label, fontSize = 13.sp) },
        leadingIcon = { MsIcon(icon, 17.dp, c.p) },
        border = BorderStroke(1.dp, c.outline),
        colors = AssistChipDefaults.assistChipColors(
            containerColor = c.sLow,
            labelColor = c.p,
            leadingIconContentColor = c.p,
        ),
    )
}
