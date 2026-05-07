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
import androidx.compose.foundation.layout.heightIn
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
import io.github.fletchmckee.liquid.samples.app.data.source.remote.GrowthTreeEdge
import io.github.fletchmckee.liquid.samples.app.data.source.remote.GrowthTreeFutureNode
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
              0 -> BloomTreeView(treeData!!)
              1 -> RiverTimelineView(treeData!!)
              2 -> ConstellationView(treeData!!)
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
  onBack: (() -> Unit)?,
) {
  val viewLabels = listOf("Bloom", "River", "Map")

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(KlikPaperApp)
      .statusBarsPadding(),
  ) {
    // Top bar — chevron alone when there's somewhere to go back to.
    // When this screen is a tab root (onBack == null) we leave the row in
    // place so the eyebrow + title stay anchored at the same y as elsewhere.
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
      if (onBack != null) {
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
      } else {
        Spacer(Modifier.size(32.dp))
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

// ==================== VIEW B: Bloom Tree ====================
//
// A real 2-D tree.  YOU is the root at the top.  Three Bezier branches
// curve out to PEOPLE / ORGS / PROJECTS primaries; each primary curves
// further to its top entities as leaves.  Faint dashed cross-edges from
// data.edges thread between leaves so the canopy looks connected.
// Every label is a real Compose Text positioned via Modifier.offset —
// no canvas drawText anywhere, so labels never collide or get clipped.

private const val BLOOM_LEAF_COUNT = 4

@Composable
fun BloomTreeView(data: GrowthTreeResponse) {
  val people = remember(data.timelineNodes) {
    val tier = mapOf("S" to 0, "A" to 1, "B" to 2, "C" to 3, "D" to 4)
    data.nodesByType(TYPE_PERSON).sortedBy { tier[it.metaString("influence_tier")] ?: 5 }
  }
  val orgs = data.nodesByType(TYPE_ORG)
  val projects = data.nodesByType(TYPE_PROJECT)
  val futureGoals = data.futureProjection
  val edges = data.edges

  val showPeople = people.take(BLOOM_LEAF_COUNT)
  val showOrgs = orgs.take(BLOOM_LEAF_COUNT)
  val showProjects = projects.take(BLOOM_LEAF_COUNT)

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .background(KlikPaperApp),
  ) {
    Spacer(Modifier.height(8.dp))

    BloomCanvasBlock(
      user = data.user,
      people = showPeople,
      orgs = showOrgs,
      projects = showProjects,
      edges = edges,
      peopleTotal = data.user.totalPeople.coerceAtLeast(people.size),
      orgsTotal = data.user.totalOrgs.coerceAtLeast(orgs.size),
      projectsTotal = data.user.totalProjects.coerceAtLeast(projects.size),
    )

    BloomTodayLine(level = data.user.level, xpToNext = data.user.xpToNextLevel)

    if (futureGoals.isNotEmpty()) {
      BloomGoalsStrip(futureGoals)
    }

    Spacer(Modifier.height(12.dp))
    DescriptionCard(
      title = "Bloom",
      description = "${data.user.displayName}'s tree as it grows. " +
        "${data.user.totalPeople} people · " +
        "${data.user.totalOrgs} orgs · " +
        "${data.user.totalProjects} projects — every leaf connects back to you.",
    )
    Spacer(Modifier.height(28.dp))
  }
}

@Composable
private fun BloomCanvasBlock(
  user: GrowthTreeUserSummary,
  people: List<GrowthTreeNode>,
  orgs: List<GrowthTreeNode>,
  projects: List<GrowthTreeNode>,
  edges: List<GrowthTreeEdge>,
  peopleTotal: Int,
  orgsTotal: Int,
  projectsTotal: Int,
) {
  BoxWithConstraints(
    modifier = Modifier
      .fillMaxWidth()
      .height(660.dp)
      .padding(horizontal = 8.dp),
  ) {
    val widthDp = maxWidth.value
    val rootX = widthDp * 0.50f
    val rootY = 50f
    val priY = 200f
    val pPriX = widthDp * 0.16f
    val oPriX = widthDp * 0.50f
    val ePriX = widthDp * 0.84f
    val leafY0 = 320f
    val leafYStep = 78f

    // Each leaf entry: (node, x, y) in dp coords inside the BoxWithConstraints.
    fun layoutBranch(priX: Float, list: List<GrowthTreeNode>): List<Triple<GrowthTreeNode, Float, Float>> {
      val out = ArrayList<Triple<GrowthTreeNode, Float, Float>>(list.size)
      for (i in 0 until list.size) {
        val jitter = if (i % 2 == 0) -10f else 10f
        out.add(Triple(list[i], priX + jitter, leafY0 + i * leafYStep))
      }
      return out
    }
    val pLeaves = layoutBranch(pPriX, people)
    val oLeaves = layoutBranch(oPriX, orgs)
    val eLeaves = layoutBranch(ePriX, projects)

    val allLeaves: List<Triple<GrowthTreeNode, Float, Float>> = pLeaves + oLeaves + eLeaves
    val byId: Map<String, Triple<GrowthTreeNode, Float, Float>> = run {
      val m = HashMap<String, Triple<GrowthTreeNode, Float, Float>>()
      for (t in allLeaves) m[t.first.id] = t
      m
    }

    Canvas(modifier = Modifier.matchParentSize()) {
      val rootXP = rootX.dp.toPx()
      val rootYP = rootY.dp.toPx()
      val priYP = priY.dp.toPx()

      // Root → primary curves
      val branches = listOf(
        Pair(pPriX, KlikInkPrimary),
        Pair(oPriX, KlikDecisionAccent),
        Pair(ePriX, KlikCommitmentAccent),
      )
      for (b in branches) {
        val targetX = b.first.dp.toPx()
        val path = Path().apply {
          moveTo(rootXP, (rootY + 13f).dp.toPx())
          val midY = ((rootY + priY) / 2f + 14f).dp.toPx()
          quadraticTo(rootXP, midY, targetX, (priY - 8f).dp.toPx())
        }
        drawPath(
          path = path,
          color = KlikInkPrimary,
          style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round),
        )
      }

      // Primary → leaf curves
      fun drawLimb(priX: Float, leaves: List<Triple<GrowthTreeNode, Float, Float>>) {
        val pxStart = priX.dp.toPx()
        val pyStart = (priY + 7f).dp.toPx()
        for (t in leaves) {
          val lx = t.second.dp.toPx()
          val ly = (t.third - 4f).dp.toPx()
          val path = Path().apply {
            moveTo(pxStart, pyStart)
            val ctlX = ((priX + t.second) / 2f).dp.toPx()
            val ctlY = ((priY + t.third) / 2f - 6f).dp.toPx()
            quadraticTo(ctlX, ctlY, lx, ly)
          }
          drawPath(
            path = path,
            color = KlikLineHairline,
            style = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Round),
          )
        }
      }
      drawLimb(pPriX, pLeaves)
      drawLimb(oPriX, oLeaves)
      drawLimb(ePriX, eLeaves)

      // Cross-connections — faint dashed lines between leaves whose ids
      // appear together in data.edges.  Cap at a generous limit so the
      // canopy stays readable rather than turning into a hairball.
      var drawn = 0
      for (edge in edges) {
        if (drawn >= 60) break
        val s = byId[edge.sourceId] ?: continue
        val t = byId[edge.targetId] ?: continue
        if (s.first.id == t.first.id) continue
        drawLine(
          color = KlikInkMuted,
          start = Offset(s.second.dp.toPx(), s.third.dp.toPx()),
          end = Offset(t.second.dp.toPx(), t.third.dp.toPx()),
          strokeWidth = 0.8.dp.toPx(),
          pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 3f)),
        )
        drawn++
      }

      // Root dot — large concentric.
      val rr = 13.dp.toPx()
      drawCircle(KlikInkPrimary, rr, Offset(rootXP, rootYP))
      drawCircle(KlikPaperApp, rr - 3f, Offset(rootXP, rootYP))
      drawCircle(KlikInkPrimary, rr - 6f, Offset(rootXP, rootYP))

      // Primary dots.
      for (b in branches) {
        drawCircle(b.second, 7.dp.toPx(), Offset(b.first.dp.toPx(), priYP))
      }

      // Leaves — accent fills, ringed for S-tier people.
      for (t in allLeaves) {
        val accent = accentForType(t.first.nodeType)
        val isStar = t.first.nodeType == TYPE_PERSON &&
          t.first.metaString("influence_tier") == "S"
        val centre = Offset(t.second.dp.toPx(), t.third.dp.toPx())
        if (isStar) {
          drawCircle(accent, 6.dp.toPx(), centre, style = Stroke(width = 1.dp.toPx()))
        }
        drawCircle(accent, 4.dp.toPx(), centre)
      }
    }

    // === Real Compose Text overlays — placed via Modifier.offset ===

    // Root labels.
    OverlayLabel(cx = rootX, y = rootY + 22f, widthDp = 80f, text = "ROOT", isEyebrow = true)
    OverlayLabel(
      cx = rootX, y = rootY + 36f, widthDp = 140f,
      text = user.displayName,
      isHeader = true,
    )

    // Primary labels.
    OverlayLabel(cx = pPriX, y = priY + 16f, widthDp = 110f, text = "PEOPLE", isEyebrow = true)
    OverlayLabel(
      cx = pPriX, y = priY + 30f, widthDp = 110f,
      text = "$peopleTotal",
      isMeta = true,
    )
    OverlayLabel(cx = oPriX, y = priY + 16f, widthDp = 90f, text = "ORGS", isEyebrow = true)
    OverlayLabel(
      cx = oPriX, y = priY + 30f, widthDp = 90f,
      text = "$orgsTotal",
      isMeta = true,
    )
    OverlayLabel(cx = ePriX, y = priY + 16f, widthDp = 110f, text = "PROJECTS", isEyebrow = true)
    OverlayLabel(
      cx = ePriX, y = priY + 30f, widthDp = 110f,
      text = "$projectsTotal",
      isMeta = true,
    )

    // Leaf labels — short text just below each leaf.
    for (t in allLeaves) {
      OverlayLabel(
        cx = t.second,
        y = t.third + 10f,
        widthDp = 86f,
        text = t.first.label.ifBlank { "—" },
        isLeaf = true,
      )
    }
  }
}

