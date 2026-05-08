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
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.fletchmckee.liquid.samples.app.data.source.remote.GrowthTreeFutureNode
import io.github.fletchmckee.liquid.samples.app.data.source.remote.GrowthTreeNode
import io.github.fletchmckee.liquid.samples.app.data.source.remote.GrowthTreeResponse
import io.github.fletchmckee.liquid.samples.app.data.source.remote.GrowthTreeUserSummary
import io.github.fletchmckee.liquid.samples.app.data.source.remote.RemoteDataFetcher
import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import io.github.fletchmckee.liquid.samples.app.platform.ShareService
import io.github.fletchmckee.liquid.samples.app.theme.KlikCommitmentAccent
import io.github.fletchmckee.liquid.samples.app.theme.KlikDecisionAccent
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkFaint
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkMuted
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkPrimary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkTertiary
import io.github.fletchmckee.liquid.samples.app.theme.KlikLineHairline
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

private const val TYPE_PERSON = "person"
private const val TYPE_ORG = "organization"
private const val TYPE_PROJECT = "project"

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
  onBack: (() -> Unit)?,
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
      KlikLogger.d("GrowthTreeScreen", "Loaded: ${treeData?.meta?.totalNodes} nodes")
    } catch (e: Exception) {
      KlikLogger.e("GrowthTreeScreen", "Failed: ${e.message}")
      errorMessage = e.message
    } finally {
      isLoading = false
    }
  }

  Box(modifier = modifier.fillMaxSize().background(KlikPaperApp)) {
    Column(modifier = Modifier.fillMaxSize()) {
      GrowthTreeTopBar(
        selectedView = selectedView,
        onViewChange = { selectedView = it },
        onBack = onBack,
        onShare = treeData?.let { data ->
          {
            val u = data.user
            ShareService.share(
              text = buildString {
                append("${u.displayName}'s Growth — Level ${u.level}\n")
                append("${u.totalXp} XP · ${u.totalSessions} sessions\n")
                append("${u.totalPeople} people · ${u.totalOrgs} orgs · ${u.totalProjects} projects")
                if (data.achievements.isNotEmpty()) append("\n${data.achievements.size} milestones reached")
                append("\n\nTracked with Klik")
              },
              subject = "My Growth — Level ${u.level}",
            )
          }
        },
      )

      when {
        isLoading -> CenterMessage {
          CircularProgressIndicator(color = KlikInkPrimary, strokeWidth = 1.5.dp, modifier = Modifier.size(20.dp))
          Spacer(Modifier.height(10.dp))
          Text("Loading…", color = KlikInkTertiary, fontSize = 12.sp)
        }
        errorMessage != null -> CenterMessage {
          Text("Couldn't load", color = KlikInkPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
          Spacer(Modifier.height(6.dp))
          Text(errorMessage ?: "Unknown error", color = KlikInkTertiary, fontSize = 12.sp, textAlign = TextAlign.Center)
          Spacer(Modifier.height(14.dp))
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(7.dp))
              .background(KlikPaperChip)
              .k1Clickable { retryKey += 1 }
              .padding(horizontal = 14.dp, vertical = 8.dp),
          ) {
            Text("Try again", color = KlikInkPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
          }
        }
        treeData != null -> {
          StatsBar(user = treeData!!.user)
          HairlineDivider()
          AnimatedContent(
            targetState = selectedView,
            transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(160)) },
            label = "GrowthView",
          ) { view ->
            when (view) {
              0 -> TreeView(treeData!!)
              else -> HeatView(treeData!!)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun CenterMessage(content: @Composable () -> Unit) {
  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) { content() }
  }
}

@Composable
private fun HairlineDivider() {
  Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(KlikLineHairline))
}

// ==================== Top Bar ====================

@Composable
private fun GrowthTreeTopBar(
  selectedView: Int,
  onViewChange: (Int) -> Unit,
  onBack: (() -> Unit)?,
  onShare: (() -> Unit)?,
) {
  Column(
    modifier = Modifier.fillMaxWidth().background(KlikPaperApp).statusBarsPadding(),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
      if (onBack != null) {
        Box(
          modifier = Modifier.size(32.dp).clip(CircleShape).k1Clickable(onClick = onBack),
          contentAlignment = Alignment.Center,
        ) {
          Canvas(Modifier.size(14.dp)) {
            val w = 1.4.dp.toPx()
            drawLine(KlikInkPrimary, strokeWidth = w, cap = StrokeCap.Round, start = Offset(9.dp.toPx(), 3.dp.toPx()), end = Offset(4.dp.toPx(), 7.dp.toPx()))
            drawLine(KlikInkPrimary, strokeWidth = w, cap = StrokeCap.Round, start = Offset(4.dp.toPx(), 7.dp.toPx()), end = Offset(9.dp.toPx(), 11.dp.toPx()))
          }
        }
      } else {
        Spacer(Modifier.size(32.dp))
      }

      Spacer(Modifier.weight(1f))
      Text("GROWTH", color = KlikInkFaint, fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.2.sp)
      Spacer(Modifier.weight(1f))

      if (onShare != null) {
        Box(
          modifier = Modifier.size(32.dp).clip(CircleShape).k1Clickable(onClick = onShare),
          contentAlignment = Alignment.Center,
        ) {
          Canvas(Modifier.size(14.dp)) {
            val w = 1.4.dp.toPx()
            val cx = size.width / 2f
            drawLine(KlikInkPrimary, strokeWidth = w, cap = StrokeCap.Round, start = Offset(cx, 9.dp.toPx()), end = Offset(cx, 1.5.dp.toPx()))
            drawLine(KlikInkPrimary, strokeWidth = w, cap = StrokeCap.Round, start = Offset(cx - 2.5.dp.toPx(), 4.5.dp.toPx()), end = Offset(cx, 1.5.dp.toPx()))
            drawLine(KlikInkPrimary, strokeWidth = w, cap = StrokeCap.Round, start = Offset(cx + 2.5.dp.toPx(), 4.5.dp.toPx()), end = Offset(cx, 1.5.dp.toPx()))
            drawLine(KlikInkPrimary, strokeWidth = w, cap = StrokeCap.Round, start = Offset(2.dp.toPx(), 11.dp.toPx()), end = Offset(12.dp.toPx(), 11.dp.toPx()))
          }
        }
      } else {
        Spacer(Modifier.size(32.dp))
      }
    }

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(KlikPaperChip).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        listOf("Tree", "Heat").forEachIndexed { index, label ->
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
    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp).horizontalScroll(rememberScrollState()),
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
  Box(modifier = Modifier.clip(RoundedCornerShape(7.dp)).background(KlikPaperChip).padding(horizontal = 10.dp, vertical = 6.dp)) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(value, color = KlikInkPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
      Spacer(Modifier.width(4.dp))
      Text(label, color = KlikInkTertiary, fontSize = 10.sp)
    }
  }
}

