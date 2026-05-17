// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.platform

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for OnceGate, the guard used inside iOS PHPicker's delegate to keep
 * cont.resume(...) from firing twice when picker(_:didFinishPicking:) is
 * delivered more than once (a known iOS quirk where dismissal animation
 * completion can trigger a second invocation with an empty result array).
 */
class OnceGateTest {

  @Test
  fun first_tryEnter_returns_true() {
    val gate = OnceGate()
    assertTrue(gate.tryEnter())
  }

  @Test
  fun second_tryEnter_returns_false() {
    val gate = OnceGate()
    gate.tryEnter()
    assertFalse(gate.tryEnter())
  }

  @Test
  fun many_calls_still_only_first_succeeds() {
    val gate = OnceGate()
    val results = (1..10).map { gate.tryEnter() }
    assertTrue(results.first())
    assertTrue(results.drop(1).all { !it })
  }
}
