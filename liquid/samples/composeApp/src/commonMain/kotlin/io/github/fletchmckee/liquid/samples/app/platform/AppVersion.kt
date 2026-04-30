// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.platform

/**
 * Platform-provided app version metadata, sourced from the bundled
 * Info.plist (iOS) or BuildConfig (Android). Read on demand by the
 * settings screen so the displayed version always tracks the IPA/APK
 * the user actually installed — no commonMain hardcoded constants.
 */
expect object AppVersion {
  /** Marketing version, e.g. "1.1". */
  val marketing: String

  /** Build number, e.g. "3". */
  val build: String
}