// ==================== Helpers ====================

private fun GrowthTreeResponse.nodesByType(type: String): List<GrowthTreeNode> =
  timelineNodes.filter { it.nodeType == type && !it.isFuture }

private fun GrowthTreeNode.metaString(key: String): String? = metadata[key]?.jsonPrimitive?.content

@Suppress("unused")
private fun GrowthTreeNode.metaFloat(key: String): Float? = metadata[key]?.jsonPrimitive?.floatOrNull

@Suppress("unused")
private fun GrowthTreeNode.metaInt(key: String): Int? = metadata[key]?.jsonPrimitive?.intOrNull

private fun String.formatShortDate(): String {
  val parts = split("-")
  if (parts.size < 2) return take(8)
  val year = parts[0].takeLast(2)
  val month = when (parts[1]) {
    "01" -> "JAN"; "02" -> "FEB"; "03" -> "MAR"; "04" -> "APR"
    "05" -> "MAY"; "06" -> "JUN"; "07" -> "JUL"; "08" -> "AUG"
    "09" -> "SEP"; "10" -> "OCT"; "11" -> "NOV"; "12" -> "DEC"
    else -> parts[1]
  }
  return "$month '$year"
}

private fun evalCubic(t: Float, p0: Float, cp1: Float, cp2: Float, p1: Float): Float {
  val u = 1f - t
  return u * u * u * p0 + 3f * u * u * t * cp1 + 3f * u * t * t * cp2 + t * t * t * p1
}

