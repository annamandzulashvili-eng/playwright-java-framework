---
name: automate-api-scenario
description: Automate one approved (Status Actual) Qase case as an API test with REST Assured + TestNG using typed clients and models, verify against the live endpoints, link it with @QaseId and tag the case ai_automated. Use when asked to automate an API test case.
argument-hint: "<qase case id | scenario key>"
allowed-tools: Read, Edit, Write, Glob, Grep, Bash(mvn -q test-compile), Bash(mvn verify *), Bash(mvn -q verify *), Bash(python scripts/validate_ai_config.py), Bash(python3 scripts/validate_ai_config.py), mcp__playwright__*, mcp__qase__qase_get, mcp__qase__qase_project_context
---

# Automate an API scenario

Read `CLAUDE.md` first; its layer rules are mandatory.

## 1. Get the approved case
Same as [automate-ui-scenario §1](../automate-ui-scenario/SKILL.md#1-get-the-approved-case): only Actual cases are automated.

## 2. Learn the endpoint from evidence
- Existing clients `qa.app.api.clients.*` and models `qa.app.api.models.*` first.
- The API reference page https://automationexercise.com/api_list (Playwright MCP `browser_navigate` + `browser_snapshot`)
  for method, path, parameters and documented response.
- Behaviour that the docs do not state (status codes in the body, exact messages) must be confirmed by the
  first test run, not assumed. Note: this service returns HTTP 200 and the real code in `responseCode`.

## 3. Implement
- **Model**: Java `record` in `qa.app.api.models`; only fields the tests use; `@JsonProperty` for snake_case.
- **Client**: one method per endpoint/variant in the matching `*ApiClient` (extends `BaseApiClient`):
  `@Step("API: ...")`, form/query params as the endpoint expects, mask secrets with `@Param(mode = MASKED)`,
  return `ApiResult<T>` via `toResult(...)`. Never assert in clients.
- **Contract**: for a new list-style response, add a JSON Schema under `src/test/resources/schemas/` and validate it.
- **Test** in `src/test/java/qa/tests/api/<Feature>ApiTest.java`:
  `@Test(groups = {Groups.API, Groups.REGRESSION[, Groups.SMOKE]}, description = "<scenario title>")`,
  `@QaseId(<id>)` when linked; multi-step scenarios use one `Allure.step("<step action>", () -> { ... })` per step.
  Data from `UserFactory`; anything created is cleaned up (`createUserViaApi()` / `deleteAfterTest`).
  Use a `@DataProvider` only when the scenario's data column lists several values.

## 4. Verify
1. `mvn -q test-compile`
2. `mvn verify -Dtest=<Class>#<method>` — green twice. On failure use [debug-failing-test](../debug-failing-test/SKILL.md).
3. `test-reviewer` subagent on the diff.

## 5. Link and tag
As in [automate-ui-scenario §6](../automate-ui-scenario/SKILL.md#6-link-and-tag), with `qa.tests.api.<Class>#<method>`.

## Output
Files changed, endpoint facts confirmed by the run vs. taken from docs, run results.
