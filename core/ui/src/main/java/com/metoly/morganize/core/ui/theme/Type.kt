package com.metoly.morganize.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Using system default for broad compatibility; swap out for a custom font if needed.
val AppFontFamily = FontFamily.Default

val AppTypography =
        Typography(
                displayLarge =
                        TextStyle(
                                fontFamily = AppFontFamily,
                                fontWeight = FontWeight.W400,
                                fontSize = 57.sp,
                                lineHeight = 64.sp,
                                letterSpacing = (-0.25).sp
                        ),
                displayMedium =
                        TextStyle(
                                fontFamily = AppFontFamily,
                                fontWeight = FontWeight.W400,
                                fontSize = 45.sp,
                                lineHeight = 52.sp
                        ),
                headlineLarge =
                        TextStyle(
                                fontFamily = AppFontFamily,
                                fontWeight = FontWeight.W600,
                                fontSize = 32.sp,
                                lineHeight = 40.sp
                        ),
                headlineMedium =
                        TextStyle(
                                fontFamily = AppFontFamily,
                                fontWeight = FontWeight.W600,
                                fontSize = 28.sp,
                                lineHeight = 36.sp
                        ),
                headlineSmall =
                        TextStyle(
                                fontFamily = AppFontFamily,
                                fontWeight = FontWeight.W600,
                                fontSize = 24.sp,
                                lineHeight = 32.sp
                        ),
                titleLarge =
                        TextStyle(
                                fontFamily = AppFontFamily,
                                fontWeight = FontWeight.W700,
                                fontSize = 22.sp,
                                lineHeight = 28.sp
                        ),
                titleMedium =
                        TextStyle(
                                fontFamily = AppFontFamily,
                                fontWeight = FontWeight.W700,
                                fontSize = 16.sp,
                                lineHeight = 24.sp,
                                letterSpacing = 0.15.sp
                        ),
                bodyLarge =
                        TextStyle(
                                fontFamily = AppFontFamily,
                                fontWeight = FontWeight.W400,
                                fontSize = 16.sp,
                                lineHeight = 24.sp,
                                letterSpacing = 0.5.sp
                        ),
                bodyMedium =
                        TextStyle(
                                fontFamily = AppFontFamily,
                                fontWeight = FontWeight.W400,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                letterSpacing = 0.25.sp
                        ),
                labelLarge =
                        TextStyle(
                                fontFamily = AppFontFamily,
                                fontWeight = FontWeight.W500,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                letterSpacing = 0.1.sp
                        )
        )
