// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.ui.klikone

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.platform.LocalDensity
import io.github.fletchmckee.liquid.samples.app.platform.HapticService
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.fletchmckee.liquid.samples.app.model.hapticEnabledState
import io.github.fletchmckee.liquid.samples.app.theme.K1FontFamilyState
import io.github.fletchmckee.liquid.samples.app.theme.K1FontSizeState
import io.github.fletchmckee.liquid.samples.app.theme.KlikAlert
import io.github.fletchmckee.liquid.samples.app.theme.KlikAvatarBg
import io.github.fletchmckee.liquid.samples.app.theme.KlikAvatarFg
import io.github.fletchmckee.liquid.samples.app.theme.KlikCommitmentAccent
import io.github.fletchmckee.liquid.samples.app.theme.KlikCommitmentBg
import io.github.fletchmckee.liquid.samples.app.theme.KlikCommitmentSubtext
import io.github.fletchmckee.liquid.samples.app.theme.KlikCommitmentText
import io.github.fletchmckee.liquid.samples.app.theme.KlikDecisionAccent
import io.github.fletchmckee.liquid.samples.app.theme.KlikDecisionBg
import io.github.fletchmckee.liquid.samples.app.theme.KlikDecisionSubtext
import io.github.fletchmckee.liquid.samples.app.theme.KlikDecisionText
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkFaint
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkMuted
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkPrimary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkSecondary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkTertiary
import io.github.fletchmckee.liquid.samples.app.theme.KlikLineHairline
import io.github.fletchmckee.liquid.samples.app.theme.KlikLineSoft
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperApp
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperCard
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperChip
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperFocal
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperSoft
import io.github.fletchmckee.liquid.samples.app.theme.KlikRiskAccent
import io.github.fletchmckee.liquid.samples.app.theme.KlikRiskBg
import io.github.fletchmckee.liquid.samples.app.theme.KlikRiskSubtext
import io.github.fletchmckee.liquid.samples.app.theme.KlikRiskText
import io.github.fletchmckee.liquid.samples.app.theme.KlikRunning
import io.github.fletchmckee.liquid.samples.app.theme.KlikWarn
import kotlin.math.abs

// ─── Ripple-less click helper ─────────────────────────────────────────────
// K1 is an editorial system: we never flash a grey rounded rectangle behind
// a tap. Every interactive surface uses this instead of raw .clickable, which
// would otherwise drop a default Compose indication under the finger.
@Composable
fun Modifier.k1Clickable(enabled: Boolean = true, onClick: () -> Unit): Modifier {
  val interaction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
  return this.clickable(
    interactionSource = interaction,
    indication = null,
    enabled = enabled,
    onClick = onClick,
  )
}

// k1LongClickable — same editorial no-ripple behaviour as k1Clickable, but
// also fires on long-press. Used by entity-detail screens to expose the
// rename / correct-feedback affordances the old liquidglass app shipped.
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun Modifier.k1LongClickable(
  enabled: Boolean = true,
  onClick: () -> Unit = {},
  onLongClick: () -> Unit,
): Modifier {
  val interaction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
  return this.combinedClickable(
    interactionSource = interaction,
    indication = null,
    enabled = enabled,
    onClick = onClick,
    onLongClick = onLongClick,
  )
}

/**
 * Edge-swipe-to-back gesture. Mimics iOS's interactive-pop: starting a drag
 * within the leading 32 dp of the surface and dragging right past 80 dp /
 * faster than a small flick fires [onBack]. Vertical drift is allowed so
 * the user doesn't fight a perfectly-horizontal contract while scrolling
 * a list. We use [androidx.compose.ui.input.pointer.pointerInput] with
 * `awaitPointerEventScope` so the gesture cooperates with vertical scroll
 * children — only consumed once we cross the activation threshold.
 */
fun Modifier.k1SwipeBack(onBack: () -> Unit): Modifier = this.pointerInput(onBack) {
  val edgeStartPx = 32.dp.toPx()
  val activatePx = 80.dp.toPx()
  awaitPointerEventScope {
    while (true) {
      val down = awaitPointerEvent(androidx.compose.ui.input.pointer.PointerEventPass.Initial)
        .changes.firstOrNull { it.pressed && !it.previousPressed } ?: continue
      if (down.position.x > edgeStartPx) continue
      var totalDx = 0f
      var totalDy = 0f
      var consumed = false
      while (true) {
        val ev = awaitPointerEvent(androidx.compose.ui.input.pointer.PointerEventPass.Initial)
        val change = ev.changes.firstOrNull { it.id == down.id } ?: break
        totalDx += change.positionChange().x
        totalDy += change.positionChange().y
        if (!consumed && totalDx > activatePx && totalDx > abs(totalDy) * 1.2f) {
          onBack()
          change.consume()
          consumed = true
          break
        }
        if (!change.pressed) break
      }
    }
  }
}

// ─── Type scale ───────────────────────────────────────────────────────────
// Matches klik_design_spec.html v1.0 §3. Only FontWeight.Normal (400) and
// FontWeight.Medium (500) are used anywhere — never 600 or 700.
//
// Each style's `fontFamily` is `K1FontFamilyState.value` and its color is
// drawn from `K1PaletteState.value` (see K1Theme.kt). Both are
// mutableStateOf — Compose observes reads automatically, so any composable
// using `K1Type.bodyMd` recomposes when the user flips dark mode or picks
// a new font. The vals themselves are plain Kotlin getters (no @Composable)
// so non-Composable callers still resolve at call time.
object K1Type {
  // type/display — marketing / onboarding hero; not in the spec type table
  val display: TextStyle get() {
    val s = K1FontSizeState.value
    return TextStyle(fontFamily = K1FontFamilyState.value, fontSize = (32 * s).sp, fontWeight = FontWeight.Medium, letterSpacing = (-0.8).sp, lineHeight = (32 * s * 1.2f).sp, color = KlikInkPrimary)
  }

  // type/xxl — page title ("Today") — 28 / 500 / -0.8 / 1.2
  val h1: TextStyle get() {
    val s = K1FontSizeState.value
    return TextStyle(fontFamily = K1FontFamilyState.value, fontSize = (28 * s).sp, fontWeight = FontWeight.Medium, letterSpacing = (-0.8).sp, lineHeight = (28 * s * 1.2f).sp, color = KlikInkPrimary)
  }

  // h2 — non-spec; kept as a deliberate mid-size for sub-page titles
  val h2: TextStyle get() {
    val s = K1FontSizeState.value
    return TextStyle(fontFamily = K1FontFamilyState.value, fontSize = (22 * s).sp, fontWeight = FontWeight.Medium, letterSpacing = (-0.4).sp, lineHeight = (22 * s * 1.3f).sp, color = KlikInkPrimary)
  }

  // type/xl — meeting title, detail hero — 19 / 500 / -0.3 / 1.25
  val h3: TextStyle get() {
    val s = K1FontSizeState.value
    return TextStyle(fontFamily = K1FontFamilyState.value, fontSize = (19 * s).sp, fontWeight = FontWeight.Medium, letterSpacing = (-0.3).sp, lineHeight = (19 * s * 1.25f).sp, color = KlikInkPrimary)
  }

  // type/lg — card title ("Investor call") — 15 / 500 / 0 / 1.4
  val cardTitle: TextStyle get() {
    val s = K1FontSizeState.value
    return TextStyle(fontFamily = K1FontFamilyState.value, fontSize = (15 * s).sp, fontWeight = FontWeight.Medium, lineHeight = (15 * s * 1.4f).sp, color = KlikInkPrimary)
  }

  // type/base — body — 13 / 400 / 0 / 1.55
  val bodySm: TextStyle get() {
    val s = K1FontSizeState.value
    return TextStyle(fontFamily = K1FontFamilyState.value, fontSize = (13 * s).sp, fontWeight = FontWeight.Normal, lineHeight = (13 * s * 1.55f).sp, color = KlikInkPrimary)
  }

  // type/base-500 — row primary, list item title
  val bodyMd: TextStyle get() {
    val s = K1FontSizeState.value
    return TextStyle(fontFamily = K1FontFamilyState.value, fontSize = (13 * s).sp, fontWeight = FontWeight.Medium, lineHeight = (13 * s * 1.4f).sp, color = KlikInkPrimary)
  }

