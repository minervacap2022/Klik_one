// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.ui.klikone

import io.github.fletchmckee.liquid.samples.app.theme.fontSizeScaleFor
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Guards the stable mapping of wire-index → size multiplier used by K1Type
 * and MainApp. The four steps (S/M/L/XL) and their exact scale ratios are
 * part of the K1 design spec — any drift breaks cross-device layout parity.
 */
class FontSizeScaleTest {

  @Test fun indexZeroGivesSmall() = assertEquals(0.85f, fontSizeScaleFor(0))

  @Test fun indexOneGivesNormal() = assertEquals(1.0f, fontSizeScaleFor(1))

  @Test fun indexTwoGivesLarge() = assertEquals(1.15f, fontSizeScaleFor(2))

  @Test fun indexThreeGivesXLarge() = assertEquals(1.3f, fontSizeScaleFor(3))

  @Test fun negativeClampsToSmall() = assertEquals(0.85f, fontSizeScaleFor(-1))

  @Test fun beyondMaxClampsToXLarge() = assertEquals(1.3f, fontSizeScaleFor(99))
}
