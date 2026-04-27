// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.fletchmckee.liquid.samples.app.theme.KlikBlack

@Composable
fun AiGeneratedBadge(label: String, modifier: Modifier = Modifier) {
  val badgeBackground = Color(0xFF9E9E9E).copy(alpha = 0.12f)
  val badgeContentColor = Color(0xFF757575)

  Row(
    modifier = modifier
      .background(badgeBackground, RoundedCornerShape(8.dp))
      .padding(horizontal = 6.dp, vertical = 2.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(3.dp),
  ) {
    Icon(
      imageVector = Icons.Filled.Star,
      contentDescription = null,
      tint = badgeContentColor,
      modifier = Modifier.size(10.dp),
    )
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = badgeContentColor,
    )
  }
}

@Composable
fun AiContentDisclaimer(modifier: Modifier = Modifier) {
  Text(
    text = "AI-generated content may contain inaccuracies. Please verify important information.",
    style = MaterialTheme.typography.labelSmall,
    color = KlikBlack.copy(alpha = 0.4f),
    modifier = modifier.padding(top = 8.dp),
  )
}