  // body 14 — kept for long-form reading blocks that benefit from a hair more size
  val body: TextStyle get() {
    val s = K1FontSizeState.value
    return TextStyle(fontFamily = K1FontFamilyState.value, fontSize = (14 * s).sp, fontWeight = FontWeight.Normal, lineHeight = (14 * s * 1.55f).sp, color = KlikInkPrimary)
  }

  // type/sm — 12 / 400 / 0 / 1.55 — sub-tab + inline captions on white
  val caption: TextStyle get() {
    val s = K1FontSizeState.value
    return TextStyle(fontFamily = K1FontFamilyState.value, fontSize = (12 * s).sp, fontWeight = FontWeight.Normal, lineHeight = (12 * s * 1.55f).sp, color = KlikInkTertiary)
  }

  // type/xs — 11 / 400 / 0 / 1.5 — chip text, row meta
  val meta: TextStyle get() {
    val s = K1FontSizeState.value
    return TextStyle(fontFamily = K1FontFamilyState.value, fontSize = (11 * s).sp, fontWeight = FontWeight.Normal, lineHeight = (11 * s * 1.5f).sp, color = KlikInkTertiary)
  }

  // type/xxs — 10 / 400 / 0 / 1.5 — timestamps
  val metaSm: TextStyle get() {
    val s = K1FontSizeState.value
    return TextStyle(fontFamily = K1FontFamilyState.value, fontSize = (10 * s).sp, fontWeight = FontWeight.Normal, lineHeight = (10 * s * 1.5f).sp, color = KlikInkMuted)
  }

  // type/label — 9 / 500 / 0.8 — UPPERCASE section labels ("IN 3 LINES")
  val eyebrow: TextStyle get() {
    val s = K1FontSizeState.value
    return TextStyle(fontFamily = K1FontFamilyState.value, fontSize = (9 * s).sp, fontWeight = FontWeight.Medium, letterSpacing = 0.8.sp, color = KlikInkTertiary)
  }

  // type/eyebrow — 11 / 500 / 1.2 — KLIK ONE brand eyebrow
  val eyebrowLg: TextStyle get() {
    val s = K1FontSizeState.value
    return TextStyle(fontFamily = K1FontFamilyState.value, fontSize = (11 * s).sp, fontWeight = FontWeight.Medium, letterSpacing = 1.2.sp, color = KlikInkFaint)
  }

  val timer: TextStyle get() {
    val s = K1FontSizeState.value
    return TextStyle(fontFamily = K1FontFamilyState.value, fontSize = (36 * s).sp, fontWeight = FontWeight.Medium, letterSpacing = (-1).sp, color = KlikInkPrimary)
  }
}

// ─── Spacing ──────────────────────────────────────────────────────────────
// Matches spec §4 — space/1 … space/14. Do not invent new values mid-build.
object K1Sp {
  val xxs = 4.dp // space/1 — icon→text micro gap
  val xs = 6.dp // space/2 — between chips
  val s = 8.dp // space/3 — between cards in a list
  val sm = 10.dp // space/4 — sub-elements within a card
  val m = 12.dp // space/5 — card vertical padding, sibling sections
  val md = 14.dp // space/6 — card horizontal padding
  val lg = 16.dp // space/7 — between distinct UI blocks
  val xl = 20.dp // space/8 — page margin, between major sections
  val xll = 22.dp // space/9 — between section groups on long pages
  val xxl = 24.dp // space/10 — below page header
  val xxxl = 32.dp // space/12 — between hero sections in onboarding
  val huge = 48.dp // space/14 — above bottom CTA in onboarding
}

// ─── Radii ────────────────────────────────────────────────────────────────
// Matches spec §5. Tight, crisp corners; avoid mixing random values.
object K1R {
  val xs = RoundedCornerShape(5.dp) // radius/xs — small inline buttons (Play)
  val chip = RoundedCornerShape(7.dp) // radius/sm — chips, small pills, secondary buttons
  val inline = RoundedCornerShape(8.dp) // radius/md — inline Decision / Commitment / stat cards
  val soft = RoundedCornerShape(10.dp) // radius/lg — raised cards, segmented control
  val card = RoundedCornerShape(12.dp) // radius/xl — primary CTA, major cards
  val tight = RoundedCornerShape(14.dp) // radius/2xl — live recording waveform container, option cards
  val sheet = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp) // radius/sheet
  val pill = RoundedCornerShape(999.dp) // radius/full — circular
}

// ─── Waveform mark (static + animated) ────────────────────────────────────
private val staticHeights = listOf(12f, 20f, 8f, 16f, 10f)

/** Static brand waveform — 4-5 vertical bars. */
@Composable
fun K1Waveform(
  modifier: Modifier = Modifier,
  heights: List<Float> = staticHeights,
  barWidth: Dp = 3.dp,
  gap: Dp = 3.dp,
  color: Color = KlikInkPrimary,
) {
  val maxH = heights.maxOrNull() ?: 20f
  val intrinsicWidth = barWidth * heights.size + gap * (heights.size - 1).coerceAtLeast(0)
  Canvas(modifier = Modifier.width(intrinsicWidth).height(maxH.dp).then(modifier)) {
    val bw = barWidth.toPx()
    val g = gap.toPx()
    heights.forEachIndexed { i, h ->
      val x = i * (bw + g)
      val y = size.height - h.dp.toPx()
      drawRoundRect(
        color = color,
        topLeft = androidx.compose.ui.geometry.Offset(x, y),
        size = Size(bw, h.dp.toPx()),
        cornerRadius = CornerRadius(bw / 2f, bw / 2f),
      )
    }
  }
}

/** Animated "live" waveform — 5 bars pulsing independently. */
@Composable
fun K1WaveformLive(
  modifier: Modifier = Modifier,
  color: Color = KlikInkPrimary,
  barWidth: Dp = 3.dp,
  gap: Dp = 3.dp,
) {
  val infinite = rememberInfiniteTransition(label = "wave")
  val phases = listOf(1200, 1000, 1400, 900, 1300)
  val anims = phases.mapIndexed { i, d ->
    infinite.animateFloat(
      initialValue = 0.3f,
      targetValue = 1f,
      animationSpec = infiniteRepeatable(tween(d), RepeatMode.Reverse),
      label = "w$i",
    )
  }
  val bases = listOf(22f, 36f, 28f, 44f, 24f)
  val intrinsicWidth = barWidth * 5 + gap * 4
  Canvas(modifier = Modifier.width(intrinsicWidth).height(44.dp).then(modifier)) {
    val bw = barWidth.toPx()
    val g = gap.toPx()
    anims.forEachIndexed { i, a ->
      val h = (bases[i] * a.value).coerceAtLeast(6f)
      val x = i * (bw + g)
      val y = (size.height - h.dp.toPx()) / 2f + (size.height - h.dp.toPx()) / 2f
      val yTop = (size.height - h) / 2f
      drawRoundRect(
        color = color,
        topLeft = androidx.compose.ui.geometry.Offset(x, yTop),
        size = Size(bw, h),
        cornerRadius = CornerRadius(bw / 2f, bw / 2f),
      )
    }
  }
}

/** Recording dot — pulsing alert-orange circle. */
@Composable
fun K1RecDot(modifier: Modifier = Modifier, color: Color = KlikAlert, size: Dp = 8.dp) {
  val infinite = rememberInfiniteTransition(label = "rec")
  val a by infinite.animateFloat(
    initialValue = 1f,
    targetValue = 0.35f,
    animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
    label = "recA",
  )
  Box(
    modifier
      .size(size)
      .background(color.copy(alpha = a), CircleShape),
  )
}

/**
 * Processing-in-flight banner — paper card with an indeterminate ink stripe
 * and the current pipeline stage. Use on TodayScreen right after a session
 * stops so the user can see: Transcribing → Analyzing → Tasks → Finalizing.
 */
