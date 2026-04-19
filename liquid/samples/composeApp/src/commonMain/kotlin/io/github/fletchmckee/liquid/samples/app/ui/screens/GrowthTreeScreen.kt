package io.github.fletchmckee.liquid.samples.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import io.github.fletchmckee.liquid.samples.app.data.source.remote.GrowthTreeResponse
import io.github.fletchmckee.liquid.samples.app.data.source.remote.GrowthTreeNode
import io.github.fletchmckee.liquid.samples.app.data.source.remote.GrowthTreeEdge
import io.github.fletchmckee.liquid.samples.app.data.source.remote.GrowthTreeFutureNode
import io.github.fletchmckee.liquid.samples.app.data.source.remote.GrowthTreeUserSummary
import io.github.fletchmckee.liquid.samples.app.data.source.remote.RemoteDataFetcher
import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import kotlin.random.Random
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


// ==================== Color Palette ====================

private val DarkBg = Color(0xFF020617)
private val DarkBgLight = Color(0xFF0F172A)
private val CyanPrimary = Color(0xFF22D3EE)
private val CyanDark = Color(0xFF0891B2)
private val PurplePrimary = Color(0xFFA78BFA)
private val PurpleDark = Color(0xFF7C3AED)
private val RosePrimary = Color(0xFFFB7185)
private val RoseDark = Color(0xFFE11D48)
private val GreenPrimary = Color(0xFF34D399)
private val GreenDark = Color(0xFF059669)
private val GoldPrimary = Color(0xFFFBBF24)
private val GoldDark = Color(0xFFF59E0B)
private val OrangePrimary = Color(0xFFF97316)
private val OrangeDark = Color(0xFFEA580C)
private val BluePrimary = Color(0xFF3B82F6)
private val LavenderPrimary = Color(0xFFC084FC)
private val LavenderDark = Color(0xFF9333EA)
private val SlateLight = Color(0xFF64748B)
private val SlateDark = Color(0xFF334155)
private val SlateDarker = Color(0xFF1E293B)
private val TextLight = Color(0xFFF1F5F9)
private val TextMuted = Color(0xFF475569)
private val TextDim = Color(0xFF94A3B8)

// Light Theme Colors
private val LightBg = Color(0xFFF8FAFC) // Slate 50
private val GlassWhite = Color.White.copy(alpha = 0.95f)

// Node Core Colors (Pastel/Glassy)
private val NodeCyan = Color(0xFF22D3EE)
private val NodePurple = Color(0xFFA78BFA)
private val NodeRose = Color(0xFFFB7185)
private val NodeGreen = Color(0xFF34D399)
private val NodeGold = Color(0xFFFBBF24)
private val NodeOrange = Color(0xFFFB923C)
private val NodeBlue = Color(0xFF60A5FA)

// Text Colors
private val TextDark = Color(0xFF0F172A) // Slate 900
private val TextSecondary = Color(0xFF64748B) // Slate 500

// ==================== Main Screen ====================

@Composable
fun GrowthTreeScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedView by remember { mutableStateOf(0) } // 0=Constellation, 1=River, 2=Connected
    var treeData by remember { mutableStateOf<GrowthTreeResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Fetch growth tree data
    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null
        try {
            treeData = RemoteDataFetcher.fetchGrowthTree()
            KlikLogger.d("GrowthTreeScreen", "Loaded growth tree: ${treeData?.meta?.totalNodes} nodes, ${treeData?.meta?.totalEdges} edges")
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
            .background(LightBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar with back button and view tabs
            GrowthTreeTopBar(
                selectedView = selectedView,
                onViewChange = { selectedView = it },
                onBack = onBack
            )

            // Content area
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = CyanPrimary,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Loading growth tree...",
                                color = SlateLight,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text(
                                "Failed to load",
                                color = RosePrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                errorMessage ?: "Unknown error",
                                color = SlateLight,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(16.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(CyanPrimary.copy(alpha = 0.15f))
                                    .clickable {
                                        // retry - will re-trigger LaunchedEffect isn't possible from here
                                        // so just show the error
                                    }
                                    .padding(horizontal = 20.dp, vertical = 10.dp)
                            ) {
                                Text("Tap back and try again", color = CyanPrimary, fontSize = 12.sp)
                            }
                        }
                    }
                }
                treeData != null -> {
                    // Stats bar
                    StatsBar(user = treeData!!.user)

                    // View content
                    AnimatedContent(
                        targetState = selectedView,
                        transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
                        label = "ViewTransition"
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

// ==================== Top Bar ====================

@Composable
private fun GrowthTreeTopBar(
    selectedView: Int,
    onViewChange: (Int) -> Unit,
    onBack: () -> Unit
) {
    val viewLabels = listOf("Constellation", "River", "Network")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(GlassWhite)
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextDark
                )
            }

            Text(
                text = "Growth Tree",
                color = TextDark,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }

        // View selector tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            viewLabels.forEachIndexed { index, label ->
                val isSelected = selectedView == index
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isSelected) Brush.linearGradient(listOf(CyanPrimary, PurplePrimary))
                            else Brush.linearGradient(listOf(Color.White, Color.White))
                        )
                        .clickable { onViewChange(index) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Color.White else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ==================== Stats Bar ====================

@Composable
private fun StatsBar(user: GrowthTreeUserSummary) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
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
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.7f))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(value, color = CyanPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(4.dp))
            Text(label, color = TextSecondary, fontSize = 10.sp)
        }
    }
}

