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

## Pages, native platforms

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

## Pages, plugin platforms

Each plugin platform has its own section under `docs/`, and the page names repeat: `index.mdx`
for setup, then `Initialize-CAS.mdx`, `Enabling-test-ads.mdx`, `User-Consent-Flow.mdx`,
`Banner-Ads.mdx`, `Interstitial-Ads.mdx`, `Rewarded-Ads.mdx`, `App-Open-Ads.mdx`,
`Targeting-options.mdx`, `Impression-Level-Data.mdx`, `Ad-errors.mdx` and
`Additional-Meta-AudienceNetwork-steps.mdx`. So the path is
`docs/<Platform>/<Page>.mdx` with `<Platform>` one of `Flutter`, `ReactNative`, `KMP`,
`Cordova`, `Unreal`, `Construct`, `Unity`.

What each one has on top of that list, or lacks, is where the platforms actually differ:

| Platform | Also has | Does not have |
|---|---|---|
| `Flutter` | `Native-Ads.mdx`, `Native-Ads-templates.mdx`, `Native-Ads-Android-Setup.mdx`, `Native-Ads-iOS-Setup.mdx`, `Purchase-Info.mdx` | — |
| `ReactNative` | `Native-Ads.mdx`, `Native-Ads-Templates.mdx`, `Native-Ad-Manual.mdx` | `Purchase-Info.mdx` |
| `KMP` | `Native-Ads.mdx`, `Native-Ads-templates.mdx`, `Native-Ads-layout.mdx` | `Purchase-Info.mdx` |
| `Cordova` | `MRec-Ads.mdx` | native ads, `Purchase-Info.mdx` |
| `Unreal` | `Configuring-SDK.mdx`, `Include-Android.mdx`, `Include-iOS.mdx`, `MREC-Ads.mdx`, `Verify-Integration.mdx`, `Privacy-Regulations.mdx`, `FAQ.mdx`, `Google-Plays-Families-Policy.mdx`, `Building-Unreal-Engine-4-with-XCode-14.mdx` | native ads, `Purchase-Info.mdx`, `Ad-errors.mdx` |
| `Construct` | `Build.mdx`, `Development-Options.mdx`, `Automation-Options.mdx`, `MRec-Ads.mdx` | native ads, `Targeting-options.mdx`, `Initialize-CAS.mdx`, `Enabling-test-ads.mdx`, `Ad-errors.mdx`, `Impression-Level-Data.mdx` |

A plugin platform still rests on the two native SDKs, so the pages under `docs/Android/` and
`docs/iOS/` stay relevant for anything the plugin does not wrap: the ad network tables, the
store disclosures and the families policy pages have no plugin copy.

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
`https://docs.page/cleveradssolutions/docs/<Platform>/<Page>`, with
`llms.txt` as the index and `llms-full.txt` as everything in one file, which is large.

## Sample apps

Android, `https://github.com/cleveradssolutions/CAS-Android`: `kotlinSample/`,
`javaSample/`, `composeSample/`, and `Adapters/README.md` for adapter versions.

iOS, `https://github.com/cleveradssolutions/CAS-iOS`: `DemoApp Swift/`,
`DemoApp Objective-C/`, `DemoApp SwiftUI/`, `Adapters/README.md`, and `Script XCodeConfig/`
for the `Info.plist` configuration script.

The plugins, under the same organisation, each with an example app:
`CAS-Flutter` (`example/`), `CAS-ReactNative`, `CAS-KMP`, `CAS-Cordova`, `CAS-Unreal`,
`CAS-Construct`, `CAS-Unity`. A plugin's example app is the shortest correct answer for that
platform, the same way the demo apps are for the native SDKs. The repository names are the
ones the pages themselves link.

## Versions

Never copy a version number out of this repository's skill files. The current values are in
`docs.json` under `variables`, and the pages render them.
