# Scenario ↔ Qase mapping

**Qase is the source of truth for case content**; the user reviews and approves cases there.
`scenarios/cases/<key>.json` is the Git snapshot of the approved version a test implements — written when a case
is automated, refreshed by `sync-scenario`, checked in CI. Skills translate with this table; keep both directions symmetric.

## Case lifecycle

```
/generate-scenario ──► Draft (tag ai_generated) ──► user reviews/edits in Qase ──► Actual
                                                                                    │
           /sync-scenario ◄── case edited in Qase ◄── automated + ai_automated ◄── /automate-*-scenario
```

| Status in Qase | Meaning | Who sets it | AI may automate? |
|---|---|---|---|
| Draft | written by AI or a person, not reviewed | `generate-scenario` (always Draft) | no |
| Actual | reviewed and approved | **the user, in Qase** | yes |
| Deprecated | no longer valid | the user | no |

AI never sets a case to Actual. The one exception is the one-time bootstrap (`tm-upload-scenarios`) of tests that
already existed and passed before Qase was connected; the user approved uploading those as Actual.

| Scenario JSON | Qase case field (MCP `qase_case_upsert`) | Notes |
|---|---|---|
| `title` | `title` | |
| `objective` | `description` | |
| `preconditions` | `preconditions` | `"None"` ↔ empty |
| `priority` | `priority` | `high` / `medium` / `low` labels |
| `severity` | `severity` | `blocker` … `trivial` labels |
| `layer` | `layer` | `api` → `api`, `e2e` → `e2e` |
| — | `type` | always `functional` |
| — | `status` | `draft` for new AI cases; snapshots exist only for `actual` cases |
| `automation.status` | `automation` | `automated` → `2`, `to-be-automated` → `1`, `manual` → `0` |
| `tags` | `tags` | full list; Qase replaces the list on update, so always send the merged set |
| `steps[].action` | `steps[].action` | business action |
| `steps[].data` | `steps[].data` | optional |
| `steps[].expectedResult` | `steps[].expected_result` | |
| `suite` (`"UI / Cart"`) | `suite_id` | resolve the path with `qase_project_context`; create missing levels with `qase_suite_upsert` (`parent_id`) |
| `qaseId` | case `id` | written back after creation |
| `automation.test` | — | Git-only: `qa.tests.<pkg>.<Class>#<method>` |

## Code ↔ case

- One case = one `@Test` method, annotated `@QaseId(<qaseId>)` (`io.qase.commons.annotation.QaseId`).
- Each case step is one Allure step in that test: a `@Step` method of a steps class or an `Allure.step("...")` block,
  in the same order and wording close to `action`.
- Tests stay independent. Never chain case steps as separate `@Test` methods with `dependsOnMethods` / `priority`:
  it breaks parallel runs and makes one failure cascade.

## AI tags

| Tag | Set when | Set by |
|---|---|---|
| `ai_generated` | the case text was drafted by AI | `generate-scenario` |
| `ai_automated` | a case was automated with AI help, the test passed twice and `test-reviewer` approved it | `automate-ui-scenario`, `automate-api-scenario` via `tm-set-tags` |
| `ai_refactored` | an existing test was synced to a changed case, passed twice and was reviewed | `sync-scenario` via `tm-set-tags` |

## Write safety

Every write to Qase (create, update, tag) is shown to the user as a summary first and needs explicit approval;
`.claude/settings.json` also puts these tools behind an approval prompt. After a write, read the entity back and
compare. All Qase delete tools, tool discovery and the raw `qase_api` tool are denied in `.claude/settings.json`.

The project code comes from the `QASE_PROJECT` environment variable (default `PJF`).
