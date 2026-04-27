// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun LiquidDemos(
  modifier: Modifier = Modifier,
  startDestination: Any = "today",
  useLiquid: Boolean = true,
  initialFrost: Float = 0f,
  initialDispersion: Float = 0f,
  isBenchmark: Boolean = false,
) {
  // Redirect to the new MainApp which contains the entire Klik Liquid OS implementation.
  MainApp()
}
