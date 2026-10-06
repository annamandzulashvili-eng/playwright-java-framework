---
name: tm-set-tags
description: Add AI tags (ai_automated or ai_refactored) to a Qase case after explicit user approval, merging with existing tags and verifying the result; also updates the local scenario file. Use at the end of automate-* and sync-scenario.
argument-hint: "<case id> <ai_automated|ai_refactored>"
allowed-tools: Read, Edit, Grep, mcp__qase__qase_get
---

# Tag a Qase case

Equivalent of the `ai_automated` / `ai_refactored` labels used in Zephyr workflows.

## Input
- Case id and one tag: `ai_automated` or `ai_refactored`. Any other tag: refuse.
- Precondition (the caller confirms): the implementing test passed twice and was reviewed (`test-reviewer`).

## Steps
1. Read the case: `mcp__qase__qase_get` (`entity: "case"`, `fields: ["id","title","tags"]`).
2. Merge: `current tags ∪ {new tag}`. Qase may return tags as objects (`{title, internal_id}`); compare and send their `title` strings. If the tag is already there, report "no change" and stop.
3. Show the user: case id, title, tags before → after. **Ask for approval.** No approval → stop.
4. Write: `mcp__qase__qase_case_upsert` with `code`, `id` and the **full merged** `tags` list
   (the API replaces the list; sending only the new tag would delete the others).
5. Verify: read the case again and confirm the tag set equals the merged set. Report any mismatch.
6. Local file: add the tag to `tags` in `scenarios/cases/<key>.json` (find it by `"qaseId": <id>`).

## Output
One line per case: `PJF-<id>  <title>  tags: [...]  ✔ verified` or the error.
