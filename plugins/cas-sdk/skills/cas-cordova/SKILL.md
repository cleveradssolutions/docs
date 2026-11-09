---
name: cas-cordova
description: >-
  Integrates, upgrades or debugs the CAS.AI (Clever Ads Solutions) ad mediation
  SDK in a Cordova, Ionic or Capacitor-with-Cordova app: the
  cordova-plugin-casai plugin and its config.xml variables, ad network choice
  per platform, initialization from deviceready, consent flow, and the banner,
  MRec, interstitial, rewarded and app open formats with their Promise and
  document-event APIs, verified on test ads.
  Use when the task mentions CAS.AI, cleveradssolutions, cordova-plugin-casai,
  the CAS-Cordova plugin, or asks to add ad monetization or migrate from AdMob,
  AppLovin MAX or LevelPlay in a Cordova or Ionic project. Don't use for native
  Android or iOS apps, which are the cas-android and cas-ios skills, or for
  Flutter, React Native, Kotlin Multiplatform, Unity, Unreal or Construct apps,
  which have their own CAS plugin and their own guide.
license: Apache-2.0
metadata:
  version: "0.1.0"
  category: CAS.AI
---
# CAS.AI on Cordova

CAS.AI is an ad mediation SDK: one integration, many ad networks bidding for each
impression. It ships no inventory of its own, so an integration without ad networks shows
nothing.

Two things shape the work here, and both are unlike the other CAS plugins:

**All configuration lives in `config.xml` plugin variables, and changing one means
removing and re-adding the plugin.** There is no CAS ID in the JavaScript at all — Android
reads the bundle package name itself, and iOS takes `IOS_CAS_ID` from a variable. So a
misconfigured app looks identical to a correct one until it runs.

**Ads are native views over the WebView, not DOM.** The banner is positioned with an
anchor and an offset in density-independent pixels, floating above the page. The app's own
layout has to make room for it; CSS cannot see it.

Work through the phases in order. Each one ends with something the publisher can check.

## Before anything else

Five rules decide whether this integration is safe. The rest are in
[references/agent-rules.md](references/agent-rules.md), read it before writing code.

1. **Test ads, both ways.** Never test on live ads: a click during development is
   invalid traffic and can get the publisher's account flagged. Never release with test
   ads on: they earn nothing. So bind `forceTestAds` to the build, from a value the build
   produces, and do not ask anyone to remember to turn it off.
2. **Never invent an identifier.** `IOS_CAS_ID`, the AdMob App ID and test device ids come
   from the CAS dashboard or from the publisher. An id copied out of a sample is not a
   placeholder, it is someone else's account.
3. **The two platforms get their ids differently.** Android is automatic, from the bundle
   package name in `config.xml`. iOS needs `IOS_CAS_ID`, the **App Store id** — the number
   from `apps.apple.com/.../id123456789`, not the bundle identifier. Without it the iOS
   build silently serves test ads for ever.
4. **CAS initializes the ad networks.** Do not call AdMob, AppLovin or any other network
   initializer, do not add their Cordova plugins, and do not add their pods or Gradle
   dependencies by hand.
5. **Verified means run on a device.** The integration is done when the app builds for each
   platform being shipped, runs, initializes, loads and shows a test ad per format, and the
   native log is clean. A browser platform has no SDK at all, and `cordova serve` proves
   nothing.

## Phase 0 - is this the right skill

A `config.xml` with a `<widget>` root and a Cordova plugin list is the signal, plus
`cordova` or `@ionic/...` in `package.json`. Then settle three facts:

| What | Where to look | Why it decides things |
|---|---|---|
| Which platforms are added | `platforms/`, and the `<platform name>` sections of `config.xml` | Everything below is per platform. A `browser` platform exists in many projects and takes no ads. |
| The framework on top | Ionic with Angular, React or Vue, or plain Cordova | It does not change the API, but it decides where `deviceready` is handled and who owns the layout the banner has to fit into. |
| Capacitor or Cordova | a `capacitor.config.*` beside `config.xml` | Capacitor can run Cordova plugins, but it does not read `config.xml` plugin variables the same way. Confirm how the project supplies them before planning. |

