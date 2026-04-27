// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.fletchmckee.liquid.samples.app.data.network.CurrentUser
import io.github.fletchmckee.liquid.samples.app.data.source.remote.PrivacyRequestDto
import io.github.fletchmckee.liquid.samples.app.data.source.remote.PrivacySettingsDto
import io.github.fletchmckee.liquid.samples.app.data.source.remote.RemoteDataFetcher
import io.github.fletchmckee.liquid.samples.app.data.source.remote.VoiceprintDto
import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import io.github.fletchmckee.liquid.samples.app.theme.KlikAlert
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkMuted
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkPrimary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkSecondary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkTertiary
import io.github.fletchmckee.liquid.samples.app.theme.KlikLineHairline
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperApp
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperCard
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperSoft
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1ButtonPrimary
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1Eyebrow
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1Sp
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1Type
import io.github.fletchmckee.liquid.samples.app.ui.klikone.k1Clickable
import kotlinx.coroutines.launch

@Composable
fun PrivacySettingsScreen(
  onBack: () -> Unit,
  onNavigateToRecordingConsent: () -> Unit,
  onNavigateToBiometricConsent: () -> Unit,
) {
  val scope = rememberCoroutineScope()

  // Loading state
  var isLoading by remember { mutableStateOf(true) }

  // Privacy settings (loaded from backend)
  var retentionDays by remember { mutableStateOf(90) }
  var aiTrainingEnabled by remember { mutableStateOf(false) }
  var memoryExtractionEnabled by remember { mutableStateOf(true) }
  var doNotSell by remember { mutableStateOf(false) }
  var limitSensitive by remember { mutableStateOf(false) }
  var voiceprintEnabled by remember { mutableStateOf(false) }

  // AI Training data types
  var aiTrainingDataTypes by remember { mutableStateOf<List<String>>(emptyList()) }

  // Voiceprint management
  var voiceprints by remember { mutableStateOf<List<VoiceprintDto>>(emptyList()) }
  var isLoadingVoiceprints by remember { mutableStateOf(false) }

  // Data Export
  var selectedExportFormat by remember { mutableStateOf("JSON") }
  var exportRequested by remember { mutableStateOf(false) }

  // CCPA Privacy Requests
  var privacyRequests by remember { mutableStateOf<List<PrivacyRequestDto>>(emptyList()) }

  // Dialogs
  var showClearMemoriesDialog by remember { mutableStateOf(false) }
  var showDeleteAccountDialog by remember { mutableStateOf(false) }
  var showDeleteAllVoiceprintsDialog by remember { mutableStateOf(false) }

  val retentionOptions = listOf(
    "30 days" to 30,
    "90 days" to 90,
    "1 year" to 365,
    "Unlimited" to -1,
  )

  fun retentionLabel(days: Int): String = when (days) {
    30 -> "30 days"
    90 -> "90 days"
    365 -> "1 year"
    -1 -> "Unlimited"
    else -> "$days days"
  }

  // Helper to save current settings to backend
  fun saveSettings() {
    scope.launch {
      try {
        RemoteDataFetcher.updatePrivacySettings(
          PrivacySettingsDto(
            retentionDays = retentionDays,
            autoDeleteEnabled = retentionDays != -1,
            doNotSell = doNotSell,
            limitSensitivePi = limitSensitive,
            memoryExtractionEnabled = memoryExtractionEnabled,
            aiTrainingOptedIn = aiTrainingEnabled,
            aiTrainingDataTypes = aiTrainingDataTypes,
            voiceprintEnabled = voiceprintEnabled,
          ),
        )
        KlikLogger.i("PrivacySettings", "Settings saved successfully")
      } catch (e: Exception) {
        KlikLogger.e("PrivacySettings", "Failed to save settings: ${e.message}", e)
      }
    }
  }

  // Load settings from backend on screen open
  LaunchedEffect(Unit) {
    try {
      val settings = RemoteDataFetcher.getPrivacySettings()
      retentionDays = settings.retentionDays
      aiTrainingEnabled = settings.aiTrainingOptedIn
      aiTrainingDataTypes = settings.aiTrainingDataTypes
      memoryExtractionEnabled = settings.memoryExtractionEnabled
      doNotSell = settings.doNotSell
      limitSensitive = settings.limitSensitivePi
      voiceprintEnabled = settings.voiceprintEnabled
      KlikLogger.i("PrivacySettings", "Loaded privacy settings from backend")
    } catch (e: Exception) {
      KlikLogger.e("PrivacySettings", "Failed to load settings: ${e.message}", e)
    }
    isLoading = false

    // Load voiceprints
    val userId = CurrentUser.userId
    if (userId != null) {
      isLoadingVoiceprints = true
      try {
        voiceprints = RemoteDataFetcher.getVoiceprints(userId)
        KlikLogger.i("PrivacySettings", "Loaded ${voiceprints.size} voiceprints")
      } catch (e: Exception) {
        KlikLogger.e("PrivacySettings", "Failed to load voiceprints: ${e.message}", e)
      }
      isLoadingVoiceprints = false
    }

    // Load CCPA privacy requests
    try {
      privacyRequests = RemoteDataFetcher.getPrivacyRequests()
      KlikLogger.i("PrivacySettings", "Loaded ${privacyRequests.size} privacy requests")
    } catch (e: Exception) {
      KlikLogger.e("PrivacySettings", "Failed to load privacy requests: ${e.message}", e)
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(KlikPaperApp)
      .statusBarsPadding()
      .navigationBarsPadding(),
  ) {
    // Top rail — K1 editorial text-only chrome
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        "Back",
        style = K1Type.metaSm.copy(color = KlikInkSecondary),
        modifier = Modifier.k1Clickable(onClick = onBack).padding(end = K1Sp.m),
      )
      Box(Modifier.weight(1f))
    }

    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
      K1Eyebrow("Klik")
      Spacer(Modifier.height(K1Sp.m))
      Text("Privacy & data.", style = K1Type.display)
      Spacer(Modifier.height(K1Sp.m))
      Text(
        "Decide what Klik keeps, what it learns from, and what leaves with you.",
        style = K1Type.bodySm.copy(color = KlikInkSecondary),
      )
    }

    Spacer(Modifier.height(K1Sp.lg))

    if (isLoading) {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          "Loading…",
          style = K1Type.bodySm.copy(color = KlikInkTertiary),
        )
      }
    } else {
      // Scrollable content
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        Spacer(Modifier.height(8.dp))

        // Section 1: Data Retention
        PrivacyCard {
          PrivacySectionHeader("Data Retention")
          Spacer(Modifier.height(12.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            retentionOptions.forEach { (label, days) ->
              SelectableChip(
                text = label,
                selected = retentionDays == days,
                onClick = {
                  retentionDays = days
                  saveSettings()
                },
                modifier = Modifier.weight(1f),
              )
            }
          }
          Spacer(Modifier.height(8.dp))
          Text(
            "Controls how long your recordings and transcripts are kept",
            style = K1Type.bodySm.copy(color = KlikInkTertiary),
          )
        }

        // Section 2: AI Training
        PrivacyCard {
          PrivacySectionHeader("AI Training")
          Spacer(Modifier.height(12.dp))
          ToggleRow(
            label = "Allow my data to improve KLIK's AI",
            checked = aiTrainingEnabled,
            onCheckedChange = {
              aiTrainingEnabled = it
              if (!it) aiTrainingDataTypes = emptyList()
              saveSettings()
            },
          )
          Spacer(Modifier.height(8.dp))
          Text(
            "When enabled, anonymized data may be used to improve AI accuracy. Default: OFF.",
            style = K1Type.bodySm.copy(color = KlikInkTertiary),
          )
          if (aiTrainingEnabled) {
            Spacer(Modifier.height(12.dp))
            Text(
              "Data types used for training:",
              style = K1Type.bodySm.copy(color = KlikInkSecondary, fontWeight = FontWeight.Medium),
            )
            Spacer(Modifier.height(4.dp))
            CheckboxRow(
              label = "Feedback corrections",
              checked = "feedback" in aiTrainingDataTypes,
              onCheckedChange = { checked ->
                aiTrainingDataTypes = if (checked) {
                  aiTrainingDataTypes + "feedback"
                } else {
                  aiTrainingDataTypes - "feedback"
                }
                saveSettings()
              },
            )
            CheckboxRow(
              label = "Anonymized transcripts",
              checked = "transcripts" in aiTrainingDataTypes,
              onCheckedChange = { checked ->
                aiTrainingDataTypes = if (checked) {
                  aiTrainingDataTypes + "transcripts"
                } else {
                  aiTrainingDataTypes - "transcripts"
                }
                saveSettings()
              },
            )
          }
        }

        // Section 3: Memory Management
        PrivacyCard {
          PrivacySectionHeader("Memories")
          Spacer(Modifier.height(12.dp))
          ToggleRow(
            label = "Automatically extract information from conversations",
            checked = memoryExtractionEnabled,
            onCheckedChange = {
              memoryExtractionEnabled = it
              saveSettings()
            },
          )
          Spacer(Modifier.height(4.dp))
          K1DestructiveRow(
            label = "Clear all memories",
            onClick = { showClearMemoriesDialog = true },
          )
        }

        // Section 4: Recording & Consent
        PrivacyCard {
          PrivacySectionHeader("Recording & Consent")
          Spacer(Modifier.height(12.dp))
          NavigationRow(
            label = "Recording Consent",
            onClick = onNavigateToRecordingConsent,
          )
          Spacer(Modifier.height(4.dp))
          NavigationRow(
            label = "Speaker Identification (Biometric)",
            onClick = onNavigateToBiometricConsent,
          )
        }

        // Section 5: Voiceprint Management (#46)
        PrivacyCard {
          PrivacySectionHeader("Voiceprints")
          Spacer(Modifier.height(12.dp))

          ToggleRow(
            label = "Speaker Identification",
            checked = voiceprintEnabled,
            onCheckedChange = {
              voiceprintEnabled = it
              saveSettings()
              if (!it) {
                // Disabling revokes biometric consent and triggers voiceprint deletion
                scope.launch {
                  try {
                    RemoteDataFetcher.revokeBiometricConsent()
                    KlikLogger.i("PrivacySettings", "Biometric consent revoked")
                  } catch (e: Exception) {
                    KlikLogger.e("PrivacySettings", "Failed to revoke biometric consent: ${e.message}", e)
                  }
                }
                showDeleteAllVoiceprintsDialog = true
              }
            },
          )

          Spacer(Modifier.height(4.dp))

          Text(
            if (voiceprintEnabled) {
              "Active — ${voiceprints.size} voiceprint(s) stored"
            } else {
              "Disabled — speakers labeled generically"
            },
            style = K1Type.bodySm.copy(
              color = if (voiceprintEnabled) KlikInkPrimary else KlikInkTertiary,
            ),
          )

          if (voiceprintEnabled && voiceprints.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))

            voiceprints.forEach { vp ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    vp.name,
                    style = K1Type.bodyMd.copy(color = KlikInkPrimary),
                  )
                  Text(
                    "${vp.sampleCount} samples",
                    style = K1Type.meta.copy(color = KlikInkTertiary),
                  )
                }
                Text(
                  "Remove",
                  style = K1Type.meta.copy(color = KlikAlert),
                  modifier = Modifier
                    .k1Clickable {
                      scope.launch {
                        try {
                          RemoteDataFetcher.deleteVoiceprint(vp.id)
                          voiceprints = voiceprints.filter { it.id != vp.id }
                          KlikLogger.i("PrivacySettings", "Deleted voiceprint: ${vp.id}")
                        } catch (e: Exception) {
                          KlikLogger.e("PrivacySettings", "Failed to delete voiceprint: ${e.message}", e)
                        }
                      }
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                )
              }
            }

            Spacer(Modifier.height(4.dp))

            K1DestructiveRow(
              label = "Delete all voiceprints",
              onClick = { showDeleteAllVoiceprintsDialog = true },
            )
          }

          if (isLoadingVoiceprints) {
            Spacer(Modifier.height(8.dp))
            CircularProgressIndicator(
              modifier = Modifier.size(20.dp),
              color = KlikInkPrimary,
              strokeWidth = 2.dp,
            )
          }
        }

        // Section 6: Data Export
        PrivacyCard {
          PrivacySectionHeader("Data Export")
          Spacer(Modifier.height(12.dp))
          K1OutlineRow(
            label = if (exportRequested) "Export requested" else "Download my data",
            enabled = !exportRequested,
            onClick = {
              exportRequested = true
              scope.launch {
                try {
                  RemoteDataFetcher.requestDataExport(selectedExportFormat.lowercase())
                  KlikLogger.i("PrivacySettings", "Data export requested in $selectedExportFormat format")
                } catch (e: Exception) {
                  KlikLogger.e("PrivacySettings", "Failed to request data export: ${e.message}", e)
                  exportRequested = false
                }
              }
            },
          )
          Spacer(Modifier.height(12.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            SelectableChip(
              text = "JSON",
              selected = selectedExportFormat == "JSON",
              onClick = { selectedExportFormat = "JSON" },
              modifier = Modifier.weight(1f),
            )
            SelectableChip(
              text = "CSV",
              selected = selectedExportFormat == "CSV",
              onClick = { selectedExportFormat = "CSV" },
              modifier = Modifier.weight(1f),
            )
          }
          Spacer(Modifier.height(8.dp))
          Text(
            "You will receive an email when your data export is ready",
            style = K1Type.bodySm.copy(color = KlikInkTertiary),
          )
        }

        // Section 7: Account
        PrivacyCard {
          PrivacySectionHeader("Account")
          Spacer(Modifier.height(12.dp))
          K1DestructiveRow(
            label = "Delete my account",
            onClick = { showDeleteAccountDialog = true },
          )
          Spacer(Modifier.height(4.dp))
          Text(
            "This will permanently delete all your data. There is a 30-day grace period.",
            style = K1Type.bodySm.copy(color = KlikInkTertiary),
          )
        }

        // Section 8: California Residents (CCPA)
        PrivacyCard {
          PrivacySectionHeader("California Residents")
          Spacer(Modifier.height(12.dp))
          ToggleRow(
            label = "Do Not Sell or Share My Personal Information",
            checked = doNotSell,
            onCheckedChange = {
              doNotSell = it
              saveSettings()
            },
          )
          Spacer(Modifier.height(4.dp))
          ToggleRow(
            label = "Limit Use of My Sensitive Personal Information",
            checked = limitSensitive,
            onCheckedChange = {
              limitSensitive = it
              saveSettings()
            },
          )
          Spacer(Modifier.height(8.dp))
          Text(
            "These controls apply regardless of whether KLIK sells data (we don't)",
            style = K1Type.bodySm.copy(color = KlikInkTertiary),
          )

          // CCPA Privacy Rights Request Forms
          Spacer(Modifier.height(16.dp))
          Text(
            "Your rights",
            style = K1Type.cardTitle.copy(color = KlikInkPrimary),
          )
          Spacer(Modifier.height(8.dp))

          val requestTypes = listOf(
            "access" to "Request access to my data",
            "correct" to "Request data correction",
            "delete" to "Request data deletion",
          )

          requestTypes.forEach { (type, label) ->
            val hasPending = privacyRequests.any {
              it.requestType == type && it.status in listOf("pending", "verified", "processing")
            }
            K1OutlineRow(
              label = if (hasPending) "$label (pending)" else label,
              enabled = !hasPending,
              onClick = {
                scope.launch {
                  try {
                    RemoteDataFetcher.submitPrivacyRequest(type)
                    privacyRequests = RemoteDataFetcher.getPrivacyRequests()
                    KlikLogger.i("PrivacySettings", "Submitted $type privacy request")
                  } catch (e: Exception) {
                    KlikLogger.e("PrivacySettings", "Failed to submit $type request: ${e.message}", e)
                  }
                }
              },
              modifier = Modifier.padding(vertical = 2.dp),
            )
          }

          // Privacy Request Status Tracker
          if (privacyRequests.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text(
              "Request history",
              style = K1Type.cardTitle.copy(color = KlikInkPrimary),
            )
            Spacer(Modifier.height(8.dp))

            privacyRequests.forEach { request ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 3.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .background(KlikPaperSoft)
                  .border(0.75.dp, KlikLineHairline, RoundedCornerShape(12.dp))
                  .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    request.requestType.replaceFirstChar { it.uppercase() },
                    style = K1Type.bodyMd.copy(color = KlikInkPrimary),
                  )
                  Text(
                    "Submitted: ${request.requestedAt.take(10)}",
                    style = K1Type.meta.copy(color = KlikInkTertiary),
                  )
                  Text(
                    "Deadline: ${request.responseDeadline.take(10)}",
                    style = K1Type.meta.copy(color = KlikInkTertiary),
                  )
                }
                StatusBadge(request.status)
              }
            }
          }
        }

        Spacer(Modifier.height(24.dp))
      }
    }
  }

  if (showClearMemoriesDialog) {
    K1ConfirmDialog(
      title = "Clear all memories.",
      body = "This permanently deletes all extracted memories. Can't be undone.",
      confirmLabel = "Clear all",
      onConfirm = {
        showClearMemoriesDialog = false
        scope.launch {
          try {
            RemoteDataFetcher.submitPrivacyRequest("delete")
          } catch (e: Exception) {
            KlikLogger.e("PrivacySettings", "Failed to clear memories: ${e.message}", e)
          }
        }
      },
      onCancel = { showClearMemoriesDialog = false },
    )
  }
  if (showDeleteAccountDialog) {
    K1ConfirmDialog(
      title = "Delete account.",
      body = "Your account and all associated data will be deleted after a 30-day grace period. Can't be undone.",
      confirmLabel = "Delete account",
      onConfirm = {
        showDeleteAccountDialog = false
        scope.launch {
          try {
            RemoteDataFetcher.submitPrivacyRequest("delete")
          } catch (e: Exception) {
            KlikLogger.e("PrivacySettings", "Failed to request account deletion: ${e.message}", e)
          }
        }
      },
      onCancel = { showDeleteAccountDialog = false },
    )
  }
  if (showDeleteAllVoiceprintsDialog) {
    K1ConfirmDialog(
      title = "Delete all voiceprints.",
      body = "All stored voiceprints will be removed. Speaker identification uses generic labels until Klik collects new ones.",
      confirmLabel = "Delete all",
      onConfirm = {
        showDeleteAllVoiceprintsDialog = false
        scope.launch {
          try {
            voiceprints.forEach { vp -> RemoteDataFetcher.deleteVoiceprint(vp.id) }
            voiceprints = emptyList()
          } catch (e: Exception) {
            KlikLogger.e("PrivacySettings", "Failed to delete voiceprints: ${e.message}", e)
          }
        }
      },
      onCancel = { showDeleteAllVoiceprintsDialog = false },
    )
  }
}

