// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("DEPRECATION") // UIApplication.keyWindow — same rationale as ShareService.ios.kt
@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
package io.github.fletchmckee.liquid.samples.app.platform

import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSItemProvider
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIViewController
import platform.darwin.NSObject

/**
 * iOS PHPicker-based implementation. Returns a JPEG re-encoded at ~1024px max
 * edge, q≈0.85, suitable for upload as a profile avatar.
 */
actual object ImagePicker {

  private const val MAX_EDGE: Double = 1024.0
  private const val JPEG_QUALITY: Double = 0.85
  private const val PUBLIC_IMAGE_UTI: String = "public.image"

  private var delegateHolder: NSObject? = null

  actual suspend fun pickAvatar(): PickedImage? = suspendCoroutine { cont ->
    val config = PHPickerConfiguration().apply {
      setSelectionLimit(1)
      setFilter(PHPickerFilter.imagesFilter)
    }
    val picker = PHPickerViewController(configuration = config)

    val delegate = object : NSObject(), PHPickerViewControllerDelegateProtocol {
      override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        picker.dismissViewControllerAnimated(true, completion = null)

        val first = didFinishPicking.firstOrNull() as? PHPickerResult
        if (first == null) {
          delegateHolder = null
          cont.resume(null)
          return
        }
        val provider: NSItemProvider = first.itemProvider

        provider.loadDataRepresentationForTypeIdentifier(PUBLIC_IMAGE_UTI) { data: NSData?, error: NSError? ->
          if (data == null) {
            KlikLogger.e("ImagePicker", "loadDataRepresentation failed: ${error?.localizedDescription}")
            delegateHolder = null
            cont.resume(null)
            return@loadDataRepresentationForTypeIdentifier
          }
          val image = UIImage.imageWithData(data)
          if (image == null) {
            KlikLogger.e("ImagePicker", "UIImage decode failed (${data.length} bytes)")
            delegateHolder = null
            cont.resume(null)
            return@loadDataRepresentationForTypeIdentifier
          }
          val downscaled = downscale(image, MAX_EDGE)
          val jpeg: NSData? = UIImageJPEGRepresentation(downscaled, JPEG_QUALITY)
          if (jpeg == null) {
            KlikLogger.e("ImagePicker", "JPEG encode failed")
            delegateHolder = null
            cont.resume(null)
            return@loadDataRepresentationForTypeIdentifier
          }
          val bytes = jpeg.toByteArray()
          delegateHolder = null
          cont.resume(
            PickedImage(bytes = bytes, mimeType = "image/jpeg", fileName = "avatar.jpg"),
          )
        }
      }
    }

    delegateHolder = delegate
    picker.delegate = delegate

    val root = topViewController()
    if (root == null) {
      KlikLogger.e("ImagePicker", "No root VC to present from")
      delegateHolder = null
      cont.resume(null)
      return@suspendCoroutine
    }
    root.presentViewController(picker, animated = true, completion = null)
  }

  private fun topViewController(): UIViewController? {
    val rootVC = UIApplication.sharedApplication.keyWindow?.rootViewController
    var top: UIViewController? = rootVC
    while (top?.presentedViewController != null) top = top.presentedViewController
    return top
  }

  private fun downscale(src: UIImage, maxEdge: Double): UIImage {
    val (w, h) = src.size.useContents { width to height }
    val longest = if (w > h) w else h
    if (longest <= maxEdge) return src
    val scale = maxEdge / longest
    val newSize = CGSizeMake(w * scale, h * scale)
    UIGraphicsBeginImageContextWithOptions(newSize, false, 1.0)
    src.drawInRect(CGRectMake(0.0, 0.0, w * scale, h * scale))
    val out = UIGraphicsGetImageFromCurrentImageContext() ?: src
    UIGraphicsEndImageContext()
    return out
  }
}

private fun NSData.toByteArray(): ByteArray {
  val len = this.length.toInt()
  val out = ByteArray(len)
  if (len > 0) {
    out.usePinned { pinned ->
      platform.posix.memcpy(pinned.addressOf(0), this.bytes, this.length)
    }
  }
  return out
}
