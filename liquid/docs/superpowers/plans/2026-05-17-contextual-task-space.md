# Contextual Task Space Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Repurpose the Moves > Featured section from "3 daily AI guesses" into a persistent, editable, multi-trigger task space owned by `KK_suggest`.

**Architecture:** Extend `KK_suggest` in place (Approach 2 from spec). Add `featured_rules` table for the persistent space; repurpose `featured_tasks` as *firings*. New REST surface for NL-driven rule CRUD. New evaluator workers (cron first, then meeting_ended + state-diff) emit firings.

**Tech Stack:** Python (FastAPI, SQLAlchemy, asyncio), PostgreSQL (jsonb), KK_LLM (Claude via internal proxy), Kotlin Multiplatform / Compose Multiplatform iOS.

**Spec:** `docs/superpowers/specs/2026-05-17-contextual-task-space-design.md`

**Repo paths used below:**
- Backend (on `gcp`): `/opt/Klik/KK_suggest/`
- iOS frontend (local): `/Users/wilsonxu/Klik_backup/Klik/Klik_one/liquid/samples/composeApp/src/commonMain/kotlin/io/github/fletchmckee/liquid/samples/app/`

All backend work happens on the `dev-chengyi` branch of `/opt/Klik` (per repo CLAUDE.md). Commit/push/PR/merge/pull/restart cycle each phase.

---

## Phase 1 — Schema foundation

### Task 1: Migration — create `featured_rules`, add `rule_id` to `featured_tasks`

**Files:**
- Create: `/opt/Klik/KK_suggest/migrations/2026_05_17_001_featured_rules.sql`
- Create: `/opt/Klik/KK_suggest/tests/test_schema.py`

- [ ] **Step 1: Write the failing test**

```python
# tests/test_schema.py
from sqlalchemy import inspect
from KK_postgresql.connection import db_manager

def test_featured_rules_table_exists():
    with db_manager.get_session() as session:
        insp = inspect(session.bind)
        assert "featured_rules" in insp.get_table_names()
        cols = {c["name"] for c in insp.get_columns("featured_rules")}
        assert {
            "id", "user_id", "source", "nl_text", "parsed_trigger",
            "parsed_action", "signal_binding", "trigger_label",
            "action_label", "is_recurring", "status", "last_fired_at",
            "created_at",
        } <= cols

def test_featured_tasks_has_rule_id():
    with db_manager.get_session() as session:
        insp = inspect(session.bind)
        cols = {c["name"] for c in insp.get_columns("featured_tasks")}
        assert "rule_id" in cols
```

- [ ] **Step 2: Run test to verify it fails**

```bash
ssh gcp 'cd /opt/Klik && .venv/bin/python -m pytest KK_suggest/tests/test_schema.py -v'
```

Expected: FAIL, table/column not found.

- [ ] **Step 3: Write the migration SQL**

```sql
-- migrations/2026_05_17_001_featured_rules.sql
BEGIN;

CREATE TABLE IF NOT EXISTS featured_rules (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id         text NOT NULL,
  source          text NOT NULL CHECK (source IN ('klik_inferred','user_defined')),
  nl_text         text NOT NULL,
  parsed_trigger  jsonb NOT NULL,
  parsed_action   jsonb NOT NULL,
  signal_binding  text NOT NULL,
  trigger_label   text NOT NULL,
  action_label    text NOT NULL,
  is_recurring    boolean NOT NULL,
  status          text NOT NULL CHECK (status IN ('pending_review','active','paused','archived')),
  last_fired_at   timestamptz,
  created_at      timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS featured_rules_user_status
  ON featured_rules(user_id, status);

CREATE INDEX IF NOT EXISTS featured_rules_signal_active
  ON featured_rules(signal_binding) WHERE status = 'active';

ALTER TABLE featured_tasks
  ADD COLUMN IF NOT EXISTS rule_id uuid
  REFERENCES featured_rules(id) ON DELETE CASCADE;

COMMIT;
```

- [ ] **Step 4: Apply the migration**

```bash
ssh gcp "PGPASSWORD=klik2024 psql -h localhost -U klik_user -d klik_pipeline \
  -f /opt/Klik/KK_suggest/migrations/2026_05_17_001_featured_rules.sql"
```

- [ ] **Step 5: Run test to verify it passes**

```bash
ssh gcp 'cd /opt/Klik && .venv/bin/python -m pytest KK_suggest/tests/test_schema.py -v'
```

Expected: PASS, both tests green.

- [ ] **Step 6: Commit**

```bash
ssh gcp 'cd /opt/Klik && git checkout dev-chengyi && \
  git add KK_suggest/migrations/2026_05_17_001_featured_rules.sql KK_suggest/tests/test_schema.py && \
  git commit -m "feat(KK_suggest): featured_rules table + featured_tasks.rule_id (spec 2026-05-17)"'
```

---

## Phase 2 — NL → rule mapper

### Task 2: System prompt + pure parser

**Files:**
- Create: `/opt/Klik/KK_suggest/rules_mapper.py`
- Create: `/opt/Klik/KK_suggest/tests/test_rules_mapper.py`

- [ ] **Step 1: Write the failing test**

```python
# tests/test_rules_mapper.py
import json
from KK_suggest.rules_mapper import parse_llm_output, MapperError

def test_parse_valid_cron_rule():
    raw = json.dumps({
        "trigger": {"type": "cron", "params": {"expr": "0 9 * * 1"}},
        "action": {"type": "draft_email", "params": {"template_hint": "weekly recap"}},
        "approximation_note": None,
        "trigger_label": "every Monday 9 AM",
        "action_label": "draft a weekly recap email",
    })
    out = parse_llm_output(raw)
    assert out.parsed_trigger["type"] == "cron"
    assert out.parsed_action["type"] == "draft_email"
    assert out.approximation_note is None
    assert out.trigger_label == "every Monday 9 AM"
    assert out.signal_binding == "cron"

def test_parse_unknown_trigger_type_raises():
    raw = json.dumps({
        "trigger": {"type": "telepathy", "params": {}},
        "action": {"type": "remind", "params": {"text": "x"}},
        "approximation_note": None,
        "trigger_label": "x", "action_label": "x",
    })
    try:
        parse_llm_output(raw)
        assert False, "expected MapperError"
    except MapperError as e:
        assert "telepathy" in str(e)

def test_parse_malformed_json_raises():
    try:
        parse_llm_output("not json")
        assert False, "expected MapperError"
    except MapperError:
        pass
```

- [ ] **Step 2: Run test to verify it fails**

```bash
ssh gcp 'cd /opt/Klik && .venv/bin/python -m pytest KK_suggest/tests/test_rules_mapper.py -v'
```

Expected: FAIL, `rules_mapper` not found.

- [ ] **Step 3: Write minimal implementation**

```python
# rules_mapper.py
"""Natural-language → structured rule mapper.

Calls KK_LLM with a constrained-output system prompt and parses the result
into a typed preview that the API layer can validate and persist.
"""
from __future__ import annotations

import json
from dataclasses import dataclass
from typing import Any, Optional

from KK_common.logger import get_logger

logger = get_logger("KK_suggest.rules_mapper")

ALLOWED_TRIGGER_TYPES = {
    "cron", "meeting_ended", "person_silence", "okr_threshold", "daily_inference",
}
ALLOWED_ACTION_TYPES = {
    "draft_email", "summarize", "remind", "notify_only", "exec_todo",
}

SYSTEM_PROMPT = """\
You are mapping a user's natural-language rule to a structured trigger + action.

Available trigger types:
  - cron            { expr: "0 9 * * 1" }                 # standard 5-field cron
  - meeting_ended   { participant_filter?: string, label_filter?: string }
  - person_silence  { days: int, person_filter?: string }
  - okr_threshold   { goal_filter?: string, percent_below: int }
  - daily_inference (system-only — do not select)

Available action types:
  - draft_email   { template_hint: string }
  - summarize     { source: "last_week_meetings" | "specific_session", ... }
  - remind        { text: string }
  - notify_only   { text: string }
  - exec_todo     { description: string }

Output JSON only, no prose:
  {
    "trigger": { "type": ..., "params": {...} },
    "action": { "type": ..., "params": {...} },
    "approximation_note": <string|null>,
    "trigger_label": <short human-readable>,
    "action_label":  <short human-readable>
  }

If the user's intent doesn't map cleanly, pick the closest available trigger
and explain via `approximation_note`. Never invent trigger or action types.
"""


@dataclass
class Preview:
    parsed_trigger: dict[str, Any]
    parsed_action: dict[str, Any]
    approximation_note: Optional[str]
    trigger_label: str
    action_label: str
    signal_binding: str   # = parsed_trigger["type"], cached for index lookups


class MapperError(Exception):
    """Raised when LLM output is malformed or contains unknown types."""


def parse_llm_output(raw: str) -> Preview:
    try:
        obj = json.loads(raw)
    except json.JSONDecodeError as e:
        raise MapperError(f"Invalid JSON: {e}") from e

    trig = obj.get("trigger") or {}
    act = obj.get("action") or {}
    if trig.get("type") not in ALLOWED_TRIGGER_TYPES:
        raise MapperError(f"Unknown trigger type: {trig.get('type')!r}")
    if act.get("type") not in ALLOWED_ACTION_TYPES:
        raise MapperError(f"Unknown action type: {act.get('type')!r}")

    return Preview(
        parsed_trigger={"type": trig["type"], "params": trig.get("params") or {}},
        parsed_action={"type": act["type"], "params": act.get("params") or {}},
        approximation_note=obj.get("approximation_note"),
        trigger_label=str(obj.get("trigger_label") or ""),
        action_label=str(obj.get("action_label") or ""),
        signal_binding=trig["type"],
    )
```

- [ ] **Step 4: Run test to verify it passes**

```bash
ssh gcp 'cd /opt/Klik && .venv/bin/python -m pytest KK_suggest/tests/test_rules_mapper.py -v'
```

Expected: PASS, 3/3.

- [ ] **Step 5: Commit**

