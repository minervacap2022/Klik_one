// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import io.github.fletchmckee.liquid.samples.app.theme.KlikPrimary
import kotlinx.coroutines.delay
import liquid_root.samples.composeapp.generated.resources.Res
import liquid_root.samples.composeapp.generated.resources.klik_logo
import org.jetbrains.compose.resources.painterResource

@Composable
fun LoadingScreen(
  minDuration: Long = 2000,
  onLoadingFinished: () -> Unit,
) {
  LaunchedEffect(Unit) {
    delay(minDuration)
    onLoadingFinished()
  }

  Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center,
  ) {
    // Hero Logo in MainApp handles the visual.
    // This screen is just for the delay logic.
  }
}
