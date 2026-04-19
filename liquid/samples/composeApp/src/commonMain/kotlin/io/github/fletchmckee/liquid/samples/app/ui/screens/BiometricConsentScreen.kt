package io.github.fletchmckee.liquid.samples.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.width
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.fletchmckee.liquid.samples.app.theme.KlikBlack
import io.github.fletchmckee.liquid.samples.app.theme.KlikPrimary

@Composable
fun BiometricConsentScreen(
    onEnable: () -> Unit,
    onDecline: () -> Unit,
    onBack: () -> Unit,
    isOnboarding: Boolean = false,
    onSignOut: () -> Unit = {}
) {
    var acknowledgeNotice by remember { mutableStateOf(false) }
    var consentCollection by remember { mutableStateOf(false) }

    val allChecked = acknowledgeNotice && consentCollection

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top bar — in onboarding the back button is hidden (consent is mandatory);
        // the only escape is signing out.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isOnboarding) {
                Spacer(Modifier.width(16.dp))
            } else {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = KlikBlack
                    )
                }
            }
            Text(
                "Speaker Identification",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = KlikBlack,
                modifier = Modifier.weight(1f)
            )
            if (isOnboarding) {
                TextButton(onClick = onSignOut) {
                    Text("Sign out", color = KlikBlack.copy(alpha = 0.7f))
                }
            }
        }

        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Written notice card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.95f), RoundedCornerShape(24.dp))
                    .border(
                        BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.12f)),
                        RoundedCornerShape(24.dp)
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "How Speaker Identification Works",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = KlikBlack
                )

                BiometricBulletPoint(
                    "KLIK uses voice characteristics (voiceprints) to identify speakers across your recordings."
                )
                BiometricBulletPoint(
                    "Your voiceprint is a 192-dimensional mathematical representation of your voice."
                )
                BiometricBulletPoint(
                    "Voiceprints are stored securely and isolated to your account."
                )
                BiometricBulletPoint(
                    "Voiceprints are retained until you delete them or close your account."
                )
                BiometricBulletPoint(
                    "You can delete your voiceprints at any time."
                )
            }

            // Checkboxes card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.95f), RoundedCornerShape(24.dp))
                    .border(
                        BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.12f)),
                        RoundedCornerShape(24.dp)
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "Your Consent",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = KlikBlack
                )

                Spacer(Modifier.height(8.dp))

                BiometricCheckboxRow(
                    checked = acknowledgeNotice,
                    onCheckedChange = { acknowledgeNotice = it },
                    label = "I acknowledge this biometric data notice"
                )

                BiometricCheckboxRow(
                    checked = consentCollection,
                    onCheckedChange = { consentCollection = it },
                    label = "I consent to the collection and use of my voiceprint for speaker identification"
                )
            }

            // Enable button
            Button(
                onClick = onEnable,
                enabled = allChecked,
                colors = ButtonDefaults.buttonColors(
                    containerColor = KlikPrimary,
                    contentColor = Color.White,
                    disabledContainerColor = KlikPrimary.copy(alpha = 0.4f),
                    disabledContentColor = Color.White.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    "Enable Speaker Identification",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Decline button — hidden during onboarding; recording is blocked without consent
            if (!isOnboarding) {
                OutlinedButton(
                    onClick = onDecline,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = KlikBlack
                    ),
                    border = BorderStroke(1.dp, KlikBlack.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        "Continue Without Speaker ID",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Decline explanation
            Text(
                "Recording will still work, but speakers will be labeled generically " +
                    "(Speaker 1, Speaker 2) without cross-session matching.",
                style = MaterialTheme.typography.bodySmall,
                color = KlikBlack.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun BiometricBulletPoint(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            "\u2022",
            style = MaterialTheme.typography.bodyLarge,
            color = KlikPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = KlikBlack.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun BiometricCheckboxRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = KlikPrimary,
                uncheckedColor = KlikBlack.copy(alpha = 0.4f)
            )
        )
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = KlikBlack
        )
    }
}