```bash
ssh gcp 'cd /opt/Klik && git add KK_suggest/rules_mapper.py KK_suggest/tests/test_rules_mapper.py && \
  git commit -m "feat(KK_suggest): rules_mapper — parse LLM output into typed Preview"'
```

---

### Task 3: KK_LLM-backed `preview_from_nl()` function

**Files:**
- Modify: `/opt/Klik/KK_suggest/rules_mapper.py` (add `preview_from_nl`)
- Modify: `/opt/Klik/KK_suggest/tests/test_rules_mapper.py` (add 1 test with mocked LLM)

- [ ] **Step 1: Write the failing test**

```python
# append to tests/test_rules_mapper.py
from unittest.mock import patch
from KK_suggest.rules_mapper import preview_from_nl

def test_preview_from_nl_calls_llm_and_parses():
    llm_response = json.dumps({
        "trigger": {"type": "cron", "params": {"expr": "0 9 * * 1"}},
        "action": {"type": "summarize", "params": {"source": "last_week_meetings"}},
        "approximation_note": None,
        "trigger_label": "every Monday 9 AM",
        "action_label": "summarize last week's meetings",
    })
    with patch("KK_suggest.rules_mapper._call_kk_llm", return_value=llm_response) as call:
        p = preview_from_nl("Every Monday at 9, recap last week.")
    assert p.signal_binding == "cron"
    assert p.parsed_action["params"]["source"] == "last_week_meetings"
    call.assert_called_once()
```

- [ ] **Step 2: Run test to verify it fails**

Expected: FAIL, `preview_from_nl` not defined.

- [ ] **Step 3: Implement `preview_from_nl` + internal `_call_kk_llm`**

```python
# append to rules_mapper.py
import httpx
from KK_common.config import settings

LLM_URL = f"http://localhost:{settings.services.llm.port}/api/llm/v1/complete"
LLM_TIMEOUT_S = 30.0

def _call_kk_llm(user_nl: str) -> str:
    """Single shot to KK_LLM. Returns the model's raw text output."""
    resp = httpx.post(
        LLM_URL,
        json={
            "system": SYSTEM_PROMPT,
            "messages": [{"role": "user", "content": user_nl}],
            "temperature": 0.0,
            "response_format": "json",
        },
        timeout=LLM_TIMEOUT_S,
    )
    resp.raise_for_status()
    return resp.json()["content"]


def preview_from_nl(nl_text: str) -> Preview:
    """End-to-end: NL string → parsed Preview. Raises MapperError on failure."""
    raw = _call_kk_llm(nl_text)
    logger.debug("rules_mapper.llm_raw", extra={"raw_len": len(raw)})
    return parse_llm_output(raw)
```

> **Verify before coding:** confirm the actual KK_LLM HTTP shape on `gcp`. If the field names / route differ, adjust `_call_kk_llm` accordingly — the test mocks `_call_kk_llm`, so the contract change is local.

- [ ] **Step 4: Run test to verify it passes**

```bash
ssh gcp 'cd /opt/Klik && .venv/bin/python -m pytest KK_suggest/tests/test_rules_mapper.py -v'
```

Expected: PASS, 4/4.

- [ ] **Step 5: Commit**

```bash
ssh gcp 'cd /opt/Klik && git add KK_suggest/rules_mapper.py KK_suggest/tests/test_rules_mapper.py && \
  git commit -m "feat(KK_suggest): preview_from_nl wraps KK_LLM call"'
```

---

## Phase 3 — Rules CRUD endpoints

### Task 4: `POST /api/suggest/v1/rules/preview`

**Files:**
- Create: `/opt/Klik/KK_suggest/rules_api.py`
- Modify: `/opt/Klik/KK_suggest/suggest_api.py` (mount the new router)
- Create: `/opt/Klik/KK_suggest/tests/test_rules_api.py`

- [ ] **Step 1: Write the failing test**

```python
# tests/test_rules_api.py
from unittest.mock import patch
from fastapi.testclient import TestClient
from KK_suggest.suggest_api import app
from KK_suggest.rules_mapper import Preview

client = TestClient(app)

def test_preview_returns_parsed_rule():
    fake = Preview(
        parsed_trigger={"type": "cron", "params": {"expr": "0 9 * * 1"}},
        parsed_action={"type": "draft_email", "params": {"template_hint": "recap"}},
        approximation_note=None,
        trigger_label="every Monday 9 AM",
        action_label="draft a recap email",
        signal_binding="cron",
    )
    with patch("KK_suggest.rules_api.preview_from_nl", return_value=fake), \
         patch("KK_suggest.rules_api._user_id_from_request", return_value="user_test"):
        r = client.post(
            "/api/suggest/v1/rules/preview",
            json={"nl_text": "Every Monday at 9 draft a recap"},
        )
    assert r.status_code == 200
    body = r.json()
    assert body["trigger_label"] == "every Monday 9 AM"
    assert body["signal_binding"] == "cron"

def test_preview_unparseable_returns_422():
    from KK_suggest.rules_mapper import MapperError
    with patch("KK_suggest.rules_api.preview_from_nl",
               side_effect=MapperError("nope")), \
         patch("KK_suggest.rules_api._user_id_from_request", return_value="user_test"):
        r = client.post(
            "/api/suggest/v1/rules/preview",
            json={"nl_text": "garbage"},
        )
    assert r.status_code == 422
    assert "nope" in r.json()["detail"]
```

- [ ] **Step 2: Run test to verify it fails**

```bash
ssh gcp 'cd /opt/Klik && .venv/bin/python -m pytest KK_suggest/tests/test_rules_api.py -v'
```

Expected: FAIL, route 404.

- [ ] **Step 3: Implement the router**

```python
# rules_api.py
from __future__ import annotations
from typing import Any, Optional
from fastapi import APIRouter, Header, HTTPException, Request
from pydantic import BaseModel

from KK_common.auth import verify_token
from KK_suggest.rules_mapper import preview_from_nl, MapperError

router = APIRouter(prefix="/api/suggest/v1", tags=["rules"])


class PreviewIn(BaseModel):
    nl_text: str


class PreviewOut(BaseModel):
    trigger_label: str
    action_label: str
    approximation_note: Optional[str]
    parsed_trigger: dict[str, Any]
    parsed_action: dict[str, Any]
    signal_binding: str


def _user_id_from_request(authorization: Optional[str]) -> str:
    if not authorization or not authorization.lower().startswith("bearer "):
        raise HTTPException(status_code=401, detail="Missing bearer token")
    claims = verify_token(authorization.split(" ", 1)[1])
    return claims["sub"]


@router.post("/rules/preview", response_model=PreviewOut)
def preview(body: PreviewIn,
            authorization: Optional[str] = Header(None)):
    _ = _user_id_from_request(authorization)
    try:
        p = preview_from_nl(body.nl_text)
    except MapperError as e:
        raise HTTPException(status_code=422, detail=str(e))
    return PreviewOut(
        trigger_label=p.trigger_label,
        action_label=p.action_label,
        approximation_note=p.approximation_note,
        parsed_trigger=p.parsed_trigger,
        parsed_action=p.parsed_action,
        signal_binding=p.signal_binding,
    )
```

```python
# suggest_api.py — add at the bottom of the file, before `if __name__ == "__main__"`
from KK_suggest.rules_api import router as rules_router
app.include_router(rules_router)
```

- [ ] **Step 4: Run test to verify it passes**

```bash
ssh gcp 'cd /opt/Klik && .venv/bin/python -m pytest KK_suggest/tests/test_rules_api.py -v'
```

Expected: PASS, 2/2.

- [ ] **Step 5: Commit**

```bash
ssh gcp 'cd /opt/Klik && git add KK_suggest/rules_api.py KK_suggest/suggest_api.py KK_suggest/tests/test_rules_api.py && \
  git commit -m "feat(KK_suggest): POST /rules/preview — NL → typed preview"'
```

---

### Task 5: `POST /api/suggest/v1/rules` (persist user-defined rule)

**Files:**
- Modify: `/opt/Klik/KK_suggest/rules_api.py`
- Modify: `/opt/Klik/KK_suggest/tests/test_rules_api.py`

- [ ] **Step 1: Write the failing test**

```python
# append to test_rules_api.py
def test_create_rule_persists_and_returns_active_status():
    from KK_postgresql.connection import db_manager
    from sqlalchemy import text as sql
    with patch("KK_suggest.rules_api._user_id_from_request", return_value="user_test_persist"):
        r = client.post(
            "/api/suggest/v1/rules",
            json={
                "nl_text": "Every Monday at 9, recap.",
                "preview": {
                    "trigger_label": "every Monday 9 AM",
                    "action_label": "summarize last week",
                    "approximation_note": None,
                    "parsed_trigger": {"type": "cron", "params": {"expr": "0 9 * * 1"}},
                    "parsed_action": {"type": "summarize", "params": {"source": "last_week_meetings"}},
                    "signal_binding": "cron",
                },
                "is_recurring": True,
            },
        )
    assert r.status_code == 201
    rid = r.json()["id"]
    with db_manager.get_session() as s:
        row = s.execute(
            sql("SELECT source, status FROM featured_rules WHERE id = :id"),
            {"id": rid},
        ).fetchone()
        assert row.source == "user_defined"
        assert row.status == "active"
    # cleanup
    with db_manager.get_session() as s:
        s.execute(sql("DELETE FROM featured_rules WHERE id = :id"), {"id": rid})
        s.commit()
```

- [ ] **Step 2: Run test to verify it fails**

Expected: 404, route not found.

- [ ] **Step 3: Implement**

