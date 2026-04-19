package io.github.fletchmckee.liquid.samples.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import io.github.fletchmckee.liquid.samples.app.ui.icons.CustomIcons
import io.github.fletchmckee.liquid.samples.app.ui.icons.Copy
import io.github.fletchmckee.liquid.liquid
import io.github.fletchmckee.liquid.rememberLiquidState
import io.github.fletchmckee.liquid.samples.app.theme.LocalLiquidGlassSettings
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.fletchmckee.liquid.samples.app.theme.KlikBlack
import io.github.fletchmckee.liquid.samples.app.theme.KlikPrimary

/**
 * Full screen loading indicator
 */
@Composable
fun LoadingScreen(
    modifier: Modifier = Modifier,
    message: String = "Loading..."
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = KlikPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = KlikBlack.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * Inline loading indicator for use within lists or cards
 */
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    size: Int = 24
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(size.dp),
            color = KlikPrimary,
            strokeWidth = 2.dp
        )
    }
}

/**
 * Full screen error state with retry option
 */
@Composable
fun ErrorScreen(
    error: String,
    onRetry: (() -> Unit)? = null,
    onReport: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Error",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.titleMedium,
                color = KlikBlack
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = KlikBlack.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
            if (onRetry != null) {
                Spacer(modifier = Modifier.height(24.dp))
                val liquidState = rememberLiquidState()
                val glassSettings = LocalLiquidGlassSettings.current

                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .liquid(liquidState) {
                             edge = if (glassSettings.applyToCards) glassSettings.edge else 0.05f
                             shape = RoundedCornerShape(24.dp)
                             if (glassSettings.applyToCards) {
                                 frost = glassSettings.frost
                                 curve = glassSettings.curve
                                 refraction = glassSettings.refraction
                             }
                             tint = KlikPrimary.copy(alpha = 0.1f)
                        }
                        .clip(RoundedCornerShape(24.dp))
                        .clickable { onRetry() }
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = KlikPrimary
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            "Try Again",
                            color = KlikPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            if (onReport != null) {
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = onReport) {
                    Text(
                        "Report Issue",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Inline error message with optional retry
 */
@Composable
fun ErrorMessage(
    error: String,
    onRetry: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    onReport: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.errorContainer,
                RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            if (onRetry != null || onDismiss != null || onReport != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (onRetry != null) {
                        TextButton(onClick = onRetry) {
                            Text("Retry", color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                    if (onReport != null) {
                        TextButton(onClick = onReport) {
                            Text("Report", color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                    if (onDismiss != null) {
                        TextButton(onClick = onDismiss) {
                            Text("Dismiss", color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Error popup overlay that:
 * - Never truncates (uses vertical scroll for long messages)
 * - Has a copy button to copy error message to clipboard
 * - Shows a warning icon and title
 * - Can be dismissed by tapping outside
 *
 * @param isVisible Whether the popup is visible
 * @param title Title text (default: "Error")
 * @param message Error message to display (will scroll if too long)
 * @param onDismiss Called when user taps outside to dismiss
 * @param modifier Optional modifier
 */
@Composable
fun ErrorPopup(
    isVisible: Boolean,
    title: String = "Error",
    message: String,
    onDismiss: () -> Unit,
    onRetry: (() -> Unit)? = null,
    onReport: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { onDismiss() })
                },
            contentAlignment = Alignment.Center
        ) {
            // Error card - scrollable content, copy button, fixed height
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFFFEBEE))
                    .pointerInput(Unit) {
                        // Consume touches inside the card to prevent dismissing
                        detectTapGestures { }
                    }
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header row with warning icon, title, and copy button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFB71C1C),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFB71C1C),
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    CopyToClipboardButton(message = message)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable error message - NEVER truncates
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp) // Fixed height to ensure scrollability
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.5f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF5D4037),
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (onReport != null) {
                        TextButton(onClick = onReport) {
                            Text(
                                text = "Report Issue",
                                color = Color(0xFFB71C1C),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }
                    Row {
                        if (onRetry != null) {
                            TextButton(onClick = onRetry) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = KlikPrimary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Retry",
                                        color = KlikPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                        TextButton(onClick = onDismiss) {
                            Text(
                                text = "Dismiss",
                                color = Color(0xFFB71C1C)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Copy button that copies the message to clipboard and shows feedback
 */
@Composable
private fun CopyToClipboardButton(message: String) {
    val clipboardManager = LocalClipboardManager.current
    var showCopiedFeedback by remember { mutableStateOf(false) }

    IconButton(
        onClick = {
            clipboardManager.setText(AnnotatedString(message))
            showCopiedFeedback = true
        }
    ) {
        if (showCopiedFeedback) {
            // Show "Copied!" feedback briefly
            Text(
                text = "Copied!",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF4CAF50),
                maxLines = 1
            )
        } else {
                Icon(
                    imageVector = CustomIcons.Copy,
                    contentDescription = "Copy error message",
                    tint = Color(0xFFB71C1C),
                    modifier = Modifier.size(18.dp)
                )
        }
    }

    // Auto-hide feedback after 1.5 seconds
    if (showCopiedFeedback) {
        androidx.compose.runtime.LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(1500)
            showCopiedFeedback = false
        }
    }
}

/**
 * Empty state for lists with no data
 */
@Composable
fun EmptyState(
    title: String,
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = KlikBlack
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = KlikBlack.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
            if (actionLabel != null && onAction != null) {
                Spacer(modifier = Modifier.height(24.dp))
                val liquidState = rememberLiquidState()
                val glassSettings = LocalLiquidGlassSettings.current
                
                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .liquid(liquidState) {
                             edge = if (glassSettings.applyToCards) glassSettings.edge else 0.05f
                             shape = RoundedCornerShape(24.dp)
                             if (glassSettings.applyToCards) {
                                 frost = glassSettings.frost
                                 curve = glassSettings.curve
                                 refraction = glassSettings.refraction
                             }
                             tint = KlikPrimary.copy(alpha = 0.1f)
                        }
                        .clip(RoundedCornerShape(24.dp))
                        .clickable { onAction() }
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        actionLabel, 
                        color = KlikPrimary, 
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * Overlay loading indicator that shows on top of existing content
 */
@Composable
fun LoadingOverlay(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        content()

        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Content state wrapper that handles loading, error, and success states
 */
@Composable
fun <T> ContentState(
    isLoading: Boolean,
    error: String?,
    data: T?,
    onRetry: (() -> Unit)? = null,
    onDismissError: (() -> Unit)? = null,
    onReport: (() -> Unit)? = null,
    emptyContent: @Composable () -> Unit = { EmptyState("No Data", "There's nothing to show here.") },
    content: @Composable (T) -> Unit
) {
    when {
        isLoading && data == null -> LoadingScreen()
        error != null && data == null -> ErrorScreen(error = error, onRetry = onRetry, onReport = onReport)
        data != null -> {
            Column {
                // Show error banner if there's an error but we have data
                if (error != null) {
                    ErrorMessage(
                        error = error,
                        onRetry = onRetry,
                        onDismiss = onDismissError,
                        onReport = onReport,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                content(data)
            }
        }
        else -> emptyContent()
    }
}

// ==================== Shimmer Loading Components ====================

/**
 * Unified pulse-animated shimmer bar for loading states.
 * Use this everywhere content is loading — cards, sections, text placeholders.
 */
@Composable
fun ShimmerBar(
    modifier: Modifier = Modifier,
    color: Color = Color.Gray,
    delayMillis: Int = 0
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, delayMillis = delayMillis),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )
    Box(
        modifier = modifier
            .background(color.copy(alpha = alpha), RoundedCornerShape(6.dp))
    )
}

/**
 * Shimmer placeholder for a text block (multiple lines).
 * Shows 2-4 breathing bars simulating text loading.
 */
@Composable
fun ShimmerTextBlock(
    lines: Int = 3,
    modifier: Modifier = Modifier,
    color: Color = Color.Gray
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        repeat(lines) { index ->
            ShimmerBar(
                modifier = Modifier
                    .fillMaxWidth(if (index == lines - 1) 0.6f else 1f)
                    .height(12.dp),
                color = color,
                delayMillis = index * 150
            )
        }
    }
}

/**
 * Shimmer placeholder for a card section (title + text block).
 */
@Composable
fun ShimmerCardContent(
    modifier: Modifier = Modifier,
    color: Color = Color.Gray
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ShimmerBar(
            modifier = Modifier.width(120.dp).height(14.dp),
            color = color,
            delayMillis = 0
        )
        ShimmerTextBlock(lines = 3, color = color)
    }
}
