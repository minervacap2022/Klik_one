// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.data.storage

import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

/**
 * Storage keys for archive/pin state
 */
object ArchivePinStorageKeys {
    const val ARCHIVED_TASK_IDS = "archived_task_ids"
    const val ARCHIVED_MEETING_IDS = "archived_meeting_ids"
    const val ARCHIVED_PROJECT_IDS = "archived_project_ids"
    const val ARCHIVED_PERSON_IDS = "archived_person_ids"
    const val ARCHIVED_ORGANIZATION_IDS = "archived_organization_ids"

    const val PINNED_TASK_IDS = "pinned_task_ids"
    const val PINNED_MEETING_IDS = "pinned_meeting_ids"
    const val PINNED_PROJECT_IDS = "pinned_project_ids"
    const val PINNED_PERSON_IDS = "pinned_person_ids"
    const val PINNED_ORGANIZATION_IDS = "pinned_organization_ids"
}

/**
 * Serializable wrapper for pin timestamps Map
 */
@Serializable
data class PinnedIdsData(
    val ids: Map<String, Long> = emptyMap()
)

/**
 * Serializable wrapper for archived IDs Set
 */
@Serializable
data class ArchivedIdsData(
    val ids: Set<String> = emptySet()
)

/**
 * Manages persistent storage for archive/pin state.
 * Archive and pin state persists across app restarts.
 */