```python
# append to rules_api.py
import uuid
from sqlalchemy import text
from KK_postgresql.connection import db_manager


class CreateIn(BaseModel):
    nl_text: str
    preview: PreviewOut
    is_recurring: bool


class RuleOut(BaseModel):
    id: str
    source: str
    nl_text: str
    trigger_label: str
    action_label: str
    status: str
    is_recurring: bool
    last_fired_at: Optional[str]


@router.post("/rules", response_model=RuleOut, status_code=201)
def create_rule(body: CreateIn,
                authorization: Optional[str] = Header(None)):
    user_id = _user_id_from_request(authorization)
    rid = str(uuid.uuid4())
    with db_manager.get_session() as s:
        s.execute(text("""
            INSERT INTO featured_rules
              (id, user_id, source, nl_text, parsed_trigger, parsed_action,
               signal_binding, trigger_label, action_label, is_recurring, status)
            VALUES
              (:id, :uid, 'user_defined', :nl, CAST(:pt AS jsonb), CAST(:pa AS jsonb),
               :sb, :tl, :al, :rec, 'active')
        """), {
            "id": rid, "uid": user_id, "nl": body.nl_text,
            "pt": json.dumps(body.preview.parsed_trigger),
            "pa": json.dumps(body.preview.parsed_action),
            "sb": body.preview.signal_binding,
            "tl": body.preview.trigger_label,
            "al": body.preview.action_label,
            "rec": body.is_recurring,
        })
        s.commit()
    return RuleOut(
        id=rid, source="user_defined", nl_text=body.nl_text,
        trigger_label=body.preview.trigger_label,
        action_label=body.preview.action_label,
        status="active", is_recurring=body.is_recurring, last_fired_at=None,
    )
```

(Add `import json` at the top of `rules_api.py` if not already present.)

- [ ] **Step 4: Run test to verify it passes**

```bash
ssh gcp 'cd /opt/Klik && .venv/bin/python -m pytest KK_suggest/tests/test_rules_api.py -v'
```

Expected: PASS, 3/3.

- [ ] **Step 5: Commit**

```bash
ssh gcp 'cd /opt/Klik && git add KK_suggest/rules_api.py KK_suggest/tests/test_rules_api.py && \
  git commit -m "feat(KK_suggest): POST /rules — persist user-defined rule as active"'
```

---

### Task 6: `GET /rules`, `PATCH /rules/{id}`, `DELETE /rules/{id}`, `POST /rules/{id}/accept`

**Files:**
- Modify: `/opt/Klik/KK_suggest/rules_api.py`
- Modify: `/opt/Klik/KK_suggest/tests/test_rules_api.py`

- [ ] **Step 1: Write the failing tests**

```python
# append to test_rules_api.py
def _make_rule(user_id: str, source: str = "user_defined", status: str = "active") -> str:
    from KK_postgresql.connection import db_manager
    from sqlalchemy import text as sql
    rid = str(uuid.uuid4())
    with db_manager.get_session() as s:
        s.execute(sql("""
            INSERT INTO featured_rules
              (id, user_id, source, nl_text, parsed_trigger, parsed_action,
               signal_binding, trigger_label, action_label, is_recurring, status)
            VALUES
              (:id, :uid, :src, 'every mon', '{"type":"cron","params":{"expr":"0 9 * * 1"}}',
               '{"type":"remind","params":{"text":"x"}}',
               'cron', 'every Mon 9', 'remind', true, :status)
        """), {"id": rid, "uid": user_id, "src": source, "status": status})
        s.commit()
    return rid

import uuid

def test_list_rules_returns_only_user_rules():
    rid = _make_rule("user_listA")
    _make_rule("user_listB")  # other user
    with patch("KK_suggest.rules_api._user_id_from_request", return_value="user_listA"):
        r = client.get("/api/suggest/v1/rules")
    assert r.status_code == 200
    ids = [it["id"] for it in r.json()]
    assert rid in ids and len(ids) == 1

def test_patch_rule_updates_recurring_flag():
    rid = _make_rule("user_patch")
    with patch("KK_suggest.rules_api._user_id_from_request", return_value="user_patch"):
        r = client.patch(f"/api/suggest/v1/rules/{rid}", json={"is_recurring": False})
    assert r.status_code == 200
    assert r.json()["is_recurring"] is False

def test_delete_rule():
    rid = _make_rule("user_del")
    with patch("KK_suggest.rules_api._user_id_from_request", return_value="user_del"):
        r = client.delete(f"/api/suggest/v1/rules/{rid}")
    assert r.status_code == 204

def test_accept_promotes_pending_to_active():
    rid = _make_rule("user_accept", source="klik_inferred", status="pending_review")
    with patch("KK_suggest.rules_api._user_id_from_request", return_value="user_accept"):
        r = client.post(f"/api/suggest/v1/rules/{rid}/accept")
    assert r.status_code == 200
    assert r.json()["status"] == "active"
```

- [ ] **Step 2: Run tests to verify they fail**

Expected: 4 × 404.

- [ ] **Step 3: Implement**

```python
# append to rules_api.py

class PatchIn(BaseModel):
    nl_text: Optional[str] = None
    is_recurring: Optional[bool] = None
    status: Optional[str] = None


def _row_to_out(row) -> RuleOut:
    return RuleOut(
        id=str(row.id), source=row.source, nl_text=row.nl_text,
        trigger_label=row.trigger_label, action_label=row.action_label,
        status=row.status, is_recurring=row.is_recurring,
        last_fired_at=row.last_fired_at.isoformat() if row.last_fired_at else None,
    )


@router.get("/rules", response_model=list[RuleOut])
def list_rules(authorization: Optional[str] = Header(None)):
    user_id = _user_id_from_request(authorization)
    with db_manager.get_session() as s:
        rows = s.execute(text(
            "SELECT id, source, nl_text, trigger_label, action_label, status, "
            "is_recurring, last_fired_at FROM featured_rules "
            "WHERE user_id = :uid AND status != 'archived' "
            "ORDER BY created_at DESC"
        ), {"uid": user_id}).fetchall()
    return [_row_to_out(r) for r in rows]


@router.patch("/rules/{rule_id}", response_model=RuleOut)
def patch_rule(rule_id: str, body: PatchIn,
               authorization: Optional[str] = Header(None)):
    user_id = _user_id_from_request(authorization)
    sets, params = [], {"id": rule_id, "uid": user_id}
    if body.nl_text is not None:
        # Re-parse via the mapper, replace structural fields atomically.
        p = preview_from_nl(body.nl_text)
        sets += [
            "nl_text = :nl", "parsed_trigger = CAST(:pt AS jsonb)",
            "parsed_action = CAST(:pa AS jsonb)", "signal_binding = :sb",
            "trigger_label = :tl", "action_label = :al",
        ]
        params.update({
            "nl": body.nl_text,
            "pt": json.dumps(p.parsed_trigger),
            "pa": json.dumps(p.parsed_action),
            "sb": p.signal_binding,
            "tl": p.trigger_label, "al": p.action_label,
        })
    if body.is_recurring is not None:
        sets.append("is_recurring = :rec"); params["rec"] = body.is_recurring
    if body.status is not None:
        sets.append("status = :st"); params["st"] = body.status
    if not sets:
        raise HTTPException(status_code=400, detail="No fields to update")
    sql = (
        "UPDATE featured_rules SET " + ", ".join(sets)
        + " WHERE id = :id AND user_id = :uid "
        + "RETURNING id, source, nl_text, trigger_label, action_label, status, "
        + "is_recurring, last_fired_at"
    )
    with db_manager.get_session() as s:
        row = s.execute(text(sql), params).fetchone()
        s.commit()
    if row is None:
        raise HTTPException(status_code=404, detail="Rule not found")
    return _row_to_out(row)


@router.delete("/rules/{rule_id}", status_code=204)
def delete_rule(rule_id: str,
                authorization: Optional[str] = Header(None)):
    user_id = _user_id_from_request(authorization)
    with db_manager.get_session() as s:
        res = s.execute(text(
            "DELETE FROM featured_rules WHERE id = :id AND user_id = :uid"
        ), {"id": rule_id, "uid": user_id})
        s.commit()
    if res.rowcount == 0:
        raise HTTPException(status_code=404, detail="Rule not found")


@router.post("/rules/{rule_id}/accept", response_model=RuleOut)
def accept_rule(rule_id: str,
                authorization: Optional[str] = Header(None)):
    user_id = _user_id_from_request(authorization)
    with db_manager.get_session() as s:
        row = s.execute(text(
            "UPDATE featured_rules SET status = 'active' "
            "WHERE id = :id AND user_id = :uid AND status = 'pending_review' "
            "RETURNING id, source, nl_text, trigger_label, action_label, status, "
            "is_recurring, last_fired_at"
        ), {"id": rule_id, "uid": user_id}).fetchone()
        s.commit()
    if row is None:
        raise HTTPException(status_code=404, detail="Pending rule not found")
    return _row_to_out(row)
```

- [ ] **Step 4: Run tests to verify they pass**

Expected: PASS, 7/7 in `test_rules_api.py`.

- [ ] **Step 5: Commit**

```bash
ssh gcp 'cd /opt/Klik && git add KK_suggest/rules_api.py KK_suggest/tests/test_rules_api.py && \
  git commit -m "feat(KK_suggest): list/patch/delete/accept rules"'
```

---

## Phase 4 — Evaluator framework + cron evaluator

### Task 7: Cron evaluator pure logic

**Files:**
- Create: `/opt/Klik/KK_suggest/evaluators/__init__.py` (empty)
- Create: `/opt/Klik/KK_suggest/evaluators/cron_eval.py`
- Create: `/opt/Klik/KK_suggest/tests/test_cron_evaluator.py`

- [ ] **Step 1: Write the failing test**

```python
# tests/test_cron_evaluator.py
from datetime import datetime, timezone, timedelta
from KK_suggest.evaluators.cron_eval import should_fire

def _rule(expr, last_fired_at=None):
    return {
        "id": "r1", "user_id": "u",
        "parsed_trigger": {"type": "cron", "params": {"expr": expr}},
        "last_fired_at": last_fired_at,
        "status": "active",
    }

def test_fires_when_current_minute_matches():
    now = datetime(2026, 5, 18, 9, 0, tzinfo=timezone.utc)  # Mon 9:00 UTC
    assert should_fire(_rule("0 9 * * 1"), now=now, user_tz="UTC") is True

def test_does_not_fire_when_already_fired_in_same_minute():
    now = datetime(2026, 5, 18, 9, 0, tzinfo=timezone.utc)
    last = now - timedelta(seconds=30)
    assert should_fire(_rule("0 9 * * 1", last_fired_at=last),
                       now=now, user_tz="UTC") is False

def test_does_not_fire_off_schedule():
    now = datetime(2026, 5, 18, 10, 0, tzinfo=timezone.utc)
    assert should_fire(_rule("0 9 * * 1"), now=now, user_tz="UTC") is False
```