// Place a real Compose Text at (cx, y) inside the surrounding BoxScope, with
// the label centred horizontally on cx and a fixed widthDp so adjacent
// labels never bleed into each other.  Style is selected by flag rather
// than by passing TextUnit values around (keeps the call sites tidy).
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
  val resolvedColor = when {
    isMeta -> KlikInkTertiary
    isEyebrow && !isHeader -> KlikInkFaint
    else -> KlikInkPrimary
  }
  Box(
    modifier = Modifier
      .offset(x = (cx - widthDp / 2f).dp, y = y.dp)
      .width(widthDp.dp),
    contentAlignment = Alignment.TopCenter,
  ) {
    Text(
      text = text.ifBlank { "—" },
      color = resolvedColor,
      fontSize = when {
        isHeader -> 14.sp
        isLeaf -> 11.sp
        isEyebrow -> 9.sp
        else -> 10.sp
      },
      fontWeight = if (isMeta) FontWeight.Normal else FontWeight.Medium,
      letterSpacing = if (isEyebrow) 1.4.sp else 0.sp,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      textAlign = TextAlign.Center,
    )
  }
}

@Composable
private fun BloomTodayLine(level: Int, xpToNext: Int) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(modifier = Modifier.weight(1f).height(1.dp).background(KlikInkPrimary))
    Spacer(Modifier.width(10.dp))
    Text(
      text = "TODAY",
      color = KlikInkPrimary,
      fontSize = 11.sp,
      fontWeight = FontWeight.Medium,
      letterSpacing = 1.4.sp,
    )
    Spacer(Modifier.width(8.dp))
    Text(
      text = "Lv $level · $xpToNext to next",
      color = KlikInkTertiary,
      fontSize = 11.sp,
    )
    Spacer(Modifier.width(10.dp))
    Box(modifier = Modifier.weight(1f).height(1.dp).background(KlikInkPrimary))
  }
}

