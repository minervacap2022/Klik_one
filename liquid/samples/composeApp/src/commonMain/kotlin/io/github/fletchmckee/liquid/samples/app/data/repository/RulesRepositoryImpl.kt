// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.data.repository

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.data.network.ApiConfig
import io.github.fletchmckee.liquid.samples.app.data.network.HttpClient
import io.github.fletchmckee.liquid.samples.app.data.network.dto.RuleDto
import io.github.fletchmckee.liquid.samples.app.data.network.dto.RulePreviewDto
import io.github.fletchmckee.liquid.samples.app.domain.repository.RulesRepository
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * RulesRepository implementation backed by KK_suggest at $SUGGEST_BASE_URL/v1/rules.
 * Uses the HttpClient singleton so JWT auth + automatic refresh are handled for us.
 */
class RulesRepositoryImpl : RulesRepository {
  private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
  private val base get() = "${ApiConfig.SUGGEST_BASE_URL}/v1/rules"

  override suspend fun preview(nlText: String): Result<RulePreviewDto> = try {
    val body = buildJsonObject { put("nl_text", nlText) }.toString()
    val resp = HttpClient.postUrl("$base/preview", body)
      ?: error("No response from POST /rules/preview")
    Result.Success(json.decodeFromString(RulePreviewDto.serializer(), resp))
  } catch (e: CancellationException) {
    throw e
  } catch (e: Throwable) {
    Result.Error(e)
  }

  override suspend fun create(nlText: String, preview: RulePreviewDto, isRecurring: Boolean): Result<RuleDto> = try {
    val body = buildJsonObject {
      put("nl_text", nlText)
      put("is_recurring", isRecurring)
      put("preview", json.encodeToJsonElement(RulePreviewDto.serializer(), preview))
    }.toString()
    val resp = HttpClient.postUrl(base, body) ?: error("No response from POST /rules")
    Result.Success(json.decodeFromString(RuleDto.serializer(), resp))
  } catch (e: CancellationException) {
    throw e
  } catch (e: Throwable) {
    Result.Error(e)
  }

  override suspend fun list(): Result<List<RuleDto>> = try {
    val resp = HttpClient.getUrl(base) ?: error("No response from GET /rules")
    Result.Success(json.decodeFromString(ListSerializer(RuleDto.serializer()), resp))
  } catch (e: CancellationException) {
    throw e
  } catch (e: Throwable) {
    Result.Error(e)
  }

  override suspend fun edit(
    id: String,
    nlText: String?,
    isRecurring: Boolean?,
    status: String?,
    snoozedUntil: String?,
  ): Result<RuleDto> = try {
    val body = buildJsonObject {
      if (nlText != null) put("nl_text", nlText)
      if (isRecurring != null) put("is_recurring", isRecurring)
      if (status != null) put("status", status)
      if (snoozedUntil != null) put("snoozed_until", snoozedUntil)
    }.toString()
    val resp = HttpClient.patchUrl("$base/$id", body)
      ?: error("No response from PATCH /rules/$id")
    Result.Success(json.decodeFromString(RuleDto.serializer(), resp))
  } catch (e: CancellationException) {
    throw e
  } catch (e: Throwable) {
    Result.Error(e)
  }

  override suspend fun delete(id: String): Result<Unit> = try {
    HttpClient.deleteUrl("$base/$id")
    Result.Success(Unit)
  } catch (e: CancellationException) {
    throw e
  } catch (e: Throwable) {
    Result.Error(e)
  }

  override suspend fun accept(id: String): Result<RuleDto> = try {
    val resp = HttpClient.postUrl("$base/$id/accept", "")
      ?: error("No response from POST /rules/$id/accept")
    Result.Success(json.decodeFromString(RuleDto.serializer(), resp))
  } catch (e: CancellationException) {
    throw e
  } catch (e: Throwable) {
    Result.Error(e)
  }
}
