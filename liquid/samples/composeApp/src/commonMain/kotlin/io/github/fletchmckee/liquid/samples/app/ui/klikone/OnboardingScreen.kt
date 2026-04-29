// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.ui.klikone

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import io.github.fletchmckee.liquid.samples.app.platform.ImagePicker
import io.github.fletchmckee.liquid.samples.app.platform.PickedImage
import kotlinx.coroutines.launch
import io.github.fletchmckee.liquid.samples.app.theme.KlikAlert
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkFaint
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkMuted
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkPrimary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkSecondary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkTertiary
import io.github.fletchmckee.liquid.samples.app.theme.KlikLineHairline
import io.github.fletchmckee.liquid.samples.app.theme.KlikLineMute
import io.github.fletchmckee.liquid.samples.app.theme.KlikLineTick
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperApp
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperCard
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperChip
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperSoft

data class KlikOneRole(val id: String, val title: String, val subtitle: String)

private val KLIK_ROLES = listOf(
  KlikOneRole("founder", "Founder / Solopreneur", "Running a small team or solo"),
  KlikOneRole("consultant", "Consultant / Advisor", "High-volume client-facing work"),
  KlikOneRole("sales", "Sales / BD", "Managing a pipeline and follow-ups"),
  KlikOneRole("pm", "Product / PM", "Cross-team coordination"),
  KlikOneRole("investor", "Investor / VC", "Many meetings, many people"),
  KlikOneRole("other", "Something else", "Tell us a bit more"),
)

/** Klik One 4-step first-run: Welcome · Pair · Name + photo · Pick role. */
@Composable
fun OnboardingScreen(
  onComplete: (name: String, pickedRole: KlikOneRole?, avatar: PickedImage?) -> Unit,
  modifier: Modifier = Modifier,
) {
  var step by remember { mutableIntStateOf(0) }
  var pickedRoleId by remember { mutableStateOf<String?>("founder") }
  var fullName by remember { mutableStateOf("") }
  var pickedAvatar by remember { mutableStateOf<PickedImage?>(null) }
  val totalSteps = 4

  Box(modifier.fillMaxSize().background(KlikPaperCard)) {
    AnimatedContent(
      targetState = step,
      transitionSpec = {
        val forward = targetState > initialState
        val dir = if (forward) 1 else -1
        (slideInHorizontally(tween(280)) { it * dir } + fadeIn(tween(220))) togetherWith
          (slideOutHorizontally(tween(280)) { -it * dir } + fadeOut(tween(180)))
      },
      label = "ob-step",
    ) { current ->
      when (current) {
        0 -> WelcomeStep(onNext = { step = 1 }, totalSteps = totalSteps)

        1 -> PairStep(
          onSkip = { step = 2 },
          onBackDot = { step = 0 },
          totalSteps = totalSteps,
        )

        2 -> NameAndPhotoStep(
          name = fullName,
          onNameChange = { fullName = it },
          avatar = pickedAvatar,
          onPickAvatar = { pickedAvatar = it },
          onContinue = { step = 3 },
          onBackDot = { step = it },
          totalSteps = totalSteps,
        )

        3 -> PickRoleStep(
          pickedRoleId = pickedRoleId,
          onPick = { pickedRoleId = it },
          onContinue = {
            onComplete(
              fullName.trim(),
              KLIK_ROLES.firstOrNull { it.id == pickedRoleId },
              pickedAvatar,
            )
          },
          onBackDot = { step = it },
          totalSteps = totalSteps,
        )

        else -> Unit
      }
    }
  }
}

// ───────────────────────────────────────────────────────────── Welcome ────
@Composable
private fun WelcomeStep(onNext: () -> Unit, totalSteps: Int) {
  Column(Modifier.fillMaxSize().statusBarsPadding()) {
    Column(
      Modifier.weight(1f).padding(horizontal = 32.dp).padding(top = 40.dp),
      verticalArrangement = Arrangement.Center,
    ) {
      K1Waveform(
        heights = listOf(12f, 20f, 8f, 16f),
        barWidth = 3.dp,
        gap = 4.dp,
        color = KlikInkPrimary,
      )
      Spacer(Modifier.height(48.dp))
      K1Eyebrow("KLIK ONE", large = true)
      Spacer(Modifier.height(14.dp))
      Text(
        "Wearable AI that works while you talk.",
        style = K1Type.display,
      )
      Spacer(Modifier.height(16.dp))
      Text(
        "Klik listens to your conversations, understands what matters, and quietly handles your work—so you can focus on what only you can do.",
        style = K1Type.body.copy(color = KlikInkTertiary),
      )
    }
    Column(
      Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 48.dp)
        .navigationBarsPadding(),
    ) {
      K1ButtonPrimary(
        label = "Get started",
        onClick = onNext,
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(20.dp))
      PageDots(total = totalSteps, current = 0, onTap = {})
    }
  }
}