@Composable
private fun BloomGoalsStrip(goals: List<GrowthTreeFutureNode>) {
  Spacer(Modifier.height(6.dp))
  Row(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
      modifier = Modifier
        .size(6.dp)
        .clip(CircleShape)
        .background(KlikInkMuted),
    )
    Spacer(Modifier.width(8.dp))
    Text(
      text = "GOALS AHEAD",
      color = KlikInkPrimary,
      fontSize = 11.sp,
      fontWeight = FontWeight.Medium,
      letterSpacing = 1.4.sp,
    )
    Spacer(Modifier.width(8.dp))
    Text(text = "· ${goals.size}", color = KlikInkTertiary, fontSize = 11.sp)
  }
  Spacer(Modifier.height(8.dp))
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .horizontalScroll(rememberScrollState())
      .padding(horizontal = 20.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    for (i in 0 until goals.size) {
      val g = goals[i]
      Row(
        modifier = Modifier
          .clip(RoundedCornerShape(999.dp))
          .background(KlikPaperCard)
          .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Canvas(modifier = Modifier.size(10.dp)) {
          val c = Offset(size.width / 2f, size.height / 2f)
          drawCircle(
            color = KlikInkMuted,
            radius = 3.dp.toPx(),
            center = c,
            style = Stroke(
              width = 1.dp.toPx(),
              pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 2f)),
            ),
          )
        }
        Spacer(Modifier.width(6.dp))
        Text(
          text = g.label.ifBlank { "Planned" },
          color = KlikInkTertiary,
          fontSize = 11.sp,
          fontWeight = FontWeight.Medium,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
  Spacer(Modifier.height(8.dp))
}

// ==================== VIEW A: Constellation ====================
//
// At-a-glance map of every entity in the user's growth.  Top: YOU, the
// origin.  Below: editorial sections per type — each section a horizontally
// scrolling row of chips so labels never collide or get truncated by canvas
// drawText.  Closes with TODAY and AHEAD.

@Composable
fun ConstellationView(data: GrowthTreeResponse) {
  val people = data.nodesByType(TYPE_PERSON)
  val orgs = data.nodesByType(TYPE_ORG)
  val projects = data.nodesByType(TYPE_PROJECT)
  val achievements = data.achievements
  val futureNodes = data.futureProjection

  val rankedPeople = remember(people) {
    val tierOrder = mapOf("S" to 0, "A" to 1, "B" to 2, "C" to 3, "D" to 4)
    people.sortedBy { tierOrder[it.metaString("influence_tier")] ?: 5 }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .background(KlikPaperApp),
  ) {
    Spacer(Modifier.height(16.dp))
    ConstellationOriginCard(data)

    ConstellationSection(
      title = "PEOPLE",
      count = data.user.totalPeople.coerceAtLeast(rankedPeople.size),
      visible = rankedPeople.size,
      accent = KlikInkPrimary,
    ) {
      rankedPeople.take(20).forEach { node ->
        val tier = node.metaString("influence_tier")
        ConstellationChip(
          label = node.label.ifBlank { "—" },
          accent = KlikInkPrimary,
          starred = tier == "S",
          subtitle = tier?.let { "tier $it" },
        )
      }
    }

    ConstellationSection(
      title = "ORGANIZATIONS",
      count = data.user.totalOrgs.coerceAtLeast(orgs.size),
      visible = orgs.size,
      accent = KlikDecisionAccent,
    ) {
      orgs.take(16).forEach { node ->
        ConstellationChip(
          label = node.label.ifBlank { "—" },
          accent = KlikDecisionAccent,
          subtitle = node.metaString("role"),
        )
      }
    }

    ConstellationSection(
      title = "PROJECTS",
      count = data.user.totalProjects.coerceAtLeast(projects.size),
      visible = projects.size,
      accent = KlikCommitmentAccent,
    ) {
      projects.take(16).forEach { node ->
        ConstellationChip(
          label = node.label.ifBlank { "—" },
          accent = KlikCommitmentAccent,
          subtitle = node.metaString("status"),
        )
      }
    }

    if (achievements.isNotEmpty()) {
      ConstellationSection(
        title = "MILESTONES",
        count = achievements.size,
        visible = achievements.size,
        accent = KlikRiskAccent,
      ) {
        achievements.forEach { ach ->
          val label = when (ach.milestoneType) {
            "level_up" -> "Level ${ach.levelReached ?: ""}".trim()
            "streak" -> "${ach.streakDays ?: 0}-day streak"
            else -> ach.achievementId.ifBlank { "Milestone" }
          }
          ConstellationChip(
            label = label,
            accent = KlikRiskAccent,
            subtitle = ach.xpEarned?.let { "+$it XP" },
          )
        }
      }
    }

    ConstellationTodayBar(data.user.level, data.user.xpToNextLevel)

    if (futureNodes.isNotEmpty()) {
      ConstellationSection(
        title = "AHEAD",
        count = futureNodes.size,
        visible = futureNodes.size,
        accent = KlikInkMuted,
      ) {
        futureNodes.forEach { fNode ->
          ConstellationChip(
            label = fNode.label.ifBlank { "Planned" },
            accent = KlikInkMuted,
            subtitle = fNode.targetDate?.formatShortDate(),
            dashed = true,
          )
        }
      }
    }

    Spacer(Modifier.height(12.dp))
    DescriptionCard(
      title = "Constellation",
      description = "${data.user.displayName}'s map at a glance — " +
        "${data.user.totalPeople} people, ${data.user.totalOrgs} orgs, " +
        "${data.user.totalProjects} projects.",
    )
    TypeLegend()
    Spacer(Modifier.height(28.dp))
  }
}

// Origin card — the YOU node.
@Composable
private fun ConstellationOriginCard(data: GrowthTreeResponse) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 20.dp, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
      modifier = Modifier.size(36.dp),
      contentAlignment = Alignment.Center,
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = 13.dp.toPx()
        drawCircle(KlikInkPrimary, r, Offset(cx, cy))
        drawCircle(KlikPaperApp, r - 3f, Offset(cx, cy))
        drawCircle(KlikInkPrimary, r - 6f, Offset(cx, cy))
      }
    }
    Spacer(Modifier.width(14.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(
        "YOU",
        color = KlikInkFaint,
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.4.sp,
      )
      Spacer(Modifier.height(2.dp))
      Text(
        text = data.user.displayName.ifBlank { "—" },
        color = KlikInkPrimary,
        fontSize = 20.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = (-0.4).sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Spacer(Modifier.height(2.dp))
      Text(
        text = "Level ${data.user.level} · ${data.user.totalXp} XP",
        color = KlikInkTertiary,
        fontSize = 11.sp,
        maxLines = 1,
      )
    }
  }
  Spacer(Modifier.height(8.dp))
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 20.dp)
      .height(1.dp)
      .background(KlikLineHairline),
  )
}

