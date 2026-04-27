// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("ktlint:compose:compositionlocal-allowlist")

package io.github.fletchmckee.liquid.samples.app.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Liquid Glass Settings Data Class
data class LiquidGlassSettings(
  val frost: Dp = 20.dp,
  val transparency: Float = 0.7f,
  val refraction: Float = 0.4f,
  val curve: Float = 0.3f,
  val edge: Float = 0.02f,
  val applyToCards: Boolean = true,
)

private val LightColorScheme = lightColorScheme(
  primary = KlikPrimary,
  secondary = LiveColor,
  tertiary = StriveColor,
  surface = KlikSurface.copy(alpha = 0.9f),
  surfaceVariant = KlikLightGrey,
  background = KlikBackground,
  onPrimary = Color.White,
  onBackground = KlikBlack,
  onSurface = KlikBlack,
  surfaceContainer = KlikLightGrey,
)

private val DarkColorScheme = darkColorScheme(
  primary = KlikPrimary,
  secondary = LiveColor,
  tertiary = StriveColor,
  surface = KlikBlack.copy(alpha = 0.5f),
  surfaceVariant = KlikDarkGrey,
  background = Color.Black,
  onPrimary = Color.White,
  onBackground = Color.White,
  onSurface = Color.White,
  surfaceContainer = KlikDarkGrey,
)

internal val LocalUseLiquid = staticCompositionLocalOf { true }
internal val LocalInitialFrost = staticCompositionLocalOf { 0f }
internal val LocalInitialDispersion = staticCompositionLocalOf { 0f }
internal val LocalIsBenchmark = staticCompositionLocalOf { false }
internal val LocalIsScreenshotTest = staticCompositionLocalOf { false }
internal val LocalBackgroundColor = staticCompositionLocalOf<((BackgroundOption) -> Unit)?> { null }

// Liquid Glass Settings CompositionLocals
val LocalLiquidGlassSettings = staticCompositionLocalOf { LiquidGlassSettings() }
val LocalSetLiquidGlassSettings = staticCompositionLocalOf<((LiquidGlassSettings) -> Unit)?> { null }

// Background index for insight card colors
val LocalBackgroundIndex = staticCompositionLocalOf { 1 }
val LocalInsightCardColors = staticCompositionLocalOf { getInsightCardColors(1) }

// Font index for typography (5 = IBM Plex font)
val LocalFontIndex = staticCompositionLocalOf { 5 }
val LocalSetFontIndex = staticCompositionLocalOf<((Int) -> Unit)?> { null }

// Typography Scales
val LocalFontSizeScale = staticCompositionLocalOf { 1f }
val LocalSetFontSizeScale = staticCompositionLocalOf<((Float) -> Unit)?> { null }

val LocalLetterSpacingScale = staticCompositionLocalOf { 1f }
val LocalSetLetterSpacingScale = staticCompositionLocalOf<((Float) -> Unit)?> { null }

val LocalLineHeightScale = staticCompositionLocalOf { 1f }
val LocalSetLineHeightScale = staticCompositionLocalOf<((Float) -> Unit)?> { null }

// Snackbar host state for undo actions
val LocalSnackbarHostState = staticCompositionLocalOf { SnackbarHostState() }

@Composable
fun LiquidTheme(
  darkMode: Boolean = isSystemInDarkTheme(),
  useLiquid: Boolean = true,
  initialFrost: Float = 0f,
  initialDispersion: Float = 0f,
  isBenchmark: Boolean = false,
  isScreenshotTest: Boolean = false,
  setBackgroundColor: ((BackgroundOption) -> Unit)? = null,
  liquidGlassSettings: LiquidGlassSettings = LiquidGlassSettings(),
  setLiquidGlassSettings: ((LiquidGlassSettings) -> Unit)? = null,
  backgroundIndex: Int = 1,
  fontIndex: Int = 5,
  setFontIndex: ((Int) -> Unit)? = null,
  fontSizeScale: Float = 1f,
  setFontSizeScale: ((Float) -> Unit)? = null,
  letterSpacingScale: Float = 1f,
  setLetterSpacingScale: ((Float) -> Unit)? = null,
  lineHeightScale: Float = 1f,
  setLineHeightScale: ((Float) -> Unit)? = null,
  content: @Composable () -> Unit,
) = CompositionLocalProvider(
  LocalUseLiquid provides useLiquid,
  LocalInitialFrost provides initialFrost,
  LocalInitialDispersion provides initialDispersion,
  LocalIsBenchmark provides isBenchmark,
  LocalIsScreenshotTest provides isScreenshotTest,
  LocalBackgroundColor provides setBackgroundColor,
  LocalLiquidGlassSettings provides liquidGlassSettings,
  LocalSetLiquidGlassSettings provides setLiquidGlassSettings,
  LocalBackgroundIndex provides backgroundIndex,
  LocalInsightCardColors provides getInsightCardColors(backgroundIndex),
  LocalFontIndex provides fontIndex,
  LocalSetFontIndex provides setFontIndex,
  LocalFontSizeScale provides fontSizeScale,
  LocalSetFontSizeScale provides setFontSizeScale,
  LocalLetterSpacingScale provides letterSpacingScale,
  LocalSetLetterSpacingScale provides setLetterSpacingScale,
  LocalLineHeightScale provides lineHeightScale,
  LocalSetLineHeightScale provides setLineHeightScale,
) {
  // Use composable typography to load custom Google Fonts
  val typography = getTypographyForFontComposable(fontIndex)

  MaterialTheme(
    colorScheme = if (darkMode) DarkColorScheme else LightColorScheme,
    typography = typography,
    content = content,
  )
}