@Composable
fun K1ProcessingBanner(
  stage: String,
  elapsedLabel: String = "",
  progressFraction: Float? = null,
  modifier: Modifier = Modifier,
  onTap: (() -> Unit)? = null,
) {
  // Indeterminate shimmer used when we don't yet have a real % from the
  // backend. Once we do, we draw a true filled bar instead.
  val infinite = rememberInfiniteTransition(label = "proc")
  val shimmer by infinite.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(tween(1600)),
    label = "shimmer",
  )
  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .background(KlikPaperCard)
      .border(0.75.dp, KlikLineHairline, RoundedCornerShape(14.dp))
      .then(if (onTap != null) Modifier.k1Clickable(onClick = onTap) else Modifier)
      .padding(horizontal = 16.dp, vertical = 14.dp),
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      K1Eyebrow("Processing")
      Spacer(Modifier.weight(1f))
      val right = buildString {
        if (progressFraction != null) {
          append((progressFraction * 100).toInt()).append('%')
          if (elapsedLabel.isNotBlank()) append(" · ")
        }
        append(elapsedLabel)
      }
      if (right.isNotBlank()) {
        Text(right, style = K1Type.metaSm.copy(color = KlikInkTertiary))
      }
    }
    Spacer(Modifier.height(8.dp))
    Text(stage, style = K1Type.bodyMd.copy(color = KlikInkPrimary))
    Spacer(Modifier.height(10.dp))
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(2.dp)
        .background(KlikLineHairline),
    ) {
      if (progressFraction != null) {
        Box(
          Modifier
            .fillMaxWidth(progressFraction.coerceIn(0f, 1f))
            .height(2.dp)
            .background(KlikInkPrimary),
        )
      } else {
        Box(
          Modifier
            .fillMaxWidth(0.28f)
            .offset(x = ((-0.28f + shimmer * 1.28f) * 360).dp)
            .height(2.dp)
            .background(KlikInkPrimary),
        )
      }
    }
  }
}

/**
 * Recording-started banner — paper card, hairline border, pulsing alert dot,
 * K1 editorial type. Auto-dismisses after [durationMillis].
 */
@Composable
fun K1RecordingBanner(
  visible: Boolean,
  title: String,
  subtitle: String,
  durationMillis: Long = 4000L,
  onDismiss: () -> Unit,
) {
  androidx.compose.runtime.LaunchedEffect(visible) {
    if (visible) {
      kotlinx.coroutines.delay(durationMillis)
      onDismiss()
    }
  }
  androidx.compose.animation.AnimatedVisibility(
    visible = visible,
    enter = androidx.compose.animation.slideInVertically(
      initialOffsetY = { -it },
      animationSpec = tween(280),
    ) + androidx.compose.animation.fadeIn(tween(200)),
    exit = androidx.compose.animation.slideOutVertically(
      targetOffsetY = { -it },
      animationSpec = tween(220),
    ) + androidx.compose.animation.fadeOut(tween(180)),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(14.dp))
        .background(KlikPaperCard)
        .border(0.75.dp, KlikLineHairline, RoundedCornerShape(14.dp))
        .k1Clickable(onClick = onDismiss)
        .padding(horizontal = 16.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      K1RecDot(size = 10.dp)
      Column(Modifier.weight(1f)) {
        Text(title, style = K1Type.bodyMd.copy(color = KlikInkPrimary))
        Spacer(Modifier.height(2.dp))
        Text(subtitle, style = K1Type.metaSm.copy(color = KlikInkTertiary))
      }
    }
  }
}

// ─── Eyebrow ──────────────────────────────────────────────────────────────
@Composable
fun K1Eyebrow(text: String, modifier: Modifier = Modifier, large: Boolean = false) {
  Text(text.uppercase(), style = if (large) K1Type.eyebrowLg else K1Type.eyebrow, modifier = modifier)
}

// ─── Section header (● SECTION · N ─── [trailing]) ───────────────────────
@Composable
fun K1SectionHeader(
  label: String,
  count: Int? = null,
  dotColor: Color? = null,
  trailing: @Composable (() -> Unit)? = null,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier.fillMaxWidth().padding(vertical = K1Sp.xs),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    if (dotColor != null) {
      Box(Modifier.size(5.dp).background(dotColor, CircleShape))
      Spacer(Modifier.width(K1Sp.s))
    }
    Text(
      buildString {
        append(label.uppercase())
        if (count != null) append(" · $count")
      },
      style = K1Type.eyebrow.copy(color = KlikInkPrimary, fontWeight = FontWeight.Medium),
    )
    Spacer(Modifier.width(K1Sp.s))
    Box(Modifier.weight(1f).height(0.5.dp).background(KlikLineHairline))
    if (trailing != null) {
      Spacer(Modifier.width(K1Sp.s))
      trailing()
    }
  }
}

// ─── Chip ─────────────────────────────────────────────────────────────────
@Composable
fun K1Chip(
  label: String,
  selected: Boolean = false,
  onClick: (() -> Unit)? = null,
  onLongClick: (() -> Unit)? = null,
  leading: @Composable (() -> Unit)? = null,
  modifier: Modifier = Modifier,
) {
  val bg = if (selected) KlikInkPrimary else KlikPaperChip
  val fg = if (selected) KlikPaperCard else KlikInkSecondary
  val gestureMod = when {
    onLongClick != null -> Modifier.k1LongClickable(
      onClick = onClick ?: {},
      onLongClick = onLongClick,
    )
    onClick != null -> Modifier.k1Clickable(onClick = onClick)
    else -> Modifier
  }
  Row(
    modifier
      .clip(K1R.chip)
      .background(bg)
      .then(gestureMod)
      .padding(horizontal = 9.dp, vertical = 3.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(5.dp),
  ) {
    if (leading != null) leading()
    Text(label, style = K1Type.meta.copy(color = fg, fontSize = 11.sp))
  }
}

// ─── Buttons ──────────────────────────────────────────────────────────────
@Composable
fun K1ButtonPrimary(
  label: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  pill: Boolean = false,
) {
  val shape = if (pill) K1R.pill else K1R.card
  val bg = if (enabled) KlikInkPrimary else KlikLineHairline
  val fg = if (enabled) KlikPaperCard else KlikInkMuted
  Box(
    modifier
      .clip(shape)
      .background(bg)
      .k1Clickable(enabled = enabled, onClick = onClick)
      .padding(horizontal = 20.dp, vertical = 14.dp),
    contentAlignment = Alignment.Center,
  ) { Text(label, style = K1Type.bodyMd.copy(color = fg, fontSize = 14.sp)) }
}

@Composable
fun K1ButtonSecondary(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
  Box(
    modifier
      .clip(K1R.card)
      .border(0.5.dp, KlikInkMuted, K1R.card)
      .k1Clickable(onClick = onClick)
      .padding(horizontal = 20.dp, vertical = 14.dp),
    contentAlignment = Alignment.Center,
  ) { Text(label, style = K1Type.bodyMd.copy(color = KlikInkPrimary, fontSize = 14.sp)) }
}

@Composable
fun K1ButtonGhost(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
  Box(
    modifier.k1Clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 8.dp),
    contentAlignment = Alignment.Center,
  ) { Text(label, style = K1Type.bodyMd.copy(color = KlikInkTertiary, fontSize = 13.sp)) }
}

// ─── Cards ────────────────────────────────────────────────────────────────

/**
 * K1 card surface. Spec v1.0 §7 card rules:
 *  - Raised card (default) — `#F9FAFB`, radius 10, padding 14.
 *  - Focal card (`focal = true`) — `#F6F7F9`, radius 14, padding 18. Used for
 *    the primary/current session card on Today.
 *  - Borderless. The raised tint gives depth against the white app bg; we don't
 *    add hairline borders per spec.
 *
 * `soft` is kept as an alias of the default raised surface for back-compat with
 * early K1 screens.
 */
@Composable
fun K1Card(
  modifier: Modifier = Modifier,
  soft: Boolean = true,
  focal: Boolean = false,
  onClick: (() -> Unit)? = null,
  onLongClick: (() -> Unit)? = null,
  content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
  val bg = if (focal) KlikPaperFocal else KlikPaperSoft
  val shape = if (focal) K1R.tight else K1R.soft
  val pad = if (focal) 18.dp else 14.dp
  val gestureMod = when {
    onLongClick != null -> Modifier.k1LongClickable(
      onClick = onClick ?: {},
      onLongClick = onLongClick,
    )
    onClick != null -> Modifier.k1Clickable(onClick = onClick)
    else -> Modifier
  }
  Column(
    modifier
      .clip(shape)
      .background(bg)
      .then(gestureMod)
      .padding(pad),
    content = content,
  )
}

