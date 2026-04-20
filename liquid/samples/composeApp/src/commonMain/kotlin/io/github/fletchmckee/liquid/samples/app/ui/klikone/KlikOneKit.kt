// Copyright 2025, Klik
// Klik One design system — editorial, minimal, near-black-on-warm-paper.
// All primitives the redesigned screens compose from. One file, one source of truth.
package io.github.fletchmckee.liquid.samples.app.ui.klikone

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperSoft
import io.github.fletchmckee.liquid.samples.app.theme.KlikRiskAccent
import io.github.fletchmckee.liquid.samples.app.theme.KlikRiskBg
import io.github.fletchmckee.liquid.samples.app.theme.KlikRiskSubtext
import io.github.fletchmckee.liquid.samples.app.theme.KlikRiskText
import io.github.fletchmckee.liquid.samples.app.theme.KlikRunning
import io.github.fletchmckee.liquid.samples.app.theme.KlikWarn
import io.github.fletchmckee.liquid.samples.app.theme.KlikAlert
import kotlin.math.abs

// ─── Type scale ───────────────────────────────────────────────────────────
object K1Type {
    val display   = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Medium, letterSpacing = (-0.8).sp, lineHeight = 38.sp, color = KlikInkPrimary)
    val h1        = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Medium, letterSpacing = (-0.8).sp, lineHeight = 32.sp, color = KlikInkPrimary)
    val h2        = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Medium, letterSpacing = (-0.4).sp, lineHeight = 28.sp, color = KlikInkPrimary)
    val h3        = TextStyle(fontSize = 19.sp, fontWeight = FontWeight.Medium, letterSpacing = (-0.3).sp, lineHeight = 24.sp, color = KlikInkPrimary)
    val body      = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, lineHeight = 22.sp, color = KlikInkPrimary)
    val bodySm    = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Normal, lineHeight = 20.sp, color = KlikInkPrimary)
    val bodyMd    = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, lineHeight = 18.sp, color = KlikInkPrimary)
    val caption   = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal, lineHeight = 18.sp, color = KlikInkTertiary)
    val meta      = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Normal, color = KlikInkTertiary)
    val metaSm    = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Normal, color = KlikInkMuted)
    val eyebrow   = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.8.sp, color = KlikInkTertiary)
    val eyebrowLg = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.2.sp, color = KlikInkFaint)
    val timer     = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.Medium, letterSpacing = (-1).sp, color = KlikInkPrimary)
}

// ─── Spacing ──────────────────────────────────────────────────────────────
object K1Sp {
    val xxs = 4.dp; val xs = 6.dp; val s = 8.dp; val sm = 10.dp
    val m = 12.dp; val md = 14.dp; val lg = 16.dp; val xl = 20.dp
    val xxl = 24.dp; val xxxl = 32.dp; val huge = 48.dp
}

// ─── Radii ────────────────────────────────────────────────────────────────
object K1R {
    val chip   = RoundedCornerShape(7.dp)
    val soft   = RoundedCornerShape(10.dp)
    val card   = RoundedCornerShape(12.dp)
    val tight  = RoundedCornerShape(14.dp)
    val sheet  = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val pill   = RoundedCornerShape(999.dp)
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
    Canvas(modifier = modifier.height(maxH.dp).wrapContentHeight()) {
        val bw = barWidth.toPx(); val g = gap.toPx()
        heights.forEachIndexed { i, h ->
            val x = i * (bw + g)
            val y = size.height - h.dp.toPx()
            drawRoundRect(
                color = color,
                topLeft = androidx.compose.ui.geometry.Offset(x, y),
                size = Size(bw, h.dp.toPx()),
                cornerRadius = CornerRadius(bw / 2f, bw / 2f)
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
            initialValue = 0.3f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(d), RepeatMode.Reverse),
            label = "w$i"
        )
    }
    val bases = listOf(22f, 36f, 28f, 44f, 24f)
    Canvas(modifier = modifier.height(44.dp)) {
        val bw = barWidth.toPx(); val g = gap.toPx()
        anims.forEachIndexed { i, a ->
            val h = (bases[i] * a.value).coerceAtLeast(6f)
            val x = i * (bw + g)
            val y = (size.height - h.dp.toPx()) / 2f + (size.height - h.dp.toPx()) / 2f
            val yTop = (size.height - h) / 2f
            drawRoundRect(
                color = color,
                topLeft = androidx.compose.ui.geometry.Offset(x, yTop),
                size = Size(bw, h),
                cornerRadius = CornerRadius(bw / 2f, bw / 2f)
            )
        }
    }
}

