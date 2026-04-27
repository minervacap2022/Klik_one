// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkPrimary
import io.github.fletchmckee.liquid.samples.app.theme.KlikLineHairline
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperCard
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1Type

/**
 * Show the recognizable brand mark for [providerId]. Falls back to a lettered
 * disc when the provider doesn't have a dedicated mark yet.
 */
@Composable
fun K1ProviderIcon(providerId: String, size: Dp = 36.dp) {
  val shape = RoundedCornerShape(10.dp)
  Box(
    modifier = Modifier
      .size(size)
      .clip(shape)
      .background(Color.White)
      .border(0.75.dp, KlikLineHairline, shape),
    contentAlignment = Alignment.Center,
  ) {
    when (providerId.lowercase()) {
      "apple_calendar" -> AppleCalendarIcon(size)

      "apple_reminders" -> AppleRemindersIcon(size)

      "slack" -> SlackIcon(size)

      "notion" -> NotionIcon(size)

      "github" -> GithubIcon(size)

      "google" -> GoogleIcon(size)

      "clickup" -> ClickUpIcon(size)

      "monday" -> MondayIcon(size)

      "asana" -> AsanaIcon(size)

      "atlassian",
      "jira",
      -> JiraIcon(size)

      "linear" -> LinearIcon(size)

      "microsoft" -> MicrosoftIcon(size)

      else -> FallbackInitial(providerId)
    }
  }
}

// ─── Apple Calendar ───────────────────────────────────────────────────────
@Composable
private fun AppleCalendarIcon(size: Dp) {
  Box(
    modifier = Modifier
      .size(size * 0.78f)
      .clip(RoundedCornerShape(6.dp))
      .background(Color.White)
      .border(0.75.dp, Color(0xFFE5E5EA), RoundedCornerShape(6.dp)),
  ) {
    Canvas(Modifier.size(size * 0.78f)) {
      // Top red band
      drawRect(
        color = Color(0xFFFF3B30),
        topLeft = Offset(0f, 0f),
        size = Size(this.size.width, this.size.height * 0.25f),
      )
    }
    Text(
      "17",
      style = K1Type.bodyMd.copy(
        color = KlikInkPrimary,
        fontWeight = FontWeight.Bold,
        fontSize = (size.value * 0.32f).toInt().sp,
      ),
      modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 2.dp),
    )
  }
}

// ─── Apple Reminders ──────────────────────────────────────────────────────
@Composable
private fun AppleRemindersIcon(size: Dp) {
  Canvas(Modifier.size(size * 0.6f)) {
    drawCircle(color = Color(0xFFFF3B30))
    val w = 2.dp.toPx()
    // Checkmark
    drawLine(
      color = Color.White,
      strokeWidth = w,
      cap = StrokeCap.Round,
      start = Offset(this.size.width * 0.3f, this.size.height * 0.52f),
      end = Offset(this.size.width * 0.46f, this.size.height * 0.68f),
    )
    drawLine(
      color = Color.White,
      strokeWidth = w,
      cap = StrokeCap.Round,
      start = Offset(this.size.width * 0.46f, this.size.height * 0.68f),
      end = Offset(this.size.width * 0.72f, this.size.height * 0.34f),
    )
  }
}

// ─── Slack (4-square pinwheel) ────────────────────────────────────────────
@Composable
private fun SlackIcon(size: Dp) {
  Canvas(Modifier.size(size * 0.62f)) {
    val r = 2.5.dp.toPx()
    val s = this.size.width
    // Aubergine top-left
    drawRoundRect(
      color = Color(0xFFE01E5A),
      topLeft = Offset(s * 0.05f, s * 0.35f),
      size = Size(s * 0.3f, s * 0.12f),
      cornerRadius = androidx.compose.ui.geometry.CornerRadius(r, r),
    )
    drawRoundRect(
      color = Color(0xFFE01E5A),
      topLeft = Offset(s * 0.35f, s * 0.05f),
      size = Size(s * 0.12f, s * 0.3f),
      cornerRadius = androidx.compose.ui.geometry.CornerRadius(r, r),
    )
    // Green
    drawRoundRect(
      color = Color(0xFF2EB67D),
      topLeft = Offset(s * 0.53f, s * 0.05f),
      size = Size(s * 0.3f, s * 0.12f),
      cornerRadius = androidx.compose.ui.geometry.CornerRadius(r, r),
    )
    drawRoundRect(
      color = Color(0xFF2EB67D),
      topLeft = Offset(s * 0.53f, s * 0.17f),
      size = Size(s * 0.12f, s * 0.3f),
      cornerRadius = androidx.compose.ui.geometry.CornerRadius(r, r),
    )
    // Blue bottom-right
    drawRoundRect(
      color = Color(0xFF36C5F0),
      topLeft = Offset(s * 0.65f, s * 0.53f),
      size = Size(s * 0.3f, s * 0.12f),
      cornerRadius = androidx.compose.ui.geometry.CornerRadius(r, r),
    )
    drawRoundRect(
      color = Color(0xFF36C5F0),
      topLeft = Offset(s * 0.53f, s * 0.53f),
      size = Size(s * 0.12f, s * 0.3f),
      cornerRadius = androidx.compose.ui.geometry.CornerRadius(r, r),
    )
    // Yellow bottom-left
    drawRoundRect(
      color = Color(0xFFECB22E),
      topLeft = Offset(s * 0.17f, s * 0.65f),
      size = Size(s * 0.3f, s * 0.12f),
      cornerRadius = androidx.compose.ui.geometry.CornerRadius(r, r),
    )
    drawRoundRect(
      color = Color(0xFFECB22E),
      topLeft = Offset(s * 0.35f, s * 0.53f),
      size = Size(s * 0.12f, s * 0.3f),
      cornerRadius = androidx.compose.ui.geometry.CornerRadius(r, r),
    )
  }
}

