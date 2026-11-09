#!/usr/bin/env bash
# Validates the repository: manifests parse, every skill has a usable header, every
# relative link resolves, and the shared copies are in sync.
set -uo pipefail

root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
status=0
fail() { echo "FAIL  $*"; status=1; }
pass() { echo "ok    $*"; }

# 1. manifests
for manifest in "$root"/.claude-plugin/marketplace.json "$root"/plugins/*/.claude-plugin/plugin.json; do
    if python3 -c "import json,sys; json.load(open(sys.argv[1]))" "$manifest" 2>/dev/null; then
        pass "json ${manifest#"$root"/}"
    else
        fail "json ${manifest#"$root"/}"
    fi
done

# 2. every plugin named in the marketplace exists
python3 - "$root" <<'PY' || status=1
import json, os, sys
root = sys.argv[1]
market = json.load(open(os.path.join(root, ".claude-plugin", "marketplace.json")))
ok = True
for plugin in market.get("plugins", []):
    source = plugin["source"].lstrip("./")
    manifest = os.path.join(root, source, ".claude-plugin", "plugin.json")
    if not os.path.isfile(manifest):
        print("FAIL  marketplace names %s, no manifest at %s" % (plugin["name"], source))
        ok = False
        continue
    name = json.load(open(manifest))["name"]
    if name != plugin["name"]:
        print("FAIL  marketplace says %s, manifest says %s" % (plugin["name"], name))
        ok = False
    else:
        print("ok    plugin %s" % name)
sys.exit(0 if ok else 1)
PY

# 3. skill headers and links
python3 - "$root" <<'PY' || status=1
import os, re, sys
root = sys.argv[1]
ok = True
skills = []
for plugin in sorted(os.listdir(os.path.join(root, "plugins"))):
    base = os.path.join(root, "plugins", plugin, "skills")
    if os.path.isdir(base):
        skills += [os.path.join(base, s) for s in sorted(os.listdir(base))]

if not skills:
    print("FAIL  no skills found")
    sys.exit(1)

for skill in skills:
    name = os.path.basename(skill)
    path = os.path.join(skill, "SKILL.md")
    if not os.path.isfile(path):
        print("FAIL  %s has no SKILL.md" % name)
        ok = False
        continue
    text = open(path).read()
    header = re.match(r"^---\n(.*?)\n---\n", text, re.S)
    if not header:
        print("FAIL  %s has no front matter" % name)
        ok = False
        continue
    # top level scalars, including folded and literal blocks (">-", "|")
    fields = {}
    lines = header.group(1).split("\n")
    i = 0
    while i < len(lines):
        match = re.match(r"^([a-z][a-z-]*):[ \t]*(.*)$", lines[i])
        i += 1
        if not match:
            continue
        key, value = match.group(1), match.group(2).strip()
        if value[:1] in (">", "|"):
            block = []
            while i < len(lines) and (lines[i].startswith((" ", "\t")) or not lines[i].strip()):
                block.append(lines[i].strip())
                i += 1
            value = " ".join(part for part in block if part)
        fields[key] = value
    if fields.get("name") != name:
        print("FAIL  %s front matter name is %r" % (name, fields.get("name")))
        ok = False
    description = fields.get("description", "")
    if len(description) < 60:
        print("FAIL  %s description is too short to match on" % name)
        ok = False
    if len(description) > 1024:
        print("FAIL  %s description is too long" % name)
        ok = False
    if ok:
        print("ok    skill %s" % name)

# every relative markdown link resolves, in the agent files only
targets = [os.path.join(root, "plugins"), os.path.join(root, "AGENTS.md")]
walked = []
for target in targets:
    if os.path.isfile(target):
        walked.append((os.path.dirname(target), [os.path.basename(target)]))
    else:
        for dirpath, _, files in os.walk(target):
            walked.append((dirpath, files))

def exists_cased(path):
    # macOS is case-insensitive, so os.path.exists() accepts docs/KMP/Banner-Ads.mdx
    # for docs/kmp/banner-ads.mdx. Every page path is lowercase; check the real casing
    # so a stale capitalised link fails here and not in CI.
    path = os.path.normpath(path)
    if not os.path.exists(path):
        return False
    head, tail = os.path.split(path)
    while tail:
        try:
            if tail not in os.listdir(head or "."):
                return False
        except OSError:
            return False
        head, tail = os.path.split(head)
    return True


for dirpath, files in walked:
    for file in files:
        if not file.endswith(".md"):
            continue
        full = os.path.join(dirpath, file)
        # fenced code blocks are not prose: a C++ lambda [this](const T& x) is not a link
        prose = re.sub(r"(?ms)^```.*?^```", "", open(full).read())
        for target in re.findall(r"\]\((?!https?://|#)([^)]+)\)", prose):
            target = target.split("#")[0]
            if not target:
                continue
            if not exists_cased(os.path.join(dirpath, target)):
                print("FAIL  %s links to missing %s" % (os.path.relpath(full, root), target))
                ok = False

sys.exit(0 if ok else 1)
PY

# 4. shared copies
"$root"/scripts/sync-shared.sh --check >/dev/null 2>&1 \
    && pass "shared copies in sync" \
    || fail "shared copies out of sync, run scripts/sync-shared.sh"

# 5. the docs tree is untouched by any of this
if [ -d "$root/docs" ] && [ -f "$root/docs.json" ]; then
    pass "docs tree present and separate"
fi

exit "$status"
