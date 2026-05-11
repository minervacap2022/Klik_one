// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkPrimary
import io.github.fletchmckee.liquid.samples.app.theme.KlikLineHairline
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1Type
import liquid_root.samples.composeapp.generated.resources.Res
import liquid_root.samples.composeapp.generated.resources.provider_apple
import liquid_root.samples.composeapp.generated.resources.provider_asana
import liquid_root.samples.composeapp.generated.resources.provider_clickup
import liquid_root.samples.composeapp.generated.resources.provider_github
import liquid_root.samples.composeapp.generated.resources.provider_google
import liquid_root.samples.composeapp.generated.resources.provider_jira
import liquid_root.samples.composeapp.generated.resources.provider_linear
import liquid_root.samples.composeapp.generated.resources.provider_microsoft
import liquid_root.samples.composeapp.generated.resources.provider_monday
import liquid_root.samples.composeapp.generated.resources.provider_notion
import liquid_root.samples.composeapp.generated.resources.provider_slack
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * Show the recognizable brand mark for [providerId]. The third-party logos
 * are real vendor SVGs sourced from each vendor's official brand assets
 * (Wikimedia Commons / Simple Icons). Apple Calendar and Apple Reminders are
 * rendered as stylized paper-and-ink approximations because Apple's app
 * icons are proprietary assets we can't redistribute as raster files.
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
      "apple" -> BrandLogo(Res.drawable.provider_apple, size, scale = 0.55f)
      "google" -> BrandLogo(Res.drawable.provider_google, size, scale = 0.68f)
      "microsoft" -> BrandLogo(Res.drawable.provider_microsoft, size, scale = 0.66f)
      "slack" -> BrandLogo(Res.drawable.provider_slack, size, scale = 0.68f)
      "notion" -> BrandLogo(Res.drawable.provider_notion, size, scale = 0.6f)
      "github" -> BrandLogo(Res.drawable.provider_github, size, scale = 0.68f)
      "clickup" -> BrandLogo(Res.drawable.provider_clickup, size, scale = 0.68f)
      "monday" -> BrandLogo(Res.drawable.provider_monday, size, scale = 0.72f)
      "asana" -> BrandLogo(Res.drawable.provider_asana, size, scale = 0.66f)
      "atlassian", "jira" -> BrandLogo(Res.drawable.provider_jira, size, scale = 0.66f)
      "linear" -> BrandLogo(Res.drawable.provider_linear, size, scale = 0.66f)
      else -> FallbackInitial(providerId)
    }
  }
}

@Composable
private fun BrandLogo(resource: DrawableResource, size: Dp, scale: Float) {
  Image(
    painter = painterResource(resource),
    contentDescription = null,
    modifier = Modifier.size(size * scale),
  )
}

// ─── Apple Calendar (paper-and-ink stand-in for the iOS app icon) ─────────
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

// ─── Apple Reminders (paper-and-ink stand-in for the iOS app icon) ────────
@Composable
private fun AppleRemindersIcon(size: Dp) {
  Canvas(Modifier.size(size * 0.6f)) {
    drawCircle(color = Color(0xFFFF3B30))
    val w = 2.dp.toPx()
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

// ─── Fallback — initial disc for unknown providers ────────────────────────
@Composable
private fun FallbackInitial(providerId: String) {
  Text(
    providerId.take(1).uppercase(),
    style = K1Type.bodyMd.copy(color = KlikInkPrimary, fontWeight = FontWeight.Medium),
  )
}