// A section header followed by a horizontally scrollable row of chips.
@Composable
private fun ConstellationSection(
  title: String,
  count: Int,
  visible: Int,
  accent: Color,
  content: @Composable () -> Unit,
) {
  Spacer(Modifier.height(18.dp))
  Row(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
      modifier = Modifier
        .size(6.dp)
        .clip(CircleShape)
        .background(accent),
    )
    Spacer(Modifier.width(8.dp))
    Text(
      text = title,
      color = KlikInkPrimary,
      fontSize = 11.sp,
      fontWeight = FontWeight.Medium,
      letterSpacing = 1.4.sp,
    )
    Spacer(Modifier.width(8.dp))
    Text(
      text = "· $count",
      color = KlikInkTertiary,
      fontSize = 11.sp,
      fontWeight = FontWeight.Normal,
    )
    Spacer(Modifier.width(10.dp))
    Box(
      modifier = Modifier
        .weight(1f)
        .height(1.dp)
        .background(KlikLineHairline),
    )
  }
  Spacer(Modifier.height(8.dp))
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .horizontalScroll(rememberScrollState())
      .padding(horizontal = 20.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    content()
    if (count > visible) {
      MoreChip(extra = count - visible)
    }
  }
}

@Composable
private fun ConstellationChip(
  label: String,
  accent: Color,
  starred: Boolean = false,
  subtitle: String? = null,
  dashed: Boolean = false,
) {
  Row(
    modifier = Modifier
      .clip(RoundedCornerShape(10.dp))
      .background(KlikPaperCard)
      .padding(start = 10.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Canvas(modifier = Modifier.size(10.dp)) {
      val c = Offset(size.width / 2f, size.height / 2f)
      val r = 3.dp.toPx()
      when {
        dashed -> drawCircle(
          color = accent,
          radius = r,
          center = c,
          style = Stroke(
            width = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 2f)),
          ),
        )
        starred -> {
          drawCircle(accent, r + 1.dp.toPx(), c, style = Stroke(width = 1.dp.toPx()))
          drawCircle(accent, r - 1.dp.toPx(), c)
        }
        else -> drawCircle(accent, r, c)
      }
    }
    Spacer(Modifier.width(8.dp))
    Column {
      Text(
        text = label,
        color = KlikInkPrimary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      if (!subtitle.isNullOrBlank()) {
        Text(
          text = subtitle,
          color = KlikInkTertiary,
          fontSize = 10.sp,
          fontWeight = FontWeight.Normal,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}

@Composable
private fun MoreChip(extra: Int) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(10.dp))
      .background(KlikPaperChip)
      .padding(horizontal = 12.dp, vertical = 8.dp),
  ) {
    Text(
      text = "+$extra more",
      color = KlikInkTertiary,
      fontSize = 11.sp,
      fontWeight = FontWeight.Medium,
    )
  }
}

@Composable
private fun ConstellationTodayBar(level: Int, xpToNext: Int) {
  Spacer(Modifier.height(20.dp))
  Row(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
      modifier = Modifier
        .weight(1f)
        .height(1.dp)
        .background(KlikInkPrimary),
    )
    Spacer(Modifier.width(10.dp))
    Text(
      text = "TODAY",
      color = KlikInkPrimary,
      fontSize = 11.sp,
      fontWeight = FontWeight.Medium,
      letterSpacing = 1.4.sp,
    )
    Spacer(Modifier.width(8.dp))
    Text(
      text = "Lv $level · $xpToNext to next",
      color = KlikInkTertiary,
      fontSize = 11.sp,
    )
    Spacer(Modifier.width(10.dp))
    Box(
      modifier = Modifier
        .weight(1f)
        .height(1.dp)
        .background(KlikInkPrimary),
    )
  }
}

