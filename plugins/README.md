# CAS.AI agent skills

Skills that teach an AI coding agent to integrate the [CAS.AI](https://cas.ai) ad
mediation SDK into an app, the way the documentation in this repository says it should be
done: with the right questions asked first, with test ads, and with a verification step at
the end.

They live next to the documentation on purpose. The one way this material fails is by
drifting from the pages it was written from, and the only thing that reliably prevents
that is changing both in the same pull request.

Today: **Android**, **iOS**, **Flutter**, **React Native**, **Kotlin Multiplatform**
and **Unreal**. The layout has room for the remaining platforms.

## Use it

### Any agent, through the open skills CLI

```bash
npx skills add cleveradssolutions/docs/plugins/cas-sdk/skills
```

The [`skills`](https://github.com/vercel-labs/skills) CLI detects the agents installed on
your machine and copies the skills into their skills directories. It supports Claude Code,
Codex, Cursor, OpenCode and many others, and needs Node.js only to run the installer.
Install one with `--skill cas-android`, update later with `npx skills update`.

### Claude Code, through the plugin marketplace

```
/plugin marketplace add cleveradssolutions/docs
/plugin install cas-sdk@cas-skills
```

The marketplace manifest is at the root of this repository, so the command names the docs
repository and the plugin inside it.

### claude.ai

Upload the folder of a single skill, for example `cas-sdk/skills/cas-android` or
`cas-sdk/skills/cas-react-native`, as a skill. Each folder is self contained; when it travels alone
it falls back to the published documentation instead of the pages in this repository.

### By hand

`SKILL.md` is plain Markdown with a small YAML header, and every file it links to sits
next to it. Copy a skill folder into your repository beside your own agent instructions,
or point your agent at the raw files. Nothing here is Claude specific except the two
plugin manifests. See [../AGENTS.md](../AGENTS.md).

## The skills

| Skill | What it does | Sample prompt |
| :--- | :--- | :--- |
| `cas-android` | Adds the CAS SDK to a native Android app: Gradle setup, ad networks, initialization, consent, ad formats, verification on test ads. | *Add CAS.AI banner and interstitial ads to this app* |
| `cas-ios` | The same for a native iOS app, with Swift Package Manager or CocoaPods and the `Info.plist` configuration. | *Integrate CAS.AI into my iOS app and show a rewarded ad* |
| `cas-flutter` | A Flutter app: the `clever_ads_solutions` package, the CAS Gradle plugin on Android, the `casconfig.rb` script on iOS, then the Dart ad formats. | *Add CAS.AI ads to my Flutter app* |
| `cas-react-native` | A React Native or Expo app: the `react-native-cas` package, the Expo config plugin or the bare native setup, then the TypeScript ad formats. | *Add CAS.AI ads to my React Native app* |
| `cas-kmp` | A Kotlin Multiplatform app: the `cas-kmp` library in `commonMain`, the CAS Gradle plugin on Android and the CAS Swift Package on iOS, then the shared ad formats and the Compose composables. | *Add CAS.AI ads to my Kotlin Multiplatform app* |
| `cas-unreal` | An Unreal Engine project: the CAS-Unreal plugin, the `CleverAdsSolutions` module, the per-platform CAS.AI Config window and the packaging prerequisites, then the C++ or Blueprint ad formats. | *Add CAS.AI rewarded ads to my Unreal game* |

Each skill detects a project that belongs to another platform and stops, rather than
integrating a native SDK underneath a plugin. Unity has a guide instead of a skill,
`llm-guides/cas-unity.md`, and so will Construct.

## Before you let an agent do this

Read what the agent changes. Skills make the result more accurate; they do not make an
agent infallible. Two things in particular are worth checking yourself: that **test ad
mode is bound to the debug build** and nothing can ship with it on or off by accident,
and that the **CAS ID** is the one from your dashboard.

While following a skill, the agent reads this repository, your project, and may fetch the
published documentation and the sample apps on GitHub.

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
