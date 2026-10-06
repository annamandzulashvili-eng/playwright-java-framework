#!/usr/bin/env python3
"""Contract checks for the AI layer and the scenario <-> test mapping.

Runs in CI (job "ai-config") and locally:  python scripts/validate_ai_config.py

Checks
  scenarios   every file matches scenarios/schema.json; key == file name; keys and qaseIds unique
  mapping     automation.test points to an existing @Test method; qaseId <-> @QaseId agree both ways
  skills      .claude/skills/*/SKILL.md frontmatter (name == folder, description) and relative links
  agents      .claude/agents/*.md frontmatter (name == file, description, tools)
  mcp         .mcp.json is valid and holds no literal secrets (only ${VAR} references)
"""
from __future__ import annotations

import json
import re
import sys
from pathlib import Path

import yaml
from jsonschema import Draft202012Validator

ROOT = Path(__file__).resolve().parent.parent
TESTS = ROOT / "src" / "test" / "java"
errors: list[str] = []


def err(where: Path | str, msg: str) -> None:
    rel = where.relative_to(ROOT) if isinstance(where, Path) else where
    errors.append(f"{rel}: {msg}")


# ---------------------------------------------------------------- java index
METHOD = re.compile(r"public\s+void\s+(\w+)\s*\(")
QASE_ID = re.compile(r"@QaseId\(\s*(\d+)\s*\)")


def java_methods(path: Path) -> dict[str, str]:
    """method name -> annotation block written directly above it"""
    text = path.read_text(encoding="utf-8")
    out: dict[str, str] = {}
    for m in METHOD.finditer(text):
        start = max(text.rfind("\n    }", 0, m.start()), text.rfind("{\n", 0, m.start()))
        out[m.group(1)] = text[start:m.start()]
    return out


def index_tests() -> dict[str, dict[str, str]]:
    index = {}
    for f in TESTS.rglob("*Test.java"):
        fqcn = ".".join(f.relative_to(TESTS).with_suffix("").parts)
        index[fqcn] = java_methods(f)
    return index


# ---------------------------------------------------------------- scenarios
def check_scenarios(tests: dict[str, dict[str, str]]) -> None:
    schema = json.loads((ROOT / "scenarios" / "schema.json").read_text(encoding="utf-8"))
    validator = Draft202012Validator(schema)
    keys, ids = set(), {}
    claimed: dict[str, str] = {}

    files = sorted((ROOT / "scenarios" / "cases").glob("*.json"))
    if not files:
        err("scenarios/cases", "no scenario files found")
    for f in files:
        try:
            data = json.loads(f.read_text(encoding="utf-8"))
        except json.JSONDecodeError as e:
            err(f, f"invalid JSON: {e}")
            continue
        for e in sorted(validator.iter_errors(data), key=lambda e: list(e.path)):
            err(f, f"schema: {'/'.join(map(str, e.path)) or '<root>'}: {e.message}")
        key = data.get("key")
        if key != f.stem:
            err(f, f"key '{key}' must equal file name '{f.stem}'")
        if key in keys:
            err(f, f"duplicate key '{key}'")
        keys.add(key)

        qid = data.get("qaseId")
        if qid is not None:
            if qid in ids:
                err(f, f"qaseId {qid} also used by {ids[qid]}")
            ids[qid] = f.name

        test = (data.get("automation") or {}).get("test")
        if not test:
            continue
        cls, _, method = test.partition("#")
        if cls not in tests or method not in tests[cls]:
            err(f, f"automation.test '{test}' does not exist")
            continue
        if test in claimed:
            err(f, f"test '{test}' is also claimed by {claimed[test]} (one case per test)")
        claimed[test] = f.name
        found = QASE_ID.findall(tests[cls][method])
        if qid is None and found:
            err(f, f"qaseId is null but {test} has @QaseId({found[0]})")
        elif qid is not None and found != [str(qid)]:
            err(f, f"qaseId {qid} but {test} has {['@QaseId(' + x + ')' for x in found] or 'no @QaseId'}")

    # every @QaseId in code must be backed by a scenario
    for cls, methods in tests.items():
        for method, block in methods.items():
            for qid in QASE_ID.findall(block):
                if int(qid) not in ids:
                    err(f"{cls}#{method}", f"@QaseId({qid}) has no scenario file")


