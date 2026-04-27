// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import io.github.fletchmckee.liquid.samples.app.di.AppModule
import io.github.fletchmckee.liquid.samples.app.domain.entity.Meeting
import io.github.fletchmckee.liquid.samples.app.model.TaskMetadata
import io.github.fletchmckee.liquid.samples.app.model.allOrganizations
import io.github.fletchmckee.liquid.samples.app.model.allPeople
import io.github.fletchmckee.liquid.samples.app.model.allProjects
import io.github.fletchmckee.liquid.samples.app.model.archivedMeetingsState
import io.github.fletchmckee.liquid.samples.app.model.archivedOrganizationIdsState
import io.github.fletchmckee.liquid.samples.app.model.archivedPersonIdsState
import io.github.fletchmckee.liquid.samples.app.model.archivedProjectIdsState
import io.github.fletchmckee.liquid.samples.app.model.archivedTasksState
import io.github.fletchmckee.liquid.samples.app.model.permanentlyDeleteMeeting
import io.github.fletchmckee.liquid.samples.app.model.permanentlyDeleteOrganization
import io.github.fletchmckee.liquid.samples.app.model.permanentlyDeletePerson
import io.github.fletchmckee.liquid.samples.app.model.permanentlyDeleteProject
import io.github.fletchmckee.liquid.samples.app.model.permanentlyDeleteTask
import io.github.fletchmckee.liquid.samples.app.model.permanentlyDeletedIdsState
import io.github.fletchmckee.liquid.samples.app.model.unarchiveMeeting
import io.github.fletchmckee.liquid.samples.app.model.unarchiveOrganization
import io.github.fletchmckee.liquid.samples.app.model.unarchivePerson
import io.github.fletchmckee.liquid.samples.app.model.unarchiveProject
import io.github.fletchmckee.liquid.samples.app.model.unarchiveTask
import io.github.fletchmckee.liquid.samples.app.platform.HapticService
import io.github.fletchmckee.liquid.samples.app.platform.OAuthBrowser
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkMuted
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkPrimary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkSecondary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkTertiary
import io.github.fletchmckee.liquid.samples.app.theme.KlikLineHairline
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperApp
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperCard
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperSoft
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1ButtonPrimary
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1Chip
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1Eyebrow
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1Sp
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1Type
import io.github.fletchmckee.liquid.samples.app.ui.klikone.k1Clickable

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
  onBack: () -> Unit = {},
) {
  val archivedTasks by archivedTasksState
  val archivedMeetings by archivedMeetingsState
  val archivedProjectIds by archivedProjectIdsState
  val archivedPersonIds by archivedPersonIdsState
  val archivedOrganizationIds by archivedOrganizationIdsState
  val permanentlyDeletedIds by permanentlyDeletedIdsState

  val visibleTasks = archivedTasks.filter { it.id !in permanentlyDeletedIds }
  val visibleMeetings = archivedMeetings.filter { it.id !in permanentlyDeletedIds }

  val archivedProjects = archivedProjectIds
    .filter { it !in permanentlyDeletedIds }
    .mapNotNull { id -> allProjects.find { it.id == id }?.let { id to it.name } }
  val archivedPeople = archivedPersonIds
    .filter { it !in permanentlyDeletedIds }
    .mapNotNull { id -> allPeople.find { it.id == id }?.let { id to it.name } }
  val archivedOrganizations = archivedOrganizationIds
    .filter { it !in permanentlyDeletedIds }
    .mapNotNull { id -> allOrganizations.find { it.id == id }?.let { id to it.name } }

  var deleteConfirmationItem by remember { mutableStateOf<Pair<String, String>?>(null) }

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

  var filter by remember { mutableStateOf("tasks") }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(KlikPaperApp)
      .statusBarsPadding()
      .navigationBarsPadding(),
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
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
        Text("Archived.", style = K1Type.display)
        Spacer(Modifier.height(K1Sp.m))
        Text(
          "Swipe right on any card to restore. Swipe left to delete for good.",
          style = K1Type.bodySm.copy(color = KlikInkSecondary),
        )
      }

      Spacer(Modifier.height(K1Sp.lg))

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        K1Chip(
          label = "Tasks · ${visibleTasks.size}",
          selected = filter == "tasks",
          onClick = { filter = "tasks" },
        )
        K1Chip(
          label = "Sessions · ${visibleMeetings.size}",
          selected = filter == "sessions",
          onClick = { filter = "sessions" },
        )
        K1Chip(
          label = "Entities · ${archivedProjects.size + archivedPeople.size + archivedOrganizations.size}",
          selected = filter == "entities",
          onClick = { filter = "entities" },
        )
      }

      Spacer(Modifier.height(K1Sp.m))

      when (filter) {
        "tasks" -> ArchivedTasksList(
          tasks = visibleTasks,
          onUnarchive = onUnarchiveTask,
          onDelete = { id -> deleteConfirmationItem = id to "task" },
        )

        "sessions" -> ArchivedSessionsList(
          meetings = visibleMeetings,
          onUnarchive = onUnarchiveMeeting,
          onDelete = { id -> deleteConfirmationItem = id to "meeting" },
        )

        "entities" -> ArchivedEntitiesList(
          projects = archivedProjects,
          people = archivedPeople,
          organizations = archivedOrganizations,
          onUnarchiveProject = onUnarchiveProject,
          onUnarchivePerson = onUnarchivePerson,
          onUnarchiveOrganization = onUnarchiveOrganization,
          onDeleteProject = { id -> deleteConfirmationItem = id to "project" },
          onDeletePerson = { id -> deleteConfirmationItem = id to "person" },
          onDeleteOrganization = { id -> deleteConfirmationItem = id to "organization" },
        )
      }
    }

    // Confirmation dialog — K1 paper modal, no Material chrome.
    deleteConfirmationItem?.let { (id, type) ->
      K1ConfirmDestructive(
        title = "Delete permanently.",
        body = "This will remove the item for good. Can't be undone.",
        confirmLabel = "Delete",
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
        onCancel = { deleteConfirmationItem = null },
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArchivedTasksList(
  tasks: List<TaskMetadata>,
  onUnarchive: (String) -> Unit,
  onDelete: (String) -> Unit,
) {
  if (tasks.isEmpty()) {
    EmptyArchiveState("No archived tasks")
    return
  }
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    items(tasks, key = { it.id }) { task ->
      SwipeToUnarchive(
        onUnarchive = { onUnarchive(task.id) },
        onDelete = { onDelete(task.id) },
      ) {
        K1ArchiveCard {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(task.title, style = K1Type.bodyMd.copy(color = KlikInkPrimary))
              Spacer(Modifier.height(2.dp))
              Text(task.subtitle, style = K1Type.metaSm.copy(color = KlikInkTertiary))
            }
            Text("Archived", style = K1Type.metaSm.copy(color = KlikInkMuted))
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
  onDelete: (String) -> Unit,
) {
  if (meetings.isEmpty()) {
    EmptyArchiveState("No archived sessions")
    return
  }
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    items(meetings, key = { it.id }) { meeting ->
      SwipeToUnarchive(
        onUnarchive = { onUnarchive(meeting.id) },
        onDelete = { onDelete(meeting.id) },
      ) {
        K1ArchiveCard {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(meeting.title, style = K1Type.bodyMd.copy(color = KlikInkPrimary))
              Spacer(Modifier.height(2.dp))
              Text(
                "${meeting.date} · ${meeting.time}",
                style = K1Type.metaSm.copy(color = KlikInkTertiary),
              )
            }
            if (!meeting.dropboxUrl.isNullOrBlank()) {
              Text(
                "Open",
                style = K1Type.metaSm.copy(color = KlikInkSecondary),
                modifier = Modifier.k1Clickable {
                  OAuthBrowser.openUrl(meeting.dropboxUrl)
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
private fun ArchivedEntitiesList(
  projects: List<Pair<String, String>>,
  people: List<Pair<String, String>>,
  organizations: List<Pair<String, String>>,
  onUnarchiveProject: (String) -> Unit,
  onUnarchivePerson: (String) -> Unit,
  onUnarchiveOrganization: (String) -> Unit,
  onDeleteProject: (String) -> Unit,
  onDeletePerson: (String) -> Unit,
  onDeleteOrganization: (String) -> Unit,
) {
  val total = projects.size + people.size + organizations.size
  if (total == 0) {
    EmptyArchiveState("No archived entities")
    return
  }
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    if (projects.isNotEmpty()) {
      item {
        Spacer(Modifier.height(4.dp))
        K1Eyebrow("Projects")
        Spacer(Modifier.height(6.dp))
      }
      items(projects, key = { "project_${it.first}" }) { (id, name) ->
        EntityArchiveCard(
          name = name,
          type = "Project",
          onUnarchive = { onUnarchiveProject(id) },
          onDelete = { onDeleteProject(id) },
        )
      }
    }
    if (people.isNotEmpty()) {
      item {
        Spacer(Modifier.height(4.dp))
        K1Eyebrow("People")
        Spacer(Modifier.height(6.dp))
      }
      items(people, key = { "person_${it.first}" }) { (id, name) ->
        EntityArchiveCard(
          name = name,
          type = "Person",
          onUnarchive = { onUnarchivePerson(id) },
          onDelete = { onDeletePerson(id) },
        )
      }
    }
    if (organizations.isNotEmpty()) {
      item {
        Spacer(Modifier.height(4.dp))
        K1Eyebrow("Organizations")
        Spacer(Modifier.height(6.dp))
      }
      items(organizations, key = { "org_${it.first}" }) { (id, name) ->
        EntityArchiveCard(
          name = name,
          type = "Organization",
          onUnarchive = { onUnarchiveOrganization(id) },
          onDelete = { onDeleteOrganization(id) },
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EntityArchiveCard(
  name: String,
  type: String,
  onUnarchive: () -> Unit,
  onDelete: () -> Unit,
) {
  SwipeToUnarchive(onUnarchive = onUnarchive, onDelete = onDelete) {
    K1ArchiveCard {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Box(
            modifier = Modifier
              .size(6.dp)
              .background(KlikInkTertiary, CircleShape),
          )
          Column {
            Text(name, style = K1Type.bodyMd.copy(color = KlikInkPrimary))
            Text(type, style = K1Type.metaSm.copy(color = KlikInkTertiary))
          }
        }
        Text("Archived", style = K1Type.metaSm.copy(color = KlikInkMuted))
      }
    }
  }
}

@Composable
private fun K1ArchiveCard(content: @Composable () -> Unit) {
  val shape = RoundedCornerShape(14.dp)
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(shape)
      .background(KlikPaperCard)
      .border(0.75.dp, KlikLineHairline, shape)
      .padding(16.dp),
  ) { content() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToUnarchive(
  onUnarchive: () -> Unit,
  onDelete: () -> Unit = {},
  content: @Composable () -> Unit,
) {
  val dismissState = rememberSwipeToDismissBoxState(
    confirmValueChange = { v ->
      when (v) {
        SwipeToDismissBoxValue.StartToEnd -> {
          HapticService.success()
          onUnarchive()
          true
        }

        SwipeToDismissBoxValue.EndToStart -> {
          HapticService.heavyImpact()
          onDelete()
          false
        }

        else -> false
      }
    },
  )
  SwipeToDismissBox(
    modifier = Modifier.fillMaxWidth(),
    state = dismissState,
    backgroundContent = {
      val isRestore = dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd
      val isDelete = dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart
      Box(
        modifier = Modifier
          .fillMaxSize()
          .clip(RoundedCornerShape(14.dp))
          .background(
            when {
              isRestore -> KlikPaperSoft
              isDelete -> KlikInkPrimary
              else -> Color.Transparent
            },
          ),
        contentAlignment = if (isRestore) Alignment.CenterStart else Alignment.CenterEnd,
      ) {
        Box(modifier = Modifier.padding(horizontal = 24.dp)) {
          Text(
            when {
              isRestore -> "Restore"
              isDelete -> "Delete"
              else -> ""
            },
            style = K1Type.bodyMd.copy(
              color = if (isDelete) KlikPaperCard else KlikInkPrimary,
            ),
          )
        }
      }
    },
    content = {
      Box(modifier = Modifier.fillMaxWidth()) { content() }
    },
  )
}

@Composable
private fun EmptyArchiveState(message: String) {
  Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.padding(horizontal = 24.dp),
    ) {
      K1Eyebrow("All clear")
      Text(message, style = K1Type.h2)
      Text(
        "Swipe any card right to restore, or left to remove permanently.",
        style = K1Type.bodySm.copy(color = KlikInkSecondary),
      )
    }
  }
}

@Composable
private fun K1ConfirmDestructive(
  title: String,
  body: String,
  confirmLabel: String,
  onConfirm: () -> Unit,
  onCancel: () -> Unit,
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = 0.32f))
      .pointerInput(Unit) { detectTapGestures(onTap = { onCancel() }) },
    contentAlignment = Alignment.Center,
  ) {
    Column(
      modifier = Modifier
        .widthIn(max = 360.dp)
        .padding(horizontal = 24.dp)
        .clip(RoundedCornerShape(20.dp))
        .background(KlikPaperCard)
        .border(0.75.dp, KlikLineHairline, RoundedCornerShape(20.dp))
        .pointerInput(Unit) { detectTapGestures { /* swallow */ } }
        .padding(horizontal = 24.dp, vertical = 24.dp),
    ) {
      K1Eyebrow("Klik")
      Spacer(Modifier.height(K1Sp.m))
      Text(title, style = K1Type.h2)
      Spacer(Modifier.height(K1Sp.s))
      Text(body, style = K1Type.bodySm.copy(color = KlikInkSecondary))
      Spacer(Modifier.height(K1Sp.xl))
      K1ButtonPrimary(
        label = confirmLabel,
        onClick = onConfirm,
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(K1Sp.m))
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(KlikPaperSoft)
          .k1Clickable(onClick = onCancel)
          .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
      ) {
        Text("Cancel", style = K1Type.bodyMd.copy(color = KlikInkPrimary))
      }
    }
  }
}