// ─── Notion (bold stylised N) ─────────────────────────────────────────────
@Composable
private fun NotionIcon(size: Dp) {
  Text(
    "N",
    style = K1Type.h2.copy(
      color = Color(0xFF000000),
      fontWeight = FontWeight.Black,
      fontSize = (size.value * 0.55f).toInt().sp,
    ),
  )
}

// ─── GitHub (black circle + minimalist silhouette stand-in) ───────────────
@Composable
private fun GithubIcon(size: Dp) {
  Box(
    modifier = Modifier
      .size(size * 0.68f)
      .clip(CircleShape)
      .background(Color(0xFF24292E)),
    contentAlignment = Alignment.Center,
  ) {
    Canvas(Modifier.size(size * 0.5f)) {
      val s = this.size.width
      // Stylised cat-silhouette: two ears (triangles) + round body
      drawCircle(
        color = Color.White,
        radius = s * 0.28f,
        center = Offset(s * 0.5f, s * 0.58f),
      )
      // Ears
      val leftEar = Path().apply {
        moveTo(s * 0.22f, s * 0.4f)
        lineTo(s * 0.34f, s * 0.18f)
        lineTo(s * 0.4f, s * 0.42f)
        close()
      }
      val rightEar = Path().apply {
        moveTo(s * 0.78f, s * 0.4f)
        lineTo(s * 0.66f, s * 0.18f)
        lineTo(s * 0.6f, s * 0.42f)
        close()
      }
      drawPath(leftEar, Color.White)
      drawPath(rightEar, Color.White)
    }
  }
}

// ─── Google (multi-color G) ───────────────────────────────────────────────
@Composable
private fun GoogleIcon(size: Dp) {
  Canvas(Modifier.size(size * 0.64f)) {
    val s = this.size.width
    val stroke = 2.8.dp.toPx()
    // Approximate Google G with a ring of 4 coloured arcs + blue bar to centre
    drawArc(
      color = Color(0xFF4285F4), // blue
      startAngle = -20f,
      sweepAngle = 100f,
      useCenter = false,
      topLeft = Offset(stroke / 2, stroke / 2),
      size = Size(s - stroke, s - stroke),
      style = Stroke(stroke, cap = StrokeCap.Butt),
    )
    drawArc(
      color = Color(0xFF34A853), // green
      startAngle = 80f,
      sweepAngle = 90f,
      useCenter = false,
      topLeft = Offset(stroke / 2, stroke / 2),
      size = Size(s - stroke, s - stroke),
      style = Stroke(stroke, cap = StrokeCap.Butt),
    )
    drawArc(
      color = Color(0xFFFBBC05), // yellow
      startAngle = 170f,
      sweepAngle = 90f,
      useCenter = false,
      topLeft = Offset(stroke / 2, stroke / 2),
      size = Size(s - stroke, s - stroke),
      style = Stroke(stroke, cap = StrokeCap.Butt),
    )
    drawArc(
      color = Color(0xFFEA4335), // red
      startAngle = 260f,
      sweepAngle = 100f,
      useCenter = false,
      topLeft = Offset(stroke / 2, stroke / 2),
      size = Size(s - stroke, s - stroke),
      style = Stroke(stroke, cap = StrokeCap.Butt),
    )
    // Inner blue bar to mark the G notch
    drawRect(
      color = Color(0xFF4285F4),
      topLeft = Offset(s * 0.5f, s * 0.44f),
      size = Size(s * 0.36f, stroke),
    )
  }
}

// ─── ClickUp (3-color dot logo) ───────────────────────────────────────────
@Composable
private fun ClickUpIcon(size: Dp) {
  Canvas(Modifier.size(size * 0.66f)) {
    val s = this.size.width
    val stroke = 2.2.dp.toPx()
    // Up-chevron in pink
    drawPath(
      path = Path().apply {
        moveTo(s * 0.15f, s * 0.65f)
        lineTo(s * 0.5f, s * 0.3f)
        lineTo(s * 0.85f, s * 0.65f)
      },
      color = Color(0xFFFF4FAE),
      style = Stroke(stroke * 1.5f, cap = StrokeCap.Round),
    )
    // Lower blue chevron
    drawPath(
      path = Path().apply {
        moveTo(s * 0.15f, s * 0.85f)
        lineTo(s * 0.5f, s * 0.5f)
        lineTo(s * 0.85f, s * 0.85f)
      },
      color = Color(0xFF7B68EE),
      style = Stroke(stroke * 1.5f, cap = StrokeCap.Round),
    )
  }
}