# ---------------------------------------------------------------- claude config
def frontmatter(path: Path) -> dict | None:
    text = path.read_text(encoding="utf-8")
    if not text.startswith("---\n"):
        err(path, "missing YAML frontmatter")
        return None
    end = text.find("\n---", 4)
    if end < 0:
        err(path, "unterminated YAML frontmatter")
        return None
    try:
        data = yaml.safe_load(text[4:end]) or {}
    except yaml.YAMLError as e:
        err(path, f"invalid YAML frontmatter: {e}")
        return None
    return data


LINK = re.compile(r"\]\(([^)#\s]+)(?:#[^)]*)?\)")


def check_links(path: Path) -> None:
    for target in LINK.findall(path.read_text(encoding="utf-8")):
        if re.match(r"^[a-z]+:", target):
            continue
        if not (path.parent / target).resolve().exists():
            err(path, f"broken link: {target}")


def check_skills() -> None:
    skills = sorted((ROOT / ".claude" / "skills").glob("*/SKILL.md"))
    if not skills:
        err(".claude/skills", "no skills found")
    for f in skills:
        fm = frontmatter(f)
        if fm is None:
            continue
        name = f.parent.name
        if not re.fullmatch(r"[a-z0-9]+(-[a-z0-9]+)*", name):
            err(f, "folder name must be lowercase-hyphenated")
        if fm.get("name") != name:
            err(f, f"frontmatter name '{fm.get('name')}' must equal folder '{name}'")
        desc = fm.get("description")
        if not isinstance(desc, str) or not (20 <= len(desc) <= 1024):
            err(f, "description must be 20-1024 characters")
        check_links(f)
        for extra in f.parent.glob("*.md"):
            if extra.name != "SKILL.md":
                check_links(extra)


def check_agents() -> None:
    for f in sorted((ROOT / ".claude" / "agents").glob("*.md")):
        fm = frontmatter(f)
        if fm is None:
            continue
        if fm.get("name") != f.stem:
            err(f, f"name '{fm.get('name')}' must equal file name '{f.stem}'")
        for field in ("description", "tools"):
            if not fm.get(field):
                err(f, f"'{field}' is required")


SECRET_REF = re.compile(r"^\$\{[A-Z0-9_]+(:-[^}]*)?\}$")


def check_mcp() -> None:
    f = ROOT / ".mcp.json"
    try:
        servers = json.loads(f.read_text(encoding="utf-8"))["mcpServers"]
    except (OSError, KeyError, json.JSONDecodeError) as e:
        err(f, f"cannot read mcpServers: {e}")
        return
    for name, cfg in servers.items():
        for key, value in (cfg.get("env") or {}).items():
            if any(w in key.upper() for w in ("TOKEN", "KEY", "SECRET", "PASSWORD")) and not SECRET_REF.match(str(value)):
                err(f, f"server '{name}': env {key} must reference a variable like ${{{key}}}, not a literal")


def main() -> int:
    tests = index_tests()
    check_scenarios(tests)
    check_skills()
    check_agents()
    check_mcp()
    if errors:
        print(f"AI config validation failed ({len(errors)} problem(s)):")
        for e in errors:
            print(f"  - {e}")
        return 1
    n = len(list((ROOT / "scenarios" / "cases").glob("*.json")))
    print(f"OK: {n} scenarios, {sum(len(m) for m in tests.values())} test methods, "
          f"{len(list((ROOT / '.claude' / 'skills').glob('*/SKILL.md')))} skills, "
          f"{len(list((ROOT / '.claude' / 'agents').glob('*.md')))} agents")
    return 0


if __name__ == "__main__":
    sys.exit(main())