// ─── Skeleton (lazy / breathing) loaders ──────────────────────────────────
// A single shimmering bar — used to compose larger skeleton blocks. Driven
// by a 1300ms in/out tween so the page feels alive while real data lands.
// Pass `alpha` explicitly when used inside K1SkeletonCard so all lines share
// one transition and pulse in sync rather than independently.
@Composable
fun K1SkeletonLine(
  modifier: Modifier = Modifier,
  width: Dp? = null,
  height: Dp = 12.dp,
  shape: RoundedCornerShape = K1R.pill,
  alpha: Float? = null,
) {
  val a = if (alpha != null) {
    alpha
  } else {
    val infinite = rememberInfiniteTransition(label = "skel")
    val v by infinite.animateFloat(
      initialValue = 0.35f,
      targetValue = 0.75f,
      animationSpec = infiniteRepeatable(tween(1300), RepeatMode.Reverse),
      label = "skelA",
    )
    v
  }
  Box(
    modifier
      .then(if (width != null) Modifier.width(width) else Modifier.fillMaxWidth())
      .height(height)
      .clip(shape)
      .background(KlikLineHairline.copy(alpha = a)),
  )
}

/**
 * Skeleton card matching K1Card's surface — three breathing lines (~85%, 100%,
 * 60%) inside a rounded paper-soft container. Use everywhere we'd otherwise
 * render an empty area while waiting on the network / LLM.
 *
 * One shared InfiniteTransition drives all lines so they pulse in sync.
 */
@Composable
fun K1SkeletonCard(
  modifier: Modifier = Modifier,
  lines: Int = 3,
) {
  val infinite = rememberInfiniteTransition(label = "skel-card")
  val a by infinite.animateFloat(
    initialValue = 0.35f,
    targetValue = 0.75f,
    animationSpec = infiniteRepeatable(tween(1300), RepeatMode.Reverse),
    label = "skelA",
  )
  Column(
    modifier
      .fillMaxWidth()
      .clip(K1R.soft)
      .background(KlikPaperSoft)
      .padding(14.dp),
  ) {
    val widths = listOf(0.85f, 1f, 0.6f, 0.9f, 0.5f)
    repeat(lines) { idx ->
      val frac = widths[idx % widths.size]
      Box(Modifier.fillMaxWidth(frac)) { K1SkeletonLine(alpha = a) }
      if (idx < lines - 1) Spacer(Modifier.height(8.dp))
    }
  }
}

/**
 * Expandable card — collapses long body text behind a "Show more" affordance.
 * Tap anywhere on the card to toggle. Animates with the K1 tween (220ms),
 * truncating to [collapsedMaxLines] when collapsed (default 3 lines).
 */
@Composable
fun K1ExpandableCard(
  modifier: Modifier = Modifier,
  soft: Boolean = true,
  focal: Boolean = false,
  initiallyExpanded: Boolean = false,
  content: @Composable androidx.compose.foundation.layout.ColumnScope.(expanded: Boolean) -> Unit,
) {
  var expanded by remember { mutableStateOf(initiallyExpanded) }
  val bg = if (focal) KlikPaperFocal else KlikPaperSoft
  val shape = if (focal) K1R.tight else K1R.soft
  val pad = if (focal) 18.dp else 14.dp
  Column(
    modifier
      .animateContentSize(tween(220))
      .clip(shape)
      .background(bg)
      .k1Clickable { expanded = !expanded }
      .padding(pad),
  ) {
    content(this, expanded)
    Spacer(Modifier.height(K1Sp.s))
    Text(
      if (expanded) "Show less" else "Show more",
      style = K1Type.meta.copy(color = KlikInkSecondary, fontWeight = FontWeight.Medium),
    )
  }
}

/** Signal callout card — decision / commitment / risk. */
enum class K1Signal { Decision, Commitment, Risk }

@Composable
fun K1SignalCard(
  signal: K1Signal,
  eyebrow: String,
  body: String,
  modifier: Modifier = Modifier,
  timeLabel: String? = null,
) {
  val (bg, text, sub, accent) = when (signal) {
    K1Signal.Decision -> Quad(KlikDecisionBg, KlikDecisionText, KlikDecisionSubtext, KlikDecisionAccent)
    K1Signal.Commitment -> Quad(KlikCommitmentBg, KlikCommitmentText, KlikCommitmentSubtext, KlikCommitmentAccent)
    K1Signal.Risk -> Quad(KlikRiskBg, KlikRiskText, KlikRiskSubtext, KlikRiskAccent)
  }
  Row(
    modifier
      .clip(K1R.soft)
      .background(bg)
      .then(
        if (signal == K1Signal.Decision) {
          Modifier.border(
            width = 0.dp,
            color = Color.Transparent,
            shape = K1R.soft,
          )
        } else {
          Modifier
        },
      )
      .padding(start = if (signal == K1Signal.Decision) 10.dp else 12.dp, top = 10.dp, end = 12.dp, bottom = 10.dp),
  ) {
    if (signal == K1Signal.Decision) {
      Box(Modifier.width(2.dp).background(accent))
      Spacer(Modifier.width(8.dp))
    }
    Column(Modifier.weight(1f)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(eyebrow.uppercase(), style = K1Type.eyebrow.copy(color = sub, fontSize = 9.sp, letterSpacing = 0.5.sp))
        if (timeLabel != null) {
          Spacer(Modifier.width(6.dp))
          Text("· $timeLabel", style = K1Type.meta.copy(color = sub, fontSize = 10.sp))
        }
      }
      Spacer(Modifier.height(4.dp))
      Text(body, style = K1Type.bodySm.copy(color = text, fontSize = 12.sp, lineHeight = 18.sp))
    }
  }
}

private data class Quad(val a: Color, val b: Color, val c: Color, val d: Color)

// ─── Avatar ───────────────────────────────────────────────────────────────
//
// Single rule for avatar colors across the app: the palette index is a
// function of a STABLE id (person.id, project.id, org.id, user.id) and never
// of the display name or initials. Renaming a person must never change their
// color, and the same person rendered in two surfaces (stacked avatar +
// expanded chip + transcript row + detail header) must always paint the
// same. Initials remain only as the *display* text inside the disc.
//
// All avatar-coloured surfaces — full circles, chip leading dots, transcript
// bubbles, project lead chips — must obtain their (bg, fg) pair through
// k1AvatarColors(idSeed). The legacy `display.hashCode() % palette.size`
// pattern is banned.

/** One seed for one rendered avatar — initials are display-only; idSeed
 *  drives the colour. avatarUrl, when present, replaces the initials disc
 *  with the uploaded image via [AvatarImage] (Coil 3). Use [k1Seed] helpers
 *  to construct from domain types. */
data class K1AvatarSeed(val initials: String, val idSeed: String?, val avatarUrl: String? = null)

/**
 * Deterministic avatar palette index. Spec v1.0 §6 — hash the person's
 * **canonical ID** (not display name — names change, ids don't). Callers that
 * have no id and pass only initials still get a stable color for that spelling,
 * but two surfaces seeded with different inputs WILL diverge, which is the
 * bug we want to make impossible everywhere we control.
 */
internal fun paletteIdx(seed: String): Int {
  if (seed.isEmpty()) return 0
  val n = seed.fold(0) { a, ch -> (a * 31 + ch.code) }
  return abs(n) % KlikAvatarBg.size
}

/** Public colour pair lookup — every non-`K1Avatar` surface that paints
 *  an avatar-coloured dot (chip leading icon, transcript marker, etc.)
 *  MUST go through this so the same seed always picks the same swatch. */
fun k1AvatarColors(idSeed: String?): Pair<Color, Color> {
  val i = paletteIdx(idSeed?.takeIf { it.isNotBlank() } ?: "")
  return KlikAvatarBg[i] to KlikAvatarFg[i]
}

