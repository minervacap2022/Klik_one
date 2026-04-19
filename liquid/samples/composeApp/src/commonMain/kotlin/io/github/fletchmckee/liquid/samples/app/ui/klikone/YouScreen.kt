// Copyright 2025, Klik — Klik One redesign of the You tab (legacy ProfileScreen).
package io.github.fletchmckee.liquid.samples.app.ui.klikone

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.fletchmckee.liquid.samples.app.core.rememberViewModel
import io.github.fletchmckee.liquid.samples.app.domain.entity.Subscription
import io.github.fletchmckee.liquid.samples.app.presentation.profile.ProfileViewModel
import io.github.fletchmckee.liquid.samples.app.theme.*

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
) {
    val ui by viewModel.state.collectAsState()
    val user = ui.user
    val devices = ui.connectedDevices

    Column(
        Modifier
            .fillMaxSize()
            .background(KlikPaperApp)
            .verticalScroll(rememberScrollState())
            .padding(top = 52.dp, bottom = 120.dp)
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
                    K1Avatar(initials, size = 56.dp)
                    Spacer(Modifier.width(K1Sp.m))
                    Column(Modifier.weight(1f)) {
                        Text(user?.name ?: "—", style = K1Type.h3)
                        Spacer(Modifier.height(2.dp))
                        Text(user?.email ?: "—", style = K1Type.caption)
                    }
                    K1Chip(label = "Edit", onClick = {})
                }
            }
        }

        Spacer(Modifier.height(K1Sp.xxl))

        // Plan
        Column(Modifier.padding(horizontal = 20.dp)) {
            K1SectionHeader("Plan")
            Spacer(Modifier.height(K1Sp.s))
            K1Card(onClick = onNavigateToPricing) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            subscription?.displayName ?: (user?.planType?.label ?: "Starter"),
                            style = K1Type.bodyMd,
                            modifier = Modifier.weight(1f)
                        )
                        subscription?.billingCycle?.let {
                            Text(it.replaceFirstChar { c -> c.uppercase() }, style = K1Type.metaSm)
                        }
                    }
                    subscription?.usage?.let { usage ->
                        Spacer(Modifier.height(6.dp))
                        val frac = if (usage.asrMinutesLimit > 0)
                            usage.asrMinutesUsed.toFloat() / usage.asrMinutesLimit
                        else 0f
                        Box(
                            Modifier.fillMaxWidth().height(3.dp).clip(K1R.pill)
                                .background(KlikLineHairline)
                        ) {
                            Box(Modifier.fillMaxHeight().fillMaxWidth(frac.coerceIn(0f, 1f))
                                .background(KlikInkPrimary))
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "ASR ${usage.asrMinutesUsed} / ${usage.asrMinutesLimit} min",
                            style = K1Type.metaSm
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(K1Sp.xxl))

        // Devices
        if (devices.isNotEmpty()) {
            Column(Modifier.padding(horizontal = 20.dp)) {
                K1SectionHeader("Devices", count = devices.size)
                Spacer(Modifier.height(K1Sp.s))
                devices.forEach { d ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            Modifier.size(8.dp).clip(androidx.compose.foundation.shape.CircleShape)
                                .background(if (d.isConnected) KlikRunning else KlikInkMuted)
                        )
                        Spacer(Modifier.width(K1Sp.m))
                        Column(Modifier.weight(1f)) {
                            Text(d.name, style = K1Type.bodySm)
                            Spacer(Modifier.height(2.dp))
                            Text(
                                if (d.isConnected) "Connected" else "Offline",
                                style = K1Type.metaSm
                            )
                        }
                    }
                    Box(Modifier.fillMaxWidth().height(0.5.dp).background(KlikPaperChip))
                }
            }
            Spacer(Modifier.height(K1Sp.xxl))
        }

        // Settings list
        Column(Modifier.padding(horizontal = 20.dp)) {
            K1SectionHeader("Settings")
            Spacer(Modifier.height(K1Sp.s))
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(K1R.card)
                    .background(KlikPaperCard)
                    .padding(vertical = 4.dp)
            ) {
                SettingsRow("Archived sessions", onClick = onNavigateToArchived)
                Divider()
                SettingsRow("Notifications", onClick = onNavigateToNotificationSettings)
                Divider()
                SettingsRow("Privacy", onClick = onNavigateToPrivacy)
                Divider()
                SettingsRow("Account & security", onClick = onNavigateToAccountSecurity)
                Divider()
                SettingsRow("Plans", onClick = onNavigateToPricing)
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
                    .clickable(onClick = { viewModel.showLogoutConfirmation() })
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Sign out",
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
}

@Composable
private fun SignOutConfirmDialog(onCancel: () -> Unit, onConfirm: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(onClick = onCancel),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier
                .padding(horizontal = 32.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(KlikPaperCard)
                .clickable(enabled = false) {} // absorb taps on card
                .padding(24.dp),
        ) {
            Text("Sign out of Klik?", style = K1Type.h3)
            Spacer(Modifier.height(K1Sp.s))
            Text(
                "You'll need to sign in again to pick up where you left off.",
                style = K1Type.bodySm.copy(color = KlikInkSecondary),
            )
            Spacer(Modifier.height(K1Sp.xl))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    Modifier
                        .weight(1f)
                        .clip(K1R.pill)
                        .background(KlikPaperChip)
                        .clickable(onClick = onCancel)
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Cancel", style = K1Type.bodyMd)
                }
                Box(
                    Modifier
                        .weight(1f)
                        .clip(K1R.pill)
                        .background(KlikAlert)
                        .clickable(onClick = onConfirm)
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Sign out",
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
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = K1Type.bodySm, modifier = Modifier.weight(1f))
        // chevron
        androidx.compose.foundation.Canvas(Modifier.size(12.dp)) {
            val w = 1.2.dp.toPx()
            drawLine(color = KlikInkMuted, strokeWidth = w, cap = androidx.compose.ui.graphics.StrokeCap.Round,
                start = androidx.compose.ui.geometry.Offset(4.dp.toPx(), 3.dp.toPx()),
                end = androidx.compose.ui.geometry.Offset(8.dp.toPx(), 6.dp.toPx()))
            drawLine(color = KlikInkMuted, strokeWidth = w, cap = androidx.compose.ui.graphics.StrokeCap.Round,
                start = androidx.compose.ui.geometry.Offset(8.dp.toPx(), 6.dp.toPx()),
                end = androidx.compose.ui.geometry.Offset(4.dp.toPx(), 9.dp.toPx()))
        }
    }
}

@Composable
private fun Divider() {
    Box(Modifier.fillMaxWidth().height(0.5.dp).background(KlikLineHairline)
        .padding(horizontal = 16.dp))
}
