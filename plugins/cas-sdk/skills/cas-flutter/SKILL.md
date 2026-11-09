---
name: cas-flutter
description: >-
  Integrates, upgrades or debugs the CAS.AI (Clever Ads Solutions) ad mediation
  SDK in a Flutter app: the clever_ads_solutions package, the CAS Gradle plugin on
  the Android side and the casconfig.rb script on the iOS side, ad network choice,
  initialization, consent flow, banner, interstitial, rewarded, app open and
  native ads, and verification on test ads. Use when the task mentions CAS.AI,
  cleveradssolutions, clever_ads_solutions, the CAS-Flutter plugin, or asks to add
  ad monetization or migrate from AppLovin MAX, LevelPlay or AdMob mediation in a
  Flutter project. Don't use for native Android or iOS apps, which are the
  cas-android and cas-ios skills, or for React Native, Unity, Unreal, Cordova,
  Construct or Kotlin Multiplatform apps, which have their own CAS plugin and
  their own guide.
license: Apache-2.0
metadata:
  version: "0.1.0"
  category: CAS.AI
---
# CAS.AI on Flutter

CAS.AI is an ad mediation SDK: one integration, many ad networks bidding for each
impression. It ships no inventory of its own, so an integration without ad networks shows
nothing.

The Flutter plugin is Dart over the two native SDKs. Most of the work is Dart, but **the
parts that decide whether anything serves are native**: the CAS Gradle plugin on Android
and the `casconfig.rb` script on iOS. A Dart-only integration builds, initializes and
never shows an ad.

Work through the phases in order. Each one ends with something the publisher can check.

## Before anything else

Four rules decide whether this integration is safe. The rest are in
[references/agent-rules.md](references/agent-rules.md), read it before writing code.

1. **Test ads, both ways.** Never test on live ads: a click during development is
   invalid traffic and can get the publisher's account flagged. Never release with test
   ads on: they earn nothing. So bind test mode to the build —
   `forceTestAds: kDebugMode || kProfileMode` — and do not ask anyone to remember to turn
   it off.
2. **Never invent an identifier.** The CAS ID, the AdMob App ID and test device ids come
   from the CAS dashboard or from the publisher. An id copied out of a sample is not a
   placeholder, it is someone else's account.
3. **Two platforms, two CAS IDs.** Android uses the application id; iOS uses the **App
   Store id**, the number from `apps.apple.com/.../id123456789`. One `casId` string for
   both is wrong unless both are `demo`. Pick with `Platform.isAndroid`.
4. **CAS initializes the ad networks.** Do not call AdMob, AppLovin, Unity Ads or any
   other network initializer, do not add their Flutter packages, and do not add their pods
   or Gradle dependencies by hand.
5. **Verified means run.** The integration is done when the app builds on both platforms
   that are being shipped, the SDK initializes, a test ad loads and shows, and the console
   is clean. Not before.

## Phase 0 - is this the right skill

`pubspec.yaml` with a `flutter:` section is the signal for this skill. Check what else is
in the project, because a Flutter module inside a bigger app changes who owns the native
side:

| Found | Belongs to |
|---|---|
| No `pubspec.yaml`, an Android or Xcode project only | the cas-android or cas-ios skill |
| `react-native` in `package.json` | the cas-react-native skill |
| `ProjectSettings/ProjectVersion.txt` | the CAS Unity plugin, guide [llm-guides/cas-unity.md](../../../../llm-guides/cas-unity.md) |
| A `.c3p` file or a `project.c3proj` | Construct 3, guide [llm-guides/cas-construct.md](../../../../llm-guides/cas-construct.md) |
| A Kotlin Multiplatform `shared` module | the cas-kmp skill |
| `config.xml` with a Cordova plugin list | the cas-cordova skill |
| An Unreal `.uproject` | the cas-unreal skill |
| A Flutter **module** added to a native host app (`.android/`, `.ios/` generated) | still this skill for the Dart part, but the native host owns Gradle and the Xcode project — say so and agree who edits them |

Say which one and stop when it is not this skill. Integrating a native SDK underneath a
plugin breaks the plugin.

## Phase 1 and 2 - research, then ask

[references/questions.md](references/questions.md) holds the read-only research table and
the questions for this platform. The shape of a question is in
[references/question-format.md](references/question-format.md), the same in every CAS
guide: research first, show a short **Found** block, then ask in rounds of at most three
questions with lettered options and a recommended answer.

Do not ask what the project answers. Do not ask who will turn test mode off; that is a
rule, not a choice.

The answers that block everything else are the two CAS IDs, which platforms are shipped,
whether this is a migration, the audience, and the ad formats with their screens.

## Phase 3 - plan

Write the plan before touching files: the package version, what lands in
`android/app/build.gradle`, whether the iOS side is Swift Package Manager or CocoaPods and
when `casconfig.rb` runs, where `initialize` goes relative to `runApp`, which formats land
on which widgets, and what the publisher has to do outside the code — in the dashboard, in
the stores, in the privacy answers. Get agreement, then apply.

## Phase 4 - apply

In this order, because each step needs the previous one:

0. **Removing the previous mediation**, when this is a migration -
   [references/migration.md](references/migration.md). It comes first: the old SDK goes
   before the package arrives.
1. **The package and the native sides** -
   [references/setup-plugin.md](references/setup-plugin.md)
2. **Initialization and consent** - [references/initialize.md](references/initialize.md)
   and [references/consent-privacy.md](references/consent-privacy.md)
3. **Ad formats** - [references/ad-formats.md](references/ad-formats.md)

Keep the project building between steps, and build **both** platforms the publisher ships:
a change to `android/app/build.gradle` cannot be verified by `flutter run` on a simulator.
If a step cannot be finished, stop there, leave the project building and say what is
missing.

## Phase 5 - verify

Follow [references/verify-and-release.md](references/verify-and-release.md). The short
version: build and run on each platform, and read the console. The integration counts as
verified when `initialize` reports no error, a test ad loads and shows for every format
that was added, and the console holds no integration errors. Report the exact log lines.

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

The plugin's own example app, `example/` in
`https://github.com/cleveradssolutions/CAS-Flutter`, is the shortest correct answer for
anything about Dart API shape. The API reference is on
`https://pub.dev/documentation/clever_ads_solutions/latest/`.

This is not the only guide for agents here. `llm-guides/` holds the same kind of material
for the rest of the surface: a Unity project belongs to `cas-unity.md`, the CAS Publishing
program to `cas-publishing-hub.md`, and the studio's Firebase setup to
`firebase-ga4-setup.md`. They use the same question format as this skill.
