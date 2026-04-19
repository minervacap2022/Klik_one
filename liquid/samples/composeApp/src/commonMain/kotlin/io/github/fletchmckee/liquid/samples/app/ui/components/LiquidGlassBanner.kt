package io.github.fletchmckee.liquid.samples.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.fletchmckee.liquid.liquid
import io.github.fletchmckee.liquid.rememberLiquidState
import io.github.fletchmckee.liquid.samples.app.theme.LocalLiquidGlassSettings
import kotlinx.coroutines.delay

/**
 * Prominent liquid-glass styled banner that slides down from the top.
 * Used for important user-facing notifications (e.g. recording started).
 *
 * Auto-dismisses after [durationMillis] when [visible] is true.
 */
@Composable
fun LiquidGlassBanner(
    visible: Boolean,
    title: String,
    subtitle: String? = null,
    accentColor: Color = Color(0xFFE53935),
    showPulse: Boolean = false,
    durationMillis: Long = 4000L,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(visible) {
        if (visible) {
            delay(durationMillis)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(animationSpec = tween(400)) { -it } + fadeIn(animationSpec = tween(400)),
        exit = slideOutVertically(animationSpec = tween(400)) { -it } + fadeOut(animationSpec = tween(400)),
        modifier = modifier
    ) {
        val liquidState = rememberLiquidState()
        val glassSettings = LocalLiquidGlassSettings.current
        val cardShape = RoundedCornerShape(20.dp)

        // Pulsing dot animation for "live" feel
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val pulseAlpha by infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(800),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseAlpha"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.92f),
                                accentColor.copy(alpha = 0.78f)
                            )
                        ),
                        shape = cardShape
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.25f),
                        shape = cardShape
                    )
                    .liquid(liquidState) {
                        edge = if (glassSettings.applyToCards) glassSettings.edge else 0.01f
                        shape = cardShape
                        if (glassSettings.applyToCards) {
                            frost = glassSettings.frost
                            curve = glassSettings.curve
                            refraction = glassSettings.refraction
                        }
                        tint = Color.White.copy(alpha = 0.05f)
                    }
                    .clip(cardShape)
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (showPulse) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    Color.White.copy(alpha = pulseAlpha),
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.size(12.dp))
                    }
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = title,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (subtitle != null) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = subtitle,
                                color = Color.White.copy(alpha = 0.92f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Liquid-glass action banner that replaces Material undo Snackbars.
 * Auto-dismisses after [durationMillis] unless the user taps the action.
 *
 * Slides up from the bottom and shows a message + tappable action label.
 */
@Composable
fun LiquidGlassActionBanner(
    visible: Boolean,
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
    onDismiss: () -> Unit,
    durationMillis: Long = 4000L,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(visible) {
        if (visible) {
            delay(durationMillis)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(animationSpec = tween(350)) { it } + fadeIn(animationSpec = tween(350)),
        exit = slideOutVertically(animationSpec = tween(350)) { it } + fadeOut(animationSpec = tween(350)),
        modifier = modifier
    ) {
        val liquidState = rememberLiquidState()
        val glassSettings = LocalLiquidGlassSettings.current
        val cardShape = RoundedCornerShape(20.dp)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color(0xFF1A1A1A).copy(alpha = 0.92f),
                        cardShape
                    )
                    .border(
                        width = 0.5.dp,
                        color = Color.White.copy(alpha = 0.15f),
                        shape = cardShape
                    )
                    .liquid(liquidState) {
                        edge = if (glassSettings.applyToCards) glassSettings.edge else 0.01f
                        shape = cardShape
                        if (glassSettings.applyToCards) {
                            frost = glassSettings.frost
                            curve = glassSettings.curve
                            refraction = glassSettings.refraction
                        }
                        tint = Color.White.copy(alpha = 0.03f)
                    }
                    .clip(cardShape)
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = message,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = actionLabel,
                    color = Color(0xFF4DA8FF),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable {
                            onAction()
                            onDismiss()
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