// ==================== VIEW E: River Timeline ====================
//
// A real Compose-text vertical timeline.  Day one at the top, today midway,
// what's ahead at the bottom.  Each moment is a row with: editorial date in
// the gutter, a hairline trunk + accent dot, and a real Text card on the
// right.  No more canvas-painted labels colliding into each other.

private fun riverVerbFor(type: String): String = when (type) {
  TYPE_PERSON -> "MET"
  TYPE_ORG -> "JOINED"
  TYPE_PROJECT -> "STARTED"
  "level_up" -> "LEVELLED UP"
  "streak" -> "STREAK"
  "achievement" -> "EARNED"
  "future" -> "PLANNED"
  else -> "MOMENT"
}

private data class RiverItem(
  val id: String,
  val timestamp: String,
  val monthKey: String,
  val monthLabel: String,
  val dayLabel: String,
  val typeKey: String,
  val verb: String,
  val title: String,
  val subtitle: String?,
  val accent: Color,
  val isFuture: Boolean,
)

private fun String.riverMonthKey(): String {
  val head = this.take(10).split("-")
  return if (head.size >= 2) "${head[0]}-${head[1]}" else this.take(7)
}

private fun String.riverDayLabel(): String {
  val head = this.take(10).split("-")
  return if (head.size >= 3) head[2].trimStart('0').ifEmpty { "1" } else "—"
}

private fun buildRiverItems(data: GrowthTreeResponse): List<RiverItem> {
  val past = mutableListOf<RiverItem>()

  data.timelineNodes
    .filter { !it.isFuture && it.timestamp.isNotBlank() }
    .forEach { node ->
      val ts = node.timestamp
      val type = node.nodeType
      val subtitle = node.metaString("influence_tier")?.let { "tier $it" }
        ?: node.metaString("role")
        ?: node.metaString("status")
      past += RiverItem(
        id = node.id,
        timestamp = ts,
        monthKey = ts.riverMonthKey(),
        monthLabel = ts.formatShortDate(),
        dayLabel = ts.riverDayLabel(),
        typeKey = type,
        verb = riverVerbFor(type),
        title = node.label.ifBlank { "Untitled" },
        subtitle = subtitle,
        accent = accentForType(type),
        isFuture = false,
      )
    }

  data.achievements
    .filter { it.earnedAt.isNotBlank() }
    .forEach { ach ->
      val ts = ach.earnedAt
      val type = ach.milestoneType
      val title = when (type) {
        "level_up" -> "Reached level ${ach.levelReached ?: ""}".trim()
        "streak" -> "${ach.streakDays ?: 0}-day streak"
        else -> ach.achievementId.ifBlank { "Milestone" }
      }
      past += RiverItem(
        id = ach.id,
        timestamp = ts,
        monthKey = ts.riverMonthKey(),
        monthLabel = ts.formatShortDate(),
        dayLabel = ts.riverDayLabel(),
        typeKey = type,
        verb = riverVerbFor(type),
        title = title,
        subtitle = ach.xpEarned?.let { "+$it XP" },
        accent = accentForType(type),
        isFuture = false,
      )
    }

  val sortedPast = past.sortedBy { it.timestamp }

  val future = data.futureProjection.mapNotNull { f ->
    val ts = f.targetDate?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
    RiverItem(
      id = f.id,
      timestamp = ts,
      monthKey = ts.riverMonthKey(),
      monthLabel = ts.formatShortDate(),
      dayLabel = ts.riverDayLabel(),
      typeKey = "future",
      verb = riverVerbFor("future"),
      title = f.label.ifBlank { "Planned" },
      subtitle = f.progressPct?.takeIf { it > 0f }?.let { "${(it * 100).toInt()}% in progress" },
      accent = KlikInkMuted,
      isFuture = true,
    )
  }.sortedBy { it.timestamp }

  return sortedPast + future
}

