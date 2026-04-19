package io.github.fletchmckee.liquid.samples.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.fletchmckee.liquid.samples.app.domain.entity.IntegrationInfo
import io.github.fletchmckee.liquid.samples.app.domain.entity.IntegrationProviders
import io.github.fletchmckee.liquid.samples.app.theme.KlikBlack
import io.github.fletchmckee.liquid.samples.app.theme.KlikPrimary
import io.github.fletchmckee.liquid.samples.app.theme.montserratFontFamily
import kotlinx.coroutines.delay

/**
 * Dialog that prompts users to connect their integrations.
 * Features:
 * - Auto-dismisses after countdown (default 3 seconds)
 * - Shows unconnected integrations as tappable cards
 * - "Never show again" checkbox option
 *
 * @param isVisible Whether the dialog is visible
 * @param unconnectedIntegrations List of integrations that are not yet connected
 * @param autoDismissSeconds Number of seconds before auto-dismiss (default 3)
 * @param onIntegrationClick Called when user taps an integration to authorize
 * @param onDismiss Called when dialog is dismissed (tap outside, countdown ends, or explicit close)
 * @param onNeverShowAgain Called when user checks "Never show again" and dismisses
 */
@Composable
fun IntegrationPromptDialog(
    isVisible: Boolean,
    unconnectedIntegrations: List<IntegrationInfo>,
    autoDismissSeconds: Int = 3,
    onIntegrationClick: (String) -> Unit,
    onDismiss: () -> Unit,
    onNeverShowAgain: () -> Unit
) {
    var remainingSeconds by remember(isVisible) { mutableStateOf(autoDismissSeconds) }
    var neverShowAgain by remember(isVisible) { mutableStateOf(false) }
    var progress by remember(isVisible) { mutableStateOf(1f) }

    // Countdown timer
    LaunchedEffect(isVisible) {
        if (isVisible) {
            remainingSeconds = autoDismissSeconds
            progress = 1f

            // Countdown loop
            while (remainingSeconds > 0) {
                delay(100) // Update every 100ms for smooth progress
                progress -= (0.1f / autoDismissSeconds)
                if (progress <= 0) {
                    remainingSeconds--
                    if (remainingSeconds > 0) {
                        // Don't reset progress on last iteration
                    }
                }
            }

            // Auto-dismiss when countdown finishes
            if (neverShowAgain) {
                onNeverShowAgain()
            } else {
                onDismiss()
            }
        }
    }

    // More accurate countdown display
    LaunchedEffect(isVisible) {
        if (isVisible) {
            remainingSeconds = autoDismissSeconds
            repeat(autoDismissSeconds) {
                delay(1000)
                remainingSeconds--
            }
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(200)) + scaleIn(tween(200), initialScale = 0.9f),
        exit = fadeOut(tween(150)) + scaleOut(tween(150), targetScale = 0.9f)
    ) {
        // Full-screen overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        if (neverShowAgain) {
                            onNeverShowAgain()
                        } else {
                            onDismiss()
                        }
                    })
                },
            contentAlignment = Alignment.Center
        ) {
            // Dialog card
            Column(
                modifier = Modifier
                    .widthIn(max = 340.dp)
                    .padding(horizontal = 24.dp)
                    .shadow(
                        elevation = 24.dp,
                        shape = RoundedCornerShape(24.dp),
                        clip = true
                    )
                    .background(Color.White, RoundedCornerShape(24.dp))
                    .pointerInput(Unit) {
                        // Consume clicks to prevent dismissing when clicking on dialog
                        detectTapGestures { }
                    }
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Text(
                    text = "Connect Your Apps",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = montserratFontFamily()
                    ),
                    fontWeight = FontWeight.Bold,
                    color = KlikBlack
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Get the most out of Klik by connecting your favorite apps",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = montserratFontFamily()
                    ),
                    color = KlikBlack.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(20.dp))

                // Integration cards (horizontal scroll)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(
                        items = unconnectedIntegrations.take(5), // Show max 5
                        key = { it.providerId }
                    ) { integration ->
                        IntegrationPromptCard(
                            integration = integration,
                            onClick = { onIntegrationClick(integration.providerId) }
                        )
                    }
                }

                if (unconnectedIntegrations.size > 5) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "+${unconnectedIntegrations.size - 5} more",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Progress bar (countdown indicator)
                LinearProgressIndicator(
                    progress = { (remainingSeconds.toFloat() / autoDismissSeconds) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = KlikPrimary,
                    trackColor = Color.Gray.copy(alpha = 0.2f),
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Closing in ${remainingSeconds}s",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )

                Spacer(Modifier.height(16.dp))

                // Never show again checkbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { neverShowAgain = !neverShowAgain }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Checkbox(
                        checked = neverShowAgain,
                        onCheckedChange = { neverShowAgain = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = KlikPrimary,
                            uncheckedColor = Color.Gray
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Don't show this again",
                        style = MaterialTheme.typography.bodySmall,
                        color = KlikBlack.copy(alpha = 0.7f)
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Dismiss button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Gray.copy(alpha = 0.1f))
                        .clickable {
                            if (neverShowAgain) {
                                onNeverShowAgain()
                            } else {
                                onDismiss()
                            }
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Maybe Later",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = KlikBlack.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * Individual integration card shown in the prompt dialog.
 * Displays differently for Apple native integrations (Calendar, Reminders)
 * vs OAuth integrations (Notion, Slack, etc.)
 */
@Composable
private fun IntegrationPromptCard(
    integration: IntegrationInfo,
    onClick: () -> Unit
) {
    val displayInfo = IntegrationProviders.getDisplayInfo(integration.providerId)
    val displayName = displayInfo?.name ?: integration.displayName
    val initial = displayInfo?.initial ?: integration.displayName.take(1)
    
    // Check if this is an Apple native integration
    val isAppleNative = IntegrationProviders.isAppleNativeProvider(integration.providerId)
    
    // Apple native integrations have a slightly different color scheme
    val iconBackgroundColor = if (isAppleNative) {
        Color(0xFF007AFF).copy(alpha = 0.1f) // iOS blue
    } else {
        KlikPrimary.copy(alpha = 0.1f)
    }
    val iconTextColor = if (isAppleNative) {
        Color(0xFF007AFF) // iOS blue
    } else {
        KlikPrimary
    }

    Column(
        modifier = Modifier
            .width(72.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Icon circle
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(iconBackgroundColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = montserratFontFamily()
                ),
                fontWeight = FontWeight.Bold,
                color = iconTextColor
            )
        }

        // Name
        Text(
            text = displayName,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = KlikBlack,
            maxLines = 1,
            textAlign = TextAlign.Center
        )

        // Connect label - different text for Apple native
        Text(
            text = if (isAppleNative) "Allow" else "Connect",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = iconTextColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}
