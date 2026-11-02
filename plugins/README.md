# CAS.AI agent skills

Skills that teach an AI coding agent to integrate the [CAS.AI](https://cas.ai) ad
mediation SDK into an app, the way the documentation in this repository says it should be
done: with the right questions asked first, with test ads, and with a verification step at
the end.

They live next to the documentation on purpose. The one way this material fails is by
drifting from the pages it was written from, and the only thing that reliably prevents
that is changing both in the same pull request.

Today: **Android** and **iOS**. The layout has room for the other platforms.

## Use it

### Claude Code

```
/plugin marketplace add cleveradssolutions/docs
/plugin install cas-sdk@cas-skills
```

The marketplace lives at the root of this repository, so the command names the docs
repository and the plugin inside it. Then ask for what you want, for example *"add CAS.AI
banner and interstitial ads to this app"*. The skill loads itself when the task matches.

### claude.ai

Upload the folder of a single skill, `cas-sdk/skills/cas-android` or
`cas-sdk/skills/cas-ios`, as a skill. Each folder is self contained.

### Any other agent

`SKILL.md` is plain Markdown with a small YAML header, and every file it links to sits
next to it. Point your agent at the raw files, or copy a skill folder into your repository
next to your own agent instructions. Nothing here is Claude specific except the two plugin
manifests. See [../AGENTS.md](../AGENTS.md).

## What is inside

```
.claude-plugin/marketplace.json          the marketplace entry, at the repository root
plugins/
├── cas-sdk/
│   ├── .claude-plugin/plugin.json       the plugin manifest
│   └── skills/
│       ├── cas-android/SKILL.md         the procedure, plus references/
│       └── cas-ios/SKILL.md             the procedure, plus references/
├── shared/                              texts both skills use
└── README.md                            this file
scripts/sync-shared.sh                   copies shared/ into every references/
scripts/check.sh                         validates all of the above
```

A skill is loaded as one folder, and an agent cannot follow a path out of it, so the
shared texts are copied in rather than linked. `references/agent-rules.md`,
`references/integration-interview.md` and `references/docs-map.md` are **generated**: edit
them in `plugins/shared/` and run the script.

```sh
scripts/sync-shared.sh           # copy
scripts/sync-shared.sh --check   # fail if a copy drifted
scripts/check.sh                 # manifests, skill headers, links, shared copies
```

Nothing here touches `docs/` or `docs.json`. docs.page reads those two and ignores the
rest of the repository, the same way it already ignores `README.md` and `cache_images.py`.

## The other guides

`llm-guides/` in this repository holds agent guides for the rest of the surface, in the
same question format: `cas-unity.md`, `cas-publishing-hub.md` and `firebase-ga4-setup.md`.
A Unity project belongs there, not here.

## Keeping it true

Every reference file **links** the documentation page it was written from, by a path into
`docs/` in this repository. That is deliberate: the agent clones the repository, so it
reads the real page instead of a copy that goes stale, and `scripts/check.sh` fails when a
page is renamed or moved. When a page in `docs/` changes, check the reference that links
it in the same pull request. When the two disagree, the documentation is right and the
skill is the bug.

The documentation is also available to agents directly, as
[llms.txt](https://docs.page/cleveradssolutions/docs/llms.txt) and `llms-full.txt`. Those
answer "where is it written". The skills answer "in what order, what to ask first and what
never to do", which is not documentation and does not belong in `docs/`.

## Contributing

Changes to a rule or a question go into `plugins/shared/`, then `scripts/sync-shared.sh`.
Changes to a platform go into that skill's `references/`. Keep `SKILL.md` short: it is
loaded in full every time the skill triggers, while `references/` is read only when
needed.
