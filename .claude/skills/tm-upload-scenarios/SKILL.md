---
name: tm-upload-scenarios
description: One-time bootstrap - create Qase cases (Status Actual, automated) from local snapshots of already automated tests that have no qaseId yet, then write the ids back into the JSON and as @QaseId on the tests. New cases come from generate-scenario instead. Asks for approval before writing.
argument-hint: "[scenario keys... | all]"
allowed-tools: Read, Edit, Glob, Grep, Bash(python scripts/validate_ai_config.py), Bash(python3 scripts/validate_ai_config.py), Bash(mvn -q test-compile), mcp__qase__qase_project_context, mcp__qase__qase_get, mcp__qase__qql_search
---

# Upload scenarios to Qase

## Input
- `$ARGUMENTS`: scenario keys, or `all` (default) = every `scenarios/cases/*.json` with `"qaseId": null`.
- Project: `QASE_PROJECT`, default `PJF`.

## Steps
1. **Validate first**: run `python scripts/validate_ai_config.py` (or `python3`). Fix nothing silently; stop on errors.
2. **Select** the scenarios with `qaseId: null`. Already linked ones are skipped and listed.
3. **Duplicate check**: `qase_project_context` → for each scenario, look for an existing case with the same title in the
   same suite (use `mcp__qase__qql_search` if the context is truncated). Existing match → propose linking instead of creating.
4. **Suites**: resolve every `suite` path against the suites tree. List the missing levels.
5. **Preview** a table for the user: key, title, suite (new/existing), priority, steps count, link-or-create.
   **Ask for approval.** No approval → stop, nothing written.
6. **Write**:
   - create missing suites top-down with `qase_suite_upsert` (`parent_id` for nested levels);
   - create cases with `qase_case_bulk_create` (max 100 per call), mapped per [the mapping table](../../../docs/ai/qase-mapping.md),
     with `status: "actual"` (these tests already exist and pass, so they need no draft review) and `automation: "2"`.
     Scenarios whose `automation.status` is not `automated` are not uploaded here: new cases go through
     `/generate-scenario` and the Draft → Actual review.
     Ids come back in submission order — keep the order list to pair them with keys.
7. **Verify**: `qase_get` two of the created cases and compare title and step count.
8. **Write back** for each created case:
   - `"qaseId": <id>` in the scenario JSON;
   - if `automation.test` is set: add `@QaseId(<id>)` directly above the test method (after `@Test(...)` and other
     annotations is fine) and `import io.qase.commons.annotation.QaseId;` once per file. Change nothing else.
9. **Check**: validator again, then `mvn -q test-compile`. Both must pass.

## Output
Table: key → `PJF-<id>` → test method, plus anything skipped and why.
Remind the user: once all automated scenarios are linked, set the GitHub repository variable
`QASE_REPORTING=true` so CI reports results into Qase.

## Rules
- Never delete or overwrite existing Qase cases here; updates belong to `sync-scenario`.
- If creation partly fails, write back only the ids that were created and report the rest.
