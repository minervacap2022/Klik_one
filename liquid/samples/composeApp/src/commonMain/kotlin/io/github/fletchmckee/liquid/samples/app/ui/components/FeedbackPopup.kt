package io.github.fletchmckee.liquid.samples.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkPrimary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkSecondary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkTertiary
import io.github.fletchmckee.liquid.samples.app.theme.KlikLineHairline
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperCard
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperSoft
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1ButtonPrimary
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1Eyebrow
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1Sp
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1Type
import io.github.fletchmckee.liquid.samples.app.ui.klikone.k1Clickable

/**
 * Feedback payload sent to the backend (entity-feedback service).
 * Shape is preserved — only the dialog chrome is rewritten in K1.
 */
data class FeedbackData(
    val originalText: String,
    val correction: String? = null,
    val isMarkedWrong: Boolean = false,
    val voiceprintId: String? = null
)

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
    var correction by remember(isVisible) { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(isVisible) {
        if (isVisible) {
            // FocusRequester is bound to the TextField inside AnimatedVisibility
            // (line 186). When isVisible flips true the AnimatedVisibility hasn't
            // composed its content yet — calling requestFocus() right away would
            // hit IllegalStateException("FocusRequester is not initialized").
            // Delay one animation frame so the target enters composition first.
            kotlinx.coroutines.delay(220)
            focusRequester.requestFocus()
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        modifier = modifier,
        enter = fadeIn(tween(200)) + scaleIn(tween(200), initialScale = 0.96f),
        exit = fadeOut(tween(150)) + scaleOut(tween(150), targetScale = 0.96f),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(overlayShape)
                .background(Color.Black.copy(alpha = 0.32f))
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        keyboardController?.hide()
                        onDismiss()
                    })
                },
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 380.dp)
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(KlikPaperCard)
                    .border(0.75.dp, KlikLineHairline, RoundedCornerShape(20.dp))
                    .pointerInput(Unit) { detectTapGestures { /* swallow */ } }
                    .padding(horizontal = 24.dp, vertical = 24.dp),
            ) {
                K1Eyebrow("Klik · Correction")
                Spacer(Modifier.height(K1Sp.m))
                Text("Fix what Klik heard.", style = K1Type.h2)
                Spacer(Modifier.height(K1Sp.s))
                Text(
                    "Klik heard this. Tell us what was actually said so future sessions stay sharp.",
                    style = K1Type.bodySm.copy(color = KlikInkSecondary),
                )

                Spacer(Modifier.height(K1Sp.lg))

                // Original (quoted) — read-only paper block.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(KlikPaperSoft)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    Text(
                        "\u201C$originalText\u201D",
                        style = K1Type.bodySm.copy(color = KlikInkPrimary),
                    )
                }

                Spacer(Modifier.height(K1Sp.m))

                // Correction input — hairline paper field.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(KlikPaperCard)
                        .border(0.75.dp, KlikLineHairline, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    if (correction.isEmpty()) {
                        Text(
                            "What was actually said?",
                            style = K1Type.bodySm.copy(color = KlikInkTertiary),
                        )
                    }
                    BasicTextField(
                        value = correction,
                        onValueChange = { correction = it },
                        textStyle = K1Type.bodySm.copy(color = KlikInkPrimary),
                        cursorBrush = SolidColor(KlikInkPrimary),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Send,
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (correction.isNotBlank()) {
                                    keyboardController?.hide()
                                    onSubmitFeedback(
                                        FeedbackData(
                                            originalText = originalText,
                                            correction = correction.trim(),
                                            isMarkedWrong = true,
                                            voiceprintId = voiceprintId,
                                        )
                                    )
                                }
                            },
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                    )
                }

                Spacer(Modifier.height(K1Sp.lg))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    // Secondary: just mark wrong without a correction text.
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(KlikPaperSoft)
                            .k1Clickable {
                                keyboardController?.hide()
                                onSubmitFeedback(
                                    FeedbackData(
                                        originalText = originalText,
                                        correction = null,
                                        isMarkedWrong = true,
                                        voiceprintId = voiceprintId,
                                    )
                                )
                            }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Mark wrong", style = K1Type.bodyMd.copy(color = KlikInkPrimary))
                    }

                    // Primary: submit with the correction.
                    Box(Modifier.weight(1f)) {
                        K1ButtonPrimary(
                            label = "Send",
                            enabled = correction.isNotBlank(),
                            onClick = {
                                keyboardController?.hide()
                                onSubmitFeedback(
                                    FeedbackData(
                                        originalText = originalText,
                                        correction = correction.trim(),
                                        isMarkedWrong = true,
                                        voiceprintId = voiceprintId,
                                    )
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                Spacer(Modifier.height(K1Sp.s))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .k1Clickable {
                            keyboardController?.hide()
                            onDismiss()
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Cancel",
                        style = K1Type.metaSm.copy(color = KlikInkTertiary),
                    )
                }
            }
        }
    }
}
