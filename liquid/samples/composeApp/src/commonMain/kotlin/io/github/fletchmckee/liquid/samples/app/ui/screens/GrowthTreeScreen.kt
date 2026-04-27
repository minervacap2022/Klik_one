// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.fletchmckee.liquid.samples.app.data.source.remote.GrowthTreeNode
import io.github.fletchmckee.liquid.samples.app.data.source.remote.GrowthTreeResponse
import io.github.fletchmckee.liquid.samples.app.data.source.remote.GrowthTreeUserSummary
import io.github.fletchmckee.liquid.samples.app.data.source.remote.RemoteDataFetcher
import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import io.github.fletchmckee.liquid.samples.app.theme.KlikCommitmentAccent
import io.github.fletchmckee.liquid.samples.app.theme.KlikDecisionAccent
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkFaint
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkMuted
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkPrimary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkTertiary
import io.github.fletchmckee.liquid.samples.app.theme.KlikLineHairline
import io.github.fletchmckee.liquid.samples.app.theme.KlikLineMute
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperApp
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperCard
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperChip
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperSoft
import io.github.fletchmckee.liquid.samples.app.theme.KlikRiskAccent
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1Sp
import io.github.fletchmckee.liquid.samples.app.ui.klikone.k1Clickable
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

// ──────────────────────────────────────────────────────────────────────────
// Editorial growth-tree visualizations.
//
// All three views (Constellation / River / Network) share one paper-and-ink
// vocabulary:
//   • paper backgrounds (KlikPaperApp / KlikPaperSoft)
//   • hairline strokes for every line and node ring
//   • filled K1 ink dots for nodes; type is encoded by a single restrained
//     accent ink (person = primary, organization = decision-gold,
//     project = commitment-teal, achievement = risk-rose, future = mute).
// No glow, no neon, no glassy gradients, no animation.
// ──────────────────────────────────────────────────────────────────────────

private const val TYPE_PERSON = "person"
private const val TYPE_ORG = "organization"
private const val TYPE_PROJECT = "project"

// Single source of truth for type → accent ink.
private fun accentForType(type: String): Color = when (type) {
  TYPE_PERSON -> KlikInkPrimary
  TYPE_ORG -> KlikDecisionAccent
  TYPE_PROJECT -> KlikCommitmentAccent
  "achievement", "level_up", "streak" -> KlikRiskAccent
  else -> KlikInkMuted
}

// ==================== Main Screen ====================

