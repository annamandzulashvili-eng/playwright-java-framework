---
name: debug-failing-test
description: Reproduce and diagnose a failing test (Class#method, scenario key or Qase case id) from real evidence — logs, Allure results, screenshot, trace — classify it as test bug, product bug or environment issue, and fix test bugs minimally. Use when a test fails locally or in CI.
argument-hint: "<Class#method | scenario key | qase case id>"
allowed-tools: Read, Edit, Glob, Grep, Bash(mvn -q test-compile), Bash(mvn verify *), Bash(mvn -q verify *), mcp__playwright__*, mcp__qase__qase_get
---

# Debug a failing test

## 1. Locate the test
- `Class#method` as given; scenario key → `automation.test` in its JSON; Qase id → `Grep` for `@QaseId(<id>)`.
- Read the test, the steps/pages or clients it uses, and its scenario JSON (the intended behaviour).

## 2. Reproduce with full evidence
```
mvn verify -Dtest=<Class>#<method> -Dtrace=on -Dvideo=retain_on_failure
```
Collect:
- console output (first exception and its cause);
- `target/surefire-reports/` for the stack trace;
- the newest `target/allure-results/*-result.json` for this test: failing step, status details, attachments;
- `target/artifacts/<test>-*/`: `trace.zip` (tell the user `npx playwright show-trace <path>`), screenshot attachment.
If it passes, run it 3 more times. Intermittent → flaky: look for missing synchronization or shared data.

## 3. Analyse
Delegate to the `failure-analyst` subagent with: test file path, scenario JSON path, the failing step,
exception text and attachment paths. Then, for UI failures, check the hypothesis on the live page with the
Playwright MCP (`browser_navigate`, `browser_snapshot`): does the element exist, did text or attributes change?

Classify:
| Class | Typical evidence | Action |
|---|---|---|
| Test bug | locator changed, missing wait, wrong expectation vs. scenario | fix in the right layer (locator → page via [stable-locators](../stable-locators/SKILL.md), sync → step, expectation → test) |
| Product bug | app behaviour contradicts the scenario and docs | do not change the test; draft a defect |
| Environment | `ServiceOverloadedException`, timeout / 5xx / DNS / site down; passes on rerun | report; consider opt-in retry only for idempotent tests |

## 4. Fix (test bugs only)
- Minimal diff in the correct layer. Never "fix" by adding sleeps, widening assertions, or catching exceptions.
- `mvn -q test-compile`, then rerun the test **twice** green.

## 5. Product bug
Show the user a defect draft: title, steps (from the scenario), expected vs actual, evidence paths.
Only after explicit approval create it with `mcp__qase__qase_triage_defect` (the tool asks again).

## Output
Root cause in one sentence, classification, evidence used, diff (if any), rerun results.