- [ ] **Step 2: Run test to verify it fails**

Expected: FAIL — module missing.

- [ ] **Step 3: Implement**

```python
# evaluators/cron_eval.py
"""Pure decision function for the cron-trigger evaluator.

The scheduler (separate worker) calls should_fire() on each candidate rule
per scan; this module has no DB and no time-of-day side effects."""
from __future__ import annotations
from datetime import datetime, timedelta
from typing import Any, Optional
from zoneinfo import ZoneInfo
from croniter import croniter


def should_fire(rule: dict[str, Any], *, now: datetime, user_tz: str) -> bool:
    if rule.get("status") != "active":
        return False
    trig = rule.get("parsed_trigger") or {}
    if trig.get("type") != "cron":
        return False
    expr = (trig.get("params") or {}).get("expr")
    if not expr:
        return False

    tz = ZoneInfo(user_tz)
    local_now = now.astimezone(tz)

    # Compute the most recent scheduled tick at or before local_now.
    itr = croniter(expr, local_now)
    prev_tick = itr.get_prev(datetime)
    if prev_tick.tzinfo is None:
        prev_tick = prev_tick.replace(tzinfo=tz)

    # Fire if we are within the same minute as that tick.
    if (local_now - prev_tick) >= timedelta(minutes=1):
        return False

    last = rule.get("last_fired_at")
    if last is not None:
        last_aware = last if last.tzinfo else last.replace(tzinfo=timezone.utc)
        if last_aware.astimezone(tz) >= prev_tick:
            return False
    return True
```

> **Dependency:** `croniter` is on PyPI. Add to `KK_suggest/pyproject.toml` (or wherever deps live) and `uv lock` before running tests.

- [ ] **Step 4: Run test to verify it passes**

```bash
ssh gcp 'cd /opt/Klik && uv add croniter --package KK_suggest && \
  .venv/bin/python -m pytest KK_suggest/tests/test_cron_evaluator.py -v'
```

Expected: PASS, 3/3.

- [ ] **Step 5: Commit**

```bash
ssh gcp 'cd /opt/Klik && git add KK_suggest/evaluators/ KK_suggest/tests/test_cron_evaluator.py KK_suggest/pyproject.toml uv.lock && \
  git commit -m "feat(KK_suggest): cron evaluator should_fire (pure)"'
```

---

### Task 8: Cron evaluator worker — emit firings

**Files:**
- Create: `/opt/Klik/KK_suggest/evaluators/cron_worker.py`
- Create: `/opt/Klik/KK_suggest/tests/test_cron_worker.py`

- [ ] **Step 1: Write the failing test**

```python
# tests/test_cron_worker.py
import uuid
from datetime import datetime, timezone
from unittest.mock import patch
from sqlalchemy import text as sql
from KK_postgresql.connection import db_manager
from KK_suggest.evaluators.cron_worker import scan_once

def test_scan_once_emits_card_for_due_rule():
    rid = str(uuid.uuid4())
    uid = "user_cron_worker"
    with db_manager.get_session() as s:
        s.execute(sql("""
          INSERT INTO featured_rules
            (id, user_id, source, nl_text, parsed_trigger, parsed_action,
             signal_binding, trigger_label, action_label, is_recurring, status)
          VALUES
            (:id, :uid, 'user_defined', 'every minute', '{"type":"cron","params":{"expr":"* * * * *"}}',
             '{"type":"remind","params":{"text":"hi"}}', 'cron', 'every minute',
             'remind hi', true, 'active')
        """), {"id": rid, "uid": uid})
        s.commit()
    try:
        now = datetime(2026, 5, 18, 9, 0, 30, tzinfo=timezone.utc)
        with patch("KK_suggest.evaluators.cron_worker._user_tz", return_value="UTC"):
            scan_once(now=now)
        with db_manager.get_session() as s:
            cards = s.execute(sql(
                "SELECT title, rule_id FROM featured_tasks WHERE rule_id = :rid"
            ), {"rid": rid}).fetchall()
        assert len(cards) == 1
        assert cards[0].rule_id == uuid.UUID(rid)
    finally:
        with db_manager.get_session() as s:
            s.execute(sql("DELETE FROM featured_tasks WHERE rule_id = :rid"), {"rid": rid})
            s.execute(sql("DELETE FROM featured_rules WHERE id = :rid"), {"rid": rid})
            s.commit()
```

- [ ] **Step 2: Run test to verify it fails**

Expected: FAIL — module missing.

- [ ] **Step 3: Implement**

```python
# evaluators/cron_worker.py
"""Per-minute scan over active cron rules; emits firings into featured_tasks."""
from __future__ import annotations
import json
import uuid
from datetime import datetime, timezone
from sqlalchemy import text
from KK_common.logger import get_logger
from KK_postgresql.connection import db_manager
from KK_suggest.evaluators.cron_eval import should_fire

logger = get_logger("KK_suggest.cron_worker")


def _user_tz(user_id: str) -> str:
    """Read the user's timezone from user_preferences. Defaults to UTC."""
    with db_manager.get_session() as s:
        row = s.execute(text(
            "SELECT data->>'timezone' AS tz FROM user_preferences WHERE user_id = :uid"
        ), {"uid": user_id}).fetchone()
    return (row and row.tz) or "UTC"


def _emit_firing(s, rule) -> None:
    s.execute(text("""
        INSERT INTO featured_tasks
          (rule_id, user_id, title, description, category, priority, reasoning)
        VALUES
          (:rid, :uid, :title, :desc, :cat, :pri, :rsn)
    """), {
        "rid": rule.id, "uid": rule.user_id,
        "title": rule.action_label, "desc": rule.trigger_label,
        "cat": "a_tools", "pri": "medium",
        "rsn": f"Triggered by rule {rule.id}",
    })
    s.execute(text(
        "UPDATE featured_rules SET last_fired_at = now() WHERE id = :rid"
    ), {"rid": rule.id})


def scan_once(*, now: datetime | None = None) -> int:
    """Single scan pass. Returns number of firings emitted."""
    now = now or datetime.now(timezone.utc)
    fired = 0
    with db_manager.get_session() as s:
        rows = s.execute(text(
            "SELECT id, user_id, parsed_trigger, last_fired_at, status, "
            "       trigger_label, action_label "
            "FROM featured_rules "
            "WHERE status = 'active' AND signal_binding = 'cron'"
        )).fetchall()
        for r in rows:
            rule = {
                "id": r.id, "user_id": r.user_id,
                "parsed_trigger": r.parsed_trigger
                    if isinstance(r.parsed_trigger, dict)
                    else json.loads(r.parsed_trigger),
                "last_fired_at": r.last_fired_at,
                "status": r.status,
            }
            tz = _user_tz(r.user_id)
            if should_fire(rule, now=now, user_tz=tz):
                _emit_firing(s, r)
                fired += 1
        s.commit()
    logger.info("cron_worker.scan", extra={"fired": fired, "scanned": len(rows)})
    return fired


def run_forever(interval_s: int = 60) -> None:
    import time
    while True:
        try:
            scan_once()
        except Exception:
            logger.exception("cron_worker.crash")
        time.sleep(interval_s)


if __name__ == "__main__":
    run_forever()
```

- [ ] **Step 4: Run test to verify it passes**

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
ssh gcp 'cd /opt/Klik && git add KK_suggest/evaluators/cron_worker.py KK_suggest/tests/test_cron_worker.py && \
  git commit -m "feat(KK_suggest): cron_worker — per-minute scan + firing emission"'
```

---

### Task 9: Wire `cron_worker` into KK_suggest startup

**Files:**
- Modify: `/opt/Klik/Scripts/restart_all.sh` (add evaluator process to the KK_suggest start block)

- [ ] **Step 1: Inspect existing KK_suggest start in `restart_all.sh`**

```bash
ssh gcp 'grep -n "KK_suggest\|suggest" /opt/Klik/Scripts/restart_all.sh | head -10'
```

- [ ] **Step 2: Add a second process launch for the cron worker**

In the KK_suggest stanza, after the API line, add (adjusting indentation to match):

```bash
restart_service_bulletproof "suggest_cron_worker" 0 \
    "$WORKSPACE_ROOT" \
    "PYTHONPATH='$WORKSPACE_ROOT:\$PYTHONPATH' nohup '$PYTHON_BIN' -m KK_suggest.evaluators.cron_worker"
```

> The `0` port means "no port to health-check". If `restart_service_bulletproof` doesn't support that, copy its body into a smaller `restart_workerless_service` helper next to it that skips port checks. Do that change in the same commit.

- [ ] **Step 3: Restart KK_suggest + new worker**

```bash
ssh gcp '/opt/Klik/Scripts/restart_all.sh 2>&1 | grep -i "suggest"'
```

Verify both `mobile_api`-style log lines for `suggest_api` and `suggest_cron_worker`.

- [ ] **Step 4: Smoke test the worker is running**

```bash
ssh gcp 'ps -ef | grep cron_worker | grep -v grep'
```

Expected: one process owned by `chengyi`.

- [ ] **Step 5: Commit**

```bash
ssh gcp 'cd /opt/Klik && git add Scripts/restart_all.sh && \
  git commit -m "ops(KK_suggest): launch cron_worker alongside suggest_api"'
```

---

## Phase 5 — Daily inference rewrite (Klik-inferred → pending rules)

### Task 10: Rewrite `inference.run_for_user` to insert `featured_rules` rows

**Files:**
- Modify: `/opt/Klik/KK_suggest/inference.py` (replace `featured_tasks` INSERT block)
- Modify: `/opt/Klik/KK_suggest/tests/test_inference.py`

- [ ] **Step 1: Write the failing test**

```python
# tests/test_inference.py — add this test
from unittest.mock import patch
from KK_postgresql.connection import db_manager
from sqlalchemy import text as sql
from KK_suggest.inference import run_for_user

