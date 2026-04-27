// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import io.github.fletchmckee.liquid.LiquidState
import io.github.fletchmckee.liquid.liquid
import io.github.fletchmckee.liquid.samples.app.domain.entity.TracedSegment
import io.github.fletchmckee.liquid.samples.app.theme.LocalLiquidGlassSettings

/**
 * Callback for traced segment navigation
 */
data class TracedSegmentNavigation(
  val sessionId: String,
  val segmentId: String,
)

/**
 * Displays a list of traced segments as clickable chips.
 * Similar to AskKlik SourcesSection but tailored for TracedSegment data.
 *
 * @param liquidState LiquidState for glass morphism effects
 * @param segments List of traced segments to display
 * @param onSegmentClick Callback when a segment is clicked
 * @param modifier Modifier for the section
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TracedSegmentsSection(
  liquidState: LiquidState,
  segments: List<TracedSegment>,
  onSegmentClick: (TracedSegmentNavigation) -> Unit,
  modifier: Modifier = Modifier,
) {
  if (segments.isEmpty()) return

  Column(modifier = modifier.fillMaxWidth()) {
    Text(
      "Sources",
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
      modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
    )

    FlowRow(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      segments.forEach { segment ->
        TracedSegmentChip(
          liquidState = liquidState,
          segment = segment,
          onClick = {
            onSegmentClick(
              TracedSegmentNavigation(
                sessionId = segment.sessionId,
                segmentId = segment.segmentId,
              ),
            )
          },
        )
      }
    }
  }
}

/**
 * Individual traced segment chip.
 * Displays meeting title and speaker name, with glass morphism effects.
 *
 * @param liquidState LiquidState for glass morphism effects
 * @param segment Traced segment to display
 * @param onClick Callback when chip is clicked
 */
@Composable
fun TracedSegmentChip(
  liquidState: LiquidState,
  segment: TracedSegment,
  onClick: () -> Unit,
) {
  val glassSettings = LocalLiquidGlassSettings.current
  val chipShape = RoundedCornerShape(12.dp)
  val chipColor = Color(0xFF4CAF50) // Green for meeting segments

  // Build display title from meeting title and speaker name
  val displayTitle = buildString {
    // Use meeting title if available; otherwise use first few words of segment text
    val title = segment.meetingTitle
      ?: segment.text.split(" ").take(5).joinToString(" ")
    append(title)

    // Only show speaker name if it's a real name (not a voiceprint ID)
    segment.speakerName?.let { speaker ->
      if (speaker.isNotEmpty() && !speaker.startsWith("VP_")) {
        append(" ($speaker)")
      }
    }
  }

  Box(
    modifier = Modifier
      .background(chipColor.copy(alpha = 0.1f), chipShape)
      .border(BorderStroke(0.5.dp, chipColor.copy(alpha = 0.3f)), chipShape)
      .liquid(liquidState) {
        edge = glassSettings.edge * 0.5f
        shape = chipShape
        frost = glassSettings.frost * 0.3f
        curve = glassSettings.curve
        refraction = glassSettings.refraction * 0.3f
        tint = chipColor.copy(alpha = 0.05f)
      }
      .clip(chipShape)
      .clickable(onClick = onClick)
      .padding(horizontal = 10.dp, vertical = 6.dp),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      Text(
        "\uD83C\uDF99️", // 🎙️ microphone emoji
        style = MaterialTheme.typography.labelSmall,
      )
      Text(
        displayTitle.take(30) + if (displayTitle.length > 30) "..." else "",
        style = MaterialTheme.typography.labelSmall,
        color = chipColor.copy(alpha = 0.9f),
        fontWeight = FontWeight.Medium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

/**
 * Expanded traced segment card showing full segment text.
 * Can be used in a modal or dialog to show segment details.
 *
 * @param liquidState LiquidState for glass morphism effects
 * @param segment Traced segment to display
 * @param modifier Modifier for the card
 */
@Composable
fun TracedSegmentCard(
  liquidState: LiquidState,
  segment: TracedSegment,
  modifier: Modifier = Modifier,
) {
  val glassSettings = LocalLiquidGlassSettings.current
  val cardShape = RoundedCornerShape(16.dp)
  val cardColor = Color.White

  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(cardColor.copy(alpha = 0.95f), cardShape)
      .border(BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.12f)), cardShape)
      .liquid(liquidState) {
        edge = glassSettings.edge
        shape = cardShape
        frost = glassSettings.frost
        curve = glassSettings.curve
        refraction = glassSettings.refraction
        tint = Color.Transparent
      }
      .clip(cardShape)
      .padding(16.dp),
  ) {
    // Meeting title
    segment.meetingTitle?.let { title ->
      Text(
        title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
      )
      Spacer(Modifier.height(4.dp))
    }

    // Speaker name and meeting time
    Row(
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      modifier = Modifier.fillMaxWidth(),
    ) {
      segment.speakerName?.let { speaker ->
        Text(
          "Speaker: $speaker",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
      }

      segment.meetingTime?.let { time ->
        Text(
          "Time: $time",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
      }
    }

    Spacer(Modifier.height(12.dp))

    // Segment text with keyword highlighting
    val highlightColor = Color(0xFF4CAF50) // Green for highlighted keywords
    val annotatedText = buildAnnotatedString {
      if (segment.matchedKeywords.isEmpty()) {
        // No keywords to highlight, display plain text
        append(segment.text)
      } else {
        // Find all keyword matches and sort by position
        val lowerText = segment.text.lowercase()
        val matches = segment.matchedKeywords.flatMap { keyword ->
          val lowerKeyword = keyword.lowercase()
          val matchRanges = mutableListOf<IntRange>()
          var startIdx = 0
          while (startIdx < lowerText.length) {
            val idx = lowerText.indexOf(lowerKeyword, startIdx)
            if (idx == -1) break
            matchRanges.add(idx until (idx + keyword.length))
            startIdx = idx + 1
          }
          matchRanges
        }.sortedBy { it.first }

        // Build annotated string with highlighted matches
        var lastEnd = 0
        for (range in matches) {
          // Skip overlapping ranges
          if (range.first < lastEnd) continue

          // Append text before match
          if (range.first > lastEnd) {
            append(segment.text.substring(lastEnd, range.first))
          }

          // Append highlighted keyword
          withStyle(
            SpanStyle(
              color = highlightColor,
              fontWeight = FontWeight.Medium,
              background = highlightColor.copy(alpha = 0.1f),
            ),
          ) {
            append(segment.text.substring(range.first, range.last + 1))
          }

          lastEnd = range.last + 1
        }

        // Append remaining text after last match
        if (lastEnd < segment.text.length) {
          append(segment.text.substring(lastEnd))
        }
      }
    }

    Text(
      annotatedText,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurface,
    )

    // Score indicator
    if (segment.score > 0) {
      Spacer(Modifier.height(8.dp))
      Text(
        "Relevance: ${(segment.score * 100).toInt()}%",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
      )
    }
  }
}
