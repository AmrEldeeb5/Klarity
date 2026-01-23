package com.example.klarity.presentation.screen.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Common UI components used across the home screen - Material 3 revamped
 */

/**
 * Material 3 FilterChip for view mode selection.
 * Replaced custom Surface with M3 FilterChip for standard selection pattern.
 */
@Composable
fun ViewModeButton(
    label: String,
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = icon,
                    fontSize = 12.sp
                )
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        leadingIcon = if (isSelected) {
            { Icon(imageVector = androidx.compose.material.icons.Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
        } else null,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

/**
 * Material 3 IconButton with emoji icons and active state.
 * Replaced custom Surface implementation with M3 FilledTonalIconButton.
 */
@Composable
fun IconActionButton(
    icon: String,
    isActive: Boolean = false,
    onClick: () -> Unit = {}
) {
    FilledTonalIconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp),  // M3 48dp touch target
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = if (isActive) 
                MaterialTheme.colorScheme.tertiaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isActive)
                MaterialTheme.colorScheme.tertiary
            else
                MaterialTheme.colorScheme.onSurface
        )
    ) {
        Text(
            text = icon,
            fontSize = 16.sp
        )
    }
}

/**
 * Material 3 small IconButton with emoji icons.
 * Replaced custom Surface with standard M3 IconButton (48dp touch target, 24dp visual).
 */
@Composable
fun SmallIconButton(icon: String, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp)  // M3 touch target
    ) {
        Text(
            text = icon,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Material 3 NavigationDrawerItem for sidebar navigation.
 * Replaced custom Surface with M3 NavigationDrawerItem for standard drawer pattern.
 */
@Composable
fun NavItem(
    icon: String,
    label: String,
    shortcut: String? = null,
    isActive: Boolean = false,
    onClick: () -> Unit = {}
) {
    NavigationDrawerItem(
        selected = isActive,
        onClick = onClick,
        icon = {
            Text(
                text = icon,
                fontSize = 18.sp
            )
        },
        label = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                if (shortcut != null && isActive) {
                    Text(
                        text = shortcut,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