@Composable
fun GrowthTreeScreen(
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var selectedView by remember { mutableStateOf(0) }
  var treeData by remember { mutableStateOf<GrowthTreeResponse?>(null) }
  var isLoading by remember { mutableStateOf(true) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var retryKey by remember { mutableStateOf(0) }

  LaunchedEffect(retryKey) {
    isLoading = true
    errorMessage = null
    try {
      treeData = RemoteDataFetcher.fetchGrowthTree()
      KlikLogger.d(
        "GrowthTreeScreen",
        "Loaded growth tree: ${treeData?.meta?.totalNodes} nodes, ${treeData?.meta?.totalEdges} edges",
      )
    } catch (e: Exception) {
      KlikLogger.e("GrowthTreeScreen", "Failed to load growth tree: ${e.message}")
      errorMessage = e.message
    } finally {
      isLoading = false
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(KlikPaperApp),
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      GrowthTreeTopBar(
        selectedView = selectedView,
        onViewChange = { selectedView = it },
        onBack = onBack,
      )

      when {
        isLoading -> CenterMessage {
          CircularProgressIndicator(
            color = KlikInkPrimary,
            strokeWidth = 1.5.dp,
            modifier = Modifier.size(20.dp),
          )
          Spacer(Modifier.height(10.dp))
          Text("Loading growth tree…", color = KlikInkTertiary, fontSize = 12.sp)
        }

        errorMessage != null -> CenterMessage {
          Text(
            "Couldn't load",
            color = KlikInkPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
          )
          Spacer(Modifier.height(6.dp))
          Text(
            errorMessage ?: "Unknown error",
            color = KlikInkTertiary,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
          )
          Spacer(Modifier.height(14.dp))
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(7.dp))
              .background(KlikPaperChip)
              .k1Clickable { retryKey += 1 }
              .padding(horizontal = 14.dp, vertical = 8.dp),
          ) {
            Text(
              "Try again",
              color = KlikInkPrimary,
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium,
            )
          }
        }

        treeData != null -> {
          StatsBar(user = treeData!!.user)
          HairlineDivider()
          AnimatedContent(
            targetState = selectedView,
            transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(160)) },
            label = "GrowthTreeView",
          ) { view ->
            when (view) {
              0 -> ConstellationView(treeData!!)
              1 -> RiverTimelineView(treeData!!)
              2 -> ConnectedConstellationView(treeData!!)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun CenterMessage(content: @Composable () -> Unit) {
  Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.padding(32.dp),
    ) { content() }
  }
}

@Composable
private fun HairlineDivider() {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(1.dp)
      .background(KlikLineHairline),
  )
}

// ==================== Top Bar ====================

@Composable
private fun GrowthTreeTopBar(
  selectedView: Int,
  onViewChange: (Int) -> Unit,
  onBack: () -> Unit,
) {
  val viewLabels = listOf("Constellation", "River", "Network")

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(KlikPaperApp)
      .statusBarsPadding(),
  ) {
    // Top bar — chevron alone, matches DetailScaffold convention so the
    // back affordance sits at the top edge instead of getting centered
    // between the eyebrow and the title.
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
      Box(
        modifier = Modifier
          .size(32.dp)
          .clip(CircleShape)
          .k1Clickable(onClick = onBack),
        contentAlignment = Alignment.Center,
      ) {
        Canvas(Modifier.size(14.dp)) {
          val w = 1.4.dp.toPx()
          drawLine(
            color = KlikInkPrimary,
            strokeWidth = w,
            cap = StrokeCap.Round,
            start = Offset(9.dp.toPx(), 3.dp.toPx()),
            end = Offset(4.dp.toPx(), 7.dp.toPx()),
          )
          drawLine(
            color = KlikInkPrimary,
            strokeWidth = w,
            cap = StrokeCap.Round,
            start = Offset(4.dp.toPx(), 7.dp.toPx()),
            end = Offset(9.dp.toPx(), 11.dp.toPx()),
          )
        }
      }
    }

    // Hero — eyebrow + title stacked, indented to the page margin.
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
      Text(
        "GROWTH",
        color = KlikInkFaint,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.2.sp,
      )
      Spacer(Modifier.height(6.dp))
      Text(
        text = "Tree",
        color = KlikInkPrimary,
        fontSize = 28.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = (-0.8).sp,
      )
    }

    Spacer(Modifier.height(K1Sp.lg))

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(KlikPaperChip)
          .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        viewLabels.forEachIndexed { index, label ->
          val isSelected = selectedView == index
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(7.dp))
              .background(if (isSelected) KlikInkPrimary else Color.Transparent)
              .k1Clickable { onViewChange(index) }
              .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = label,
              color = if (isSelected) KlikPaperCard else KlikInkTertiary,
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium,
            )
          }
        }
      }
    }

    Spacer(Modifier.height(K1Sp.s))
  }
}

// ==================== Stats Bar ====================

@Composable
private fun StatsBar(user: GrowthTreeUserSummary) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 10.dp)
      .horizontalScroll(rememberScrollState()),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    StatChip("Lv ${user.level}", "Level")
    StatChip("${user.totalXp}", "XP")
    StatChip("${user.totalSessions}", "Sessions")
    StatChip("${user.totalPeople}", "People")
    StatChip("${user.totalOrgs}", "Orgs")
    StatChip("${user.totalProjects}", "Projects")
  }
}

@Composable
private fun StatChip(value: String, label: String) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(7.dp))
      .background(KlikPaperChip)
      .padding(horizontal = 10.dp, vertical = 6.dp),
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(value, color = KlikInkPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
      Spacer(Modifier.width(4.dp))
      Text(label, color = KlikInkTertiary, fontSize = 10.sp, fontWeight = FontWeight.Normal)
    }
  }
}

// ==================== Helpers ====================

private fun GrowthTreeResponse.nodesByType(type: String): List<GrowthTreeNode> = timelineNodes.filter { it.nodeType == type && !it.isFuture }

private fun GrowthTreeNode.metaString(key: String): String? = metadata[key]?.jsonPrimitive?.content

@Suppress("unused")
private fun GrowthTreeNode.metaFloat(key: String): Float? = metadata[key]?.jsonPrimitive?.floatOrNull

@Suppress("unused")
private fun GrowthTreeNode.metaInt(key: String): Int? = metadata[key]?.jsonPrimitive?.intOrNull

