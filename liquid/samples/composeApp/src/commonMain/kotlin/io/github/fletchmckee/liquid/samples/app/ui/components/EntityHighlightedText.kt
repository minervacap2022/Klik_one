package io.github.fletchmckee.liquid.samples.app.ui.components

import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import io.github.fletchmckee.liquid.samples.app.domain.entity.Meeting
import io.github.fletchmckee.liquid.samples.app.domain.entity.Organization
import io.github.fletchmckee.liquid.samples.app.domain.entity.Person
import io.github.fletchmckee.liquid.samples.app.domain.entity.Project
import io.github.fletchmckee.liquid.samples.app.model.TaskMetadata

/**
 * Entity mention found in text
 */
data class EntityMention(
    val text: String,
    val entityType: EntityType,
    val entityId: String,
    val startIndex: Int,
    val endIndex: Int
)

/**
 * Entity navigation callback
 */
data class EntityNavigationData(
    val entityType: EntityType,
    val entityId: String
)

/**
 * Composable that displays text with highlighted, clickable entity mentions.
 * Entities are identified by matching against provided entity lists.
 *
 * @param text The text to display
 * @param tasks List of tasks to match against
 * @param meetings List of meetings to match against
 * @param projects List of projects to match against
 * @param people List of people to match against
 * @param organizations List of organizations to match against
 * @param onEntityClick Callback when an entity is clicked
 * @param modifier Modifier for the text
 * @param style Text style
 * @param color Text color
 * @param fontSize Font size
 * @param maxLines Maximum lines to display
 */