// ==================== Helper: Parse Nodes by Type ====================

private fun GrowthTreeResponse.nodesByType(type: String): List<GrowthTreeNode> =
    timelineNodes.filter { it.nodeType == type && !it.isFuture }

private fun GrowthTreeNode.metaString(key: String): String? =
    metadata[key]?.jsonPrimitive?.content

private fun GrowthTreeNode.metaFloat(key: String): Float? =
    metadata[key]?.jsonPrimitive?.floatOrNull

private fun GrowthTreeNode.metaInt(key: String): Int? =
    metadata[key]?.jsonPrimitive?.intOrNull

// ==================== Star Field Helper ====================

private data class StarInfo(
    val x: Float, val y: Float, val radius: Float,
    val baseAlpha: Float, val twinkleSpeed: Float, val twinklePhase: Float
)

private fun generateStars(count: Int, seed: Int = 42): List<StarInfo> {
    val rng = Random(seed)
    return List(count) {
        StarInfo(
            x = rng.nextFloat(),
            y = rng.nextFloat(),
            radius = rng.nextFloat() * 1.5f + 0.5f,
            baseAlpha = rng.nextFloat() * 0.4f + 0.1f,
            twinkleSpeed = rng.nextFloat() * 4f + 2f,
            twinklePhase = rng.nextFloat() * PI.toFloat() * 2f
        )
    }
}

// ==================== VIEW A: Constellation ====================

// ==================== VIEW A: Constellation ====================

