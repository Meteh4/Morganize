package com.metoly.morganize.feature.list.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

@Composable
internal fun NoteCard(
    note: Note,
    category: Category?,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val resolvedNoteColor = resolveNoteColor(note.backgroundColor)

    /* ── Şerit → tam dolgu animasyonu ── */
    val fillFraction by animateFloatAsState(
        targetValue = if (isSelected && resolvedNoteColor != null) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "fill"
    )

    /* Renksiz notlar için seçim arkaplanı */
    val containerColor by animateColorAsState(
        targetValue = if (isSelected && resolvedNoteColor == null)
            MaterialTheme.colorScheme.secondaryContainer
        else
            MaterialTheme.colorScheme.surfaceVariant,
        label = "card_bg"
    )

    /* Yazı rengi, mevcut arkaplan kontrastına uyum sağlar */
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
        shape = RoundedCornerShape(16.dp),
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
            /* Renk şeridi için boşluk (layout push) */
            if (resolvedNoteColor != null) {
                Spacer(modifier = Modifier.width(5.dp))
            }

            Column(modifier = Modifier.weight(1f).padding(16.dp)) {

                if (category != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Label,
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

                Text(
                    text = note.title.ifBlank { stringResource(R.string.feature_list_untitled) },
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (note.content.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor.copy(alpha = 0.8f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
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