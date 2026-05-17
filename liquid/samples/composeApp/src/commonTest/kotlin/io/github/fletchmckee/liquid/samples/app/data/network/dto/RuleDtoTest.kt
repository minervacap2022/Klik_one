package io.github.fletchmckee.liquid.samples.app.data.network.dto

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.serialization.json.Json

class RuleDtoTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test fun parses_rule_dto() {
        val src = """
          {"id":"r1","source":"user_defined","nl_text":"every Mon 9",
           "trigger_label":"every Mon 9 AM","action_label":"recap",
           "status":"active","is_recurring":true,"last_fired_at":null}
        """.trimIndent()
        val r = json.decodeFromString(RuleDto.serializer(), src)
        assertEquals("r1", r.id)
        assertEquals("user_defined", r.source)
        assertEquals("active", r.status)
        assertNull(r.lastFiredAt)
    }

    @Test fun parses_preview_with_approximation() {
        val src = """
          {"trigger_label":"after every 1:1","action_label":"draft recap",
           "approximation_note":"I'll fire after any meeting labeled 1:1",
           "parsed_trigger":{"type":"meeting_ended","params":{"label_filter":"1:1"}},
           "parsed_action":{"type":"draft_email","params":{"template_hint":"recap"}},
           "signal_binding":"meeting_ended"}
        """.trimIndent()
        val p = json.decodeFromString(RulePreviewDto.serializer(), src)
        assertEquals("meeting_ended", p.signalBinding)
        assertEquals("I'll fire after any meeting labeled 1:1", p.approximationNote)
    }
}
