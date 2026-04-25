package com.metoly.morganize.feature.list.components
import androidx.compose.runtime.getValue

import androidx.compose.ui.res.painterResource

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.metoly.components.resolveNoteColor
import com.metoly.morganize.core.model.Category
import com.metoly.morganize.core.model.Note
import com.metoly.morganize.feature.list.R
import com.metoly.morganize.feature.list.util.DateFormatter
import com.metoly.morganize.core.ui.theme.MorgAnimation
import com.metoly.morganize.core.ui.theme.MorgShapes

/**
 * A rich card representing a single Note in the master list pane.
 * Renders category tags, title (or placeholder), and indicates selection state via color fills.
 *
 * @param note The domain entity for the note.
 * @param category The optionally assigned category for the note.
 * @param isSelected Whether this note is currently active in the detail pane.
 * @param onClick Callback triggered when tapped.
 */
@Composable
internal fun NoteCard(
    note: Note,
    category: Category?,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val resolvedNoteColor = resolveNoteColor(note.backgroundColor)

    val fillFraction by animateFloatAsState(
        targetValue = if (isSelected && resolvedNoteColor != null) 1f else 0f,
        animationSpec = MorgAnimation.standard(),
        label = "fill"
    )

    val containerColor by animateColorAsState(
        targetValue = if (isSelected && resolvedNoteColor == null)
            MaterialTheme.colorScheme.secondaryContainer
        else
            MaterialTheme.colorScheme.surfaceVariant,
        label = "card_bg"
    )

    val textColor by animateColorAsState(
        targetValue = when {
            isSelected && resolvedNoteColor != null ->
                if (resolvedNoteColor.luminance() > 0.5f) Color.Black else Color.White
            isSelected ->
                MaterialTheme.colorScheme.onSecondaryContainer
            else ->
                MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "text_color"
    )

    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = MorgShapes.card,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .then(
                    if (resolvedNoteColor != null) {
                        Modifier.drawBehind {
                            val stripPx = 5.dp.toPx()
                            val fillWidth = stripPx + (size.width - stripPx) * fillFraction
                            drawRect(
                                color = resolvedNoteColor,
                                size = Size(fillWidth, size.height)
                            )
                        }
                    } else Modifier
                )
        ) {
            if (resolvedNoteColor != null) {
                Spacer(modifier = Modifier.width(5.dp))
            }

            Column(modifier = Modifier.weight(1f).padding(16.dp)) {
                if (category != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = com.metoly.morganize.core.ui.R.drawable.category),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color(category.colorArgb)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.labelMedium,
                            color = textColor.copy(alpha = 0.8f)
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (note.isSecret && note.title.isBlank()) "Secret Note" else note.title.ifBlank { stringResource(R.string.feature_list_untitled) },
                        style = MaterialTheme.typography.titleMedium,
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    if (note.isSecret) {
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            painter = painterResource(id = com.metoly.morganize.core.ui.R.drawable.lock_locked),
                            contentDescription = "Secret Note",
                            modifier = Modifier.size(16.dp),
                            tint = textColor.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = DateFormatter.formatWithTime(note.updatedAt),
                    style = MaterialTheme.typography.labelLarge,
                    color = textColor.copy(alpha = 0.6f)
                )
            }
        }
    }
}
