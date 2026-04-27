// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.fletchmckee.liquid.samples.app.theme.KlikAlert
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkMuted
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkPrimary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkSecondary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkTertiary
import io.github.fletchmckee.liquid.samples.app.theme.KlikLineHairline
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperApp
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperCard
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperSoft
import io.github.fletchmckee.liquid.samples.app.ui.icons.Copy
import io.github.fletchmckee.liquid.samples.app.ui.icons.CustomIcons
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1R
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1Sp
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1Type
import io.github.fletchmckee.liquid.samples.app.ui.klikone.k1Clickable

// ─── Full-screen loading ──────────────────────────────────────────────────

@Composable
fun LoadingScreen(
  modifier: Modifier = Modifier,
  message: String = "Loading…",
) {
  Box(
    modifier = modifier.fillMaxSize().background(KlikPaperApp),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      CircularProgressIndicator(
        modifier = Modifier.size(28.dp),
        color = KlikInkPrimary,
        strokeWidth = 2.dp,
      )
      Spacer(Modifier.height(K1Sp.md))
      Text(text = message, style = K1Type.bodySm.copy(color = KlikInkTertiary))
    }
  }
}

@Composable
fun LoadingIndicator(
  modifier: Modifier = Modifier,
  size: Int = 20,
) {
  Box(modifier = modifier, contentAlignment = Alignment.Center) {
    CircularProgressIndicator(
      modifier = Modifier.size(size.dp),
      color = KlikInkPrimary,
      strokeWidth = 2.dp,
    )
  }
}

// ─── Full-screen error ────────────────────────────────────────────────────

@Composable
fun ErrorScreen(
  error: String,
  onRetry: (() -> Unit)? = null,
  onReport: (() -> Unit)? = null,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier.fillMaxSize().background(KlikPaperApp),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier.padding(K1Sp.xxxl),
    ) {
      K1AlertGlyph()
      Spacer(Modifier.height(K1Sp.md))
      Text("Something went wrong", style = K1Type.h3)
      Spacer(Modifier.height(K1Sp.xs))
      Text(
        text = error,
        style = K1Type.bodySm.copy(color = KlikInkSecondary),
        textAlign = TextAlign.Center,
      )
      if (onRetry != null) {
        Spacer(Modifier.height(K1Sp.xl))
        K1PillButton(label = "Try again", onClick = onRetry, primary = true)
      }
      if (onReport != null) {
        Spacer(Modifier.height(K1Sp.s))
        K1TextLink(label = "Report issue", onClick = onReport, tint = KlikAlert)
      }
    }
  }
}

// ─── Inline error ─────────────────────────────────────────────────────────

@Composable
fun ErrorMessage(
  error: String,
  onRetry: (() -> Unit)? = null,
  onDismiss: (() -> Unit)? = null,
  onReport: (() -> Unit)? = null,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(K1R.inline)
      .background(KlikPaperSoft)
      .border(1.dp, KlikLineHairline, K1R.inline)
      .padding(K1Sp.md),
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        Modifier
          .size(6.dp)
          .background(KlikAlert, CircleShape),
      )
      Spacer(Modifier.width(K1Sp.s))
      Text(text = error, style = K1Type.bodySm, modifier = Modifier.weight(1f))
    }
    if (onRetry != null || onDismiss != null || onReport != null) {
      Spacer(Modifier.height(K1Sp.s))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
      ) {
        if (onReport != null) {
          K1TextLink(label = "Report", onClick = onReport, tint = KlikAlert)
          Spacer(Modifier.width(K1Sp.md))
        }
        if (onRetry != null) {
          K1TextLink(label = "Retry", onClick = onRetry, tint = KlikInkPrimary)
          Spacer(Modifier.width(K1Sp.md))
        }
        if (onDismiss != null) {
          K1TextLink(label = "Dismiss", onClick = onDismiss, tint = KlikInkTertiary)
        }
      }
    }
  }
}

// ─── Modal error popup ────────────────────────────────────────────────────

