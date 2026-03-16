package com.metoly.components

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun NoteDrawingDialog(
    onDismiss: () -> Unit,
    onDrawingSaved: (path: String) -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .height(400.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.medium
                )
                .padding(8.dp)
        ) {
            DrawingCanvas(
                modifier = Modifier.fillMaxSize(),
                onSave = { bitmap ->
                    onDrawingSaved(saveBitmapToInternalStorage(context, bitmap))
                }
            )
        }
    }
}

private fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap): String {
    val fileName = "drawing_${System.currentTimeMillis()}.png"
    context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
    }
    return context.getFileStreamPath(fileName).absolutePath
}