# Locator strategy

Reference for [the stable-locators skill](SKILL.md). Examples use this repo's page objects.

## The ladder

| # | Strategy | Example | Use when |
|---|---|---|---|
| 1 | Test id | `page.getByTestId("login-email")` (from `LoginPage`) — `data-qa`, configured in `testid.attribute` | element has a dedicated test hook |
| 2 | Role + accessible name | `page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Login"))` | semantic controls without a test id |
| 3 | Label / placeholder | `page.getByLabel("Email")`, `page.getByPlaceholder("Search Product")` | form fields |
| 4 | Exact static text | `root.getByText("Logged in as")` | fixed UI copy |
| 5 | Scoped CSS on stable attributes | `root.locator("a[href='/view_cart']")`, `page.locator("#search_product")` | no semantic hook; attribute is part of the app contract (routes, form names, hand-written ids) |
| 6 | XPath | — | last resort, with a comment explaining why |

## Scoping instead of indexing

```java
// BAD: position-dependent, breaks when products are reordered
page.locator(".productinfo a.add-to-cart").nth(2);

// GOOD: find the card by its business key, then the control inside it
productCards()
    .filter(new Locator.FilterOptions().setHas(
        page.locator(".productinfo p").getByText(productName, new Locator.GetByTextOptions().setExact(true))))
    .locator(".productinfo a.add-to-cart");
```

Component roots keep locators short and unambiguous: `Header` scopes everything under `#header`,
so `root.locator("a[href='/login']")` cannot hit a footer link with the same href.

## Red flags — reject the candidate

- generated ids or classes: `#ember1234`, `.css-1q2w3e`, `[id^='react-select-']`, long digit runs
- layout / styling classes: `.col-sm-4`, `.pull-right`, `.btn-default`
- absolute XPath or structure chains: `/html/body/div[2]/...`, `div > div > div:nth-child(3)`
- `nth()` / `first()` / `last()` to "fix" multiple matches
- text that depends on data (user names, prices, dates) or may be translated
- attributes that change with state: `.active`, `[aria-expanded]`, `[style*=…]`

## Synchronization is not the locator's job

Locators describe *where*; steps decide *when*. A flaky test is often a missing wait in the step
(`waitFor`, `waitForURL`, web-first `assertThat`), not a bad locator. Never compensate with sleeps.

## Strictness as a safety net

Playwright actions (`click`, `fill`, …) throw if a locator matches more than one element. A test that passes
twice in CI therefore also confirms every acted-on locator is unique at run time.