@Composable
fun K1Avatar(
  initials: String,
  size: Dp = 36.dp,
  modifier: Modifier = Modifier,
  /** Person.id / Project.id / Org.id / user_id. Without this the colour is
   *  derived from initials and will not match other surfaces for the same
   *  person — pass it. */
  idSeed: String? = null,
  /** When non-blank, the avatar is rendered as the uploaded image (Coil 3)
   *  with a transparent crop into the same circular footprint. The initials
   *  disc remains the loading + error fallback so a slow network never
   *  produces an empty hole. Every small avatar in the app routes through
   *  this single path — one upload propagates everywhere. */
  avatarUrl: String? = null,
) {
  val (bg, fg) = k1AvatarColors(idSeed?.takeIf { it.isNotBlank() } ?: initials)
  val fontSize = when {
    size <= 20.dp -> 9.sp
    size <= 28.dp -> 10.sp
    size <= 36.dp -> 12.sp
    size <= 48.dp -> 14.sp
    else -> 16.sp // spec §6: 56px avatar uses 16/500
  }
  if (!avatarUrl.isNullOrBlank()) {
    io.github.fletchmckee.liquid.samples.app.ui.components.AvatarImage(
      avatarUrl = avatarUrl,
      initials = initials.take(2).uppercase(),
      size = size,
      backgroundColor = bg,
      initialsColor = fg,
      initialsStyle = TextStyle(
        fontSize = fontSize,
        fontWeight = FontWeight.Medium,
        color = fg,
        textAlign = TextAlign.Center,
      ),
      modifier = modifier,
    )
    return
  }
  Box(modifier.size(size).background(bg, CircleShape), contentAlignment = Alignment.Center) {
    Text(
      initials.take(2).uppercase(),
      style = TextStyle(
        fontSize = fontSize,
        fontWeight = FontWeight.Medium,
        color = fg,
        textAlign = TextAlign.Center,
      ),
    )
  }
}

@Composable
fun K1AvatarStack(
  seeds: List<K1AvatarSeed>,
  size: Dp = 24.dp,
  modifier: Modifier = Modifier,
) {
  // Spec §6 stacking rule: overlap 30–35% with a 1.5px border matching the bg.
  // Compose's padding modifier rejects negative values on Android, so we use
  // offset to pull each subsequent avatar back over the previous one.
  Row(modifier) {
    seeds.forEachIndexed { i, seed ->
      Box(
        Modifier
          .then(if (i > 0) Modifier.offset(x = (size.value * -0.3f).dp) else Modifier)
          .size(size)
          .background(KlikPaperApp, CircleShape) // border color = bg per spec
          .padding(1.5.dp),
      ) { K1Avatar(seed.initials, size = size - 3.dp, idSeed = seed.idSeed, avatarUrl = seed.avatarUrl) }
    }
  }
}

// ─── Floating Ask button ──────────────────────────────────────────────────
// Editorial minimal: a single 4-point sparkle glyph, centered in a small black
// disc. No text, no waveform — the mark reads as "AI spark" on first glance.
@Composable
fun K1AskFab(onClick: () -> Unit, modifier: Modifier = Modifier) {
  Box(
    modifier
      .size(56.dp)
      .background(KlikInkPrimary, CircleShape)
      .k1Clickable(onClick = onClick),
    contentAlignment = Alignment.Center,
  ) {
    Canvas(Modifier.size(22.dp)) {
      val cx = size.width / 2f
      val cy = size.height / 2f
      val r = size.minDimension / 2f
      // Main vertical diamond
      val mainPath = androidx.compose.ui.graphics.Path().apply {
        moveTo(cx, cy - r)
        lineTo(cx + r * 0.22f, cy)
        lineTo(cx, cy + r)
        lineTo(cx - r * 0.22f, cy)
        close()
      }
      // Horizontal diamond (shorter, narrower — secondary arm)
      val armPath = androidx.compose.ui.graphics.Path().apply {
        moveTo(cx - r * 0.72f, cy)
        lineTo(cx, cy - r * 0.18f)
        lineTo(cx + r * 0.72f, cy)
        lineTo(cx, cy + r * 0.18f)
        close()
      }
      drawPath(mainPath, color = KlikPaperCard)
      drawPath(armPath, color = KlikPaperCard)
    }
  }
}

// ─── Bottom nav ───────────────────────────────────────────────────────────
data class K1NavItem(
  val route: String,
  val label: String,
  val iconPath: @Composable () -> Unit,
  val badgeCount: Int = 0,
)

@Composable
fun K1BottomNav(
  items: List<K1NavItem>,
  currentRoute: String,
  onSelect: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier.fillMaxWidth().background(KlikPaperCard)) {
    Box(Modifier.fillMaxWidth().height(0.5.dp).background(KlikLineSoft))
    Row(
      Modifier.fillMaxWidth().padding(top = 10.dp).navigationBarsPadding(),
      horizontalArrangement = Arrangement.SpaceAround,
      verticalAlignment = Alignment.Bottom,
    ) {
      items.forEach { item ->
        val isActive = currentRoute == item.route
        Column(
          Modifier
            .width(64.dp)
            .k1Clickable { onSelect(item.route) }
            .padding(bottom = 12.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Box {
            item.iconPath()
            if (item.badgeCount > 0) {
              val label = if (item.badgeCount > 99) "99+" else item.badgeCount.toString()
              Box(
                Modifier
                  .align(Alignment.TopEnd)
                  .offset(x = 9.dp, y = (-4).dp)
                  .heightIn(min = 14.dp)
                  .widthIn(min = 14.dp)
                  .background(KlikAlert, RoundedCornerShape(50))
                  .padding(horizontal = 4.dp, vertical = 1.dp),
                contentAlignment = Alignment.Center,
              ) {
                Text(
                  label,
                  style = TextStyle(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    color = KlikPaperCard,
                  ),
                )
              }
            }
          }
          Spacer(Modifier.height(3.dp))
          Text(
            item.label,
            style = TextStyle(
              fontSize = 9.sp,
              fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal,
              color = if (isActive) KlikInkPrimary else KlikInkMuted,
            ),
          )
        }
      }
    }
  }
}

// ─── Mini icons (line, 18dp canvas, 1.4px stroke, rounded-square language) ─
// Every nav icon is drawn on an 18×18 canvas with identical stroke width,
// all corners radius 2dp, all strokes cap=Round and join=Round. Glyphs are
// centered vertically within the 18dp canvas so every tab reads the same
// optical weight. Do not introduce a sharp corner anywhere in this block.
@Composable
fun K1IconToday(active: Boolean) {
  K1LineIcon(active) { w, c ->
    // Calendar glyph: rounded outer square + top divider + two tick marks.
    val r = 2.dp.toPx()
    drawRoundRect(
      color = c,
      style = Stroke(w, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round),
      cornerRadius = CornerRadius(r, r),
      topLeft = androidx.compose.ui.geometry.Offset(2.5.dp.toPx(), 4.dp.toPx()),
      size = Size(13.dp.toPx(), 11.5.dp.toPx()),
    )
    drawLine(
      color = c,
      strokeWidth = w,
      cap = androidx.compose.ui.graphics.StrokeCap.Round,
      start = androidx.compose.ui.geometry.Offset(3.dp.toPx(), 7.5.dp.toPx()),
      end = androidx.compose.ui.geometry.Offset(15.dp.toPx(), 7.5.dp.toPx()),
    )
    drawLine(
      color = c,
      strokeWidth = w,
      cap = androidx.compose.ui.graphics.StrokeCap.Round,
      start = androidx.compose.ui.geometry.Offset(6.5.dp.toPx(), 2.5.dp.toPx()),
      end = androidx.compose.ui.geometry.Offset(6.5.dp.toPx(), 5.dp.toPx()),
    )
    drawLine(
      color = c,
      strokeWidth = w,
      cap = androidx.compose.ui.graphics.StrokeCap.Round,
      start = androidx.compose.ui.geometry.Offset(11.5.dp.toPx(), 2.5.dp.toPx()),
      end = androidx.compose.ui.geometry.Offset(11.5.dp.toPx(), 5.dp.toPx()),
    )
  }
}

