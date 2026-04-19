package io.github.fletchmckee.liquid.samples.app.ui.utils

import io.github.fletchmckee.liquid.samples.app.domain.entity.Meeting

/**
 * Utility object for handling precise segment navigation in meeting transcripts.
 *
 * This provides unified logic for navigating to specific transcript segments
 * across all cards (Insights, Highlights, Encourage, etc.) to ensure consistent
 * and accurate navigation behavior.
 */
object SegmentNavigationUtils {

    /**
     * Find the index of a transcript line that matches the given segment.
     *
     * This uses a multi-strategy approach for maximum accuracy:
     * 1. Try to parse segmentId as a line index (if it's a number)
     * 2. Try to parse segmentId as a timestamp and match against speaker timestamps
     * 3. Fall back to text matching with enhanced fuzzy logic
     * 4. As last resort, use simple contains matching
     *
     * @param lines List of transcript lines (speaker, text) pairs
     * @param segmentId The segment identifier from TracedSegment
     * @param segmentText The segment text content
     * @return The index of the matching line, or -1 if not found
     */
    fun findSegmentIndex(
        lines: List<Pair<String, String>>,
        segmentId: String,
        segmentText: String
    ): Int {
        if (lines.isEmpty()) return -1

        // Strategy 1: Try to parse segmentId as a direct line index
        val indexFromId = segmentId.toIntOrNull()
        if (indexFromId != null && indexFromId in lines.indices) {
            // Verify the text roughly matches to ensure correctness
            val lineText = lines[indexFromId].second
            if (textSimilarity(lineText, segmentText) > 0.3) {
                return indexFromId
            }
        }

        // Strategy 2: Try to extract timestamp from segmentId and match against speaker timestamps
        // Format: "[HH:MM:SS - HH:MM:SS]" or similar
        val timestampMatch = extractTimestamp(segmentId)
        if (timestampMatch != null) {
            val index = lines.indexOfFirst { (speaker, _) ->
                speaker.contains(timestampMatch, ignoreCase = true)
            }
            if (index >= 0) return index
        }

        // Strategy 3: Enhanced text matching with fuzzy logic
        // Find the line with highest similarity score above threshold
        val threshold = 0.5
        var bestIndex = -1
        var bestScore = threshold

        lines.forEachIndexed { index, (_, text) ->
            val similarity = textSimilarity(text, segmentText)
            if (similarity > bestScore) {
                bestScore = similarity
                bestIndex = index
            }
        }

        if (bestIndex >= 0) return bestIndex

        // Strategy 4: Simple contains matching (case-insensitive) - least strict
        val simpleMatchIndex = lines.indexOfFirst { (_, text) ->
            text.contains(segmentText, ignoreCase = true)
        }

        return simpleMatchIndex
    }

    /**
     * Calculate text similarity between two strings using a simple word overlap metric.
     * Returns a value between 0.0 (no similarity) and 1.0 (identical).
     *
     * This is more robust than simple contains() matching because it:
     * - Handles partial matches better
     * - Is less sensitive to word order
     * - Accounts for text length differences
     */
    private fun textSimilarity(text1: String, text2: String): Double {
        if (text1 == text2) return 1.0
        if (text1.isEmpty() || text2.isEmpty()) return 0.0

        val words1 = text1.lowercase()
            .split(Regex("\\s+"))
            .filter { it.length > 2 } // Ignore very short words
            .toSet()

        val words2 = text2.lowercase()
            .split(Regex("\\s+"))
            .filter { it.length > 2 }
            .toSet()

        if (words1.isEmpty() || words2.isEmpty()) {
            // For short texts, use simple contains comparison
            return if (text1.lowercase().contains(text2.lowercase()) ||
                      text2.lowercase().contains(text1.lowercase())) 0.6 else 0.0
        }

        val intersection = words1.intersect(words2).size
        val union = words1.union(words2).size

        // Jaccard similarity
        return intersection.toDouble() / union.toDouble()
    }

    /**
     * Extract timestamp from a segment ID string.
     * Looks for patterns like:
     * - "[0:00:09 - 0:00:12]"
     * - "0:00:09"
     * - "00:09"
     * etc.
     *
     * Returns the first timestamp pattern found, or null if none found.
     */
    private fun extractTimestamp(segmentId: String): String? {
        // Pattern 1: "[HH:MM:SS - HH:MM:SS]" or "[MM:SS - MM:SS]"
        val bracketedPattern = Regex("""\[(\d+:\d+:\d+|\d+:\d+)""")
        bracketedPattern.find(segmentId)?.let { matchResult ->
            return matchResult.groupValues[1]
        }

        // Pattern 2: Standalone "HH:MM:SS" or "MM:SS"
        val standalonePattern = Regex("""(\d+:\d+:\d+|\d+:\d+)""")
        standalonePattern.find(segmentId)?.let { matchResult ->
            return matchResult.groupValues[1]
        }

        return null
    }

    /**
     * Find a meeting by session ID in a list of meetings.
     * This handles the common case where we need to navigate to a meeting
     * based on a traced segment's sessionId.
     *
     * @param meetings List of all meetings
     * @param sessionId The session ID to search for
     * @return The matching meeting, or null if not found
     */
    fun findMeetingBySessionId(
        meetings: List<Meeting>,
        sessionId: String
    ): Meeting? {
        return meetings.find { it.id == sessionId }
    }

    /**
     * Validate that a segment index is within valid bounds and the segment
     * roughly matches the expected content.
     *
     * This is a safety check to ensure we don't navigate to the wrong line
     * due to data inconsistencies.
     *
     * @param lines List of transcript lines
     * @param index The index to validate
     * @param expectedText The expected text content
     * @param minSimilarity Minimum similarity threshold (default 0.3)
     * @return True if the index is valid and content matches
     */
    fun validateSegmentMatch(
        lines: List<Pair<String, String>>,
        index: Int,
        expectedText: String,
        minSimilarity: Double = 0.3
    ): Boolean {
        if (index !in lines.indices) return false

        val actualText = lines[index].second
        val similarity = textSimilarity(actualText, expectedText)

        return similarity >= minSimilarity
    }
}