private fun String.formatShortDate(): String {
  val parts = this.split("-")
  if (parts.size < 2) return this.take(8)
  val year = parts[0].takeLast(2)
  val month = when (parts[1]) {
    "01" -> "JAN"
    "02" -> "FEB"
    "03" -> "MAR"
    "04" -> "APR"
    "05" -> "MAY"
    "06" -> "JUN"
    "07" -> "JUL"
    "08" -> "AUG"
    "09" -> "SEP"
    "10" -> "OCT"
    "11" -> "NOV"
    "12" -> "DEC"
    else -> parts[1]
  }
  return "$month '$year"
}

// Editorial typography defaults used inside Canvas drawText.
private val NodeLabelStyle = TextStyle(
  color = KlikInkPrimary,
  fontSize = 10.sp,
  fontWeight = FontWeight.Medium,
)
private val MetaLabelStyle = TextStyle(
  color = KlikInkTertiary,
  fontSize = 9.sp,
  fontWeight = FontWeight.Normal,
  letterSpacing = 0.6.sp,
)
private val EyebrowStyle = TextStyle(
  color = KlikInkFaint,
  fontSize = 9.sp,
  fontWeight = FontWeight.Medium,
  letterSpacing = 1.0.sp,
)

// ==================== VIEW A: Constellation ====================