/** Recording dot — pulsing alert-orange circle. */
@Composable
fun K1RecDot(modifier: Modifier = Modifier, color: Color = KlikAlert, size: Dp = 8.dp) {
    val infinite = rememberInfiniteTransition(label = "rec")
    val a by infinite.animateFloat(
        initialValue = 1f, targetValue = 0.35f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label = "recA"
    )
    Box(modifier
        .size(size)
        .background(color.copy(alpha = a), CircleShape)
    )
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
    Row(modifier.fillMaxWidth().padding(vertical = K1Sp.xs),
        verticalAlignment = Alignment.CenterVertically) {
        if (dotColor != null) {
            Box(Modifier.size(5.dp).background(dotColor, CircleShape))
            Spacer(Modifier.width(K1Sp.s))
        }
        Text(
            buildString {
                append(label.uppercase())
                if (count != null) append(" · $count")
            },
            style = K1Type.eyebrow.copy(color = KlikInkPrimary, fontWeight = FontWeight.Medium)
        )
        Spacer(Modifier.width(K1Sp.s))
        Box(Modifier.weight(1f).height(0.5.dp).background(KlikLineHairline))
        if (trailing != null) { Spacer(Modifier.width(K1Sp.s)); trailing() }
    }
}

// ─── Chip ─────────────────────────────────────────────────────────────────
@Composable
fun K1Chip(
    label: String,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    leading: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val bg = if (selected) KlikInkPrimary else KlikPaperChip
    val fg = if (selected) KlikPaperCard else KlikInkSecondary
    Row(
        modifier
            .clip(K1R.chip)
            .background(bg)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
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
            .clickable(enabled = enabled, onClick = onClick)
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
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) { Text(label, style = K1Type.bodyMd.copy(color = KlikInkPrimary, fontSize = 14.sp)) }
}

@Composable
fun K1ButtonGhost(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier.clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) { Text(label, style = K1Type.bodyMd.copy(color = KlikInkTertiary, fontSize = 13.sp)) }
}

