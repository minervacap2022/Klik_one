// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.platform

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for the typed accessors on OAuthSessionResult.Completed and the
 * underlying parseQueryParams helper. Replaces the stringly-typed
 * `callbackUrl.contains("success=true")` checks in the call sites and
 * guards against future regressions like a value containing the string
 * "success=true" being treated as a successful auth.
 */
class OAuthSessionResultTest {

  @Test
  fun typed_accessors_resolve_a_successful_callback() {
    val r = OAuthSessionResult.Completed("klik://oauth-callback?success=true&provider=microsoft")
    assertTrue(r.isSuccess)
    assertEquals("microsoft", r.provider)
    assertNull(r.errorCode)
  }

  @Test
  fun typed_accessors_resolve_a_provider_error_callback() {
    val r = OAuthSessionResult.Completed("klik://oauth-callback?error=access_denied&provider=google")
    assertFalse(r.isSuccess)
    assertEquals("access_denied", r.errorCode)
    assertEquals("google", r.provider)
  }

  @Test
  fun isSuccess_requires_explicit_true_value() {
    // Defense against the prior `contains("success=true")` bug — a value
    // that merely *contains* "success=true" inside another field must NOT
    // be treated as success.
    val r = OAuthSessionResult.Completed("klik://oauth-callback?error=success%3Dtrue_was_lying&provider=x")
    assertFalse(r.isSuccess)
    assertEquals("x", r.provider)
    assertEquals("success=true_was_lying", r.errorCode)
  }

  @Test
  fun missing_query_string_yields_empty_params() {
    val r = OAuthSessionResult.Completed("klik://oauth-callback")
    assertFalse(r.isSuccess)
    assertNull(r.provider)
    assertNull(r.errorCode)
    assertTrue(r.params.isEmpty())
  }

  @Test
  fun params_are_lazily_computed_once() {
    val r = OAuthSessionResult.Completed("klik://oauth-callback?success=true&provider=notion")
    val first = r.params
    val second = r.params
    assertTrue(first === second, "params should be the same lazy reference across calls")
  }

  @Test
  fun parser_handles_percent_encoding_and_plus_for_space() {
    val params = parseQueryParams("klik://x?msg=hello%20world&note=a+b")
    assertEquals("hello world", params["msg"])
    assertEquals("a b", params["note"])
  }

  @Test
  fun parser_skips_malformed_pairs_without_throwing() {
    val params = parseQueryParams("klik://x?ok=1&=missingkey&missingvalue&also=fine")
    assertEquals("1", params["ok"])
    assertEquals("fine", params["also"])
    assertEquals(2, params.size)
  }

  @Test
  fun parser_strips_fragment_before_parsing() {
    val params = parseQueryParams("klik://oauth-callback?success=true&provider=slack#frag-from-redirect")
    assertEquals("true", params["success"])
    assertEquals("slack", params["provider"])
    assertFalse(params.containsKey("frag-from-redirect"))
  }
}
