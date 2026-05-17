// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Locks the contract of [extractFastApiDetail]: it must handle both FastAPI
 * detail shapes (string and array of {loc,msg,type}) without throwing. The
 * array case is the regression that previously crashed the avatar 422 path.
 */
class AuthRepositoryFastApiDetailTest {

  @Test fun extracts_string_detail() {
    val r = extractFastApiDetail("""{"detail":"Bad request"}""")
    assertEquals("Bad request", r)
  }

  @Test fun extracts_array_detail_messages() {
    val r = extractFastApiDetail(
      """{"detail":[{"loc":["body","file"],"msg":"field required","type":"value_error.missing"}]}""",
    )
    assertEquals("field required", r)
  }

  @Test fun multiple_array_messages_joined() {
    val r = extractFastApiDetail("""{"detail":[{"msg":"a"},{"msg":"b"}]}""")
    assertEquals("a; b", r)
  }

  @Test fun absent_detail_returns_null() {
    val r = extractFastApiDetail("""{"foo":"bar"}""")
    assertNull(r)
  }

  @Test fun malformed_json_returns_null() {
    val r = extractFastApiDetail("not json")
    assertNull(r)
  }
}