@Composable
fun RiverTimelineView(data: GrowthTreeResponse) {
  val items = remember(data) { buildRiverItems(data) }
  val past = items.filter { !it.isFuture }
  val future = items.filter { it.isFuture }
  val grouped = past.groupBy { it.monthKey }
  val sortedKeys = grouped.keys.sorted()
  val monthGroups: List<List<RiverItem>> = sortedKeys.map { grouped.getValue(it) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .background(KlikPaperApp),
  ) {
    Spacer(Modifier.height(12.dp))
    RiverOriginRow(data.user)

    for (groupIndex in 0 until monthGroups.size) {
      val monthItems: List<RiverItem> = monthGroups[groupIndex]
      RiverMonthEyebrow(monthItems[0].monthLabel)
      for (index in 0 until monthItems.size) {
        RiverEventRow(
          item = monthItems[index],
          isLastInGroup = index == monthItems.size - 1,
        )
      }
    }

    RiverTodayRow(level = data.user.level, xpToNext = data.user.xpToNextLevel)

    if (future.isNotEmpty()) {
      RiverAheadEyebrow()
      for (index in 0 until future.size) {
        RiverEventRow(
          item = future[index],
          isLastInGroup = index == future.size - 1,
        )
      }
    }

    Spacer(Modifier.height(12.dp))
    DescriptionCard(
      title = "River",
      description = "${data.user.displayName}'s journey, day one to today. " +
        "${past.size} moments downstream" +
        (if (future.isNotEmpty()) " · ${future.size} ahead." else "."),
    )
    TypeLegend()
    Spacer(Modifier.height(28.dp))
  }
}

@Composable
private fun RiverOriginRow(user: GrowthTreeUserSummary) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
    verticalAlignment = Alignment.Top,
  ) {
    Column(
      horizontalAlignment = Alignment.End,
      modifier = Modifier.width(48.dp).padding(top = 4.dp),
    ) {
      Text(
        "DAY",
        color = KlikInkFaint,
        fontSize = 9.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.sp,
      )
      Text(
        "ONE",
        color = KlikInkPrimary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.6.sp,
      )
    }
    Spacer(Modifier.width(12.dp))
    Box(modifier = Modifier.width(20.dp).height(72.dp)) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2f
        drawLine(
          color = KlikLineHairline,
          strokeWidth = 1.dp.toPx(),
          start = Offset(cx, 18.dp.toPx()),
          end = Offset(cx, size.height),
        )
        val cy = 12.dp.toPx()
        val r = 7.dp.toPx()
        drawCircle(KlikInkPrimary, r, Offset(cx, cy))
        drawCircle(KlikPaperApp, r - 2f, Offset(cx, cy))
        drawCircle(KlikInkPrimary, r - 4f, Offset(cx, cy))
      }
    }
    Spacer(Modifier.width(14.dp))
    Column(modifier = Modifier.weight(1f).padding(top = 2.dp)) {
      Text(
        "YOU",
        color = KlikInkFaint,
        fontSize = 9.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.2.sp,
      )
      Spacer(Modifier.height(2.dp))
      Text(
        text = user.displayName.ifBlank { "—" },
        color = KlikInkPrimary,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Spacer(Modifier.height(2.dp))
      val seed = user.accountCreatedAt.takeIf { it.isNotBlank() }?.formatShortDate()
      Text(
        text = "Seeded ${seed ?: "the day you began"} · Lv ${user.level}",
        color = KlikInkTertiary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Normal,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
private fun RiverMonthEyebrow(label: String) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Spacer(Modifier.width(48.dp))
    Spacer(Modifier.width(12.dp))
    Box(modifier = Modifier.width(20.dp).height(28.dp)) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        drawLine(
          color = KlikLineHairline,
          strokeWidth = 1.dp.toPx(),
          start = Offset(size.width / 2f, 0f),
          end = Offset(size.width / 2f, size.height),
        )
      }
    }
    Spacer(Modifier.width(14.dp))
    Text(
      text = label,
      color = KlikInkFaint,
      fontSize = 10.sp,
      fontWeight = FontWeight.Medium,
      letterSpacing = 1.2.sp,
    )
    Spacer(Modifier.width(8.dp))
    Box(
      modifier = Modifier
        .weight(1f)
        .height(1.dp)
        .background(KlikLineHairline),
    )
  }
}

@Composable
private fun RiverEventRow(
  item: RiverItem,
  isLastInGroup: Boolean,
) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
    verticalAlignment = Alignment.Top,
  ) {
    Column(
      horizontalAlignment = Alignment.End,
      modifier = Modifier.width(48.dp).padding(top = 16.dp),
    ) {
      Text(
        text = item.dayLabel,
        color = KlikInkPrimary,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
      )
    }
    Spacer(Modifier.width(12.dp))
    Box(modifier = Modifier.width(20.dp).heightIn(min = 60.dp)) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val dotY = 22.dp.toPx()
        drawLine(
          color = KlikLineHairline,
          strokeWidth = 1.dp.toPx(),
          start = Offset(cx, 0f),
          end = Offset(cx, if (isLastInGroup) dotY + 2f else size.height),
        )
        if (item.isFuture) {
          drawCircle(
            color = KlikInkMuted,
            radius = 4.dp.toPx(),
            center = Offset(cx, dotY),
            style = Stroke(
              width = 1.dp.toPx(),
              pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.5f, 2.5f)),
            ),
          )
        } else {
          drawCircle(
            color = item.accent,
            radius = 3.5.dp.toPx(),
            center = Offset(cx, dotY),
          )
        }
      }
    }
    Spacer(Modifier.width(14.dp))
    Column(
      modifier = Modifier
        .weight(1f)
        .padding(top = 12.dp, bottom = 14.dp),
    ) {
      Text(
        text = item.verb,
        color = item.accent,
        fontSize = 9.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.2.sp,
      )
      Spacer(Modifier.height(2.dp))
      Text(
        text = item.title,
        color = if (item.isFuture) KlikInkTertiary else KlikInkPrimary,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 18.sp,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
      )
      if (!item.subtitle.isNullOrBlank()) {
        Spacer(Modifier.height(2.dp))
        Text(
          text = item.subtitle,
          color = KlikInkTertiary,
          fontSize = 11.sp,
          fontWeight = FontWeight.Normal,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}

@Composable
private fun RiverTodayRow(level: Int, xpToNext: Int) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Spacer(Modifier.width(48.dp))
    Spacer(Modifier.width(12.dp))
    Box(modifier = Modifier.width(20.dp).height(36.dp)) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2f
        drawLine(
          color = KlikLineHairline,
          strokeWidth = 1.dp.toPx(),
          start = Offset(cx, 0f),
          end = Offset(cx, size.height / 2f),
        )
        drawLine(
          color = KlikInkMuted,
          strokeWidth = 1.dp.toPx(),
          start = Offset(cx, size.height / 2f),
          end = Offset(cx, size.height),
          pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 3f)),
        )
        drawCircle(
          color = KlikInkPrimary,
          radius = 5.dp.toPx(),
          center = Offset(cx, size.height / 2f),
        )
      }
    }
    Spacer(Modifier.width(14.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(
        "TODAY",
        color = KlikInkPrimary,
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.4.sp,
      )
      Text(
        "Level $level · $xpToNext XP to next",
        color = KlikInkTertiary,
        fontSize = 11.sp,
      )
    }
  }
}