@Composable
fun ConstellationView(data: GrowthTreeResponse) {
  val textMeasurer = rememberTextMeasurer()

  val people = data.nodesByType(TYPE_PERSON)
  val orgs = data.nodesByType(TYPE_ORG)
  val projects = data.nodesByType(TYPE_PROJECT)
  val achievements = data.achievements
  val futureNodes = data.futureProjection

  val scrollState = rememberScrollState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .background(KlikPaperApp),
  ) {
    val canvasHeight = (640 + futureNodes.size * 56).coerceAtLeast(680)

    Canvas(
      modifier = Modifier
        .fillMaxWidth()
        .height(canvasHeight.dp),
    ) {
      val w = size.width
      val h = size.height
      val dotR = 3.5.dp.toPx()
      val ringR = 5.5.dp.toPx()
      val youR = 9.dp.toPx()

      // Layout zones
      val topPad = 64f
      val todayY = h * 0.66f
      val pastH = todayY - topPad

      // Date eyebrows (left rail)
      buildConstellationTimeLabels(data).forEach { (label, yFraction) ->
        val ly = topPad + pastH * yFraction
        val style = if (label == "TODAY") {
          EyebrowStyle.copy(color = KlikInkPrimary)
        } else {
          EyebrowStyle
        }
        drawText(
          textMeasurer = textMeasurer,
          text = label,
          topLeft = Offset(16f, ly - 5f),
          style = style,
        )
      }

      // YOU node (filled black with hairline ring)
      val youCenter = Offset(w * 0.5f, topPad + 6f)
      drawCircle(KlikInkPrimary, youR, youCenter)
      drawCircle(KlikPaperApp, youR - 2f, youCenter)
      drawCircle(KlikInkPrimary, youR - 4f, youCenter)
      drawText(
        textMeasurer = textMeasurer,
        text = data.user.displayName.take(14),
        topLeft = Offset(youCenter.x - 40f, youCenter.y + youR + 6f),
        style = NodeLabelStyle.copy(color = KlikInkPrimary),
        size = Size(80f, 14f),
      )
      drawText(
        textMeasurer = textMeasurer,
        text = "Lv ${data.user.level}",
        topLeft = Offset(youCenter.x - 30f, youCenter.y + youR + 22f),
        style = MetaLabelStyle,
        size = Size(60f, 12f),
      )

      // Organizations (right column)
      orgs.forEachIndexed { i, node ->
        val yFraction = (i + 1).toFloat() / (orgs.size + 2).toFloat() * 0.42f + 0.08f
        val xFraction = 0.62f + (i % 3) * 0.10f
        drawDotNode(
          textMeasurer,
          Offset(w * xFraction.coerceIn(0.12f, 0.88f), topPad + pastH * yFraction),
          label = node.label.take(12),
          accent = accentForType(TYPE_ORG),
          dotR = dotR,
        )
      }

      // Projects (left column)
      projects.forEachIndexed { i, node ->
        val yFraction = (i + 1).toFloat() / (projects.size + 2).toFloat() * 0.50f + 0.12f
        val xFraction = 0.16f + (i % 4) * 0.09f
        drawDotNode(
          textMeasurer,
          Offset(w * xFraction.coerceIn(0.12f, 0.88f), topPad + pastH * yFraction),
          label = node.label.take(12),
          accent = accentForType(TYPE_PROJECT),
          dotR = dotR,
        )
      }

      // People (scattered mid-band)
      val topPeople = people.take(20)
      topPeople.forEachIndexed { i, node ->
        val tier = node.metaString("influence_tier") ?: "C"
        val isStar = tier == "S"
        val yFraction = 0.42f + (i / topPeople.size.toFloat().coerceAtLeast(1f)) * 0.32f
        val xFraction = if (isStar) {
          0.5f
        } else {
          (0.18f + (i % 5) * 0.14f + (i * 0.025f)) % 0.84f
        }
        val center = Offset(
          w * xFraction.coerceIn(0.10f, 0.90f),
          topPad + pastH * yFraction,
        )
        if (isStar) {
          drawCircle(KlikInkPrimary, ringR, center, style = Stroke(width = 1.dp.toPx()))
          drawCircle(KlikInkPrimary, dotR, center)
        } else {
          drawCircle(KlikInkPrimary, dotR, center)
        }
        drawText(
          textMeasurer = textMeasurer,
          text = node.label.take(10),
          topLeft = Offset(center.x - 36f, center.y + dotR + 4f),
          style = MetaLabelStyle.copy(color = KlikInkPrimary),
          size = Size(72f, 12f),
        )
      }

      // Achievements
      achievements.forEachIndexed { i, ach ->
        val yFraction = 0.52f + i * 0.045f
        val xFraction = 0.18f + (i % 4) * 0.20f
        val center = Offset(
          w * xFraction.coerceIn(0.12f, 0.88f),
          topPad + pastH * yFraction.coerceAtMost(0.92f),
        )
        val label = when (ach.milestoneType) {
          "level_up" -> "Lv ${ach.levelReached ?: ""}"
          "streak" -> "${ach.streakDays}d"
          else -> ach.achievementId.take(10)
        }
        drawDotNode(
          textMeasurer,
          center,
          label = label,
          accent = KlikRiskAccent,
          dotR = dotR,
        )
      }

      // TODAY hairline rule (dashed)
      drawLine(
        color = KlikInkPrimary,
        start = Offset(16f, todayY),
        end = Offset(w - 16f, todayY),
        strokeWidth = 1.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f)),
      )
      drawText(
        textMeasurer = textMeasurer,
        text = "TODAY",
        topLeft = Offset(16f, todayY - 16f),
        style = EyebrowStyle.copy(color = KlikInkPrimary),
      )

      // Future nodes — empty rings (dashed) below TODAY
      futureNodes.forEachIndexed { i, fNode ->
        val nodeY = todayY + 36f + i * 56f
        val nodeX = w * (0.30f + (i % 2) * 0.40f)
        if (nodeY < h - 24f) {
          drawFutureRing(textMeasurer, Offset(nodeX, nodeY), fNode.label.take(14))
        }
      }
    }

    DescriptionCard(
      title = "Constellation",
      description = "${data.user.displayName}'s journey as a paper map. " +
        "${data.user.totalSessions} sessions · ${data.user.totalPeople} people · " +
        "${data.user.totalProjects} projects.",
    )
    TypeLegend()
    Spacer(Modifier.height(20.dp))
  }
}

private fun buildConstellationTimeLabels(data: GrowthTreeResponse): List<Pair<String, Float>> {
  val labels = mutableListOf<Pair<String, Float>>()
  labels.add(data.meta.fromDate.formatShortDate() to 0.05f)
  labels.add("TODAY" to 0.95f)
  return labels
}

// ==================== VIEW E: River Timeline ====================

