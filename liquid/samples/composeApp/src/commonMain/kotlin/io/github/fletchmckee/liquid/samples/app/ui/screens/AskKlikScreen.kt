package io.github.fletchmckee.liquid.samples.app.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import io.github.fletchmckee.liquid.samples.app.ui.components.AiGeneratedBadge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import io.github.fletchmckee.liquid.samples.app.ui.icons.CustomIcons
import io.github.fletchmckee.liquid.samples.app.ui.icons.Mic
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.fletchmckee.liquid.LiquidState
import io.github.fletchmckee.liquid.liquid
import io.github.fletchmckee.liquid.rememberLiquidState
import io.github.fletchmckee.liquid.samples.app.core.rememberViewModel
import io.github.fletchmckee.liquid.samples.app.domain.entity.ChatMessage
import io.github.fletchmckee.liquid.samples.app.domain.entity.ChatSource
import io.github.fletchmckee.liquid.samples.app.domain.entity.ChatSourceType
import io.github.fletchmckee.liquid.samples.app.domain.entity.SuggestedQuestion
import io.github.fletchmckee.liquid.samples.app.presentation.askklik.AskKlikEvent
import io.github.fletchmckee.liquid.samples.app.presentation.askklik.AskKlikViewModel
import io.github.fletchmckee.liquid.samples.app.theme.KlikPrimary
import io.github.fletchmckee.liquid.samples.app.theme.KlikBlack
import io.github.fletchmckee.liquid.samples.app.theme.LocalLiquidGlassSettings
import io.github.fletchmckee.liquid.samples.app.ui.components.MarkdownText
import io.github.fletchmckee.liquid.samples.app.platform.VoiceRecorderService
import io.github.fletchmckee.liquid.samples.app.data.network.AliyunASRService
import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import liquid_root.samples.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AskKlikScreen(
    viewModel: AskKlikViewModel = rememberViewModel { AskKlikViewModel() },
    onDismiss: () -> Unit,
    onNavigateToSource: (ChatSource) -> Unit = {},
    hasRecordingConsent: Boolean = true,
    onRequestRecordingConsent: () -> Unit = {}
) {
    val liquidState = rememberLiquidState()
    val glassSettings = LocalLiquidGlassSettings.current
    val uiState by viewModel.state.collectAsState()
    val events by viewModel.events.collectAsState()
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Voice recording state
    val isRecording by VoiceRecorderService.isRecording.collectAsState()
    var isTranscribing by remember { mutableStateOf(false) }
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    // Handle one-time events
    LaunchedEffect(events) {
        when (val event = events) {
            is AskKlikEvent.ShowError -> {
                errorMessage = event.message
                viewModel.consumeEvent()
            }
            is AskKlikEvent.MessageSent -> {
                errorMessage = null
                viewModel.consumeEvent()
            }
            is AskKlikEvent.ResponseReceived -> {
                errorMessage = null
                viewModel.consumeEvent()
            }
            is AskKlikEvent.FeedbackSubmitted -> {
                viewModel.consumeEvent()
            }
            is AskKlikEvent.HistoryCleared -> {
                viewModel.consumeEvent()
            }
            is AskKlikEvent.ActionTriggered -> {
                viewModel.consumeEvent()
            }
            is AskKlikEvent.CopyToClipboard -> {
                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(event.text))
                viewModel.consumeEvent()
            }
            null -> { /* No event */ }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 136.dp)
            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .background(Color.White.copy(alpha = 0.95f))
            .border(BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.12f)), RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .liquid(liquidState) {
                edge = glassSettings.edge
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                frost = glassSettings.frost
                curve = glassSettings.curve
                refraction = glassSettings.refraction
                tint = Color.White.copy(alpha = 0.1f)
            }
    ) {
        Column(Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(Res.string.ask_klik_header),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, stringResource(Res.string.nav_close))
                }
            }

            // Chat List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                reverseLayout = true
            ) {
                // Show loading indicator
                if (uiState.isLoading) {
                    item {
                        val infiniteTransition = rememberInfiniteTransition(label = "loading")
                        val loadingAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.3f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(800),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "loadingAlpha"
                        )

                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.95f)
                                        .height(14.dp)
                                        .background(KlikPrimary.copy(alpha = loadingAlpha * 0.2f), RoundedCornerShape(4.dp))
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.85f)
                                        .height(14.dp)
                                        .background(KlikPrimary.copy(alpha = loadingAlpha * 0.15f), RoundedCornerShape(4.dp))
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.7f)
                                        .height(14.dp)
                                        .background(KlikPrimary.copy(alpha = loadingAlpha * 0.1f), RoundedCornerShape(4.dp))
                                )
                            }
                        }
                    }
                }

                // Show suggested questions when no messages
                if (uiState.messages.isEmpty() && !uiState.isLoading) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                stringResource(Res.string.ask_klik_how_can_i_help),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(Modifier.height(16.dp))

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                uiState.suggestedQuestions.forEach { question ->
                                    SuggestedQuestionChip(
                                        liquidState = liquidState,
                                        question = question,
                                        onClick = { viewModel.sendSuggestedQuestion(question) }
                                    )
                                }
                            }
                        }
                    }
                }

                // Show messages
                items(uiState.messages.reversed()) { msg ->
                    ChatBubble(
                        liquidState = liquidState,
                        message = msg,
                        onSourceClick = onNavigateToSource
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            // Sending indicator
            if (uiState.isSending) {
                val infiniteTransition = rememberInfiniteTransition(label = "thinking")
                val loadingAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "loadingAlpha"
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        stringResource(Res.string.ask_klik_thinking),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .height(12.dp)
                                .background(KlikPrimary.copy(alpha = loadingAlpha * 0.2f), RoundedCornerShape(4.dp))
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.55f)
                                .height(12.dp)
                                .background(KlikPrimary.copy(alpha = loadingAlpha * 0.15f), RoundedCornerShape(4.dp))
                        )
                    }
                }
            }

            // Error message
            if (errorMessage != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .background(Color(0xFFFDEDED), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        errorMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFB71C1C),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        stringResource(Res.string.retry),
                        style = MaterialTheme.typography.labelMedium,
                        color = KlikPrimary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                errorMessage = null
                                viewModel.sendMessage()
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = stringResource(Res.string.dismiss),
                        tint = Color(0xFFB71C1C).copy(alpha = 0.6f),
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { errorMessage = null }
                    )
                }
            }

            // Input Area
            val density = androidx.compose.ui.platform.LocalDensity.current
            val imeBottom = WindowInsets.ime.getBottom(density)
            val isKeyboardVisible = imeBottom > 0
            val bottomPadding = if (isKeyboardVisible) 0.dp else 110.dp

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.9f))
                    .windowInsetsPadding(WindowInsets.ime)
                    .padding(16.dp)
                    .padding(bottom = bottomPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Voice recording button
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            if (isRecording) {
                                // Stop recording and transcribe
                                KlikLogger.i("AskKlikScreen", "Stopping voice recording...")
                                isTranscribing = true
                                val audioBase64 = VoiceRecorderService.stopRecording()
                                if (audioBase64 != null) {
                                    KlikLogger.i("AskKlikScreen", "Transcribing audio...")
                                    try {
                                        val transcription = AliyunASRService.transcribe(audioBase64)
                                        viewModel.updateInput(transcription)
                                        KlikLogger.i("AskKlikScreen", "Transcription complete: $transcription")
                                    } catch (e: Exception) {
                                        errorMessage = "Failed to transcribe audio: ${e.message}"
                                        KlikLogger.e("AskKlikScreen", "Transcription failed: ${e.message}", e)
                                    }
                                } else {
                                    errorMessage = "Failed to record audio"
                                    KlikLogger.e("AskKlikScreen", "Recording failed")
                                }
                                isTranscribing = false
                            } else {
                                // Start recording — BIPA/compliance: require recording_tos consent
                                // before any audio is captured, regardless of OS permission state.
                                if (!hasRecordingConsent) {
                                    KlikLogger.i("AskKlikScreen", "Recording consent missing — routing to consent screen")
                                    onRequestRecordingConsent()
                                    return@launch
                                }
                                if (!VoiceRecorderService.hasMicrophonePermission()) {
                                    val granted = VoiceRecorderService.requestMicrophonePermission()
                                    if (!granted) {
                                        errorMessage = "Microphone permission required for voice input"
                                        return@launch
                                    }
                                }
                                KlikLogger.i("AskKlikScreen", "Starting voice recording...")
                                val started = VoiceRecorderService.startRecording()
                                if (!started) {
                                    errorMessage = "Failed to start recording"
                                    KlikLogger.e("AskKlikScreen", "Failed to start recording")
                                }
                            }
                        }
                    },
                    enabled = !uiState.isSending && !isTranscribing
                ) {
                    if (isTranscribing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = KlikPrimary
                        )
                    } else {
                        Icon(
                            CustomIcons.Mic,
                            contentDescription = if (isRecording) stringResource(Res.string.stop_recording) else stringResource(Res.string.voice_message),
                            tint = if (isRecording) Color.Red else KlikBlack
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))

                OutlinedTextField(
                    value = uiState.currentInput,
                    onValueChange = { viewModel.updateInput(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(stringResource(Res.string.ask_klik_placeholder)) },
                    shape = RoundedCornerShape(24.dp),
                    enabled = !uiState.isSending
                )
                Spacer(Modifier.width(8.dp))

                IconButton(
                    onClick = { viewModel.sendMessage() },
                    enabled = uiState.currentInput.isNotBlank() && !uiState.isSending
                ) {
                    Icon(
                        Icons.Filled.Send,
                        stringResource(Res.string.send),
                        tint = if (uiState.currentInput.isNotBlank() && !uiState.isSending)
                            KlikBlack
                        else
                            KlikBlack.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

@Composable
fun SuggestedQuestionChip(
    liquidState: LiquidState,
    question: SuggestedQuestion,
    onClick: () -> Unit
) {
    val glassSettings = LocalLiquidGlassSettings.current

    Box(
        modifier = Modifier
            .background(Color.White.copy(alpha = glassSettings.transparency), RoundedCornerShape(20.dp))
            .border(BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.12f)), RoundedCornerShape(20.dp))
            .liquid(liquidState) {
                edge = glassSettings.edge
                shape = RoundedCornerShape(20.dp)
                frost = glassSettings.frost * 0.5f
                curve = glassSettings.curve
                refraction = glassSettings.refraction * 0.5f
                tint = KlikPrimary.copy(alpha = 0.08f)
            }
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            question.text,
            style = MaterialTheme.typography.bodySmall,
            color = KlikBlack.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun ChatBubble(
    liquidState: LiquidState,
    message: ChatMessage,
    onSourceClick: (ChatSource) -> Unit = {}
) {
    val glassSettings = LocalLiquidGlassSettings.current
    val alignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
    val textColor = if (message.isUser) Color.White else MaterialTheme.colorScheme.onSurface
    val bubbleShape = RoundedCornerShape(
        topStart = 20.dp,
        topEnd = 20.dp,
        bottomStart = if (message.isUser) 20.dp else 4.dp,
        bottomEnd = if (message.isUser) 4.dp else 20.dp
    )

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .align(alignment)
                .fillMaxWidth(0.85f)
        ) {
            // Message bubble
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (message.isUser) KlikPrimary.copy(alpha = glassSettings.transparency)
                        else Color.White.copy(alpha = glassSettings.transparency),
                        bubbleShape
                    )
                    .border(BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.12f)), bubbleShape)
                    .liquid(liquidState) {
                        edge = glassSettings.edge
                        shape = bubbleShape
                        frost = glassSettings.frost
                        curve = glassSettings.curve
                        refraction = glassSettings.refraction
                        tint = if (message.isUser) KlikPrimary.copy(alpha = 0.15f) else Color.Transparent
                    }
                    .clip(bubbleShape)
                    .padding(14.dp)
            ) {
                MarkdownText(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )
            }

            // AI badge for assistant messages
            if (!message.isUser) {
                Spacer(Modifier.height(4.dp))
                AiGeneratedBadge(stringResource(Res.string.ask_klik_ai_response))
            }

            // Sources section (only for AI messages with sources)
            if (!message.isUser && message.sources.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                SourcesSection(
                    liquidState = liquidState,
                    sources = message.sources,
                    onSourceClick = onSourceClick
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SourcesSection(
    liquidState: LiquidState,
    sources: List<ChatSource>,
    onSourceClick: (ChatSource) -> Unit
) {
    val glassSettings = LocalLiquidGlassSettings.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            stringResource(Res.string.ask_klik_sources),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            sources.forEach { source ->
                SourceChip(
                    liquidState = liquidState,
                    source = source,
                    onClick = { onSourceClick(source) }
                )
            }
        }
    }
}

