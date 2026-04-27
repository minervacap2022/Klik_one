// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.resources

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import liquid_root.samples.composeapp.generated.resources.Res
import liquid_root.samples.composeapp.generated.resources.klik_logo
import org.jetbrains.compose.resources.painterResource

@Composable
actual fun klikLogoPainter(): Painter = painterResource(Res.drawable.klik_logo)