// ==================== VIEW: Tree ====================
//
// You are at the bottom (root). Three branches curve upward:
//   left → People, center → Orgs, right → Projects.
// Entities are leaf dots placed along each branch, oldest near the
// trunk (lower), newest near the tip (upper) — the tree grows over time.
// Achievement milestones are small diamond markers on the trunk.
// Future goals are dashed circles just past the branch tips.

@Composable
private fun TreeView(data: GrowthTreeResponse) {
  val people = remember(data.timelineNodes) {
    data.nodesByType(TYPE_PERSON).sortedBy { it.timestamp }.take(6)
  }
  val orgs = remember(data.timelineNodes) {
    data.nodesByType(TYPE_ORG).sortedBy { it.timestamp }.take(6)
  }
  val projects = remember(data.timelineNodes) {
    data.nodesByType(TYPE_PROJECT).sortedBy { it.timestamp }.take(6)
  }

  Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).background(KlikPaperApp)) {
    Spacer(Modifier.height(4.dp))
    TreeCanvasBlock(
      user = data.user,
      people = people,
      orgs = orgs,
      projects = projects,
      achievementCount = data.achievements.size,
      goals = data.futureProjection,
      peopleTotal = data.user.totalPeople.coerceAtLeast(data.nodesByType(TYPE_PERSON).size),
      orgsTotal = data.user.totalOrgs.coerceAtLeast(data.nodesByType(TYPE_ORG).size),
      projectsTotal = data.user.totalProjects.coerceAtLeast(data.nodesByType(TYPE_PROJECT).size),
    )
    TodayLine(level = data.user.level, xpToNext = data.user.xpToNextLevel)
    if (data.futureProjection.isNotEmpty()) {
      GoalsStrip(data.futureProjection)
    }
    Spacer(Modifier.height(28.dp))
  }
}