class ArchivePinStorageManager(private val storage: SecureStorage) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    // ==================== ARCHIVED IDS ====================

    fun saveArchivedTaskIds(ids: Set<String>) {
        saveArchivedIds(ArchivePinStorageKeys.ARCHIVED_TASK_IDS, ids)
    }

    fun loadArchivedTaskIds(): Set<String> {
        return loadArchivedIds(ArchivePinStorageKeys.ARCHIVED_TASK_IDS)
    }

    fun saveArchivedMeetingIds(ids: Set<String>) {
        saveArchivedIds(ArchivePinStorageKeys.ARCHIVED_MEETING_IDS, ids)
    }

    fun loadArchivedMeetingIds(): Set<String> {
        return loadArchivedIds(ArchivePinStorageKeys.ARCHIVED_MEETING_IDS)
    }

    fun saveArchivedProjectIds(ids: Set<String>) {
        saveArchivedIds(ArchivePinStorageKeys.ARCHIVED_PROJECT_IDS, ids)
    }

    fun loadArchivedProjectIds(): Set<String> {
        return loadArchivedIds(ArchivePinStorageKeys.ARCHIVED_PROJECT_IDS)
    }

    fun saveArchivedPersonIds(ids: Set<String>) {
        saveArchivedIds(ArchivePinStorageKeys.ARCHIVED_PERSON_IDS, ids)
    }

    fun loadArchivedPersonIds(): Set<String> {
        return loadArchivedIds(ArchivePinStorageKeys.ARCHIVED_PERSON_IDS)
    }

    fun saveArchivedOrganizationIds(ids: Set<String>) {
        saveArchivedIds(ArchivePinStorageKeys.ARCHIVED_ORGANIZATION_IDS, ids)
    }

    fun loadArchivedOrganizationIds(): Set<String> {
        return loadArchivedIds(ArchivePinStorageKeys.ARCHIVED_ORGANIZATION_IDS)
    }

    // ==================== PINNED IDS ====================

    fun savePinnedTaskIds(ids: Map<String, Long>) {
        savePinnedIds(ArchivePinStorageKeys.PINNED_TASK_IDS, ids)
    }

    fun loadPinnedTaskIds(): Map<String, Long> {
        return loadPinnedIds(ArchivePinStorageKeys.PINNED_TASK_IDS)
    }

    fun savePinnedMeetingIds(ids: Map<String, Long>) {
        savePinnedIds(ArchivePinStorageKeys.PINNED_MEETING_IDS, ids)
    }

    fun loadPinnedMeetingIds(): Map<String, Long> {
        return loadPinnedIds(ArchivePinStorageKeys.PINNED_MEETING_IDS)
    }

    fun savePinnedProjectIds(ids: Map<String, Long>) {
        savePinnedIds(ArchivePinStorageKeys.PINNED_PROJECT_IDS, ids)
    }

    fun loadPinnedProjectIds(): Map<String, Long> {
        return loadPinnedIds(ArchivePinStorageKeys.PINNED_PROJECT_IDS)
    }

    fun savePinnedPersonIds(ids: Map<String, Long>) {
        savePinnedIds(ArchivePinStorageKeys.PINNED_PERSON_IDS, ids)
    }

    fun loadPinnedPersonIds(): Map<String, Long> {
        return loadPinnedIds(ArchivePinStorageKeys.PINNED_PERSON_IDS)
    }

    fun savePinnedOrganizationIds(ids: Map<String, Long>) {
        savePinnedIds(ArchivePinStorageKeys.PINNED_ORGANIZATION_IDS, ids)
    }

    fun loadPinnedOrganizationIds(): Map<String, Long> {
        return loadPinnedIds(ArchivePinStorageKeys.PINNED_ORGANIZATION_IDS)
    }

    // ==================== CLEAR ALL ====================

    fun clearAll() {
        storage.remove(ArchivePinStorageKeys.ARCHIVED_TASK_IDS)
        storage.remove(ArchivePinStorageKeys.ARCHIVED_MEETING_IDS)
        storage.remove(ArchivePinStorageKeys.ARCHIVED_PROJECT_IDS)
        storage.remove(ArchivePinStorageKeys.ARCHIVED_PERSON_IDS)
        storage.remove(ArchivePinStorageKeys.ARCHIVED_ORGANIZATION_IDS)
        storage.remove(ArchivePinStorageKeys.PINNED_TASK_IDS)
        storage.remove(ArchivePinStorageKeys.PINNED_MEETING_IDS)
        storage.remove(ArchivePinStorageKeys.PINNED_PROJECT_IDS)
        storage.remove(ArchivePinStorageKeys.PINNED_PERSON_IDS)
        storage.remove(ArchivePinStorageKeys.PINNED_ORGANIZATION_IDS)
        KlikLogger.i("ArchivePinStorage", "Cleared all archive/pin state")
    }

    // ==================== PRIVATE HELPERS ====================

    private fun saveArchivedIds(key: String, ids: Set<String>) {
        try {
            val data = ArchivedIdsData(ids)
            val jsonString = json.encodeToString(data)
            storage.saveString(key, jsonString)
            KlikLogger.i("ArchivePinStorage", "Saved $key: ${ids.size} items")
        } catch (e: Exception) {
            KlikLogger.e("ArchivePinStorage", "Failed to save $key: ${e.message}", e)
        }
    }

    private fun loadArchivedIds(key: String): Set<String> {
        return try {
            val jsonString = storage.getString(key)
            if (jsonString != null) {
                val data = json.decodeFromString<ArchivedIdsData>(jsonString)
                KlikLogger.i("ArchivePinStorage", "Loaded $key: ${data.ids.size} items")
                data.ids
            } else {
                emptySet()
            }
        } catch (e: Exception) {
            KlikLogger.e("ArchivePinStorage", "Failed to load $key: ${e.message}", e)
            emptySet()
        }
    }

    private fun savePinnedIds(key: String, ids: Map<String, Long>) {
        try {
            val data = PinnedIdsData(ids)
            val jsonString = json.encodeToString(data)
            storage.saveString(key, jsonString)
            KlikLogger.i("ArchivePinStorage", "Saved $key: ${ids.size} items")
        } catch (e: Exception) {
            KlikLogger.e("ArchivePinStorage", "Failed to save $key: ${e.message}", e)
        }
    }

    private fun loadPinnedIds(key: String): Map<String, Long> {
        return try {
            val jsonString = storage.getString(key)
            if (jsonString != null) {
                val data = json.decodeFromString<PinnedIdsData>(jsonString)
                KlikLogger.i("ArchivePinStorage", "Loaded $key: ${data.ids.size} items")
                data.ids
            } else {
                emptyMap()
            }
        } catch (e: Exception) {
            KlikLogger.e("ArchivePinStorage", "Failed to load $key: ${e.message}", e)
            emptyMap()
        }
    }
}
