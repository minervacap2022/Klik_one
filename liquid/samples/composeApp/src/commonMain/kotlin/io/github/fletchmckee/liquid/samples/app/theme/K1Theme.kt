// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("ktlint:compose:compositionlocal-allowlist")

package io.github.fletchmckee.liquid.samples.app.theme

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

/**
 * K1 paper-and-ink palette. Every K1 surface, ink and line token lives here
 * so dark mode is a single swap from [K1PaletteLight] to [K1PaletteDark].
 *
 * Light mode is the original editorial paper palette (white paper, near-black
 * ink). Dark mode keeps the same ratio of contrast but inverts the substrate:
 * a warm graphite "paper" and a warm cream "ink" so the editorial feel survives.
 */
data class K1Palette(
  val inkPrimary: Color,
  val inkBody: Color,
  val inkSecondary: Color,
  val inkTertiary: Color,
  val inkMuted: Color,
  val inkFaint: Color,
  val paperApp: Color,
  val paperCard: Color,
  val paperSoft: Color,
  val paperFocal: Color,
  val paperChip: Color,
  val lineHairline: Color,
  val lineSoft: Color,
  val lineMute: Color,
  val lineTick: Color,
)

internal val K1PaletteLight = K1Palette(
  inkPrimary = Color(0xFF1C1D21),
  inkBody = Color(0xFF3A3D44),
  inkSecondary = Color(0xFF5A5D64),
  inkTertiary = Color(0xFF7A7D85),
  inkMuted = Color(0xFFA8ABB2),
  inkFaint = Color(0xFFB0B3BA),
  paperApp = Color(0xFFFFFFFF),
  paperCard = Color(0xFFFFFFFF),
  paperSoft = Color(0xFFF9FAFB),
  paperFocal = Color(0xFFF6F7F9),
  paperChip = Color(0xFFF4F5F7),
  lineHairline = Color(0xFFE8E9EC),
  lineSoft = Color(0xFFEDEEF1),
  lineMute = Color(0xFFD4D6DB),
  lineTick = Color(0xFFC8C9CD),
)

// Warm graphite substrate (NOT pure black) and warm cream ink so the dark
// theme reads as "night paper", not Material night-mode. Contrast ratios on
// the primary ink/paper pair are AAA (>= 14:1).
internal val K1PaletteDark = K1Palette(
  inkPrimary = Color(0xFFF2EEE7), // warm cream — primary text/icons
  inkBody = Color(0xFFD9D4CB),
  inkSecondary = Color(0xFFB8B2A9),
  inkTertiary = Color(0xFF8D8780),
  inkMuted = Color(0xFF6A655E),
  inkFaint = Color(0xFF5C5852),
  paperApp = Color(0xFF161413), // warm near-black
  paperCard = Color(0xFF1D1B19),
  paperSoft = Color(0xFF221F1D),
  paperFocal = Color(0xFF272421),
  paperChip = Color(0xFF2A2724),
  lineHairline = Color(0xFF35312D),
  lineSoft = Color(0xFF2E2A27),
  lineMute = Color(0xFF4A453F),
  lineTick = Color(0xFF6A655E),
)

/**
 * Module-level palette state. Reading `K1PaletteState.value` from a Composable
 * subscribes that Composable to palette changes (mutableStateOf is observed by
 * the Compose runtime). Non-composable callers — e.g., legacy top-level color
 * vals consumed by Material's lightColorScheme — get the current value at
 * call time. LiquidTheme is the only writer; user toggles route through it.
 */
internal val K1PaletteState = mutableStateOf(K1PaletteLight)

internal val LocalK1Palette = staticCompositionLocalOf { K1PaletteLight }

/**
 * Set by LiquidTheme. Top-level token getters use this so non-composable
 * call sites (e.g., parameter defaults evaluated at class init) still resolve.
 */
internal val K1FontFamilyState = mutableStateOf<FontFamily>(FontFamily.Default)

/**
 * Three font families exposed in PreferencesScreen. Indexes are stable wire
 * values — server sends `selectedFontIndex: Int` and we map it here. Any
 * index outside 0..2 clamps to System.
 *
 *  0 — System (default platform sans: SF Pro on iOS, Roboto on Android)
 *  1 — Serif  (FontFamily.Serif — Charter / Source Serif on iOS, Noto Serif on Android)
 *  2 — Mono   (FontFamily.Monospace — Menlo on iOS, Droid Sans Mono on Android)
 */
fun k1FontFamilyForIndex(index: Int): FontFamily = when (index) {
  1 -> FontFamily.Serif
  2 -> FontFamily.Monospace
  else -> FontFamily.Default
}

internal val LocalK1FontFamily = staticCompositionLocalOf { FontFamily.Default }