def test_run_for_user_writes_pending_rules_not_cards():
    uid = "user_inference_rewrite"
    fake_items = [{
        "title": "Recap weekly", "description": "Summarize the week",
        "category": "a_tools", "priority": "medium",
        "reasoning": "User does this on Mondays",
    }] * 3
    with patch("KK_suggest.inference._call_llm", return_value=fake_items):
        run_for_user(uid, predicted_for="2026-05-18")
    try:
        with db_manager.get_session() as s:
            rules = s.execute(sql(
                "SELECT status, source FROM featured_rules WHERE user_id = :u"
            ), {"u": uid}).fetchall()
            cards = s.execute(sql(
                "SELECT count(*) AS c FROM featured_tasks WHERE user_id = :u "
                "AND rule_id IS NOT NULL"
            ), {"u": uid}).fetchone()
        assert len(rules) == 3
        assert all(r.status == "pending_review" for r in rules)
        assert all(r.source == "klik_inferred" for r in rules)
        assert cards.c == 0   # pending rules don't emit cards until accepted
    finally:
        with db_manager.get_session() as s:
            s.execute(sql("DELETE FROM featured_rules WHERE user_id = :u"), {"u": uid})
            s.commit()
```

- [ ] **Step 2: Run test to verify it fails**

Expected: FAIL — current `run_for_user` writes to `featured_tasks`, not `featured_rules`.

- [ ] **Step 3: Implement — replace the INSERT block in `inference.py`**

Find the existing `DELETE FROM featured_tasks WHERE …` + `INSERT INTO featured_tasks …` block at lines ~135–150 and replace with:

```python
# inference.py — replacement
import json, uuid
def _persist_as_pending_rules(session, user_id: str, items: list[dict]) -> None:
    for it in items:
        session.execute(text("""
            INSERT INTO featured_rules
              (id, user_id, source, nl_text, parsed_trigger, parsed_action,
               signal_binding, trigger_label, action_label, is_recurring, status)
            VALUES
              (:id, :uid, 'klik_inferred', :nl,
               '{"type":"daily_inference","params":{}}'::jsonb,
               CAST(:action AS jsonb),
               'daily_inference', 'today', :title, false, 'pending_review')
        """), {
            "id": str(uuid.uuid4()), "uid": user_id,
            "nl": it["description"], "title": it["title"],
            "action": json.dumps({"type": "exec_todo",
                                  "params": {"description": it["description"]}}),
        })
```

Wire `_persist_as_pending_rules(session, user_id, items)` in place of the old `DELETE`/`INSERT` to `featured_tasks`. Keep all upstream LLM logic untouched.

- [ ] **Step 4: Run test to verify it passes**

Expected: PASS.

- [ ] **Step 5: Restart `suggest_api` so the new code path is live**

```bash
ssh gcp '/opt/Klik/Scripts/restart_all.sh 2>&1 | grep -i suggest_api'
```

- [ ] **Step 6: Commit**

```bash
ssh gcp 'cd /opt/Klik && git add KK_suggest/inference.py KK_suggest/tests/test_inference.py && \
  git commit -m "feat(KK_suggest): daily inference now writes pending rules"'
```

---

## Phase 6 — `GET /featured` carries rule context

### Task 11: Enrich `/featured` with `rule_id` + `trigger_label`

**Files:**
- Modify: `/opt/Klik/KK_suggest/suggest_api.py`
- Modify: `/opt/Klik/KK_suggest/tests/test_api.py`

- [ ] **Step 1: Write the failing test**

```python
# tests/test_api.py — add
import uuid
from sqlalchemy import text as sql
from KK_postgresql.connection import db_manager
from fastapi.testclient import TestClient
from unittest.mock import patch
from KK_suggest.suggest_api import app

def test_featured_returns_rule_id_and_trigger_label():
    rid = str(uuid.uuid4())
    uid = "user_featured_enrich"
    with db_manager.get_session() as s:
        s.execute(sql("""
          INSERT INTO featured_rules
            (id, user_id, source, nl_text, parsed_trigger, parsed_action,
             signal_binding, trigger_label, action_label, is_recurring, status)
          VALUES
            (:rid, :uid, 'user_defined', 'every mon', '{"type":"cron","params":{}}',
             '{"type":"remind","params":{}}', 'cron', 'every Mon 9', 'recap', true, 'active')
        """), {"rid": rid, "uid": uid})
        s.execute(sql("""
          INSERT INTO featured_tasks (rule_id, user_id, title, description, category, priority, reasoning)
          VALUES (:rid, :uid, 'recap', 'recap', 'a_tools', 'medium', 'rule fired')
        """), {"rid": rid, "uid": uid})
        s.commit()
    try:
        with patch("KK_suggest.suggest_api._user_id", return_value=uid):
            r = TestClient(app).get("/api/suggest/v1/featured?tz=UTC")
        body = r.json()
        item = next(x for x in body if x.get("rule_id") == rid)
        assert item["trigger_label"] == "every Mon 9"
    finally:
        with db_manager.get_session() as s:
            s.execute(sql("DELETE FROM featured_tasks WHERE rule_id = :rid"), {"rid": rid})
            s.execute(sql("DELETE FROM featured_rules WHERE id = :rid"), {"rid": rid})
            s.commit()
```

- [ ] **Step 2: Run test to verify it fails**

Expected: FAIL — current shape has no `rule_id`/`trigger_label`.

- [ ] **Step 3: Implement — modify `suggest_api.py` /featured handler**

Extend the `FeaturedTask` model with optional new fields, change the SQL to `LEFT JOIN featured_rules ON featured_tasks.rule_id = featured_rules.id` and select `featured_rules.trigger_label`. Return both in the response.

```python
# in suggest_api.py
class FeaturedTask(BaseModel):
    title: str
    description: str
    category: str
    priority: str
    rule_id: Optional[str] = None
    trigger_label: Optional[str] = None
```

SQL change:

```sql
SELECT ft.title, ft.description, ft.category, ft.priority,
       ft.rule_id::text AS rule_id, fr.trigger_label
FROM featured_tasks ft
LEFT JOIN featured_rules fr ON fr.id = ft.rule_id
WHERE ft.user_id = :uid
  AND (ft.predicted_for = :today OR ft.rule_id IS NOT NULL)
ORDER BY ft.created_at ASC
```

- [ ] **Step 4: Run test to verify it passes**

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
ssh gcp 'cd /opt/Klik && git add KK_suggest/suggest_api.py KK_suggest/tests/test_api.py && \
  git commit -m "feat(KK_suggest): /featured returns rule_id + trigger_label"'
```

---

## Phase 7 — `meeting_ended` evaluator

### Task 12: Pure `should_fire` for meeting_ended

**Files:**
- Create: `/opt/Klik/KK_suggest/evaluators/meeting_eval.py`
- Create: `/opt/Klik/KK_suggest/tests/test_meeting_evaluator.py`

- [ ] **Step 1: Write the failing test**

```python
# tests/test_meeting_evaluator.py
from KK_suggest.evaluators.meeting_eval import should_fire

def _rule(participant_filter=None, label_filter=None):
    return {
        "status": "active",
        "parsed_trigger": {
            "type": "meeting_ended",
            "params": {
                "participant_filter": participant_filter,
                "label_filter": label_filter,
            },
        },
    }

def test_no_filter_fires_for_any_meeting():
    assert should_fire(_rule(), meeting={"participants": [], "labels": []}) is True

def test_participant_filter_matches_substring_case_insensitive():
    rule = _rule(participant_filter="anthropic")
    assert should_fire(rule, meeting={"participants": ["Sam @ Anthropic"], "labels": []}) is True
    assert should_fire(rule, meeting={"participants": ["John @ Acme"], "labels": []}) is False

def test_label_filter_matches_exact():
    rule = _rule(label_filter="1:1")
    assert should_fire(rule, meeting={"participants": [], "labels": ["1:1"]}) is True
    assert should_fire(rule, meeting={"participants": [], "labels": ["all-hands"]}) is False
```

- [ ] **Step 2: Run test to verify it fails**

Expected: FAIL — missing module.

- [ ] **Step 3: Implement**

```python
# evaluators/meeting_eval.py
from typing import Any

def should_fire(rule: dict[str, Any], *, meeting: dict[str, Any]) -> bool:
    if rule.get("status") != "active":
        return False
    trig = rule.get("parsed_trigger") or {}
    if trig.get("type") != "meeting_ended":
        return False
    params = trig.get("params") or {}
    pf = (params.get("participant_filter") or "").lower().strip()
    lf = (params.get("label_filter") or "").strip()
    if pf:
        if not any(pf in (p or "").lower() for p in meeting.get("participants", [])):
            return False
    if lf:
        if lf not in (meeting.get("labels") or []):
            return False
    return True
```

- [ ] **Step 4: Run test to verify it passes**

Expected: PASS, 4/4.

- [ ] **Step 5: Commit**

```bash
ssh gcp 'cd /opt/Klik && git add KK_suggest/evaluators/meeting_eval.py KK_suggest/tests/test_meeting_evaluator.py && \
  git commit -m "feat(KK_suggest): meeting_ended evaluator (pure)"'
```

---

### Task 13: Meeting-end event subscriber (worker)

**Files:**
- Create: `/opt/Klik/KK_suggest/evaluators/meeting_worker.py`
- Create: `/opt/Klik/KK_suggest/tests/test_meeting_worker.py`

> **Pre-req:** confirm how `KK_session` publishes "session.completed" events. If a Postgres `NOTIFY` is already used, subscribe to that channel. Otherwise, fall back to a 30-second poll of `sessions WHERE status='completed' AND processed_by_rules_at IS NULL` and add the `processed_by_rules_at` column in this task's migration.

- [ ] **Step 1: Decide subscription mechanism** (LISTEN/NOTIFY vs poll). Pick polling for v1 unless NOTIFY already exists.

- [ ] **Step 2: Migration — add `processed_by_rules_at`**

```sql
-- migrations/2026_05_17_002_sessions_processed_by_rules.sql
ALTER TABLE sessions
  ADD COLUMN IF NOT EXISTS processed_by_rules_at timestamptz;
```

