// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.platform

/**
 * Single-shot gate. The first call to [tryEnter] returns true; every subsequent
 * call returns false. Used to make iOS UIKit/PhotosUI delegate callbacks
 * idempotent when the system can deliver them more than once (e.g. PHPicker's
 * picker(_:didFinishPicking:) firing both with the selection and again on
 * dismissal animation completion), which would otherwise double-resume a
 * suspendCoroutine continuation and crash with IllegalStateException.
 *
 * Not thread-safe by design — UIKit/PhotosUI delegate methods run on the main
 * thread. If used elsewhere, wrap in atomicfu.
 */
class OnceGate {
  private var entered = false

  fun tryEnter(): Boolean {
    if (entered) return false
    entered = true
    return true
  }
}
