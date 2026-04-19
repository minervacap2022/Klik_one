// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app

interface Platform {
  val name: String
}

expect fun getPlatform(): Platform

expect fun displayNavIcons(): Boolean
