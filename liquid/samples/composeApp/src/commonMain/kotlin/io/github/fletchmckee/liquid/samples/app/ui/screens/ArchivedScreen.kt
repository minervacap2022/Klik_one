package io.github.fletchmckee.liquid.samples.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.fletchmckee.liquid.liquid
import io.github.fletchmckee.liquid.rememberLiquidState
import io.github.fletchmckee.liquid.samples.app.domain.entity.Meeting
import io.github.fletchmckee.liquid.samples.app.model.TaskMetadata
import io.github.fletchmckee.liquid.samples.app.model.archivedTasksState
import io.github.fletchmckee.liquid.samples.app.model.archivedMeetingsState
import io.github.fletchmckee.liquid.samples.app.model.archivedProjectIdsState
import io.github.fletchmckee.liquid.samples.app.model.archivedPersonIdsState
import io.github.fletchmckee.liquid.samples.app.model.archivedOrganizationIdsState
import io.github.fletchmckee.liquid.samples.app.model.unarchiveTask
import io.github.fletchmckee.liquid.samples.app.model.unarchiveMeeting
import io.github.fletchmckee.liquid.samples.app.model.unarchiveProject
import io.github.fletchmckee.liquid.samples.app.model.unarchivePerson
import io.github.fletchmckee.liquid.samples.app.model.unarchiveOrganization
import io.github.fletchmckee.liquid.samples.app.model.allProjects
import io.github.fletchmckee.liquid.samples.app.model.allPeople
import io.github.fletchmckee.liquid.samples.app.model.allOrganizations
import io.github.fletchmckee.liquid.samples.app.model.permanentlyDeletedIdsState
import io.github.fletchmckee.liquid.samples.app.model.permanentlyDeleteTask
import io.github.fletchmckee.liquid.samples.app.model.permanentlyDeleteMeeting
import io.github.fletchmckee.liquid.samples.app.model.permanentlyDeleteProject
import io.github.fletchmckee.liquid.samples.app.model.permanentlyDeletePerson
import io.github.fletchmckee.liquid.samples.app.model.permanentlyDeleteOrganization
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import io.github.fletchmckee.liquid.samples.app.platform.HapticService
import io.github.fletchmckee.liquid.samples.app.theme.KlikBlack
import io.github.fletchmckee.liquid.samples.app.theme.KlikPrimary
import io.github.fletchmckee.liquid.samples.app.theme.LocalLiquidGlassSettings
import io.github.fletchmckee.liquid.samples.app.theme.montserratFontFamily
import io.github.fletchmckee.liquid.samples.app.ui.icons.Archive
import io.github.fletchmckee.liquid.samples.app.ui.icons.CustomIcons
import io.github.fletchmckee.liquid.samples.app.ui.icons.FolderOpen
import io.github.fletchmckee.liquid.samples.app.platform.OAuthBrowser
import io.github.fletchmckee.liquid.samples.app.di.AppModule

