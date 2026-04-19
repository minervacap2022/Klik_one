// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.utils

import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import kotlinx.datetime.LocalDate

/**
 * Utility functions for parsing and manipulating session IDs.
 *
 * Session ID Format: SESSION_YYYYMMDD_HHMMSS_hash
 * Example: SESSION_20251223_000336_aa4f9a10
 *
 * PRODUCTION: Used for navigating to historical meetings by extracting dates
 * from session identifiers and loading appropriate date ranges.
 */
object SessionIdUtils {

    /**
     * Extract LocalDate from session ID format.
     *
     * @param sessionId The session ID in format SESSION_YYYYMMDD_HHMMSS_hash
     * @return LocalDate parsed from the session ID, or null if parsing fails
     *
     * Example:
     * - Input: "SESSION_20251223_000336_aa4f9a10"
     * - Output: LocalDate(2025, 12, 23)
     */
    fun extractDateFromSessionId(sessionId: String): LocalDate? {
        // Pattern matches: SESSION_<8 digits>_
        val pattern = Regex("SESSION_(\\d{8})_")
        val match = pattern.find(sessionId) ?: return null
        val dateStr = match.groupValues[1] // Extract "20251223"

        return try {
            val year = dateStr.substring(0, 4).toInt()   // "2025"
            val month = dateStr.substring(4, 6).toInt()  // "12"
            val day = dateStr.substring(6, 8).toInt()    // "23"

            // Validate ranges
            if (month < 1 || month > 12) {
                KlikLogger.w("SessionIdUtils", "Invalid month: $month in sessionId: $sessionId")
                return null
            }
            if (day < 1 || day > 31) {
                KlikLogger.w("SessionIdUtils", "Invalid day: $day in sessionId: $sessionId")
                return null
            }

            LocalDate(year, month, day)
        } catch (e: Exception) {
            KlikLogger.e("SessionIdUtils", "Failed parsing date from $sessionId: ${e.message}", e)
            null
        }
    }

    /**
     * Get the full month date range for a given date.
     *
     * @param date The date to get the month range for
     * @return Pair of (first day of month, last day of month)
     *
     * Example:
     * - Input: LocalDate(2025, 12, 23)
     * - Output: Pair(LocalDate(2025, 12, 1), LocalDate(2025, 12, 31))
     */
    fun extractDateRangeForMonth(date: LocalDate): Pair<LocalDate, LocalDate> {
        val firstDay = LocalDate(date.year, date.month, 1)

        // Calculate last day of month (handles leap years)
        val lastDay = when (date.monthNumber) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if (isLeapYear(date.year)) 29 else 28
            else -> throw IllegalArgumentException("Invalid month number: ${date.monthNumber}")
        }

        return Pair(firstDay, LocalDate(date.year, date.month, lastDay))
    }

    /**
     * Check if a year is a leap year.
     *
     * @param year The year to check
     * @return true if the year is a leap year, false otherwise
     */
    private fun isLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }

    /**
     * Validate that a session ID matches the expected format.
     *
     * @param sessionId The session ID to validate
     * @return true if the session ID matches the expected format
     */
    fun isValidSessionIdFormat(sessionId: String): Boolean {
        val pattern = Regex("^SESSION_\\d{8}_\\d{6}_[a-z0-9]+$")
        return pattern.matches(sessionId)
    }
}
