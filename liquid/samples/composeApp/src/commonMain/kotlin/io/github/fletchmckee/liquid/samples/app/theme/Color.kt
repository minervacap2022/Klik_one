// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
//
// KLIK ONE — editorial, minimal, near-black-on-warm-paper.
// All public names preserved so existing screens compile unchanged. Values
// repointed to the Klik One token set. Gradient brushes collapse to flat
// paper tones — liquid-glass visual effects dissolve into quiet neutrals.
package io.github.fletchmckee.liquid.samples.app.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ─── Klik One core tokens ─────────────────────────────────────────────────
// Text stack — spec v1.0 §2.
val KlikInkPrimary   = Color(0xFF1C1D21) // text/primary — headlines, icons, buttons
val KlikInkBody      = Color(0xFF3A3D44) // text/body — paragraphs on white (rare; usually primary)
val KlikInkSecondary = Color(0xFF5A5D64) // text/chip — secondary UI + chip text
val KlikInkTertiary  = Color(0xFF7A7D85) // text/secondary — subtitles, meta
val KlikInkMuted     = Color(0xFFA8ABB2) // text/tertiary — timestamps, hints, counts
val KlikInkFaint     = Color(0xFFB0B3BA) // text/quiet — eyebrow, weekend dates

// Surfaces — spec v1.0 §2. Note: surface/base is WHITE, not gray.
// The app background is pure white; cards pick up the raised #F9FAFB tint.
val KlikPaperApp   = Color(0xFFFFFFFF) // surface/base — main app bg
val KlikPaperCard  = Color(0xFFFFFFFF) // surface/base — card surface on gray pages
val KlikPaperSoft  = Color(0xFFF9FAFB) // surface/raised — session cards, raised cards
val KlikPaperFocal = Color(0xFFF6F7F9) // surface/focal — today's primary session card
val KlikPaperChip  = Color(0xFFF4F5F7) // surface/chip — chips, segmented track, inputs

// Lines & dividers — spec v1.0 §2.
val KlikLineHairline = Color(0xFFE8E9EC) // divider/default — card borders, sub-tab underline
val KlikLineSoft     = Color(0xFFEDEEF1) // divider/primary — tab bar top border
val KlikLineMute     = Color(0xFFD4D6DB) // sheet grabber + misc
val KlikLineTick     = Color(0xFFC8C9CD) // icon/disabled — unchecked checkboxes, empty-state icons

// Signal — decision (amber), commitment (green), risk (pink), alert, warn, running.
val KlikDecisionBg      = Color(0xFFFAEEDA)
val KlikDecisionText    = Color(0xFF412402)
val KlikDecisionSubtext = Color(0xFF854F0B)
val KlikDecisionAccent  = Color(0xFFBA7517)

val KlikCommitmentBg      = Color(0xFFE1F5EE)
val KlikCommitmentText    = Color(0xFF04342C)      // teal/ink — body inside teal fills
val KlikCommitmentSubtext = Color(0xFF085041)      // teal/label — inline label ("COMMITMENT")
val KlikCommitmentStatus  = Color(0xFF0F6E56)      // teal/text — status label "COMPLETED" on white
val KlikCommitmentAccent  = Color(0xFF1D9E75)

val KlikRiskBg      = Color(0xFFFBEAF0)
val KlikRiskText    = Color(0xFF4B1528)
val KlikRiskSubtext = Color(0xFF993556)
val KlikRiskAccent  = Color(0xFF993556)

val KlikAlert   = Color(0xFFD85A30)
val KlikWarn    = Color(0xFFD4A23C)
val KlikRunning = Color(0xFF1D9E75)

// Avatar palette — deterministic by initials hash.
val KlikAvatarBg = listOf(
    Color(0xFFE2DBF3), Color(0xFFF5DDE2), Color(0xFFD4EDE0), Color(0xFFFBE8D2), Color(0xFFDCEAF6)
)
val KlikAvatarFg = listOf(
    Color(0xFF6E62B6), Color(0xFFB3637A), Color(0xFF4A8B65), Color(0xFFA07840), Color(0xFF6E9DC0)
)

// Entity dot colors for mentioned chips.
val KlikDotPerson  = Color(0xFFA8ABB2)
val KlikDotProject = Color(0xFF7F77DD)
val KlikDotOrg     = Color(0xFF1D9E75)

// ─── Legacy-compatible names (repointed to Klik One) ──────────────────────
// Kept so every existing screen compiles with zero edits.
val KlikPrimary    = KlikInkPrimary
val KlikBackground = KlikPaperApp
val KlikSurface    = KlikPaperCard

val StriveColor = KlikDotPerson   // was: people orange
val EngageColor = KlikDotProject  // was: orgs purple
val LiveColor   = KlikDotOrg      // was: projects teal

val KlikBlack     = KlikInkPrimary
val KlikDarkGrey  = KlikInkSecondary
val KlikLightGrey = KlikPaperChip

