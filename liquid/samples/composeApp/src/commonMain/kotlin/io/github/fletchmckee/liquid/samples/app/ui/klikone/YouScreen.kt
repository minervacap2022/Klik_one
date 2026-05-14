// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.ui.klikone

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.fletchmckee.liquid.samples.app.core.rememberViewModel
import io.github.fletchmckee.liquid.samples.app.domain.entity.Subscription
import io.github.fletchmckee.liquid.samples.app.presentation.profile.ProfileViewModel
import io.github.fletchmckee.liquid.samples.app.theme.KlikAlert
import io.github.fletchmckee.liquid.samples.app.theme.KlikAvatarBg
import io.github.fletchmckee.liquid.samples.app.theme.KlikDecisionAccent
import io.github.fletchmckee.liquid.samples.app.theme.KlikDecisionSubtext
import io.github.fletchmckee.liquid.samples.app.theme.KlikDecisionText
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkMuted
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkPrimary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkSecondary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkTertiary
import io.github.fletchmckee.liquid.samples.app.theme.KlikLineHairline
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperApp
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperCard
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperChip
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperSoft
import io.github.fletchmckee.liquid.samples.app.theme.KlikRunning
import io.github.fletchmckee.liquid.samples.app.ui.components.AvatarImage
import io.github.fletchmckee.liquid.samples.app.ui.components.K1ProviderIcon
import io.github.fletchmckee.liquid.samples.app.ui.klikone.LocalKlikStrings

