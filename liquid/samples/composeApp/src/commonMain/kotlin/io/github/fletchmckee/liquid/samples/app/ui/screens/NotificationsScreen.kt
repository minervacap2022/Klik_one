// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.fletchmckee.liquid.samples.app.data.source.remote.NotificationDto
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkPrimary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkSecondary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkTertiary
import io.github.fletchmckee.liquid.samples.app.theme.KlikLineHairline
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperApp
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperCard
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperSoft
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1Eyebrow
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1PullRefreshIndicator
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1Sp
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1Type
import io.github.fletchmckee.liquid.samples.app.ui.klikone.k1Clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
  notifications: List<NotificationDto>,
  isLoading: Boolean,
  isRefreshing: Boolean,
  onRefresh: () -> Unit,
  onMarkRead: (Int) -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val pullRefreshState = rememberPullToRefreshState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(KlikPaperApp)
      .statusBarsPadding()
      .navigationBarsPadding(),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        "Back",
        style = K1Type.metaSm.copy(color = KlikInkSecondary),
        modifier = Modifier.k1Clickable(onClick = onBack).padding(end = K1Sp.m),
      )
      Box(Modifier.weight(1f))
    }

    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
      K1Eyebrow("Klik")
      Spacer(Modifier.height(K1Sp.m))
      Text("What Klik noticed.", style = K1Type.display)
      Spacer(Modifier.height(K1Sp.m))
      Text(
        "Recent nudges, meetings, and signals from your work.",
        style = K1Type.bodySm.copy(color = KlikInkSecondary),
      )
    }

    Spacer(Modifier.height(K1Sp.xl))

    when {
      isLoading && notifications.isEmpty() -> {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Text(
            "Loading…",
            style = K1Type.bodySm.copy(color = KlikInkTertiary),
          )
        }
      }

      notifications.isEmpty() -> {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 24.dp),
          ) {
            K1Eyebrow("All clear")
            Text(
              "Nothing to report.",
              style = K1Type.h2,
            )
            Text(
              "Klik will drop fresh nudges here as they surface.",
              style = K1Type.bodySm.copy(color = KlikInkSecondary),
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
            K1PullRefreshIndicator(
              state = pullRefreshState,
              isRefreshing = isRefreshing,
              modifier = Modifier.align(Alignment.TopCenter),
            )
          },
        ) {
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            items(items = notifications, key = { it.id }) { notification ->
              K1NotificationCard(
                notification = notification,
                onClick = {
                  if (!notification.isRead) onMarkRead(notification.id)
                },
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun K1NotificationCard(
  notification: NotificationDto,
  onClick: () -> Unit,
) {
  val shape = RoundedCornerShape(14.dp)
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(shape)
      .background(if (notification.isRead) KlikPaperCard else KlikPaperSoft)
      .border(0.75.dp, KlikLineHairline, shape)
      .k1Clickable(onClick = onClick)
      .padding(16.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = Alignment.Top,
  ) {
    // Eyebrow disc — the K1 "initial" glyph instead of Material icons.
    val glyph = eventTypeEyebrow(notification.eventType)
    Box(
      Modifier
        .size(32.dp)
        .clip(CircleShape)
        .background(KlikPaperCard)
        .border(0.75.dp, KlikLineHairline, CircleShape),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        glyph,
        style = K1Type.metaSm.copy(color = KlikInkPrimary, fontWeight = FontWeight.Medium),
      )
    }

    Column(Modifier.weight(1f)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
      ) {
        Text(
          text = notification.title.orEmpty(),
          style = K1Type.bodyMd.copy(
            color = KlikInkPrimary,
            fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Medium,
          ),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f),
        )
        if (!notification.isRead) {
          Spacer(Modifier.size(8.dp))
          Box(
            Modifier
              .padding(top = 6.dp)
              .size(6.dp)
              .background(KlikInkPrimary, CircleShape),
          )
        }
      }

      if (notification.body != null) {
        Spacer(Modifier.height(4.dp))
        Text(
          text = notification.body,
          style = K1Type.bodySm.copy(color = KlikInkSecondary),
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )
      }

      Spacer(Modifier.height(6.dp))
      Text(
        text = formatNotificationTime(notification.createdAt),
        style = K1Type.metaSm.copy(color = KlikInkTertiary),
      )
    }
  }
}

/** Single-glyph eyebrow per event type — pure text, no Material icons. */
private fun eventTypeEyebrow(eventType: String): String = when (eventType) {
  "meeting_generated" -> "M"
  "sensitive_task_created" -> "!"
  "daily_task_completed" -> "✓"
  "daily_task_cannot_do" -> "✕"
  else -> "·"
}

private fun formatNotificationTime(createdAt: String): String {
  if (createdAt.isBlank()) return ""
  return try {
    val datePart = createdAt.substringBefore("T")
    val parts = datePart.split("-")
    if (parts.size == 3) {
      val month = when (parts[1]) {
        "01" -> "Jan"
        "02" -> "Feb"
        "03" -> "Mar"
        "04" -> "Apr"
        "05" -> "May"
        "06" -> "Jun"
        "07" -> "Jul"
        "08" -> "Aug"
        "09" -> "Sep"
        "10" -> "Oct"
        "11" -> "Nov"
        "12" -> "Dec"
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