@Composable
private fun PrivacyCard(content: @Composable () -> Unit) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .background(KlikPaperCard)
      .border(0.75.dp, KlikLineHairline, RoundedCornerShape(14.dp))
      .padding(20.dp),
  ) {
    content()
  }
}

@Composable
private fun PrivacySectionHeader(title: String) {
  K1Eyebrow(title)
  Spacer(Modifier.height(4.dp))
}

@Composable
private fun ToggleRow(
  label: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .k1Clickable { onCheckedChange(!checked) }
      .padding(vertical = 10.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      label,
      style = K1Type.bodyMd.copy(color = KlikInkPrimary),
      modifier = Modifier.weight(1f),
    )
    Spacer(Modifier.width(12.dp))
    K1InlineSwitch(checked = checked)
  }
}

@Composable
private fun K1InlineSwitch(checked: Boolean) {
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
private fun NavigationRow(
  label: String,
  onClick: () -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .k1Clickable(onClick = onClick)
      .padding(vertical = 14.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(label, style = K1Type.bodyMd.copy(color = KlikInkPrimary))
    Text("›", style = K1Type.bodyMd.copy(color = KlikInkTertiary))
  }
}

@Composable
private fun CheckboxRow(
  label: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .k1Clickable { onCheckedChange(!checked) }
      .padding(vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    K1TinyCheckPrivacy(checked = checked)
    Spacer(Modifier.width(10.dp))
    Text(label, style = K1Type.bodySm.copy(color = KlikInkPrimary))
  }
}

@Composable
private fun K1TinyCheckPrivacy(checked: Boolean) {
  val shape = RoundedCornerShape(4.dp)
  Box(
    Modifier
      .size(16.dp)
      .clip(shape)
      .background(if (checked) KlikInkPrimary else KlikPaperCard)
      .border(0.75.dp, if (checked) KlikInkPrimary else KlikLineHairline, shape),
    contentAlignment = Alignment.Center,
  ) {
    if (checked) Text("✓", style = K1Type.metaSm.copy(color = KlikPaperCard))
  }
}

@Composable
private fun StatusBadge(status: String) {
  val label = when (status) {
    "pending" -> "Pending"
    "verified" -> "Verified"
    "processing" -> "Processing"
    "completed" -> "Completed"
    "denied" -> "Denied"
    else -> status.replaceFirstChar { it.uppercase() }
  }
  val onInk = status == "completed" || status == "verified"
  Text(
    label,
    style = K1Type.metaSm.copy(
      color = if (onInk) KlikPaperCard else KlikInkPrimary,
      fontWeight = FontWeight.Medium,
    ),
    modifier = Modifier
      .clip(CircleShape)
      .background(if (onInk) KlikInkPrimary else KlikPaperSoft)
      .padding(horizontal = 10.dp, vertical = 4.dp),
  )
}

@Composable
private fun SelectableChip(
  text: String,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val bg = if (selected) KlikInkPrimary else KlikPaperSoft
  val fg = if (selected) KlikPaperCard else KlikInkSecondary
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(10.dp))
      .background(bg)
      .k1Clickable(onClick = onClick)
      .padding(horizontal = 12.dp, vertical = 10.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text,
      style = K1Type.metaSm.copy(
        color = fg,
        fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
      ),
    )
  }
}