/** Klik One — You. Drop-in replacement for `ProfileScreen`. */
@Composable
fun YouScreen(
  viewModel: ProfileViewModel = rememberViewModel { ProfileViewModel() },
  onNavigateToArchived: () -> Unit = {},
  subscription: Subscription? = null,
  onNavigateToPricing: () -> Unit = {},
  onNavigateToPrivacy: () -> Unit = {},
  onNavigateToAccountSecurity: () -> Unit = {},
  onNavigateToNotificationSettings: () -> Unit = {},
  onNavigateToXpLogs: () -> Unit = {},
  onNavigateToPreferences: () -> Unit = {},
  onNavigateToAchievements: () -> Unit = {},
) {
  val s = LocalKlikStrings.current
  val ui by viewModel.state.collectAsState()
  val user = ui.user
  val devices = ui.connectedDevices

  Column(
    Modifier
      .fillMaxSize()
      .background(KlikPaperApp)
      .verticalScroll(rememberScrollState())
      .padding(top = 52.dp, bottom = 120.dp),
  ) {
    K1Header(title = "You")
    Spacer(Modifier.height(K1Sp.md))

    // Identity card
    Column(Modifier.padding(horizontal = 20.dp)) {
      K1Card {
        Row(verticalAlignment = Alignment.CenterVertically) {
          val initials = user?.name?.trim()?.split(" ")
            ?.filter { it.isNotEmpty() }
            ?.take(2)
            ?.joinToString("") { it.take(1).uppercase() }
            ?: "—"
          Box(
            Modifier.size(56.dp).k1Clickable { viewModel.pickAndUploadAvatar() },
            contentAlignment = Alignment.Center,
          ) {
            AvatarImage(
              avatarUrl = user?.avatarUrl,
              initials = initials,
              size = 56.dp,
              backgroundColor = KlikAvatarBg.firstOrNull() ?: KlikPaperCard,
              initialsColor = KlikInkPrimary,
            )
          }
          Spacer(Modifier.width(K1Sp.m))
          Column(Modifier.weight(1f)) {
            Text(user?.name ?: "—", style = K1Type.h3)
            Spacer(Modifier.height(2.dp))
            Text(user?.email ?: "—", style = K1Type.caption)
          }
          K1Chip(label = s.edit, onClick = { viewModel.showEditProfile() })
        }
      }
    }

    Spacer(Modifier.height(K1Sp.xxl))

    // Plan
    Column(Modifier.padding(horizontal = 20.dp)) {
      K1SectionHeader(s.plan)
      Spacer(Modifier.height(K1Sp.s))
      val planName = subscription?.displayName ?: (user?.planType?.label ?: "Starter")
      val isPro = planName.contains("pro", ignoreCase = true) ||
        user?.planType?.tierCode?.contains("pro", ignoreCase = true) == true ||
        subscription?.planCode?.contains("pro", ignoreCase = true) == true
      if (isPro) {
        // Pro — warm amber/gold card with sparkle accent
        Box(
          Modifier
            .fillMaxWidth()
            .clip(K1R.soft)
            .background(
              Brush.linearGradient(
                listOf(Color(0xFFFFF8EC), Color(0xFFFAEEDA)),
              ),
            )
            .border(1.dp, Color(0xFFE8C87A), K1R.soft)
            .k1Clickable(onClick = onNavigateToPricing)
            .padding(14.dp),
        ) {
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text("✦", style = K1Type.bodySm.copy(color = KlikDecisionAccent))
              Spacer(Modifier.width(6.dp))
              Text(
                planName,
                style = K1Type.bodyMd.copy(
                  color = KlikDecisionText,
                  fontWeight = FontWeight.SemiBold,
                ),
                modifier = Modifier.weight(1f),
              )
              subscription?.billingCycle?.let {
                Text(
                  it.replaceFirstChar { c -> c.uppercase() },
                  style = K1Type.metaSm.copy(color = KlikDecisionSubtext),
                )
              }
            }
            subscription?.usage?.let { usage ->
              Spacer(Modifier.height(8.dp))
              val frac = if (usage.asrMinutesLimit > 0) {
                usage.asrMinutesUsed.toFloat() / usage.asrMinutesLimit
              } else {
                0f
              }
              Box(
                Modifier.fillMaxWidth().height(3.dp).clip(K1R.pill)
                  .background(Color(0xFFE8C87A).copy(alpha = 0.35f)),
              ) {
                Box(
                  Modifier.fillMaxHeight().fillMaxWidth(frac.coerceIn(0f, 1f))
                    .background(KlikDecisionAccent),
                )
              }
              Spacer(Modifier.height(6.dp))
              Text(
                "ASR ${usage.asrMinutesUsed} / ${usage.asrMinutesLimit} min",
                style = K1Type.metaSm.copy(color = KlikDecisionSubtext),
              )
            }
          }
        }
      } else {
        K1Card(onClick = onNavigateToPricing) {
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                planName,
                style = K1Type.bodyMd,
                modifier = Modifier.weight(1f),
              )
              subscription?.billingCycle?.let {
                Text(it.replaceFirstChar { c -> c.uppercase() }, style = K1Type.metaSm)
              }
            }
            subscription?.usage?.let { usage ->
              Spacer(Modifier.height(6.dp))
              val frac = if (usage.asrMinutesLimit > 0) {
                usage.asrMinutesUsed.toFloat() / usage.asrMinutesLimit
              } else {
                0f
              }
              Box(
                Modifier.fillMaxWidth().height(3.dp).clip(K1R.pill)
                  .background(KlikLineHairline),
              ) {
                Box(
                  Modifier.fillMaxHeight().fillMaxWidth(frac.coerceIn(0f, 1f))
                    .background(KlikInkPrimary),
                )
              }
              Spacer(Modifier.height(6.dp))
              Text(
                "ASR ${usage.asrMinutesUsed} / ${usage.asrMinutesLimit} min",
                style = K1Type.metaSm,
              )
            }
          }
        }
      }
    }

    Spacer(Modifier.height(K1Sp.xxl))

    // Devices
    if (devices.isNotEmpty()) {
      Column(Modifier.padding(horizontal = 20.dp)) {
        K1SectionHeader(s.devices, count = devices.size)
        Spacer(Modifier.height(K1Sp.s))
        devices.forEach { d ->
          Row(
            Modifier.fillMaxWidth().padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Box(
              Modifier.size(8.dp).clip(androidx.compose.foundation.shape.CircleShape)
                .background(if (d.isConnected) KlikRunning else KlikInkMuted),
            )
            Spacer(Modifier.width(K1Sp.m))
            Column(Modifier.weight(1f)) {
              Text(d.name, style = K1Type.bodySm)
              Spacer(Modifier.height(2.dp))
              Text(
                if (d.isConnected) s.connected else s.offline,
                style = K1Type.metaSm,
              )
            }
          }
          Box(Modifier.fillMaxWidth().height(0.5.dp).background(KlikPaperChip))
        }
      }
      Spacer(Modifier.height(K1Sp.xxl))
    }

    // Integrations — three-state OAuth status (alive/expired/invalid/null).
    // `needsReconnect` (validation_status="invalid") is its own bucket: the
    // user previously connected but the token has been silently revoked,
    // so we need a distinct red affordance — rendering as "Connect" would
    // hide that the integration broke.
    //
    // We ALWAYS render the section, even when the catalogue is empty, so
    // the user can't lose the entry point if the backend returns nothing
    // (NO SILENT SWALLOW — show the empty state).
    val integrations = ui.integrations
    run {
      val needsReconnect = integrations.filter { it.needsReconnect }
      val connected = integrations.filter { it.connected }
      val available = integrations.filter { !it.connected && !it.needsReconnect }
      Column(Modifier.padding(horizontal = 20.dp)) {
        K1SectionHeader(s.connectors, count = integrations.size)
        Spacer(Modifier.height(K1Sp.s))
        if (integrations.isEmpty()) {
          Box(
            Modifier
              .fillMaxWidth()
              .clip(K1R.card)
              .background(KlikPaperCard)
              .padding(16.dp),
          ) {
            Text(
              if (ui.isLoadingIntegrations) "Loading connectors…" else "No connectors loaded.",
              style = K1Type.bodySm.copy(color = KlikInkSecondary),
            )
          }
        }
        if (needsReconnect.isNotEmpty()) {
          Column(
            Modifier.fillMaxWidth().clip(K1R.card).background(KlikPaperCard)
              .padding(vertical = 4.dp),
          ) {
            needsReconnect.forEachIndexed { i, info ->
              IntegrationRow(
                providerId = info.providerId,
                displayName = info.displayName,
                state = s.reconnect,
                stateColor = KlikAlert,
                detail = info.invalidReason ?: s.connectionExpired,
                onClick = { viewModel.authorizeIntegration(info.providerId) },
              )
              if (i < needsReconnect.size - 1) Divider()
            }
          }
          Spacer(Modifier.height(K1Sp.m))
        }
        if (connected.isNotEmpty()) {
          Column(
            Modifier.fillMaxWidth().clip(K1R.card).background(KlikPaperCard)
              .padding(vertical = 4.dp),
          ) {
            connected.forEachIndexed { i, info ->
              IntegrationRow(
                providerId = info.providerId,
                displayName = info.displayName,
                state = s.connected,
                stateColor = KlikRunning,
                onClick = { viewModel.disconnectIntegration(info.providerId) },
              )
              if (i < connected.size - 1) Divider()
            }
          }
          Spacer(Modifier.height(K1Sp.m))
        }
        if (available.isNotEmpty()) {
          Column(
            Modifier.fillMaxWidth().clip(K1R.card).background(KlikPaperCard)
              .padding(vertical = 4.dp),
          ) {
            available.forEachIndexed { i, info ->
              IntegrationRow(
                providerId = info.providerId,
                displayName = info.displayName,
                state = s.connect,
                stateColor = KlikInkPrimary,
                onClick = { viewModel.authorizeIntegration(info.providerId) },
              )
              if (i < available.size - 1) Divider()
            }
          }
        }
      }
      Spacer(Modifier.height(K1Sp.xxl))
    }

    // Settings list
    Column(Modifier.padding(horizontal = 20.dp)) {
      K1SectionHeader(s.settings)
      Spacer(Modifier.height(K1Sp.s))
      var showImportSheet by remember { mutableStateOf(false) }
      Column(
        Modifier
          .fillMaxWidth()
          .clip(K1R.card)
          .background(KlikPaperCard)
          .padding(vertical = 4.dp),
      ) {
        SettingsRow(s.achievements, onClick = onNavigateToAchievements)
        Divider()
        SettingsRow(s.preferences, onClick = onNavigateToPreferences)
        Divider()
        SettingsRow(s.archivedSessions, onClick = onNavigateToArchived)
        Divider()
        SettingsRow(s.notifications, onClick = onNavigateToNotificationSettings)
        Divider()
        SettingsRow(s.privacy, onClick = onNavigateToPrivacy)
        Divider()
        SettingsRow(s.accountAndSecurity, onClick = onNavigateToAccountSecurity)
        Divider()
        SettingsRow(s.importFromAgent, onClick = { showImportSheet = true })
        Divider()
        SettingsRow(s.plans, onClick = onNavigateToPricing)
        Divider()
        SettingsRow(s.xpLogs, onClick = onNavigateToXpLogs)
        Divider()
        // About row — last in the settings card. Trailing label shows the
        // marketing version + build. Read at runtime from AppVersion (iOS:
        // Info.plist, Android: PackageInfo) so it always tracks the
        // installed binary; nothing hardcoded in commonMain.
        Row(
          Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text("About", style = K1Type.bodyMd, modifier = Modifier.weight(1f))
          Text(
            "v${io.github.fletchmckee.liquid.samples.app.platform.AppVersion.marketing} (${io.github.fletchmckee.liquid.samples.app.platform.AppVersion.build})",
            style = K1Type.metaSm.copy(color = KlikInkTertiary),
          )
        }
      }
      if (showImportSheet) {
        ImportFromAgentSheet(onDismiss = { showImportSheet = false })
      }
    }

    Spacer(Modifier.height(K1Sp.xxl))

    // Sign out — tap shows confirm dialog; dialog calls real logout.
    Column(Modifier.padding(horizontal = 20.dp)) {
      Row(
        Modifier
          .fillMaxWidth()
          .clip(K1R.card)
          .background(KlikPaperSoft)
          .k1Clickable(onClick = { viewModel.showLogoutConfirmation() })
          .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          s.signOut,
          style = K1Type.bodyMd.copy(
            color = KlikAlert,
            fontWeight = FontWeight.Medium,
          ),
          modifier = Modifier.weight(1f),
        )
      }
    }
  }

  // Logout confirmation modal
  if (ui.showLogoutConfirmation) {
    SignOutConfirmDialog(
      onCancel = { viewModel.dismissLogoutConfirmation() },
      onConfirm = { viewModel.confirmLogout() },
    )
  }

  // Edit profile modal
  if (ui.showEditProfile) {
    EditProfileDialog(
      name = ui.editName,
      email = ui.editEmail,
      isSaving = ui.isSavingProfile,
      onNameChange = viewModel::updateEditName,
      onEmailChange = viewModel::updateEditEmail,
      onCancel = { viewModel.dismissEditProfile() },
      onSave = { viewModel.saveProfile() },
    )
  }

}

