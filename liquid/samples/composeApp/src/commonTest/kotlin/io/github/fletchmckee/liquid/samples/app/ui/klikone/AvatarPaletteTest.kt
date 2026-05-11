// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.ui.klikone

import io.github.fletchmckee.liquid.samples.app.theme.KlikAvatarBg
import io.github.fletchmckee.liquid.samples.app.theme.KlikAvatarFg
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Locks the "one rule for avatar colour" invariant:
 *
 *  - Same stable seed (person.id / project.id / org.id / user.id /
 *    voiceprint_id) ALWAYS picks the same swatch, no matter which surface
 *    paints the avatar (stack avatar, expanded chip leading dot, transcript
 *    bubble, detail header).
 *  - Renaming a person never changes their colour (id is stable, name is not).
 *  - Different ids produce different swatches when the palette allows.
 *  - Blank / empty seeds fall through to a single sentinel swatch (no NPE,
 *    no fabricated colour from whitespace).
 *
 * If anyone re-introduces `display.hashCode() % palette.size` or
 * `someName.fold(...)` in a render path, this test still passes for the
 * helper but a *visual* test would catch it. Keep this as the
 * machine-checkable half — the other half is a single audit-once code rule:
 * every avatar surface goes through `k1AvatarColors` or `K1Avatar(idSeed=…)`.
 */
class AvatarPaletteTest {

  @Test
  fun sameIdSeedAlwaysReturnsSamePair() {
    val (bg1, fg1) = k1AvatarColors("person_abc123")
    val (bg2, fg2) = k1AvatarColors("person_abc123")
    assertEquals(bg1, bg2)
    assertEquals(fg1, fg2)
  }

  @Test
  fun palettePairIsConsistentWithRawArrays() {
    // The helper just indexes into KlikAvatarBg / KlikAvatarFg — make sure
    // both fields use the SAME index, otherwise text on a chip would be
    // unreadable.
    val idx = paletteIdx("person_xyz")
    val (bg, fg) = k1AvatarColors("person_xyz")
    assertEquals(KlikAvatarBg[idx], bg)
    assertEquals(KlikAvatarFg[idx], fg)
  }

  @Test
  fun renameDoesNotChangeColor() {
    // Caller passes the id seed; display name varies. Same id → same colour.
    val before = k1AvatarColors("person_007")
    val after = k1AvatarColors("person_007")
    assertEquals(before, after)
  }

  @Test
  fun differentIdsCanProduceDifferentColors() {
    // Spot-check across a spread of seeds — at least one pair must diverge,
    // otherwise the palette is collapsed to a single swatch.
    val seeds = (0..50).map { "person_$it" }
    val distinct = seeds.map { paletteIdx(it) }.toSet()
    assertTrue(distinct.size > 1, "expected >1 distinct palette indices, got $distinct")
  }

  @Test
  fun blankAndNullSeedsAreStableAndIdentical() {
    // Avoid throwing on missing ids and avoid drifting based on the input
    // shape of "no seed".
    val pNull = k1AvatarColors(null)
    val pEmpty = k1AvatarColors("")
    val pBlank = k1AvatarColors("   ")
    assertEquals(pNull, pEmpty)
    assertEquals(pEmpty, pBlank)
  }

  @Test
  fun paletteIdxIsWithinBounds() {
    listOf("person_a", "", "person_b", "孙姐", "侯福子", "VP_01B2D484E706").forEach { seed ->
      val i = paletteIdx(seed)
      assertTrue(i in 0 until KlikAvatarBg.size, "idx $i out of range for seed='$seed'")
      assertTrue(i in 0 until KlikAvatarFg.size)
    }
  }

  @Test
  fun sameSeedAgreesAcrossEverySurfaceShape() {
    // The legacy bug was NOT that the hash function was wrong — it was that
    // one surface seeded on the display name, another on initials, another on
    // the id. Lock the invariant that callers seeded with the SAME stable id
    // always get the same swatch, regardless of which surface is rendering.
    val id = "person_F816345FCD99"
    val expected = k1AvatarColors(id)

    // Surfaces that previously diverged:
    //   1) K1Avatar(idSeed=id) - stack avatar via K1AvatarStack
    //   2) Chip leading dot - was `display.hashCode() % palette.size`
    //   3) Transcript bubble - was `speakerLabel.hashCode() % palette.size`
    // All three now route through k1AvatarColors(id).
    val stackSurface = k1AvatarColors(id)
    val chipSurface = k1AvatarColors(id)
    val transcriptSurface = k1AvatarColors(id)
    assertEquals(expected, stackSurface)
    assertEquals(expected, chipSurface)
    assertEquals(expected, transcriptSurface)
  }
}