@Composable
fun ErrorPopup(
  isVisible: Boolean,
  title: String = "Error",
  message: String,
  onDismiss: () -> Unit,
  onRetry: (() -> Unit)? = null,
  onReport: (() -> Unit)? = null,
  modifier: Modifier = Modifier,
) {
  AnimatedVisibility(visible = isVisible, enter = fadeIn(), exit = fadeOut()) {
    Box(
      modifier = modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.42f))
        .pointerInput(Unit) { detectTapGestures(onTap = { onDismiss() }) },
      contentAlignment = Alignment.Center,
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth(0.9f)
          .clip(K1R.card)
          .background(KlikPaperCard)
          .border(1.dp, KlikLineHairline, K1R.card)
          .pointerInput(Unit) { detectTapGestures { } }
          .padding(K1Sp.lg),
      ) {
        // Header: alert glyph + title + copy
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          K1AlertGlyph(size = 18)
          Spacer(Modifier.width(K1Sp.s))
          Text(
            text = title,
            style = K1Type.cardTitle,
            modifier = Modifier.weight(1f),
          )
          CopyToClipboardButton(message = message)
        }

        Spacer(Modifier.height(K1Sp.m))

        // Message block — scrollable so long errors are never truncated.
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 96.dp, max = 220.dp)
            .clip(K1R.inline)
            .background(KlikPaperSoft)
            .border(1.dp, KlikLineHairline, K1R.inline)
            .padding(K1Sp.m),
        ) {
          Text(
            text = message,
            style = K1Type.bodySm.copy(color = KlikInkSecondary),
            modifier = Modifier
              .fillMaxSize()
              .verticalScroll(rememberScrollState()),
          )
        }

        Spacer(Modifier.height(K1Sp.m))

        // Actions row — text links on the left (report), primary pill on the right.
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          if (onReport != null) {
            K1TextLink(label = "Report", onClick = onReport, tint = KlikAlert)
          }
          Spacer(Modifier.weight(1f))
          K1TextLink(label = "Dismiss", onClick = onDismiss, tint = KlikInkTertiary)
          if (onRetry != null) {
            Spacer(Modifier.width(K1Sp.md))
            K1PillButton(label = "Retry", onClick = onRetry, primary = true)
          }
        }
      }
    }
  }
}

@Composable
private fun CopyToClipboardButton(message: String) {
  val clipboard = LocalClipboardManager.current
  var copied by remember { mutableStateOf(false) }

  Box(
    modifier = Modifier
      .clip(K1R.chip)
      .background(KlikPaperSoft)
      .border(1.dp, KlikLineHairline, K1R.chip)
      .k1Clickable {
        clipboard.setText(AnnotatedString(message))
        copied = true
      }
      .padding(horizontal = K1Sp.s, vertical = K1Sp.xxs),
    contentAlignment = Alignment.Center,
  ) {
    if (copied) {
      Text("Copied", style = K1Type.meta.copy(color = KlikInkPrimary))
    } else {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = CustomIcons.Copy,
          contentDescription = "Copy error",
          tint = KlikInkSecondary,
          modifier = Modifier.size(12.dp),
        )
        Spacer(Modifier.width(K1Sp.xxs))
        Text("Copy", style = K1Type.meta.copy(color = KlikInkSecondary))
      }
    }
  }

  if (copied) {
    LaunchedEffect(Unit) {
      kotlinx.coroutines.delay(1500)
      copied = false
    }
  }
}

// ─── Empty state ──────────────────────────────────────────────────────────

@Composable
fun EmptyState(
  title: String,
  message: String,
  actionLabel: String? = null,
  onAction: (() -> Unit)? = null,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier.fillMaxSize().background(KlikPaperApp),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier.padding(K1Sp.xxxl),
    ) {
      Text(text = title, style = K1Type.h3)
      Spacer(Modifier.height(K1Sp.xs))
      Text(
        text = message,
        style = K1Type.bodySm.copy(color = KlikInkSecondary),
        textAlign = TextAlign.Center,
      )
      if (actionLabel != null && onAction != null) {
        Spacer(Modifier.height(K1Sp.xl))
        K1PillButton(label = actionLabel, onClick = onAction, primary = true)
      }
    }
  }
}

// ─── Overlay spinner ──────────────────────────────────────────────────────

@Composable
fun LoadingOverlay(
  isLoading: Boolean,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  Box(modifier = modifier) {
    content()
    AnimatedVisibility(visible = isLoading, enter = fadeIn(), exit = fadeOut()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black.copy(alpha = 0.28f)),
        contentAlignment = Alignment.Center,
      ) {
        Box(
          modifier = Modifier
            .clip(K1R.card)
            .background(KlikPaperCard)
            .border(1.dp, KlikLineHairline, K1R.card)
            .padding(K1Sp.xl),
        ) {
          CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = KlikInkPrimary,
            strokeWidth = 2.dp,
          )
        }
      }
    }
  }
}

// ─── Content state wrapper ────────────────────────────────────────────────

