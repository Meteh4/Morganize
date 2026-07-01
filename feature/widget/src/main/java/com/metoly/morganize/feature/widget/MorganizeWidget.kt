package com.metoly.morganize.feature.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.glance.Button
import androidx.glance.ButtonDefaults
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextDefaults
import androidx.glance.text.TextStyle
import com.metoly.morganize.core.data.NoteRepository
import com.metoly.morganize.core.model.Note
import com.metoly.morganize.core.model.ResponseState
import com.metoly.morganize.core.model.grid.GridItem
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MorganizeWidget : GlanceAppWidget(), KoinComponent {

    private val noteRepository: NoteRepository by inject()

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Fetch all notes
        val notesState = noteRepository.getAllNotes().first { it !is ResponseState.Loading && it !is ResponseState.Idle }
        val allNotes = if (notesState is ResponseState.Success) {
            notesState.data
        } else {
            emptyList()
        }

        provideContent {
            val prefs = currentState<Preferences>()
            val selectedNoteId = prefs[longPreferencesKey("selected_note_id")]

            val selectedNote = allNotes.find { it.id == selectedNoteId } ?: allNotes.firstOrNull()

            GlanceTheme {
                WidgetResponsiveContent(
                    context = context,
                    selectedNote = selectedNote,
                    recentNotes = allNotes.take(3)
                )
            }
        }
    }

    @Composable
    private fun WidgetResponsiveContent(
        context: Context,
        selectedNote: Note?,
        recentNotes: List<Note>
    ) {
        val outerBackgroundColor = GlanceTheme.colors.surfaceVariant
        val outerTextColor = GlanceTheme.colors.onSurfaceVariant
        val selectedNoteId = selectedNote?.id ?: -1L

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(outerBackgroundColor)
                .cornerRadius(20.dp)
                .clickable(
                    actionStartActivity(
                        android.content.ComponentName(context, "com.metoly.morganize.MainActivity"),
                        androidx.glance.action.actionParametersOf(
                            androidx.glance.action.ActionParameters.Key<Long>("open_note_id") to selectedNoteId
                        )
                    )
                )
        ) {
            Row(modifier = GlanceModifier.fillMaxSize()) {
                // Colored strip on the side
                val bgColor = selectedNote?.backgroundColor
                if (bgColor != null) {
                    Box(
                        modifier = GlanceModifier
                            .width(8.dp)
                            .fillMaxHeight()
                            .background(ColorProvider(day = Color(bgColor), night = Color(bgColor)))
                    ) {}
                }

                Column(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .fillMaxHeight()
                        .padding(16.dp)
                ) {
                    // Top Bar: Title
                    Row(
                        modifier = GlanceModifier.fillMaxWidth().padding(bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedNote?.title?.ifEmpty { "Untitled" } ?: "No Note",
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                color = outerTextColor
                            ),
                            maxLines = 1,
                            modifier = GlanceModifier.defaultWeight()
                        )
                    }

                    // Inner Box (Page Content) — Grid layout simulation
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .defaultWeight()
                            .background(GlanceTheme.colors.surface)
                            .cornerRadius(16.dp)
                    ) {
                        if (selectedNote != null) {
                            val firstPage = selectedNote.pages.firstOrNull()
                            if (firstPage != null && firstPage.items.isNotEmpty()) {
                                GridLayoutContent(items = firstPage.items)
                            } else {
                                // Empty page content
                                Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "Empty Page",
                                        style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant)
                                    )
                                }
                            }
                        } else {
                            // No note selected
                            Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Please configure the widget",
                                    style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Simulates the app's 10-column grid layout by computing the exact dp size of each cell
     * and using padding(start, top) for absolute positioning inside a Box.
     */
    @Composable
    private fun GridLayoutContent(items: List<GridItem>) {
        val columns = 10
        val widgetSize = LocalSize.current
        // Available width = widget width minus color strip(8) and Column padding(16*2)
        val availableWidth = widgetSize.width - 40.dp
        val colWidth = availableWidth / columns

        val maxRows = items.maxOfOrNull { it.y + it.height } ?: 10
        val totalHeight = colWidth * maxRows

        LazyColumn(
            modifier = GlanceModifier.fillMaxSize()
        ) {
            item {
                Box(modifier = GlanceModifier.fillMaxWidth().height(totalHeight)) {
                    for (item in items) {
                        // Offset wrapper (acts as absolute positioning)
                        Box(
                            modifier = GlanceModifier.padding(
                                start = colWidth * item.x,
                                top = colWidth * item.y
                            )
                        ) {
                            // Size wrapper
                            Box(
                                modifier = GlanceModifier
                                    .width(colWidth * item.width)
                                    .height(colWidth * item.height)
                                    .padding(4.dp) // inner spacing between grid items
                            ) {
                                RenderGridItem(item)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun RenderGridItem(item: GridItem) {
        when (item) {
            is GridItem.Text -> {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(GlanceTheme.colors.surfaceVariant)
                        .cornerRadius(8.dp)
                        .padding(8.dp)
                ) {
                    Text(
                        text = item.textContent,
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurface,
                            fontSize = androidx.compose.ui.unit.TextUnit(
                                item.fontSize.coerceIn(10f, 18f),
                                androidx.compose.ui.unit.TextUnitType.Sp
                            )
                        ),
                        modifier = GlanceModifier.fillMaxWidth()
                    )
                }
            }
            is GridItem.Checklist -> {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(GlanceTheme.colors.surfaceVariant)
                        .cornerRadius(8.dp)
                        .padding(8.dp)
                ) {
                    Column(modifier = GlanceModifier.fillMaxWidth()) {
                        if (item.title.isNotEmpty()) {
                            Text(
                                text = item.title,
                                style = TextStyle(
                                    fontWeight = FontWeight.Bold,
                                    color = GlanceTheme.colors.onSurface,
                                    fontSize = androidx.compose.ui.unit.TextUnit(14f, androidx.compose.ui.unit.TextUnitType.Sp)
                                ),
                                modifier = GlanceModifier.padding(bottom = 4.dp)
                            )
                        }
                        for (entry in item.entries) {
                            val icon = if (entry.isChecked) "☑" else "☐"
                            Text(
                                text = "$icon ${entry.text}",
                                style = TextStyle(
                                    color = if (entry.isChecked) GlanceTheme.colors.onSurfaceVariant else GlanceTheme.colors.onSurface,
                                    fontSize = androidx.compose.ui.unit.TextUnit(13f, androidx.compose.ui.unit.TextUnitType.Sp),
                                    textDecoration = if (entry.isChecked) TextDecoration.LineThrough else TextDecoration.None
                                ),
                                modifier = GlanceModifier.padding(bottom = 2.dp)
                            )
                        }
                    }
                }
            }
            is GridItem.SecretItem -> {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(GlanceTheme.colors.surfaceVariant)
                        .cornerRadius(8.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🔒 Locked",
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurfaceVariant,
                            fontSize = androidx.compose.ui.unit.TextUnit(13f, androidx.compose.ui.unit.TextUnitType.Sp)
                        )
                    )
                }
            }
            is GridItem.Image -> {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(GlanceTheme.colors.surfaceVariant)
                        .cornerRadius(8.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🖼 Image",
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurfaceVariant,
                            fontSize = androidx.compose.ui.unit.TextUnit(13f, androidx.compose.ui.unit.TextUnitType.Sp)
                        )
                    )
                }
            }
        }
    }
}
