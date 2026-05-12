package com.nammasanthe.ledger.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary          = Saffron700,
    onPrimary        = SurfaceWhite,
    primaryContainer = Saffron100,
    onPrimaryContainer= TextPrimary,
    secondary        = CreditGreen,
    onSecondary      = SurfaceWhite,
    secondaryContainer= CreditLight,
    error            = PaymentRed,
    onError          = SurfaceWhite,
    errorContainer   = PaymentLight,
    background       = ScaffoldBg,
    onBackground     = TextPrimary,
    surface          = SurfaceWhite,
    onSurface        = TextPrimary,
    surfaceVariant   = Saffron50,
    outline          = DividerColor,
)

@Composable
fun NammaSantheLedgerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // App currently supports light mode only (dark mode = future scope)
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography   = Typography,
        content      = content
    )
}