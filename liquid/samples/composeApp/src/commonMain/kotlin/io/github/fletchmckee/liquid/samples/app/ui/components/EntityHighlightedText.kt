// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.ui.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
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
  val endIndex: Int,
)

/**
 * Entity navigation callback
 */
data class EntityNavigationData(
  val entityType: EntityType,
  val entityId: String,
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
  maxLines: Int = Int.MAX_VALUE,
) {
  // Find all entity mentions in the text (memoized to avoid O(n^2) recomputation)
  val mentions = remember(text, tasks, meetings, projects, people, organizations) {
    findEntityMentions(
      text = text,
      tasks = tasks,
      meetings = meetings,
      projects = projects,
      people = people,
      organizations = organizations,
    )
  }

  // Build annotated string with clickable entity mentions (memoized).
  // Uses LinkAnnotation.Clickable (the modern replacement for the deprecated
  // ClickableText + StringAnnotation pair) so Text composable handles dispatch.
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

        val entityStyle = SpanStyle(
          color = getEntityColor(mention.entityType),
          fontWeight = FontWeight.Medium,
          textDecoration = TextDecoration.Underline,
        )
        withLink(
          LinkAnnotation.Clickable(
            tag = "ENTITY",
            styles = TextLinkStyles(style = entityStyle),
            linkInteractionListener = {
              onEntityClick(EntityNavigationData(mention.entityType, mention.entityId))
            },
          ),
        ) {
          append(mention.text)
        }

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

  Text(
    text = annotatedString,
    modifier = modifier,
    style = style.copy(
      color = if (color != Color.Unspecified) color else style.color,
      fontSize = if (fontSize != TextUnit.Unspecified) fontSize else style.fontSize,
    ),
    maxLines = maxLines,
  )
}

/**
 * Find all entity mentions in the text.
 *
 * Single-pass fuzzy matcher. For every entity type we collect candidate name
 * forms (canonical, display name, aliases, and each whitespace token of those),
 * then walk the text's word windows (1..3 consecutive words) and compute the
 * best Jaro-Winkler similarity vs every candidate. Windows scoring ≥ 0.92
 * become highlights. Tasks and meetings use their titles as candidates.
 *
 * Exact equality is just the upper bound of Jaro-Winkler (1.0), so there is no
 * separate exact-match path — one algorithm, one pass. Overlapping matches
 * resolve longest-window-wins so "John Smith" beats "John" when both score
 * over the threshold.
 */
private fun findEntityMentions(
  text: String,
  tasks: List<TaskMetadata>,
  meetings: List<Meeting>,
  projects: List<Project>,
  people: List<Person>,
  organizations: List<Organization>,
): List<EntityMention> {
  if (text.isBlank()) return emptyList()

  val spans = wordSpans(text)
  val allMatches = mutableListOf<EntityMention>()

  data class Bucket(val type: EntityType, val id: String, val candidates: List<String>)

  val buckets = buildList<Bucket> {
    tasks.forEach { t ->
      val raw = listOf(t.title, t.relatedProject).filter { it.isNotBlank() }
      if (raw.isNotEmpty()) add(Bucket(EntityType.TASK, t.id, raw))
    }
    meetings.forEach { m ->
      if (m.title.isNotBlank()) add(Bucket(EntityType.MEETING, m.id, listOf(m.title)))
    }
    projects.forEach { p ->
      add(Bucket(EntityType.PROJECT, p.id, listOf(p.canonicalName, p.name) + p.aliases))
    }
    people.forEach { p ->
      add(Bucket(EntityType.PERSON, p.id, listOf(p.canonicalName, p.name) + p.aliases))
    }
    organizations.forEach { o ->
      add(Bucket(EntityType.ORGANIZATION, o.id, listOf(o.canonicalName, o.name) + o.aliases))
    }
  }

  // Expand each bucket's candidates into normalized matchable forms — full
  // name + every whitespace token of length ≥ 3. Lowercased once so the inner
  // loop is just similarity math.
  val prepared = buckets.map { b ->
    val full = b.candidates.filter { it.isNotBlank() }.map { it.lowercase() }
    val tokens = full.flatMap { it.split(Regex("\\s+")) }.filter { it.length >= 3 }
    b to (full + tokens).distinct()
  }

  // Window over the text: 1, 2, and 3 consecutive words. Multi-word windows
  // catch full names like "John Smith"; single-word windows catch short forms
  // and abbreviations. Each window is matched against every candidate; the
  // best similarity wins.
  for (windowSize in 1..3) {
    for (i in 0..(spans.size - windowSize)) {
      val start = spans[i].start
      val end = spans[i + windowSize - 1].end
      val window = text.substring(start, end).lowercase()

      var bestType: EntityType? = null
      var bestId: String? = null
      var bestScore = THRESHOLD
      var bestIsTokenLevel = false

      for ((bucket, forms) in prepared) {
        for (form in forms) {
          val score = if (form == window) 1.0 else jaroWinklerSimilarity(window, form)
          if (score >= bestScore) {
            // Token-level matches (single-word window vs single-word form) are
            // most prone to false positives on common nouns. Require the word
            // in the source text to start uppercase to act on those.
            val tokenLevel = windowSize == 1 && !form.contains(' ')
            if (tokenLevel && !text[start].isUpperCase()) continue
            bestScore = score
            bestType = bucket.type
            bestId = bucket.id
            bestIsTokenLevel = tokenLevel
          }
        }
      }
      if (bestType != null && bestId != null) {
        allMatches.add(
          EntityMention(
            text = text.substring(start, end),
            entityType = bestType,
            entityId = bestId,
            startIndex = start,
            endIndex = end,
          ),
        )
      }
      // Suppress warning on unused variable when the heuristic keeps a token
      // match — the flag is used inside the loop, the linter just can't tell.
      @Suppress("UNUSED_EXPRESSION") bestIsTokenLevel
    }
  }

  // Longest window wins on overlap so "John Smith" supersedes "John".
  allMatches.sortByDescending { it.endIndex - it.startIndex }
  val mentions = mutableListOf<EntityMention>()
  val occupied = mutableListOf<IntRange>()
  allMatches.forEach { m ->
    val range = m.startIndex until m.endIndex
    if (occupied.none { it.overlaps(range) }) {
      mentions += m
      occupied += range
    }
  }
  return mentions
}

