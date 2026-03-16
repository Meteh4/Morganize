package com.metoly.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/**
 * Light/dark çifti taşıyan not rengi.
 * [light] variasyonunun ARGB değeri veritabanına kaydedilen anahtar olarak kullanılır.
 */
data class NoteThemeColor(
    val light: Color,
    val dark: Color
) {
    val argb: Int get() = light.toArgb()
}

/** Google Keep tarzı, her iki temada da iyi görünen renk paleti. */
val NoteThemeColors: List<NoteThemeColor> = listOf(
    NoteThemeColor(light = Color(0xFFF28B82), dark = Color(0xFF5C2B29)), // Coral
    NoteThemeColor(light = Color(0xFFFBBC04), dark = Color(0xFF614A19)), // Peach
    NoteThemeColor(light = Color(0xFFFFF475), dark = Color(0xFF635D19)), // Sand
    NoteThemeColor(light = Color(0xFFCCFF90), dark = Color(0xFF345920)), // Sage
    NoteThemeColor(light = Color(0xFFA7FFEB), dark = Color(0xFF16504B)), // Mint
    NoteThemeColor(light = Color(0xFFCBF0F8), dark = Color(0xFF2D555E)), // Teal
    NoteThemeColor(light = Color(0xFFAECBFA), dark = Color(0xFF1E3A5F)), // Fog
    NoteThemeColor(light = Color(0xFFD7AEFA), dark = Color(0xFF42275E)), // Storm
    NoteThemeColor(light = Color(0xFFE6C9A8), dark = Color(0xFF5B4636)), // Chalk
)

/**
 * Veritabanında saklanan ARGB değerini mevcut temaya uygun [Color]'a çevirir.
 * null → tema varsayılanı demektir.
 */
@Composable
fun resolveNoteColor(storedArgb: Int?): Color? {
    if (storedArgb == null) return null
    val isDark = isSystemInDarkTheme()
    val themeColor = NoteThemeColors.firstOrNull { it.argb == storedArgb }
        ?: return Color(storedArgb)
    return if (isDark) themeColor.dark else themeColor.light
}