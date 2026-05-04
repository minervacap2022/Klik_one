// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.ui.klikone

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.fletchmckee.liquid.samples.app.core.rememberViewModel
import io.github.fletchmckee.liquid.samples.app.domain.entity.ChatMessage
import io.github.fletchmckee.liquid.samples.app.domain.entity.ChatSource
import io.github.fletchmckee.liquid.samples.app.domain.entity.ChatSourceType
import io.github.fletchmckee.liquid.samples.app.presentation.askklik.AskKlikViewModel
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkMuted
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkPrimary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkTertiary
import io.github.fletchmckee.liquid.samples.app.theme.KlikLineMute
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperCard
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperChip

/**
 * Klik One Ask Klik sheet — drop-in replacement for the full-screen
 * [io.github.fletchmckee.liquid.samples.app.ui.screens.AskKlikScreen].
 */
@Composable
fun AskKlikSheet(
  viewModel: AskKlikViewModel = rememberViewModel { AskKlikViewModel() },
  onDismiss: () -> Unit,
  onNavigateToSource: (ChatSource) -> Unit = {},
  hasRecordingConsent: Boolean = true,
  onRequestRecordingConsent: () -> Unit = {},
) {
  val uiState by viewModel.state.collectAsState()

  Box(
    Modifier.fillMaxSize()
      .background(Color.Black.copy(alpha = 0.35f))
      .k1Clickable(onClick = onDismiss),
  ) {
    Box(
      Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .fillMaxHeight(0.88f)
        .clip(K1R.sheet)
        .background(KlikPaperCard)
        .k1Clickable(onClick = {}),
    ) {
      Column(
        Modifier.fillMaxWidth()
          .padding(horizontal = 20.dp)
          .padding(top = 20.dp)
          .navigationBarsPadding()
          .imePadding()
          .padding(bottom = 20.dp),
      ) {
        // Grabber.
        Box(Modifier.fillMaxWidth().padding(bottom = 20.dp), contentAlignment = Alignment.Center) {
          Box(
            Modifier.width(36.dp).height(4.dp).clip(RoundedCornerShape(2.dp))
              .background(KlikLineMute),
          )
        }

        SheetHeader(onClose = onDismiss)
        Spacer(Modifier.height(16.dp))

        val isEmpty = uiState.messages.isEmpty() && !uiState.isSending
        if (isEmpty) {
          // Compact stack: header → empty-state → chips → input, close together.
          EmptyState()
          if (uiState.suggestedQuestions.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Row(
              Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              uiState.suggestedQuestions.take(3).forEachIndexed { i, q ->
                K1Chip(
                  label = q.text,
                  onClick = { viewModel.sendSuggestedQuestion(uiState.suggestedQuestions[i]) },
                )
              }
            }
          }
          Spacer(Modifier.weight(1f))
          Spacer(Modifier.height(12.dp))
        } else {
          Column(
            Modifier.weight(1f)
              .verticalScroll(rememberScrollState()),
          ) {
            uiState.messages.forEach { msg ->
              ChatRow(msg, onNavigateToSource)
              Spacer(Modifier.height(14.dp))
            }
            if (uiState.isSending || uiState.isStreaming) {
              TypingRow(streaming = uiState.streamingResponse)
              Spacer(Modifier.height(14.dp))
            }
            uiState.error?.let { err ->
              ErrorRow(err, onRetry = {
                viewModel.clearError()
                if (uiState.currentInput.isNotBlank()) viewModel.sendMessage()
              })
              Spacer(Modifier.height(14.dp))
            }
          }
          Spacer(Modifier.height(12.dp))
        }

        InputBar(
          value = uiState.currentInput,
          sending = uiState.isSending,
          onValueChange = viewModel::updateInput,
          onSend = {
            if (uiState.currentInput.isNotBlank()) viewModel.sendMessage()
          },
        )
      }
    }
  }
}

@Composable
private fun SheetHeader(onClose: () -> Unit) {
  Row(
    Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    Box(
      Modifier.size(32.dp).clip(CircleShape).background(KlikInkPrimary),
      contentAlignment = Alignment.Center,
    ) {
      K1Waveform(
        heights = listOf(5f, 9f, 4f, 7f),
        barWidth = 1.8.dp,
        gap = 1.5.dp,
        color = KlikPaperCard,
      )
    }
    Column(Modifier.weight(1f)) {
      Text("Hi Klik", style = K1Type.bodyMd)
      Text("Your context, on tap", style = K1Type.meta)
    }
    Text(
      "Close",
      style = K1Type.bodySm.copy(color = KlikInkTertiary),
      modifier = Modifier.k1Clickable(onClick = onClose),
    )
  }
}

@Composable
private fun EmptyState() {
  Column(
    Modifier.fillMaxWidth().padding(vertical = 24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      "Ask anything about your work.",
      style = K1Type.bodySm.copy(color = KlikInkTertiary),
      textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(6.dp))
    Text(
      "Klik pulls context from your meetings, people, and projects.",
      style = K1Type.meta.copy(color = KlikInkMuted),
      textAlign = TextAlign.Center,
    )
  }
}

@Composable
private fun ChatRow(message: ChatMessage, onSourceClick: (ChatSource) -> Unit) {
  if (message.isUser) {
    UserBubble(message.text)
  } else {
    KlikBubble(message, onSourceClick)
  }
}

@Composable
private fun UserBubble(text: String) {
  Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
    Box(
      Modifier
        .fillMaxWidth(0.8f)
        .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 14.dp, bottomEnd = 4.dp))
        .background(KlikInkPrimary)
        .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
      Text(text, style = K1Type.bodySm.copy(color = KlikPaperCard))
    }
  }
}

