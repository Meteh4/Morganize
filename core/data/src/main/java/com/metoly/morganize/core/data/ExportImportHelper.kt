package com.metoly.morganize.core.data

import android.content.Context
import android.net.Uri
import com.metoly.morganize.core.model.Note
import com.metoly.morganize.core.model.ResponseState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

class ExportImportHelper(
    private val context: Context,
    private val json: Json,
    private val noteRepository: NoteRepository
) {
    suspend fun exportNotes(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val responseState = noteRepository.getAllNotes().firstOrNull()
            val notes = (responseState as? ResponseState.Success<List<Note>>)?.data ?: emptyList()
            
            val jsonString = json.encodeToString(notes)
            
            context.contentResolver.openOutputStream(uri)?.use { outputStream: OutputStream ->
                outputStream.write(jsonString.toByteArray())
            } ?: throw IllegalStateException("Could not open output stream")
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importNotes(uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream: InputStream ->
                inputStream.bufferedReader().use { it.readText() }
            } ?: throw IllegalStateException("Could not open input stream")
            
            val notes = json.decodeFromString<List<Note>>(jsonString)
            
            var importedCount = 0
            for (note in notes) {
                // Ensure the ID is reset so Room auto-generates a new ID
                val importedNote = note.copy(id = 0)
                noteRepository.insertNote(importedNote)
                importedCount++
            }
            
            Result.success(importedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