@Composable
fun K1IconMoves(active: Boolean) {
  K1LineIcon(active) { w, c ->
    // Rounded-square checkbox — same corner radius as Today. Inner check glyph
    // is centered in the square, not offset to the upper-left.
    val r = 2.dp.toPx()
    drawRoundRect(
      color = c,
      style = Stroke(w, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round),
      cornerRadius = CornerRadius(r, r),
      topLeft = androidx.compose.ui.geometry.Offset(2.5.dp.toPx(), 2.5.dp.toPx()),
      size = Size(13.dp.toPx(), 13.dp.toPx()),
    )
    drawLine(
      color = c,
      strokeWidth = w,
      cap = androidx.compose.ui.graphics.StrokeCap.Round,
      start = androidx.compose.ui.geometry.Offset(5.8.dp.toPx(), 9.3.dp.toPx()),
      end = androidx.compose.ui.geometry.Offset(8.2.dp.toPx(), 11.3.dp.toPx()),
    )
    drawLine(
      color = c,
      strokeWidth = w,
      cap = androidx.compose.ui.graphics.StrokeCap.Round,
      start = androidx.compose.ui.geometry.Offset(8.2.dp.toPx(), 11.3.dp.toPx()),
      end = androidx.compose.ui.geometry.Offset(12.2.dp.toPx(), 6.8.dp.toPx()),
    )
  }
}

@Composable
fun K1IconNetwork(active: Boolean) {
  K1LineIcon(active) { w, c ->
    // Two tangent rings centered on 9,9 — editorial "network" mark. Radii
    // sized so the pair sits dead-center in the 18dp canvas.
    val cy = 9.dp.toPx()
    drawCircle(
      color = c,
      style = Stroke(w),
      radius = 2.6.dp.toPx(),
      center = androidx.compose.ui.geometry.Offset(6.dp.toPx(), cy),
    )
    drawCircle(
      color = c,
      style = Stroke(w),
      radius = 2.6.dp.toPx(),
      center = androidx.compose.ui.geometry.Offset(12.dp.toPx(), cy),
    )
  }
}

@Composable
fun K1IconYou(active: Boolean) {
  K1LineIcon(active) { w, c ->
    // Head + shoulder arc, centered on x=9. Arc baseline sits at y=15.5
    // so the glyph has the same optical weight as the calendar/checkbox.
    drawCircle(
      color = c,
      style = Stroke(w),
      radius = 2.7.dp.toPx(),
      center = androidx.compose.ui.geometry.Offset(9.dp.toPx(), 6.2.dp.toPx()),
    )
    val p = androidx.compose.ui.graphics.Path().apply {
      moveTo(3.5.dp.toPx(), 15.3.dp.toPx())
      cubicTo(
        3.5.dp.toPx(),
        12.2.dp.toPx(),
        5.8.dp.toPx(),
        10.3.dp.toPx(),
        9.dp.toPx(),
        10.3.dp.toPx(),
      )
      cubicTo(
        12.2.dp.toPx(),
        10.3.dp.toPx(),
        14.5.dp.toPx(),
        12.2.dp.toPx(),
        14.5.dp.toPx(),
        15.3.dp.toPx(),
      )
    }
    drawPath(
      p,
      color = c,
      style = Stroke(w, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round),
    )
  }
}

@Composable
private fun K1LineIcon(
  active: Boolean,
  draw: androidx.compose.ui.graphics.drawscope.DrawScope.(strokeWidth: Float, color: Color) -> Unit,
) {
  val color = if (active) KlikInkPrimary else KlikInkMuted
  Canvas(Modifier.size(18.dp)) {
    val w = 1.4.dp.toPx()
    draw(w, color)
  }
}

// ─── Screen scaffold ──────────────────────────────────────────────────────
@Composable
fun K1Screen(
  navContent: @Composable () -> Unit,
  fab: (@Composable () -> Unit)? = null,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  Box(modifier.fillMaxSize().background(KlikPaperApp)) {
    content()
    Box(Modifier.align(Alignment.BottomCenter)) { navContent() }
    if (fab != null) {
      Box(Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 100.dp)) {
        fab()
      }
    }
  }
}

// ─── Standard row with chevron ────────────────────────────────────────────
@Composable
fun K1SettingsRow(
  label: String,
  value: String? = null,
  onClick: (() -> Unit)? = null,
  icon: ImageVector? = null,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier
      .fillMaxWidth()
      .then(if (onClick != null) Modifier.k1Clickable(onClick = onClick) else Modifier)
      .padding(horizontal = 16.dp, vertical = 14.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    if (icon != null) {
      Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = KlikInkPrimary)
      Spacer(Modifier.width(12.dp))
    }
    Text(label, style = K1Type.bodySm, modifier = Modifier.weight(1f))
    if (value != null) {
      Text(value, style = K1Type.meta)
      Spacer(Modifier.width(6.dp))
    }
    // chevron glyph
    Canvas(Modifier.size(12.dp)) {
      val w = 1.2.dp.toPx()
      drawLine(
        color = KlikInkMuted,
        strokeWidth = w,
        cap = androidx.compose.ui.graphics.StrokeCap.Round,
        start = androidx.compose.ui.geometry.Offset(4.dp.toPx(), 3.dp.toPx()),
        end = androidx.compose.ui.geometry.Offset(8.dp.toPx(), 6.dp.toPx()),
      )
      drawLine(
        color = KlikInkMuted,
        strokeWidth = w,
        cap = androidx.compose.ui.graphics.StrokeCap.Round,
        start = androidx.compose.ui.geometry.Offset(8.dp.toPx(), 6.dp.toPx()),
        end = androidx.compose.ui.geometry.Offset(4.dp.toPx(), 9.dp.toPx()),
      )
    }
  }
}

// ─── Status pill (Running · ETA 12m, Needs OK, Done) ──────────────────────
enum class K1Status { Running, NeedsOk, Done, Paused, Failed }

@Composable
fun K1StatusPill(status: K1Status, text: String, modifier: Modifier = Modifier) {
  val (bg, fg) = when (status) {
    K1Status.Running -> KlikCommitmentBg to KlikCommitmentSubtext
    K1Status.NeedsOk -> KlikDecisionBg to KlikDecisionSubtext
    K1Status.Done -> KlikPaperChip to KlikInkTertiary
    K1Status.Paused -> KlikPaperChip to KlikInkSecondary
    K1Status.Failed -> KlikRiskBg to KlikRiskSubtext
  }
  Box(modifier.clip(K1R.chip).background(bg).padding(horizontal = 9.dp, vertical = 3.dp)) {
    Text(text, style = K1Type.meta.copy(color = fg, fontSize = 10.sp, fontWeight = FontWeight.Medium))
  }
}

// ─── Top bar (back · title · actions) ─────────────────────────────────────
@Composable
fun K1TopBar(
  title: String,
  onBack: (() -> Unit)? = null,
  trailing: (@Composable () -> Unit)? = null,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier
      .fillMaxWidth()
      .statusBarsPadding()
      .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    if (onBack != null) {
      Box(Modifier.size(32.dp).k1Clickable(onClick = onBack), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(16.dp)) {
          val w = 1.3.dp.toPx()
          drawLine(
            color = KlikInkPrimary,
            strokeWidth = w,
            cap = androidx.compose.ui.graphics.StrokeCap.Round,
            start = androidx.compose.ui.geometry.Offset(10.dp.toPx(), 3.5.dp.toPx()),
            end = androidx.compose.ui.geometry.Offset(5.5.dp.toPx(), 8.dp.toPx()),
          )
          drawLine(
            color = KlikInkPrimary,
            strokeWidth = w,
            cap = androidx.compose.ui.graphics.StrokeCap.Round,
            start = androidx.compose.ui.geometry.Offset(5.5.dp.toPx(), 8.dp.toPx()),
            end = androidx.compose.ui.geometry.Offset(10.dp.toPx(), 12.5.dp.toPx()),
          )
        }
      }
      Spacer(Modifier.width(8.dp))
    }
    Text(title, style = K1Type.h2, modifier = Modifier.weight(1f))
    if (trailing != null) trailing()
  }
}

// ─── Brand eyebrow header (KLIK ONE + title) ──────────────────────────────
@Composable
fun K1Header(
  title: String,
  eyebrow: String = "KLIK",
  trailing: (@Composable () -> Unit)? = null,
  onTitleClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
    verticalAlignment = Alignment.Top,
  ) {
    Column(Modifier.weight(1f)) {
      K1Eyebrow(eyebrow, large = true)
      Spacer(Modifier.height(4.dp))
      Text(
        title,
        style = K1Type.h1,
        modifier = if (onTitleClick != null) Modifier.k1Clickable(onClick = onTitleClick) else Modifier,
      )
    }
    if (trailing != null) trailing()
  }
}

