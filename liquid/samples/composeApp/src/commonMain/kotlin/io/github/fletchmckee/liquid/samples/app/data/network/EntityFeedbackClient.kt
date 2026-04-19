package io.github.fletchmckee.liquid.samples.app.data.network

import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

@Serializable
data class EntityUpdateRequest(
    val user_id: String,
    val entity_id: String,
    val entity_type: String,
    val updates: Map<String, String>
)

@Serializable
data class EntityUpdateResponse(
    val success: Boolean,
    val message: String,
    val entity_id: String,
    val updated_fields: List<String>
)

object EntityFeedbackClient {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Update an entity via the feedback API.
     * Uses HttpClient singleton which handles automatic token refresh on 401.
     */
    suspend fun updateEntity(
        entityTypeStr: String,
        entityId: String,
        updates: Map<String, String>
    ): Result<EntityUpdateResponse> {
        return try {
            val userId = CurrentUser.userId ?: return Result.failure(
                Exception("User not authenticated")
            )

            val url = "${ApiConfig.ENTITY_FEEDBACK_BASE_URL}${ApiConfig.Endpoints.ENTITY_UPDATE}/$entityTypeStr/$entityId"

            val requestBody = EntityUpdateRequest(
                user_id = userId,
                entity_id = entityId,
                entity_type = entityTypeStr,
                updates = updates
            )

            val requestBodyJson = json.encodeToString(requestBody)

            KlikLogger.d("EntityFeedback", "PUT $url | Body: $requestBodyJson")

            // Use HttpClient.putUrl which handles automatic token refresh
            val responseBody = HttpClient.putUrl(url, requestBodyJson)

            if (responseBody != null) {
                KlikLogger.d("EntityFeedback", "Response: $responseBody")
                val entityResponse = json.decodeFromString<EntityUpdateResponse>(responseBody)
                Result.success(entityResponse)
            } else {
                KlikLogger.e("EntityFeedback", "Empty response")
                Result.failure(Exception("Entity update failed: Empty response"))
            }
        } catch (e: Exception) {
            KlikLogger.e("EntityFeedback", "Error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun updateProjectEntity(
        projectId: String,
        name: String? = null,
        budget: String? = null,
        status: String? = null,
        stage: String? = null,
        lead: String? = null
    ): Result<EntityUpdateResponse> {
        val updates = mutableMapOf<String, String>()
        name?.let { updates["name"] = it }
        budget?.let { updates["budget"] = it }
        status?.let { updates["status"] = it }
        stage?.let { updates["stage"] = it }
        lead?.let { updates["lead"] = it }

        if (updates.isEmpty()) {
            return Result.failure(Exception("No fields to update"))
        }

        return updateEntity("project", projectId, updates)
    }

    suspend fun updatePersonEntity(
        personId: String,
        name: String? = null,
        email: String? = null,
        phone: String? = null,
        role: String? = null
    ): Result<EntityUpdateResponse> {
        val updates = mutableMapOf<String, String>()
        name?.let { updates["name"] = it }
        email?.let { updates["email"] = it }
        phone?.let { updates["phone"] = it }
        role?.let { updates["role"] = it }

        if (updates.isEmpty()) {
            return Result.failure(Exception("No fields to update"))
        }

        return updateEntity("person", personId, updates)
    }

    suspend fun updateOrganizationEntity(
        orgId: String,
        name: String? = null,
        industry: String? = null,
        sizeHeadcount: Int? = null,
        strategicFocus: String? = null
    ): Result<EntityUpdateResponse> {
        val updates = mutableMapOf<String, String>()
        name?.let { updates["name"] = it }
        industry?.let { updates["industry"] = it }
        sizeHeadcount?.let { updates["sizeHeadcount"] = it.toString() }
        strategicFocus?.let { updates["strategicFocus"] = it }

        if (updates.isEmpty()) {
            return Result.failure(Exception("No fields to update"))
        }

        return updateEntity("organization", orgId, updates)
    }
}
