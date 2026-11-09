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
| Project setup | `docs/android/index.mdx` | `docs/ios/index.mdx` |
| Manual setup | `docs/android/manual-setup.mdx` | `docs/ios/manually-configure-project.mdx` |
| Initialization | `docs/android/initialize-cas.mdx` | `docs/ios/initialize-cas.mdx` |
| Test ads | `docs/android/enabling-test-ads.mdx` | `docs/ios/enabling-test-ads.mdx` |
| Consent flow | `docs/android/user-consent-flow.mdx` | `docs/ios/user-consent-flow.mdx` |
| Banner | `docs/android/banner-ads.mdx` | `docs/ios/banner-ads.mdx` |
| Interstitial | `docs/android/interstitial-ads.mdx` | `docs/ios/interstitial-ads.mdx` |
| Rewarded | `docs/android/rewarded-ads.mdx` | `docs/ios/rewarded-ads.mdx` |
| App open | `docs/android/app-open-ads.mdx` | `docs/ios/app-open-ads.mdx` |
| Native, load | `docs/android/load-native-ads.mdx` | `docs/ios/load-native-ads.mdx` |
| Native, display | `docs/android/display-native-ad.mdx` | `docs/ios/display-native-ad.mdx` |
| Native assets | `docs/android/native-ad-assets.mdx` | `docs/ios/native-ad-assets.mdx` |
| Ad networks | `docs/android/mediated-networks.mdx` | `docs/ios/mediated-networks.mdx` |
| Targeting | `docs/android/targeting-options.mdx` | `docs/ios/targeting-options.mdx` |
| Impression level data | `docs/android/impression-level-data.mdx` | `docs/ios/impression-level-data.mdx` |
| Error codes | `docs/android/ad-errors.mdx` | `docs/ios/ad-errors.mdx` |
| Purchases | `docs/android/purchase-info.mdx` | `docs/ios/purchase-info.mdx` |
| Meta Audience Network | `docs/android/additional-meta-audiencenetwork-steps.mdx` | `docs/ios/additional-meta-audiencenetwork-steps.mdx` |
| Families policy | `docs/android/google-plays-families-policy.mdx` | `docs/general/apple-kids-category.mdx` |
| Store disclosures | `docs/general/google-play-data-disclosure.mdx` | `docs/general/appstore-data-disclosure.mdx` |
| Apple ad attribution | — | `docs/general/apple-ad-attribution.mdx` |
| Handing work to an agent | `docs/general/ai-agent-integration.mdx` | same page |

The pages are MDX. Components such as `<Info>`, `<Warning>`, `<Error>`, `<Tabs>` and
`<CodeGroup>` wrap the text; read through them. A `{{ versions.android }}` style value is
a variable resolved from `docs.json`, where the current version numbers live.

## Pages, plugin platforms

Each plugin platform has its own section under `docs/`, and the page names repeat: `index.mdx`
for setup, then `initialize-cas.mdx`, `enabling-test-ads.mdx`, `user-consent-flow.mdx`,
`banner-ads.mdx`, `interstitial-ads.mdx`, `rewarded-ads.mdx`, `app-open-ads.mdx`,
`targeting-options.mdx`, `impression-level-data.mdx`, `ad-errors.mdx` and
`additional-meta-audiencenetwork-steps.mdx`. So the path is
`docs/<platform>/<page>.mdx` with `<platform>` one of `flutter`, `reactnative`, `kmp`,
`cordova`, `unreal`, `construct`, `unity`.

**Every path under `docs/` is lowercase**, directory and file name alike, and so is every
published URL. A capitalised path is from before that change and does not resolve.

What each one has on top of that list, or lacks, is where the platforms actually differ:

| Platform | Also has | Does not have |
|---|---|---|
| `flutter` | `native-ads.mdx`, `native-ads-templates.mdx`, `native-ads-android-setup.mdx`, `native-ads-ios-setup.mdx`, `purchase-info.mdx` | — |
| `reactnative` | `native-ads.mdx`, `native-ads-templates.mdx`, `native-ad-manual.mdx` | `purchase-info.mdx` |
| `kmp` | `native-ads.mdx`, `native-ads-templates.mdx`, `native-ads-layout.mdx` | `purchase-info.mdx` |
| `cordova` | `mrec-ads.mdx` | native ads, `purchase-info.mdx` |
| `unreal` | `configuring-sdk.mdx`, `include-android.mdx`, `include-ios.mdx`, `mrec-ads.mdx`, `verify-integration.mdx`, `privacy-regulations.mdx`, `faq.mdx`, `google-plays-families-policy.mdx`, `building-unreal-engine-4-with-xcode-14.mdx` | native ads, `purchase-info.mdx`, `ad-errors.mdx` |
| `construct` | `build.mdx`, `development-options.mdx`, `automation-options.mdx`, `mrec-ads.mdx` | native ads, `targeting-options.mdx`, `initialize-cas.mdx`, `enabling-test-ads.mdx`, `ad-errors.mdx`, `impression-level-data.mdx` |

A plugin platform still rests on the two native SDKs, so the pages under `docs/android/` and
`docs/ios/` stay relevant for anything the plugin does not wrap: the ad network tables, the
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
`https://cleveradssolutions.docs.page/docs/<platform>/<page>`, all lowercase, with
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
