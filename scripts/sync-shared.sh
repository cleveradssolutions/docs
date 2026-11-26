#!/usr/bin/env bash
# Copies plugins/shared/*.md into the references/ folder of every cas-sdk skill.
#
# A skill has to be self contained: Claude Code, claude.ai and any other agent load one
# skill folder and cannot follow a path out of it. So the shared texts live once in
# plugins/shared/ and are copied in, and this script is the only thing allowed to write
# those copies.
#
#   scripts/sync-shared.sh           copy
#   scripts/sync-shared.sh --check   fail if a copy is out of date, for CI
set -euo pipefail

root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
mode="${1:-copy}"
status=0

shopt -s nullglob
# Only the cas-sdk skills: the shared texts are the rules for integrating the SDK into a
# publisher's app, and say nothing about the internal work the cas-mediation skills cover.
for skill in "$root"/plugins/cas-sdk/skills/*/; do
    refs="$skill/references"
    mkdir -p "$refs"
    for file in "$root"/plugins/shared/*.md; do
        target="$refs/$(basename "$file")"
        if [ "$mode" = "--check" ]; then
            if ! cmp -s "$file" "$target"; then
                echo "out of date: ${target#"$root"/}"
                status=1
            fi
        else
            cp "$file" "$target"
            echo "wrote ${target#"$root"/}"
        fi
    done
done

if [ "$mode" = "--check" ] && [ "$status" -eq 0 ]; then
    echo "shared copies are up to date"
fi
exit "$status"
