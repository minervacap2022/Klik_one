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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import io.github.fletchmckee.liquid.samples.app.data.network.ApiConfig
import io.github.fletchmckee.liquid.samples.app.platform.OAuthBrowser
import io.github.fletchmckee.liquid.samples.app.theme.KlikBlack
import io.github.fletchmckee.liquid.samples.app.theme.KlikPrimary

@Composable
fun RecordingConsentScreen(
    onAccept: () -> Unit,
    onBack: () -> Unit,
    isOnboarding: Boolean = false,
    onSignOut: () -> Unit = {}
) {
    var informParticipants by remember { mutableStateOf(false) }
    var acceptResponsibility by remember { mutableStateOf(false) }
    var agreeToTerms by remember { mutableStateOf(false) }

    val allChecked = informParticipants && acceptResponsibility && agreeToTerms

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top bar — in onboarding the back button is hidden (consent is mandatory to proceed);
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
                "Recording Consent",
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

            // Explanation card
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
                    "What KLIK Records",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = KlikBlack
                )

                BulletPoint(
                    "KLIK records audio during your sessions to generate meeting transcripts, summaries, and action items."
                )
                BulletPoint(
                    "Recordings are processed using AI and stored securely on our servers."
                )
                BulletPoint(
                    "You can delete your recordings at any time from your account settings."
                )
            }

            // All-Party Consent State Warning
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFF8E1), RoundedCornerShape(24.dp))
                    .border(
                        BorderStroke(0.5.dp, Color(0xFFFFB300).copy(alpha = 0.3f)),
                        RoundedCornerShape(24.dp)
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Filled.Warning,
                    contentDescription = null,
                    tint = Color(0xFFFF8F00),
                    modifier = Modifier.size(24.dp)
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "All-Party Consent Warning",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100)
                    )
                    Text(
                        "Your state may require consent from ALL participants before recording. " +
                            "This applies to residents of CA, FL, IL, MA, WA, DE, MD, MT, NV, NH, and PA.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF795548)
                    )
                }
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
                    "Your Acknowledgement",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = KlikBlack
                )

                Spacer(Modifier.height(8.dp))

                ConsentCheckboxRow(
                    checked = informParticipants,
                    onCheckedChange = { informParticipants = it },
                    label = "I will inform all participants before recording"
                )

                ConsentCheckboxRow(
                    checked = acceptResponsibility,
                    onCheckedChange = { acceptResponsibility = it },
                    label = "I accept responsibility for obtaining consent from all parties"
                )

                ConsentCheckboxRow(
                    checked = agreeToTerms,
                    onCheckedChange = { agreeToTerms = it },
                    label = "I have read and agree to the Privacy Policy and Terms of Service"
                )
            }

            // Links
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Privacy Policy",
                    style = MaterialTheme.typography.bodyMedium,
                    color = KlikPrimary,
                    fontWeight = FontWeight.Medium,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable {
                        OAuthBrowser.openUrl(ApiConfig.PRIVACY_URL)
                    }
                )
                Spacer(Modifier.width(24.dp))
                Text(
                    "Terms of Service",
                    style = MaterialTheme.typography.bodyMedium,
                    color = KlikPrimary,
                    fontWeight = FontWeight.Medium,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable {
                        OAuthBrowser.openUrl(ApiConfig.TERMS_URL)
                    }
                )
            }

            // Accept button
            Button(
                onClick = onAccept,
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
                    "I Accept",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun BulletPoint(text: String) {
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
private fun ConsentCheckboxRow(
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
