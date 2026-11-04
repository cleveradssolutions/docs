# Where the canonical answer lives

The documentation is in this repository, under `docs/`. An agent that installed the skill
cloned it, so **read the page from the working tree**, not from the web: it is the exact
source of the published page, it is the version this branch is on, and it costs no network
call.

The paths below are relative to the repository root. From a reference file inside a skill,
that is five levels up: `../../../../../docs/...`.

This repository never becomes the source of truth. Every reference file links the page it
was written from. When the two disagree, the documentation wins and the reference is wrong
and has to be fixed, in the same pull request.

## Pages

| Topic | Android | iOS |
|---|---|---|
| Project setup | `docs/Android/index.mdx` | `docs/iOS/index.mdx` |
| Manual setup | `docs/Android/Manual-setup.mdx` | `docs/iOS/Manually-configure-project.mdx` |
| Initialization | `docs/Android/Initialize-CAS.mdx` | `docs/iOS/Initialize-CAS.mdx` |
| Test ads | `docs/Android/Enabling-test-ads.mdx` | `docs/iOS/Enabling-test-ads.mdx` |
| Consent flow | `docs/Android/User-Consent-Flow.mdx` | `docs/iOS/User-Consent-Flow.mdx` |
| Banner | `docs/Android/Banner-Ads.mdx` | `docs/iOS/Banner-Ads.mdx` |
| Interstitial | `docs/Android/Interstitial-Ads.mdx` | `docs/iOS/Interstitial-Ads.mdx` |
| Rewarded | `docs/Android/Rewarded-Ads.mdx` | `docs/iOS/Rewarded-Ads.mdx` |
| App open | `docs/Android/App-Open-Ads.mdx` | `docs/iOS/App-Open-Ads.mdx` |
| Native, load | `docs/Android/Load-Native-Ads.mdx` | `docs/iOS/Load-Native-Ads.mdx` |
| Native, display | `docs/Android/Display-Native-Ad.mdx` | `docs/iOS/Display-Native-Ad.mdx` |
| Native assets | `docs/Android/Native-Ad-assets.mdx` | `docs/iOS/Native-Ad-assets.mdx` |
| Ad networks | `docs/Android/Mediated-Networks.mdx` | `docs/iOS/Mediated-Networks.mdx` |
| Targeting | `docs/Android/Targeting-options.mdx` | `docs/iOS/Targeting-options.mdx` |
| Impression level data | `docs/Android/Impression-Level-Data.mdx` | `docs/iOS/Impression-Level-Data.mdx` |
| Error codes | `docs/Android/Ad-errors.mdx` | `docs/iOS/Ad-errors.mdx` |
| Purchases | `docs/Android/Purchase-Info.mdx` | `docs/iOS/Purchase-Info.mdx` |
| Meta Audience Network | `docs/Android/Additional-Meta-AudienceNetwork-steps.mdx` | `docs/iOS/Additional-Meta-AudienceNetwork-steps.mdx` |
| Families policy | `docs/Android/Google-Plays-Families-Policy.mdx` | `docs/general/Apple-Kids-Category.mdx` |
| Store disclosures | `docs/general/Google-Play-data-disclosure.mdx` | `docs/general/AppStore-data-disclosure.mdx` |
| Apple ad attribution | — | `docs/general/Apple-Ad-Attribution.mdx` |
| Handing work to an agent | `docs/general/AI-Agent-Integration.mdx` | same page |

The pages are MDX. Components such as `<Info>`, `<Warning>`, `<Error>`, `<Tabs>` and
`<CodeGroup>` wrap the text; read through them. A `{{ versions.android }}` style value is
a variable resolved from `docs.json`, where the current version numbers live.

## Other guides for agents in this repository

`llm-guides/` holds the guides for the rest of the surface, in the same question format as
this skill. Paths from the repository root, like the pages above:
`llm-guides/cas-unity.md`, `llm-guides/cas-publishing-hub.md` and
`llm-guides/firebase-ga4-setup.md`. A Unity project belongs to `cas-unity.md`, not to
these skills.

This file is copied into each skill, so it gives paths rather than links: a link would be
right in one copy and wrong in the other. The per-topic reference files sit at one known
depth and do link their pages.

## When the working tree is not there

A skill folder copied on its own, for example uploaded to claude.ai, has no `docs/`. Then
use the published pages, same names:
`https://cleveradssolutions.docs.page/docs/<Platform>/<Page>`, with
`llms.txt` as the index and `llms-full.txt` as everything in one file, which is large.

## Sample apps

Android, `https://github.com/cleveradssolutions/CAS-Android`: `kotlinSample/`,
`javaSample/`, `composeSample/`, and `Adapters/README.md` for adapter versions.

iOS, `https://github.com/cleveradssolutions/CAS-iOS`: `DemoApp Swift/`,
`DemoApp Objective-C/`, `DemoApp SwiftUI/`, `Adapters/README.md`, and `Script XCodeConfig/`
for the `Info.plist` configuration script.

## Versions

Never copy a version number out of this repository's skill files. The current values are in
`docs.json` under `variables`, and the pages render them.