// ─── Monday.com (three horizontal pill bars) ──────────────────────────────
@Composable
private fun MondayIcon(size: Dp) {
  Canvas(Modifier.size(size * 0.64f)) {
    val s = this.size.width
    val barH = s * 0.12f
    val barW = s * 0.75f
    val r = barH / 2f
    val cr = androidx.compose.ui.geometry.CornerRadius(r, r)
    // red
    drawRoundRect(
      color = Color(0xFFE2445C),
      topLeft = Offset((s - barW) / 2f, s * 0.15f),
      size = Size(barW, barH),
      cornerRadius = cr,
    )
    // yellow
    drawRoundRect(
      color = Color(0xFFFDAB3D),
      topLeft = Offset((s - barW) / 2f, s * 0.44f),
      size = Size(barW, barH),
      cornerRadius = cr,
    )
    // green
    drawRoundRect(
      color = Color(0xFF00C875),
      topLeft = Offset((s - barW) / 2f, s * 0.73f),
      size = Size(barW, barH),
      cornerRadius = cr,
    )
  }
}

// ─── Asana (3 orange dots in a triangle) ──────────────────────────────────
@Composable
private fun AsanaIcon(size: Dp) {
  Canvas(Modifier.size(size * 0.6f)) {
    val s = this.size.width
    val dot = s * 0.22f
    drawCircle(
      color = Color(0xFFF06A6A),
      radius = dot,
      center = Offset(s * 0.5f, s * 0.28f),
    )
    drawCircle(
      color = Color(0xFFF06A6A),
      radius = dot,
      center = Offset(s * 0.25f, s * 0.72f),
    )
    drawCircle(
      color = Color(0xFFF06A6A),
      radius = dot,
      center = Offset(s * 0.75f, s * 0.72f),
    )
  }
}

// ─── Jira / Atlassian (blue paired triangle) ──────────────────────────────
@Composable
private fun JiraIcon(size: Dp) {
  Canvas(Modifier.size(size * 0.64f)) {
    val s = this.size.width
    // Big blue triangle
    val big = Path().apply {
      moveTo(s * 0.5f, s * 0.12f)
      lineTo(s * 0.92f, s * 0.82f)
      lineTo(s * 0.08f, s * 0.82f)
      close()
    }
    drawPath(big, Color(0xFF2684FF))
    // Inner lighter triangle (paired Atlassian mark)
    val small = Path().apply {
      moveTo(s * 0.5f, s * 0.42f)
      lineTo(s * 0.77f, s * 0.82f)
      lineTo(s * 0.23f, s * 0.82f)
      close()
    }
    drawPath(small, Color(0xFF0052CC))
  }
}

// ─── Linear (angled rectangles logo) ──────────────────────────────────────
@Composable
private fun LinearIcon(size: Dp) {
  Canvas(Modifier.size(size * 0.6f)) {
    val s = this.size.width
    val stroke = 2.dp.toPx()
    val c = Color(0xFF5E6AD2)
    drawLine(
      c,
      strokeWidth = stroke,
      cap = StrokeCap.Round,
      start = Offset(s * 0.18f, s * 0.5f),
      end = Offset(s * 0.6f, s * 0.08f),
    )
    drawLine(
      c,
      strokeWidth = stroke,
      cap = StrokeCap.Round,
      start = Offset(s * 0.4f, s * 0.92f),
      end = Offset(s * 0.92f, s * 0.4f),
    )
    drawLine(
      c,
      strokeWidth = stroke * 1.5f,
      cap = StrokeCap.Round,
      start = Offset(s * 0.08f, s * 0.28f),
      end = Offset(s * 0.72f, s * 0.92f),
    )
  }
}

// ─── Microsoft (4 coloured squares) ───────────────────────────────────────
@Composable
private fun MicrosoftIcon(size: Dp) {
  Canvas(Modifier.size(size * 0.6f)) {
    val s = this.size.width
    val cell = s * 0.46f
    drawRect(Color(0xFFF25022), topLeft = Offset(0f, 0f), size = Size(cell, cell)) // red
    drawRect(Color(0xFF7FBA00), topLeft = Offset(s - cell, 0f), size = Size(cell, cell)) // green
    drawRect(Color(0xFF00A4EF), topLeft = Offset(0f, s - cell), size = Size(cell, cell)) // blue
    drawRect(Color(0xFFFFB900), topLeft = Offset(s - cell, s - cell), size = Size(cell, cell)) // yellow
  }
}

// ─── Fallback — initial disc for unknown providers ────────────────────────
@Composable
private fun FallbackInitial(providerId: String) {
  Text(
    providerId.take(1).uppercase(),
    style = K1Type.bodyMd.copy(color = KlikInkPrimary, fontWeight = FontWeight.Medium),
  )
}