@Composable
private fun RiverAheadEyebrow() {
  Row(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Spacer(Modifier.width(48.dp))
    Spacer(Modifier.width(12.dp))
    Box(modifier = Modifier.width(20.dp).height(20.dp)) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        drawLine(
          color = KlikInkMuted,
          strokeWidth = 1.dp.toPx(),
          start = Offset(size.width / 2f, 0f),
          end = Offset(size.width / 2f, size.height),
          pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 3f)),
        )
      }
    }
    Spacer(Modifier.width(14.dp))
    Text(
      text = "AHEAD",
      color = KlikInkFaint,
      fontSize = 10.sp,
      fontWeight = FontWeight.Medium,
      letterSpacing = 1.2.sp,
    )
    Spacer(Modifier.width(8.dp))
    Box(
      modifier = Modifier
        .weight(1f)
        .height(1.dp)
        .background(KlikLineMute),
    )
  }
}

// ==================== VIEW F: Connected Network ====================
//
// Network keeps the canvas for dots and edges only — no on-canvas labels,
// because adjacent dots make their labels collide.  Below the diagram we
// show a clean numbered legend so every plotted node is readable.

@Composable
fun ConnectedConstellationView(data: GrowthTreeResponse) {
  val people = data.nodesByType(TYPE_PERSON)
  val orgs = data.nodesByType(TYPE_ORG)
  val projects = data.nodesByType(TYPE_PROJECT)
  val edges = data.edges
  val futureNodes = data.futureProjection

  val rankedPeople = remember(people) {
    val order = mapOf("S" to 0, "A" to 1, "B" to 2, "C" to 3, "D" to 4)
    people.sortedBy { order[it.metaString("influence_tier")] ?: 5 }
  }

  val tier1 = remember(rankedPeople, orgs) { rankedPeople.take(5) + orgs.take(3) }
  val tier2 = remember(projects) { projects.take(6) }
  val plotted = remember(tier1, tier2) { tier1 + tier2 }

  // Relative positions on a 0..1 plane (xR, yR).  YOU sits at top centre;
  // tier1 fans out beneath; tier2 sits in the lower band.
  val positions = remember(tier1, tier2, data.user.userId) {
    val m = mutableMapOf<String, Pair<Float, Float>>()
    m[data.user.userId] = 0.50f to 0.10f
    tier1.forEachIndexed { i, n ->
      val t = if (tier1.size <= 1) 0.5f else i.toFloat() / (tier1.size - 1)
      val x = 0.10f + 0.80f * t
      val y = 0.42f + (i % 2) * 0.08f
      m[n.id] = x to y
    }
    tier2.forEachIndexed { i, n ->
      val t = if (tier2.size <= 1) 0.5f else i.toFloat() / (tier2.size - 1)
      val x = 0.10f + 0.80f * t
      val y = 0.78f + (i % 2) * 0.06f
      m[n.id] = x to y
    }
    m
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .background(KlikPaperApp),
  ) {
    EdgeLegend()

    // Diagram — pure shapes, no text.
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 4.dp)
        .clip(RoundedCornerShape(14.dp))
        .background(KlikPaperSoft),
    ) {
      Canvas(
        modifier = Modifier
          .fillMaxWidth()
          .height(360.dp)
          .padding(20.dp),
      ) {
        val w = size.width
        val h = size.height
        val dotR = 4.dp.toPx()
        val ringR = 6.5.dp.toPx()
        val youR = 9.dp.toPx()

        edges.forEach { edge ->
          val s = positions[edge.sourceId] ?: return@forEach
          val t = positions[edge.targetId] ?: return@forEach
          val dashed = edge.relationshipType in DASHED_REL_TYPES
          drawLine(
            color = KlikLineHairline,
            start = Offset(s.first * w, s.second * h),
            end = Offset(t.first * w, t.second * h),
            strokeWidth = 1.dp.toPx(),
            pathEffect = if (dashed) PathEffect.dashPathEffect(floatArrayOf(5f, 4f)) else null,
          )
        }

        plotted.forEach { node ->
          val p = positions[node.id] ?: return@forEach
          val center = Offset(p.first * w, p.second * h)
          val isStar = node.nodeType == TYPE_PERSON &&
            node.metaString("influence_tier") == "S"
          if (isStar) {
            drawCircle(
              color = KlikInkPrimary,
              radius = ringR,
              center = center,
              style = Stroke(width = 1.dp.toPx()),
            )
          }
          drawCircle(accentForType(node.nodeType), dotR, center)
        }

        val you = positions[data.user.userId]
        if (you != null) {
          val yc = Offset(you.first * w, you.second * h)
          drawCircle(KlikInkPrimary, youR, yc)
          drawCircle(KlikPaperSoft, youR - 2f, yc)
          drawCircle(KlikInkPrimary, youR - 4f, yc)
        }
      }
    }

    // Indexed legend — every plotted node, color-coded, fully readable.
    NetworkLegendSection(
      title = "INNER CIRCLE",
      count = tier1.size,
      accent = KlikInkPrimary,
      nodes = tier1,
    )
    NetworkLegendSection(
      title = "PROJECTS",
      count = tier2.size,
      accent = KlikCommitmentAccent,
      nodes = tier2,
    )

    if (futureNodes.isNotEmpty()) {
      NetworkLegendFutureSection(futureNodes)
    }

    Spacer(Modifier.height(12.dp))
    DescriptionCard(
      title = "Network",
      description = "${data.user.totalRelationships} relationships. " +
        "Solid lines are direct ties; dashed lines are softer signals.",
    )
    TypeLegend()
    Spacer(Modifier.height(28.dp))
  }
}

