// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.domain.repository

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.data.network.dto.RuleDto
import io.github.fletchmckee.liquid.samples.app.data.network.dto.RulePreviewDto

/**
 * Repository for KK_suggest rules — the persistent contextual-task rule store.
 * Backend lives at $SUGGEST_BASE_URL/v1/rules.
 */
interface RulesRepository {
  suspend fun preview(nlText: String): Result<RulePreviewDto>
  suspend fun create(nlText: String, preview: RulePreviewDto, isRecurring: Boolean): Result<RuleDto>
  suspend fun list(): Result<List<RuleDto>>
  suspend fun edit(
    id: String,
    nlText: String? = null,
    isRecurring: Boolean? = null,
    status: String? = null,
  ): Result<RuleDto>
  suspend fun delete(id: String): Result<Unit>
  suspend fun accept(id: String): Result<RuleDto>
}