private const val THRESHOLD = 0.92

/** Token spans in [text]: contiguous letter/digit runs. */
private data class WordSpan(val start: Int, val end: Int, val word: String)
private fun wordSpans(text: String): List<WordSpan> {
  val out = mutableListOf<WordSpan>()
  var i = 0
  while (i < text.length) {
    if (text[i].isLetterOrDigit()) {
      var j = i
      while (j < text.length && text[j].isLetterOrDigit()) j++
      out.add(WordSpan(i, j, text.substring(i, j)))
      i = j
    } else {
      i++
    }
  }
  return out
}

/**
 * Jaro-Winkler similarity in [0.0, 1.0]. Returns 1.0 on equality, 0.0 on no
 * shared characters within the matching window. Pure Kotlin so it works on
 * commonMain.
 */
private fun jaroWinklerSimilarity(a: String, b: String): Double {
  if (a == b) return 1.0
  if (a.isEmpty() || b.isEmpty()) return 0.0

  val matchDistance = (maxOf(a.length, b.length) / 2) - 1
  val aMatches = BooleanArray(a.length)
  val bMatches = BooleanArray(b.length)
  var matches = 0
  for (i in a.indices) {
    val lo = maxOf(0, i - matchDistance)
    val hi = minOf(i + matchDistance + 1, b.length)
    for (j in lo until hi) {
      if (bMatches[j]) continue
      if (a[i] != b[j]) continue
      aMatches[i] = true
      bMatches[j] = true
      matches++
      break
    }
  }
  if (matches == 0) return 0.0

  var transpositions = 0
  var k = 0
  for (i in a.indices) {
    if (!aMatches[i]) continue
    while (!bMatches[k]) k++
    if (a[i] != b[k]) transpositions++
    k++
  }
  val m = matches.toDouble()
  val jaro = (m / a.length + m / b.length + (m - transpositions / 2.0) / m) / 3.0

  // Winkler boost for shared prefix up to 4 chars.
  var prefix = 0
  for (i in 0 until minOf(4, minOf(a.length, b.length))) {
    if (a[i] == b[i]) prefix++ else break
  }
  return jaro + prefix * 0.1 * (1 - jaro)
}

/**
 * Check if two ranges overlap
 */
private fun IntRange.overlaps(other: IntRange): Boolean = this.first < other.last && other.first < this.last

/**
 * Get color for entity type — K1 palette.
 * Matches the mentioned-chip dots in the Klik kit:
 *   project = purple (#7F77DD), org = green (#1D9E75), person = muted blue,
 *   task = commitment green, meeting = ink (underlined, no color swap).
 */
private fun getEntityColor(entityType: EntityType): Color = when (entityType) {
  EntityType.TASK -> Color(0xFF085041)

  // KlikCommitmentSubtext — deep green
  EntityType.MEETING -> Color(0xFF1C1D21)

  // KlikInkPrimary — neutral but underlined
  EntityType.PROJECT -> Color(0xFF7F77DD)

  // K1 project dot purple
  EntityType.PERSON -> Color(0xFF6E9DC0)

  // K1 avatar blue, muted enough to read inline
  EntityType.ORGANIZATION -> Color(0xFF1D9E75) // K1 org dot green
}
