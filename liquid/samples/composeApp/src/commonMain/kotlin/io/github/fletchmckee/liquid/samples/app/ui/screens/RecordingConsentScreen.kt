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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import io.github.fletchmckee.liquid.samples.app.data.network.ApiConfig
import io.github.fletchmckee.liquid.samples.app.platform.OAuthBrowser
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkPrimary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkSecondary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkTertiary
import io.github.fletchmckee.liquid.samples.app.theme.KlikLineHairline
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperApp
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperCard
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1ButtonPrimary
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1Eyebrow
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1Signal
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1SignalCard
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1Sp
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1Type
import io.github.fletchmckee.liquid.samples.app.ui.klikone.k1Clickable

@Composable
fun RecordingConsentScreen(
  onAccept: () -> Unit,
  onBack: () -> Unit,
  isOnboarding: Boolean = false,
  onSignOut: () -> Unit = {},
) {
  var informParticipants by remember { mutableStateOf(false) }
  var acceptResponsibility by remember { mutableStateOf(false) }
  var agreeToTerms by remember { mutableStateOf(false) }

  val allChecked = informParticipants && acceptResponsibility && agreeToTerms

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(KlikPaperApp)
      .statusBarsPadding()
      .navigationBarsPadding(),
  ) {
    // Top rail — editorial. No chrome buttons; text-only affordances.
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
        "Recording consent.",
        style = K1Type.display,
      )
      Spacer(Modifier.height(K1Sp.m))
      Text(
        "A quick acknowledgement before Klik can listen, transcribe, and quietly handle your meetings.",
        style = K1Type.bodySm.copy(color = KlikInkSecondary),
      )

      Spacer(Modifier.height(K1Sp.xxl))

      // Section 1 — What Klik records
      K1Eyebrow("What Klik records")
      Spacer(Modifier.height(K1Sp.m))
      ConsentBullet(
        "Klik records audio during your sessions to generate transcripts, summaries, and action items.",
      )
      ConsentBullet(
        "Recordings are processed with AI and stored securely on our servers.",
      )
      ConsentBullet(
        "You can delete your recordings any time from your account settings.",
      )

      Spacer(Modifier.height(K1Sp.xxl))

      // All-party consent warning — signal card in the K1 system
      K1SignalCard(
        signal = K1Signal.Risk,
        eyebrow = "All-party consent",
        body = "Your state may require consent from every participant before recording. Applies to residents of CA, FL, IL, MA, WA, DE, MD, MT, NV, NH, and PA.",
      )

      Spacer(Modifier.height(K1Sp.xxl))

      // Section 2 — Acknowledgements
      K1Eyebrow("Your acknowledgement")
      Spacer(Modifier.height(K1Sp.m))
      K1ConsentRow(
        checked = informParticipants,
        onCheckedChange = { informParticipants = it },
        label = "I will inform all participants before recording.",
      )
      HairlineDivider()
      K1ConsentRow(
        checked = acceptResponsibility,
        onCheckedChange = { acceptResponsibility = it },
        label = "I accept responsibility for obtaining consent from all parties.",
      )
      HairlineDivider()
      K1ConsentRow(
        checked = agreeToTerms,
        onCheckedChange = { agreeToTerms = it },
        label = "I have read and agree to the Privacy Policy and Terms of Service.",
      )

      Spacer(Modifier.height(K1Sp.xxl))

      // Policy links, text-only
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          "Privacy Policy",
          style = K1Type.metaSm.copy(
            color = KlikInkSecondary,
            textDecoration = TextDecoration.Underline,
          ),
          modifier = Modifier.k1Clickable {
            OAuthBrowser.openUrl(ApiConfig.PRIVACY_URL)
          },
        )
        Spacer(Modifier.width(K1Sp.xl))
        Text(
          "Terms of Service",
          style = K1Type.metaSm.copy(
            color = KlikInkSecondary,
            textDecoration = TextDecoration.Underline,
          ),
          modifier = Modifier.k1Clickable {
            OAuthBrowser.openUrl(ApiConfig.TERMS_URL)
          },
        )
      }

      Spacer(Modifier.height(K1Sp.xxl))

      K1ButtonPrimary(
        label = "I accept",
        onClick = onAccept,
        enabled = allChecked,
        modifier = Modifier.fillMaxWidth(),
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
    // Tiny paper-disc bullet in the K1 style.
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
      Text(
        "✓",
        style = K1Type.metaSm.copy(color = KlikPaperCard),
      )
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