@Composable
fun RiverTimelineView(data: GrowthTreeResponse) {
  val textMeasurer = rememberTextMeasurer()

  val people = data.nodesByType(TYPE_PERSON)
  val orgs = data.nodesByType(TYPE_ORG)
  val projects = data.nodesByType(TYPE_PROJECT)
  val achievements = data.achievements
  val futureNodes = data.futureProjection

  data class RiverEvent(
    val label: String,
    val type: String,
    val yFraction: Float,
    val side: String,
    val isFuture: Boolean = false,
  )

  val events = remember(data) {
    val list = mutableListOf<RiverEvent>()
    val total = (orgs.size + projects.size + people.take(15).size + achievements.size).coerceAtLeast(1)
    list.addAll(
      orgs.mapIndexed { i, n ->
        RiverEvent(n.label, TYPE_ORG, (i + 1f) / total * 0.85f, "left")
      },
    )
    list.addAll(
      people.take(15).mapIndexed { i, n ->
        RiverEvent(n.label, TYPE_PERSON, (orgs.size + i + 1f) / total * 0.85f, "left")
      },
    )
    list.addAll(
      projects.take(10).mapIndexed { i, n ->
        RiverEvent(n.label, TYPE_PROJECT, (i + 1f) / total * 0.85f, "right")
      },
    )
    list.addAll(
      achievements.mapIndexed { i, a ->
        val label = when (a.milestoneType) {
          "level_up" -> "Lv ${a.levelReached ?: ""}"
          "streak" -> "${a.streakDays}d streak"
          else -> a.achievementId.take(14)
        }
        RiverEvent(label, "achievement", (projects.size + i + 1f) / total * 0.85f, "right")
      },
    )
    list.sortedBy { it.yFraction }
  }

  val scrollState = rememberScrollState()
  val canvasHeight = (760 + futureNodes.size * 48).coerceAtLeast(760)

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .background(KlikPaperApp),
  ) {
    Canvas(
      modifier = Modifier
        .fillMaxWidth()
        .height(canvasHeight.dp),
    ) {
      val w = size.width
      val h = size.height
      val centerX = w * 0.5f
      val pillW = 132f
      val pillH = 22f
      val branch = 56f

      // Trunk (single hairline)
      drawLine(
        color = KlikInkPrimary,
        start = Offset(centerX, 28f),
        end = Offset(centerX, h - 28f),
        strokeWidth = 1.dp.toPx(),
        cap = StrokeCap.Round,
      )

      // YOU marker at top
      drawCircle(KlikInkPrimary, 6.dp.toPx(), Offset(centerX, 28f))
      drawText(
        textMeasurer = textMeasurer,
        text = "YOU",
        topLeft = Offset(centerX - 18f, 8f),
        style = EyebrowStyle.copy(color = KlikInkPrimary),
        size = Size(36f, 12f),
      )

      // TODAY rule
      val todayY = h * 0.74f
      drawLine(
        color = KlikInkPrimary,
        start = Offset(28f, todayY),
        end = Offset(w - 28f, todayY),
        strokeWidth = 1.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f)),
      )
      drawText(
        textMeasurer = textMeasurer,
        text = "TODAY",
        topLeft = Offset(28f, todayY - 16f),
        style = EyebrowStyle.copy(color = KlikInkPrimary),
      )

      // Past events
      events.forEach { event ->
        val yPos = 60f + (todayY - 96f) * event.yFraction
        val isRight = event.side == "right"
        val accent = accentForType(event.type)

        val endX = if (isRight) centerX + branch else centerX - branch
        val pillX = if (isRight) endX else endX - pillW

        // Branch hairline
        drawLine(
          color = KlikLineHairline,
          start = Offset(centerX, yPos),
          end = Offset(if (isRight) pillX else pillX + pillW, yPos),
          strokeWidth = 1.dp.toPx(),
        )

        // Trunk dot
        drawCircle(KlikInkPrimary, 2.5.dp.toPx(), Offset(centerX, yPos))

        drawEditorialPill(
          textMeasurer = textMeasurer,
          topLeft = Offset(pillX, yPos - pillH / 2f),
          width = pillW,
          height = pillH,
          label = event.label.take(18),
          accent = accent,
          isFuture = false,
        )
      }

      // Future events (below TODAY) — same pill, dashed border
      futureNodes.forEachIndexed { i, fNode ->
        val yPos = todayY + 40f + i * 48f
        if (yPos > h - 28f) return@forEachIndexed
        val isRight = i % 2 == 1
        val endX = if (isRight) centerX + branch else centerX - branch
        val pillX = if (isRight) endX else endX - pillW
        drawLine(
          color = KlikLineMute,
          start = Offset(centerX, yPos),
          end = Offset(if (isRight) pillX else pillX + pillW, yPos),
          strokeWidth = 1.dp.toPx(),
          pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)),
        )
        drawCircle(
          KlikInkMuted,
          3.dp.toPx(),
          Offset(centerX, yPos),
          style = Stroke(width = 1.dp.toPx()),
        )
        drawEditorialPill(
          textMeasurer = textMeasurer,
          topLeft = Offset(pillX, yPos - pillH / 2f),
          width = pillW,
          height = pillH,
          label = fNode.label.take(18),
          accent = KlikInkMuted,
          isFuture = true,
        )
      }
    }

    DescriptionCard(
      title = "River",
      description = "A vertical timeline of moments along your trunk — past above, future below.",
    )
    TypeLegend()
    Spacer(Modifier.height(20.dp))
  }
}