@Composable
private fun TreeCanvasBlock(
  user: GrowthTreeUserSummary,
  people: List<GrowthTreeNode>,
  orgs: List<GrowthTreeNode>,
  projects: List<GrowthTreeNode>,
  achievementCount: Int,
  goals: List<GrowthTreeFutureNode>,
  peopleTotal: Int,
  orgsTotal: Int,
  projectsTotal: Int,
) {
  BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(540.dp).padding(horizontal = 8.dp)) {
    val wDp = maxWidth.value

    // Root at bottom center (in dp coords within the box)
    val rootX = wDp * 0.50f
    val rootY = 500f

    // Trunk top (where branches diverge)
    val trunkTopY = 320f

    // Branch tips
    val pTipX = wDp * 0.12f; val pTipY = 60f   // People — upper left
    val oTipX = wDp * 0.50f; val oTipY = 44f   // Orgs  — upper centre
    val eTipX = wDp * 0.88f; val eTipY = 60f   // Projects — upper right

    // Cubic bezier control points for each branch
    val pCp1X = rootX - 6f; val pCp1Y = trunkTopY - 70f
    val pCp2X = pTipX + 44f; val pCp2Y = pTipY + 110f
    val oCp1X = rootX; val oCp1Y = trunkTopY - 110f
    val oCp2X = oTipX; val oCp2Y = oTipY + 110f
    val eCp1X = rootX + 6f; val eCp1Y = trunkTopY - 70f
    val eCp2X = eTipX - 44f; val eCp2Y = eTipY + 110f

    // Distribute N leaves along a bezier: oldest at t=low (near trunk), newest at t=high (tip)
    fun leafPositions(
      nodes: List<GrowthTreeNode>,
      cp1x: Float, cp1y: Float,
      cp2x: Float, cp2y: Float,
      tipX: Float, tipY: Float,
    ): List<Triple<GrowthTreeNode, Float, Float>> {
      if (nodes.isEmpty()) return emptyList()
      return nodes.mapIndexed { i, node ->
        val t = (i + 1f) / (nodes.size + 1f)
        Triple(node, evalCubic(t, rootX, cp1x, cp2x, tipX), evalCubic(t, trunkTopY, cp1y, cp2y, tipY))
      }
    }

    val pLeaves = leafPositions(people, pCp1X, pCp1Y, pCp2X, pCp2Y, pTipX, pTipY)
    val oLeaves = leafPositions(orgs, oCp1X, oCp1Y, oCp2X, oCp2Y, oTipX, oTipY)
    val eLeaves = leafPositions(projects, eCp1X, eCp1Y, eCp2X, eCp2Y, eTipX, eTipY)

    Canvas(modifier = Modifier.matchParentSize()) {
      val rx = rootX.dp.toPx(); val ry = rootY.dp.toPx()
      val ttx = rootX.dp.toPx(); val tty = trunkTopY.dp.toPx()

      // Trunk
      drawLine(
        color = KlikInkPrimary,
        strokeWidth = 1.8.dp.toPx(),
        cap = StrokeCap.Round,
        start = Offset(rx, ry - 13.dp.toPx()),
        end = Offset(ttx, tty),
      )

      // Three branches
      fun branch(cp1x: Float, cp1y: Float, cp2x: Float, cp2y: Float, tipX: Float, tipY: Float) {
        val path = Path().apply {
          moveTo(ttx, tty)
          cubicTo(cp1x.dp.toPx(), cp1y.dp.toPx(), cp2x.dp.toPx(), cp2y.dp.toPx(), tipX.dp.toPx(), tipY.dp.toPx())
        }
        drawPath(path, KlikLineHairline, style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round))
      }
      branch(pCp1X, pCp1Y, pCp2X, pCp2Y, pTipX, pTipY)
      branch(oCp1X, oCp1Y, oCp2X, oCp2Y, oTipX, oTipY)
      branch(eCp1X, eCp1Y, eCp2X, eCp2Y, eTipX, eTipY)

      // Leaf dots
      for (leaf in pLeaves) drawCircle(KlikInkPrimary, 3.5.dp.toPx(), Offset(leaf.second.dp.toPx(), leaf.third.dp.toPx()))
      for (leaf in oLeaves) drawCircle(KlikDecisionAccent, 3.5.dp.toPx(), Offset(leaf.second.dp.toPx(), leaf.third.dp.toPx()))
      for (leaf in eLeaves) drawCircle(KlikCommitmentAccent, 3.5.dp.toPx(), Offset(leaf.second.dp.toPx(), leaf.third.dp.toPx()))

      // Achievement diamonds on trunk (milestone markers)
      if (achievementCount > 0) {
        val n = achievementCount.coerceAtMost(6)
        val trunkLen = rootY - 14f - trunkTopY
        for (i in 0 until n) {
          val t = (i + 1f) / (n + 1f)
          val dx = rx
          val dy = (ry - 13.dp.toPx()) - t * trunkLen.dp.toPx()
          val ds = 2.8.dp.toPx()
          drawPath(
            Path().apply {
              moveTo(dx, dy - ds); lineTo(dx + ds, dy); lineTo(dx, dy + ds); lineTo(dx - ds, dy); close()
            },
            KlikRiskAccent,
          )
        }
      }

      // Goal circles at branch tips (future growth)
      if (goals.isNotEmpty()) {
        for ((tipX, tipY) in listOf(pTipX to pTipY, oTipX to oTipY, eTipX to eTipY)) {
          drawCircle(
            color = KlikInkMuted,
            radius = 5.dp.toPx(),
            center = Offset(tipX.dp.toPx(), tipY.dp.toPx()),
            style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 2f))),
          )
        }
      }

      // Root dot — concentric rings
      drawCircle(KlikInkPrimary, 11.dp.toPx(), Offset(rx, ry))
      drawCircle(KlikPaperApp, 7.5.dp.toPx(), Offset(rx, ry))
      drawCircle(KlikInkPrimary, 4.dp.toPx(), Offset(rx, ry))

      // Branch tip dots
      drawCircle(KlikInkPrimary, 4.5.dp.toPx(), Offset(pTipX.dp.toPx(), pTipY.dp.toPx()))
      drawCircle(KlikDecisionAccent, 4.5.dp.toPx(), Offset(oTipX.dp.toPx(), oTipY.dp.toPx()))
      drawCircle(KlikCommitmentAccent, 4.5.dp.toPx(), Offset(eTipX.dp.toPx(), eTipY.dp.toPx()))
    }

    // Text overlays
    OverlayLabel(cx = rootX, y = rootY + 14f, widthDp = 160f, text = user.displayName, isHeader = true)

    OverlayLabel(cx = pTipX, y = pTipY + 10f, widthDp = 74f, text = "PEOPLE", isEyebrow = true)
    OverlayLabel(cx = pTipX, y = pTipY + 22f, widthDp = 74f, text = "$peopleTotal", isMeta = true)

    OverlayLabel(cx = oTipX, y = oTipY + 10f, widthDp = 74f, text = "ORGS", isEyebrow = true)
    OverlayLabel(cx = oTipX, y = oTipY + 22f, widthDp = 74f, text = "$orgsTotal", isMeta = true)

    OverlayLabel(cx = eTipX, y = eTipY + 10f, widthDp = 74f, text = "PROJECTS", isEyebrow = true)
    OverlayLabel(cx = eTipX, y = eTipY + 22f, widthDp = 74f, text = "$projectsTotal", isMeta = true)

    for (leaf in pLeaves) {
      OverlayLabel(cx = leaf.second, y = leaf.third + 7f, widthDp = 80f, text = leaf.first.label.ifBlank { "—" }, isLeaf = true)
    }
    for (leaf in oLeaves) {
      OverlayLabel(cx = leaf.second, y = leaf.third + 7f, widthDp = 80f, text = leaf.first.label.ifBlank { "—" }, isLeaf = true)
    }
    for (leaf in eLeaves) {
      OverlayLabel(cx = leaf.second, y = leaf.third + 7f, widthDp = 80f, text = leaf.first.label.ifBlank { "—" }, isLeaf = true)
    }
  }
}

