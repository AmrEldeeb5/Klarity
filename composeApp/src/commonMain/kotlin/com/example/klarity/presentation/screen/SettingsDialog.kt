package com.example.klarity.presentation.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.klarity.presentation.components.MsIconButton
import com.example.klarity.presentation.theme.DevbookTheme
import kotlinx.coroutines.launch

/** Settings dialog — configures the AI assistant (provider, API key, model, base URL). */
@OptIn(ExperimentalMaterial3Api::class)
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
        Surface(
            modifier = Modifier.imePadding().widthIn(max = 480.dp).fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = c.sLowest,
            border = BorderStroke(1.dp, c.outlinev),
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
            ) {
                Text("Settings", color = c.on, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(18.dp))

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MsIcon(DbIcons.autoAwesome, 18.dp, c.p)
                    Text("Lou — AI assistant", color = c.on, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Connect any AI provider for full answers grounded in your notes & tasks. " +
                        "Without a key, Lou uses local search only.",
                    color = c.onv, fontSize = 12.5.sp, lineHeight = 18.sp,
                )
                Spacer(Modifier.height(16.dp))

                // Provider
                FieldLabel("Provider")
                ExposedDropdownMenuBox(expanded = providerMenu, onExpandedChange = { providerMenu = it }) {
                    OutlinedTextField(
                        value = provider.label,
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = providerMenu) },
                        colors = fieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    )
                    ExposedDropdownMenu(expanded = providerMenu, onDismissRequest = { providerMenu = false }) {
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
                OutlinedTextField(
                    value = key,
                    onValueChange = { key = it },
                    singleLine = true,
                    placeholder = { Text(provider.keyHint, fontSize = 14.sp) },
                    leadingIcon = { MsIcon(DbIcons.vpnKey, 18.dp, c.onv) },
                    trailingIcon = {
                        MsIconButton(
                            icon = if (reveal) DbIcons.visibilityOff else DbIcons.visibility,
                            onClick = { reveal = !reveal },
                            contentDescription = if (reveal) "Hide API key" else "Show API key",
                            iconSize = 18.dp,
                        )
                    },
                    visualTransformation = if (reveal) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = fieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
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
                        AssistChip(
                            onClick = {
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
                            },
                            enabled = !loadingModels,
                            label = { Text(if (loadingModels) "Loading…" else "Load models", fontSize = 12.5.sp) },
                            leadingIcon = {
                                if (loadingModels) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = c.p)
                                } else {
                                    MsIcon(DbIcons.refresh, 16.dp, c.onv)
                                }
                            },
                            border = null,
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = c.sCont,
                                labelColor = c.onv,
                                leadingIconContentColor = c.onv,
                            ),
                        )
                        if (fetchedModels.isNotEmpty()) {
                            TextButton(
                                onClick = { modelMenu = true },
                                colors = ButtonDefaults.textButtonColors(contentColor = c.p),
                            ) {
                                Text("${fetchedModels.size} available", fontSize = 12.sp)
                            }
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
                            ModelChip(label = m, selected = m == model) { model = m }
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
                        TextButton(
                            onClick = { vm.saveAiSettings(provider, null, model, baseUrl); key = "" },
                            colors = ButtonDefaults.textButtonColors(contentColor = c.err),
                        ) {
                            Text("Remove key", fontSize = 13.sp)
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(contentColor = c.onv),
                    ) {
                        Text("Cancel", fontSize = 14.sp)
                    }
                    Spacer(Modifier.size(8.dp))
                    Button(
                        onClick = {
                            vm.saveAiSettings(provider, key.ifBlank { null }, model, baseUrl)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(20.dp),
                    ) {
                        Text("Save", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

/** Shared OutlinedTextField colours mapped to the Devbook tokens. */
@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = DevbookTheme.colors.sLow,
    unfocusedContainerColor = DevbookTheme.colors.sLow,
    focusedTextColor = DevbookTheme.colors.on,
    unfocusedTextColor = DevbookTheme.colors.on,
    cursorColor = DevbookTheme.colors.p,
    focusedBorderColor = DevbookTheme.colors.p,
    unfocusedBorderColor = DevbookTheme.colors.outlinev,
    focusedPlaceholderColor = DevbookTheme.colors.outline,
    unfocusedPlaceholderColor = DevbookTheme.colors.outline,
)

@Composable
private fun PlainField(value: String, onChange: (String) -> Unit, placeholder: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        singleLine = true,
        placeholder = { Text(placeholder, fontSize = 14.sp) },
        colors = fieldColors(),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ModelChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val c = DevbookTheme.colors
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontSize = 12.sp) },
        border = null,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = c.sCont,
            labelColor = c.onv,
            selectedContainerColor = c.secc,
            selectedLabelColor = c.onsecc,
        ),
    )
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
