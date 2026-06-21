package com.metoly.morganize.feature.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.metoly.morganize.core.data.NoteRepository
import com.metoly.morganize.core.model.Note
import com.metoly.morganize.core.model.ResponseState
import com.metoly.morganize.core.ui.theme.MorganizeTheme
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MorganizeWidgetConfigureActivity : ComponentActivity() {

    private val noteRepository: NoteRepository by inject()

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Find the widget id from the intent.
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        // Set the result to CANCELED. This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(Activity.RESULT_CANCELED)

        setContent {
            MorganizeTheme {
                val notesState by noteRepository.getAllNotes().collectAsState(initial = ResponseState.Loading)
                val scope = rememberCoroutineScope()

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Select Note for Widget") }
                        )
                    }
                ) { padding ->
                    Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                        when (notesState) {
                            is ResponseState.Loading, is ResponseState.Idle -> {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            }
                            is ResponseState.Error -> {
                                Text(
                                    "Failed to load notes: ${(notesState as ResponseState.Error).message}",
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                            is ResponseState.Success -> {
                                val notes = (notesState as ResponseState.Success).data
                                if (notes.isEmpty()) {
                                    Text(
                                        "No notes found. Create a note first.",
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                } else {
                                    LazyColumn {
                                        items(notes) { note ->
                                            NoteSelectionItem(note) {
                                                scope.launch {
                                                    saveWidgetConfigAndFinish(note.id)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun saveWidgetConfigAndFinish(noteId: Long) {
        val glanceId = GlanceAppWidgetManager(this).getGlanceIdBy(appWidgetId)
        
        updateAppWidgetState(this, PreferencesGlanceStateDefinition, glanceId) { prefs ->
            prefs.toMutablePreferences().apply {
                this[longPreferencesKey("selected_note_id")] = noteId
            }
        }
        
        MorganizeWidget().update(this, glanceId)

        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }
}

@Composable
fun NoteSelectionItem(note: Note, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = note.title.ifEmpty { "Untitled" },
                style = MaterialTheme.typography.titleMedium
            )
            val snippet = note.toShareText().take(50).replace("\n", " ")
            if (snippet.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = snippet,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )
            }
        }
    }
}
