// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1ButtonPrimary
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1Eyebrow
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1Sp
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1Type
import io.github.fletchmckee.liquid.samples.app.ui.klikone.k1Clickable

@Composable
fun BiometricConsentScreen(
  onEnable: () -> Unit,
  onDecline: () -> Unit,
  onBack: () -> Unit,
  isOnboarding: Boolean = false,
  onSignOut: () -> Unit = {},
) {
  var acknowledgeNotice by remember { mutableStateOf(false) }
  var consentCollection by remember { mutableStateOf(false) }

  val allChecked = acknowledgeNotice && consentCollection

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(KlikPaperApp)
      .statusBarsPadding()
      .navigationBarsPadding(),
  ) {
    // Top rail — K1 editorial text-only chrome
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 16.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      if (!isOnboarding) {
        Text(
          "Back",
          style = K1Type.metaSm.copy(color = KlikInkSecondary),
          modifier = Modifier
            .k1Clickable(onClick = onBack)
            .padding(end = K1Sp.m),
        )
      }
      Box(Modifier.weight(1f))
      if (isOnboarding) {
        Text(
          "Sign out",
          style = K1Type.metaSm.copy(color = KlikInkSecondary),
          modifier = Modifier.k1Clickable(onClick = onSignOut),
        )
      }
    }

    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp, vertical = 8.dp),
    ) {
      // Editorial header
      K1Eyebrow("Klik", large = false)
      Spacer(Modifier.height(K1Sp.m))
      Text(
        "Speaker identification.",
        style = K1Type.display,
      )
      Spacer(Modifier.height(K1Sp.m))
      Text(
        "Let Klik recognize your voice across sessions so speakers stay named without manual tagging.",
        style = K1Type.bodySm.copy(color = KlikInkSecondary),
      )

      Spacer(Modifier.height(K1Sp.xxl))

      // Section 1 — How it works
      K1Eyebrow("How it works")
      Spacer(Modifier.height(K1Sp.m))
      ConsentBullet(
        "Klik derives voiceprints — numeric fingerprints — from your audio, then uses them to identify speakers across recordings.",
      )
      ConsentBullet(
        "Each voiceprint is a 192-dimensional vector, not a playable recording of your voice.",
      )
      ConsentBullet(
        "Voiceprints are stored securely and isolated to your account.",
      )
      ConsentBullet(
        "They're retained until you delete them or close the account.",
      )
      ConsentBullet(
        "You can delete your voiceprints at any time.",
      )

      Spacer(Modifier.height(K1Sp.xxl))

      // Section 2 — Consent
      K1Eyebrow("Your consent")
      Spacer(Modifier.height(K1Sp.m))
      K1ConsentRow(
        checked = acknowledgeNotice,
        onCheckedChange = { acknowledgeNotice = it },
        label = "I acknowledge this biometric data notice.",
      )
      HairlineDivider()
      K1ConsentRow(
        checked = consentCollection,
        onCheckedChange = { consentCollection = it },
        label = "I consent to the collection and use of my voiceprint for speaker identification.",
      )

      Spacer(Modifier.height(K1Sp.xxl))

      K1ButtonPrimary(
        label = "Enable speaker identification",
        onClick = onEnable,
        enabled = allChecked,
        modifier = Modifier.fillMaxWidth(),
      )

      if (!isOnboarding) {
        Spacer(Modifier.height(K1Sp.m))
        Text(
          "Continue without speaker ID",
          style = K1Type.bodyMd.copy(color = KlikInkSecondary),
          modifier = Modifier
            .fillMaxWidth()
            .k1Clickable(onClick = onDecline)
            .padding(vertical = 14.dp),
        )
      }

      Spacer(Modifier.height(K1Sp.lg))

      Text(
        "Recording still works without this — speakers will be labelled generically (Speaker 1, Speaker 2) with no cross-session matching.",
        style = K1Type.metaSm.copy(color = KlikInkTertiary),
      )

      Spacer(Modifier.height(K1Sp.xxl))
    }
  }
}

@Composable
private fun ConsentBullet(text: String) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
    horizontalArrangement = Arrangement.spacedBy(10.dp),
    verticalAlignment = Alignment.Top,
  ) {
    Box(
      Modifier
        .padding(top = 8.dp)
        .size(3.dp)
        .clip(CircleShape)
        .background(KlikInkTertiary),
    )
    Text(
      text,
      style = K1Type.bodySm.copy(color = KlikInkSecondary),
    )
  }
}

@Composable
private fun K1ConsentRow(
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  label: String,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .k1Clickable { onCheckedChange(!checked) }
      .padding(vertical = 14.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    K1CheckMark(checked = checked)
    Text(
      label,
      style = K1Type.bodySm.copy(color = KlikInkPrimary),
      modifier = Modifier.weight(1f),
    )
  }
}

@Composable
private fun K1CheckMark(checked: Boolean) {
  val shape = RoundedCornerShape(4.dp)
  Box(
    Modifier
      .size(18.dp)
      .clip(shape)
      .background(if (checked) KlikInkPrimary else KlikPaperCard)
      .border(0.75.dp, if (checked) KlikInkPrimary else KlikLineHairline, shape),
    contentAlignment = Alignment.Center,
  ) {
    if (checked) {
      Text("✓", style = K1Type.metaSm.copy(color = KlikPaperCard))
    }
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
