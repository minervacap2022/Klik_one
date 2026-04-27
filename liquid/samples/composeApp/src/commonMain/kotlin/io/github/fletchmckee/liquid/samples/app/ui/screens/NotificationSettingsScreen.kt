// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkPrimary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkSecondary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkTertiary
import io.github.fletchmckee.liquid.samples.app.theme.KlikLineHairline
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperApp
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperCard
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperSoft
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1Eyebrow
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1Sp
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1Type
import io.github.fletchmckee.liquid.samples.app.ui.klikone.k1Clickable

@Composable
fun NotificationSettingsScreen(
  onSavePreferences: (meetingReminders: Boolean, taskUpdates: Boolean, insightsDigest: Boolean, pushEnabled: Boolean) -> Unit,
  onBack: () -> Unit,
) {
  var meetingReminders by remember { mutableStateOf(true) }
  var taskUpdates by remember { mutableStateOf(true) }
  var insightsDigest by remember { mutableStateOf(true) }
  var pushEnabled by remember { mutableStateOf(true) }

  fun saveAndBack() {
    onSavePreferences(meetingReminders, taskUpdates, insightsDigest, pushEnabled)
    onBack()
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(KlikPaperApp)
      .statusBarsPadding()
      .navigationBarsPadding(),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        "Back",
        style = K1Type.metaSm.copy(color = KlikInkSecondary),
        modifier = Modifier.k1Clickable(onClick = ::saveAndBack).padding(end = K1Sp.m),
      )
      Box(Modifier.weight(1f))
    }

    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp),
    ) {
      K1Eyebrow("Klik")
      Spacer(Modifier.height(K1Sp.m))
      Text("Notifications.", style = K1Type.display)
      Spacer(Modifier.height(K1Sp.m))
      Text(
        "Pick what Klik taps you on the shoulder for.",
        style = K1Type.bodySm.copy(color = KlikInkSecondary),
      )

      Spacer(Modifier.height(K1Sp.xxl))

      K1Eyebrow("Push")
      Spacer(Modifier.height(K1Sp.m))
      K1ToggleRow(
        label = "Enable push notifications",
        description = "Receive Klik notifications on this device.",
        checked = pushEnabled,
        onCheckedChange = { pushEnabled = it },
      )

      Spacer(Modifier.height(K1Sp.xxl))

      K1Eyebrow("What to notify")
      Spacer(Modifier.height(K1Sp.m))
      K1ToggleRow(
        label = "Meeting reminders",
        description = "A quiet ping before an upcoming meeting.",
        checked = meetingReminders,
        onCheckedChange = { meetingReminders = it },
      )
      HairlineDivider()
      K1ToggleRow(
        label = "Task updates",
        description = "When someone assigns you something or a status changes.",
        checked = taskUpdates,
        onCheckedChange = { taskUpdates = it },
      )
      HairlineDivider()
      K1ToggleRow(
        label = "Insights digest",
        description = "A short daily read on what Klik noticed.",
        checked = insightsDigest,
        onCheckedChange = { insightsDigest = it },
      )

      Spacer(Modifier.height(K1Sp.xxl))
    }
  }
}

@Composable
private fun K1ToggleRow(
  label: String,
  description: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .k1Clickable { onCheckedChange(!checked) }
      .padding(vertical = 14.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(label, style = K1Type.bodyMd.copy(color = KlikInkPrimary))
      Spacer(Modifier.height(2.dp))
      Text(description, style = K1Type.metaSm.copy(color = KlikInkTertiary))
    }
    Spacer(Modifier.width(16.dp))
    K1Switch(checked = checked)
  }
}

/** K1 editorial switch — paper track, ink thumb, no Material gloss. */
@Composable
private fun K1Switch(checked: Boolean) {
  val thumbOffset by animateDpAsState(
    targetValue = if (checked) 18.dp else 2.dp,
    animationSpec = tween(180),
    label = "thumb",
  )
  Box(
    Modifier
      .size(width = 40.dp, height = 22.dp)
      .clip(CircleShape)
      .background(if (checked) KlikInkPrimary else KlikPaperSoft),
    contentAlignment = Alignment.CenterStart,
  ) {
    Box(
      Modifier
        .offset(x = thumbOffset)
        .size(18.dp)
        .clip(CircleShape)
        .background(KlikPaperCard),
    )
  }
}

@Composable
private fun HairlineDivider() {
  Box(
    Modifier
      .fillMaxWidth()
      .height(0.5.dp)
      .background(KlikLineHairline),
  )
}