@Composable
fun ConstellationView(data: GrowthTreeResponse) {
    val textMeasurer = rememberTextMeasurer()
    val infiniteTransition = rememberInfiniteTransition(label = "constellation")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = PI.toFloat() * 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // Subtle background particles instead of bright stars
    val stars = remember { generateStars(80, seed = 7) }

    // Collect nodes
    val people = data.nodesByType("person")
    val orgs = data.nodesByType("organization")
    val projects = data.nodesByType("project")
    val achievements = data.achievements
    val futureNodes = data.futureProjection

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Canvas for constellation
        val canvasHeight = (720 + futureNodes.size * 60).coerceAtLeast(780)

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(canvasHeight.dp)
        ) {
            val w = size.width
            val h = size.height

            // Background (Light Gradient)
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, LightBg),
                    center = Offset(w * 0.5f, h * 0.15f),
                    radius = w * 1.5f
                )
            )

            // Dust Particles
            stars.forEach { star ->
                val twinkle = (sin(time * star.twinkleSpeed + star.twinklePhase) * 0.5f + 0.5f)
                val alpha = star.baseAlpha * (0.1f + twinkle * 0.2f) // Very subtle
                drawCircle(
                    color = NodeBlue.copy(alpha = alpha),
                    radius = star.radius,
                    center = Offset(star.x * w, star.y * h)
                )
            }

            // Layout zones
            val topPadding = 60f
            val todayY = h * 0.72f
            val futureStartY = todayY + 30f
            val pastHeight = todayY - topPadding

            // User node at top center
            drawUserNode(
                center = Offset(w * 0.5f, topPadding + 20f),
                radius = 36f,
                label = data.user.displayName,
                subLabel = "Lv ${data.user.level}",
                textMeasurer = textMeasurer,
                pulsePhase = time
            )

            // Time labels
            val timeLabels = buildConstellationTimeLabels(data)
            timeLabels.forEachIndexed { index, (label, yFraction) ->
                val ly = topPadding + pastHeight * yFraction
                val isToday = label == "TODAY"
                drawText(
                    textMeasurer = textMeasurer,
                    text = label,
                    topLeft = Offset(12f, ly - 6f),
                    style = TextStyle(
                        color = if (isToday) NodeGold else TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.SemiBold
                    )
                )
            }

            // Organization nodes (Rose)
            orgs.forEachIndexed { index, node ->
                val yFraction = (index + 1).toFloat() / (orgs.size + 2).toFloat() * 0.4f + 0.1f
                val xFraction = 0.6f + (index % 3) * 0.12f
                val nodeY = topPadding + pastHeight * yFraction
                val nodeX = w * xFraction
                drawEntityNode(
                    center = Offset(nodeX, nodeY),
                    radius = 20f,
                    color1 = NodeRose,
                    color2 = Color.Transparent,
                    label = node.label.take(10),
                    textMeasurer = textMeasurer
                )
            }

            // Project nodes (Green)
            projects.forEachIndexed { index, node ->
                val yFraction = (index + 1).toFloat() / (projects.size + 2).toFloat() * 0.5f + 0.15f
                val xFraction = 0.3f + (index % 4) * 0.1f
                val nodeY = topPadding + pastHeight * yFraction
                val nodeX = w * xFraction
                drawEntityNode(
                    center = Offset(nodeX, nodeY),
                    radius = 22f,
                    color1 = NodeGreen,
                    color2 = Color.Transparent,
                    label = node.label.take(10),
                    textMeasurer = textMeasurer
                )
            }

            // People nodes (Purple/Gold)
            val topPeople = people.take(20)
            topPeople.forEachIndexed { index, node ->
                val tier = node.metaString("influence_tier") ?: "C"
                val isStar = tier == "S"
                val radius = if (isStar) 27f else 20f
                val yFraction = 0.45f + (index / topPeople.size.toFloat()) * 0.25f
                val xFraction = if (isStar) 0.5f else (0.15f + (index % 5) * 0.15f + (index * 0.03f)) % 0.85f
                val nodeY = topPadding + pastHeight * yFraction
                val nodeX = w * xFraction.coerceIn(0.1f, 0.9f)

                drawEntityNode(
                    center = Offset(nodeX, nodeY),
                    radius = radius,
                    color1 = if (isStar) NodeGold else NodePurple,
                    color2 = Color.Transparent,
                    label = node.label.take(8),
                    textMeasurer = textMeasurer,
                    glow = isStar,
                    glowPhase = time
                )
            }

            // Achievements (Lavender/Orange)
            achievements.forEachIndexed { index, ach ->
                val yFraction = 0.55f + index * 0.04f
                val xFraction = 0.15f + (index % 4) * 0.2f
                val nodeY = topPadding + pastHeight * yFraction
                val nodeX = w * xFraction
                val isLevelUp = ach.milestoneType == "level_up"
                val label = when (ach.milestoneType) {
                    "level_up" -> "Lv ${ach.levelReached ?: ""}"
                    "streak" -> "${ach.streakDays}d"
                    else -> ach.achievementId.replace("_", "\n").take(12)
                }
                drawEntityNode(
                    center = Offset(nodeX, nodeY),
                    radius = 18f,
                    color1 = if (isLevelUp) NodeCyan else NodeOrange,
                    color2 = Color.Transparent,
                    label = label,
                    textMeasurer = textMeasurer
                )
            }

            // TODAY Line (Gold Gradient)
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, NodeGold, Color.Transparent),
                    startX = w * 0.1f,
                    endX = w * 0.9f
                ),
                start = Offset(w * 0.1f, todayY),
                end = Offset(w * 0.9f, todayY),
                strokeWidth = 2f
            )
            
            drawText(
                textMeasurer = textMeasurer,
                text = "TODAY",
                topLeft = Offset(w * 0.1f, todayY - 18f),
                style = TextStyle(color = NodeGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            )

            // Future nodes
            futureNodes.forEachIndexed { index, fNode ->
                val nodeY = futureStartY + 50f + index * 60f
                val nodeX = w * (0.3f + (index % 2) * 0.4f)
                drawFutureNode(
                    center = Offset(nodeX, nodeY),
                    radius = 24f,
                    label = fNode.label.take(12),
                    textMeasurer = textMeasurer
                )
            }
        }

        // Description
        DescriptionCard(
            title = "Constellation",
            description = "${data.user.displayName}'s journey mapped as a network of growth. " +
                "${data.user.totalSessions} sessions | ${data.user.totalPeople} people | " +
                "${data.user.totalProjects} projects"
        )
    }
}

