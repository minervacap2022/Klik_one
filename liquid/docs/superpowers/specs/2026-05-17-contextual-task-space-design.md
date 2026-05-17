# Contextual Task Space — Design

**Date:** 2026-05-17
**Status:** Approved for planning
**Owner:** Wilson Xu

## Goal

Turn the existing **Featured** section on the Moves screen from "three daily AI guesses" into a **persistent task space** of rules that fire as Move cards on a schedule or in response to context.

Rules are either **Klik-inferred** (the existing daily inference, but now persisted with user approval) or **user-defined** (typed in natural language). Both kinds live in the same store and surface through the same card stream.

## Non-goals

- A new top-level navigation tab. The Featured slot is the home.
- Replacing or deleting `KK_suggest`. We extend it in place.
- A generic event bus. Each new signal type is added as a dedicated evaluator.
- Auto-execution without a card. Every firing still produces a Moves card that the user taps Start on. The KK_exec flow downstream of Start is unchanged.

## Concepts

| Term | Meaning |
|---|---|
| Rule | A persistent, editable row in the task space. Has a trigger and an action. |
| Firing | One activation of a rule. Produces one Moves card. |
| Pending rule | A Klik-inferred rule the user has not yet accepted. Renders as a card with Accept/Decline. |
| Signal | A trigger type the backend can actually detect (cron, meeting-ended, etc.). |
| Approximation | When the user's NL trigger doesn't map to a known signal, the closest available signal is picked and the user confirms. |

## Architecture

Single service: **`KK_suggest`**, extended. No new service. Rationale: faster ship, no new operational surface. The known cost is that `KK_suggest` becomes dual-purpose (daily inference + rule lifecycle + trigger evaluation). We accept that for v1.

### Backend components

```
┌─────────────────────────────────────────────────────────┐
│                       KK_suggest                        │
│                                                         │
│  ┌──────────────────┐    ┌────────────────────────┐    │
│  │  REST endpoints  │    │   NL → rule mapper     │    │
│  │  (rules CRUD,    │───▶│   (KK_LLM call)        │    │
│  │   preview,       │    └────────────────────────┘    │
│  │   featured)      │                                  │
│  └──────────────────┘                                  │
│           │                                            │
│           ▼                                            │
│  ┌──────────────────────────────────────────────┐     │
│  │  featured_rules  ◀──── daily_inference cron  │     │
│  │  featured_tasks  ◀──── trigger evaluators     │     │
│  └──────────────────────────────────────────────┘     │
│                            ▲                          │
│                            │                          │
│  ┌─────────────────────────┴──────────────────────┐   │
│  │  Trigger evaluators (workers)                  │   │
│  │  - cron evaluator       (per-minute scan)      │   │
│  │  - meeting_ended hook   (KK_session events)    │   │
│  │  - state-diff scanner   (nightly)              │   │
│  │  - daily_inference      (= existing 6am cron)  │   │
│  └────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
                              │
                              ▼
                      iOS Klik_one
                  (Moves → Featured)
```

## Schema

### `featured_rules` (new — persistent task space)

```sql
CREATE TABLE featured_rules (
  id              uuid PRIMARY KEY,
  user_id         uuid NOT NULL,
  source          text NOT NULL CHECK (source IN ('klik_inferred','user_defined')),
  nl_text         text NOT NULL,
  parsed_trigger  jsonb NOT NULL,    -- {type, params, approximation_note}
  parsed_action   jsonb NOT NULL,    -- {type, params}
  signal_binding  text NOT NULL,     -- which evaluator owns this rule
  trigger_label   text NOT NULL,     -- human-readable, for UI
  action_label    text NOT NULL,     -- human-readable, for UI
  is_recurring    boolean NOT NULL,
  status          text NOT NULL CHECK (status IN ('pending_review','active','paused','archived')),
  last_fired_at   timestamptz,
  created_at      timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX featured_rules_user_status ON featured_rules(user_id, status);
CREATE INDEX featured_rules_signal_active ON featured_rules(signal_binding) WHERE status = 'active';
```

### `featured_tasks` (existing — repurposed as *firings*)

Each row is now one firing of one rule. Migration:

```sql
ALTER TABLE featured_tasks
  ADD COLUMN rule_id uuid REFERENCES featured_rules(id) ON DELETE CASCADE;
-- predicted_for retained for back-compat during migration; new writes leave it NULL.
```

A firing carries the card content (`title, description, category, priority, reasoning`) so the cards endpoint can hydrate without joining back through the rule for every read.

## NL → rule pipeline

Single KK_LLM call. System prompt (sketch):

```
You are mapping a user's natural-language rule to a structured trigger + action.

Available trigger types:
  - cron            { expr: "0 9 * * 1" }
  - meeting_ended   { participant_filter?: string, label_filter?: string }
  - person_silence  { days: int, person_filter?: string }
  - okr_threshold   { goal_filter?: string, percent_below: int }
  - daily_inference (system-only)

Available action types:
  - draft_email   { template_hint: string }
  - summarize     { source: 'last_week_meetings' | 'specific_session', ... }
  - remind        { text: string }
  - notify_only   { text: string }
  - exec_todo     { description: string }   // routes to KK_exec

Output JSON only:
  { trigger, action, approximation_note, trigger_label, action_label }

If the user's intent doesn't map cleanly, pick the closest available signal
and explain via approximation_note. Never invent trigger or action types.
```

Approximation behavior: the mapper never refuses. The frontend renders `approximation_note` on the preview card so the user sees exactly what Klik will actually do before confirming.

## REST surface

All under `/api/suggest/v1`.