// ─── Pull-to-refresh indicator ────────────────────────────────────────────
// Editorial K1 refresh: a small raised disc with the same 4-point sparkle as
// the ASK FAB. While pulling the sparkle fades in and rotates with distance;
// while refreshing it spins continuously. No shadow, no material blue ring.
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun K1PullRefreshIndicator(
  state: androidx.compose.material3.pulltorefresh.PullToRefreshState,
  isRefreshing: Boolean,
  modifier: Modifier = Modifier,
) {
  val target = if (isRefreshing) 1f else state.distanceFraction.coerceIn(0f, 1f)
  val scale by androidx.compose.animation.core.animateFloatAsState(
    targetValue = target,
    animationSpec = tween(durationMillis = 180),
    label = "k1-refresh-scale",
  )
  val rotation: Float = if (isRefreshing) {
    val infinite = rememberInfiniteTransition(label = "k1-refresh-spin")
    val a by infinite.animateFloat(
      initialValue = 0f,
      targetValue = 360f,
      animationSpec = infiniteRepeatable(tween(900, easing = androidx.compose.animation.core.LinearEasing)),
      label = "k1-refresh-angle",
    )
    a
  } else {
    state.distanceFraction * 220f
  }

  if (scale > 0.01f) {
    Box(
      modifier = modifier
        .padding(top = 56.dp)
        .size(36.dp)
        .scale(scale)
        .background(KlikPaperSoft, CircleShape)
        .border(0.5.dp, KlikLineHairline, CircleShape),
      contentAlignment = Alignment.Center,
    ) {
      Canvas(
        Modifier
          .size(16.dp)
          .rotate(rotation),
      ) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = size.minDimension / 2f
        val mainPath = androidx.compose.ui.graphics.Path().apply {
          moveTo(cx, cy - r)
          lineTo(cx + r * 0.22f, cy)
          lineTo(cx, cy + r)
          lineTo(cx - r * 0.22f, cy)
          close()
        }
        val armPath = androidx.compose.ui.graphics.Path().apply {
          moveTo(cx - r * 0.72f, cy)
          lineTo(cx, cy - r * 0.18f)
          lineTo(cx + r * 0.72f, cy)
          lineTo(cx, cy + r * 0.18f)
          close()
        }
        drawPath(mainPath, color = KlikInkPrimary)
        drawPath(armPath, color = KlikInkPrimary)
      }
    }
  }
}

// ─── Swipe-to-action row ─────────────────────────────────────────────────
// Editorial swipe pattern. Drag right reveals a pin glyph + label, drag
// left reveals an archive glyph + label. Crossing the activation threshold
// fires a medium-impact haptic and the matching callback; the row then
// springs back to rest. We deliberately avoid Material3's SwipeToDismissBox
// — its anchored-state machine froze rows in EventsScreen — and instead
// drive the offset with a single Animatable so a callback can never leave
// the row stuck off-screen.
//
// The pinned chrome (KlikCommitmentBg + KlikInkPrimary) and the archived
// chrome (KlikInkPrimary + KlikPaperCard) follow the same paper-and-ink
// palette as K1Card / K1Chip, so the reveal reads as a continuation of the
// row, not a Material widget.
@Composable
fun K1SwipeRow(
  modifier: Modifier = Modifier,
  isPinned: Boolean = false,
  pinLabel: String = "Pin",
  unpinLabel: String = "Unpin",
  archiveLabel: String = "Archive",
  onPin: (() -> Unit)? = null,
  onArchive: (() -> Unit)? = null,
  content: @Composable () -> Unit,
) {
  val density = LocalDensity.current
  val activatePx = with(density) { 72.dp.toPx() }
  val maxRevealPx = with(density) { 96.dp.toPx() }
  val offsetX = remember { Animatable(0f) }
  val scope = androidx.compose.runtime.rememberCoroutineScope()

  Box(modifier.fillMaxWidth()) {
    // Background reveal — pin (right pull) on the leading edge, archive
    // (left pull) on the trailing edge. Only one is visible at a time
    // because the offset's sign is mutually exclusive.
    val o = offsetX.value
    val showPin = o > 1f && onPin != null
    val showArchive = o < -1f && onArchive != null
    if (showPin || showArchive) {
      Box(
        Modifier
          .matchParentSize()
          .clip(K1R.soft)
          .background(
            when {
              showPin -> if (isPinned) KlikPaperSoft else KlikCommitmentBg
              else -> KlikInkPrimary
            },
          ),
        contentAlignment = if (showPin) Alignment.CenterStart else Alignment.CenterEnd,
      ) {
        Row(
          Modifier.padding(horizontal = K1Sp.lg),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          if (showPin) {
            K1PinGlyph(color = KlikInkPrimary)
            Spacer(Modifier.width(K1Sp.xs))
            Text(
              if (isPinned) unpinLabel else pinLabel,
              style = K1Type.meta.copy(
                color = KlikInkPrimary,
                fontWeight = FontWeight.Medium,
              ),
            )
          } else {
            Text(
              archiveLabel,
              style = K1Type.meta.copy(
                color = KlikPaperCard,
                fontWeight = FontWeight.Medium,
              ),
            )
            Spacer(Modifier.width(K1Sp.xs))
            K1ArchiveGlyph(color = KlikPaperCard)
          }
        }
      }
    }

    // Foreground row — translates with the drag.
    Box(
      Modifier
        .offset { IntOffset(offsetX.value.roundToInt(), 0) }
        .pointerInput(onPin, onArchive) {
          detectHorizontalDragGestures(
            onDragStart = { /* no-op: we don't lock vertical scroll */ },
            onDragEnd = {
              val current = offsetX.value
              val crossedRight = current >= activatePx && onPin != null
              val crossedLeft = current <= -activatePx && onArchive != null
              if (crossedRight) {
                if (hapticEnabledState.value) HapticService.mediumImpact()
                onPin?.invoke()
              } else if (crossedLeft) {
                if (hapticEnabledState.value) HapticService.mediumImpact()
                onArchive?.invoke()
              }
              scope.launch {
                offsetX.animateTo(
                  targetValue = 0f,
                  animationSpec = spring(dampingRatio = 0.85f, stiffness = 380f),
                )
              }
            },
            onDragCancel = {
              scope.launch {
                offsetX.animateTo(
                  targetValue = 0f,
                  animationSpec = spring(dampingRatio = 0.85f, stiffness = 380f),
                )
              }
            },
            onHorizontalDrag = { change, dragAmount ->
              val raw = offsetX.value + dragAmount
              // Clamp to the configured reveal width and disallow direction
              // for which there is no callback, so the row can't be dragged
              // into a blank state.
              val clamped = when {
                raw > 0f && onPin == null -> 0f
                raw < 0f && onArchive == null -> 0f
                else -> raw.coerceIn(-maxRevealPx, maxRevealPx)
              }
              if (clamped != offsetX.value) {
                scope.launch { offsetX.snapTo(clamped) }
                change.consume()
              }
            },
          )
        },
    ) {
      content()
    }
  }
}

// ─── Swipe-action glyphs ─────────────────────────────────────────────────
// Editorial line glyphs that ride the swipe reveal. Both render at 14 dp so
// they sit centred against meta-sized text.

@Composable
private fun K1PinGlyph(color: Color) {
  Canvas(Modifier.size(14.dp)) {
    val w = 1.2.dp.toPx()
    val cx = size.width / 2f
    val headTop = 2.dp.toPx()
    val headBottom = 7.dp.toPx()
    val needleEnd = 12.dp.toPx()
    // Pin head
    drawLine(
      color = color,
      strokeWidth = w,
      cap = androidx.compose.ui.graphics.StrokeCap.Round,
      start = androidx.compose.ui.geometry.Offset(cx - 3.dp.toPx(), headTop),
      end = androidx.compose.ui.geometry.Offset(cx + 3.dp.toPx(), headTop),
    )
    drawLine(
      color = color,
      strokeWidth = w,
      cap = androidx.compose.ui.graphics.StrokeCap.Round,
      start = androidx.compose.ui.geometry.Offset(cx - 3.dp.toPx(), headTop),
      end = androidx.compose.ui.geometry.Offset(cx - 1.dp.toPx(), headBottom),
    )
    drawLine(
      color = color,
      strokeWidth = w,
      cap = androidx.compose.ui.graphics.StrokeCap.Round,
      start = androidx.compose.ui.geometry.Offset(cx + 3.dp.toPx(), headTop),
      end = androidx.compose.ui.geometry.Offset(cx + 1.dp.toPx(), headBottom),
    )
    drawLine(
      color = color,
      strokeWidth = w,
      cap = androidx.compose.ui.graphics.StrokeCap.Round,
      start = androidx.compose.ui.geometry.Offset(cx - 1.dp.toPx(), headBottom),
      end = androidx.compose.ui.geometry.Offset(cx + 1.dp.toPx(), headBottom),
    )
    // Needle
    drawLine(
      color = color,
      strokeWidth = w,
      cap = androidx.compose.ui.graphics.StrokeCap.Round,
      start = androidx.compose.ui.geometry.Offset(cx, headBottom),
      end = androidx.compose.ui.geometry.Offset(cx, needleEnd),
    )
  }
}