@Composable
private fun IntegrationRow(
  providerId: String,
  displayName: String,
  state: String,
  stateColor: Color,
  onClick: () -> Unit,
  detail: String? = null,
) {
  Row(
    Modifier.fillMaxWidth()
      .k1Clickable(onClick = onClick)
      .padding(horizontal = 16.dp, vertical = 14.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    K1ProviderIcon(providerId = providerId, size = 28.dp)
    Spacer(Modifier.width(K1Sp.m))
    Column(Modifier.weight(1f)) {
      Text(displayName, style = K1Type.bodySm)
      if (!detail.isNullOrBlank()) {
        Spacer(Modifier.height(2.dp))
        Text(detail, style = K1Type.metaSm.copy(color = stateColor))
      }
    }
    Text(state, style = K1Type.metaSm.copy(color = stateColor, fontWeight = FontWeight.Medium))
  }
}

@Composable
private fun EditProfileDialog(
  name: String,
  email: String,
  isSaving: Boolean,
  onNameChange: (String) -> Unit,
  onEmailChange: (String) -> Unit,
  onCancel: () -> Unit,
  onSave: () -> Unit,
) {
  val s = LocalKlikStrings.current
  Box(
    Modifier.fillMaxSize()
      .background(Color.Black.copy(alpha = 0.45f))
      .k1Clickable(onClick = onCancel),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      Modifier
        .padding(horizontal = 24.dp)
        .fillMaxWidth()
        .clip(RoundedCornerShape(20.dp))
        .background(KlikPaperCard)
        .k1Clickable(enabled = false) {}
        .padding(24.dp),
    ) {
      Text(s.editProfile, style = K1Type.h3)
      Spacer(Modifier.height(K1Sp.xl))
      Text(s.nameLabel, style = K1Type.metaSm)
      Spacer(Modifier.height(4.dp))
      Box(
        Modifier
          .fillMaxWidth()
          .clip(K1R.card)
          .background(KlikPaperChip)
          .padding(horizontal = 12.dp, vertical = 10.dp),
      ) {
        androidx.compose.foundation.text.BasicTextField(
          value = name,
          onValueChange = onNameChange,
          singleLine = true,
          textStyle = K1Type.bodyMd,
          modifier = Modifier.fillMaxWidth(),
        )
      }
      Spacer(Modifier.height(K1Sp.m))
      Text(s.emailLabel, style = K1Type.metaSm)
      Spacer(Modifier.height(4.dp))
      Box(
        Modifier
          .fillMaxWidth()
          .clip(K1R.card)
          .background(KlikPaperChip)
          .padding(horizontal = 12.dp, vertical = 10.dp),
      ) {
        androidx.compose.foundation.text.BasicTextField(
          value = email,
          onValueChange = onEmailChange,
          singleLine = true,
          textStyle = K1Type.bodyMd,
          modifier = Modifier.fillMaxWidth(),
        )
      }
      Spacer(Modifier.height(K1Sp.xl))
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
          Modifier.weight(1f).clip(K1R.pill).background(KlikPaperChip)
            .k1Clickable(onClick = onCancel).padding(vertical = 14.dp),
          contentAlignment = Alignment.Center,
        ) { Text(s.cancel, style = K1Type.bodyMd) }
        Box(
          Modifier.weight(1f).clip(K1R.pill)
            .background(if (isSaving) KlikInkMuted else KlikInkPrimary)
            .k1Clickable(enabled = !isSaving, onClick = onSave).padding(vertical = 14.dp),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            if (isSaving) s.savingEllipsis else s.save,
            style = K1Type.bodyMd.copy(
              color = KlikPaperCard,
              fontWeight = FontWeight.Medium,
            ),
          )
        }
      }
    }
  }
}

