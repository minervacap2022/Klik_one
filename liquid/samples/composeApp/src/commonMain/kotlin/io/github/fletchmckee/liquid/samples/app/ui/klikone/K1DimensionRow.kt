// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.ui.klikone

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.fletchmckee.liquid.samples.app.domain.entity.DimensionScore
import io.github.fletchmckee.liquid.samples.app.theme.KlikAlert
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkMuted
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkPrimary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkTertiary
import io.github.fletchmckee.liquid.samples.app.theme.KlikLineHairline
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperSoft
import io.github.fletchmckee.liquid.samples.app.theme.KlikRunning
import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import io.github.fletchmckee.liquid.samples.app.theme.KlikWarn
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** Tone of a resolved dimension — drives the dot accent next to the value. */
enum class K1DimensionTone { POSITIVE, WATCH, ALERT, NEUTRAL }

/** Resolver output. Pure data — easy to unit test. */
data class ResolvedDimension(
  val displayName: String,
  val displayValue: String,
  val tone: K1DimensionTone,
  val period: String?,
)

/**
 * Pure-logic resolver — given a [DimensionScore] (or a missing key), produces the
 * label, value, tone, and period text that the K1 score box should display.
 */
object K1DimensionResolver {

  private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
  }

  fun resolve(d: DimensionScore): ResolvedDimension {
    val parsed: JsonObject? = try {
      if (d.details.isBlank()) {
        null
      } else {
        json.parseToJsonElement(d.details).jsonObject
      }
    } catch (e: Throwable) {
      KlikLogger.w("K1DimensionRow", "Failed to parse dimension details: ${e.message}", e)
      null
    }

    val statusRaw = parsed?.get("status")?.jsonPrimitive?.content
    val levelRaw = parsed?.get("level")?.jsonPrimitive?.content
    val stateRaw = parsed?.get("state")?.jsonPrimitive?.content
    val textRaw = statusRaw ?: levelRaw ?: stateRaw

    val displayValue = when {
      !textRaw.isNullOrBlank() -> textRaw.titleCase()
      d.score != null -> d.score.toInt().toString()
      else -> "—"
    }

    val tone = computeTone(d.dimension, statusRaw?.lowercase(), d.score)

    return ResolvedDimension(
      displayName = displayName(d.dimension),
      displayValue = displayValue,
      tone = tone,
      period = d.periodType.takeIf { it.isNotBlank() },
    )
  }

  /** Placeholder for an entity that has no score yet for [dimensionKey]. */
  fun resolveMissing(dimensionKey: String): ResolvedDimension = ResolvedDimension(
    displayName = displayName(dimensionKey),
    displayValue = "—",
    tone = K1DimensionTone.NEUTRAL,
    period = null,
  )

  private fun displayName(raw: String): String = raw.split("_").joinToString(" ") { part ->
    part.replaceFirstChar { it.uppercaseChar() }
  }

  private fun String.titleCase(): String = split(" ").joinToString(" ") { part ->
    part.replaceFirstChar { it.uppercaseChar() }
  }

  private fun computeTone(
    dimension: String,
    statusLower: String?,
    score: Float?,
  ): K1DimensionTone {
    // Per-dimension status overrides (matches OLD WorkLifeScreen.kt:3272-3289).
    when (dimension.lowercase()) {
      "health" -> when (statusLower) {
        "green" -> return K1DimensionTone.POSITIVE
        "yellow" -> return K1DimensionTone.WATCH
        "red" -> return K1DimensionTone.ALERT
      }

      "weather" -> when (statusLower) {
        "sunny" -> return K1DimensionTone.POSITIVE
        "partly cloudy" -> return K1DimensionTone.WATCH
        "foggy" -> return K1DimensionTone.NEUTRAL
        "stormy", "rainy" -> return K1DimensionTone.ALERT
      }
    }
    // Generic numeric thresholds for every other dimension.
    return when {
      score == null -> K1DimensionTone.NEUTRAL
      score >= 70f -> K1DimensionTone.POSITIVE
      score >= 40f -> K1DimensionTone.WATCH
      else -> K1DimensionTone.ALERT
    }
  }
}

// ─── Composables ──────────────────────────────────────────────────────────

/**
 * Three-up row of K1 score boxes for an entity's signal dimensions. Order is
 * fixed by [keys] so the same column always means the same thing across the
 * list (e.g. People always show Voice / Connection / Reliability left-to-right).
 *
 * If a key is absent from [dimensions], the box renders a visible "(none)"
 * placeholder rather than silently disappearing — matches the NO SILENT
 * SWALLOW rule on the parent project.
 */
@Composable
fun K1DimensionRow(
  keys: List<String>,
  dimensions: List<DimensionScore>,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    keys.forEach { key ->
      val match = dimensions.firstOrNull { it.dimension.equals(key, ignoreCase = true) }
      val resolved = if (match != null) {
        K1DimensionResolver.resolve(match)
      } else {
        K1DimensionResolver.resolveMissing(key)
      }
      K1DimensionBox(resolved, Modifier.weight(1f))
    }
  }
}

@Composable
private fun K1DimensionBox(
  d: ResolvedDimension,
  modifier: Modifier = Modifier,
) {
  val toneColor = when (d.tone) {
    K1DimensionTone.POSITIVE -> KlikRunning
    K1DimensionTone.WATCH -> KlikWarn
    K1DimensionTone.ALERT -> KlikAlert
    K1DimensionTone.NEUTRAL -> KlikInkMuted
  }
  Column(
    modifier
      .clip(K1R.soft)
      .background(KlikPaperSoft)
      .padding(horizontal = 12.dp, vertical = 10.dp),
  ) {
    Text(
      d.displayName.uppercase(),
      style = K1Type.eyebrow.copy(color = KlikInkTertiary),
      maxLines = 1,
    )
    Spacer(Modifier.height(6.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(
        d.displayValue,
        style = K1Type.bodyMd.copy(
          color = if (d.tone == K1DimensionTone.NEUTRAL) KlikInkMuted else KlikInkPrimary,
          fontWeight = FontWeight.Medium,
          fontSize = 15.sp,
        ),
        maxLines = 1,
      )
      if (d.tone != K1DimensionTone.NEUTRAL) {
        Spacer(Modifier.width(6.dp))
        Box(
          Modifier
            .size(5.dp)
            .background(toneColor, CircleShape),
        )
      }
    }
    if (d.period != null) {
      Spacer(Modifier.height(4.dp))
      Text(
        d.period.replace("_", " "),
        style = K1Type.metaSm.copy(color = KlikInkMuted),
        maxLines = 1,
      )
    }
  }
}