@Composable
fun <T> ContentState(
  isLoading: Boolean,
  error: String?,
  data: T?,
  onRetry: (() -> Unit)? = null,
  onDismissError: (() -> Unit)? = null,
  onReport: (() -> Unit)? = null,
  emptyContent: @Composable () -> Unit = { EmptyState("No data", "There's nothing to show here.") },
  content: @Composable (T) -> Unit,
) {
  when {
    isLoading && data == null -> LoadingScreen()

    error != null && data == null -> ErrorScreen(error = error, onRetry = onRetry, onReport = onReport)

    data != null -> {
      Column {
        if (error != null) {
          ErrorMessage(
            error = error,
            onRetry = onRetry,
            onDismiss = onDismissError,
            onReport = onReport,
            modifier = Modifier.padding(horizontal = K1Sp.lg, vertical = K1Sp.s),
          )
        }
        content(data)
      }
    }

    else -> emptyContent()
  }
}

// ─── Shimmer helpers (unchanged visually, K1-safe palette) ────────────────

@Composable
fun ShimmerBar(
  modifier: Modifier = Modifier,
  color: Color = KlikInkMuted,
  delayMillis: Int = 0,
) {
  val transition = rememberInfiniteTransition(label = "shimmer")
  val alpha by transition.animateFloat(
    initialValue = 0.15f,
    targetValue = 0.35f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1000, delayMillis = delayMillis),
      repeatMode = RepeatMode.Reverse,
    ),
    label = "shimmerAlpha",
  )
  Box(modifier = modifier.background(color.copy(alpha = alpha), RoundedCornerShape(6.dp)))
}

@Composable
fun ShimmerTextBlock(
  lines: Int = 3,
  modifier: Modifier = Modifier,
  color: Color = KlikInkMuted,
) {
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
    repeat(lines) { index ->
      ShimmerBar(
        modifier = Modifier
          .fillMaxWidth(if (index == lines - 1) 0.6f else 1f)
          .height(12.dp),
        color = color,
        delayMillis = index * 150,
      )
    }
  }
}

@Composable
fun ShimmerCardContent(
  modifier: Modifier = Modifier,
  color: Color = KlikInkMuted,
) {
  Column(
    modifier = modifier.padding(K1Sp.lg),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    ShimmerBar(
      modifier = Modifier.width(120.dp).height(14.dp),
      color = color,
      delayMillis = 0,
    )
    ShimmerTextBlock(lines = 3, color = color)
  }
}

// ─── K1 primitives local to this file ─────────────────────────────────────

/** Hairline-bordered alert glyph — warm orange ring, single line. Editorial, not Material. */
@Composable
private fun K1AlertGlyph(size: Int = 40) {
  Box(
    modifier = Modifier
      .size(size.dp)
      .clip(CircleShape)
      .border(1.dp, KlikAlert, CircleShape)
      .background(KlikPaperSoft),
    contentAlignment = Alignment.Center,
  ) {
    Text("!", style = K1Type.h3.copy(color = KlikAlert))
  }
}

/** Primary = dark ink pill on paper; secondary = paper pill with hairline. */
@Composable
private fun K1PillButton(
  label: String,
  onClick: () -> Unit,
  primary: Boolean,
) {
  val bg = if (primary) KlikInkPrimary else KlikPaperCard
  val fg = if (primary) KlikPaperCard else KlikInkPrimary
  Box(
    modifier = Modifier
      .clip(K1R.pill)
      .background(bg)
      .border(1.dp, if (primary) KlikInkPrimary else KlikLineHairline, K1R.pill)
      .k1Clickable(onClick = onClick)
      .padding(horizontal = K1Sp.xl, vertical = K1Sp.s),
    contentAlignment = Alignment.Center,
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      if (label.equals("Retry", ignoreCase = true) || label.equals("Try again", ignoreCase = true)) {
        Icon(
          imageVector = Icons.Default.Refresh,
          contentDescription = null,
          tint = fg,
          modifier = Modifier.size(14.dp),
        )
        Spacer(Modifier.width(K1Sp.xs))
      }
      Text(text = label, style = K1Type.bodyMd.copy(color = fg))
    }
  }
}

@Composable
private fun K1TextLink(label: String, onClick: () -> Unit, tint: Color) {
  Box(
    modifier = Modifier
      .k1Clickable(onClick = onClick)
      .padding(horizontal = K1Sp.s, vertical = K1Sp.xs),
  ) {
    Text(text = label, style = K1Type.bodyMd.copy(color = tint))
  }
}
