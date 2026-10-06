---
name: automate-ui-scenario
description: Automate one approved (Status Actual) Qase case as a Playwright Java + TestNG UI test following this framework's layers, with locators from the stable-locators skill; verify it compiles and passes, link it with @QaseId and tag the case ai_automated. Use when asked to automate a UI/E2E test case.
argument-hint: "<qase case id | scenario key>"
allowed-tools: Read, Edit, Write, Glob, Grep, Bash(mvn -q test-compile), Bash(mvn verify *), Bash(mvn -q verify *), Bash(python scripts/validate_ai_config.py), Bash(python3 scripts/validate_ai_config.py), mcp__playwright__*, mcp__qase__qase_get, mcp__qase__qase_project_context
---

# Automate a UI scenario

Read `CLAUDE.md` first; its layer rules are mandatory.

## 1. Get the approved case
- Input is a Qase id (`PJF-25`) or a scenario key (use its `qaseId`; no `qaseId` → stop and suggest `/tm-upload-scenarios`).
- Fetch the **current** case with [tm-get-case](../tm-get-case/SKILL.md). Qase is the source of truth for the case text.
- **Gate: status must be Actual** (reviewed and approved by the user). Draft → stop with
  "PJF-<id> is still a Draft — review it in Qase and set Status → Actual first." Deprecated → stop.
- Write or refresh the local snapshot `scenarios/cases/<key>.json` from the fetched case (keep `key`; set `qaseId`).
- Already automated (`automation.status: automated` with an existing test) → suggest `/sync-scenario` instead.

## 2. Find what can be reused
- Test classes in `src/test/java/qa/tests/ui` — pick the class for the feature (Auth, Catalog, Cart) or create `<Feature>UiTest`.
- Pages/components in `qa.app.ui.pages|components`, flows in `qa.app.ui.steps`, API setup in `BaseTest` / `qa.app.api.clients`.
- Ask the user only if two existing tests look like valid templates and they differ in approach.

## 3. Explore the real UI and choose locators
Perform the case steps once by hand with the Playwright MCP (`browser_navigate`, `browser_snapshot`).
For every element the test needs that has no locator yet, follow [stable-locators](../stable-locators/SKILL.md):
it picks the locator from the live page, proves it is unique and stable, and tells you where it belongs.
If a step cannot be performed on the live site, stop and report what you saw.

## 4. Implement (smallest change that fits the layers)
- **Page / component**: new locator methods returning `Locator`, atomic actions. No assertions, no waits-by-time.
- **Steps**: one public `@Step("...")` method per scenario step that is a user action; it synchronizes on the
  result of its own action (`waitFor`, `waitForURL`) and returns the page the user lands on.
  Register new flows in `qa.app.ui.Storefront` if they are new.
- **Test** method in the feature class:
  - `@Test(groups = {Groups.UI, Groups.REGRESSION[, Groups.SMOKE]}, description = "<scenario title>")`
  - `@QaseId(<id>)` when the scenario has a `qaseId`; `@Severity` for blocker/critical.
  - Arrange with API setup (`createUserViaApi()`, API clients), act through `ui()`, assert with Playwright
    `assertThat(locator)` for UI and AssertJ `then(...)` for data. Order of actions mirrors the scenario steps.
  - Test data from `UserFactory` / API; no hard-coded accounts; cleanup registered for anything created.
- Do not touch unrelated code. Do not add dependencies.

## 5. Verify
1. `mvn -q test-compile`
2. Run only this test: `mvn verify -Dtest=<Class>#<method>`; on failure use [debug-failing-test](../debug-failing-test/SKILL.md),
   fix, rerun. Run it **twice** green to rule out flakiness.
3. Review: delegate the diff to the `test-reviewer` subagent; apply justified findings.

## 6. Link and tag
1. Scenario JSON: `automation.status: "automated"`, `automation.test: "qa.tests.ui.<Class>#<method>"`.
2. `python scripts/validate_ai_config.py` must pass.
3. If the scenario has a `qaseId`: [tm-set-tags](../tm-set-tags/SKILL.md) with `ai_automated` (asks the user).
   If not: offer `/tm-upload-scenarios <key>` first.

## Output
Files changed, locators chosen and where they came from, test run results, anything left unverified.