// ==================== VIEW E: River Timeline ====================

@Composable
fun RiverTimelineView(data: GrowthTreeResponse) {
    val textMeasurer = rememberTextMeasurer()
    val infiniteTransition = rememberInfiniteTransition(label = "river")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = PI.toFloat() * 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "riverTime"
    )

    val people = data.nodesByType("person")
    val orgs = data.nodesByType("organization")
    val projects = data.nodesByType("project")
    val achievements = data.achievements
    val futureNodes = data.futureProjection

    // Build timeline events (same logic, just sorting)
    data class RiverEvent(
        val label: String, val type: String, val yFraction: Float,
        val side: String,
        val tier: String = "", val isStar: Boolean = false
    )

    val events = remember(data) {
        val list = mutableListOf<RiverEvent>()
        // Simple linear distribution for demo
        val totalItems = orgs.size + projects.size + people.size + achievements.size
        // ... (Using same logic as before but mapped to new structure if needed)
        // For brevity preserving the logic:
         list.addAll(orgs.mapIndexed { i, n -> RiverEvent(n.label, "organization", (i+1f)/totalItems*0.8f, "left") })
         list.addAll(people.take(15).mapIndexed { i, n -> RiverEvent(n.label, "person", (orgs.size+i+1f)/totalItems*0.8f, "left", tier=n.metaString("influence_tier")?:"C") })
         list.addAll(projects.take(10).mapIndexed { i, n -> RiverEvent(n.label, "project", (i+1f)/totalItems*0.8f, "right") })
         list.addAll(achievements.mapIndexed { i, n -> RiverEvent(n.achievementId, n.milestoneType, (projects.size+i+1f)/totalItems*0.8f, "right") })
        list.sortedBy { it.yFraction }
    }

    val scrollState = rememberScrollState()
    val canvasHeight = (960 + futureNodes.size * 50).coerceAtLeast(960)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(canvasHeight.dp)
        ) {
            val w = size.width
            val h = size.height
            val centerX = w * 0.5f

            // Background
            drawRect(Brush.verticalGradient(listOf(LightBg, Color.White, LightBg)))

            // Trunk (Liquid Gradient)
            val trunkGradient = Brush.verticalGradient(
                colors = listOf(NodeBlue.copy(alpha = 0.2f), NodeCyan.copy(alpha = 0.3f), NodePurple.copy(alpha = 0.2f)),
                startY = 0f, endY = h
            )
            
            // Draw Trunk
            drawLine(
                brush = trunkGradient,
                start = Offset(centerX, 24f),
                end = Offset(centerX, h - 24f),
                strokeWidth = 12f,
                cap = StrokeCap.Round
            )
            
            // TODAY line
            val todayY = h * 0.73f
            drawLine(
                color = NodeGold,
                start = Offset(w * 0.15f, todayY),
                end = Offset(w * 0.85f, todayY),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
            )

            // Draw Events
            events.forEachIndexed { index, event ->
                val yPos = 60f + (todayY - 100f) * event.yFraction
                val isRight = event.side == "right"
                val color = when(event.type) {
                    "person" -> NodePurple
                    "organization" -> NodeRose
                    "project" -> NodeGreen
                    else -> NodeOrange
                }
                
                // Curve logic
                val startX = centerX
                val endX = if (isRight) centerX + 60f else centerX - 60f
                val controlX = if (isRight) centerX + 30f else centerX - 30f
                
                // Connection Line
                drawPath(
                    path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(centerX, yPos)
                        cubicTo(controlX, yPos, controlX, yPos, endX, yPos)
                    },
                    color = color.copy(alpha = 0.5f),
                    style = Stroke(width = 2f)
                )
                
                // Content Pill
                val pillW = 120f
                val pillH = 24f
                val pillX = if (isRight) endX else endX - pillW
                
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.8f),
                    topLeft = Offset(pillX, yPos - pillH/2),
                    size = Size(pillW, pillH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f),
                    style = androidx.compose.ui.graphics.drawscope.Fill
                )
                drawRoundRect(
                    color = color.copy(alpha = 0.3f),
                    topLeft = Offset(pillX, yPos - pillH/2),
                    size = Size(pillW, pillH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f),
                    style = Stroke(width = 1f)
                )
                
                // Text
                drawText(
                    textMeasurer = textMeasurer,
                    text = event.label.take(15),
                    topLeft = Offset(pillX + 10f, yPos - 6f),
                    style = TextStyle(color = TextDark, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                )
                
                // Dot
                drawCircle(color = color, radius = 4f, center = Offset(centerX, yPos))
            }
        }
    }
}

