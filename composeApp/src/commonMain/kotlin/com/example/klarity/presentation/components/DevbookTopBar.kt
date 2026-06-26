package com.example.klarity.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klarity.presentation.DevbookScreen
import com.example.klarity.presentation.theme.DevbookTheme

@Composable
fun DevbookTopBar(
    screen: DevbookScreen,
    showMenu: Boolean,
    onMenu: () -> Unit,
    onCta: () -> Unit,
) {
    val c = DevbookTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(c.bg)
            .padding(horizontal = 28.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (showMenu) {
            RoundIconButton(DbIcons.menu, onClick = onMenu)
        }
        MsIcon(screen.barIcon, 24.dp, c.onv)
        Column {
            Text(screen.barCrumb, color = c.onv, fontSize = 12.sp)
            Text(screen.barTitle, color = c.on, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RoundIconButton(DbIcons.history, onClick = {})
            RoundIconButton(screen.barAction, onClick = {})
            // Primary CTA
            Row(
                modifier = Modifier
                    .height(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(c.p)
                    .clickable { onCta() }
                    .padding(start = 16.dp, end = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MsIcon(DbIcons.add, 20.dp, c.op)
                Text(screen.barCta, color = c.op, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
    // Bottom divider
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(c.outlinev))
}

@Composable
private fun RoundIconButton(icon: ImageVector, onClick: () -> Unit) {
    val c = DevbookTheme.colors
    Box(
        modifier = Modifier
            .size(40.dp)
            .hoverBg(CircleShape, c.sCont)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        MsIcon(icon, 22.dp, c.onv)
    }
}