// ==================== VIEW F: Connected Network ====================

@Composable
fun ConnectedConstellationView(data: GrowthTreeResponse) {
  val textMeasurer = rememberTextMeasurer()

  val people = data.nodesByType(TYPE_PERSON)
  val orgs = data.nodesByType(TYPE_ORG)
  val projects = data.nodesByType(TYPE_PROJECT)
  val edges = data.edges
  val futureNodes = data.futureProjection

  data class NodePos(val id: String, val x: Float, val y: Float, val type: String, val label: String)

  val scrollState = rememberScrollState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .background(KlikPaperApp),
  ) {
    EdgeLegend()
    Canvas(
      modifier = Modifier
        .fillMaxWidth()
        .height(720.dp),
    ) {
      val w = size.width
      val h = size.height
      val dotR = 3.5.dp.toPx()
      val ringR = 5.5.dp.toPx()
      val youR = 8.dp.toPx()

      val nodePositions = mutableMapOf<String, NodePos>()

      // YOU at top center
      val userY = 60f
      val userX = w * 0.5f
      nodePositions[data.user.userId] = NodePos(
        data.user.userId,
        userX,
        userY,
        "user",
        data.user.displayName,
      )

      // Tier 1: top people + orgs along an arc
      val tier1 = (people.take(6) + orgs.take(3))
      tier1.forEachIndexed { i, node ->
        val t = if (tier1.size <= 1) 0.5f else i.toFloat() / (tier1.size - 1).toFloat()
        val x = w * (0.10f + 0.80f * t)
        val y = userY + 130f + ((i % 2) * 28f)
        nodePositions[node.id] = NodePos(node.id, x, y, node.nodeType, node.label)
      }

      // Tier 2: projects + remaining people across the middle band
      val tier2 = (projects.take(6) + people.drop(6).take(4))
      tier2.forEachIndexed { i, node ->
        val t = if (tier2.size <= 1) 0.5f else i.toFloat() / (tier2.size - 1).toFloat()
        val x = w * (0.10f + 0.80f * t)
        val y = userY + 290f + ((i % 2) * 28f)
        nodePositions[node.id] = NodePos(node.id, x, y, node.nodeType, node.label)
      }

      // Edges — hairline, solid or dashed by type
      edges.forEach { edge ->
        val s = nodePositions[edge.sourceId] ?: return@forEach
        val t = nodePositions[edge.targetId] ?: return@forEach
        val dashed = edge.relationshipType in DASHED_REL_TYPES
        drawLine(
          color = KlikLineHairline,
          start = Offset(s.x, s.y),
          end = Offset(t.x, t.y),
          strokeWidth = 1.dp.toPx(),
          pathEffect = if (dashed) PathEffect.dashPathEffect(floatArrayOf(5f, 4f)) else null,
        )
      }

      // Tier 2 nodes
      tier2.forEach { node ->
        val pos = nodePositions[node.id] ?: return@forEach
        drawDotNode(
          textMeasurer = textMeasurer,
          center = Offset(pos.x, pos.y),
          label = pos.label.take(10),
          accent = accentForType(node.nodeType),
          dotR = dotR,
        )
      }

      // Tier 1 nodes (slightly larger; star-tier people get a ring)
      tier1.forEach { node ->
        val pos = nodePositions[node.id] ?: return@forEach
        val isStar = node.nodeType == TYPE_PERSON &&
          (node.metaString("influence_tier") == "S")
        val center = Offset(pos.x, pos.y)
        if (isStar) {
          drawCircle(KlikInkPrimary, ringR, center, style = Stroke(width = 1.dp.toPx()))
        }
        drawCircle(accentForType(node.nodeType), dotR, center)
        drawText(
          textMeasurer = textMeasurer,
          text = pos.label.take(10),
          topLeft = Offset(center.x - 40f, center.y + dotR + 4f),
          style = MetaLabelStyle.copy(color = KlikInkPrimary),
          size = Size(80f, 12f),
        )
      }

      // YOU node (filled with hairline ring)
      drawCircle(KlikInkPrimary, youR, Offset(userX, userY))
      drawCircle(KlikPaperApp, youR - 2f, Offset(userX, userY))
      drawCircle(KlikInkPrimary, youR - 4f, Offset(userX, userY))
      drawText(
        textMeasurer = textMeasurer,
        text = "YOU",
        topLeft = Offset(userX - 18f, userY + youR + 6f),
        style = EyebrowStyle.copy(color = KlikInkPrimary),
        size = Size(36f, 12f),
      )

      // FUTURE divider
      val futureY = h - 90f - (futureNodes.size.coerceAtMost(2) * 18f)
      drawLine(
        color = KlikInkPrimary,
        start = Offset(28f, futureY),
        end = Offset(w - 28f, futureY),
        strokeWidth = 1.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f)),
      )
      drawText(
        textMeasurer = textMeasurer,
        text = "FUTURE",
        topLeft = Offset(28f, futureY - 16f),
        style = EyebrowStyle.copy(color = KlikInkPrimary),
      )
      futureNodes.forEachIndexed { i, fNode ->
        val n = futureNodes.size.coerceAtLeast(1)
        val xPos = w * (0.18f + (i.toFloat() / n) * 0.64f)
        val yPos = (futureY + 32f + i * 36f).coerceAtMost(h - 16f)
        drawFutureRing(textMeasurer, Offset(xPos, yPos), fNode.label.take(14))
      }
    }

    DescriptionCard(
      title = "Network",
      description = "${data.user.totalRelationships} relationships. " +
        "Solid lines are direct ties; dashed lines are softer signals.",
    )
    TypeLegend()
    Spacer(Modifier.height(20.dp))
  }
}

