// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.resources

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import io.github.fletchmckee.liquid.samples.app.R

@Composable
actual fun klikLogoPainter(): Painter = painterResource(R.drawable.klik_logo)