// ==================== VIEW F: Connected Constellation ====================

@Composable
fun ConnectedConstellationView(data: GrowthTreeResponse) {
    val textMeasurer = rememberTextMeasurer()
    val infiniteTransition = rememberInfiniteTransition(label = "connected")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = PI.toFloat() * 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "connectedTime"
    )

    // Dust particles
    val stars = remember { generateStars(60, seed = 13) }

    val people = data.nodesByType("person")
    val orgs = data.nodesByType("organization")
    val projects = data.nodesByType("project")
    val edges = data.edges
    val futureNodes = data.futureProjection

    // Create node position map for edge drawing
    data class NodePos(val id: String, val x: Float, val y: Float, val type: String)

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Legend
        ConnectedLegend()

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(780.dp)
        ) {
            val w = size.width
            val h = size.height

            // Background (Light Gradient)
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, LightBg),
                    center = Offset(w * 0.5f, h * 0.3f),
                    radius = w * 1.5f
                )
            )

            // Dust Particles
            stars.forEach { star ->
                val twinkle = (sin(time * star.twinkleSpeed + star.twinklePhase) * 0.5f + 0.5f)
                val alpha = star.baseAlpha * (0.1f + twinkle * 0.2f)
                drawCircle(
                    color = NodeBlue.copy(alpha = alpha),
                    radius = star.radius,
                    center = Offset(star.x * w, star.y * h)
                )
            }

            // Build node positions for 3-tier layout
            val nodePositions = mutableMapOf<String, NodePos>()

            // User
            val userY = 80f
            val userX = w * 0.5f
            nodePositions[data.user.userId] = NodePos(data.user.userId, userX, userY, "user")

            // Tier 1: Top People & Orgs
            val tier1Nodes = (people.take(6) + orgs.take(3)).shuffled(Random(1))
            tier1Nodes.forEachIndexed { i, node ->
                val angle = PI.toFloat() + (i.toFloat() / tier1Nodes.size) * PI.toFloat() // Arc layout
                val rad = w * 0.35f
                val x = w * 0.5f + cos(angle) * rad
                val y = userY + 120f + sin(angle) * 60f + (i % 2) * 30f
                nodePositions[node.id] = NodePos(node.id, x, y, node.nodeType)
            }

            // Tier 2: Projects & Others
            val tier2Nodes = (projects.take(6) + people.drop(6).take(4)).shuffled(Random(2))
            tier2Nodes.forEachIndexed { i, node ->
                val x = w * (0.15f + (i.toFloat() / tier2Nodes.size) * 0.7f)
                val y = userY + 300f + (i % 2) * 60f
                nodePositions[node.id] = NodePos(node.id, x, y, node.nodeType)
            }

            // Draw Edges (Glass Lines)
            edges.forEach { edge ->
                val start = nodePositions[edge.sourceId]
                val end = nodePositions[edge.targetId]
                if (start != null && end != null) {
                    val color = when (edge.relationshipType) {
                        "collab" -> NodePurple
                        "works_at" -> NodeBlue
                        "involved_in" -> NodeGreen
                        "owns" -> NodeGold
                        "leads" -> NodeOrange
                        else -> TextSecondary
                    }
                    val isDashed = edge.relationshipType in listOf("collab", "involved_in", "leads")
                    
                    drawLine(
                        color = color.copy(alpha = 0.4f),
                        start = Offset(start.x, start.y),
                        end = Offset(end.x, end.y),
                        strokeWidth = 1.5f,
                        pathEffect = if (isDashed) PathEffect.dashPathEffect(floatArrayOf(5f, 5f)) else null
                    )
                }
            }

            // Draw Nodes
            
            // Tier 2
            tier2Nodes.forEach { node ->
                val pos = nodePositions[node.id] ?: return@forEach
                val color = if (node.nodeType == "project") NodeGreen else NodePurple
                drawEntityNode(
                    center = Offset(pos.x, pos.y),
                    radius = 20f,
                    color1 = color,
                    color2 = Color.Transparent,
                    label = node.label.take(10),
                    textMeasurer = textMeasurer
                )
            }

            // Tier 1
            tier1Nodes.forEach { node ->
                val pos = nodePositions[node.id] ?: return@forEach
                val isOrg = node.nodeType == "organization"
                drawEntityNode(
                    center = Offset(pos.x, pos.y),
                    radius = 24f,
                    color1 = if (isOrg) NodeRose else NodePurple,
                    color2 = Color.Transparent,
                    label = node.label.take(10),
                    textMeasurer = textMeasurer,
                    glow = !isOrg,
                    glowPhase = time
                )
            }

            // User Node
            drawUserNode(
                center = Offset(userX, userY),
                radius = 32f,
                label = "YOU",
                subLabel = "",
                textMeasurer = textMeasurer,
                pulsePhase = time
            )

            // FUTURE divider
            val futureY = 620f
            drawLine(
                color = NodeGold.copy(alpha = 0.5f),
                start = Offset(w * 0.1f, futureY),
                end = Offset(w * 0.9f, futureY),
                strokeWidth = 1.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))
            )
            drawText(
                textMeasurer = textMeasurer,
                text = "FUTURE",
                topLeft = Offset(w * 0.5f - 20f, futureY - 16f),
                style = TextStyle(color = NodeGold.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            )

            // Future nodes
            futureNodes.forEachIndexed { i, fNode ->
                val xPos = w * (0.2f + (i.toFloat() / futureNodes.size.coerceAtLeast(1)) * 0.6f)
                val yPos = futureY + 50f + i * 40f
                drawFutureNode(
                    center = Offset(xPos, yPos.coerceAtMost(h - 30f)),
                    radius = 20f,
                    label = fNode.label.take(12),
                    textMeasurer = textMeasurer
                )
            }
        }

        DescriptionCard(
            title = "Connected Constellation",
            description = "${data.user.totalRelationships} relationships visualized. " +
                "Line colors show connection types between people, orgs, and projects."
        )
    }
}

