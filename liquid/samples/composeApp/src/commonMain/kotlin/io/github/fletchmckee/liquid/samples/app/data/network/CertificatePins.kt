// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.data.network

/**
 * TLS certificate pins for hiklik.ai.
 *
 * Each pin is the Base64-encoded SHA-256 hash of the Subject Public Key Info (SPKI)
 * of a certificate in the server's chain. We pin both the leaf and the intermediate CA
 * so that routine leaf-cert rotation does not break the app as long as the same CA issues it.
 *
 * Current chain (as of 2026-04-10):
 *   Leaf:         CN=hiklik.ai, issued by Google Trust Services WE1
 *   Intermediate: Google Trust Services WE1
 *
 * IMPORTANT: These pins must be updated when the certificate authority changes or when
 * Google Trust Services rotates its intermediate key. Monitor certificate expiration
 * and update pins BEFORE they rotate.
 */
object CertificatePins {

    /** SHA-256 SPKI pin of the hiklik.ai leaf certificate. */
    private const val LEAF_PIN = "SKGnCPNAtqFRixMHV6JcAcjAtd6B3DSF+NJhQBnDIpg="

    /** SHA-256 SPKI pin of the Google Trust Services WE1 intermediate CA. */
    private const val INTERMEDIATE_PIN = "kIdp6NNEd8wsugYyyIYFsi1ylMCED3hZbSR8ZFsa/A4="

    /** Set of all accepted pins in "sha256/<base64>" format. */
    val HIKLIK_PINS: Set<String> = setOf(
        "sha256/$LEAF_PIN",
        "sha256/$INTERMEDIATE_PIN",
    )

    /** Hostname that pinning applies to. */
    const val PINNED_HOSTNAME = "hiklik.ai"
}