Apply it.

- [ ] **Step 3: Write the failing test**

```python
# tests/test_meeting_worker.py
import uuid
from sqlalchemy import text as sql
from KK_postgresql.connection import db_manager
from KK_suggest.evaluators.meeting_worker import scan_once

def test_emits_card_for_active_rule_when_session_completes():
    uid = "user_meeting_worker"
    rid = str(uuid.uuid4())
    sid = str(uuid.uuid4())
    with db_manager.get_session() as s:
        s.execute(sql("""
          INSERT INTO featured_rules
            (id, user_id, source, nl_text, parsed_trigger, parsed_action,
             signal_binding, trigger_label, action_label, is_recurring, status)
          VALUES
            (:rid, :uid, 'user_defined', 'after 1:1s',
             '{"type":"meeting_ended","params":{"label_filter":"1:1"}}',
             '{"type":"draft_email","params":{"template_hint":"recap"}}',
             'meeting_ended', 'after 1:1s', 'draft recap', true, 'active')
        """), {"rid": rid, "uid": uid})
        s.execute(sql("""
          INSERT INTO sessions (id, user_id, status, labels, participants)
          VALUES (:sid, :uid, 'completed', ARRAY['1:1'], ARRAY['John'])
        """), {"sid": sid, "uid": uid})
        s.commit()
    try:
        emitted = scan_once()
        with db_manager.get_session() as s:
            cards = s.execute(sql(
                "SELECT count(*) AS c FROM featured_tasks WHERE rule_id = :rid"
            ), {"rid": rid}).fetchone()
            processed = s.execute(sql(
                "SELECT processed_by_rules_at FROM sessions WHERE id = :sid"
            ), {"sid": sid}).fetchone()
        assert emitted == 1
        assert cards.c == 1
        assert processed.processed_by_rules_at is not None
    finally:
        with db_manager.get_session() as s:
            s.execute(sql("DELETE FROM featured_tasks WHERE rule_id = :rid"), {"rid": rid})
            s.execute(sql("DELETE FROM featured_rules WHERE id = :rid"), {"rid": rid})
            s.execute(sql("DELETE FROM sessions WHERE id = :sid"), {"sid": sid})
            s.commit()
```

> If the real `sessions` schema doesn't have `labels`/`participants` arrays, adapt the test fixture to whatever columns/joins reflect the real shape. Don't invent columns.

- [ ] **Step 4: Implement `meeting_worker.py`**

```python
# evaluators/meeting_worker.py
"""Poll new completed sessions, emit firings for matching meeting_ended rules."""
from __future__ import annotations
import json, uuid
from sqlalchemy import text
from KK_common.logger import get_logger
from KK_postgresql.connection import db_manager
from KK_suggest.evaluators.meeting_eval import should_fire

logger = get_logger("KK_suggest.meeting_worker")


def _emit(s, rule_row, session_row) -> None:
    s.execute(text("""
        INSERT INTO featured_tasks
          (rule_id, user_id, title, description, category, priority, reasoning)
        VALUES (:rid, :uid, :title, :desc, 'a_tools', 'medium', :rsn)
    """), {
        "rid": rule_row.id, "uid": rule_row.user_id,
        "title": rule_row.action_label,
        "desc": rule_row.trigger_label,
        "rsn": f"Triggered by session {session_row.id}",
    })
    s.execute(text("UPDATE featured_rules SET last_fired_at = now() WHERE id = :rid"),
              {"rid": rule_row.id})


def scan_once() -> int:
    fired = 0
    with db_manager.get_session() as s:
        sessions = s.execute(text(
            "SELECT id, user_id, labels, participants FROM sessions "
            "WHERE status = 'completed' AND processed_by_rules_at IS NULL "
            "ORDER BY ended_at ASC LIMIT 100"
        )).fetchall()
        for sess in sessions:
            rules = s.execute(text(
                "SELECT id, user_id, parsed_trigger, status, trigger_label, action_label "
                "FROM featured_rules "
                "WHERE user_id = :uid AND status='active' "
                "  AND signal_binding = 'meeting_ended'"
            ), {"uid": sess.user_id}).fetchall()
            for r in rules:
                rule = {
                    "status": r.status,
                    "parsed_trigger": r.parsed_trigger
                        if isinstance(r.parsed_trigger, dict)
                        else json.loads(r.parsed_trigger),
                }
                meeting = {
                    "labels": list(sess.labels or []),
                    "participants": list(sess.participants or []),
                }
                if should_fire(rule, meeting=meeting):
                    _emit(s, r, sess)
                    fired += 1
            s.execute(text(
                "UPDATE sessions SET processed_by_rules_at = now() WHERE id = :sid"
            ), {"sid": sess.id})
        s.commit()
    logger.info("meeting_worker.scan", extra={"fired": fired, "sessions": len(sessions)})
    return fired


def run_forever(interval_s: int = 30) -> None:
    import time
    while True:
        try:
            scan_once()
        except Exception:
            logger.exception("meeting_worker.crash")
        time.sleep(interval_s)


if __name__ == "__main__":
    run_forever()
```

- [ ] **Step 5: Run test to verify it passes**

```bash
ssh gcp 'cd /opt/Klik && .venv/bin/python -m pytest KK_suggest/tests/test_meeting_worker.py -v'
```

- [ ] **Step 6: Wire into restart_all.sh** (same pattern as Task 9, second worker process). Restart.

- [ ] **Step 7: Commit**

```bash
ssh gcp 'cd /opt/Klik && git add KK_suggest/evaluators/meeting_worker.py KK_suggest/tests/test_meeting_worker.py \
   KK_suggest/migrations/2026_05_17_002_sessions_processed_by_rules.sql Scripts/restart_all.sh && \
  git commit -m "feat(KK_suggest): meeting_worker — emit firings on completed sessions"'
```

---

## Phase 8 — state-diff scanner (`person_silence`, `okr_threshold`)

### Task 14: Pure evaluators

**Files:**
- Create: `/opt/Klik/KK_suggest/evaluators/state_eval.py`
- Create: `/opt/Klik/KK_suggest/tests/test_state_evaluators.py`

- [ ] **Step 1: Failing tests** — write `test_person_silence_fires_when_days_exceeded`, `test_person_silence_does_not_refire_same_day`, `test_okr_threshold_fires_below_percent`. Each follows the same shape as `test_cron_evaluator.py`.

- [ ] **Step 2: Implement two pure functions:**

```python
# evaluators/state_eval.py
from datetime import datetime, timedelta, timezone
from typing import Any

def person_silence_should_fire(rule, *, person_last_contacted, now=None) -> bool:
    if rule.get("status") != "active":
        return False
    params = (rule.get("parsed_trigger") or {}).get("params") or {}
    days = int(params.get("days", 0))
    if days <= 0:
        return False
    now = now or datetime.now(timezone.utc)
    if person_last_contacted is None:
        return False
    age = now - person_last_contacted
    if age < timedelta(days=days):
        return False
    last_fired = rule.get("last_fired_at")
    if last_fired and (now - last_fired) < timedelta(hours=23):
        return False
    return True


def okr_threshold_should_fire(rule, *, current_progress_pct) -> bool:
    if rule.get("status") != "active":
        return False
    params = (rule.get("parsed_trigger") or {}).get("params") or {}
    threshold = int(params.get("percent_below", 0))
    if threshold <= 0:
        return False
    return current_progress_pct < threshold
```

- [ ] **Step 3: Tests pass. Commit.**

```bash
ssh gcp 'cd /opt/Klik && git add KK_suggest/evaluators/state_eval.py KK_suggest/tests/test_state_evaluators.py && \
  git commit -m "feat(KK_suggest): state-diff evaluators (person_silence, okr_threshold)"'
```

---

### Task 15: Nightly state-diff worker

**Files:**
- Create: `/opt/Klik/KK_suggest/evaluators/state_worker.py`
- Create: `/opt/Klik/KK_suggest/tests/test_state_worker.py`

Follow the same pattern as Task 13's `meeting_worker`:
1. SELECT all active rules whose `signal_binding` is `person_silence` or `okr_threshold`.
2. For each rule, read the context (last-contacted timestamp for the person filter; current goal progress %).
3. Call the matching pure evaluator. If true → `_emit_firing` + bump `last_fired_at`.

Wire as a nightly process (cron timer) — not a per-minute loop. Add to `restart_all.sh` if you want it long-running, or schedule via systemd timer if that's the existing pattern.

> **Don't invent context queries.** Verify the exact columns for "last contact per person" (probably `people.last_contact_at`) and "goal progress %" (probably `user_goals.progress_pct`) on the live DB before writing the test fixtures.

- [ ] Write failing tests with real DB fixtures, implement, restart, commit.

```bash
ssh gcp 'cd /opt/Klik && git add KK_suggest/evaluators/state_worker.py KK_suggest/tests/test_state_worker.py Scripts/restart_all.sh && \
  git commit -m "feat(KK_suggest): nightly state-diff worker (person_silence, okr_threshold)"'
```

---

## Phase 9 — iOS frontend: DTOs + repository

### Task 16: `RuleDto` + `RulePreviewDto`

**Files:**
- Modify: `samples/composeApp/src/commonMain/kotlin/io/github/fletchmckee/liquid/samples/app/data/network/dto/UserDto.kt` *or* create new file:
- Create: `samples/composeApp/src/commonMain/kotlin/io/github/fletchmckee/liquid/samples/app/data/network/dto/RuleDto.kt`
- Create: `samples/composeApp/src/commonTest/kotlin/io/github/fletchmckee/liquid/samples/app/data/network/dto/RuleDtoTest.kt`

- [ ] **Step 1: Failing test**

```kotlin
// commonTest/.../RuleDtoTest.kt
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
```

- [ ] **Step 2: Run test to verify it fails**

```bash
cd /Users/wilsonxu/Klik_backup/Klik/Klik_one/liquid && \
  ./gradlew :samples:composeApp:iosSimulatorArm64Test \
  --tests "io.github.fletchmckee.liquid.samples.app.data.network.dto.RuleDtoTest"
```