@Composable
private fun SignOutConfirmDialog(onCancel: () -> Unit, onConfirm: () -> Unit) {
  val s = LocalKlikStrings.current
  Box(
    Modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = 0.45f))
      .k1Clickable(onClick = onCancel),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      Modifier
        .padding(horizontal = 32.dp)
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .background(KlikPaperCard)
        .k1Clickable(enabled = false) {} // absorb taps on card
        .padding(24.dp),
    ) {
      Text(s.signOutOfKlik, style = K1Type.h3)
      Spacer(Modifier.height(K1Sp.s))
      Text(
        s.signOutConfirmation,
        style = K1Type.bodySm.copy(color = KlikInkSecondary),
      )
      Spacer(Modifier.height(K1Sp.xl))
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
          Modifier
            .weight(1f)
            .clip(K1R.pill)
            .background(KlikPaperChip)
            .k1Clickable(onClick = onCancel)
            .padding(vertical = 14.dp),
          contentAlignment = Alignment.Center,
        ) {
          Text(s.cancel, style = K1Type.bodyMd)
        }
        Box(
          Modifier
            .weight(1f)
            .clip(K1R.pill)
            .background(KlikAlert)
            .k1Clickable(onClick = onConfirm)
            .padding(vertical = 14.dp),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            s.signOut,
            style = K1Type.bodyMd.copy(
              color = KlikPaperCard,
              fontWeight = FontWeight.Medium,
            ),
          )
        }
      }
    }
  }
}

