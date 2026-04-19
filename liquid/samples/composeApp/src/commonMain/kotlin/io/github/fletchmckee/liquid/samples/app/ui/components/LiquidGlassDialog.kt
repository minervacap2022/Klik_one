package io.github.fletchmckee.liquid.samples.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.fletchmckee.liquid.liquid
import io.github.fletchmckee.liquid.rememberLiquidState
import io.github.fletchmckee.liquid.samples.app.theme.KlikPrimary
import io.github.fletchmckee.liquid.samples.app.theme.LocalLiquidGlassSettings

/**
 * Liquid-glass styled dialog that replaces Material AlertDialog throughout the app.
 *
 * Uses the same liquid effect, white frosted background, and KlikPrimary accents
 * to match the rest of the UI.
 */
@Composable
fun LiquidGlassDialog(
    onDismissRequest: () -> Unit,
    title: String,
    message: String,
    confirmText: String = "OK",
    onConfirm: () -> Unit,
    dismissText: String? = null,
    onDismissAction: (() -> Unit)? = null,
    confirmColor: Color = KlikPrimary,
    isDestructive: Boolean = false,
    confirmEnabled: Boolean = true
) {
    LiquidGlassDialogScaffold(
        onDismissRequest = onDismissRequest,
        title = title,
        confirmText = confirmText,
        onConfirm = onConfirm,
        dismissText = dismissText,
        onDismissAction = onDismissAction,
        confirmColor = confirmColor,
        isDestructive = isDestructive,
        confirmEnabled = confirmEnabled
    ) {
        Text(
            text = message,
            fontSize = 14.sp,
            color = Color(0xFF555555),
            lineHeight = 20.sp
        )
    }
}

/**
 * Liquid-glass dialog scaffold with a custom content slot.
 *
 * Use this for dialogs that need form fields, scrollable content, or other
 * custom UI inside the dialog body.
 */
@Composable
fun LiquidGlassDialogScaffold(
    onDismissRequest: () -> Unit,
    title: String,
    confirmText: String = "OK",
    onConfirm: () -> Unit,
    dismissText: String? = null,
    onDismissAction: (() -> Unit)? = null,
    confirmColor: Color = KlikPrimary,
    isDestructive: Boolean = false,
    confirmEnabled: Boolean = true,
    icon: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { onDismissRequest() })
                },
            contentAlignment = Alignment.Center
        ) {
            val liquidState = rememberLiquidState()
            val glassSettings = LocalLiquidGlassSettings.current
            val cardShape = RoundedCornerShape(24.dp)

            Column(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .background(Color.White.copy(alpha = 0.96f), cardShape)
                    .border(
                        width = 0.5.dp,
                        color = Color.Black.copy(alpha = 0.08f),
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
                        tint = Color.Black.copy(alpha = 0.02f)
                    }
                    .clip(cardShape)
                    .pointerInput(Unit) {
                        // Consume taps inside the card
                        detectTapGestures { }
                    }
                    .padding(24.dp),
                horizontalAlignment = Alignment.Start
            ) {
                if (icon != null) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        icon()
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Spacer(modifier = Modifier.height(12.dp))
                content()
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (dismissText != null) {
                        Text(
                            text = dismissText,
                            color = Color(0xFF888888),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { (onDismissAction ?: onDismissRequest)() }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = confirmText,
                        color = if (!confirmEnabled) Color(0xFFBBBBBB)
                                else if (isDestructive) Color(0xFFE53935)
                                else confirmColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .let { if (confirmEnabled) it.clickable { onConfirm() } else it }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}