// Flattened "liquid" gradient tokens — now solid ink on paper.
val LiquidGradientStart      = KlikInkPrimary
val LiquidGradientEnd        = KlikInkPrimary
val LiquidBackgroundBrush    = listOf(KlikPaperApp, KlikPaperApp)

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
    private val inkBrush = Brush.linearGradient(listOf(KlikInkPrimary, KlikInkPrimary))
    val Default          = inkBrush
    val SunriseGlow      = inkBrush
    val MorningMist      = inkBrush
    val SoftDawn         = inkBrush
    val Ethereal         = inkBrush
    val OceanBreeze      = inkBrush
    val LavenderDream    = inkBrush
    val GoldenHour       = inkBrush
    val MintFresh        = inkBrush
    val RoseQuartz       = inkBrush
    val PeachVanilla     = inkBrush
    val LavenderBlush    = inkBrush
    val AliceBlue        = inkBrush
    val LightBlue        = inkBrush
    val LightGreen       = inkBrush
    val LightOrange      = inkBrush
    val LightPink        = inkBrush
    val LightPurple      = inkBrush
    val LightYellow      = inkBrush
    val LightCyan        = inkBrush
    val OffWhite         = inkBrush
    val LightIndigo      = inkBrush
    val DeepOrangeTint   = inkBrush
    val LightGreenTint   = inkBrush
    val AmberTint        = inkBrush
    val BlueGreyTint     = inkBrush
    val LimeTint         = inkBrush
    val BrownTint        = inkBrush
    val Pink100          = inkBrush
    val DeepPurple100    = inkBrush
    val Indigo100        = inkBrush
}

fun getKlikAccentGradient(backgroundIndex: Int): Brush = KlikAccentGradients.Default
fun getKlikPrimaryColor(backgroundIndex: Int): Color = KlikInkPrimary

// ─── Insight card tints — muted Klik One signal bg tones ──────────────────
// Calendar → warm decision amber; Function → project purple; Growth → commitment green.
data class InsightCardColors(
    val calendarTint: Color,
    val functionTint: Color,
    val growthTint: Color
)

private val klikOneInsightTints = InsightCardColors(
    calendarTint = KlikDecisionBg,
    functionTint = KlikDotProject.copy(alpha = 0.16f),
    growthTint   = KlikCommitmentBg
)

fun getInsightCardColors(backgroundIndex: Int): InsightCardColors = klikOneInsightTints

// ─── Background options — all resolve to Klik One paper ───────────────────
// The picker stays functional (names preserved) but every option paints
// #EFEFF3. No more peach/blue/lavender gradients — pure editorial paper.
val BackgroundOptions: List<BackgroundOption> = listOf(
    BackgroundOption.ColorBackground(KlikPaperApp, "Klik Paper"),
    BackgroundOption.ColorBackground(KlikPaperApp, "Sunrise Glow"),
    BackgroundOption.ColorBackground(KlikPaperApp, "Morning Mist"),
    BackgroundOption.ColorBackground(KlikPaperApp, "Soft Dawn"),
    BackgroundOption.ColorBackground(KlikPaperApp, "Ethereal"),
    BackgroundOption.ColorBackground(KlikPaperApp, "Ocean Breeze"),
    BackgroundOption.ColorBackground(KlikPaperApp, "Lavender Dream"),
    BackgroundOption.ColorBackground(KlikPaperApp, "Golden Hour"),
    BackgroundOption.ColorBackground(KlikPaperApp, "Mint Fresh"),
    BackgroundOption.ColorBackground(KlikPaperApp, "Rose Quartz"),
    BackgroundOption.ColorBackground(KlikPaperApp, "Peach Vanilla"),
    BackgroundOption.ColorBackground(KlikPaperApp, "Lavender Blush"),
    BackgroundOption.ColorBackground(KlikPaperApp, "Alice Blue"),
    BackgroundOption.ColorBackground(KlikPaperApp, "Light Blue"),
    BackgroundOption.ColorBackground(KlikPaperApp, "Light Green"),
    BackgroundOption.ColorBackground(KlikPaperApp, "Light Orange"),
    BackgroundOption.ColorBackground(KlikPaperApp, "Light Pink"),
    BackgroundOption.ColorBackground(KlikPaperApp, "Light Purple"),
    BackgroundOption.ColorBackground(KlikPaperApp, "Light Yellow"),
    BackgroundOption.ColorBackground(KlikPaperApp, "Light Cyan"),
    BackgroundOption.ColorBackground(KlikPaperApp, "Off White"),
    BackgroundOption.ColorBackground(KlikPaperApp, "Light Indigo"),
    BackgroundOption.ColorBackground(KlikPaperApp, "Deep Orange"),
    BackgroundOption.ColorBackground(KlikPaperApp, "Sage Green"),
    BackgroundOption.ColorBackground(KlikPaperApp, "Amber"),
    BackgroundOption.ColorBackground(KlikPaperApp, "Blue Grey"),
    BackgroundOption.ColorBackground(KlikPaperApp, "Lime"),
    BackgroundOption.ColorBackground(KlikPaperApp, "Brown"),
    BackgroundOption.ColorBackground(KlikPaperApp, "Pink"),
    BackgroundOption.ColorBackground(KlikPaperApp, "Deep Purple"),
    BackgroundOption.ColorBackground(KlikPaperApp, "Indigo"),
)
