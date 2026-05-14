// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.ui.klikone

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.fletchmckee.liquid.samples.app.data.network.dto.RemoteUserPreferencesDto
import io.github.fletchmckee.liquid.samples.app.data.source.remote.RemoteDataFetcher
import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import io.github.fletchmckee.liquid.samples.app.model.appLanguageState
import io.github.fletchmckee.liquid.samples.app.model.darkModeEnabledState
import io.github.fletchmckee.liquid.samples.app.model.fontSizeIndexState
import io.github.fletchmckee.liquid.samples.app.model.hapticEnabledState
import io.github.fletchmckee.liquid.samples.app.model.selectedFontIndexState
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkPrimary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkSecondary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkTertiary
import io.github.fletchmckee.liquid.samples.app.theme.KlikLineHairline
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperApp
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperCard
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperChip
import kotlinx.coroutines.launch

/**
 * K1 — Preferences. Cross-device sync of UI prefs (font family, dark mode,
 * haptic feedback). Reads + writes `/api/v1/user/preferences`.
 *
 * Each toggle change autosaves via PUT. If save fails the toggle reverts and
 * the failure surfaces in the inline status row — no silent swallow.
 *
 * Notifications live on their own screen (see NotificationSettingsScreen) so
 * we don't duplicate the canonical push/digest controls here — this row is a
 * navigation, not a master toggle.
 */
