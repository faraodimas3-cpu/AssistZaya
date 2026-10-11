package com.assistzaya.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val ZBg = Color(0xFF0B0A0C)
val ZSurface = Color(0xFF1A191C)
val ZSurface2 = Color(0xFF242226)
val ZOutline = Color(0xFF3A383D)
val ZRed = Color(0xFFB02A28)
val ZRedDark = Color(0xFF7A1F1F)
val ZGold = Color(0xFFF5B942)
val ZMuted = Color(0xFFA6A3A9)

@Composable
fun AssistZayaTheme(content: @Composable () -> Unit) {
    val scheme = darkColorScheme(
        primary = ZRed, onPrimary = Color.White,
        background = ZBg, onBackground = Color.White,
        surface = ZSurface, onSurface = Color.White,
        surfaceVariant = ZSurface2, onSurfaceVariant = ZMuted,
        outline = ZOutline, secondary = ZGold
    )
    MaterialTheme(colorScheme = scheme, content = content)
}