@Composable
private fun BoxScope.OverlayLabel(
  cx: Float,
  y: Float,
  widthDp: Float,
  text: String,
  isEyebrow: Boolean = false,
  isHeader: Boolean = false,
  isMeta: Boolean = false,
  isLeaf: Boolean = false,
) {
  Box(
    modifier = Modifier.offset(x = (cx - widthDp / 2f).dp, y = y.dp).width(widthDp.dp),
    contentAlignment = Alignment.TopCenter,
  ) {
    Text(
      text = text.ifBlank { "—" },
      color = when {
        isMeta -> KlikInkTertiary
        isEyebrow && !isHeader -> KlikInkFaint
        else -> KlikInkPrimary
      },
      fontSize = when {
        isHeader -> 14.sp
        isLeaf -> 10.sp
        isEyebrow -> 8.5.sp
        else -> 10.sp
      },
      fontWeight = if (isMeta) FontWeight.Normal else FontWeight.Medium,
      letterSpacing = if (isEyebrow) 1.2.sp else 0.sp,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      textAlign = TextAlign.Center,
    )
  }
}

@Composable
private fun TodayLine(level: Int, xpToNext: Int) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(modifier = Modifier.weight(1f).height(1.dp).background(KlikInkPrimary))
    Spacer(Modifier.width(10.dp))
    Text("TODAY", color = KlikInkPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.4.sp)
    Spacer(Modifier.width(8.dp))
    Text("Lv $level · $xpToNext to next", color = KlikInkTertiary, fontSize = 11.sp)
    Spacer(Modifier.width(10.dp))
    Box(modifier = Modifier.weight(1f).height(1.dp).background(KlikInkPrimary))
  }
}

@Composable
private fun GoalsStrip(goals: List<GrowthTreeFutureNode>) {
  Spacer(Modifier.height(4.dp))
  Row(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Canvas(Modifier.size(6.dp)) {
      drawCircle(KlikInkMuted, 3.dp.toPx(), Offset(size.width / 2f, size.height / 2f))
    }
    Spacer(Modifier.width(8.dp))
    Text("GOALS AHEAD", color = KlikInkPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.4.sp)
    Spacer(Modifier.width(8.dp))
    Text("· ${goals.size}", color = KlikInkTertiary, fontSize = 11.sp)
  }
  Spacer(Modifier.height(8.dp))
  Row(
    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    for (g in goals) {
      Row(
        modifier = Modifier
          .clip(RoundedCornerShape(999.dp))
          .background(KlikPaperCard)
          .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Canvas(modifier = Modifier.size(10.dp)) {
          val c = Offset(size.width / 2f, size.height / 2f)
          drawCircle(KlikInkMuted, 3.dp.toPx(), c, style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 2f))))
        }
        Spacer(Modifier.width(6.dp))
        Text(g.label.ifBlank { "Planned" }, color = KlikInkTertiary, fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
      }
    }
  }
  Spacer(Modifier.height(8.dp))
}

