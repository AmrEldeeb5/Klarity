package com.example.klarity.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klarity.presentation.DevbookScreen
import com.example.klarity.presentation.theme.DevbookTheme
import com.example.klarity.presentation.theme.LocalWindowMetrics

@Composable
fun DevbookTopBar(
    screen: DevbookScreen,
    showMenu: Boolean,
    onMenu: () -> Unit,
    onCta: () -> Unit,
) {
    val c = DevbookTheme.colors
    val compact = LocalWindowMetrics.current.isCompact
    Surface(color = c.bg, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = LocalWindowMetrics.current.screenPaddingH),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 14.dp),
        ) {
            if (showMenu) {
                MsIconButton(DbIcons.menu, onClick = onMenu, contentDescription = "Open navigation menu")
            }
            MsIcon(screen.barIcon, 24.dp, c.onv)
            Column {
                Text(screen.barCrumb, color = c.onv, fontSize = 12.sp)
                Text(screen.barTitle, color = c.on, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Secondary actions are desktop-only affordances; hide them on phones to free space.
                if (!compact) {
                    MsIconButton(DbIcons.history, onClick = {}, contentDescription = null)
                    MsIconButton(screen.barAction, onClick = {}, contentDescription = null)
                }
                // Primary CTA
                Button(
                    onClick = onCta,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.height(40.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 18.dp),
                ) {
                    MsIcon(DbIcons.add, 20.dp, c.op)
                    Spacer(Modifier.width(8.dp))
                    Text(screen.barCta, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
    // Bottom divider
    HorizontalDivider(thickness = 1.dp, color = c.outlinev)
}