@Composable
private fun K1ArchiveGlyph(color: Color) {
  Canvas(Modifier.size(14.dp)) {
    val w = 1.2.dp.toPx()
    // Lid
    drawLine(
      color = color,
      strokeWidth = w,
      cap = androidx.compose.ui.graphics.StrokeCap.Round,
      start = androidx.compose.ui.geometry.Offset(2.dp.toPx(), 4.dp.toPx()),
      end = androidx.compose.ui.geometry.Offset(12.dp.toPx(), 4.dp.toPx()),
    )
    // Box outline
    drawLine(
      color = color,
      strokeWidth = w,
      cap = androidx.compose.ui.graphics.StrokeCap.Round,
      start = androidx.compose.ui.geometry.Offset(3.dp.toPx(), 4.dp.toPx()),
      end = androidx.compose.ui.geometry.Offset(3.dp.toPx(), 12.dp.toPx()),
    )
    drawLine(
      color = color,
      strokeWidth = w,
      cap = androidx.compose.ui.graphics.StrokeCap.Round,
      start = androidx.compose.ui.geometry.Offset(11.dp.toPx(), 4.dp.toPx()),
      end = androidx.compose.ui.geometry.Offset(11.dp.toPx(), 12.dp.toPx()),
    )
    drawLine(
      color = color,
      strokeWidth = w,
      cap = androidx.compose.ui.graphics.StrokeCap.Round,
      start = androidx.compose.ui.geometry.Offset(3.dp.toPx(), 12.dp.toPx()),
      end = androidx.compose.ui.geometry.Offset(11.dp.toPx(), 12.dp.toPx()),
    )
    // Tab notch
    drawLine(
      color = color,
      strokeWidth = w,
      cap = androidx.compose.ui.graphics.StrokeCap.Round,
      start = androidx.compose.ui.geometry.Offset(5.5.dp.toPx(), 7.5.dp.toPx()),
      end = androidx.compose.ui.geometry.Offset(8.5.dp.toPx(), 7.5.dp.toPx()),
    )
  }
}

// ─── K1 Bottom Sheet ──────────────────────────────────────────────────────
// Editorial bottom sheet for K1. Wrapped in a Popup so the scrim and card
// overlay the bottom navigation bar at MainApp level — without the Popup,
// the sheet only covers the screen-content area and the tab bar shows
// through (buttons get hidden behind it). Tap the scrim to dismiss.
@Composable
fun K1BottomSheet(
  onDismiss: () -> Unit,
  content: @Composable () -> Unit,
) {
  androidx.compose.ui.window.Popup(
    alignment = Alignment.BottomCenter,
    onDismissRequest = onDismiss,
    properties = androidx.compose.ui.window.PopupProperties(
      focusable = true,
      dismissOnBackPress = true,
      dismissOnClickOutside = true,
    ),
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.4f))
        .k1Clickable(onClick = onDismiss),
    ) {
      Box(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .fillMaxWidth()
          .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
          .background(KlikPaperCard)
          .k1Clickable {}, // consume taps so they don't reach the scrim
      ) {
        content()
      }
    }
  }
}

// ─── K1 Contextual Menu ───────────────────────────────────────────────────
// Lightweight editorial action sheet — used by long-press on Featured cards
// to expose Edit / Pause / Delete / Snooze. Avoids Material3 DropdownMenu
// (Material ripple + elevation shadow). Renders as a centered paper card
// over a scrim; scrim tap dismisses.
@Composable
fun K1Menu(
  onDismiss: () -> Unit,
  content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = 0.4f))
      .k1Clickable(onClick = onDismiss),
  ) {
    Column(
      modifier = Modifier
        .align(Alignment.Center)
        .padding(horizontal = 40.dp)
        .fillMaxWidth()
        .clip(K1R.card)
        .background(KlikPaperCard)
        .border(0.5.dp, KlikLineHairline, K1R.card)
        .k1Clickable {}, // swallow taps so menu items don't dismiss scrim
      content = content,
    )
  }
}

@Composable
fun K1MenuItem(
  label: String,
  danger: Boolean = false,
  onClick: () -> Unit,
) {
  val color = if (danger) KlikAlert else KlikInkPrimary
  Box(
    Modifier
      .fillMaxWidth()
      .k1Clickable(onClick = onClick)
      .padding(horizontal = 18.dp, vertical = 14.dp),
  ) {
    Text(label, style = K1Type.bodyMd.copy(color = color))
  }
  Box(Modifier.fillMaxWidth().height(0.5.dp).background(KlikLineHairline))
}

// ─── K1 Confirm Dialog ────────────────────────────────────────────────────
// Editorial confirm dialog. Same scrim-card pattern as K1Menu; two stacked
// buttons (Cancel / Confirm) avoid Material3 AlertDialog defaults.
@Composable
fun K1ConfirmDialog(
  title: String,
  message: String,
  confirmLabel: String,
  cancelLabel: String = "Cancel",
  danger: Boolean = true,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = 0.4f))
      .k1Clickable(onClick = onDismiss),
  ) {
    Column(
      modifier = Modifier
        .align(Alignment.Center)
        .padding(horizontal = 40.dp)
        .fillMaxWidth()
        .clip(K1R.card)
        .background(KlikPaperCard)
        .border(0.5.dp, KlikLineHairline, K1R.card)
        .k1Clickable {}
        .padding(18.dp),
    ) {
      Text(title, style = K1Type.h3)
      Spacer(Modifier.height(K1Sp.s))
      Text(message, style = K1Type.bodySm.copy(color = KlikInkSecondary))
      Spacer(Modifier.height(K1Sp.lg))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Box(
          Modifier
            .weight(1f)
            .clip(K1R.chip)
            .border(0.5.dp, KlikLineHairline, K1R.chip)
            .k1Clickable(onClick = onDismiss)
            .padding(vertical = 10.dp),
          contentAlignment = Alignment.Center,
        ) {
          Text(cancelLabel, style = K1Type.bodySm.copy(color = KlikInkSecondary))
        }
        Box(
          Modifier
            .weight(1f)
            .clip(K1R.chip)
            .background(if (danger) KlikAlert else KlikInkPrimary)
            .k1Clickable(onClick = onConfirm)
            .padding(vertical = 10.dp),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            confirmLabel,
            style = K1Type.bodySm.copy(color = KlikPaperCard, fontWeight = FontWeight.Medium),
          )
        }
      }
    }
  }
}

// ─── K1 Toast ─────────────────────────────────────────────────────────────
// Tiny pill anchored to the bottom of the screen. Used to surface success
// and error feedback from fire-and-forget rule actions (Pause / Delete /
// Accept / Decline / Snooze). Auto-dismisses after 2.5s; tap to dismiss
// early. K1 ink-on-paper inverted — dark pill, light text — matching the
// primary button in K1ConfirmDialog.
@Composable
fun K1Toast(message: String?, onDismiss: () -> Unit) {
  if (message == null) return
  LaunchedEffect(message) {
    kotlinx.coroutines.delay(2500)
    onDismiss()
  }
  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(bottom = 96.dp),
    contentAlignment = Alignment.BottomCenter,
  ) {
    Row(
      modifier = Modifier
        .clip(RoundedCornerShape(999.dp))
        .background(KlikInkPrimary)
        .k1Clickable(onClick = onDismiss)
        .padding(horizontal = 16.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(message, style = K1Type.bodySm.copy(color = KlikPaperCard))
    }
  }
}