@Composable
private fun KlikBubble(message: ChatMessage, onSourceClick: (ChatSource) -> Unit) {
  Row(
    Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(10.dp),
    verticalAlignment = Alignment.Top,
  ) {
    Box(
      Modifier.size(26.dp).clip(CircleShape).background(KlikInkPrimary),
      contentAlignment = Alignment.Center,
    ) {
      K1Waveform(
        heights = listOf(3f, 6f, 2.5f, 5f),
        barWidth = 1.4.dp,
        gap = 1.2.dp,
        color = KlikPaperCard,
      )
    }
    Column(Modifier.weight(1f)) {
      Box(
        Modifier
          .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 4.dp, bottomEnd = 14.dp))
          .background(KlikPaperChip)
          .padding(horizontal = 14.dp, vertical = 12.dp),
      ) {
        Column {
          io.github.fletchmckee.liquid.samples.app.ui.components.MarkdownText(
            text = message.text,
            style = K1Type.bodySm,
          )
          val cards = message.sources.filter { !it.content.isNullOrBlank() }
          if (cards.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              cards.forEach { CommitmentCard(it, onClick = { onSourceClick(it) }) }
            }
          }
        }
      }
      message.sources.firstOrNull { it.sessionId != null }?.let { src ->
        Spacer(Modifier.height(6.dp))
        val prefix = when (src.type) {
          ChatSourceType.MEETING_SEGMENT -> "From"
          ChatSourceType.SESSION_SUMMARY -> "From"
          ChatSourceType.ENTITY -> "Person"
          else -> "Source"
        }
        val date = src.metadata["date"] ?: src.metadata["timestamp"]
        Text(
          if (date.isNullOrBlank()) "$prefix ${src.title}" else "$prefix ${src.title} · $date",
          style = K1Type.meta,
          modifier = Modifier.k1Clickable { onSourceClick(src) },
        )
      }
    }
  }
}

@Composable
private fun CommitmentCard(source: ChatSource, onClick: () -> Unit) {
  val secondary = source.metadata["date"]
    ?: source.metadata["timestamp"]
    ?: source.metadata["subtitle"]
    ?: source.content?.take(80)
  Column(
    Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(KlikPaperCard)
      .k1Clickable(onClick = onClick).padding(horizontal = 10.dp, vertical = 8.dp),
  ) {
    Text(source.title, style = K1Type.bodySm.copy(fontWeight = FontWeight.Medium, fontSize = 12.sp))
    if (!secondary.isNullOrBlank()) {
      Spacer(Modifier.height(2.dp))
      Text(secondary, style = K1Type.metaSm)
    }
  }
}

@Composable
private fun TypingRow(streaming: String) {
  Row(
    Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(10.dp),
    verticalAlignment = Alignment.Top,
  ) {
    Box(
      Modifier.size(26.dp).clip(CircleShape).background(KlikInkPrimary),
      contentAlignment = Alignment.Center,
    ) {
      K1WaveformLive(color = KlikPaperCard, barWidth = 1.4.dp, gap = 1.2.dp)
    }
    Box(
      Modifier
        .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 4.dp, bottomEnd = 14.dp))
        .background(KlikPaperChip)
        .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
      if (streaming.isNotBlank()) {
        io.github.fletchmckee.liquid.samples.app.ui.components.MarkdownText(
          text = streaming,
          style = K1Type.bodySm,
        )
      } else {
        CircularProgressIndicator(
          strokeWidth = 1.dp,
          color = KlikInkTertiary,
          modifier = Modifier.size(14.dp),
        )
      }
    }
  }
}

@Composable
private fun ErrorRow(message: String, onRetry: () -> Unit) {
  Row(
    Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(10.dp),
    verticalAlignment = Alignment.Top,
  ) {
    Box(
      Modifier
        .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 4.dp, bottomEnd = 14.dp))
        .background(KlikPaperChip)
        .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
      Column {
        Text(
          "Klik couldn't reach its brain right now.",
          style = K1Type.bodyMd.copy(color = KlikInkPrimary),
        )
        Spacer(Modifier.height(4.dp))
        Text(
          message,
          style = K1Type.metaSm.copy(color = KlikInkTertiary),
        )
        Spacer(Modifier.height(8.dp))
        Text(
          "Retry",
          style = K1Type.bodyMd.copy(color = KlikInkPrimary),
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(KlikPaperCard)
            .k1Clickable(onClick = onRetry)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        )
      }
    }
  }
}

@Composable
private fun InputBar(value: String, sending: Boolean, onValueChange: (String) -> Unit, onSend: () -> Unit) {
  Row(
    Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(KlikPaperChip)
      .padding(horizontal = 14.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
      if (value.isBlank()) {
        Text(
          "Ask anything about your work…",
          style = K1Type.bodySm.copy(color = KlikInkMuted),
        )
      }
      BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = false,
        maxLines = 4,
        textStyle = K1Type.bodySm,
        cursorBrush = SolidColor(KlikInkPrimary),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
        keyboardActions = KeyboardActions(onSend = { onSend() }),
        modifier = Modifier.fillMaxWidth(),
      )
    }
    Box(
      Modifier.size(28.dp).clip(CircleShape)
        .background(if (value.isBlank() || sending) KlikInkMuted else KlikInkPrimary)
        .k1Clickable(enabled = !sending && value.isNotBlank(), onClick = onSend),
      contentAlignment = Alignment.Center,
    ) {
      if (sending) {
        CircularProgressIndicator(
          strokeWidth = 1.4.dp,
          color = KlikPaperCard,
          modifier = Modifier.size(14.dp),
        )
      } else {
        Text(
          "↑",
          style = TextStyle(
            color = KlikPaperCard,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
          ),
        )
      }
    }
  }
}
