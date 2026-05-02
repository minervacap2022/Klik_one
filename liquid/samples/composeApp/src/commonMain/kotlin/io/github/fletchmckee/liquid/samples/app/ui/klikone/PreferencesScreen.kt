// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.ui.klikone

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.fletchmckee.liquid.samples.app.data.network.dto.RemoteUserPreferencesDto
import io.github.fletchmckee.liquid.samples.app.data.source.remote.RemoteDataFetcher
import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkPrimary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkSecondary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkTertiary
import io.github.fletchmckee.liquid.samples.app.theme.KlikLineHairline
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperApp
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperCard
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperChip
import kotlinx.coroutines.launch

/**
 * K1 — Preferences. Cross-device sync of UI prefs (background, font, dark
 * mode, notifications master switch, haptic). Reads + writes to `/api/v1/user/preferences`.
 *
 * Server is the source of truth. Each toggle change autosaves the full prefs
 * object via PUT. If save fails the toggle reverts and the failure surfaces
 * in the inline status row — no silent swallow.
 */
@Composable
fun PreferencesScreen(onBack: () -> Unit) {
  var prefs by remember { mutableStateOf<RemoteUserPreferencesDto?>(null) }
  var error by remember { mutableStateOf<String?>(null) }
  var saving by remember { mutableStateOf(false) }
  val scope = rememberCoroutineScope()

  LaunchedEffect(Unit) {
    try {
      prefs = RemoteDataFetcher.fetchUserPreferences()
    } catch (e: Exception) {
      error = e.message ?: "Failed to load preferences"
      KlikLogger.e("Preferences", "load failed: ${e.message}", e)
    }
  }

  fun update(transform: (RemoteUserPreferencesDto) -> RemoteUserPreferencesDto) {
    val current = prefs ?: return
    val next = transform(current)
    prefs = next
    error = null
    saving = true
    scope.launch {
      try {
        prefs = RemoteDataFetcher.updateUserPreferences(next)
      } catch (e: Exception) {
        prefs = current
        error = e.message ?: "Save failed"
        KlikLogger.e("Preferences", "save failed: ${e.message}", e)
      } finally {
        saving = false
      }
    }
  }

  Column(
    Modifier
      .fillMaxSize()
      .background(KlikPaperApp)
      .k1SwipeBack(onBack)
      .verticalScroll(rememberScrollState()),
  ) {
    Row(
      Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(
        Modifier.k1Clickable(onClick = onBack).padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text("‹", style = K1Type.h2.copy(color = KlikInkPrimary))
      }
      Spacer(Modifier.size(4.dp))
      Text("Preferences", style = K1Type.h2)
    }

    val ui = prefs
    if (ui == null && error == null) {
      Column(Modifier.padding(20.dp)) {
        K1SkeletonCard(lines = 4)
      }
    } else if (ui == null) {
      Column(Modifier.padding(20.dp)) {
        K1Card(soft = true) {
          Text(
            "Couldn't load preferences",
            style = K1Type.bodyMd.copy(color = KlikInkPrimary, fontWeight = FontWeight.Medium),
          )
          Spacer(Modifier.height(4.dp))
          Text(error.orEmpty(), style = K1Type.bodySm.copy(color = KlikInkSecondary))
        }
      }
    } else {
      Column(Modifier.padding(horizontal = 20.dp)) {
        K1Eyebrow("Appearance")
        Spacer(Modifier.height(K1Sp.s))
        Column(
          Modifier.fillMaxWidth().clip(K1R.card).background(KlikPaperCard).padding(vertical = 4.dp),
        ) {
          Stepper(
            label = "Background",
            value = ui.selectedBackgroundIndex,
            min = 0,
            max = 30,
            onChange = { v -> update { it.copy(selectedBackgroundIndex = v) } },
          )
          Divider()
          Stepper(
            label = "Font",
            value = ui.selectedFontIndex,
            min = 0,
            max = 7,
            onChange = { v -> update { it.copy(selectedFontIndex = v) } },
          )
          Divider()
          Toggle(
            label = "Dark mode",
            value = ui.darkModeEnabled,
            onChange = { v -> update { it.copy(darkModeEnabled = v) } },
          )
        }

        Spacer(Modifier.height(K1Sp.xxl))
        K1Eyebrow("Feedback")
        Spacer(Modifier.height(K1Sp.s))
        Column(
          Modifier.fillMaxWidth().clip(K1R.card).background(KlikPaperCard).padding(vertical = 4.dp),
        ) {
          Toggle(
            label = "Notifications",
            value = ui.notificationsEnabled,
            onChange = { v -> update { it.copy(notificationsEnabled = v) } },
          )
          Divider()
          Toggle(
            label = "Haptic feedback",
            value = ui.hapticFeedbackEnabled,
            onChange = { v -> update { it.copy(hapticFeedbackEnabled = v) } },
          )
        }

        Spacer(Modifier.height(K1Sp.lg))
        Row(verticalAlignment = Alignment.CenterVertically) {
          val statusText = when {
            error != null -> error.orEmpty()
            saving -> "Saving…"
            else -> "Synced across devices"
          }
          val statusColor = if (error != null) KlikInkPrimary else KlikInkTertiary
          Text(statusText, style = K1Type.metaSm.copy(color = statusColor))
        }
        Spacer(Modifier.height(K1Sp.xxl))
      }
    }
  }
}

@Composable
private fun Toggle(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
  Row(
    Modifier.fillMaxWidth().k1Clickable { onChange(!value) }.padding(horizontal = 16.dp, vertical = 14.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(label, style = K1Type.bodyMd.copy(color = KlikInkPrimary), modifier = Modifier.padding(end = 8.dp))
    Spacer(Modifier.weight(1f))
    Box(
      Modifier
        .clip(K1R.pill)
        .background(if (value) KlikInkPrimary else KlikPaperChip)
        .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
      Text(
        if (value) "On" else "Off",
        style = K1Type.metaSm.copy(
          color = if (value) KlikPaperCard else KlikInkSecondary,
          fontWeight = FontWeight.Medium,
        ),
      )
    }
  }
}

@Composable
private fun Stepper(
  label: String,
  value: Int,
  min: Int,
  max: Int,
  onChange: (Int) -> Unit,
) {
  Row(
    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(label, style = K1Type.bodyMd.copy(color = KlikInkPrimary))
    Spacer(Modifier.weight(1f))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
      Box(
        Modifier
          .clip(CircleShape)
          .background(KlikPaperChip)
          .k1Clickable(enabled = value > min) { onChange((value - 1).coerceAtLeast(min)) }
          .padding(horizontal = 12.dp, vertical = 6.dp),
      ) { Text("−", style = K1Type.bodyMd.copy(color = KlikInkPrimary)) }
      Text(
        value.toString(),
        style = K1Type.bodyMd.copy(color = KlikInkPrimary, fontWeight = FontWeight.Medium),
        modifier = Modifier.padding(horizontal = 4.dp),
      )
      Box(
        Modifier
          .clip(CircleShape)
          .background(KlikPaperChip)
          .k1Clickable(enabled = value < max) { onChange((value + 1).coerceAtMost(max)) }
          .padding(horizontal = 12.dp, vertical = 6.dp),
      ) { Text("+", style = K1Type.bodyMd.copy(color = KlikInkPrimary)) }
    }
  }
}

@Composable
private fun Divider() {
  Box(Modifier.fillMaxWidth().height(0.75.dp).background(KlikLineHairline))
}
