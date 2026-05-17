package io.github.fletchmckee.liquid.samples.app.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class RuleDto(
    val id: String,
    val source: String,
    @SerialName("nl_text") val nlText: String,
    @SerialName("trigger_label") val triggerLabel: String,
    @SerialName("action_label") val actionLabel: String,
    val status: String,
    @SerialName("is_recurring") val isRecurring: Boolean,
    @SerialName("last_fired_at") val lastFiredAt: String? = null,
    @SerialName("snoozed_until") val snoozedUntil: String? = null,
)

@Serializable
data class RulePreviewDto(
    @SerialName("trigger_label") val triggerLabel: String,
    @SerialName("action_label") val actionLabel: String,
    @SerialName("approximation_note") val approximationNote: String? = null,
    @SerialName("parsed_trigger") val parsedTrigger: JsonObject,
    @SerialName("parsed_action") val parsedAction: JsonObject,
    @SerialName("signal_binding") val signalBinding: String,
)