@Composable
fun PreferencesScreen(
  onBack: () -> Unit,
  onOpenNotifications: () -> Unit,
) {
  val s = LocalKlikStrings.current
  var prefs by remember { mutableStateOf<RemoteUserPreferencesDto?>(null) }
  var error by remember { mutableStateOf<String?>(null) }
  var saving by remember { mutableStateOf(false) }
  val scope = rememberCoroutineScope()

  LaunchedEffect(Unit) {
    try {
      val loaded = RemoteDataFetcher.fetchUserPreferences()
      prefs = loaded
      applyToGlobals(loaded)
    } catch (e: Exception) {
      error = e.message ?: "Failed to load preferences"
      KlikLogger.e("Preferences", "load failed: ${e.message}", e)
    }
  }

  fun update(transform: (RemoteUserPreferencesDto) -> RemoteUserPreferencesDto) {
    val current = prefs ?: return
    val next = transform(current)
    prefs = next
    applyToGlobals(next)
    error = null
    saving = true
    scope.launch {
      try {
        val saved = RemoteDataFetcher.updateUserPreferences(next)
        prefs = saved
        applyToGlobals(saved)
      } catch (e: Exception) {
        prefs = current
        applyToGlobals(current)
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
      Box(
        Modifier.size(32.dp).k1Clickable(onClick = onBack),
        contentAlignment = Alignment.Center,
      ) { K1BackChevronGlyph() }
      Spacer(Modifier.weight(1f))
    }

    Column(Modifier.padding(horizontal = 20.dp)) {
      K1Eyebrow(s.settings)
      Spacer(Modifier.height(6.dp))
      Text(s.preferences, style = K1Type.h2)
      Spacer(Modifier.height(4.dp))
      Text(
        s.preferencesSubtitle,
        style = K1Type.bodySm.copy(color = KlikInkSecondary),
      )
    }
    Spacer(Modifier.height(K1Sp.xl))

    val ui = prefs
    if (ui == null && error == null) {
      Column(Modifier.padding(20.dp)) {
        K1SkeletonCard(lines = 4)
      }
    } else if (ui == null) {
      Column(Modifier.padding(20.dp)) {
        K1Card(soft = true) {
          Text(
            s.couldntLoadPreferences,
            style = K1Type.bodyMd.copy(color = KlikInkPrimary, fontWeight = FontWeight.Medium),
          )
          Spacer(Modifier.height(4.dp))
          Text(error.orEmpty(), style = K1Type.bodySm.copy(color = KlikInkSecondary))
        }
      }
    } else {
      Column(Modifier.padding(horizontal = 20.dp)) {
        K1Eyebrow(s.appearance)
        Spacer(Modifier.height(K1Sp.s))
        Column(
          Modifier.fillMaxWidth().clip(K1R.card).background(KlikPaperCard).padding(vertical = 4.dp),
        ) {
          FontPicker(
            label = s.font,
            value = ui.selectedFontIndex,
            onChange = { v -> update { it.copy(selectedFontIndex = v) } },
          )
          Divider()
          FontSizePicker(
            value = ui.fontSizeIndex,
            onChange = { v -> update { it.copy(fontSizeIndex = v) } },
          )
          Divider()
          Toggle(
            label = s.darkMode,
            value = ui.darkModeEnabled,
            onChange = { v -> update { it.copy(darkModeEnabled = v) } },
          )
        }

        Spacer(Modifier.height(K1Sp.xxl))
        K1Eyebrow(s.language)
        Spacer(Modifier.height(K1Sp.s))
        Column(
          Modifier.fillMaxWidth().clip(K1R.card).background(KlikPaperCard).padding(vertical = 4.dp),
        ) {
          LanguagePicker(
            value = ui.language,
            onChange = { v -> update { it.copy(language = v) } },
          )
        }

        Spacer(Modifier.height(K1Sp.xxl))
        K1Eyebrow(s.feedback)
        Spacer(Modifier.height(K1Sp.s))
        Column(
          Modifier.fillMaxWidth().clip(K1R.card).background(KlikPaperCard).padding(vertical = 4.dp),
        ) {
          NavRow(label = s.notifications, onClick = onOpenNotifications)
          Divider()
          Toggle(
            label = s.hapticFeedback,
            value = ui.hapticFeedbackEnabled,
            onChange = { v -> update { it.copy(hapticFeedbackEnabled = v) } },
          )
        }

        Spacer(Modifier.height(K1Sp.lg))
        Row(verticalAlignment = Alignment.CenterVertically) {
          val statusText = when {
            error != null -> error.orEmpty()
            saving -> s.savingEllipsis
            else -> s.syncedAcrossDevices
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

// K1FontIndex.kt — the three fonts the user can pick. Indexes are stable.
private val K1_FONT_LABELS = listOf("System", "Serif", "Mono")

@Composable
private fun FontPicker(label: String, value: Int, onChange: (Int) -> Unit) {
  val safeIndex = value.coerceIn(0, K1_FONT_LABELS.lastIndex)
  Row(
    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(label, style = K1Type.bodyMd.copy(color = KlikInkPrimary))
    Spacer(Modifier.weight(1f))
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
      K1_FONT_LABELS.forEachIndexed { index, name ->
        val selected = index == safeIndex
        Box(
          Modifier
            .clip(K1R.pill)
            .background(if (selected) KlikInkPrimary else KlikPaperChip)
            .k1Clickable { onChange(index) }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
          Text(
            name,
            style = K1Type.metaSm.copy(
              color = if (selected) KlikPaperCard else KlikInkSecondary,
              fontWeight = FontWeight.Medium,
            ),
          )
        }
      }
    }
  }
}

private val K1_SIZE_LABELS = listOf("S", "M", "L", "XL")

@Composable
private fun FontSizePicker(value: Int, onChange: (Int) -> Unit) {
  val safeIndex = value.coerceIn(0, K1_SIZE_LABELS.lastIndex)
  Row(
    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(LocalKlikStrings.current.textSize, style = K1Type.bodyMd.copy(color = KlikInkPrimary))
    Spacer(Modifier.weight(1f))
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
      K1_SIZE_LABELS.forEachIndexed { index, label ->
        val selected = index == safeIndex
        Box(
          Modifier
            .clip(K1R.pill)
            .background(if (selected) KlikInkPrimary else KlikPaperChip)
            .k1Clickable { onChange(index) }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
          Text(
            label,
            style = K1Type.metaSm.copy(
              color = if (selected) KlikPaperCard else KlikInkSecondary,
              fontWeight = FontWeight.Medium,
            ),
          )
        }
      }
    }
  }
}

/**
 * Push the persisted-prefs DTO into the three global state slots that the
 * theme + haptic engine read. Keeping this in one place avoids the toggles
 * drifting from the global state — every fetch + every save funnels here.
 */
private fun applyToGlobals(p: RemoteUserPreferencesDto) {
  darkModeEnabledState.value = p.darkModeEnabled
  selectedFontIndexState.value = p.selectedFontIndex
  fontSizeIndexState.value = p.fontSizeIndex
  hapticEnabledState.value = p.hapticFeedbackEnabled
  appLanguageState.value = p.language
  RemoteDataFetcher.currentLanguage = p.language
}

@Composable
private fun NavRow(label: String, onClick: () -> Unit) {
  Row(
    Modifier.fillMaxWidth().k1Clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(label, style = K1Type.bodyMd.copy(color = KlikInkPrimary))
    Spacer(Modifier.weight(1f))
    Text("›", style = K1Type.bodyMd.copy(color = KlikInkTertiary))
  }
}

@Composable
private fun Divider() {
  Box(Modifier.fillMaxWidth().height(0.75.dp).background(KlikLineHairline))
}

@Composable
private fun K1BackChevronGlyph() {
  Canvas(Modifier.size(16.dp)) {
    val w = 1.3.dp.toPx()
    drawLine(
      color = KlikInkPrimary,
      strokeWidth = w,
      cap = StrokeCap.Round,
      start = Offset(10.dp.toPx(), 3.5.dp.toPx()),
      end = Offset(5.5.dp.toPx(), 8.dp.toPx()),
    )
    drawLine(
      color = KlikInkPrimary,
      strokeWidth = w,
      cap = StrokeCap.Round,
      start = Offset(5.5.dp.toPx(), 8.dp.toPx()),
      end = Offset(10.dp.toPx(), 12.5.dp.toPx()),
    )
  }
}

private val K1_LANG_OPTIONS = listOf(
  "en" to "English",
  "zh" to "中文",
  "es" to "Español",
  "fr" to "Français",
  "de" to "Deutsch",
  "ja" to "日本語",
  "ko" to "한국어",
  "pt" to "Português",
)

@Composable
private fun LanguagePicker(value: String, onChange: (String) -> Unit) {
  val safeLang = if (K1_LANG_OPTIONS.any { it.first == value }) value else "en"
  Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
      K1_LANG_OPTIONS.take(4).forEach { (code, label) ->
        val selected = code == safeLang
        Box(
          Modifier
            .weight(1f)
            .clip(K1R.pill)
            .background(if (selected) KlikInkPrimary else KlikPaperChip)
            .k1Clickable { onChange(code) }
            .padding(horizontal = 8.dp, vertical = 6.dp),
          contentAlignment = androidx.compose.ui.Alignment.Center,
        ) {
          Text(
            label,
            style = K1Type.metaSm.copy(
              color = if (selected) KlikPaperCard else KlikInkSecondary,
              fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
            ),
          )
        }
      }
    }
    Spacer(Modifier.height(6.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
      K1_LANG_OPTIONS.drop(4).forEach { (code, label) ->
        val selected = code == safeLang
        Box(
          Modifier
            .weight(1f)
            .clip(K1R.pill)
            .background(if (selected) KlikInkPrimary else KlikPaperChip)
            .k1Clickable { onChange(code) }
            .padding(horizontal = 8.dp, vertical = 6.dp),
          contentAlignment = androidx.compose.ui.Alignment.Center,
        ) {
          Text(
            label,
            style = K1Type.metaSm.copy(
              color = if (selected) KlikPaperCard else KlikInkSecondary,
              fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
            ),
          )
        }
      }
    }
  }
}
