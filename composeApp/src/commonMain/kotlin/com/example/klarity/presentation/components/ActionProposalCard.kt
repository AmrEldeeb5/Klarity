package com.example.klarity.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klarity.data.ai.isArchive
import com.example.klarity.presentation.ActionStatus
import com.example.klarity.presentation.ProposedAction
import com.example.klarity.presentation.theme.DevbookTheme

/**
 * A confirmation card for one action Lou proposed. While [ProposedAction.status] is PENDING it shows
 * Approve / Cancel; once resolved it collapses to a small status line. Shared by the full-screen
 * assistant and the side panel so the confirm-each-action UX is identical in both.
 */
@Composable
fun ActionProposalCard(
    action: ProposedAction,
    onApprove: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = DevbookTheme.colors
    // Archives (recoverable removals) get a distinct icon + "Archive" label so a remove reads
    // differently from a create/edit; nothing the assistant does is a permanent delete.
    val archive = action.action.isArchive
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = c.sLow,
        border = BorderStroke(1.dp, c.outlinev),
    ) {
        Column(modifier = Modifier.padding(start = 14.dp, top = 12.dp, end = 12.dp, bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MsIcon(if (archive) DbIcons.delete else DbIcons.autoAwesome, 18.dp, if (archive) c.onv else c.p)
                Text(
                    action.label,
                    color = c.on,
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                )
            }
            when (action.status) {
                ActionStatus.PENDING -> {
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(onClick = onDecline, colors = ButtonDefaults.textButtonColors(contentColor = c.onv)) {
                            Text("Cancel", fontSize = 13.sp)
                        }
                        Button(
                            onClick = onApprove,
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = c.p, contentColor = c.op),
                        ) {
                            Text(if (archive) "Archive" else "Approve", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                ActionStatus.DONE -> StatusLine(DbIcons.checkCircle, "Done", c.p)
                ActionStatus.DECLINED -> StatusLine(DbIcons.close, "Cancelled", c.onv)
                ActionStatus.FAILED -> StatusLine(DbIcons.close, "Couldn't complete that", c.err)
            }
        }
    }
}

@Composable
private fun StatusLine(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, tint: androidx.compose.ui.graphics.Color) {
    Spacer(Modifier.height(8.dp))
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        MsIcon(icon, 15.dp, tint)
        Text(label, color = tint, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}