And projects that are not this skill's at all:

| Found | Belongs to |
|---|---|
| `pubspec.yaml` | the cas-flutter skill |
| `react-native` in `package.json` | the cas-react-native skill |
| A Kotlin Multiplatform `shared` module | the cas-kmp skill |
| An Unreal `.uproject` | the cas-unreal skill |
| `ProjectSettings/ProjectVersion.txt` | the CAS Unity plugin, guide [llm-guides/cas-unity.md](../../../../llm-guides/cas-unity.md) |
| No `config.xml`, an Android or Xcode project only | the cas-android or cas-ios skill |

Say which one and stop when it is not this skill.

## Phase 1 and 2 - research, then ask

[references/questions.md](references/questions.md) holds the read-only research table and
the questions for this platform. The shape of a question is in
[references/question-format.md](references/question-format.md), the same in every CAS
guide: research first, show a short **Found** block, then ask in rounds of at most three
questions with lettered options and a recommended answer.

Do not ask what the project answers. Do not ask who will turn test mode off; that is a
rule, not a choice.

The answers that block everything else are which platforms ship, the iOS App Store id,
whether this is a migration, the audience, and the ad formats with the screens and the
layout room a banner would need.

## Phase 3 - plan

Write the plan before touching files: every `config.xml` variable and its value, where
`initialize` goes relative to `deviceready`, which formats land where, how the page layout
makes room for a native banner, and what the publisher has to do outside the code. Name the
remove-and-re-add step explicitly, because it is how any variable change takes effect. Get
agreement, then apply.

## Phase 4 - apply

In this order, because each step needs the previous one:

0. **Removing the previous mediation**, when this is a migration -
   [references/migration.md](references/migration.md). It comes first: the old SDK goes
   before the plugin arrives.
1. **The plugin and its variables** -
   [references/setup-plugin.md](references/setup-plugin.md)
2. **Initialization and consent** - [references/initialize.md](references/initialize.md)
   and [references/consent-privacy.md](references/consent-privacy.md)
3. **Ad formats** - [references/ad-formats.md](references/ad-formats.md)

Keep the project building between steps, and build **both** platforms the publisher ships.
A change to a plugin variable is not applied by a rebuild alone — the plugin has to be
removed and added again. If a step cannot be finished, stop there, leave the project
building and say what is missing.

## Phase 5 - verify

Follow [references/verify-and-release.md](references/verify-and-release.md). The short
version: build and run on each platform, and read the **native** console, not the WebView
one. The integration counts as verified when `initialize` resolves with no error, a test ad
loads and shows for every format that was added, and the log holds no integration errors.
Report the exact log lines.

When an ad fails, look the error code up in
[references/troubleshooting.md](references/troubleshooting.md) before guessing a cause.

## Phase 6 - hand over

End with a short checklist for the publisher: what changed, every `config.xml` variable and
what it has to be before release, what belongs in the Google Play and App Store privacy
answers, and anything still waiting on the dashboard or the account manager.

## Documentation and samples

**The documentation is in this repository, under `docs/`.** Read the page from the working
tree rather than fetching the website: it is the same source, on the same branch, and it
costs no network call. Every reference file links the page it was written from, and the
map of all of them is in [references/docs-map.md](references/docs-map.md). When this skill
and the documentation disagree, the documentation is right.

A reference gives the shape of the integration and the constraints around it; the page
gives the full, current code. Read both before writing, and do not copy code out of a
reference when the page is one link away. Every format page shows the same flow three
times — Promise, async/await and document events — so read the tab that matches the
project's style.

The plugin's own example app, `example/src/` in
`https://github.com/cleveradssolutions/CAS-Cordova`, is the shortest correct answer for
anything about API shape, and the package is
`https://www.npmjs.com/package/cordova-plugin-casai`.

This is not the only guide for agents here. `llm-guides/` holds the same kind of material
for the rest of the surface: a Unity project belongs to `cas-unity.md`, the CAS Publishing
program to `cas-publishing-hub.md`, and the studio's Firebase setup to
`firebase-ga4-setup.md`. They use the same question format as this skill.
