---
name: sync-scenario
description: Synchronize an automated test with a test case that changed in Qase — compare steps, apply the minimal code change, update the scenario JSON, verify, and tag the case ai_refactored. Use when a Qase case was edited after it was automated.
argument-hint: "<qase case id>"
allowed-tools: Read, Edit, Glob, Grep, Bash(mvn -q test-compile), Bash(mvn verify *), Bash(mvn -q verify *), Bash(python scripts/validate_ai_config.py), Bash(python3 scripts/validate_ai_config.py), mcp__playwright__*, mcp__qase__qase_get, mcp__qase__qase_project_context
---

# Sync a test with its changed case

## 1. Compare
1. [tm-get-case](../tm-get-case/SKILL.md) for the id → status, remote scenario and diff with the local snapshot.
   Status must be **Actual**: an edited case that is back in Draft is still under review → stop and say so.
2. Locate the test (`automation.test`, or `Grep` for `@QaseId(<id>)`). No test → suggest `/automate-ui-scenario`.
3. Build a step matrix and show it:

| # | Local step | Remote step | Change | Code impact |
|---|---|---|---|---|
| 1 | … | … | unchanged / modified / added / removed / moved | file + method |

No differences → report "in sync" and stop.

## 2. Apply the minimal change
- Change only what the matrix requires, in the right layer (CLAUDE.md). Keep names, order and style of untouched code.
- Removed steps: delete their code only if nothing else uses it (`Grep` callers first).
- Added or changed UI elements: get locators with [stable-locators](../stable-locators/SKILL.md).
- Update the local scenario JSON to the remote version (keep `key`, `qaseId`, `automation`).

## 3. Verify
`python scripts/validate_ai_config.py`, `mvn -q test-compile`, `mvn verify -Dtest=<Class>#<method>` twice green,
`test-reviewer` subagent on the diff.

## 4. Tag
[tm-set-tags](../tm-set-tags/SKILL.md) with `ai_refactored` (asks the user).

## Output
The step matrix, files changed, run results, tag result.
