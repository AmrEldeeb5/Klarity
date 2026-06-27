package com.example.klarity.presentation.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klarity.domain.models.Note
import com.example.klarity.presentation.ChatMessage
import com.example.klarity.presentation.Conversation
import com.example.klarity.presentation.WorkspaceViewModel
import com.example.klarity.presentation.components.ActionProposalCard
import com.example.klarity.presentation.components.DbIcons
import com.example.klarity.presentation.components.MarkdownText
import com.example.klarity.presentation.components.MsIcon
import com.example.klarity.presentation.components.MsIconButton
import com.example.klarity.presentation.components.hoverBg
import com.example.klarity.presentation.theme.DevbookTheme
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * The Notion-style assistant side panel. It docks on the right of the shell (or slides in as a
 * drawer on phones) and shares the [WorkspaceViewModel]'s conversation state with the full-screen
 * Assistant — so a chat started here continues there and vice-versa.
 *
 * @param onExpand opens the full-screen Assistant for the active chat.
 * @param onOpenNote jumps to a source note referenced by an answer.
 */
@Composable
fun AssistantPanel(
    vm: WorkspaceViewModel,
    onClose: () -> Unit,
    onExpand: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenNote: (Note) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = DevbookTheme.colors
    val chat by vm.chat.collectAsState()
    val thinking by vm.thinking.collectAsState()
    val settings by vm.settings.collectAsState()
    val conversations by vm.conversations.collectAsState()
    val activeId by vm.activeConversationId.collectAsState()

    var input by remember { mutableStateOf("") }
    var historyOpen by remember { mutableStateOf(false) }
    val scroll = rememberScrollState()

    LaunchedEffect(chat.size, thinking) { scroll.animateScrollTo(scroll.maxValue) }

    val send: () -> Unit = {
        val q = input.trim()
        if (q.isNotEmpty()) {
            vm.ask(q)
            input = ""
        }
    }

    val activeTitle = conversations.firstOrNull { it.id == activeId }?.title ?: "New AI chat"

    Box(modifier = modifier.background(c.surf)) {
        Column(Modifier.fillMaxSize()) {
            // ── Header ───────────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().height(52.dp).padding(start = 8.dp, end = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier
                            .hoverBg(RoundedCornerShape(8.dp), c.sCont)
                            .clickable { historyOpen = !historyOpen }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Text(
                            activeTitle,
                            color = c.on,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        MsIcon(DbIcons.expandMore, 18.dp, c.onv)
                    }
                }
                PanelIconButton(DbIcons.edit, "New chat") { vm.newChat(); input = ""; historyOpen = false }
                PanelIconButton(DbIcons.openInFull, "Open full screen") { onExpand() }
                PanelIconButton(DbIcons.close, "Close Lou") { onClose() }
            }
            HorizontalDivider(color = c.outlinev)

            // ── Body ─────────────────────────────────────────────────────────────
            Box(Modifier.weight(1f).fillMaxWidth()) {
                if (chat.isEmpty() && !thinking) {
                    EmptyState(
                        onSummarize = { vm.summarizeWorkspace() },
                        onOpenTasks = { vm.summarizeOpenTasks() },
                        onPrefill = { input = it },
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize().verticalScroll(scroll)
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                    ) {
                        chat.forEach { msg ->
                            if (msg.fromUser) {
                                UserBubble(msg.text)
                            } else {
                                AssistantBubble(
                                    msg = msg,
                                    onOpenNote = onOpenNote,
                                    onApprove = { actionId -> vm.approveAction(msg.id, actionId) },
                                    onDecline = { actionId -> vm.declineAction(msg.id, actionId) },
                                )
                            }
                        }
                        if (thinking) TypingRow()
                    }
                }
            }

            // ── Composer ─────────────────────────────────────────────────────────
            Column(Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 12.dp)) {
                if (!settings.enabled) {
                    ConnectKeyChip(settings.provider.label, onOpenSettings)
                    Spacer(Modifier.height(8.dp))
                }
                Composer(
                    input = input,
                    onInput = { input = it },
                    onSend = send,
                    providerLabel = if (settings.enabled) settings.provider.short else "Local",
                    onOpenSettings = onOpenSettings,
                )
            }
        }

        // ── History dropdown overlay ─────────────────────────────────────────────
        if (historyOpen) {
            Box(
                Modifier.fillMaxSize()
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                        historyOpen = false
                    },
            )
            HistoryDropdown(
                conversations = conversations,
                activeId = activeId,
                onNewChat = { vm.newChat(); input = ""; historyOpen = false },
                onSelect = { vm.selectConversation(it); historyOpen = false },
                modifier = Modifier.padding(start = 10.dp, end = 10.dp, top = 50.dp),
            )
        }
    }
}

