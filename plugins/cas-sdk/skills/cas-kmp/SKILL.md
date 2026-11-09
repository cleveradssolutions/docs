---
name: cas-kmp
description: >-
  Integrates, upgrades or debugs the CAS.AI (Clever Ads Solutions) ad mediation
  SDK in a Kotlin Multiplatform app: the com.cleveradssolutions:cas-kmp library
  in commonMain, the CAS Gradle plugin on the Android target, the CAS Swift
  Package in the Xcode project, ad network choice per platform, initialization,
  consent flow, and the interstitial, rewarded, app open, banner and native
  formats including the Compose Multiplatform composables, verified on test ads.
  Use when the task mentions CAS.AI, cleveradssolutions, cas-kmp, the CAS-KMP
  plugin, or asks to add ad monetization or migrate from AppLovin MAX, LevelPlay
  or AdMob mediation in a Kotlin Multiplatform or Compose Multiplatform project.
  Don't use for native Android or iOS apps, which are the cas-android and cas-ios
  skills, or for Flutter, React Native, Unity, Unreal, Cordova or Construct apps,
  which have their own CAS plugin and their own guide.
license: Apache-2.0
metadata:
  version: "0.1.0"
  category: CAS.AI
---
# CAS.AI on Kotlin Multiplatform

CAS.AI is an ad mediation SDK: one integration, many ad networks bidding for each
impression. It ships no inventory of its own, so an integration without ad networks shows
nothing.

The plugin is a single `expect` API in `commonMain` over the two native SDKs, and Banner
and Native are Compose Multiplatform composables, so the whole integration can live in
shared code. **The library in `commonMain` is the small half of the work.** The Android
artifact declares the CAS Android SDK as `compileOnly` and the iOS side is a Swift Package,
which means the SDK and every adapter come from the platform setup — and that setup is two
different mechanisms, done twice, kept in step by hand.

Work through the phases in order. Each one ends with something the publisher can check.

## Before anything else

Five rules decide whether this integration is safe. The rest are in
[references/agent-rules.md](references/agent-rules.md), read it before writing code.

1. **Test ads, both ways.** Never test on live ads: a click during development is
   invalid traffic and can get the publisher's account flagged. Never release with test
   ads on: they earn nothing. So bind test mode to the build — `forceTestAds = isDebug`,
   from a value the build sets — and do not ask anyone to remember to turn it off.
2. **Never invent an identifier.** The CAS ID, the AdMob App ID and test device ids come
   from the CAS dashboard or from the publisher. An id copied out of a sample is not a
   placeholder, it is someone else's account.
3. **Two platforms, two CAS IDs.** Android uses the application id; iOS uses the **App
   Store id**, the number from `apps.apple.com/.../id123456789`. One id for both is wrong
   unless both are the demo id. `expect val casId: String` with an `actual` per source set
   is the switch, and the documentation spells it exactly that way.
4. **CAS initializes the ad networks.** Do not call AdMob, AppLovin, Unity Ads or any
   other network initializer, do not add their Gradle dependencies, and do not add their
   pods or Swift packages by hand.
5. **Verified means run.** The integration is done when the app builds and runs on both
   platforms that are being shipped, the SDK initializes, a test ad loads and shows, and
   the console is clean. Not before. Compiling the shared module is not running the app.

## Phase 0 - is this the right skill

`kotlin("multiplatform")` in a Gradle script with a `commonMain` source set is the signal,
usually a `shared/` module next to an `androidApp`/`composeApp` and an `iosApp/`. Then
settle three facts, because the rest of the work depends on them:

| What | Where to look | Why it decides things |
|---|---|---|
| Which targets exist | the `kotlin { }` block | CAS supports `iosArm64` and `iosSimulatorArm64` only. A `jvm`, `js`, `wasmJs` or desktop target takes no ads, and the shared ad code has to compile without CAS there. |
| Is the UI Compose Multiplatform | a `composeApp`, `androidx.compose` in `commonMain`, a `MainViewController` wrapping `ComposeUIViewController` | Banner and Native are **composables**. A project with platform-native UI — SwiftUI or UIKit on iOS, Views on Android — cannot place those two from shared code. |
| Which module holds the Android target | the `plugins` block of each module | The CAS Gradle plugin is applied there, after the Android plugin, and nowhere else. |

And projects that are not this skill's at all:

| Found | Belongs to |
|---|---|
| No `kotlin("multiplatform")`, an Android project only | the cas-android skill |
| No shared module, an Xcode project only | the cas-ios skill |
| `pubspec.yaml` | the cas-flutter skill |
| `react-native` in `package.json` | the cas-react-native skill |
| `ProjectSettings/ProjectVersion.txt` | the CAS Unity plugin, guide [llm-guides/cas-unity.md](../../../../llm-guides/cas-unity.md) |
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

The answers that block everything else are the two CAS IDs, which platforms are shipped,
whether the UI is Compose Multiplatform, whether this is a migration, the audience, and
the ad formats with their screens.

## Phase 3 - plan

Write the plan before touching files: the three version numbers and how they are kept in
step, which module gets the CAS Gradle plugin and the `cas { }` block, where the Maven
repositories go, how the Swift Package and its products are added, how the Kotlin framework
reaches Xcode, how `casconfig.rb` is run, where the `expect`/`actual` CAS ID lives, where
`initialize` is called from on each platform, which formats land where, and what the
publisher has to do outside the code. Get agreement, then apply.

## Phase 4 - apply

In this order, because each step needs the previous one:

1. **The library and the two platform setups** -
   [references/setup-plugin.md](references/setup-plugin.md)
2. **Initialization and consent** - [references/initialize.md](references/initialize.md)
   and [references/consent-privacy.md](references/consent-privacy.md)
3. **Ad formats** - [references/ad-formats.md](references/ad-formats.md)

Keep the project building between steps, and build **both** platforms the publisher ships.
A Gradle sync is not a build, and `:shared:compileKotlinIosArm64` says nothing about
whether the Xcode project links. If a step cannot be finished, stop there, leave the
project building and say what is missing.

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

The plugin's own demo app, `cas-demo-app/src/commonMain/kotlin/.../screens/` in
`https://github.com/cleveradssolutions/CAS-KMP`, is the shortest correct answer for
anything about API shape, and every format page links the screen for that format.

This is not the only guide for agents here. `llm-guides/` holds the same kind of material
for the rest of the surface: a Unity project belongs to `cas-unity.md`, the CAS Publishing
program to `cas-publishing-hub.md`, and the studio's Firebase setup to
`firebase-ga4-setup.md`. They use the same question format as this skill.
