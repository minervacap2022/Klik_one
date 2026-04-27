// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.ui.klikone

import io.github.fletchmckee.liquid.samples.app.domain.entity.DimensionScore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Locks in the contract for surfacing the 9 backend dimensions
 * (voice/connection/reliability for People, formation/tribe_vibe/pulse for Orgs,
 * clarity/weather/health for Projects) in the K1 UI.
 *
 * The OLD liquid-glass app rendered all three per entity. The K1 redesign dropped
 * everything except `voice` (used to bucket InfluenceTier S/A/B). The data still
 * flows from the backend on `Person.dimensions / Organization.dimensions /
 * Project.dimensions`; the UI just stopped looking at it. This resolver is the
 * pure-logic seam between the entity field and the K1 score box.
 */
class K1DimensionResolverTest {

  private fun ds(
    dimension: String,
    score: Float? = null,
    details: String = "{}",
    period: String = "",
  ) = DimensionScore(
    id = 0,
    entityType = "people",
    entityId = "p1",
    dimension = dimension,
    score = score,
    details = details,
    periodType = period,
    periodDate = "",
    userId = "u1",
    createdAt = "",
    updatedAt = "",
  )

  // ─── displayName: snake_case → Title Case ─────────────────────────────
  @Test
  fun `tribe_vibe is rendered as Tribe Vibe`() {
    assertEquals("Tribe Vibe", K1DimensionResolver.resolve(ds("tribe_vibe", 50f)).displayName)
  }

  @Test
  fun `voice is rendered as Voice`() {
    assertEquals("Voice", K1DimensionResolver.resolve(ds("voice", 50f)).displayName)
  }

  // ─── numeric score buckets — POSITIVE / WATCH / ALERT ─────────────────
  // Matches OLD WorkLifeScreen.kt:3286-3288 thresholds: ≥70 green, ≥40 orange, else red.
  @Test
  fun `score 85 is POSITIVE`() {
    val r = K1DimensionResolver.resolve(ds("voice", 85f))
    assertEquals("85", r.displayValue)
    assertEquals(K1DimensionTone.POSITIVE, r.tone)
  }

  @Test
  fun `score 70 is POSITIVE at boundary`() {
    assertEquals(K1DimensionTone.POSITIVE, K1DimensionResolver.resolve(ds("voice", 70f)).tone)
  }

  @Test
  fun `score 55 is WATCH`() {
    val r = K1DimensionResolver.resolve(ds("voice", 55f))
    assertEquals("55", r.displayValue)
    assertEquals(K1DimensionTone.WATCH, r.tone)
  }

  @Test
  fun `score 40 is WATCH at boundary`() {
    assertEquals(K1DimensionTone.WATCH, K1DimensionResolver.resolve(ds("voice", 40f)).tone)
  }

  @Test
  fun `score 30 is ALERT`() {
    val r = K1DimensionResolver.resolve(ds("voice", 30f))
    assertEquals("30", r.displayValue)
    assertEquals(K1DimensionTone.ALERT, r.tone)
  }

  // ─── missing data — NEUTRAL with em-dash, never silently hidden ───────
  @Test
  fun `null score with no status falls back to em-dash and NEUTRAL`() {
    val r = K1DimensionResolver.resolve(ds("voice", score = null))
    assertEquals("—", r.displayValue)
    assertEquals(K1DimensionTone.NEUTRAL, r.tone)
  }

  @Test
  fun `null DimensionScore returns a NEUTRAL placeholder for the requested name`() {
    val r = K1DimensionResolver.resolveMissing("reliability")
    assertEquals("Reliability", r.displayName)
    assertEquals("—", r.displayValue)
    assertEquals(K1DimensionTone.NEUTRAL, r.tone)
    assertNull(r.period)
  }

  // ─── status text from details JSON wins over score ───────────────────
  @Test
  fun `status field in details JSON is preferred over numeric score`() {
    val r = K1DimensionResolver.resolve(
      ds("weather", score = 22f, details = """{"status":"sunny"}"""),
    )
    assertEquals("Sunny", r.displayValue)
  }

  @Test
  fun `level field is used when status is absent`() {
    val r = K1DimensionResolver.resolve(
      ds("connection", score = null, details = """{"level":"strong"}"""),
    )
    assertEquals("Strong", r.displayValue)
  }

  @Test
  fun `state field is used when status and level are absent`() {
    val r = K1DimensionResolver.resolve(
      ds("formation", score = null, details = """{"state":"forming"}"""),
    )
    assertEquals("Forming", r.displayValue)
  }

  // ─── per-dimension tone overrides — health, weather ──────────────────
  // Matches OLD WorkLifeScreen.kt:3272-3284.
  @Test
  fun `health green is POSITIVE`() {
    assertEquals(
      K1DimensionTone.POSITIVE,
      K1DimensionResolver.resolve(ds("health", details = """{"status":"green"}""")).tone,
    )
  }

  @Test
  fun `health yellow is WATCH`() {
    assertEquals(
      K1DimensionTone.WATCH,
      K1DimensionResolver.resolve(ds("health", details = """{"status":"yellow"}""")).tone,
    )
  }

  @Test
  fun `health red is ALERT`() {
    assertEquals(
      K1DimensionTone.ALERT,
      K1DimensionResolver.resolve(ds("health", details = """{"status":"red"}""")).tone,
    )
  }

  @Test
  fun `weather sunny is POSITIVE`() {
    assertEquals(
      K1DimensionTone.POSITIVE,
      K1DimensionResolver.resolve(ds("weather", details = """{"status":"sunny"}""")).tone,
    )
  }

  @Test
  fun `weather partly cloudy is WATCH`() {
    assertEquals(
      K1DimensionTone.WATCH,
      K1DimensionResolver.resolve(ds("weather", details = """{"status":"partly cloudy"}""")).tone,
    )
  }

  @Test
  fun `weather foggy is NEUTRAL`() {
    assertEquals(
      K1DimensionTone.NEUTRAL,
      K1DimensionResolver.resolve(ds("weather", details = """{"status":"foggy"}""")).tone,
    )
  }

  // ─── parse failures must not crash and must not silently drop the row ─
  @Test
  fun `malformed details JSON falls back to numeric score`() {
    val r = K1DimensionResolver.resolve(ds("voice", score = 72f, details = "not json"))
    assertEquals("72", r.displayValue)
    assertEquals(K1DimensionTone.POSITIVE, r.tone)
  }

  @Test
  fun `period text is passed through unchanged`() {
    val r = K1DimensionResolver.resolve(ds("voice", 80f, period = "this_week"))
    assertEquals("this_week", r.period)
  }

  @Test
  fun `empty period is normalized to null`() {
    assertNull(K1DimensionResolver.resolve(ds("voice", 80f, period = "")).period)
  }
}
