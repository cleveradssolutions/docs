---
name: cas-react-native
description: >-
  Integrates, upgrades or debugs the CAS.AI (Clever Ads Solutions) ad mediation
  SDK in a React Native or Expo app: the react-native-cas package, the Expo
  config plugin or the bare native setup with the CAS Gradle plugin and the iOS
  build phase script, ad network choice, initialization, consent flow, banner,
  interstitial, rewarded, app open and native ads, and verification on test ads.
  Use when the task mentions CAS.AI, cleveradssolutions, react-native-cas, the
  CAS-ReactNative plugin, or asks to add ad monetization or migrate from AppLovin
  MAX, LevelPlay or AdMob mediation in a React Native project. Don't use for
  native Android or iOS apps, which are the cas-android and cas-ios skills, or
  for Flutter, Unity, Unreal, Cordova, Construct or Kotlin Multiplatform apps,
  which have their own CAS plugin and their own guide.
license: Apache-2.0
metadata:
  version: "0.1.0"
  category: CAS.AI
---
# CAS.AI on React Native

CAS.AI is an ad mediation SDK: one integration, many ad networks bidding for each
impression. It ships no inventory of its own, so an integration without ad networks shows
nothing.

The plugin is TypeScript over the two native SDKs. **Which native setup you do depends on
one question asked first: Expo with a config plugin, or a bare project you configure by
hand.** Getting that wrong is the single most expensive mistake here, because the two
paths touch different files and the wrong one quietly does nothing.

Work through the phases in order. Each one ends with something the publisher can check.

## Before anything else

Five rules decide whether this integration is safe. The rest are in
[references/agent-rules.md](references/agent-rules.md), read it before writing code.

1. **Test ads, both ways.** Never test on live ads: a click during development is
   invalid traffic and can get the publisher's account flagged. Never release with test
   ads on: they earn nothing. So bind test mode to the build — `__DEV__` — and do not ask
   anyone to remember to turn it off.
2. **Never invent an identifier.** The CAS ID, the AdMob App ID and test device ids come
   from the CAS dashboard or from the publisher. An id copied out of a sample is not a
   placeholder, it is someone else's account.
3. **Two platforms, two CAS IDs.** Android uses the application id; iOS uses the **App
   Store id**, the number from `apps.apple.com/.../id123456789`. One id for both is wrong
   unless both are `demo`. `Platform.select({ ios: ..., android: ... })` is the switch.
4. **CAS initializes the ad networks.** Do not call AdMob, AppLovin, Unity Ads or any
   other network initializer, do not add their npm packages, and do not add their pods or
   Gradle dependencies by hand.
5. **Verified means run.** The integration is done when the app builds on both platforms
   that are being shipped, the SDK initializes, a test ad loads and shows, and the console
   is clean. Not before. A Metro reload is not a build.

## Phase 0 - is this the right skill

`react-native` in `package.json` is the signal. Then establish which of the two paths this
project is on, because everything native follows from it:

| Found | Path |
|---|---|
| `expo` in `package.json`, an `app.json` or `app.config.*`, no `android/` and `ios/` in the tree | **Expo, managed.** The config plugin does the native work at `npx expo prebuild`. |
| `expo` present **and** `android/`, `ios/` committed | Expo with prebuilt native projects. The config plugin still owns them; editing them by hand is overwritten on the next prebuild. Say so. |
| No `expo`, with `android/` and `ios/` in the tree | **Bare React Native.** You configure the native projects yourself. |

And projects that are not this skill's at all:

| Found | Belongs to |
|---|---|
| `pubspec.yaml` | the cas-flutter skill |
| No `react-native`, an Android or Xcode project only | the cas-android or cas-ios skill |
| `ProjectSettings/ProjectVersion.txt` | the CAS Unity plugin, guide [llm-guides/cas-unity.md](../../../../llm-guides/cas-unity.md) |
| A Kotlin Multiplatform `shared` module | the cas-kmp skill |
| `config.xml` with a Cordova plugin list | the CAS Cordova plugin |
| An Unreal `.uproject` | the cas-unreal skill |

Say which one and stop when it is not this skill.

## Phase 1 and 2 - research, then ask

[references/questions.md](references/questions.md) holds the read-only research table and
the questions for this platform. The shape of a question is in
[references/question-format.md](references/question-format.md), the same in every CAS
guide: research first, show a short **Found** block, then ask in rounds of at most three
questions with lettered options and a recommended answer.

Do not ask what the project answers. Do not ask who will turn test mode off; that is a
rule, not a choice.

The answers that block everything else are Expo or bare, the two CAS IDs, which platforms
are shipped, whether this is a migration, the audience, and the ad formats with their
screens.

## Phase 3 - plan

Write the plan before touching files: the package version, whether the native work is a
config plugin entry or hand edits, what lands in `android/app/build.gradle` and which Maven
repositories come with it, how the iOS CAS ID is supplied, where `initialize` goes relative
to the root component, which formats land where, and what the publisher has to do outside
the code. Get agreement, then apply.

## Phase 4 - apply

In this order, because each step needs the previous one:

1. **The package and the native sides** -
   [references/setup-plugin.md](references/setup-plugin.md)
2. **Initialization and consent** - [references/initialize.md](references/initialize.md)
   and [references/consent-privacy.md](references/consent-privacy.md)
3. **Ad formats** - [references/ad-formats.md](references/ad-formats.md)

Keep the project building between steps, and build **both** platforms the publisher ships.
A change to `android/app/build.gradle` or to the Expo config cannot be verified by a Metro
reload. If a step cannot be finished, stop there, leave the project building and say what
is missing.

## Phase 5 - verify

Follow [references/verify-and-release.md](references/verify-and-release.md). The short
version: build and run on each platform, and read the native console. The integration
counts as verified when `initialize` reports no error, a test ad loads and shows for every
format that was added, and the console holds no integration errors. Report the exact log
lines.

When an ad fails, look the error code up in
[references/troubleshooting.md](references/troubleshooting.md) before guessing a cause.

## Phase 6 - hand over

End with a short checklist for the publisher: what changed, what has to be turned off
before release, what belongs in the Google Play and App Store privacy answers, and anything
still waiting on the dashboard or the account manager.

## Documentation and samples

**The documentation is in this repository, under `docs/`.** Read the page from the working
tree rather than fetching the website: it is the same source, on the same branch, and it
costs no network call. Every reference file links the page it was written from, and the
map of all of them is in [references/docs-map.md](references/docs-map.md). When this skill
and the documentation disagree, the documentation is right.

A reference gives the shape of the integration and the constraints around it; the page
gives the full, current code. Read both before writing, and do not copy code out of a
reference when the page is one link away.

The plugin's own example app in `https://github.com/cleveradssolutions/CAS-ReactNative` is
the shortest correct answer for anything about API shape, and the package is
`https://www.npmjs.com/package/react-native-cas`.

This is not the only guide for agents here. `llm-guides/` holds the same kind of material
for the rest of the surface: a Unity project belongs to `cas-unity.md`, the CAS Publishing
program to `cas-publishing-hub.md`, and the studio's Firebase setup to
`firebase-ga4-setup.md`. They use the same question format as this skill.
