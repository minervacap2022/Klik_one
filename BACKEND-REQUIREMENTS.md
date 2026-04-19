# Backend Requirements for Klik One

This document lists every backend gap that the Klik One frontend surfaces but
the current `Klik` backend (ssh gcp) does not yet satisfy. Each item lists
**where** in the frontend it's needed, **what** is missing, and a **proposed
endpoint / schema change**.

The frontend already compiles and runs against the legacy shapes — these items
describe upgrades that unlock the full Klik One experience. Nothing here blocks
shipping the current build.

---

## 1. Onboarding role persistence

**Where:** `src/ui/klikone/OnboardingScreen.kt` → `MainApp.kt` at
`"klikone_onboarding"` route completion.

**What's missing:** The picked role (`founder / consultant / sales / pm /
investor / other`) is currently written to `SecureStorage` under
`KlikOneOnboardingKeys.roleKey(userId)`. It never reaches the backend, so the
role is lost if the user re-installs or signs in on another device.

**Proposed:** `POST /api/v1/users/me/profile` (or extend the existing user
update endpoint) with body `{"klik_one_role": "founder"}`. Field stored on the
user record, returned in `GET /api/v1/users/me`. Frontend then mirrors from
the API back into the local cache on sign-in.

---

## 2. `TaskMetadata` → Moves mapping fields

**Where:** `src/ui/klikone/MovesScreen.kt`.

**What's missing:** Moves needs three status buckets — **Needs OK**, **Running**,
**Done**. Today we map as follows:

| Moves bucket | Current filter |
|---|---|
| Needs OK | `status == IN_REVIEW` or `needsConfirmation == true` |
| Running  | `status == IN_PROGRESS` |
| Done     | `status == COMPLETED` |

This is close but imperfect. The HTML mockup shows three specific states —
"Needs your OK · 3", "Running · 2", "Done today · 3". The "Running" bucket in
the mockup is about tasks Klik is **actively executing** with a visible ETA
(e.g. "Update roadmap in Notion · ETA 12 min · Notion"). `IN_PROGRESS` is too
generic.

**Proposed:** Add an `execution` block to the KK_exec todo payload:

```json
{
  "id": "…",
  "status": "RUNNING",
  "execution": {
    "started_at": "2026-04-19T12:15:00Z",
    "eta_seconds": 720,
    "integration": "Notion",
    "progress_hint": "Updating roadmap"
  }
}
```

Frontend will surface `eta_seconds` and `integration` in the Moves Running
card instead of the current `dueInfo` field.

Also missing: **"Done today"** needs an explicit `completed_at` timestamp.
Currently `TaskMetadata.completedInfo` is a pre-formatted display string
("Completed 14:00, Dec 8") — should be an ISO-8601 timestamp the client can
format for the user's timezone.

---

## 3. Move draft preview text

**Where:** `NeedsOkCard` inside `MovesScreen.kt`.

**What's missing:** The HTML mockup shows the draft body of an email or
message ("Hi team, following today's product sync, we're pushing v2.1 to
April 22 and deferring offline mode to v2.2…"). Today we fall back to
`suggestionText ?: description`, neither of which is the actual draft body.

**Proposed:** Add `draft_preview: String?` to the KK_exec todo payload for
tasks of category `a_simple`/`b_apis` that generate an outgoing artifact.
Length-capped to ~280 chars server-side.

---

## 4. Source provenance for Moves cards

**Where:** `NeedsOkCard` footer — "From Product sync · 9:22".

**What's missing:** `TaskMetadata.relatedMeetingId` is a session id, but we
don't have the **session title** or **timestamp of the supporting turn**. We
currently render `"From ${task.relatedProject.ifBlank { "meeting" }}"` which
is fuzzy.

**Proposed:** Add `source_meeting_title: String?` and
`source_segment_timestamp: String?` (e.g. `"9:22"`) to the task payload.

---

## 5. Network "Seen this week" signal

**Where:** `src/ui/klikone/NetworkScreen.kt` — `seenThisWeek: List<Pair<Person, Int>>`.

**What's missing:** No endpoint currently returns *which* people appeared in
sessions over the last 7 days, nor the session count per person. Frontend
currently passes `emptyList()` from `MainApp.kt`.

**Proposed:** `GET /api/v1/people/weekly?days=7` returning:

```json
[
  {"person_id": "p_ab12", "session_count": 4, "last_seen": "2026-04-18T14:30:00Z"},
  …
]
```

Ordered by `session_count desc`. Frontend joins to the already-cached Person
list by `person_id`.

---

## 6. Network "Needs attention" derivation

**Where:** `NetworkScreen.kt` → `needsAttention: List<NetworkAttentionItem>`.

**What's missing:** The amber cards ("Priya Kumar — you said you'd follow up
12 days ago", "Rafael Nunez — promised to share deck last Tuesday") are a
derived signal from unfulfilled commitments. No endpoint exposes this today.

**Proposed:** `GET /api/v1/people/attention` returning:

```json
[
  {
    "person_id": "p_ab12",
    "name": "Priya Kumar",
    "reason_type": "unfulfilled_followup",
    "reason_text": "You said you'd follow up 12 days ago",
    "origin_segment_id": "seg_123",
    "origin_session_id": "sess_xyz",
    "suggested_action": "send_followup_email",
    "age_days": 12
  }
]
```

The `reason_type` should be an enum so the client can decide button labels
("Klik it" vs "Nudge" vs "Schedule").

---

## 7. Session Detail — 3-line summary

**Where:** `src/ui/klikone/SessionDetailScreen.kt` →
`threeLineSummary: String?`.

**What's missing:** `Meeting.summary` is a long-form paragraph. The HTML
mockup shows a terse 3-line digest ("Team pushed v2.1 to April 22 and dropped
offline mode from MVP. Sarah leads QA sprint planning. 2 timeline concerns
flagged."). We need a structured short summary distinct from the long
summary.

**Proposed:** Add `three_line_summary: String?` to the meeting payload.
Backend generates it from the transcript with an LLM call — the same step
that produces decisions / open questions today.

---

## 8. Session Detail — Decisions

**Where:** `SessionDetailScreen.kt` → `decisions: List<SessionDecision>`.

**What's missing:** No structured list of decisions in the Meeting entity.
The HTML shows:

| Decision | Stamp | Attribution |
|---|---|---|
| Push v2.1 release to April 22 | 14:32 | Jordan, Sarah |
| Drop offline mode from MVP scope | 21:15 | unanimous |
| Sarah leads QA sprint planning | 35:04 | assigned by Jordan |

**Proposed:** Add to the meeting payload:

```json
"decisions": [
  {
    "id": "dec_1",
    "text": "Push v2.1 release to April 22",
    "transcript_timestamp": "14:32",
    "attributed_to": ["Jordan Lee", "Sarah Kim"],
    "attribution_kind": "jointly"  // "unanimous" | "jointly" | "assigned" | "solo"
  }
]
```

---

## 9. Session Detail — Open questions

**Where:** `SessionDetailScreen.kt` → `openQuestions: List<String>`.

**What's missing:** Same as above — no structured list. Today there's no
place in the Meeting entity to surface open loops.

**Proposed:** `"open_questions": ["Will QA have bandwidth for the accelerated timeline?", …]`
as a top-level array on the meeting payload. Keep it strings for now; a
future extension can add `asked_by`, `directed_at`, etc.

---

## 10. Session Detail — Mentioned entities

**Where:** `SessionDetailScreen.kt` → `mentioned: List<String>`.

**What's missing:** The HTML shows small entity chips — people
("Marcus R."), projects ("v2.1 Release", "Offline Mode"), orgs ("Acme Corp"),
and integrations ("Notion") that were referenced in the session.

Today the meeting entity has `participants` (people who were physically
attendees) but nothing for *mentioned* entities.

**Proposed:** Add `mentioned_entities` array on the meeting payload:

```json
"mentioned_entities": [
  {"id": "p_mr", "type": "person", "display": "Marcus R."},
  {"id": "prj_v2", "type": "project", "display": "v2.1 Release"},
  {"id": "prj_offline", "type": "project", "display": "Offline Mode"},
  {"id": "org_acme", "type": "organization", "display": "Acme Corp"},
  {"id": "int_notion", "type": "integration", "display": "Notion"}
]
```

Frontend will color-code by `type` using the Klik One palette.

---

## 11. `TodoItem` assignee + due date

**Where:** `SessionDetailScreen.kt` → `TodosBody`.

**What's missing:** `TodoItem` currently has only `id`, `text`, `isCompleted`,
`type`. The HTML implies subtitles like "Sarah · by Friday". We dropped
those lines for now because the fields don't exist.

**Proposed:** Extend `TodoItem`:

```kotlin
data class TodoItem(
    val id: String,
    val text: String,
    val isCompleted: Boolean = false,
    val type: TodoType = TodoType.TODO,
    val assignee: String? = null,      // NEW — display name
    val assigneePersonId: String? = null,  // NEW — entity link
    val dueDate: String? = null,       // NEW — ISO date or human-readable
)
```

Backend populates these from the transcript LLM pass.

---

## 12. Live Recording — transcript stream

**Where:** `src/ui/klikone/LiveRecordingScreen.kt` →
`recentTurns: List<LiveCaptureTurn>`, `detections: List<LiveKlikDetection>`.

**What's missing:** The frontend has no live channel for transcript turns
during a recording. `FixedSessionAudioStreamer` streams audio *out* but there
is no paired *in* stream delivering recognized turns back.

**Proposed:** WebSocket (or SSE) endpoint
`GET /api/v1/sessions/{session_id}/live`
pushing events:

```json
{ "type": "turn", "id": "t_1", "speaker_id": "S1", "speaker_label": "Speaker 1",
  "timestamp": "12:32", "text": "…so if we move the deadline…" }

