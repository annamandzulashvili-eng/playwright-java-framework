---
name: generate-scenario
description: Turn a user story, bug report or feature description into business-level test cases and create them in Qase as Draft cases tagged ai_generated, for the user to review and approve in Qase. Use when the user asks to design or write test cases.
argument-hint: "<story text or path to a story file>"
allowed-tools: Read, Glob, Grep, mcp__playwright__*, mcp__qase__qase_project_context, mcp__qase__qase_get, mcp__qase__qql_search
---

# Generate test cases as Qase drafts

AI writes, the user approves. This skill only ever creates cases with status **Draft**. The user reviews,
edits and sets them to **Actual** in Qase; only Actual cases can be automated
(see [the case lifecycle](../../../docs/ai/qase-mapping.md#case-lifecycle)).

## Input
- `$ARGUMENTS`: story / acceptance criteria as text, or a path to a file with them.
- If the story is too vague to derive a testable outcome, ask one focused question before drafting.

## Steps
1. **Current coverage**: `qase_project_context` (suites) and `qql_search` for cases in the related suites;
   local snapshots in `scenarios/cases/*.json`. Note overlaps; do not draft duplicates.
2. **Product facts**: look at the live page with the Playwright MCP (`browser_navigate`, `browser_snapshot`) or the
   API reference https://automationexercise.com/api_list. Record only what you observed.
3. **Design** the minimum set of cases that covers the acceptance criteria:
   - one case = one user goal with one observable outcome; positive path first, then the negatives that matter;
   - API cases for contract and business rules, UI cases only for behaviour a user sees;
   - steps are business actions ("Add two products to the cart"), not clicks; 1-6 steps;
   - every step has a checkable expected result with concrete values where known;
   - preconditions describe data state ("An account created via API"), never real personal data or secrets.
4. **Preview** in chat: a table (title, suite, priority, severity, number of steps), then each case in full,
   then the facts you could not confirm. **Ask for approval to create the drafts**; apply the user's edits first.
5. **Create** after approval (each write tool asks again):
   - missing suites with `qase_suite_upsert`, top-down with `parent_id`;
   - cases with `qase_case_bulk_create`, fields per [the mapping](../../../docs/ai/qase-mapping.md), with
     `status: "draft"`, `automation: "1"` (to be automated) and tags `ai_generated`, `regression`, `api` or `ui`
     (`smoke` only for a critical path).
6. **Verify**: `qase_get` each new case; title, status and step count must match the preview.

## Output
Table `PJF-<id>` · title · suite, each with its link `https://app.qase.io/case/PJF-<id>`, then:
> Review each case in Qase, edit if needed and set **Status → Actual**. Then run
> `/automate-ui-scenario PJF-<id>` or `/automate-api-scenario PJF-<id>`.

## Rules
- Never create a case with any status other than Draft; never edit or delete existing cases from this skill.
- No local files here: the local snapshot is written when an approved case is automated.
- Do not invent product behaviour; unconfirmed facts go into the report, not into the case.