@Composable
fun SourceChip(
    liquidState: LiquidState,
    source: ChatSource,
    onClick: () -> Unit
) {
    val glassSettings = LocalLiquidGlassSettings.current
    val chipShape = RoundedCornerShape(12.dp)

    val (icon, chipColor) = when (source.type) {
        ChatSourceType.MEETING_SEGMENT -> "🎙️" to Color(0xFF4CAF50)
        ChatSourceType.ENTITY -> "👤" to Color(0xFF2196F3)
        ChatSourceType.SESSION_SUMMARY -> "📋" to Color(0xFF9C27B0)
        ChatSourceType.OTHER -> "📎" to Color(0xFF757575)
    }

    Box(
        modifier = Modifier
            .background(chipColor.copy(alpha = 0.1f), chipShape)
            .border(BorderStroke(0.5.dp, chipColor.copy(alpha = 0.3f)), chipShape)
            .liquid(liquidState) {
                edge = glassSettings.edge * 0.5f
                shape = chipShape
                frost = glassSettings.frost * 0.3f
                curve = glassSettings.curve
                refraction = glassSettings.refraction * 0.3f
                tint = chipColor.copy(alpha = 0.05f)
            }
            .clip(chipShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                icon,
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                source.title.take(20) + if (source.title.length > 20) "..." else "",
                style = MaterialTheme.typography.labelSmall,
                color = chipColor.copy(alpha = 0.9f),
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}