// ──────────────────────────────────────────────────────── Pair device ────
@Composable
private fun PairStep(onSkip: () -> Unit, onBackDot: () -> Unit, totalSteps: Int) {
  Column(Modifier.fillMaxSize().statusBarsPadding()) {
    Column(
      Modifier.weight(1f).padding(horizontal = 32.dp).padding(top = 40.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      DeviceVisual()
      Spacer(Modifier.height(36.dp))
      Text(
        "Pair your Klik One",
        style = K1Type.h2,
        textAlign = TextAlign.Center,
      )
      Spacer(Modifier.height(10.dp))
      Text(
        "Tap the center button on your device to start pairing. We'll find it automatically.",
        style = K1Type.bodySm.copy(color = KlikInkTertiary),
        textAlign = TextAlign.Center,
      )
      Spacer(Modifier.height(28.dp))
      Row(
        Modifier
          .clip(RoundedCornerShape(10.dp))
          .background(KlikPaperSoft)
          .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        K1WaveformLive(color = KlikInkPrimary, barWidth = 2.dp, gap = 2.dp)
        Text(
          "Searching for nearby devices…",
          style = K1Type.caption.copy(color = KlikInkSecondary),
        )
      }
    }
    Column(
      Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 48.dp)
        .navigationBarsPadding(),
    ) {
      K1ButtonPrimary(
        label = "Skip for now",
        onClick = onSkip,
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(20.dp))
      PageDots(total = totalSteps, current = 1, onTap = { if (it == 0) onBackDot() })
    }
  }
}

// Concentric-circle mock of the Klik One earable.
@Composable
private fun DeviceVisual() {
  Box(
    Modifier.size(220.dp).clip(CircleShape)
      .border(0.5.dp, KlikLineTick, CircleShape),
    contentAlignment = Alignment.Center,
  ) {
    Box(
      Modifier.size(180.dp).clip(CircleShape).background(KlikPaperChip),
      contentAlignment = Alignment.Center,
    ) {
      Box(
        Modifier.size(100.dp).clip(CircleShape).background(
          Brush.linearGradient(
            colors = listOf(KlikLineMute, KlikInkMuted),
          ),
        ),
        contentAlignment = Alignment.Center,
      ) {
        Box(
          Modifier.size(70.dp).clip(CircleShape).background(Color(0xFF2A2B2F)),
          contentAlignment = Alignment.Center,
        ) {
          K1RecDot(color = Color(0xFFF5D547), size = 8.dp)
        }
      }
    }
  }
}

// ─────────────────────────────────────────────────────────── Pick role ────
@Composable
private fun PickRoleStep(
  pickedRoleId: String?,
  onPick: (String) -> Unit,
  onContinue: () -> Unit,
  onBackDot: (Int) -> Unit,
  totalSteps: Int,
) {
  Column(Modifier.fillMaxSize().statusBarsPadding()) {
    Column(
      Modifier.weight(1f).verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp).padding(top = 20.dp),
    ) {
      Text("What do you do?", style = K1Type.h2)
      Spacer(Modifier.height(8.dp))
      Text(
        "Klik will tune how it listens and what it surfaces based on your role.",
        style = K1Type.bodySm.copy(color = KlikInkTertiary),
      )
      Spacer(Modifier.height(24.dp))
      KLIK_ROLES.forEach { role ->
        val selected = role.id == pickedRoleId
        RoleRow(role = role, selected = selected, onClick = { onPick(role.id) })
        Spacer(Modifier.height(8.dp))
      }
      Spacer(Modifier.height(24.dp))
    }
    Column(
      Modifier.fillMaxWidth().background(KlikPaperCard),
    ) {
      Box(Modifier.fillMaxWidth().height(0.5.dp).background(KlikPaperChip))
      Column(
        Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp)
          .padding(bottom = 36.dp).navigationBarsPadding(),
      ) {
        K1ButtonPrimary(
          label = "Continue",
          onClick = onContinue,
          modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(14.dp))
        PageDots(
          total = totalSteps,
          current = totalSteps - 1,
          onTap = { if (it < totalSteps - 1) onBackDot(it) },
        )
      }
    }
  }
}