@Composable
fun EntityHighlightedText(
    text: String,
    tasks: List<TaskMetadata> = emptyList(),
    meetings: List<Meeting> = emptyList(),
    projects: List<Project> = emptyList(),
    people: List<Person> = emptyList(),
    organizations: List<Organization> = emptyList(),
    onEntityClick: (EntityNavigationData) -> Unit = {},
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    maxLines: Int = Int.MAX_VALUE
) {
    // Find all entity mentions in the text (memoized to avoid O(n^2) recomputation)
    val mentions = remember(text, tasks, meetings, projects, people, organizations) {
        findEntityMentions(
            text = text,
            tasks = tasks,
            meetings = meetings,
            projects = projects,
            people = people,
            organizations = organizations
        )
    }

    // Build annotated string with clickable entity mentions (memoized)
    val annotatedString = remember(mentions, text, color) {
        buildAnnotatedString {
            var currentIndex = 0

            mentions.sortedBy { it.startIndex }.forEach { mention ->
                // Add text before the entity mention
                if (currentIndex < mention.startIndex) {
                    withStyle(style = SpanStyle(color = color)) {
                        append(text.substring(currentIndex, mention.startIndex))
                    }
                }

                // Add entity mention with highlighting and click annotation
                pushStringAnnotation(
                    tag = "ENTITY",
                    annotation = "${mention.entityType.name}:${mention.entityId}"
                )

                withStyle(
                    style = SpanStyle(
                        color = getEntityColor(mention.entityType),
                        fontWeight = FontWeight.Medium,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append(mention.text)
                }

                pop()

                currentIndex = mention.endIndex
            }

            // Add remaining text after last entity mention
            if (currentIndex < text.length) {
                withStyle(style = SpanStyle(color = color)) {
                    append(text.substring(currentIndex))
                }
            }
        }
    }

    ClickableText(
        text = annotatedString,
        onClick = { offset ->
            annotatedString.getStringAnnotations(
                tag = "ENTITY",
                start = offset,
                end = offset
            ).firstOrNull()?.let { annotation ->
                val parts = annotation.item.split(":")
                if (parts.size == 2) {
                    val entityType = EntityType.valueOf(parts[0])
                    val entityId = parts[1]
                    onEntityClick(EntityNavigationData(entityType, entityId))
                }
            }
        },
        modifier = modifier,
        style = style.copy(
            color = if (color != Color.Unspecified) color else style.color,
            fontSize = if (fontSize != TextUnit.Unspecified) fontSize else style.fontSize
        ),
        maxLines = maxLines
    )
}

/**
 * Find all entity mentions in the text.
 * Uses longest-match-first strategy to avoid overlapping matches.
 */
private fun findEntityMentions(
    text: String,
    tasks: List<TaskMetadata>,
    meetings: List<Meeting>,
    projects: List<Project>,
    people: List<Person>,
    organizations: List<Organization>
): List<EntityMention> {
    val mentions = mutableListOf<EntityMention>()
    val lowerText = text.lowercase()

    // Build list of all potential matches with their indices
    val allMatches = mutableListOf<EntityMention>()

    // Match tasks
    tasks.forEach { task ->
        val searchTerms = listOf(task.title) +
            (if (task.relatedProject.isNotEmpty()) listOf(task.relatedProject) else emptyList())

        searchTerms.forEach { term ->
            if (term.isNotEmpty()) {
                var startIndex = 0
                while (startIndex < lowerText.length) {
                    val index = lowerText.indexOf(term.lowercase(), startIndex)
                    if (index == -1) break

                    allMatches.add(
                        EntityMention(
                            text = text.substring(index, index + term.length),
                            entityType = EntityType.TASK,
                            entityId = task.id,
                            startIndex = index,
                            endIndex = index + term.length
                        )
                    )
                    startIndex = index + 1
                }
            }
        }
    }

    // Match meetings
    meetings.forEach { meeting ->
        if (meeting.title.isNotEmpty()) {
            var startIndex = 0
            while (startIndex < lowerText.length) {
                val index = lowerText.indexOf(meeting.title.lowercase(), startIndex)
                if (index == -1) break

                allMatches.add(
                    EntityMention(
                        text = text.substring(index, index + meeting.title.length),
                        entityType = EntityType.MEETING,
                        entityId = meeting.id,
                        startIndex = index,
                        endIndex = index + meeting.title.length
                    )
                )
                startIndex = index + 1
            }
        }
    }

    // Match projects
    projects.forEach { project ->
        val searchTerms = listOf(project.canonicalName, project.name) + project.aliases

        searchTerms.forEach { term ->
            if (term.length >= 3) { // Skip very short names to avoid false matches
                var startIndex = 0
                while (startIndex < lowerText.length) {
                    val index = lowerText.indexOf(term.lowercase(), startIndex)
                    if (index == -1) break

                    val beforeOk = index == 0 || !lowerText[index - 1].isLetterOrDigit()
                    val afterOk = (index + term.length) >= lowerText.length || !lowerText[index + term.length].isLetterOrDigit()
                    if (beforeOk && afterOk) {
                        allMatches.add(
                            EntityMention(
                                text = text.substring(index, index + term.length),
                                entityType = EntityType.PROJECT,
                                entityId = project.id,
                                startIndex = index,
                                endIndex = index + term.length
                            )
                        )
                    }
                    startIndex = index + 1
                }
            }
        }
    }

    // Match people
    people.forEach { person ->
        val searchTerms = listOf(person.canonicalName, person.name) + person.aliases

        searchTerms.forEach { term ->
            if (term.length >= 3) { // Skip very short names to avoid false matches
                var startIndex = 0
                while (startIndex < lowerText.length) {
                    val index = lowerText.indexOf(term.lowercase(), startIndex)
                    if (index == -1) break

                    // Only match at word boundaries to avoid matching substrings of other words
                    val beforeOk = index == 0 || !lowerText[index - 1].isLetterOrDigit()
                    val afterOk = (index + term.length) >= lowerText.length || !lowerText[index + term.length].isLetterOrDigit()
                    if (beforeOk && afterOk) {
                        allMatches.add(
                            EntityMention(
                                text = text.substring(index, index + term.length),
                                entityType = EntityType.PERSON,
                                entityId = person.id,
                                startIndex = index,
                                endIndex = index + term.length
                            )
                        )
                    }
                    startIndex = index + 1
                }
            }
        }
    }

    // Match organizations
    organizations.forEach { org ->
        val searchTerms = listOf(org.canonicalName, org.name) + org.aliases

        searchTerms.forEach { term ->
            if (term.length >= 3) { // Skip very short names to avoid false matches
                var startIndex = 0
                while (startIndex < lowerText.length) {
                    val index = lowerText.indexOf(term.lowercase(), startIndex)
                    if (index == -1) break

                    val beforeOk = index == 0 || !lowerText[index - 1].isLetterOrDigit()
                    val afterOk = (index + term.length) >= lowerText.length || !lowerText[index + term.length].isLetterOrDigit()
                    if (beforeOk && afterOk) {
                        allMatches.add(
                            EntityMention(
                                text = text.substring(index, index + term.length),
                                entityType = EntityType.ORGANIZATION,
                                entityId = org.id,
                                startIndex = index,
                                endIndex = index + term.length
                            )
                        )
                    }
                    startIndex = index + 1
                }
            }
        }
    }

    // Sort by length (longest first) to prefer longer matches
    allMatches.sortByDescending { it.endIndex - it.startIndex }

    // Filter overlapping matches (keep longest matches)
    val occupiedRanges = mutableListOf<IntRange>()
    allMatches.forEach { match ->
        val range = match.startIndex until match.endIndex
        if (occupiedRanges.none { it.overlaps(range) }) {
            mentions.add(match)
            occupiedRanges.add(range)
        }
    }

    return mentions
}

/**
 * Check if two ranges overlap
 */
private fun IntRange.overlaps(other: IntRange): Boolean {
    return this.first < other.last && other.first < this.last
}

/**
 * Get color for entity type — K1 palette.
 * Matches the mentioned-chip dots in the Klik kit:
 *   project = purple (#7F77DD), org = green (#1D9E75), person = muted blue,
 *   task = commitment green, meeting = ink (underlined, no color swap).
 */
private fun getEntityColor(entityType: EntityType): Color {
    return when (entityType) {
        EntityType.TASK -> Color(0xFF085041)          // KlikCommitmentSubtext — deep green
        EntityType.MEETING -> Color(0xFF1C1D21)       // KlikInkPrimary — neutral but underlined
        EntityType.PROJECT -> Color(0xFF7F77DD)       // K1 project dot purple
        EntityType.PERSON -> Color(0xFF6E9DC0)        // K1 avatar blue, muted enough to read inline
        EntityType.ORGANIZATION -> Color(0xFF1D9E75)  // K1 org dot green
    }
}