// ==================== Legend for Connected View ====================

@Composable
private fun ConnectedLegend() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        LegendItem("collab", NodePurple, dashed = true)
        LegendItem("works_at", NodeBlue, dashed = false)
        LegendItem("involved_in", NodeGreen, dashed = true)
        LegendItem("owns", NodeGold, dashed = false)
        LegendItem("leads", NodeOrange, dashed = true)
    }
}

@Composable
private fun LegendItem(label: String, color: Color, dashed: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.6f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Canvas(modifier = Modifier.size(width = 24.dp, height = 2.dp)) {
            drawLine(
                color = color,
                start = Offset(0f, size.height / 2),
                end = Offset(size.width, size.height / 2),
                strokeWidth = 2f,
                pathEffect = if (dashed) PathEffect.dashPathEffect(floatArrayOf(6f, 4f)) else null
            )
        }
        Spacer(Modifier.width(5.dp))
        Text(label, color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ==================== Description Card ====================

@Composable
fun DescriptionCard(title: String, description: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.6f))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = title,
                color = NodeBlue,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = description,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}

// ==================== Canvas Drawing Helpers ====================

private fun DrawScope.drawUserNode(
    center: Offset,
    radius: Float,
    label: String,
    subLabel: String,
    textMeasurer: TextMeasurer,
    pulsePhase: Float
) {
    // Pulse glow
    val pulseRadius = radius + 6f + sin(pulsePhase) * 4f
    val pulseAlpha = 0.1f + sin(pulsePhase) * 0.05f
    drawCircle(
        color = NodeCyan.copy(alpha = pulseAlpha),
        radius = pulseRadius,
        center = center
    )

    // Glass Orb Body
    drawCircle(
        color = Color.White,
        radius = radius,
        center = center,
        style = androidx.compose.ui.graphics.drawscope.Fill
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(NodeCyan.copy(alpha = 0.2f), NodeCyan.copy(alpha = 0.8f)),
            center = center.copy(y = center.y - radius * 0.3f),
            radius = radius
        ),
        radius = radius,
        center = center
    )
    
    // Glass Border
    drawCircle(
        color = Color.White.copy(alpha = 0.9f),
        radius = radius,
        center = center,
        style = Stroke(width = 2f)
    )

    // Label
    drawText(
        textMeasurer = textMeasurer,
        text = label.take(12),
        topLeft = Offset(center.x - radius + 4f, center.y - 8f),
        style = TextStyle(
            color = TextDark,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        ),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
    // Sub-label
    if (subLabel.isNotEmpty()) {
        drawText(
            textMeasurer = textMeasurer,
            text = subLabel.take(16),
            topLeft = Offset(center.x - radius + 4f, center.y + 4f),
            style = TextStyle(
                color = TextSecondary,
                fontSize = 8.sp,
                fontWeight = FontWeight.Medium
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun DrawScope.drawEntityNode(
    center: Offset,
    radius: Float,
    color1: Color,
    color2: Color,
    label: String,
    textMeasurer: TextMeasurer,
    glow: Boolean = false,
    glowPhase: Float = 0f
) {
    // Glow effect
    if (glow) {
        val glowRadius = radius + 8f + sin(glowPhase * 1.5f) * 4f
        val glowAlpha = 0.15f + sin(glowPhase) * 0.05f
        drawCircle(
            color = color1.copy(alpha = glowAlpha),
            radius = glowRadius,
            center = center
        )
    }

    // Shadow
    drawCircle(
        color = Color.Black.copy(alpha = 0.05f),
        radius = radius + 2f,
        center = center.copy(y = center.y + 2f)
    )

    // Glass Orb Body
    drawCircle(
        color = Color.White,
        radius = radius,
        center = center
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color1.copy(alpha = 0.3f), color1),
            center = center.copy(y = center.y - radius * 0.4f),
            radius = radius * 1.2f
        ),
        radius = radius,
        center = center
    )
    
    // Highlight (Reflection)
    drawCircle(
        color = Color.White.copy(alpha = 0.4f),
        radius = radius * 0.6f,
        center = center.copy(x = center.x - radius * 0.3f, y = center.y - radius * 0.3f)
    )

    // Glass Border
    drawCircle(
        color = Color.White,
        radius = radius,
        center = center,
        style = Stroke(width = 1.5f)
    )

    // Label text
    drawText(
        textMeasurer = textMeasurer,
        text = label,
        topLeft = Offset(center.x - radius + 2f, center.y - 5f),
        style = TextStyle(
            color = TextDark.copy(alpha = 0.8f),
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold
        ),
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}

private fun DrawScope.drawFutureNode(
    center: Offset,
    radius: Float,
    label: String,
    textMeasurer: TextMeasurer
) {
    // Dashed circle border
    drawCircle(
        color = Color.White.copy(alpha = 0.5f),
        radius = radius,
        center = center
    )
    drawCircle(
        color = TextSecondary.copy(alpha = 0.3f),
        radius = radius,
        center = center,
        style = Stroke(
            width = 1.5f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
        )
    )
    // Label
    drawText(
        textMeasurer = textMeasurer,
        text = label,
        topLeft = Offset(center.x - radius + 4f, center.y - 5f),
        style = TextStyle(
            color = TextSecondary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium
        ),
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}

// ==================== Time Label Helpers ====================

private fun buildConstellationTimeLabels(data: GrowthTreeResponse): List<Pair<String, Float>> {
    val labels = mutableListOf<Pair<String, Float>>()
    // Use meta dates if available
    labels.add(data.meta.fromDate.formatShortDate() to 0.05f)
    labels.add("TODAY" to 0.72f)
    if (data.futureProjection.isNotEmpty()) {
        labels.add(data.meta.toDate.formatShortDate() to 0.95f)
    }
    return labels
}

private fun String.formatShortDate(): String {
    // Parse "2025-12-11" or similar date string to "DEC '25" format
    val parts = this.split("-")
    if (parts.size < 2) return this.take(8)
    val year = parts[0].takeLast(2)
    val month = when (parts[1]) {
        "01" -> "JAN"; "02" -> "FEB"; "03" -> "MAR"; "04" -> "APR"
        "05" -> "MAY"; "06" -> "JUN"; "07" -> "JUL"; "08" -> "AUG"
        "09" -> "SEP"; "10" -> "OCT"; "11" -> "NOV"; "12" -> "DEC"
        else -> parts[1]
    }
    return "$month '$year"
}
