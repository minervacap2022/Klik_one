// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.demos.webview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.fletchmckee.liquid.samples.app.common.SliderScaffold

@Composable
fun LiquidWebViewScreen(
  modifier: Modifier = Modifier,
  onBackClick: (() -> Unit)? = null,
  url: String = "https://www.google.com",
) = SliderScaffold(
  modifier = modifier,
  onBackClick = onBackClick,
) { paddingValues ->
  // WebView disabled - cinterop incompatible with Xcode 26 beta
  Box(
    modifier = modifier.padding(paddingValues).fillMaxSize(),
    contentAlignment = Alignment.Center,
  ) {
    Text("WebView unavailable: $url")
  }
}
