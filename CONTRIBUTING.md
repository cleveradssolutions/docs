# Contributing

## Documentation

Pages live in `docs/` as MDX and are published by [docs.page](https://docs.page). The
navigation is `docs.json`; a new page has to be added there to appear in the sidebar and
in [llms.txt](https://docs.page/cleveradssolutions/docs/llms.txt).

Preview a branch before merging:

```
https://docs.page/cleveradssolutions/docs~<branch>
```

## Agent skills

`plugins/` holds the agent skills, see [plugins/README.md](plugins/README.md). They are
kept in this repository for one reason: so that a page and the skill that quotes it change
together.

**When you change a page in `docs/`, check whether a skill reference quotes it, and update
it in the same pull request.** Every file under `plugins/cas-sdk/skills/*/references/`
names the page it was written from at the top, so `grep` for the page name finds it. When
the two disagree, the documentation is right and the skill is the bug.

Shared texts live in `plugins/shared/` and are copied into each skill; edit them there and
run the script:

```sh
scripts/sync-shared.sh           # copy
scripts/check.sh                 # manifests, skill headers, links, shared copies
```

`scripts/check.sh` also runs in CI.