Expected: FAIL — `RuleDto` / `RulePreviewDto` not found.

- [ ] **Step 3: Implement DTOs**

```kotlin
// commonMain/.../RuleDto.kt
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
```

- [ ] **Step 4: Run tests, then commit**

```bash
./gradlew :samples:composeApp:iosSimulatorArm64Test \
  --tests "io.github.fletchmckee.liquid.samples.app.data.network.dto.RuleDtoTest"
# expected: PASS

git add samples/composeApp/src/commonMain/kotlin/io/github/fletchmckee/liquid/samples/app/data/network/dto/RuleDto.kt \
        samples/composeApp/src/commonTest/kotlin/io/github/fletchmckee/liquid/samples/app/data/network/dto/RuleDtoTest.kt
git commit -m "feat(ios): RuleDto + RulePreviewDto"
```

---

### Task 17: `RulesRepository` (domain interface + impl)

**Files:**
- Create: `samples/composeApp/src/commonMain/kotlin/io/github/fletchmckee/liquid/samples/app/domain/repository/RulesRepository.kt`
- Create: `samples/composeApp/src/commonMain/kotlin/io/github/fletchmckee/liquid/samples/app/data/repository/RulesRepositoryImpl.kt`
- Modify: `samples/composeApp/src/commonMain/kotlin/io/github/fletchmckee/liquid/samples/app/di/AppModule.kt`

- [ ] **Step 1: Domain interface**

```kotlin
// domain/repository/RulesRepository.kt
package io.github.fletchmckee.liquid.samples.app.domain.repository

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.data.network.dto.RuleDto
import io.github.fletchmckee.liquid.samples.app.data.network.dto.RulePreviewDto

interface RulesRepository {
    suspend fun preview(nlText: String): Result<RulePreviewDto>
    suspend fun create(nlText: String, preview: RulePreviewDto, isRecurring: Boolean): Result<RuleDto>
    suspend fun list(): Result<List<RuleDto>>
    suspend fun edit(id: String, nlText: String? = null, isRecurring: Boolean? = null, status: String? = null): Result<RuleDto>
    suspend fun delete(id: String): Result<Unit>
    suspend fun accept(id: String): Result<RuleDto>
}
```

- [ ] **Step 2: Implementation using `HttpClient`**

```kotlin
// data/repository/RulesRepositoryImpl.kt
package io.github.fletchmckee.liquid.samples.app.data.repository

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.data.network.ApiConfig
import io.github.fletchmckee.liquid.samples.app.data.network.HttpClient
import io.github.fletchmckee.liquid.samples.app.data.network.dto.RuleDto
import io.github.fletchmckee.liquid.samples.app.data.network.dto.RulePreviewDto
import io.github.fletchmckee.liquid.samples.app.domain.repository.RulesRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class RulesRepositoryImpl : RulesRepository {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val base = "${ApiConfig.SUGGEST_BASE_URL}/v1/rules"

    override suspend fun preview(nlText: String): Result<RulePreviewDto> = runCatching {
        val body = buildJsonObject { put("nl_text", nlText) }.toString()
        val resp = HttpClient.postUrl("$base/preview", body)
            ?: error("No response from /rules/preview")
        json.decodeFromString(RulePreviewDto.serializer(), resp)
    }.fold({ Result.Success(it) }, { Result.Error(it as Exception) })

    override suspend fun create(nlText: String, preview: RulePreviewDto, isRecurring: Boolean): Result<RuleDto> = runCatching {
        val body = buildJsonObject {
            put("nl_text", nlText)
            put("is_recurring", isRecurring)
            put("preview", json.encodeToJsonElement(RulePreviewDto.serializer(), preview))
        }.toString()
        val resp = HttpClient.postUrl(base, body) ?: error("No response from /rules")
        json.decodeFromString(RuleDto.serializer(), resp)
    }.fold({ Result.Success(it) }, { Result.Error(it as Exception) })

    override suspend fun list(): Result<List<RuleDto>> = runCatching {
        val resp = HttpClient.getUrl(base) ?: error("No response from GET /rules")
        json.decodeFromString(kotlinx.serialization.builtins.ListSerializer(RuleDto.serializer()), resp)
    }.fold({ Result.Success(it) }, { Result.Error(it as Exception) })

    override suspend fun edit(id: String, nlText: String?, isRecurring: Boolean?, status: String?): Result<RuleDto> = runCatching {
        val body = buildJsonObject {
            if (nlText != null) put("nl_text", nlText)
            if (isRecurring != null) put("is_recurring", isRecurring)
            if (status != null) put("status", status)
        }.toString()
        val resp = HttpClient.patchUrl("$base/$id", body) ?: error("No response from PATCH /rules/{id}")
        json.decodeFromString(RuleDto.serializer(), resp)
    }.fold({ Result.Success(it) }, { Result.Error(it as Exception) })

    override suspend fun delete(id: String): Result<Unit> = runCatching {
        HttpClient.deleteUrl("$base/$id")
        Unit
    }.fold({ Result.Success(it) }, { Result.Error(it as Exception) })

    override suspend fun accept(id: String): Result<RuleDto> = runCatching {
        val resp = HttpClient.postUrl("$base/$id/accept", "") ?: error("No response from accept")
        json.decodeFromString(RuleDto.serializer(), resp)
    }.fold({ Result.Success(it) }, { Result.Error(it as Exception) })
}
```

> If `ApiConfig.SUGGEST_BASE_URL` doesn't exist yet, add it (Environment.kt → Config) following the pattern of `AUTH_BASE_URL`. Resolves to `https://hiklik.ai/api/suggest`.

- [ ] **Step 3: Wire in AppModule**

```kotlin
// di/AppModule.kt — add alongside _userRepository
private var _rulesRepository: RulesRepository? = null
val rulesRepository: RulesRepository
    get() = _rulesRepository ?: RulesRepositoryImpl().also { _rulesRepository = it }
```

- [ ] **Step 4: Build to verify it compiles**

```bash
./gradlew :samples:composeApp:linkDebugFrameworkIosSimulatorArm64
```

- [ ] **Step 5: Commit**

```bash
git add samples/composeApp/src/commonMain/kotlin/io/github/fletchmckee/liquid/samples/app/{domain/repository/RulesRepository.kt,data/repository/RulesRepositoryImpl.kt,di/AppModule.kt,data/network/ApiConfig.kt,data/network/Environment.kt}
git commit -m "feat(ios): RulesRepository + AppModule wiring + SUGGEST_BASE_URL"
```

---

## Phase 10 — iOS frontend: `NewRuleSheet`

### Task 18: `NewRuleSheet` composable + view model

**Files:**
- Create: `samples/composeApp/src/commonMain/kotlin/io/github/fletchmckee/liquid/samples/app/presentation/rules/NewRuleViewModel.kt`
- Create: `samples/composeApp/src/commonMain/kotlin/io/github/fletchmckee/liquid/samples/app/ui/klikone/NewRuleSheet.kt`
- Create: `samples/composeApp/src/commonTest/kotlin/io/github/fletchmckee/liquid/samples/app/presentation/rules/NewRuleViewModelTest.kt`

- [ ] **Step 1: Failing test** — `NewRuleViewModelTest`

```kotlin
package io.github.fletchmckee.liquid.samples.app.presentation.rules

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.data.network.dto.RuleDto
import io.github.fletchmckee.liquid.samples.app.data.network.dto.RulePreviewDto
import io.github.fletchmckee.liquid.samples.app.domain.repository.RulesRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject

private class FakeRulesRepo(private val preview: RulePreviewDto) : RulesRepository {
    override suspend fun preview(nlText: String) = Result.Success(preview)
    override suspend fun create(nlText: String, preview: RulePreviewDto, isRecurring: Boolean) =
        Result.Success(RuleDto("r1", "user_defined", nlText, preview.triggerLabel,
                               preview.actionLabel, "active", true, null))
    override suspend fun list() = Result.Success(emptyList<RuleDto>())
    override suspend fun edit(id: String, nlText: String?, isRecurring: Boolean?, status: String?) =
        Result.Error(NotImplementedError())
    override suspend fun delete(id: String) = Result.Error(NotImplementedError())
    override suspend fun accept(id: String) = Result.Error(NotImplementedError())
}

class NewRuleViewModelTest {
    @Test fun submitting_runs_preview_then_create_and_emits_done() = runTest {
        val fake = FakeRulesRepo(RulePreviewDto(
            triggerLabel = "every Mon 9 AM", actionLabel = "draft recap",
            approximationNote = null,
            parsedTrigger = JsonObject(emptyMap()),
            parsedAction = JsonObject(emptyMap()),
            signalBinding = "cron",
        ))
        val vm = NewRuleViewModel(fake)
        vm.updateNl("every Mon 9, draft recap")
        vm.runPreview()
        assertEquals("every Mon 9 AM", vm.state.value.preview?.triggerLabel)
        vm.confirm()
        assertNotNull(vm.state.value.createdRuleId)
    }
}
```

- [ ] **Step 2: Run test, see it fail**

- [ ] **Step 3: Implement `NewRuleViewModel`**

```kotlin
// presentation/rules/NewRuleViewModel.kt
package io.github.fletchmckee.liquid.samples.app.presentation.rules

import io.github.fletchmckee.liquid.samples.app.core.BaseViewModel
import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.data.network.dto.RulePreviewDto
import io.github.fletchmckee.liquid.samples.app.domain.repository.RulesRepository

data class NewRuleUiState(
    val nlText: String = "",
    val preview: RulePreviewDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val createdRuleId: String? = null,
)

sealed class NewRuleEvent {
    data object Dismiss : NewRuleEvent()
}

class NewRuleViewModel(
    private val repo: RulesRepository,
) : BaseViewModel<NewRuleUiState, NewRuleEvent>() {

    override val initialState = NewRuleUiState()

    fun updateNl(text: String) { updateState { copy(nlText = text, error = null) } }

    fun runPreview() = launch {
        updateState { copy(isLoading = true, error = null) }
        when (val r = repo.preview(currentState.nlText)) {
            is Result.Success -> updateState { copy(preview = r.data, isLoading = false) }
            is Result.Error -> updateState { copy(isLoading = false, error = r.exception.message) }
            is Result.Loading -> Unit
        }
    }

    fun confirm() = launch {
        val p = currentState.preview ?: return@launch
        when (val r = repo.create(currentState.nlText, p, isRecurring = true)) {
            is Result.Success -> {
                updateState { copy(createdRuleId = r.data.id) }
                sendEvent(NewRuleEvent.Dismiss)
            }
            is Result.Error -> updateState { copy(error = r.exception.message) }
            is Result.Loading -> Unit
        }
    }
}
```

