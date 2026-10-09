package com.example.project2.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * 一张纸。不跟系统深色、不取动态色：纸就是纸。
 * Material 的色槽只拿来垫底，真正的样式都在 Paper.kt 的构件里。
 */
private val PaperScheme = lightColorScheme(
    primary = Ink,
    onPrimary = Paper,
    secondary = Ink50,
    onSecondary = Paper,
    tertiary = Ink35,
    onTertiary = Paper,
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = PaperWarm,
    onSurfaceVariant = Ink70,
    outline = HairStrong,
    outlineVariant = Hair,
    error = Ink,
    onError = Paper,
)

@Composable
fun Project2Theme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = PaperScheme, typography = Typography, content = content)
}
