package com.dss.workstation.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val WorkstationColorScheme = lightColorScheme(
    primary = DssPrimary,
    onPrimary = DssOnPrimary,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = Color(0xFF334155),
    onSecondary = Color(0xFFFFFFFF),
    background = DssBackground,
    onBackground = DssTextPrimary,
    surface = DssSurface,
    onSurface = DssTextPrimary,
    error = DssDanger,
    onError = DssOnDanger
)

@Composable
fun DssWorkstationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = WorkstationColorScheme,
        typography = DssTypography,
        content = content
    )
}
