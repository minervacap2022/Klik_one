// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ─── Klik One core tokens ─────────────────────────────────────────────────
// Every paper/ink/line token reads from K1PaletteState (see K1Theme.kt).
// Composables that read these subscribe to recomposition automatically
// because mutableStateOf reads are observed by the Compose runtime; non-
// composable callers (e.g., Material's lightColorScheme initialised at
// module load) get the value at call time and effectively snapshot the
// light palette since LiquidTheme is the only writer and runs in a
// composition.
//
// LightKlik* constants pin a few well-known refs (Material color scheme,
// BackgroundOptions list) to the light palette unconditionally — these
// are non-composable lists that compose at class-init time and never
// re-evaluate, so they must use a stable value.

private val LightKlikInkPrimary = K1PaletteLight.inkPrimary
private val LightKlikPaperApp = K1PaletteLight.paperApp
private val LightKlikPaperCard = K1PaletteLight.paperCard
private val LightKlikPaperChip = K1PaletteLight.paperChip
private val LightKlikInkSecondary = K1PaletteLight.inkSecondary

val KlikInkPrimary: Color get() = K1PaletteState.value.inkPrimary
val KlikInkBody: Color get() = K1PaletteState.value.inkBody
val KlikInkSecondary: Color get() = K1PaletteState.value.inkSecondary
val KlikInkTertiary: Color get() = K1PaletteState.value.inkTertiary
val KlikInkMuted: Color get() = K1PaletteState.value.inkMuted
val KlikInkFaint: Color get() = K1PaletteState.value.inkFaint

val KlikPaperApp: Color get() = K1PaletteState.value.paperApp
val KlikPaperCard: Color get() = K1PaletteState.value.paperCard
val KlikPaperSoft: Color get() = K1PaletteState.value.paperSoft
val KlikPaperFocal: Color get() = K1PaletteState.value.paperFocal
val KlikPaperChip: Color get() = K1PaletteState.value.paperChip

val KlikLineHairline: Color get() = K1PaletteState.value.lineHairline
val KlikLineSoft: Color get() = K1PaletteState.value.lineSoft
val KlikLineMute: Color get() = K1PaletteState.value.lineMute
val KlikLineTick: Color get() = K1PaletteState.value.lineTick

// Signal — decision (amber), commitment (green), risk (pink), alert, warn, running.
val KlikDecisionBg = Color(0xFFFAEEDA)
val KlikDecisionText = Color(0xFF412402)
val KlikDecisionSubtext = Color(0xFF854F0B)
val KlikDecisionAccent = Color(0xFFBA7517)

val KlikCommitmentBg = Color(0xFFE1F5EE)
val KlikCommitmentText = Color(0xFF04342C) // teal/ink — body inside teal fills
val KlikCommitmentSubtext = Color(0xFF085041) // teal/label — inline label ("COMMITMENT")
val KlikCommitmentStatus = Color(0xFF0F6E56) // teal/text — status label "COMPLETED" on white
val KlikCommitmentAccent = Color(0xFF1D9E75)

val KlikRiskBg = Color(0xFFFBEAF0)
val KlikRiskText = Color(0xFF4B1528)
val KlikRiskSubtext = Color(0xFF993556)
val KlikRiskAccent = Color(0xFF993556)

val KlikAlert = Color(0xFFD85A30)
val KlikWarn = Color(0xFFD4A23C)
val KlikRunning = Color(0xFF1D9E75)

// Avatar palette — deterministic by initials hash.
val KlikAvatarBg = listOf(
  Color(0xFFE2DBF3),
  Color(0xFFF5DDE2),
  Color(0xFFD4EDE0),
  Color(0xFFFBE8D2),
  Color(0xFFDCEAF6),
)
val KlikAvatarFg = listOf(
  Color(0xFF6E62B6),
  Color(0xFFB3637A),
  Color(0xFF4A8B65),
  Color(0xFFA07840),
  Color(0xFF6E9DC0),
)

// Entity dot colors for mentioned chips.
val KlikDotPerson = Color(0xFFA8ABB2)
val KlikDotProject = Color(0xFF7F77DD)
val KlikDotOrg = Color(0xFF1D9E75)

// ─── Legacy-compatible names (repointed to Klik One) ──────────────────────
// Kept so every existing screen compiles with zero edits. These are
// non-composable top-level vals so they bind to the LIGHT palette
// constants — Material's lightColorScheme (LiquidTheme.kt) reads them
// at module load. The K1 dark-mode swap happens inside Compose via
// LocalK1Palette and does not need these constants to react.
val KlikPrimary = LightKlikInkPrimary
val KlikBackground = LightKlikPaperApp
val KlikSurface = LightKlikPaperCard

val StriveColor = KlikDotPerson // was: people orange
val EngageColor = KlikDotProject // was: orgs purple
val LiveColor = KlikDotOrg // was: projects teal

val KlikBlack = LightKlikInkPrimary
val KlikDarkGrey = LightKlikInkSecondary
val KlikLightGrey = LightKlikPaperChip

// Flattened "liquid" gradient tokens — now solid ink on paper.
val LiquidGradientStart = LightKlikInkPrimary
val LiquidGradientEnd = LightKlikInkPrimary
val LiquidBackgroundBrush = listOf(LightKlikPaperApp, LightKlikPaperApp)

// ─── Background option model (preserved; values flattened) ────────────────
sealed class BackgroundOption {
  abstract val name: String
  data class ColorBackground(val color: Color, override val name: String) : BackgroundOption()
  data class GradientBackground(val brush: Brush, override val name: String) : BackgroundOption()
  data class ImageBackground(val resourceName: String, override val name: String) : BackgroundOption()
}