- [ ] **Step 4: Implement `NewRuleSheet` composable**

```kotlin
// ui/klikone/NewRuleSheet.kt
package io.github.fletchmckee.liquid.samples.app.ui.klikone

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import io.github.fletchmckee.liquid.samples.app.presentation.rules.NewRuleViewModel

@Composable
fun NewRuleSheet(
    viewModel: NewRuleViewModel,
    onDismiss: () -> Unit,
) {
    val ui by viewModel.state.collectAsState()

    Column(
        Modifier.fillMaxWidth().background(KlikPaperCard).padding(24.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Teach Klik a rule", style = K1Type.h2)
        Text(
            "Klik will run this for you on schedule or in context. Write it like you'd ask a teammate.",
            style = K1Type.bodySm.copy(color = KlikInkTertiary),
        )

        Column(
            Modifier.fillMaxWidth().clip(K1R.card).background(KlikPaperSoft)
                .border(0.5.dp, KlikLineHairline, K1R.card).padding(14.dp),
        ) {
            BasicTextField(
                value = ui.nlText,
                onValueChange = viewModel::updateNl,
                textStyle = K1Type.bodyMd,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
            )
        }

        // Example chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                "After every 1:1, draft a recap email",
                "Every Monday 9 AM, summarize last week",
                "When I haven't talked to a key contact in 14 days",
            ).forEach { ex ->
                K1Chip(label = ex, onClick = { viewModel.updateNl(ex) })
            }
        }

        ui.preview?.let { p ->
            Column(Modifier.fillMaxWidth().clip(K1R.card).background(KlikPaperSoft)
                .border(0.5.dp, KlikLineHairline, K1R.card).padding(14.dp)) {
                Text("Klik will: ${p.actionLabel}", style = K1Type.bodyMd)
                Text("When: ${p.triggerLabel}", style = K1Type.bodyMd.copy(color = KlikInkSecondary))
                if (!p.approximationNote.isNullOrBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text("⚠ ${p.approximationNote}", style = K1Type.metaSm.copy(color = KlikInkTertiary))
                }
            }
        }

        ui.error?.let {
            Text("Couldn't parse: $it. Try rephrasing.", style = K1Type.metaSm.copy(color = KlikInkSecondary))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            K1ButtonGhost(label = "Cancel", onClick = onDismiss, modifier = Modifier.weight(1f))
            if (ui.preview == null) {
                K1ButtonPrimary(
                    label = if (ui.isLoading) "Parsing…" else "Preview",
                    enabled = !ui.isLoading && ui.nlText.trim().isNotEmpty(),
                    onClick = { viewModel.runPreview() },
                    modifier = Modifier.weight(1f),
                )
            } else {
                K1ButtonPrimary(
                    label = "Add to Featured",
                    onClick = { viewModel.confirm() },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
```

> If `K1ButtonGhost` doesn't exist, add a minimal one in `KlikOneKit.kt` mirroring `K1ButtonPrimary` with a hairline border + transparent background.

- [ ] **Step 5: Tests + build pass**

```bash
./gradlew :samples:composeApp:iosSimulatorArm64Test --tests "*.presentation.rules.NewRuleViewModelTest"
./gradlew :samples:composeApp:linkDebugFrameworkIosSimulatorArm64
```

- [ ] **Step 6: Commit**

```bash
git add samples/composeApp/src/commonMain/kotlin/io/github/fletchmckee/liquid/samples/app/presentation/rules/NewRuleViewModel.kt \
        samples/composeApp/src/commonMain/kotlin/io/github/fletchmckee/liquid/samples/app/ui/klikone/NewRuleSheet.kt \
        samples/composeApp/src/commonTest/kotlin/io/github/fletchmckee/liquid/samples/app/presentation/rules/NewRuleViewModelTest.kt
git commit -m "feat(ios): NewRuleSheet + NewRuleViewModel"
```

---

## Phase 11 — Featured card enhancements

### Task 19: Inline `trigger_label` on Featured cards

**Files:**
- Modify: `samples/composeApp/src/commonMain/kotlin/io/github/fletchmckee/liquid/samples/app/data/network/dto/FeaturedTaskDto.kt`
- Modify: the `FeaturedCard` composable in `MovesScreen.kt`

- [ ] Add `ruleId: String? = null` and `triggerLabel: String? = null` to `FeaturedTaskDto`. Render the label as a small `K1Type.metaSm` row beneath the title when non-null.

- [ ] Build, simulator-test (visual), commit.

```bash
git commit -m "feat(ios): Featured card shows rule trigger label inline"
```

---

### Task 20: "+" button + sheet wiring on Featured section header

**Files:** Modify `MovesScreen.kt` (Featured section header).

- [ ] Add a state `var showNewRuleSheet by rememberSaveable { mutableStateOf(false) }`.
- [ ] Render a small "+" button in the section header. `onClick = { showNewRuleSheet = true }`.
- [ ] When `showNewRuleSheet`: render `ModalBottomSheet { NewRuleSheet(viewModel = …, onDismiss = { showNewRuleSheet = false; viewModel.refreshFeatured() }) }`.
- [ ] After dismiss, refetch `/featured` to surface the new rule's first card.

- [ ] Build, simulator-test the end-to-end flow once (create rule → see it on Moves).
- [ ] Commit.

```bash
git commit -m "feat(ios): + on Featured header opens NewRuleSheet, refreshes on dismiss"
```

---

### Task 21: Long-press menu (Edit / Pause / Delete / Snooze 7d)

**Files:** Modify `MovesScreen.kt`.

- [ ] Wrap each Featured card in a long-press detector. On long-press, show a `DropdownMenu` with four actions:
  - **Edit** → re-open `NewRuleSheet` prefilled with the rule's `nl_text` (need a method `RulesRepository.fetch(id)` or pass the rule from list response).
  - **Pause** → `repo.edit(id, status = "paused")` then refetch.
  - **Delete** → confirm dialog → `repo.delete(id)` → refetch.
  - **Snooze 7d** → for v1, equivalent to `Pause` + a local 7-day reminder; ship as `Pause` only, leave Snooze stubbed with a TODO log. (Documented gap, not a placeholder in code.)
- [ ] Build, simulator-test, commit.

```bash
git commit -m "feat(ios): long-press Featured card → Edit/Pause/Delete menu"
```

---

### Task 22: Accept/Decline for pending Klik-inferred rules

**Files:** Modify `MovesScreen.kt` (`FeaturedCard`).

- [ ] When `dto.status == "pending_review"` (need to expose status on FeaturedTaskDto OR fetch rule lazily), render **Accept / Decline** instead of Start / Skip.
- [ ] Accept → `repo.accept(ruleId)` → refetch.
- [ ] Decline → `repo.edit(ruleId, status = "archived")` → refetch.
- [ ] Build, simulator-test, commit.

```bash
git commit -m "feat(ios): pending rules render Accept/Decline on Featured"
```

---

## Phase 12 — Ship

### Task 23: TestFlight build

- [ ] Bump `samples/iosApp/Configuration/Config.xcconfig` → `CURRENT_PROJECT_VERSION=<next>`.
- [ ] `./gradlew :samples:composeApp:linkReleaseFrameworkIosArm64`
- [ ] `xcodebuild archive` → `-exportArchive` → `xcrun altool --upload-app` (per repo CLAUDE.md commands).
- [ ] Commit + push.

---

## Self-review

**Spec coverage:**
- ✅ `featured_rules` + `featured_tasks.rule_id` schema → Task 1
- ✅ NL → rule mapper with constrained output → Tasks 2–3
- ✅ All 6 REST endpoints (preview, create, list, patch, delete, accept) → Tasks 4–6
- ✅ Existing `/featured` enriched, shape preserved → Task 11
- ✅ Cron evaluator + worker → Tasks 7–9
- ✅ Daily inference rewrite (now writes pending rules) → Task 10
- ✅ meeting_ended evaluator + worker → Tasks 12–13
- ✅ state-diff evaluators (person_silence, okr_threshold) + nightly worker → Tasks 14–15
- ✅ iOS DTOs + repository → Tasks 16–17
- ✅ NewRuleSheet (+ examples + approximation rendering) → Task 18
- ✅ Featured "+" header + sheet wiring → Task 20
- ✅ Inline `trigger_label` on cards → Task 19
- ✅ Long-press menu → Task 21
- ✅ Accept/Decline for pending rules → Task 22
- ⚠️ Migration cleanup (drop `predicted_for` after 2 weeks) is in the spec as a *plan*, not implemented in this plan. Will live as a follow-up issue.
- ⚠️ "Snooze 7d" is shipped as Pause with a stubbed TODO log per Task 21 — flagged.

**Placeholder scan:** No "TBD" / "fill in" steps. The two "pre-req" notes in Tasks 13 and 15 ("confirm exact session schema", "verify last-contact / progress columns") are required investigation moments — they tell the implementer to *check the DB* before writing fixtures, which is the right discipline.

**Type consistency:** `RuleDto`/`RulePreviewDto` field names match across DTO, repository, view model. Backend `RuleOut` keys match the DTO `@SerialName` annotations.

---

## Execution Handoff

**Plan complete and saved to `liquid/docs/superpowers/plans/2026-05-17-contextual-task-space.md`.** Two execution options:

1. **Subagent-Driven (recommended)** — I dispatch a fresh subagent per task, review between tasks, fast iteration.
2. **Inline Execution** — Execute tasks in this session using executing-plans, batch execution with checkpoints.

Which approach?
