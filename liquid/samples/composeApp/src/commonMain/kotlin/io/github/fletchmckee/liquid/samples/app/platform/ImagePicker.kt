// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.platform

/**
 * A picked image, downscaled and re-encoded to JPEG by the platform impl.
 */
data class PickedImage(
  val bytes: ByteArray,
  val mimeType: String,
  val fileName: String,
)

/**
 * Platform-specific image picker (camera roll / photos library).
 * iOS: PHPickerViewController (iOS 14+).
 * Android: ActivityResultContracts.PickVisualMedia.
 */
expect object ImagePicker {
  /**
   * Present the system photo picker. Resolves with the picked image (downscaled
   * to ~1024px on the longest edge, re-encoded as JPEG with q≈0.85), or null
   * if the user cancelled.
   */
  suspend fun pickAvatar(): PickedImage?
}
