---
name: cas-android
description: >-
  Integrates, upgrades or debugs the CAS.AI (Clever Ads Solutions) ad mediation
  SDK in a native Android app: Gradle setup and ad network choice, initialization,
  consent flow, banner, interstitial, rewarded, app open and native ads, and
  verification on test ads. Use when the task mentions CAS.AI, cleveradssolutions,
  the CAS Gradle plugin, or asks to add ad monetization or migrate from AppLovin
  MAX, LevelPlay or AdMob mediation in an Android project. Don't use for Flutter,
  React Native, Unity, Unreal, Cordova or Kotlin Multiplatform apps, which have
  their own CAS plugin and their own guide, or for the iOS side of a project,
  which is the cas-ios skill.
license: Apache-2.0
metadata:
  version: "0.1.0"
  category: CAS.AI
---
# CAS.AI on Android

CAS.AI is an ad mediation SDK: one integration, many ad networks bidding for each
impression. It ships no inventory of its own, so an integration without ad networks shows
nothing.

Work through the phases in order. Each one ends with something the publisher can check.

## Before anything else

Four rules decide whether this integration is safe. The rest are in
[references/agent-rules.md](references/agent-rules.md), read it before writing code.

1. **Test ads, both ways.** Never test on live ads: a click during development is
   invalid traffic and can get the publisher's account flagged. Never release with test
   ads on: they earn nothing. So bind test mode to the build, the debug build flag, and
   do not ask anyone to remember to turn it off.
2. **Never invent an identifier.** The CAS ID, the AdMob App ID and test device ids come
   from the CAS dashboard or from the publisher. An id copied out of a sample is not a
   placeholder, it is someone else's account.
3. **CAS initializes the ad networks.** Do not call AdMob, AppLovin, Unity Ads or any
   other network initializer, and do not add their dependencies by hand.
4. **Verified means run.** The integration is done when the app builds, the SDK
   initializes, a test ad loads and shows, and the log is clean. Not before.

## Phase 0 - is this the right skill

Check the project kind before anything else. A cross-platform project has Android
folders too, and belongs to its own CAS plugin, not here:

| Found | Belongs to |
|---|---|
| `pubspec.yaml` | the cas-flutter skill |
| `react-native` in `package.json` | the cas-react-native skill |
| `ProjectSettings/ProjectVersion.txt` | the CAS Unity plugin, guide [llm-guides/cas-unity.md](../../../../llm-guides/cas-unity.md) |
| A Kotlin Multiplatform `shared` module | the cas-kmp skill |
| `config.xml` with a Cordova plugin list | the CAS Cordova plugin |
| An Unreal `.uproject` | the CAS Unreal plugin |

Say which one and stop. Integrating the native SDK underneath a plugin breaks the
plugin.

## Phase 1 and 2 - research, then ask

[references/questions.md](references/questions.md) holds the read-only research table and
the questions for this platform. The shape of a question is in
[references/question-format.md](references/question-format.md), the same in every CAS
guide: research first, show a short **Found** block, then ask in rounds of at most three
questions with lettered options and a recommended answer.

Do not ask what the project answers. Do not ask who will turn test mode off; that is a
rule, not a choice.

The answers that block everything else are the CAS ID, whether this is a migration, the
audience, and the ad formats with their screens.

## Phase 3 - plan

Write the plan before touching files: which files change, which dependencies arrive, where
initialization goes, which formats land on which screens, and what the publisher has to do
outside the code, in the dashboard or the store listing. Get agreement, then apply.

## Phase 4 - apply

In this order, because each step needs the previous one:

0. **Removing the previous mediation**, when this is a migration -
   [references/migration.md](references/migration.md). It comes first: the old SDK goes
   before the new one arrives.
1. **Gradle and ad networks** - [references/setup-gradle.md](references/setup-gradle.md)
2. **Initialization and consent** - [references/initialize.md](references/initialize.md)
   and [references/consent-privacy.md](references/consent-privacy.md)
3. **Ad formats** - [references/ad-formats.md](references/ad-formats.md)

Keep the project compiling between steps. If a step cannot be finished, stop there, leave
the project building and say what is missing.

## Phase 5 - verify

Follow [references/verify-and-release.md](references/verify-and-release.md). The short
version: build, install, run, and read the log. The integration counts as verified when
the initialization callback reports no error, a test ad loads and shows for every format
that was added, and the log holds no integration errors. Report the exact log lines.

When an ad fails, look the error code up in
[references/troubleshooting.md](references/troubleshooting.md) before guessing a cause.

## Phase 6 - hand over

End with a short checklist for the publisher: what changed, what has to be turned off
before release, what belongs in the store data disclosure, and anything still waiting on
the dashboard or the account manager.

## Documentation and samples

**The documentation is in this repository, under `docs/`.** Read the page from the working
tree rather than fetching the website: it is the same source, on the same branch, and it
costs no network call. Every reference file links the page it was written from, and the
map of all of them is in [references/docs-map.md](references/docs-map.md). When this skill
and the documentation disagree, the documentation is right.

A reference gives the shape of the integration and the constraints around it; the page
gives the full, current code. Read both before writing, and do not copy code out of a
reference when the page is one link away.

This is not the only guide for agents here. `llm-guides/` holds the same kind of material
for the rest of the surface: a Unity project belongs to `cas-unity.md`, the CAS Publishing
program to `cas-publishing-hub.md`, and the studio's Firebase setup to
`firebase-ga4-setup.md`. They use the same question format as this skill.
