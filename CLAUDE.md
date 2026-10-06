# CLAUDE.md

Guidance for AI assistants working in this repository. Keep changes consistent with these rules.

## What this is
UI + API test automation framework for https://automationexercise.com.
Java 21, Maven, TestNG, Playwright Java, REST Assured, Allure.

## Commands
- Compile only: `mvn -q test-compile`
- Run a suite: `mvn verify -Dsuite=smoke|regression|api|ui|ui-smoke`
- Debug one UI run visibly: `mvn verify -Dsuite=ui -Dheadless=false -Dslowmo.ms=300 -Dtrace=on`
- Open report: `mvn allure:serve`
- Open a trace: `npx playwright show-trace target/artifacts/<test>/trace.zip`

## Layers (dependencies point down only)
```
tests (src/test/java/qa/tests)      → orchestrate + assert
  app/ui/Storefront → steps         → business flows, one Allure @Step each, sync on their own result
                     → pages/components → locators + atomic actions only
  app/api/clients                    → one method per endpoint, return ApiResult<T>, never assert
  app/data                           → test data models and factories
core (src/main/java/qa/core)        → config, browser lifecycle, API base, reporting, cleanup
```
`core` must never import from `app` or `tests`.

## Rules
- Locators live only in page/component classes. Prefer `getByTestId` (data-qa), then role/label/text, then stable CSS. No XPath unless nothing else exists. Never invent a selector — inspect the page first.
- No `Thread.sleep` / fixed waits. Synchronize on state: `locator.waitFor`, `page.waitForURL`, web-first `assertThat(locator)`.
- Assertions only in tests. UI: Playwright `assertThat`. Data: AssertJ `then(...)`.
- Every test is independent and parallel-safe: create its own data (`UserFactory`, `createUserViaApi()`), register undo with `Cleanup.register(...)` or `deleteAfterTest(user)`.
- Prefer API-assisted setup; drive the UI only for the behaviour under test.
- Anything stateful in a test class is `ThreadLocal` (suites run `parallel="methods"`).
- Every `@Test` has `groups` from `Groups` and a `description`. Add `smoke` only for critical paths.
- No secrets or real personal data in code, logs, step names or reports. Mask passwords with `@Param(mode = MASKED)`.
- Retries are opt-in (`retryAnalyzer = RetryAnalyzer.class`) and only for idempotent tests.
- After any change: `mvn -q test-compile` must pass; run the affected suite when a browser is available.