// ─── Klik accent gradients → now flat black brushes ───────────────────────
// Every accent collapses to ink so logos/FABs read as clean black marks.
object KlikAccentGradients {
  private val inkBrush = Brush.linearGradient(listOf(LightKlikInkPrimary, LightKlikInkPrimary))
  val Default = inkBrush
  val SunriseGlow = inkBrush
  val MorningMist = inkBrush
  val SoftDawn = inkBrush
  val Ethereal = inkBrush
  val OceanBreeze = inkBrush
  val LavenderDream = inkBrush
  val GoldenHour = inkBrush
  val MintFresh = inkBrush
  val RoseQuartz = inkBrush
  val PeachVanilla = inkBrush
  val LavenderBlush = inkBrush
  val AliceBlue = inkBrush
  val LightBlue = inkBrush
  val LightGreen = inkBrush
  val LightOrange = inkBrush
  val LightPink = inkBrush
  val LightPurple = inkBrush
  val LightYellow = inkBrush
  val LightCyan = inkBrush
  val OffWhite = inkBrush
  val LightIndigo = inkBrush
  val DeepOrangeTint = inkBrush
  val LightGreenTint = inkBrush
  val AmberTint = inkBrush
  val BlueGreyTint = inkBrush
  val LimeTint = inkBrush
  val BrownTint = inkBrush
  val Pink100 = inkBrush
  val DeepPurple100 = inkBrush
  val Indigo100 = inkBrush
}

fun getKlikAccentGradient(backgroundIndex: Int): Brush = KlikAccentGradients.Default
fun getKlikPrimaryColor(backgroundIndex: Int): Color = LightKlikInkPrimary

// ─── Insight card tints — muted Klik One signal bg tones ──────────────────
// Calendar → warm decision amber; Function → project purple; Growth → commitment green.
data class InsightCardColors(
  val calendarTint: Color,
  val functionTint: Color,
  val growthTint: Color,
)

private val klikOneInsightTints = InsightCardColors(
  calendarTint = KlikDecisionBg,
  functionTint = KlikDotProject.copy(alpha = 0.16f),
  growthTint = KlikCommitmentBg,
)

fun getInsightCardColors(backgroundIndex: Int): InsightCardColors = klikOneInsightTints

// ─── Background options — all resolve to Klik One paper ───────────────────
// The picker stays functional (names preserved) but every option paints
// #EFEFF3. No more peach/blue/lavender gradients — pure editorial paper.
val BackgroundOptions: List<BackgroundOption> = listOf(
  BackgroundOption.ColorBackground(LightKlikPaperApp, "Klik Paper"),
  BackgroundOption.ColorBackground(LightKlikPaperApp, "Sunrise Glow"),
  BackgroundOption.ColorBackground(LightKlikPaperApp, "Morning Mist"),
  BackgroundOption.ColorBackground(LightKlikPaperApp, "Soft Dawn"),
  BackgroundOption.ColorBackground(LightKlikPaperApp, "Ethereal"),
  BackgroundOption.ColorBackground(LightKlikPaperApp, "Ocean Breeze"),
  BackgroundOption.ColorBackground(LightKlikPaperApp, "Lavender Dream"),
  BackgroundOption.ColorBackground(LightKlikPaperApp, "Golden Hour"),
  BackgroundOption.ColorBackground(LightKlikPaperApp, "Mint Fresh"),
  BackgroundOption.ColorBackground(LightKlikPaperApp, "Rose Quartz"),
  BackgroundOption.ColorBackground(LightKlikPaperApp, "Peach Vanilla"),
  BackgroundOption.ColorBackground(LightKlikPaperApp, "Lavender Blush"),
  BackgroundOption.ColorBackground(LightKlikPaperApp, "Alice Blue"),
  BackgroundOption.ColorBackground(LightKlikPaperApp, "Light Blue"),
  BackgroundOption.ColorBackground(LightKlikPaperApp, "Light Green"),
  BackgroundOption.ColorBackground(LightKlikPaperApp, "Light Orange"),
  BackgroundOption.ColorBackground(LightKlikPaperApp, "Light Pink"),
  BackgroundOption.ColorBackground(LightKlikPaperApp, "Light Purple"),
  BackgroundOption.ColorBackground(LightKlikPaperApp, "Light Yellow"),
  BackgroundOption.ColorBackground(LightKlikPaperApp, "Light Cyan"),
  BackgroundOption.ColorBackground(LightKlikPaperApp, "Off White"),
  BackgroundOption.ColorBackground(LightKlikPaperApp, "Light Indigo"),
  BackgroundOption.ColorBackground(LightKlikPaperApp, "Deep Orange"),
  BackgroundOption.ColorBackground(LightKlikPaperApp, "Sage Green"),
  BackgroundOption.ColorBackground(LightKlikPaperApp, "Amber"),
  BackgroundOption.ColorBackground(LightKlikPaperApp, "Blue Grey"),
  BackgroundOption.ColorBackground(LightKlikPaperApp, "Lime"),
  BackgroundOption.ColorBackground(LightKlikPaperApp, "Brown"),
  BackgroundOption.ColorBackground(LightKlikPaperApp, "Pink"),
  BackgroundOption.ColorBackground(LightKlikPaperApp, "Deep Purple"),
  BackgroundOption.ColorBackground(LightKlikPaperApp, "Indigo"),
)
