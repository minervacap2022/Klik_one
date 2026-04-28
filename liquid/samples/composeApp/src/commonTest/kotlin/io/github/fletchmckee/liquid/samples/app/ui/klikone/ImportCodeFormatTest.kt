// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.ui.klikone

import io.github.fletchmckee.liquid.samples.app.presentation.importcode.formatCountdown
import io.github.fletchmckee.liquid.samples.app.presentation.importcode.formatImportCode
import kotlin.test.Test
import kotlin.test.assertEquals

class ImportCodeFormatTest {

  @Test
  fun `formatImportCode splits 6 digits into two groups of three`() {
    assertEquals("123 456", formatImportCode("123456"))
    assertEquals("000 000", formatImportCode("000000"))
  }

  @Test
  fun `formatImportCode passes shorter strings through unchanged`() {
    assertEquals("12345", formatImportCode("12345"))
    assertEquals("", formatImportCode(""))
  }

  @Test
  fun `formatCountdown renders mmss for typical values`() {
    assertEquals("30:00", formatCountdown(1800))
    assertEquals("05:09", formatCountdown(309))
    assertEquals("00:01", formatCountdown(1))
    assertEquals("00:00", formatCountdown(0))
  }

  @Test
  fun `formatCountdown clamps negatives to zero`() {
    assertEquals("00:00", formatCountdown(-5))
  }
}