| Endpoint | Purpose |
|---|---|
| `POST /rules/preview` | NL in → parsed preview out. Does not persist. |
| `POST /rules` | Accept preview, persist as `status='active'` (user_defined). |
| `GET /rules` | List the caller's rules. |
| `PATCH /rules/{id}` | Edit. If `nl_text` changes, re-parse. |
| `DELETE /rules/{id}` | Remove. Cascades to `featured_tasks`. |
| `POST /rules/{id}/accept` | Promote `pending_review` → `active`. For Klik-inferred proposals. |
| `GET /featured` | **Unchanged shape**. Returns active firings, enriched with `rule_id` + `trigger_label`. |

## Trigger evaluators

Each signal type is a worker process inside `KK_suggest`. They share the DB. Each writes a `featured_tasks` row when it decides a rule should fire.

- **cron evaluator** — one-per-minute scan over `featured_rules WHERE status='active' AND parsed_trigger->>'type'='cron'`. Evaluates the cron expression against current time in the user's timezone. Idempotent via `last_fired_at` watermark.
- **meeting_ended hook** — subscribes to KK_session "session.completed" events (wiring is part of v1 scope). For each event, scans matching rules, applies filters, emits cards.
- **state-diff scanner** — nightly. Computes per-rule conditions like `person_silence` (days since last contact) and `okr_threshold` (goal progress %). Emits cards for rules whose condition newly became true (transition-based, not steady-state, to avoid re-firing every night).
- **daily_inference evaluator** — the existing 6 AM KK_suggest cron, unchanged internally. The only delta: it writes to `featured_rules` with `status='pending_review'` instead of writing cards to `featured_tasks` directly.

## iOS frontend

### Moves screen — Featured section

- Section header gets a **"+"** button → opens `NewRuleSheet`.
- Each card gains a small inline label showing `trigger_label` (e.g. *"every Mon 9 AM"*, *"after 1:1s"*).
- **Long-press** card → menu: *Edit rule · Pause · Delete · Snooze 7d*.
- **Pending Klik-inferred rules** render with **Accept / Decline** in place of Start / Skip. Accept → status flips to `active`; Decline → row archived. Once accepted, the rule's *first* firing also creates a card so the user can immediately Start it.

### `NewRuleSheet` (bottom sheet)

- Multiline `TextField`, capitalize sentences.
- Example chips below the field, e.g. *"After every 1:1, draft a recap email"*, *"Every Monday 9 AM, summarize last week"*, *"When I haven't talked to a key contact in 14 days"*.
- Submit → loading → preview card:
  ```
  Klik will: <action_label>
  When: <trigger_label>
  ⚠ I couldn't detect <X> exactly, so I'll <Y>. OK?    (only if approximation_note present)
  ```
- Confirm → `POST /rules` → sheet dismisses → new rule appears in Featured.

### New types

```kotlin
data class RuleDto(
  val id: String,
  val source: String,           // "klik_inferred" | "user_defined"
  val nlText: String,
  val triggerLabel: String,
  val actionLabel: String,
  val status: String,           // "pending_review" | "active" | "paused" | "archived"
  val isRecurring: Boolean,
  val lastFiredAt: String?,
)

data class RulePreviewDto(
  val triggerLabel: String,
  val actionLabel: String,
  val approximationNote: String?,
  val parsedTrigger: JsonObject,
  val parsedAction: JsonObject,
)
```

New repository: `RulesRepository` (domain + impl), called from a new `RulesViewModel`.

`FeaturedTaskDto` gains `ruleId: String?` and `triggerLabel: String?`. Existing cards keep working when these are null.

## Failure modes

| Failure | Behavior |
|---|---|
| LLM returns invalid JSON | `POST /rules/preview` returns 422 with the raw model output for debugging. Sheet shows *"Couldn't parse, try rephrasing."* |
| Evaluator crashes | Rule stays armed; `last_fired_at` watermark prevents duplicate firings on restart. |
| Approximation mismatches user intent | User long-presses the rule → Edit → re-types NL → re-parse. |
| Signal not yet wired | Mapper picks closest available signal; never produces a silently-dead rule. |
| KK_session event delivery delayed | meeting_ended evaluator is event-driven, so delay is bounded by the event bus. No periodic polling fallback in v1. |

## Testing

### Backend
- Unit: NL mapper against a fixture set of NL prompts (mock KK_LLM with golden outputs).
- Unit: each evaluator's `should_fire(rule, context)` as a pure function.
- Integration: cron evaluator fires → row appears in `featured_tasks` → visible via `GET /featured`.
- Integration: `POST /rules` for a user-defined rule, then trigger its signal in test, assert card emitted.
- Migration: `predicted_for` left intact for old rows; new flow writes `rule_id` populated, `predicted_for` null.

### iOS
- `RuleDtoTest`, `RulePreviewDtoTest` — JSON parsing.
- `NewRuleSheetTest` — preview rendering, approximation-note rendering, submit disabled on empty input.
- Manual on simulator: create rule → see in Featured → long-press → edit → re-fire flow.

## Migration plan

1. Add `featured_rules` table + `rule_id` column on `featured_tasks` with safe defaults.
2. Deploy new endpoints alongside existing ones. `GET /featured` still works because `rule_id` is nullable.
3. Switch the 6 AM cron from writing `featured_tasks` to writing `featured_rules` with `status='pending_review'`. First-firing emission to `featured_tasks` happens at acceptance time.
4. Ship iOS build with the new sheet, long-press actions, and pending-rule Accept/Decline.
5. After two clean weeks, drop `predicted_for` from `featured_tasks`.

## Out of scope (v1)

- Multi-step rules (chaining triggers).
- Per-rule access control / team-shared rules.
- Backfilling rules from historical session patterns.
- Voice input for rule creation.
- Notification preferences per rule (only the existing global notification controls apply).
