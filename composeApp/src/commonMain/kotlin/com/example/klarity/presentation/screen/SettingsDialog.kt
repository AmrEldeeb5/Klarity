package com.example.klarity.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.klarity.data.ai.AiException
import com.example.klarity.domain.repositories.AiProvider
import com.example.klarity.presentation.WorkspaceViewModel
import com.example.klarity.presentation.components.DbIcons
import com.example.klarity.presentation.components.MsIcon
import com.example.klarity.presentation.theme.DevbookTheme
import kotlinx.coroutines.launch

/** Settings dialog — configures the AI assistant (provider, API key, model, base URL). */
@Composable
fun SettingsDialog(vm: WorkspaceViewModel, onDismiss: () -> Unit) {
    val c = DevbookTheme.colors
    val settings by vm.settings.collectAsState()

    var provider by remember(settings.provider) { mutableStateOf(settings.provider) }
    var key by remember(settings.apiKey) { mutableStateOf(settings.apiKey ?: "") }
    var model by remember(settings.provider, settings.model) { mutableStateOf(settings.model) }
    var baseUrl by remember(settings.provider, settings.baseUrl) { mutableStateOf(settings.baseUrl) }
    var reveal by remember { mutableStateOf(false) }
    var providerMenu by remember { mutableStateOf(false) }

    // Model auto-discovery (fetched from the provider's /models endpoint).
    val scope = rememberCoroutineScope()
    var fetchedModels by remember(settings.provider) { mutableStateOf<List<String>>(emptyList()) }
    var loadingModels by remember { mutableStateOf(false) }
    var modelsError by remember { mutableStateOf<String?>(null) }
    var modelMenu by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(c.sLowest)
                .border(1.dp, c.outlinev, RoundedCornerShape(24.dp))
                .padding(24.dp),
        ) {
            Text("Settings", color = c.on, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(18.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MsIcon(DbIcons.autoAwesome, 18.dp, c.p)
                Text("AI assistant", color = c.on, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Connect any AI provider for full answers grounded in your notes & tasks. " +
                    "Without a key, the assistant uses local search only.",
                color = c.onv, fontSize = 12.5.sp, lineHeight = 18.sp,
            )
            Spacer(Modifier.height(16.dp))

            // Provider
            FieldLabel("Provider")
            Box {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(c.sLow)
                        .border(1.dp, c.outlinev, RoundedCornerShape(12.dp))
                        .clickable { providerMenu = true }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(provider.label, color = c.on, fontSize = 14.sp, modifier = Modifier.weight(1f))
                    MsIcon(DbIcons.unfoldMore, 18.dp, c.onv)
                }
                DropdownMenu(expanded = providerMenu, onDismissRequest = { providerMenu = false }) {
                    AiProvider.entries.forEach { p ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    p.label,
                                    color = if (p == provider) c.p else c.on,
                                    fontWeight = if (p == provider) FontWeight.SemiBold else FontWeight.Normal,
                                )
                            },
                            onClick = {
                                // Switching providers resets model/base URL to that provider's defaults.
                                if (p != provider) {
                                    provider = p
                                    model = p.defaultModel
                                    baseUrl = p.defaultBaseUrl
                                    fetchedModels = emptyList()
                                    modelsError = null
                                }
                                providerMenu = false
                            },
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            // API key
            FieldLabel("API key")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(c.sLow)
                    .border(1.dp, c.outlinev, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MsIcon(DbIcons.vpnKey, 18.dp, c.onv)
                Box(modifier = Modifier.weight(1f).padding(vertical = 10.dp), contentAlignment = Alignment.CenterStart) {
                    if (key.isEmpty()) Text(provider.keyHint, color = c.outline, fontSize = 14.sp)
                    BasicTextField(
                        value = key,
                        onValueChange = { key = it },
                        singleLine = true,
                        textStyle = TextStyle(color = c.on, fontSize = 14.sp),
                        cursorBrush = SolidColor(c.p),
                        visualTransformation = if (reveal) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Box(
                    modifier = Modifier.size(30.dp).clip(RoundedCornerShape(8.dp)).clickable { reveal = !reveal },
                    contentAlignment = Alignment.Center,
                ) {
                    MsIcon(if (reveal) DbIcons.visibilityOff else DbIcons.visibility, 18.dp, c.onv)
                }
            }
            Spacer(Modifier.height(16.dp))

            // Base URL (only meaningful for OpenAI-compatible)
            if (provider == AiProvider.OPENAI) {
                FieldLabel("Base URL")
                PlainField(value = baseUrl, onChange = { baseUrl = it }, placeholder = provider.defaultBaseUrl)
                Spacer(Modifier.height(4.dp))
                Text(provider.baseUrlHint, color = c.onv, fontSize = 11.sp, lineHeight = 15.sp)
                Spacer(Modifier.height(16.dp))
            }

            // Model
            FieldLabel("Model")
            PlainField(value = model, onChange = { model = it }, placeholder = provider.defaultModel)
            Spacer(Modifier.height(8.dp))
            Box {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9.dp))
                            .background(c.sCont)
                            .clickable(enabled = !loadingModels) {
                                scope.launch {
                                    loadingModels = true
                                    modelsError = null
                                    try {
                                        fetchedModels = vm.listModels(provider, key.ifBlank { null }, baseUrl)
                                        if (fetchedModels.isEmpty()) modelsError = "No models returned."
                                        else {
                                            modelMenu = true
                                            if (model.isBlank()) model = fetchedModels.first()
                                        }
                                    } catch (e: AiException) {
                                        modelsError = e.message
                                    } catch (e: Exception) {
                                        modelsError = "Couldn't load models."
                                    } finally {
                                        loadingModels = false
                                    }
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        if (loadingModels) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = c.p)
                        } else {
                            MsIcon(DbIcons.refresh, 16.dp, c.onv)
                        }
                        Text(if (loadingModels) "Loading…" else "Load models", color = c.onv, fontSize = 12.5.sp)
                    }
                    if (fetchedModels.isNotEmpty()) {
                        Text(
                            "${fetchedModels.size} available",
                            color = c.p,
                            fontSize = 12.sp,
                            modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { modelMenu = true }.padding(horizontal = 6.dp, vertical = 4.dp),
                        )
                    }
                }
                DropdownMenu(expanded = modelMenu, onDismissRequest = { modelMenu = false }) {
                    fetchedModels.forEach { m ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    m,
                                    color = if (m == model) c.p else c.on,
                                    fontWeight = if (m == model) FontWeight.SemiBold else FontWeight.Normal,
                                )
                            },
                            onClick = { model = m; modelMenu = false },
                        )
                    }
                }
            }
            modelsError?.let {
                Spacer(Modifier.height(6.dp))
                Text(it, color = c.err, fontSize = 11.5.sp, lineHeight = 16.sp)
            }
            // Quick examples (handy before you've loaded the live list).
            if (fetchedModels.isEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    provider.exampleModels.forEach { m ->
                        SuggestionChip(label = m, selected = m == model) { model = m }
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                "Your key is stored locally on this device and sent only to the provider you choose.",
                color = c.onv, fontSize = 11.5.sp, lineHeight = 16.sp,
            )
            Spacer(Modifier.height(22.dp))

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                if (settings.apiKey != null) {
                    Text(
                        "Remove key",
                        color = c.err,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { vm.saveAiSettings(provider, null, model, baseUrl); key = "" }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    "Cancel",
                    color = c.onv,
                    fontSize = 14.sp,
                    modifier = Modifier.clip(RoundedCornerShape(20.dp)).clickable { onDismiss() }.padding(horizontal = 16.dp, vertical = 9.dp),
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    "Save",
                    color = c.op,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(c.p)
                        .clickable {
                            vm.saveAiSettings(provider, key.ifBlank { null }, model, baseUrl)
                            onDismiss()
                        }
                        .padding(horizontal = 18.dp, vertical = 9.dp),
                )
            }
        }
    }
}

@Composable
private fun PlainField(value: String, onChange: (String) -> Unit, placeholder: String) {
    val c = DevbookTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(c.sLow)
            .border(1.dp, c.outlinev, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        if (value.isEmpty()) Text(placeholder, color = c.outline, fontSize = 14.sp)
        BasicTextField(
            value = value,
            onValueChange = onChange,
            singleLine = true,
            textStyle = TextStyle(color = c.on, fontSize = 14.sp),
            cursorBrush = SolidColor(c.p),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SuggestionChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val c = DevbookTheme.colors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(9.dp))
            .background(if (selected) c.secc else c.sCont)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(
            label,
            color = if (selected) c.onsecc else c.onv,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
        )
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text,
        color = DevbookTheme.colors.onv,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(bottom = 6.dp),
    )
}