{ "type": "detection", "id": "d_1", "kind": "POSSIBLE DECISION",
  "summary": "Move deadline to next Friday to include offline sync",
  "origin_turn_id": "t_1" }

{ "type": "speaker_resolved", "speaker_id": "S1", "resolved_to_person_id": "p_jl" }

{ "type": "duration", "elapsed_seconds": 767 }
```

Frontend subscribes on session start and renders live. `elapsed_seconds` is
authoritative so clock drift between device and server doesn't matter.

---

## 13. Live Recording — speaker voiceprint → name mapping

**Where:** `LiveCaptureTurn.speakerLabel` — currently "Speaker 1".

**What's missing:** Once the voiceprint system resolves a speaker to a known
person (under BIPA consent), the UI needs to update the label from "Speaker 1"
to the person's first name. Requires the `speaker_resolved` event shown
above.

**Proposed:** Part of the WebSocket schema in §12.

---

## 14. Ask Klik — structured source metadata

**Where:** `src/ui/klikone/AskKlikSheet.kt` →
`NestedCommitmentCard` and the `buildSourceLabel()` helper.

**What's missing:** The HTML shows nested commitment cards like "Lead QA
sprint planning / Assigned at 9:35 · before Friday". Today we read from
`ChatSource.metadata["date"]` / `metadata["timestamp"]` / `metadata["subtitle"]`
as free-form string bags. This is fragile.

**Proposed:** Extend `ChatSource`:

```kotlin
data class ChatSource(
    val id: String,
    val type: ChatSourceType,
    val title: String,
    val content: String? = null,
    val sessionId: String? = null,
    val score: Float = 0f,
    val metadata: Map<String, String> = emptyMap(),
    // NEW fields:
    val subtitle: String? = null,      // e.g. "Assigned at 9:35 · before Friday"
    val displayDate: String? = null,   // e.g. "Apr 15"
    val segmentTimestamp: String? = null,  // e.g. "9:35" — jump target into transcript
)
```

Backend fills from the citation metadata in the RAG response. `subtitle`
supersedes the `metadata["subtitle"]` string bag.

---

## 15. "Klik it" action resolver

**Where:** `NetworkScreen.kt` amber attention cards, plus Ask Klik follow-up
chips ("Remind me Friday", "Nudge Sarah", "Add to my list").

**What's missing:** These chips currently invoke the passed-in callback but
the callback has no backend to call. There's no endpoint that says "given
this person and this reason, what's the right follow-up action and payload?"

**Proposed:** `POST /api/v1/actions/resolve` with body:

```json
{
  "intent": "nudge" | "remind" | "followup" | "add_task" | "schedule",
  "subject_type": "person" | "commitment" | "decision" | "question",
  "subject_id": "…",
  "due_hint": "friday" | null
}
```

Returns a fully-formed KK_exec todo draft that the user can approve.

---

## 16. Session Detail — attendee management

**Where:** `SessionDetailScreen.kt` → `onAddAttendee` callback.

**What's missing:** The "+ Add" pill lets users retrospectively attribute
attendees who weren't captured. No endpoint exists.

**Proposed:** `POST /api/v1/sessions/{session_id}/attendees` with body
`{"person_id": "…"}` or `{"name": "…"}` (the latter creates a Person record
on the fly).

---

## 17. Push notifications for Klik moves

**Where:** Push notification service (iOS APNs) — not a UI file, but part of
the Klik One experience ("Klik handled 3 moves while you were offline" style
notifications).

**What's missing:** Current `PushNotificationService` exists but there's no
backend pipeline that generates Moves-specific notifications when a todo
transitions to `IN_REVIEW` (needing user approval) or `COMPLETED` (by
KK_exec on the user's behalf).

**Proposed:** KK_exec status-change hooks emit APNs pushes with category
`klikone.move.needs_ok` / `klikone.move.completed`. Payload includes the
`todo_id` so tapping the notification can deep-link via the existing
`DeepLinkHandler`.

---

## 18. Subscription gating

**Where:** `PricingScreen.kt` (already wired) + potentially Moves/Network
limits.

**What's missing:** The current subscription tiers (starter/basic/pro) gate
goals + risk_analysis. For Klik One, free-tier users probably need to be
capped on **number of Moves executed per month** and **number of captures
per month** rather than the feature being binary-gated.

**Proposed:** Extend `SubscriptionFeatures`:

```json
{
  "moves_executed_this_month": 12,
  "moves_per_month_limit": 30,
  "captures_this_month": 4,
  "captures_per_month_limit": 10
}
```

Frontend will surface usage on ProfileScreen.

---

## Priority order

If backend resources are limited, tackle in this order — each row unblocks
the largest user-visible chunk of Klik One:

1. §12 Live Recording WebSocket (blocks LiveRecording being useful at all)
2. §7 + §8 + §9 Session Detail 3-line summary + decisions + open questions
3. §14 ChatSource structured subtitle / segmentTimestamp
4. §5 + §6 Network signals (weekly + attention)
5. §2 + §3 Moves execution block + draft preview
6. §11 TodoItem assignee + due date
7. §1 Onboarding role persistence
8. §17 Push notifications for Klik moves
9. §15 Action resolver
10. The rest