private val DASHED_REL_TYPES = setOf("collab", "involved_in", "leads")

// ==================== Legends ====================

@Composable
private fun TypeLegend() {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 6.dp)
      .horizontalScroll(rememberScrollState()),
    horizontalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    DotLegendItem("You", KlikInkPrimary, filledRing = true)
    DotLegendItem("Person", KlikInkPrimary)
    DotLegendItem("Org", KlikDecisionAccent)
    DotLegendItem("Project", KlikCommitmentAccent)
    DotLegendItem("Achievement", KlikRiskAccent)
    DotLegendItem("Future", KlikInkMuted, dashed = true)
  }
}

@Composable
private fun DotLegendItem(
  label: String,
  color: Color,
  dashed: Boolean = false,
  filledRing: Boolean = false,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .clip(RoundedCornerShape(7.dp))
      .background(KlikPaperChip)
      .padding(horizontal = 8.dp, vertical = 5.dp),
  ) {
    Canvas(modifier = Modifier.size(10.dp)) {
      val r = 3.dp.toPx()
      val c = Offset(size.width / 2f, size.height / 2f)
      when {
        dashed -> drawCircle(
          color = color,
          radius = r,
          center = c,
          style = Stroke(
            width = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 2f)),
          ),
        )

        filledRing -> {
          drawCircle(color, r + 1.dp.toPx(), c, style = Stroke(width = 1.dp.toPx()))
          drawCircle(color, r - 1.dp.toPx(), c)
        }

        else -> drawCircle(color, r, c)
      }
    }
    Spacer(Modifier.width(6.dp))
    Text(label, color = KlikInkTertiary, fontSize = 10.sp, fontWeight = FontWeight.Medium)
  }
}

@Composable
private fun EdgeLegend() {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 6.dp)
      .horizontalScroll(rememberScrollState()),
    horizontalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    EdgeLegendItem("works_at", dashed = false)
    EdgeLegendItem("owns", dashed = false)
    EdgeLegendItem("collab", dashed = true)
    EdgeLegendItem("involved_in", dashed = true)
    EdgeLegendItem("leads", dashed = true)
  }
}