/** The collapsed launcher — a floating avatar button that opens the panel (Notion's bubble). */
@Composable
fun AssistantFab(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = DevbookTheme.colors
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.size(56.dp).shadow(10.dp, CircleShape),
        containerColor = c.p,
        contentColor = c.op,
        shape = CircleShape,
        // The 10dp shadow above already supplies the elevation look; avoid double-shadowing.
        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
    ) {
        MsIcon(DbIcons.autoAwesome, 26.dp, c.op, contentDescription = "Open Lou")
    }
}

// ════════════════════════════════════════════════════════════════════════════════
// EMPTY STATE
// ════════════════════════════════════════════════════════════════════════════════

@Composable
private fun EmptyState(
    onSummarize: () -> Unit,
    onOpenTasks: () -> Unit,
    onPrefill: (String) -> Unit,
) {
    val c = DevbookTheme.colors
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.Bottom,
    ) {
        AiAvatar(size = 40.dp, corner = 13.dp, iconSize = 24.dp)
        Spacer(Modifier.height(14.dp))
        Text("Hi, I'm Lou — how can I help?", color = c.on, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(14.dp))
        SuggestionRow(DbIcons.summarize, "Summarize my workspace", onSummarize)
        SuggestionRow(DbIcons.checklist, "What's still open?", onOpenTasks)
        SuggestionRow(DbIcons.editNote, "Draft a note") { onPrefill("Draft a note about ") }
        SuggestionRow(DbIcons.lightbulb, "Brainstorm ideas") { onPrefill("Brainstorm ideas for ") }
    }
}

@Composable
private fun SuggestionRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    val c = DevbookTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .hoverBg(RoundedCornerShape(10.dp), c.sLow)
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MsIcon(icon, 20.dp, c.p)
        Text(label, color = c.on, fontSize = 14.sp)
    }
}

// ════════════════════════════════════════════════════════════════════════════════
// MESSAGES
// ════════════════════════════════════════════════════════════════════════════════

@Composable
private fun UserBubble(text: String) {
    val c = DevbookTheme.colors
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 6.dp, bottomStart = 18.dp))
                .background(c.secc)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Text(text, color = c.onsecc, fontSize = 14.5.sp, lineHeight = 22.sp)
        }
    }
}

@Composable
private fun AssistantBubble(
    msg: ChatMessage,
    onOpenNote: (Note) -> Unit,
    onApprove: (String) -> Unit,
    onDecline: (String) -> Unit,
) {
    val c = DevbookTheme.colors
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        AiAvatar()
        Column(Modifier.weight(1f)) {
            MarkdownText(msg.text, color = c.on, fontSize = 14.5.sp, lineHeight = 23.sp)
            if (msg.actions.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                Spacer(Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Sources", color = c.onv, fontSize = 11.5.sp)
                    msg.sources.forEach { note ->
                        SourceChip(note.title.ifBlank { "Untitled note" }) { onOpenNote(note) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SourceChip(label: String, onClick: () -> Unit) {
    val c = DevbookTheme.colors
    AssistChip(
        onClick = onClick,
        label = { Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1) },
        leadingIcon = { MsIcon(DbIcons.description, 14.dp, c.opc) },
        border = null,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = c.pc,
            labelColor = c.opc,
            leadingIconContentColor = c.opc,
        ),
    )
}

@Composable
private fun TypingRow() {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        AiAvatar()
        TypingDots()
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TypingDots() {
    val c = DevbookTheme.colors
    // Expressive M3 loading indicator in place of the hand-rolled bouncing dots.
    LoadingIndicator(modifier = Modifier.padding(top = 6.dp).size(24.dp), color = c.p)
}

// ════════════════════════════════════════════════════════════════════════════════
// COMPOSER
// ════════════════════════════════════════════════════════════════════════════════

@Composable
private fun Composer(
    input: String,
    onInput: (String) -> Unit,
    onSend: () -> Unit,
    providerLabel: String,
    onOpenSettings: () -> Unit,
) {
    val c = DevbookTheme.colors
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = c.sLowest,
        border = BorderStroke(1.dp, c.outlinev),
    ) {
        Column(
            modifier = Modifier.padding(start = 14.dp, end = 8.dp, top = 12.dp, bottom = 8.dp),
        ) {
            // Keep the borderless multiline composer input; only the shell is M3 now.
            Box(Modifier.fillMaxWidth().padding(end = 6.dp)) {
                if (input.isEmpty()) {
                    Text("Ask anything about your workspace…", color = c.onv, fontSize = 14.5.sp)
                }
                BasicTextField(
                    value = input,
                    onValueChange = onInput,
                    textStyle = TextStyle(color = c.on, fontSize = 14.5.sp, lineHeight = 21.sp),
                    cursorBrush = SolidColor(c.p),
                    maxLines = 6,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { onSend() }),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                PanelIconButton(DbIcons.tune, "Lou settings", size = 32.dp, iconSize = 18.dp) { onOpenSettings() }
                Spacer(Modifier.weight(1f))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(15.dp))
                        .hoverBg(RoundedCornerShape(15.dp), c.sCont)
                        .clickable { onOpenSettings() }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    MsIcon(DbIcons.autoAwesome, 15.dp, c.p)
                    Text(providerLabel, color = c.onv, fontSize = 12.5.sp)
                }
                Spacer(Modifier.width(6.dp))
                SendButton(enabled = input.isNotBlank(), onClick = onSend)
            }
        }
    }
}

@Composable
private fun SendButton(enabled: Boolean, onClick: () -> Unit) {
    val c = DevbookTheme.colors
    FilledIconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(34.dp),
        shape = CircleShape,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = c.p,
            contentColor = c.op,
            disabledContainerColor = c.sHigh,
            disabledContentColor = c.onv,
        ),
    ) {
        MsIcon(DbIcons.arrowUpward, 19.dp, if (enabled) c.op else c.onv, contentDescription = "Send message")
    }
}