// Archive item types
sealed class ArchivedItem {
    data class Task(val metadata: TaskMetadata) : ArchivedItem()
    data class Session(val meeting: Meeting) : ArchivedItem()
    data class Project(val id: String, val name: String) : ArchivedItem()
    data class Person(val id: String, val name: String) : ArchivedItem()
    data class Organization(val id: String, val name: String) : ArchivedItem()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchivedScreen(
    onBack: () -> Unit = {}
) {
    val liquidState = rememberLiquidState()
    val glassSettings = LocalLiquidGlassSettings.current

    // Use global archive state from Models
    val archivedTasks by archivedTasksState
    val archivedMeetings by archivedMeetingsState
    val archivedProjectIds by archivedProjectIdsState
    val archivedPersonIds by archivedPersonIdsState
    val archivedOrganizationIds by archivedOrganizationIdsState
    val permanentlyDeletedIds by permanentlyDeletedIdsState

    // Filter out permanently deleted items
    val visibleTasks = archivedTasks.filter { it.id !in permanentlyDeletedIds }
    val visibleMeetings = archivedMeetings.filter { it.id !in permanentlyDeletedIds }

    // Look up entity names from data, excluding permanently deleted
    val archivedProjects = archivedProjectIds
        .filter { it !in permanentlyDeletedIds }
        .mapNotNull { id -> allProjects.find { it.id == id }?.let { id to it.name } }
    val archivedPeople = archivedPersonIds
        .filter { it !in permanentlyDeletedIds }
        .mapNotNull { id -> allPeople.find { it.id == id }?.let { id to it.name } }
    val archivedOrganizations = archivedOrganizationIds
        .filter { it !in permanentlyDeletedIds }
        .mapNotNull { id -> allOrganizations.find { it.id == id }?.let { id to it.name } }

    // Delete confirmation dialog state
    var deleteConfirmationItem by remember { mutableStateOf<Pair<String, String>?>(null) } // id to type

    // Unarchive handlers using global functions
    val onUnarchiveTask: (String) -> Unit = { id -> unarchiveTask(id) }
    val onUnarchiveMeeting: (String) -> Unit = { id -> unarchiveMeeting(id) }
    val onUnarchiveProject: (String) -> Unit = { id ->
        unarchiveProject(id)
        AppModule.unarchiveProject(id)
    }
    val onUnarchivePerson: (String) -> Unit = { id ->
        unarchivePerson(id)
        AppModule.unarchivePerson(id)
    }
    val onUnarchiveOrganization: (String) -> Unit = { id ->
        unarchiveOrganization(id)
        AppModule.unarchiveOrganization(id)
    }

    // Delete confirmation dialog
    deleteConfirmationItem?.let { (id, type) ->
        io.github.fletchmckee.liquid.samples.app.ui.components.LiquidGlassDialog(
            onDismissRequest = { deleteConfirmationItem = null },
            title = "Permanently Delete",
            message = "This item will be permanently removed. This cannot be undone.",
            confirmText = "Delete",
            isDestructive = true,
            onConfirm = {
                when (type) {
                    "task" -> permanentlyDeleteTask(id)
                    "meeting" -> permanentlyDeleteMeeting(id)
                    "project" -> permanentlyDeleteProject(id)
                    "person" -> permanentlyDeletePerson(id)
                    "organization" -> permanentlyDeleteOrganization(id)
                }
                deleteConfirmationItem = null
            },
            dismissText = "Cancel"
        )
    }

    // Tab state
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Tasks", "Sessions", "Entities")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 68.dp)
    ) {
        // Header with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = KlikBlack
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Archived",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontFamily = FontFamily.Serif
                ),
                color = KlikBlack
            )
        }

        // Tabs
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.Transparent,
            contentColor = KlikBlack,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = KlikPrimary
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                val count = when (index) {
                    0 -> visibleTasks.size
                    1 -> visibleMeetings.size
                    2 -> archivedProjects.size + archivedPeople.size + archivedOrganizations.size
                    else -> 0
                }
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                            if (count > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .background(KlikPrimary.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        count.toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = KlikPrimary
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Content based on selected tab
        when (selectedTabIndex) {
            0 -> ArchivedTasksList(
                tasks = visibleTasks,
                onUnarchive = onUnarchiveTask,
                onDelete = { id -> deleteConfirmationItem = id to "task" }
            )
            1 -> ArchivedSessionsList(
                meetings = visibleMeetings,
                onUnarchive = onUnarchiveMeeting,
                onDelete = { id -> deleteConfirmationItem = id to "meeting" }
            )
            2 -> ArchivedEntitiesList(
                projects = archivedProjects,
                people = archivedPeople,
                organizations = archivedOrganizations,
                onUnarchiveProject = onUnarchiveProject,
                onUnarchivePerson = onUnarchivePerson,
                onUnarchiveOrganization = onUnarchiveOrganization,
                onDeleteProject = { id -> deleteConfirmationItem = id to "project" },
                onDeletePerson = { id -> deleteConfirmationItem = id to "person" },
                onDeleteOrganization = { id -> deleteConfirmationItem = id to "organization" }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArchivedTasksList(
    tasks: List<TaskMetadata>,
    onUnarchive: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    val liquidState = rememberLiquidState()
    val glassSettings = LocalLiquidGlassSettings.current
    val cardShape = RoundedCornerShape(16.dp)

    if (tasks.isEmpty()) {
        EmptyArchiveState("No archived tasks")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tasks, key = { it.id }) { task ->
                SwipeToUnarchiveCard(
                    onUnarchive = { onUnarchive(task.id) },
                    onDelete = { onDelete(task.id) }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Color.White.copy(alpha = if (glassSettings.applyToCards) glassSettings.transparency else 0.95f),
                                cardShape
                            )
                            .border(BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.1f)), cardShape)
                            .liquid(liquidState) {
                                edge = if (glassSettings.applyToCards) glassSettings.edge else 0.01f
                                shape = cardShape
                                tint = Color.Gray.copy(alpha = 0.05f)
                            }
                            .clip(cardShape)
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    task.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = KlikBlack
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    task.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                            Icon(
                                CustomIcons.Archive,
                                contentDescription = "Archived",
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArchivedSessionsList(
    meetings: List<Meeting>,
    onUnarchive: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    val liquidState = rememberLiquidState()
    val glassSettings = LocalLiquidGlassSettings.current
    val cardShape = RoundedCornerShape(16.dp)

    if (meetings.isEmpty()) {
        EmptyArchiveState("No archived sessions")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(meetings, key = { it.id }) { meeting ->
                SwipeToUnarchiveCard(
                    onUnarchive = { onUnarchive(meeting.id) },
                    onDelete = { onDelete(meeting.id) }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Color.White.copy(alpha = if (glassSettings.applyToCards) glassSettings.transparency else 0.95f),
                                cardShape
                            )
                            .border(BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.1f)), cardShape)
                            .liquid(liquidState) {
                                edge = if (glassSettings.applyToCards) glassSettings.edge else 0.01f
                                shape = cardShape
                                tint = Color.Gray.copy(alpha = 0.05f)
                            }
                            .clip(cardShape)
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    meeting.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = KlikBlack
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "${meeting.date} · ${meeting.time}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                            // Right side: Dropbox link + Archive indicator
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Dropbox link button - only show if URL exists
                                if (!meeting.dropboxUrl.isNullOrBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .clickable {
                                                OAuthBrowser.openUrl(meeting.dropboxUrl)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            CustomIcons.FolderOpen,
                                            contentDescription = "Open in Dropbox",
                                            tint = Color(0xFF0061FF), // Dropbox blue
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Icon(
                                    CustomIcons.Archive,
                                    contentDescription = "Archived",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArchivedEntitiesList(
    projects: List<Pair<String, String>>,
    people: List<Pair<String, String>>,
    organizations: List<Pair<String, String>>,
    onUnarchiveProject: (String) -> Unit,
    onUnarchivePerson: (String) -> Unit,
    onUnarchiveOrganization: (String) -> Unit,
    onDeleteProject: (String) -> Unit,
    onDeletePerson: (String) -> Unit,
    onDeleteOrganization: (String) -> Unit
) {
    val liquidState = rememberLiquidState()
    val glassSettings = LocalLiquidGlassSettings.current
    val cardShape = RoundedCornerShape(16.dp)

    val totalItems = projects.size + people.size + organizations.size

    if (totalItems == 0) {
        EmptyArchiveState("No archived entities")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Projects section
            if (projects.isNotEmpty()) {
                item {
                    Text(
                        "Projects",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = KlikBlack.copy(alpha = 0.6f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(projects, key = { "project_${it.first}" }) { (id, name) ->
                    EntityArchiveCard(
                        name = name,
                        type = "Project",
                        color = Color(0xFF4CAF50),
                        onUnarchive = { onUnarchiveProject(id) },
                        onDelete = { onDeleteProject(id) }
                    )
                }
            }

            // People section
            if (people.isNotEmpty()) {
                item {
                    Text(
                        "People",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = KlikBlack.copy(alpha = 0.6f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(people, key = { "person_${it.first}" }) { (id, name) ->
                    EntityArchiveCard(
                        name = name,
                        type = "Person",
                        color = Color(0xFF2196F3),
                        onUnarchive = { onUnarchivePerson(id) },
                        onDelete = { onDeletePerson(id) }
                    )
                }
            }

            // Organizations section
            if (organizations.isNotEmpty()) {
                item {
                    Text(
                        "Organizations",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = KlikBlack.copy(alpha = 0.6f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(organizations, key = { "org_${it.first}" }) { (id, name) ->
                    EntityArchiveCard(
                        name = name,
                        type = "Organization",
                        color = Color(0xFFFF9800),
                        onUnarchive = { onUnarchiveOrganization(id) },
                        onDelete = { onDeleteOrganization(id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EntityArchiveCard(
    name: String,
    type: String,
    color: Color,
    onUnarchive: () -> Unit,
    onDelete: () -> Unit
) {
    val liquidState = rememberLiquidState()
    val glassSettings = LocalLiquidGlassSettings.current
    val cardShape = RoundedCornerShape(16.dp)

    SwipeToUnarchiveCard(onUnarchive = onUnarchive, onDelete = onDelete) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color.White.copy(alpha = if (glassSettings.applyToCards) glassSettings.transparency else 0.95f),
                    cardShape
                )
                .border(BorderStroke(0.5.dp, color.copy(alpha = 0.2f)), cardShape)
                .liquid(liquidState) {
                    edge = if (glassSettings.applyToCards) glassSettings.edge else 0.01f
                    shape = cardShape
                    tint = color.copy(alpha = 0.05f)
                }
                .clip(cardShape)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Type indicator
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(color, CircleShape)
                    )
                    Column {
                        Text(
                            name,
                            style = MaterialTheme.typography.titleSmall,
                            color = KlikBlack
                        )
                        Text(
                            type,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
                Icon(
                    CustomIcons.Archive,
                    contentDescription = "Archived",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToUnarchiveCard(
    onUnarchive: () -> Unit,
    onDelete: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val liquidState = rememberLiquidState()
    val glassSettings = LocalLiquidGlassSettings.current
    val cardShape = RoundedCornerShape(16.dp)

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    // Swipe right to unarchive
                    HapticService.success()
                    onUnarchive()
                    true
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    // Swipe left to permanently delete (shows confirmation dialog)
                    HapticService.heavyImpact()
                    onDelete()
                    false // Return false so the card snaps back; dialog handles actual deletion
                }
                else -> false
            }
        }
    )

    SwipeToDismissBox(
        modifier = Modifier.fillMaxWidth(),
        state = dismissState,
        backgroundContent = {
            val color = when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> Color(0xFF4CAF50) // Green for unarchive
                SwipeToDismissBoxValue.EndToStart -> Color(0xFFFF3B30) // Red for delete
                else -> Color.Transparent
            }
            val icon = when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> Icons.Filled.Refresh
                SwipeToDismissBoxValue.EndToStart -> Icons.Filled.Delete
                else -> Icons.Filled.Refresh
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .liquid(liquidState) {
                        edge = if (glassSettings.applyToCards) glassSettings.edge else 0.01f
                        shape = cardShape
                        tint = color.copy(alpha = 0.2f)
                    },
                contentAlignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd)
                    Alignment.CenterStart else Alignment.CenterEnd
            ) {
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
                }
            }
        },
        content = {
            Box(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    )
}

@Composable
private fun EmptyArchiveState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                CustomIcons.Archive,
                contentDescription = null,
                tint = Color.Gray.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp)
            )
            Text(
                message,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray.copy(alpha = 0.7f)
            )
            Text(
                "Swipe left on items to archive them",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray.copy(alpha = 0.5f)
            )
        }
    }
}
