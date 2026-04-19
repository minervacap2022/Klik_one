package io.github.fletchmckee.liquid.samples.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.fletchmckee.liquid.liquid
import io.github.fletchmckee.liquid.rememberLiquidState
import io.github.fletchmckee.liquid.samples.app.data.source.remote.NotificationDto
import io.github.fletchmckee.liquid.samples.app.theme.KlikBlack
import io.github.fletchmckee.liquid.samples.app.theme.LocalLiquidGlassSettings
import io.github.fletchmckee.liquid.samples.app.ui.components.LiquidPullToRefreshIndicator
import liquid_root.samples.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    notifications: List<NotificationDto>,
    isLoading: Boolean,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onMarkRead: (Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val liquidState = rememberLiquidState()
    val pullRefreshState = rememberPullToRefreshState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top bar with back button
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
                    contentDescription = stringResource(Res.string.nav_back),
                    tint = KlikBlack
                )
            }
            Text(
                stringResource(Res.string.notifications_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = KlikBlack
            )
        }

        when {
            isLoading && notifications.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(40.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            stringResource(Res.string.notifications_loading),
                            color = KlikBlack.copy(alpha = 0.5f),
                            fontSize = 14.sp
                        )
                    }
                }
            }
            notifications.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Filled.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = KlikBlack.copy(alpha = 0.2f)
                        )
                        Text(
                            stringResource(Res.string.notifications_none),
                            style = MaterialTheme.typography.titleMedium,
                            color = KlikBlack.copy(alpha = 0.5f)
                        )
                    }
                }
            }
            else -> {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    state = pullRefreshState,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize(),
                    indicator = {
                        LiquidPullToRefreshIndicator(
                            state = pullRefreshState,
                            isRefreshing = isRefreshing,
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    }
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = notifications,
                            key = { it.id }
                        ) { notification ->
                            NotificationCard(
                                notification = notification,
                                onClick = {
                                    if (!notification.isRead) {
                                        onMarkRead(notification.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification: NotificationDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val liquidState = rememberLiquidState()
    val glassSettings = LocalLiquidGlassSettings.current
    val cardShape = RoundedCornerShape(24.dp)

    val eventIcon = getEventTypeIcon(notification.eventType)
    val eventColor = getEventTypeColor(notification.eventType)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Color.White.copy(alpha = if (glassSettings.applyToCards) glassSettings.transparency else 0.95f),
                cardShape
            )
            .border(BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.12f)), cardShape)
            .liquid(liquidState) {
                edge = if (glassSettings.applyToCards) glassSettings.edge else 0.01f
                shape = cardShape
                if (glassSettings.applyToCards) {
                    frost = glassSettings.frost
                    curve = glassSettings.curve
                    refraction = glassSettings.refraction
                }
                tint = Color.Transparent
            }
            .clip(cardShape)
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Unread indicator + Event type icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(eventColor.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                eventIcon,
                contentDescription = null,
                tint = eventColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = notification.title ?: notification.eventType.replace("_", " ").replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.SemiBold,
                    color = KlikBlack,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Unread dot
                if (!notification.isRead) {
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(8.dp)
                            .background(Color(0xFF4A90E2), CircleShape)
                    )
                }
            }

            if (notification.body != null) {
                Text(
                    text = notification.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = KlikBlack.copy(alpha = 0.6f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = formatNotificationTime(notification.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = KlikBlack.copy(alpha = 0.4f)
            )
        }
    }
}

private fun getEventTypeIcon(eventType: String): ImageVector {
    return when (eventType) {
        "meeting_generated" -> Icons.Filled.DateRange
        "sensitive_task_created" -> Icons.Filled.Star
        "daily_task_completed", "daily_task_cannot_do" -> Icons.Filled.Settings
        else -> Icons.Filled.Notifications
    }
}

private fun getEventTypeColor(eventType: String): Color {
    return when (eventType) {
        "meeting_generated" -> Color(0xFF4A90E2)        // Blue
        "sensitive_task_created" -> Color(0xFFFF9F1C)    // Orange
        "daily_task_completed" -> Color(0xFF2EC4B6)      // Teal
        "daily_task_cannot_do" -> Color(0xFFE84855)      // Red
        else -> Color(0xFFA663CC)                        // Purple
    }
}

private fun formatNotificationTime(createdAt: String): String {
    if (createdAt.isBlank()) return ""
    // Show date portion from ISO timestamp (e.g., "2026-02-09T10:30:00" -> "Feb 9")
    return try {
        val datePart = createdAt.substringBefore("T")
        val parts = datePart.split("-")
        if (parts.size == 3) {
            val month = when (parts[1]) {
                "01" -> "Jan"; "02" -> "Feb"; "03" -> "Mar"; "04" -> "Apr"
                "05" -> "May"; "06" -> "Jun"; "07" -> "Jul"; "08" -> "Aug"
                "09" -> "Sep"; "10" -> "Oct"; "11" -> "Nov"; "12" -> "Dec"
                else -> parts[1]
            }
            val day = parts[2].trimStart('0')
            "$month $day"
        } else {
            datePart
        }
    } catch (_: Exception) {
        createdAt.substringBefore("T")
    }
}