@Composable
private fun NetworkLegendSection(
  title: String,
  count: Int,
  accent: Color,
  nodes: List<GrowthTreeNode>,
) {
  if (nodes.isEmpty()) return
  Spacer(Modifier.height(14.dp))
  Row(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
      modifier = Modifier
        .size(6.dp)
        .clip(CircleShape)
        .background(accent),
    )
    Spacer(Modifier.width(8.dp))
    Text(
      text = title,
      color = KlikInkPrimary,
      fontSize = 11.sp,
      fontWeight = FontWeight.Medium,
      letterSpacing = 1.4.sp,
    )
    Spacer(Modifier.width(8.dp))
    Text(
      text = "· $count",
      color = KlikInkTertiary,
      fontSize = 11.sp,
    )
    Spacer(Modifier.width(10.dp))
    Box(
      modifier = Modifier
        .weight(1f)
        .height(1.dp)
        .background(KlikLineHairline),
    )
  }
  Spacer(Modifier.height(6.dp))
  Column(modifier = Modifier.fillMaxWidth()) {
    nodes.forEach { node ->
      NetworkNodeRow(node = node)
    }
  }
}

@Composable
private fun NetworkNodeRow(node: GrowthTreeNode) {
  val accent = accentForType(node.nodeType)
  val isStar = node.nodeType == TYPE_PERSON &&
    node.metaString("influence_tier") == "S"
  val subtitle = when (node.nodeType) {
    TYPE_PERSON -> node.metaString("influence_tier")?.let { "tier $it" }
    TYPE_ORG -> node.metaString("role")
    TYPE_PROJECT -> node.metaString("status")
    else -> null
  }
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 20.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Canvas(modifier = Modifier.size(14.dp)) {
      val c = Offset(size.width / 2f, size.height / 2f)
      val r = 4.dp.toPx()
      if (isStar) {
        drawCircle(accent, r + 1.5.dp.toPx(), c, style = Stroke(width = 1.dp.toPx()))
      }
      drawCircle(accent, r, c)
    }
    Spacer(Modifier.width(10.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = node.label.ifBlank { "—" },
        color = KlikInkPrimary,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      if (!subtitle.isNullOrBlank()) {
        Text(
          text = subtitle,
          color = KlikInkTertiary,
          fontSize = 10.sp,
          fontWeight = FontWeight.Normal,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
    Text(
      text = node.nodeType.uppercase(),
      color = KlikInkFaint,
      fontSize = 9.sp,
      fontWeight = FontWeight.Medium,
      letterSpacing = 1.sp,
    )
  }
}

@Composable
private fun NetworkLegendFutureSection(future: List<GrowthTreeFutureNode>) {
  Spacer(Modifier.height(14.dp))
  Row(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
      modifier = Modifier
        .size(6.dp)
        .clip(CircleShape)
        .background(KlikInkMuted),
    )
    Spacer(Modifier.width(8.dp))
    Text(
      "AHEAD",
      color = KlikInkPrimary,
      fontSize = 11.sp,
      fontWeight = FontWeight.Medium,
      letterSpacing = 1.4.sp,
    )
    Spacer(Modifier.width(8.dp))
    Text("· ${future.size}", color = KlikInkTertiary, fontSize = 11.sp)
    Spacer(Modifier.width(10.dp))
    Box(
      modifier = Modifier
        .weight(1f)
        .height(1.dp)
        .background(KlikLineMute),
    )
  }
  Spacer(Modifier.height(6.dp))
  for (fn in future) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Canvas(modifier = Modifier.size(14.dp)) {
        val c = Offset(size.width / 2f, size.height / 2f)
        drawCircle(
          color = KlikInkMuted,
          radius = 4.dp.toPx(),
          center = c,
          style = Stroke(
            width = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 2f)),
          ),
        )
      }
      Spacer(Modifier.width(10.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = fn.label.ifBlank { "Planned" },
          color = KlikInkTertiary,
          fontSize = 13.sp,
          fontWeight = FontWeight.Medium,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        val sub = fn.targetDate?.takeIf { it.isNotBlank() }?.formatShortDate()
        if (!sub.isNullOrBlank()) {
          Text(
            text = sub,
            color = KlikInkFaint,
            fontSize = 10.sp,
            maxLines = 1,
          )
        }
      }
    }
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