@Composable
private fun EdgeLegendItem(label: String, dashed: Boolean) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .clip(RoundedCornerShape(7.dp))
      .background(KlikPaperChip)
      .padding(horizontal = 8.dp, vertical = 5.dp),
  ) {
    Canvas(modifier = Modifier.size(width = 22.dp, height = 2.dp)) {
      drawLine(
        color = KlikInkTertiary,
        start = Offset(0f, size.height / 2f),
        end = Offset(size.width, size.height / 2f),
        strokeWidth = 1.dp.toPx(),
        pathEffect = if (dashed) PathEffect.dashPathEffect(floatArrayOf(4f, 3f)) else null,
      )
    }
    Spacer(Modifier.width(6.dp))
    Text(label, color = KlikInkTertiary, fontSize = 10.sp, fontWeight = FontWeight.Medium)
  }
}

// ==================== Description Card ====================

@Composable
private fun DescriptionCard(title: String, description: String) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 12.dp)
      .clip(RoundedCornerShape(14.dp))
      .background(KlikPaperSoft)
      .padding(14.dp),
  ) {
    Column {
      Text(
        text = title.uppercase(),
        color = KlikInkFaint,
        fontSize = 9.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.sp,
      )
      Spacer(Modifier.height(4.dp))
      Text(
        text = description,
        color = KlikInkPrimary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 17.sp,
      )
    }
  }
}

// ==================== Canvas helpers ====================

private fun DrawScope.drawDotNode(
  textMeasurer: TextMeasurer,
  center: Offset,
  label: String,
  accent: Color,
  dotR: Float,
) {
  drawCircle(accent, dotR, center)
  drawText(
    textMeasurer = textMeasurer,
    text = label,
    topLeft = Offset(center.x - 40f, center.y + dotR + 4f),
    style = MetaLabelStyle.copy(color = KlikInkPrimary),
    size = Size(80f, 12f),
    overflow = TextOverflow.Ellipsis,
    maxLines = 1,
  )
}

private fun DrawScope.drawFutureRing(
  textMeasurer: TextMeasurer,
  center: Offset,
  label: String,
) {
  drawCircle(
    color = KlikInkMuted,
    radius = 4.5.dp.toPx(),
    center = center,
    style = Stroke(
      width = 1.dp.toPx(),
      pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 3f)),
    ),
  )
  drawText(
    textMeasurer = textMeasurer,
    text = label,
    topLeft = Offset(center.x - 40f, center.y + 4.5.dp.toPx() + 4f),
    style = MetaLabelStyle,
    size = Size(80f, 12f),
    overflow = TextOverflow.Ellipsis,
    maxLines = 1,
  )
}

private fun DrawScope.drawEditorialPill(
  textMeasurer: TextMeasurer,
  topLeft: Offset,
  width: Float,
  height: Float,
  label: String,
  accent: Color,
  isFuture: Boolean,
) {
  val radius = CornerRadius(7.dp.toPx())
  val strokeWidth = 1.dp.toPx()
  drawRoundRect(
    color = if (isFuture) KlikPaperApp else KlikPaperSoft,
    topLeft = topLeft,
    size = Size(width, height),
    cornerRadius = radius,
  )
  if (isFuture) {
    drawRoundRect(
      color = KlikLineMute,
      topLeft = topLeft,
      size = Size(width, height),
      cornerRadius = radius,
      style = Stroke(
        width = strokeWidth,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 3f)),
      ),
    )
  } else {
    drawRoundRect(
      color = KlikLineHairline,
      topLeft = topLeft,
      size = Size(width, height),
      cornerRadius = radius,
      style = Stroke(width = strokeWidth),
    )
  }
  // 2dp left rule in accent
  val rulePath = Path().apply {
    addRoundRect(
      androidx.compose.ui.geometry.RoundRect(
        left = topLeft.x,
        top = topLeft.y,
        right = topLeft.x + 2.dp.toPx(),
        bottom = topLeft.y + height,
        topLeftCornerRadius = radius,
        bottomLeftCornerRadius = radius,
        topRightCornerRadius = CornerRadius(0f),
        bottomRightCornerRadius = CornerRadius(0f),
      ),
    )
  }
  drawPath(rulePath, color = accent)
  drawText(
    textMeasurer = textMeasurer,
    text = label,
    topLeft = Offset(topLeft.x + 8.dp.toPx(), topLeft.y + (height - 12f) / 2f),
    style = TextStyle(
      color = if (isFuture) KlikInkTertiary else KlikInkPrimary,
      fontSize = 10.sp,
      fontWeight = FontWeight.Medium,
    ),
    size = Size(width - 12.dp.toPx(), height),
    overflow = TextOverflow.Ellipsis,
    maxLines = 1,
  )
}
