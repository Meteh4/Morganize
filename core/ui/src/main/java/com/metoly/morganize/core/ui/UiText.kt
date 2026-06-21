package com.metoly.morganize.core.ui

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/**
 * A wrapper class that can hold either a raw string or a string resource ID with optional format arguments.
 * Useful for passing strings from ViewModels to the UI without passing a Context.
 */
sealed class UiText {
    data class DynamicString(val value: String) : UiText()
    class StringResource(@StringRes val resId: Int, vararg val args: Any) : UiText()

    @Composable
    fun asString(): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> stringResource(resId, *args)
        }
    }

    fun asString(context: Context): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> context.getString(resId, *args)
        }
    }
}
