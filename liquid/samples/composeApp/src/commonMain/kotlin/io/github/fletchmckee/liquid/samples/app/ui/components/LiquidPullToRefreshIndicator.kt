package io.github.fletchmckee.liquid.samples.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.fletchmckee.liquid.liquid
import io.github.fletchmckee.liquid.rememberLiquidState
import io.github.fletchmckee.liquid.samples.app.theme.KlikPrimary
import io.github.fletchmckee.liquid.samples.app.theme.LocalLiquidGlassSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiquidPullToRefreshIndicator(
    state: PullToRefreshState,
    isRefreshing: Boolean,
    modifier: Modifier = Modifier
) {
    val glassSettings = LocalLiquidGlassSettings.current
    val liquidState = rememberLiquidState()
    
    // Scale animation: appear when pulling, disappear when idle
    val scaleFraction = if (isRefreshing) 1f else state.distanceFraction.coerceIn(0f, 1f)
    val scale by animateFloatAsState(
        targetValue = scaleFraction,
        animationSpec = tween(durationMillis = 200)
    )

    // Rotation animation: spin when refreshing, follow pull distance otherwise
    val rotation = if (isRefreshing) {
        val infiniteTransition = rememberInfiniteTransition(label = "refesh_spin")
        val angle by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable<Float>(
                animation = tween(1000, easing = LinearEasing)
            ),
            label = "spin_angle"
        )
        angle
    } else {
        state.distanceFraction * 180f
    }

    // Only show if there's some pull progress or refreshing
    if (scaleFraction > 0.01f) {
        Box(
            modifier = modifier
                .padding(top = 56.dp) // Push down from status bar/header
                .size(48.dp)
                .scale(scale)
                .shadow(elevation = 8.dp, shape = CircleShape, clip = false)
                .background(Color.White.copy(alpha = 0.9f), CircleShape)
                .border(0.5.dp, Color.Black.copy(alpha = 0.05f), CircleShape)
                .liquid(liquidState) {
                    edge = if (glassSettings.applyToCards) glassSettings.edge else 0.02f
                    shape = CircleShape
                    if (glassSettings.applyToCards) {
                        frost = glassSettings.frost
                        curve = glassSettings.curve
                        refraction = glassSettings.refraction
                    }
                    tint = KlikPrimary.copy(alpha = 0.05f)
                }
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refreshing",
                modifier = Modifier
                    .size(24.dp)
                    .rotate(rotation),
                tint = KlikPrimary
            )
        }
    }
}