@Composable
private fun K1ConfirmDialog(
  title: String,
  body: String,
  confirmLabel: String,
  onConfirm: () -> Unit,
  onCancel: () -> Unit,
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = 0.32f))
      .pointerInput(Unit) { detectTapGestures(onTap = { onCancel() }) },
    contentAlignment = Alignment.Center,
  ) {
    Column(
      modifier = Modifier
        .widthIn(max = 360.dp)
        .padding(horizontal = 24.dp)
        .clip(RoundedCornerShape(20.dp))
        .background(KlikPaperCard)
        .border(0.75.dp, KlikLineHairline, RoundedCornerShape(20.dp))
        .pointerInput(Unit) { detectTapGestures { /* swallow */ } }
        .padding(horizontal = 24.dp, vertical = 24.dp),
    ) {
      K1Eyebrow("Klik")
      Spacer(Modifier.height(K1Sp.m))
      Text(title, style = K1Type.h2)
      Spacer(Modifier.height(K1Sp.s))
      Text(body, style = K1Type.bodySm.copy(color = KlikInkSecondary))
      Spacer(Modifier.height(K1Sp.xl))
      K1ButtonPrimary(
        label = confirmLabel,
        onClick = onConfirm,
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(K1Sp.m))
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(KlikPaperSoft)
          .k1Clickable(onClick = onCancel)
          .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
      ) {
        Text("Cancel", style = K1Type.bodyMd.copy(color = KlikInkPrimary))
      }
    }
  }
}

@Composable
private fun K1DestructiveRow(label: String, onClick: () -> Unit) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(KlikPaperSoft)
      .border(0.75.dp, KlikLineHairline, RoundedCornerShape(12.dp))
      .k1Clickable(onClick = onClick)
      .padding(vertical = 14.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(label, style = K1Type.bodyMd.copy(color = KlikAlert))
  }
}

@Composable
private fun K1OutlineRow(
  label: String,
  onClick: () -> Unit,
  enabled: Boolean = true,
  modifier: Modifier = Modifier,
) {
  val fg = if (enabled) KlikInkPrimary else KlikInkMuted
  val border = if (enabled) KlikLineHairline else KlikLineHairline.copy(alpha = 0.4f)
  Box(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(KlikPaperCard)
      .border(0.75.dp, border, RoundedCornerShape(12.dp))
      .then(if (enabled) Modifier.k1Clickable(onClick = onClick) else Modifier)
      .padding(vertical = 14.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(label, style = K1Type.bodyMd.copy(color = fg))
  }
}