// ─── Cards ────────────────────────────────────────────────────────────────
@Composable
fun K1Card(
    modifier: Modifier = Modifier,
    soft: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    val bg = if (soft) KlikPaperSoft else KlikPaperCard
    Column(
        modifier
            .clip(K1R.card)
            .background(bg)
            .then(if (!soft) Modifier.border(0.5.dp, KlikLineHairline, K1R.card) else Modifier)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(14.dp),
        content = content,
    )
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
            .then(if (signal == K1Signal.Decision) Modifier.border(
                width = 0.dp, color = Color.Transparent, shape = K1R.soft
            ) else Modifier)
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
private fun paletteIdx(initials: String): Int {
    if (initials.isEmpty()) return 0
    val n = initials.fold(0) { a, ch -> a + ch.code }
    return abs(n) % KlikAvatarBg.size
}

@Composable
fun K1Avatar(initials: String, size: Dp = 36.dp, modifier: Modifier = Modifier) {
    val i = paletteIdx(initials)
    val bg = KlikAvatarBg[i]; val fg = KlikAvatarFg[i]
    val fontSize = when {
        size <= 20.dp -> 9.sp
        size <= 28.dp -> 10.sp
        size <= 36.dp -> 12.sp
        size <= 48.dp -> 14.sp
        else -> 18.sp
    }
    Box(modifier.size(size).background(bg, CircleShape), contentAlignment = Alignment.Center) {
        Text(initials.take(2).uppercase(), style = TextStyle(
            fontSize = fontSize, fontWeight = FontWeight.Medium, color = fg, textAlign = TextAlign.Center
        ))
    }
}

@Composable
fun K1AvatarStack(initialsList: List<String>, size: Dp = 24.dp, modifier: Modifier = Modifier) {
    Row(modifier) {
        initialsList.forEachIndexed { i, ini ->
            Box(
                Modifier
                    .then(if (i > 0) Modifier.padding(start = (size.value * -0.25f).dp) else Modifier)
                    .size(size)
                    .background(KlikPaperSoft, CircleShape)
                    .padding(1.5.dp)
            ) { K1Avatar(ini, size = size - 3.dp) }
        }
    }
}

// ─── Floating Ask button ──────────────────────────────────────────────────
@Composable
fun K1AskFab(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(60.dp)
            .background(KlikInkPrimary, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            K1Waveform(
                heights = listOf(8f, 13f, 6f, 11f, 7f),
                barWidth = 2.5.dp, gap = 2.dp,
                color = KlikPaperCard,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                "ASK",
                style = TextStyle(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp,
                    color = KlikPaperCard,
                ),
            )
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
                        .clickable(onClick = { onSelect(item.route) })
                        .padding(bottom = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box {
                        item.iconPath()
                        if (item.badgeCount > 0) {
                            Box(
                                Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(start = 12.dp)
                                    .size(14.dp)
                                    .background(KlikAlert, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(item.badgeCount.toString(), style = TextStyle(
                                    fontSize = 8.sp, fontWeight = FontWeight.Medium, color = KlikPaperCard
                                ))
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
                        )
                    )
                }
            }
        }
    }
}

// ─── Mini icons (line, 18dp, 1.2px stroke equivalent) ─────────────────────
@Composable
fun K1IconToday(active: Boolean) { K1LineIcon(active) { w, c ->
    // Calendar glyph: outer rounded rect, top line, two tick marks
    drawRoundRect(color = c, style = androidx.compose.ui.graphics.drawscope.Stroke(w), cornerRadius = CornerRadius(1.5.dp.toPx()),
        topLeft = androidx.compose.ui.geometry.Offset(3.dp.toPx(), 4.dp.toPx()),
        size = Size(12.dp.toPx(), 11.dp.toPx()))
    drawLine(color = c, strokeWidth = w, start = androidx.compose.ui.geometry.Offset(3.dp.toPx(), 7.dp.toPx()),
        end = androidx.compose.ui.geometry.Offset(15.dp.toPx(), 7.dp.toPx()))
    drawLine(color = c, strokeWidth = w, start = androidx.compose.ui.geometry.Offset(6.5.dp.toPx(), 2.5.dp.toPx()),
        end = androidx.compose.ui.geometry.Offset(6.5.dp.toPx(), 5.5.dp.toPx()), cap = androidx.compose.ui.graphics.StrokeCap.Round)
    drawLine(color = c, strokeWidth = w, start = androidx.compose.ui.geometry.Offset(11.5.dp.toPx(), 2.5.dp.toPx()),
        end = androidx.compose.ui.geometry.Offset(11.5.dp.toPx(), 5.5.dp.toPx()), cap = androidx.compose.ui.graphics.StrokeCap.Round)
} }

@Composable
fun K1IconMoves(active: Boolean) { K1LineIcon(active) { w, c ->
    // Folded doc with check
    val path = androidx.compose.ui.graphics.Path().apply {
        moveTo(5.5.dp.toPx(), 2.5.dp.toPx())
        lineTo(2.5.dp.toPx(), 5.5.dp.toPx())
        lineTo(2.5.dp.toPx(), 15.dp.toPx())
        lineTo(15.5.dp.toPx(), 15.dp.toPx())
        lineTo(15.5.dp.toPx(), 5.5.dp.toPx())
        lineTo(12.5.dp.toPx(), 2.5.dp.toPx())
        close()
    }
    drawPath(path, color = c, style = androidx.compose.ui.graphics.drawscope.Stroke(w))
    // check mark
    drawLine(color = c, strokeWidth = w, cap = androidx.compose.ui.graphics.StrokeCap.Round,
        start = androidx.compose.ui.geometry.Offset(6.dp.toPx(), 9.dp.toPx()),
        end = androidx.compose.ui.geometry.Offset(7.8.dp.toPx(), 10.8.dp.toPx()))
    drawLine(color = c, strokeWidth = w, cap = androidx.compose.ui.graphics.StrokeCap.Round,
        start = androidx.compose.ui.geometry.Offset(7.8.dp.toPx(), 10.8.dp.toPx()),
        end = androidx.compose.ui.geometry.Offset(11.5.dp.toPx(), 7.dp.toPx()))
} }

@Composable
fun K1IconNetwork(active: Boolean) { K1LineIcon(active) { w, c ->
    drawCircle(color = c, style = androidx.compose.ui.graphics.drawscope.Stroke(w),
        radius = 2.2.dp.toPx(), center = androidx.compose.ui.geometry.Offset(6.5.dp.toPx(), 6.5.dp.toPx()))
    drawCircle(color = c, style = androidx.compose.ui.graphics.drawscope.Stroke(w),
        radius = 1.8.dp.toPx(), center = androidx.compose.ui.geometry.Offset(12.5.dp.toPx(), 7.5.dp.toPx()))
    drawLine(color = c, strokeWidth = w, cap = androidx.compose.ui.graphics.StrokeCap.Round,
        start = androidx.compose.ui.geometry.Offset(1.dp.toPx(), 13.5.dp.toPx()),
        end = androidx.compose.ui.geometry.Offset(8.2.dp.toPx(), 13.5.dp.toPx()))
    drawLine(color = c, strokeWidth = w, cap = androidx.compose.ui.graphics.StrokeCap.Round,
        start = androidx.compose.ui.geometry.Offset(8.5.dp.toPx(), 13.5.dp.toPx()),
        end = androidx.compose.ui.geometry.Offset(14.9.dp.toPx(), 13.5.dp.toPx()))
} }

@Composable
fun K1IconYou(active: Boolean) { K1LineIcon(active) { w, c ->
    drawCircle(color = c, style = androidx.compose.ui.graphics.drawscope.Stroke(w),
        radius = 2.7.dp.toPx(), center = androidx.compose.ui.geometry.Offset(9.dp.toPx(), 6.5.dp.toPx()))
    val p = androidx.compose.ui.graphics.Path().apply {
        moveTo(3.5.dp.toPx(), 15.dp.toPx())
        cubicTo(
            3.5.dp.toPx(), 12.3.dp.toPx(),
            5.8.dp.toPx(), 10.5.dp.toPx(),
            9.dp.toPx(), 10.5.dp.toPx()
        )
        cubicTo(
            12.2.dp.toPx(), 10.5.dp.toPx(),
            14.5.dp.toPx(), 12.3.dp.toPx(),
            14.5.dp.toPx(), 15.dp.toPx()
        )
    }
    drawPath(p, color = c, style = androidx.compose.ui.graphics.drawscope.Stroke(w, cap = androidx.compose.ui.graphics.StrokeCap.Round))
} }

@Composable
private fun K1LineIcon(
    active: Boolean,
    draw: androidx.compose.ui.graphics.drawscope.DrawScope.(strokeWidth: Float, color: Color) -> Unit,
) {
    val color = if (active) KlikInkPrimary else KlikInkMuted
    Canvas(Modifier.size(18.dp)) {
        val w = 1.2.dp.toPx()
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
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = KlikInkPrimary)
            Spacer(Modifier.width(12.dp))
        }
        Text(label, style = K1Type.bodySm, modifier = Modifier.weight(1f))
        if (value != null) { Text(value, style = K1Type.meta); Spacer(Modifier.width(6.dp)) }
        // chevron glyph
        Canvas(Modifier.size(12.dp)) {
            val w = 1.2.dp.toPx()
            drawLine(color = KlikInkMuted, strokeWidth = w, cap = androidx.compose.ui.graphics.StrokeCap.Round,
                start = androidx.compose.ui.geometry.Offset(4.dp.toPx(), 3.dp.toPx()),
                end = androidx.compose.ui.geometry.Offset(8.dp.toPx(), 6.dp.toPx()))
            drawLine(color = KlikInkMuted, strokeWidth = w, cap = androidx.compose.ui.graphics.StrokeCap.Round,
                start = androidx.compose.ui.geometry.Offset(8.dp.toPx(), 6.dp.toPx()),
                end = androidx.compose.ui.geometry.Offset(4.dp.toPx(), 9.dp.toPx()))
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
        K1Status.Done    -> KlikPaperChip to KlikInkTertiary
        K1Status.Paused  -> KlikPaperChip to KlikInkSecondary
        K1Status.Failed  -> KlikRiskBg to KlikRiskSubtext
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
            Box(Modifier.size(32.dp).clickable(onClick = onBack), contentAlignment = Alignment.Center) {
                Canvas(Modifier.size(16.dp)) {
                    val w = 1.3.dp.toPx()
                    drawLine(color = KlikInkPrimary, strokeWidth = w, cap = androidx.compose.ui.graphics.StrokeCap.Round,
                        start = androidx.compose.ui.geometry.Offset(10.dp.toPx(), 3.5.dp.toPx()),
                        end = androidx.compose.ui.geometry.Offset(5.5.dp.toPx(), 8.dp.toPx()))
                    drawLine(color = KlikInkPrimary, strokeWidth = w, cap = androidx.compose.ui.graphics.StrokeCap.Round,
                        start = androidx.compose.ui.geometry.Offset(5.5.dp.toPx(), 8.dp.toPx()),
                        end = androidx.compose.ui.geometry.Offset(10.dp.toPx(), 12.5.dp.toPx()))
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
fun K1Header(title: String, eyebrow: String = "KLIK", trailing: (@Composable () -> Unit)? = null, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f)) {
            K1Eyebrow(eyebrow, large = true)
            Spacer(Modifier.height(4.dp))
            Text(title, style = K1Type.h1)
        }
        if (trailing != null) trailing()
    }
}