// ==================== VIEW: Heat ====================
//
// Three-column heatmap — People | Projects | Orgs.
// Each row is a calendar month; cell darkness reflects activity volume.
// Achievement months get a rose accent corner dot.
// Future goal months appear below a divider with dashed circles.

@Composable
private fun HeatView(data: GrowthTreeResponse) {
  val nodesByMonthType = remember(data.timelineNodes) {
    val map = mutableMapOf<String, MutableMap<String, Int>>()
    data.timelineNodes
      .filter { !it.isFuture && it.timestamp.isNotBlank() }
      .forEach { node ->
        val month = node.timestamp.take(7)
        val typeMap = map.getOrPut(month) { mutableMapOf() }
        typeMap[node.nodeType] = (typeMap[node.nodeType] ?: 0) + 1
      }
    map
  }

  val achievementMonths = remember(data.achievements) {
    data.achievements.filter { it.earnedAt.isNotBlank() }.map { it.earnedAt.take(7) }.toSet()
  }

  val goalMonths = remember(data.futureProjection) {
    data.futureProjection.mapNotNull { it.targetDate?.takeIf { s -> s.isNotBlank() }?.take(7) }.toSet()
  }

  val allMonths = remember(nodesByMonthType) { nodesByMonthType.keys.sortedDescending() }

  val maxCount = remember(nodesByMonthType) {
    nodesByMonthType.values.flatMap { it.values }.maxOrNull()?.coerceAtLeast(1) ?: 1
  }

  val columnTypes = listOf(TYPE_PERSON, TYPE_PROJECT, TYPE_ORG)
  val columnLabels = listOf("PEOPLE", "PROJECTS", "ORGS")
  val columnAccents = listOf(KlikInkPrimary, KlikCommitmentAccent, KlikDecisionAccent)

  Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).background(KlikPaperApp)) {
    Spacer(Modifier.height(12.dp))

    // Column headers
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
      Spacer(Modifier.width(56.dp))
      columnLabels.forEachIndexed { i, label ->
        Column(
          modifier = Modifier.weight(1f),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Canvas(Modifier.size(6.dp)) {
            drawCircle(columnAccents[i], 3.dp.toPx(), Offset(size.width / 2f, size.height / 2f))
          }
          Spacer(Modifier.height(3.dp))
          Text(label, color = KlikInkFaint, fontSize = 8.5.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.8.sp, textAlign = TextAlign.Center)
        }
      }
    }

    Spacer(Modifier.height(10.dp))

    if (allMonths.isEmpty()) {
      Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
        Text("No data yet", color = KlikInkTertiary, fontSize = 13.sp)
      }
    } else {
      Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
      ) {
        for (month in allMonths) {
          val hasAchievement = month in achievementMonths
          val counts = nodesByMonthType[month] ?: emptyMap()
          HeatRow(month = month, counts = counts, columnTypes = columnTypes, columnAccents = columnAccents, maxCount = maxCount, hasAchievement = hasAchievement)
        }

        if (goalMonths.isNotEmpty()) {
          Spacer(Modifier.height(8.dp))
          HairlineDivider()
          Spacer(Modifier.height(4.dp))
          Text("AHEAD", color = KlikInkFaint, fontSize = 9.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp, modifier = Modifier.padding(start = 60.dp))
          Spacer(Modifier.height(4.dp))
          for (month in goalMonths.sorted()) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = month.heatLabel(),
                color = KlikInkFaint,
                fontSize = 9.sp,
                modifier = Modifier.width(56.dp),
                textAlign = TextAlign.End,
              )
              Spacer(Modifier.width(4.dp))
              columnTypes.forEachIndexed { i, _ ->
                Box(
                  modifier = Modifier.weight(1f).height(24.dp).clip(RoundedCornerShape(4.dp)).background(KlikPaperChip),
                  contentAlignment = Alignment.Center,
                ) {
                  Canvas(Modifier.size(8.dp)) {
                    drawCircle(KlikInkMuted, 3.dp.toPx(), Offset(size.width / 2f, size.height / 2f), style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 2f))))
                  }
                }
                if (i < columnTypes.size - 1) Spacer(Modifier.width(3.dp))
              }
            }
          }
        }
      }
    }

    Spacer(Modifier.height(16.dp))
    HeatLegend(columnAccents)
    Spacer(Modifier.height(28.dp))
  }
}