// ──────────────────────────────────────────────── Name + photo step ────
@Composable
private fun NameAndPhotoStep(
  name: String,
  onNameChange: (String) -> Unit,
  avatar: PickedImage?,
  onPickAvatar: (PickedImage?) -> Unit,
  onContinue: () -> Unit,
  onBackDot: (Int) -> Unit,
  totalSteps: Int,
) {
  val scope = rememberCoroutineScope()
  var picking by remember { mutableStateOf(false) }
  val canContinue = name.trim().isNotEmpty()

  Column(Modifier.fillMaxSize().statusBarsPadding()) {
    Column(
      Modifier.weight(1f).verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp).padding(top = 28.dp),
    ) {
      Text("Tell us who you are", style = K1Type.h2)
      Spacer(Modifier.height(8.dp))
      Text(
        "Your name and photo show up on shared sessions and follow-ups. You can change them later.",
        style = K1Type.bodySm.copy(color = KlikInkTertiary),
      )
      Spacer(Modifier.height(28.dp))

      // Avatar picker
      Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(
          Modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(KlikPaperSoft)
            .border(0.5.dp, KlikLineHairline, CircleShape)
            .k1Clickable(enabled = !picking) {
              picking = true
              scope.launch {
                try {
                  val picked = ImagePicker.pickAvatar()
                  if (picked != null) onPickAvatar(picked)
                } finally {
                  picking = false
                }
              }
            },
          contentAlignment = Alignment.Center,
        ) {
          if (avatar != null) {
            AsyncImage(
              model = avatar.bytes,
              contentDescription = "Selected avatar",
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize().clip(CircleShape),
            )
          } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("+", style = K1Type.display.copy(color = KlikInkSecondary))
              Spacer(Modifier.height(2.dp))
              Text(
                "Add photo",
                style = K1Type.metaSm.copy(color = KlikInkTertiary),
              )
            }
          }
        }
      }

      if (avatar != null) {
        Spacer(Modifier.height(12.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
          Text(
            "Tap photo to change",
            style = K1Type.metaSm.copy(color = KlikInkTertiary),
          )
        }
      }

      Spacer(Modifier.height(28.dp))

      // Name field
      Column(
        Modifier
          .fillMaxWidth()
          .clip(K1R.card)
          .background(KlikPaperCard)
          .border(0.5.dp, KlikLineHairline, K1R.card)
          .padding(horizontal = 14.dp, vertical = 12.dp),
      ) {
        Text("Your name", style = K1Type.metaSm.copy(color = KlikInkTertiary))
        Spacer(Modifier.height(2.dp))
        BasicTextField(
          value = name,
          onValueChange = onNameChange,
          singleLine = true,
          textStyle = K1Type.bodyMd,
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            capitalization = KeyboardCapitalization.Words,
          ),
          modifier = Modifier.fillMaxWidth(),
        )
      }

      Spacer(Modifier.height(28.dp))
    }

    Column(
      Modifier.fillMaxWidth().background(KlikPaperCard),
    ) {
      Box(Modifier.fillMaxWidth().height(0.5.dp).background(KlikPaperChip))
      Column(
        Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp)
          .padding(bottom = 36.dp).navigationBarsPadding(),
      ) {
        K1ButtonPrimary(
          label = "Continue",
          onClick = onContinue,
          enabled = canContinue,
          modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(14.dp))
        PageDots(total = totalSteps, current = 2, onTap = { if (it < 2) onBackDot(it) })
      }
    }
  }
}

@Composable
private fun RoleRow(role: KlikOneRole, selected: Boolean, onClick: () -> Unit) {
  val borderColor = if (selected) KlikInkPrimary else KlikLineHairline
  val bgColor = if (selected) KlikPaperSoft else KlikPaperCard
  Row(
    Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(bgColor)
      .border(0.5.dp, borderColor, RoundedCornerShape(10.dp))
      .k1Clickable(onClick = onClick)
      .padding(horizontal = 16.dp, vertical = 14.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Box(
      Modifier.size(16.dp).clip(CircleShape).border(
        1.5.dp,
        if (selected) KlikInkPrimary else KlikLineTick,
        CircleShape,
      ),
      contentAlignment = Alignment.Center,
    ) {
      if (selected) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(KlikInkPrimary))
      }
    }
    Column(Modifier.weight(1f)) {
      Text(role.title, style = K1Type.bodyMd)
      Spacer(Modifier.height(2.dp))
      Text(role.subtitle, style = K1Type.meta)
    }
  }
}

// ───────────────────────────────────────────────────────── Page dots ────
@Composable
private fun PageDots(total: Int, current: Int, onTap: (Int) -> Unit) {
  Row(
    Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.Center,
  ) {
    repeat(total) { i ->
      val on = i == current
      Box(
        Modifier.padding(horizontal = 3.dp).size(5.dp).clip(CircleShape)
          .background(if (on) KlikInkPrimary else KlikLineHairline)
          .k1Clickable(enabled = !on) { onTap(i) },
      )
    }
  }
}