@Composable
private fun SettingsRow(label: String, onClick: () -> Unit) {
  Row(
    Modifier
      .fillMaxWidth()
      .k1Clickable(onClick = onClick)
      .padding(horizontal = 16.dp, vertical = 14.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(label, style = K1Type.bodySm, modifier = Modifier.weight(1f))
    // chevron
    androidx.compose.foundation.Canvas(Modifier.size(12.dp)) {
      val w = 1.2.dp.toPx()
      drawLine(
        color = KlikInkMuted,
        strokeWidth = w,
        cap = androidx.compose.ui.graphics.StrokeCap.Round,
        start = androidx.compose.ui.geometry.Offset(4.dp.toPx(), 3.dp.toPx()),
        end = androidx.compose.ui.geometry.Offset(8.dp.toPx(), 6.dp.toPx()),
      )
      drawLine(
        color = KlikInkMuted,
        strokeWidth = w,
        cap = androidx.compose.ui.graphics.StrokeCap.Round,
        start = androidx.compose.ui.geometry.Offset(8.dp.toPx(), 6.dp.toPx()),
        end = androidx.compose.ui.geometry.Offset(4.dp.toPx(), 9.dp.toPx()),
      )
    }
  }
}

@Composable
private fun Divider() {
  Box(
    Modifier.fillMaxWidth().height(0.5.dp).background(KlikLineHairline)
      .padding(horizontal = 16.dp),
  )
}