@Composable
private fun ConnectKeyChip(providerLabel: String, onOpenSettings: () -> Unit) {
    val c = DevbookTheme.colors
    Surface(
        onClick = onOpenSettings,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = c.pc,
        contentColor = c.opc,
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, top = 2.dp, end = 4.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MsIcon(DbIcons.vpnKey, 16.dp, c.opc)
            Text("Add a $providerLabel API key for full AI", color = c.opc, fontSize = 12.5.sp, modifier = Modifier.weight(1f))
            TextButton(onClick = onOpenSettings, colors = ButtonDefaults.textButtonColors(contentColor = c.opc)) {
                Text("Settings", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════════
// HISTORY DROPDOWN
// ════════════════════════════════════════════════════════════════════════════════

@Composable
private fun HistoryDropdown(
    conversations: List<Conversation>,
    activeId: String,
    onNewChat: () -> Unit,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = DevbookTheme.colors
    val groups = remember(conversations) { groupConversations(conversations, Clock.System.now()) }
    Surface(
        modifier = modifier.fillMaxWidth().shadow(14.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = c.sHigh,
        border = BorderStroke(1.dp, c.outlinev),
    ) {
        Column(modifier = Modifier.padding(vertical = 6.dp)) {
            DropdownRow(DbIcons.edit, "New AI chat", selected = false, onClick = onNewChat)
            if (groups.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp, horizontal = 12.dp), color = c.outlinev)
                Column(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp).verticalScroll(rememberScrollState()),
                ) {
                    groups.forEach { (label, items) ->
                        Text(
                            label.uppercase(),
                            color = c.onv,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 4.dp),
                        )
                        items.forEach { conv ->
                            DropdownRow(
                                icon = DbIcons.chat,
                                label = conv.title,
                                selected = conv.id == activeId,
                                onClick = { onSelect(conv.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DropdownRow(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    val c = DevbookTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 1.dp)
            .hoverBg(RoundedCornerShape(8.dp), c.sHighest, base = if (selected) c.sCont else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        MsIcon(icon, 16.dp, c.onv)
        Text(label, color = c.on, fontSize = 13.5.sp, maxLines = 1, modifier = Modifier.weight(1f))
    }
}

/** Buckets conversations into Notion-style date sections (all current-session chats land in Today). */
private fun groupConversations(items: List<Conversation>, now: Instant): List<Pair<String, List<Conversation>>> {
    val today = ArrayList<Conversation>()
    val week = ArrayList<Conversation>()
    val month = ArrayList<Conversation>()
    val older = ArrayList<Conversation>()
    items.forEach { conv ->
        val days = (now - conv.updatedAt).inWholeDays
        when {
            days < 1 -> today.add(conv)
            days < 7 -> week.add(conv)
            days < 30 -> month.add(conv)
            else -> older.add(conv)
        }
    }
    return buildList {
        if (today.isNotEmpty()) add("Today" to today)
        if (week.isNotEmpty()) add("Previous 7 days" to week)
        if (month.isNotEmpty()) add("Previous 30 days" to month)
        if (older.isNotEmpty()) add("Older" to older)
    }
}

// ════════════════════════════════════════════════════════════════════════════════
// SHARED PRIMITIVES
// ════════════════════════════════════════════════════════════════════════════════

@Composable
private fun AiAvatar(size: Dp = 30.dp, corner: Dp = 10.dp, iconSize: Dp = 18.dp) {
    val c = DevbookTheme.colors
    Box(
        modifier = Modifier.size(size).clip(RoundedCornerShape(corner)).background(c.p),
        contentAlignment = Alignment.Center,
    ) {
        MsIcon(DbIcons.autoAwesome, iconSize, c.op)
    }
}

@Composable
private fun PanelIconButton(
    icon: ImageVector,
    contentDescription: String?,
    size: Dp = 34.dp,
    iconSize: Dp = 19.dp,
    onClick: () -> Unit,
) {
    MsIconButton(
        icon = icon,
        onClick = onClick,
        contentDescription = contentDescription,
        tint = DevbookTheme.colors.onv,
        buttonSize = size,
        iconSize = iconSize,
    )
}
