package io.github.fletchmckee.liquid.samples.app.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.fletchmckee.liquid.samples.app.data.network.CurrentUser
import io.github.fletchmckee.liquid.samples.app.data.source.remote.PrivacyRequestDto
import io.github.fletchmckee.liquid.samples.app.data.source.remote.PrivacySettingsDto
import io.github.fletchmckee.liquid.samples.app.data.source.remote.RemoteDataFetcher
import io.github.fletchmckee.liquid.samples.app.data.source.remote.VoiceprintDto
import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import io.github.fletchmckee.liquid.samples.app.theme.KlikBlack
import io.github.fletchmckee.liquid.samples.app.theme.KlikPrimary
import kotlinx.coroutines.launch

@Composable
fun PrivacySettingsScreen(
    onBack: () -> Unit,
    onNavigateToRecordingConsent: () -> Unit,
    onNavigateToBiometricConsent: () -> Unit
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
        "Unlimited" to -1
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
                        voiceprintEnabled = voiceprintEnabled
                    )
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
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = KlikBlack
                )
            }
            Text(
                "Privacy & Data",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = KlikBlack
            )
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = KlikPrimary)
            }
        } else {
            // Scrollable content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(Modifier.height(8.dp))

                // Section 1: Data Retention
                PrivacyCard {
                    SectionHeader("Data Retention")
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        retentionOptions.forEach { (label, days) ->
                            SelectableChip(
                                text = label,
                                selected = retentionDays == days,
                                onClick = {
                                    retentionDays = days
                                    saveSettings()
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Controls how long your recordings and transcripts are kept",
                        style = MaterialTheme.typography.bodySmall,
                        color = KlikBlack.copy(alpha = 0.5f)
                    )
                }

                // Section 2: AI Training
                PrivacyCard {
                    SectionHeader("AI Training")
                    Spacer(Modifier.height(12.dp))
                    ToggleRow(
                        label = "Allow my data to improve KLIK's AI",
                        checked = aiTrainingEnabled,
                        onCheckedChange = {
                            aiTrainingEnabled = it
                            if (!it) aiTrainingDataTypes = emptyList()
                            saveSettings()
                        }
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "When enabled, anonymized data may be used to improve AI accuracy. Default: OFF.",
                        style = MaterialTheme.typography.bodySmall,
                        color = KlikBlack.copy(alpha = 0.5f)
                    )
                    if (aiTrainingEnabled) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Data types used for training:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = KlikBlack.copy(alpha = 0.7f)
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
                            }
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
                            }
                        )
                    }
                }

                // Section 3: Memory Management
                PrivacyCard {
                    SectionHeader("Memories")
                    Spacer(Modifier.height(12.dp))
                    ToggleRow(
                        label = "Automatically extract information from conversations",
                        checked = memoryExtractionEnabled,
                        onCheckedChange = {
                            memoryExtractionEnabled = it
                            saveSettings()
                        }
                    )
                    Spacer(Modifier.height(4.dp))
                    TextButton(
                        onClick = { showClearMemoriesDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Clear All Memories",
                            color = Color(0xFFE53935),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Section 4: Recording & Consent
                PrivacyCard {
                    SectionHeader("Recording & Consent")
                    Spacer(Modifier.height(12.dp))
                    NavigationRow(
                        label = "Recording Consent",
                        onClick = onNavigateToRecordingConsent
                    )
                    Spacer(Modifier.height(4.dp))
                    NavigationRow(
                        label = "Speaker Identification (Biometric)",
                        onClick = onNavigateToBiometricConsent
                    )
                }

                // Section 5: Voiceprint Management (#46)
                PrivacyCard {
                    SectionHeader("Voiceprints")
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
                        }
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        if (voiceprintEnabled) "Active — ${voiceprints.size} voiceprint(s) stored"
                        else "Disabled — speakers labeled generically",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (voiceprintEnabled) KlikPrimary else KlikBlack.copy(alpha = 0.5f)
                    )

                    if (voiceprintEnabled && voiceprints.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))

                        voiceprints.forEach { vp ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        vp.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = KlikBlack
                                    )
                                    Text(
                                        "${vp.sampleCount} samples",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = KlikBlack.copy(alpha = 0.5f)
                                    )
                                }
                                IconButton(
                                    onClick = {
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
                                ) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "Delete voiceprint",
                                        tint = Color(0xFFE53935),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(4.dp))

                        TextButton(
                            onClick = { showDeleteAllVoiceprintsDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Delete All Voiceprints",
                                color = Color(0xFFE53935),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (isLoadingVoiceprints) {
                        Spacer(Modifier.height(8.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = KlikPrimary,
                            strokeWidth = 2.dp
                        )
                    }
                }

                // Section 6: Data Export
                PrivacyCard {
                    SectionHeader("Data Export")
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
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
                        enabled = !exportRequested,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = KlikPrimary
                        ),
                        border = BorderStroke(1.dp, KlikPrimary.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (exportRequested) "Export Requested" else "Download My Data",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SelectableChip(
                            text = "JSON",
                            selected = selectedExportFormat == "JSON",
                            onClick = { selectedExportFormat = "JSON" },
                            modifier = Modifier.weight(1f)
                        )
                        SelectableChip(
                            text = "CSV",
                            selected = selectedExportFormat == "CSV",
                            onClick = { selectedExportFormat = "CSV" },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "You will receive an email when your data export is ready",
                        style = MaterialTheme.typography.bodySmall,
                        color = KlikBlack.copy(alpha = 0.5f)
                    )
                }

                // Section 7: Account
                PrivacyCard {
                    SectionHeader("Account")
                    Spacer(Modifier.height(12.dp))
                    TextButton(
                        onClick = { showDeleteAccountDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Delete My Account",
                            color = Color(0xFFE53935),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "This will permanently delete all your data. There is a 30-day grace period.",
                        style = MaterialTheme.typography.bodySmall,
                        color = KlikBlack.copy(alpha = 0.5f)
                    )
                }

                // Section 8: California Residents (CCPA)
                PrivacyCard {
                    SectionHeader("California Residents")
                    Spacer(Modifier.height(12.dp))
                    ToggleRow(
                        label = "Do Not Sell or Share My Personal Information",
                        checked = doNotSell,
                        onCheckedChange = {
                            doNotSell = it
                            saveSettings()
                        }
                    )
                    Spacer(Modifier.height(4.dp))
                    ToggleRow(
                        label = "Limit Use of My Sensitive Personal Information",
                        checked = limitSensitive,
                        onCheckedChange = {
                            limitSensitive = it
                            saveSettings()
                        }
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "These controls apply regardless of whether KLIK sells data (we don't)",
                        style = MaterialTheme.typography.bodySmall,
                        color = KlikBlack.copy(alpha = 0.5f)
                    )

                    // CCPA Privacy Rights Request Forms
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Your Rights",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = KlikBlack
                    )
                    Spacer(Modifier.height(8.dp))

                    val requestTypes = listOf(
                        "access" to "Request Access to My Data",
                        "correct" to "Request Data Correction",
                        "delete" to "Request Data Deletion"
                    )

                    requestTypes.forEach { (type, label) ->
                        val hasPending = privacyRequests.any {
                            it.requestType == type && it.status in listOf("pending", "verified", "processing")
                        }
                        OutlinedButton(
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
                            enabled = !hasPending,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = KlikPrimary,
                                disabledContentColor = KlikBlack.copy(alpha = 0.4f)
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (hasPending) KlikBlack.copy(alpha = 0.1f) else KlikPrimary.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                        ) {
                            Text(
                                if (hasPending) "$label (Pending)" else label,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Privacy Request Status Tracker
                    if (privacyRequests.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Request History",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = KlikBlack
                        )
                        Spacer(Modifier.height(8.dp))

                        privacyRequests.forEach { request ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .background(KlikBlack.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        request.requestType.replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = KlikBlack
                                    )
                                    Text(
                                        "Submitted: ${request.requestedAt.take(10)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = KlikBlack.copy(alpha = 0.5f)
                                    )
                                    Text(
                                        "Deadline: ${request.responseDeadline.take(10)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = KlikBlack.copy(alpha = 0.5f)
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

    // Clear Memories confirmation dialog
    if (showClearMemoriesDialog) {
        io.github.fletchmckee.liquid.samples.app.ui.components.LiquidGlassDialog(
            onDismissRequest = { showClearMemoriesDialog = false },
            title = "Clear All Memories",
            message = "This will permanently delete all extracted memories. This action cannot be undone.",
            confirmText = "Clear All",
            isDestructive = true,
            onConfirm = {
                showClearMemoriesDialog = false
                scope.launch {
                    try {
                        RemoteDataFetcher.submitPrivacyRequest("delete")
                        KlikLogger.i("PrivacySettings", "Clear all memories requested")
                    } catch (e: Exception) {
                        KlikLogger.e("PrivacySettings", "Failed to clear memories: ${e.message}", e)
                    }
                }
            },
            dismissText = "Cancel"
        )
    }

    // Delete Account confirmation dialog
    if (showDeleteAccountDialog) {
        io.github.fletchmckee.liquid.samples.app.ui.components.LiquidGlassDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = "Delete Account",
            message = "This will permanently delete your account and all associated data after a 30-day grace period. This action cannot be undone.",
            confirmText = "Delete Account",
            isDestructive = true,
            onConfirm = {
                showDeleteAccountDialog = false
                scope.launch {
                    try {
                        RemoteDataFetcher.submitPrivacyRequest("delete")
                        KlikLogger.i("PrivacySettings", "Account deletion requested")
                    } catch (e: Exception) {
                        KlikLogger.e("PrivacySettings", "Failed to request account deletion: ${e.message}", e)
                    }
                }
            },
            dismissText = "Cancel"
        )
    }

    // Delete All Voiceprints confirmation dialog
    if (showDeleteAllVoiceprintsDialog) {
        io.github.fletchmckee.liquid.samples.app.ui.components.LiquidGlassDialog(
            onDismissRequest = { showDeleteAllVoiceprintsDialog = false },
            title = "Delete All Voiceprints",
            message = "This will permanently delete all stored voiceprints. Speaker identification will use generic labels until new voiceprints are collected.",
            confirmText = "Delete All",
            isDestructive = true,
            onConfirm = {
                showDeleteAllVoiceprintsDialog = false
                scope.launch {
                    try {
                        voiceprints.forEach { vp ->
                            RemoteDataFetcher.deleteVoiceprint(vp.id)
                        }
                        voiceprints = emptyList()
                        KlikLogger.i("PrivacySettings", "All voiceprints deleted")
                    } catch (e: Exception) {
                        KlikLogger.e("PrivacySettings", "Failed to delete voiceprints: ${e.message}", e)
                    }
                }
            },
            dismissText = "Cancel"
        )
    }
}

@Composable
private fun PrivacyCard(
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.95f), RoundedCornerShape(24.dp))
            .border(
                BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.12f)),
                RoundedCornerShape(24.dp)
            )
            .clip(RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        content()
    }
}

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = KlikBlack,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = KlikPrimary,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = KlikBlack.copy(alpha = 0.2f),
                uncheckedBorderColor = KlikBlack.copy(alpha = 0.1f)
            )
        )
    }
}

@Composable
private fun NavigationRow(
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = KlikBlack
        )
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = KlikBlack.copy(alpha = 0.4f),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun CheckboxRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = KlikPrimary,
                uncheckedColor = KlikBlack.copy(alpha = 0.4f)
            )
        )
        Spacer(Modifier.width(8.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = KlikBlack
        )
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (color, label) = when (status) {
        "pending" -> Color(0xFFFFA726) to "Pending"
        "verified" -> Color(0xFF42A5F5) to "Verified"
        "processing" -> Color(0xFF42A5F5) to "Processing"
        "completed" -> Color(0xFF66BB6A) to "Completed"
        "denied" -> Color(0xFFE53935) to "Denied"
        else -> KlikBlack.copy(alpha = 0.5f) to status.replaceFirstChar { it.uppercase() }
    }
    Text(
        label,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = Modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Composable
private fun SelectableChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = if (selected) KlikPrimary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                BorderStroke(
                    1.dp,
                    if (selected) KlikPrimary else KlikBlack.copy(alpha = 0.2f)
                ),
                RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) Color.White else KlikBlack.copy(alpha = 0.7f)
        )
    }
}