@Composable
private fun HeatRow(
  month: String,
  counts: Map<String, Int>,
  columnTypes: List<String>,
  columnAccents: List<Color>,
  maxCount: Int,
  hasAchievement: Boolean,
) {
  Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
    Text(
      text = month.heatLabel(),
      color = KlikInkFaint,
      fontSize = 9.sp,
      fontWeight = FontWeight.Medium,
      modifier = Modifier.width(56.dp),
      textAlign = TextAlign.End,
    )
    Spacer(Modifier.width(4.dp))
    columnTypes.forEachIndexed { i, type ->
      val count = counts[type] ?: 0
      val accent = columnAccents[i]
      val alpha = when {
        count == 0 -> 0f
        count <= maxCount * 0.25f -> 0.18f
        count <= maxCount * 0.50f -> 0.40f
        count <= maxCount * 0.75f -> 0.65f
        else -> 0.85f
      }
      val bg = if (count == 0) KlikPaperChip else accent.copy(alpha = alpha)
      Box(
        modifier = Modifier.weight(1f).height(24.dp).clip(RoundedCornerShape(4.dp)).background(bg),
        contentAlignment = Alignment.Center,
      ) {
        if (count > 0) {
          Text(
            text = "$count",
            color = if (alpha > 0.5f) KlikPaperApp else accent,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
          )
        }
        // Achievement indicator — rose dot in top-right corner
        if (hasAchievement && type == TYPE_PERSON) {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopEnd) {
            Canvas(Modifier.size(8.dp).padding(2.dp)) {
              drawCircle(KlikRiskAccent, 2.dp.toPx(), Offset(size.width / 2f, size.height / 2f))
            }
          }
        }
      }
      if (i < columnTypes.size - 1) Spacer(Modifier.width(3.dp))
    }
  }
}

@Composable
private fun HeatLegend(accents: List<Color>) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
    horizontalArrangement = Arrangement.spacedBy(10.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    // Intensity scale
    Text("Less", color = KlikInkFaint, fontSize = 9.sp)
    Spacer(Modifier.width(2.dp))
    for (alpha in listOf(0.15f, 0.35f, 0.60f, 0.85f)) {
      Box(modifier = Modifier.size(width = 14.dp, height = 14.dp).clip(RoundedCornerShape(3.dp)).background(KlikInkPrimary.copy(alpha = alpha)))
    }
    Spacer(Modifier.width(2.dp))
    Text("More", color = KlikInkFaint, fontSize = 9.sp)
    Spacer(Modifier.weight(1f))
    // Achievement dot
    Row(verticalAlignment = Alignment.CenterVertically) {
      Canvas(Modifier.size(8.dp)) {
        drawCircle(KlikRiskAccent, 3.dp.toPx(), Offset(size.width / 2f, size.height / 2f))
      }
      Spacer(Modifier.width(4.dp))
      Text("Milestone", color = KlikInkFaint, fontSize = 9.sp)
    }
  }
}

private fun String.heatLabel(): String {
  val parts = split("-")
  if (parts.size < 2) return take(7)
  val yr = parts[0].takeLast(2)
  val mo = when (parts[1]) {
    "01" -> "JAN"; "02" -> "FEB"; "03" -> "MAR"; "04" -> "APR"
    "05" -> "MAY"; "06" -> "JUN"; "07" -> "JUL"; "08" -> "AUG"
    "09" -> "SEP"; "10" -> "OCT"; "11" -> "NOV"; "12" -> "DEC"
    else -> parts[1]
  }
  return "$mo '$yr"
}
