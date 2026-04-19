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
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.fletchmckee.liquid.samples.app.theme.KlikBlack
import io.github.fletchmckee.liquid.samples.app.theme.KlikPrimary
import io.github.fletchmckee.liquid.liquid
import io.github.fletchmckee.liquid.rememberLiquidState
import io.github.fletchmckee.liquid.samples.app.theme.LocalLiquidGlassSettings

/**
 * Data class representing feedback from user
 */
data class FeedbackData(
    val originalText: String,
    val correction: String? = null,
    val isMarkedWrong: Boolean = false,
    val voiceprintId: String? = null
)

/**
 * Feedback popup component that appears on long-press.
 * Provides a text input for corrections and a "Wrong" button.
 *
 * @param isVisible Whether the popup is visible
 * @param originalText The original text being corrected
 * @param onDismiss Called when the popup is dismissed
 * @param onSubmitFeedback Called when user submits feedback (correction text or null if just marking wrong)
 * @param overlayShape Shape for the overlay background (should match card shape)
 */
@Composable
fun FeedbackPopup(
    isVisible: Boolean,
    originalText: String,
    voiceprintId: String? = null,
    onDismiss: () -> Unit,
    onSubmitFeedback: (FeedbackData) -> Unit,
    overlayShape: Shape = RoundedCornerShape(24.dp),
    modifier: Modifier = Modifier
) {
    var correctionText by remember(isVisible) { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val liquidState = rememberLiquidState()
    val glassSettings = LocalLiquidGlassSettings.current

    // Request focus when popup becomes visible
    LaunchedEffect(isVisible) {
        if (isVisible) {
            try {
                focusRequester.requestFocus()
            } catch (e: Exception) {
                io.github.fletchmckee.liquid.samples.app.logging.KlikLogger.w("FeedbackPopup", "Focus request failed: ${e.message}", e)
            }
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        modifier = modifier,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(150))
    ) {
        // Dim overlay with rounded corners matching card shape - NO shadow on overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(overlayShape)
                .background(Color.Black.copy(alpha = 0.4f))
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        keyboardController?.hide()
                        onDismiss()
                    })
                },
            contentAlignment = Alignment.Center
        ) {
            // Popup dialog with rounded shadow
            Column(
                modifier = Modifier
                    .widthIn(max = 320.dp)
                    .padding(horizontal = 24.dp)
                    .shadow(
                        elevation = 24.dp,
                        shape = RoundedCornerShape(20.dp),
                        clip = true
                    )
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .pointerInput(Unit) {
                        // Consume clicks to prevent dismissing when clicking on popup
                        detectTapGestures { }
                    }
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Original text preview (truncated)
                Text(
                    text = "\"${originalText.take(50)}${if (originalText.length > 50) "..." else ""}\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                // Input Row: Text Input + Wrong Button (parallel, same height)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Text Input Field
                    BasicTextField(
                        value = correctionText,
                        onValueChange = { correctionText = it },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .focusRequester(focusRequester),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = KlikBlack
                        ),
                        cursorBrush = SolidColor(KlikPrimary),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                if (correctionText.isNotBlank()) {
                                    onSubmitFeedback(
                                        FeedbackData(
                                            originalText = originalText,
                                            correction = correctionText.trim(),
                                            isMarkedWrong = false,
                                            voiceprintId = voiceprintId
                                        )
                                    )
                                }
                            }
                        ),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF5F5F5))
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (correctionText.isEmpty()) {
                                    Text(
                                        "Correction...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray.copy(alpha = 0.6f)
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )

                    // Wrong Button (parallel to input, same height)
                    Box(
                        modifier = Modifier
                            .height(48.dp)
                            .liquid(liquidState) {
                                edge = if (glassSettings.applyToCards) glassSettings.edge else 0.05f
                                shape = RoundedCornerShape(12.dp)
                                if (glassSettings.applyToCards) {
                                    frost = glassSettings.frost
                                    curve = glassSettings.curve
                                    refraction = glassSettings.refraction
                                }
                                tint = Color(0xFFFF3B30).copy(alpha = 0.1f)
                            }
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                keyboardController?.hide()
                                onSubmitFeedback(
                                    FeedbackData(
                                        originalText = originalText,
                                        correction = null,
                                        isMarkedWrong = true,
                                        voiceprintId = voiceprintId
                                    )
                                )
                            }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Filled.Warning,
                                contentDescription = null,
                                tint = Color(0xFFFF3B30),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Wrong",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFFF3B30)
                            )
                        }
                    }
                }

                // Submit Button (only visible when there's text)
                if (correctionText.isNotBlank()) {
                    Spacer(Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .liquid(liquidState) {
                                edge = if (glassSettings.applyToCards) glassSettings.edge else 0.05f
                                shape = RoundedCornerShape(12.dp)
                                if (glassSettings.applyToCards) {
                                    frost = glassSettings.frost
                                    curve = glassSettings.curve
                                    refraction = glassSettings.refraction
                                }
                                tint = KlikPrimary.copy(alpha = 0.1f)
                            }
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                keyboardController?.hide()
                                onSubmitFeedback(
                                    FeedbackData(
                                        originalText = originalText,
                                        correction = correctionText.trim(),
                                        isMarkedWrong = false,
                                        voiceprintId = voiceprintId
                                    )
                                )
                            }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Submit",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = KlikPrimary
                        )
                    }
                }
            }
        }
    }
}

/**
 * A wrapper composable that adds long-press feedback capability to its content.
 *
 * @param text The text that can be corrected
 * @param onFeedback Callback when feedback is submitted
 * @param content The content to wrap (should be clickable-compatible)
 */
@Composable
fun FeedbackEnabled(
    text: String,
    onFeedback: (FeedbackData) -> Unit = {},
    content: @Composable (onLongPress: () -> Unit) -> Unit
) {
    var showFeedbackPopup by remember { mutableStateOf(false) }

    Box {
        content { showFeedbackPopup = true }

        FeedbackPopup(
            isVisible = showFeedbackPopup,
            originalText = text,
            onDismiss = { showFeedbackPopup = false },
            onSubmitFeedback = { feedback ->
                onFeedback(feedback)
                showFeedbackPopup = false
            }
        )
    }
}

/**
 * Modifier extension to add long-press detection for feedback.
 * This is a reusable modifier that can be applied to any composable.
 */
fun Modifier.feedbackLongPress(
    onLongPress: () -> Unit
): Modifier = this.pointerInput(Unit) {
    detectTapGestures(
        onLongPress = { onLongPress() }
    )
}
