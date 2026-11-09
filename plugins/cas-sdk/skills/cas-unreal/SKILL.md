---
name: cas-unreal
description: >-
  Integrates, upgrades or debugs the CAS.AI (Clever Ads Solutions) ad mediation
  SDK in an Unreal Engine project: the CAS-Unreal plugin in Plugins/, the
  CleverAdsSolutions module dependency, the per-platform CAS.AI Config settings
  window, the Android and iOS packaging prerequisites, initialization, consent
  flow, and the banner, MREC, interstitial, rewarded and app open formats in C++
  or Blueprint, verified on test ads with ValidateAdsIntegration.
  Use when the task mentions CAS.AI, cleveradssolutions, CAS-Unreal,
  UCASMobileAds, or asks to add ad monetization or migrate from AppLovin MAX,
  LevelPlay, AdMob mediation or Unreal's built-in AdMob support in an Unreal
  project. Don't use for native Android or iOS apps, which are the cas-android
  and cas-ios skills, or for Flutter, React Native, Kotlin Multiplatform, Unity,
  Cordova or Construct apps, which have their own CAS plugin and their own guide.
license: Apache-2.0
metadata:
  version: "0.1.0"
  category: CAS.AI
---
# CAS.AI on Unreal Engine

CAS.AI is an ad mediation SDK: one integration, many ad networks bidding for each
impression. It ships no inventory of its own, so an integration without ad networks shows
nothing.

Unreal is unlike every other CAS platform in one way that shapes the whole job: **most of
the integration is not code.** The CAS ID, the ad networks, test mode, the audience, the
consent flow and the autoload formats are all checkboxes and fields in the editor's
`CAS.AI Config` window, set separately for Android and iOS. The agent writes the event
wiring and the show calls; the publisher clicks the settings. Say which is which early, or
the integration looks finished and is configured for nobody.

The second difference: on Unreal Engine 4 some of the prerequisites edit **the engine
installation**, not the project. Those are shared across every project on the machine and
need the publisher's explicit agreement before anything is touched.

Work through the phases in order. Each one ends with something the publisher can check.

## Before anything else

Five rules decide whether this integration is safe. The rest are in
[references/agent-rules.md](references/agent-rules.md), read it before writing code.

1. **Test ads, both ways.** Never test on live ads: a click during development is
   invalid traffic and can get the publisher's account flagged. Never release with test
   ads on: they earn nothing. Here `TestAdsMode` is a checkbox per platform rather than a
   build flag, so it is a release checklist item and has to be written into the hand-over.
2. **Never invent an identifier.** The CAS App ID and test device ids come from the CAS
   dashboard or from the publisher. An id copied out of a sample is not a placeholder, it
   is someone else's account.
3. **Two platforms, two CAS App IDs.** Android uses the package name; iOS uses the **App
   Store id**, the number from `apps.apple.com/.../id123456789`. They go into two different
   `CAS.AI Config - [Platform]` panels, and filling one is the most common half-done
   integration on this platform.
4. **CAS initializes the ad networks.** Do not call AdMob, AppLovin, Unity Ads or any
   other network initializer, do not add their plugins, and — specifically on Unreal —
   **turn off the engine's built-in AdMob support** and never use its Blueprint nodes.
5. **Verified means packaged and run.** The integration is done when the project packages
   for each platform being shipped, runs on a device, `ValidateAdsIntegration` reports the
   chosen networks as VERIFIED, a test ad loads and shows, and the log is clean. Playing in
   the editor proves nothing: there is no SDK in the editor.

## Phase 0 - is this the right skill

A `.uproject` file is the signal. Then settle four facts, because the rest of the work
depends on all of them:

| What | Where to look | Why it decides things |
|---|---|---|
| **Engine version** | `EngineAssociation` in the `.uproject` | UE4 needs engine-level prerequisites that UE5 does not: the Gradle wrapper, the Android Gradle plugin, `WebViewControl.java`, the `android:exported` tag, and Clang 15 flags in the `Target.cs` files. |
| **C++ or Blueprint-only** | a `Source/` folder with a `.Build.cs`, or none | The module dependency and every code example are C++. A Blueprint-only project can use the plugin's Blueprint nodes, but **an agent cannot author a Blueprint graph** — `.uasset` files are binary. There the agent writes the settings and the node-by-node instructions, and the publisher wires them. |
| Which platforms ship | the packaging settings and the platforms configured | Everything is set per platform, twice. A single-platform project needs one CAS App ID and one set of prerequisites. |
| Plugin already present | `Plugins/CAS-Unreal/CleverAdsSolutions.uplugin`, `CleverAdsSolutions` in `PublicDependencyModuleNames`, `UCASMobileAds` in the code, a `[CASPluginBuildConfig]` section in `Config/DefaultEngine.ini` | Then: which solution is switched on per platform, and whether `TestAdsMode` is still checked. |

And projects that are not this skill's at all:

| Found | Belongs to |
|---|---|
| `ProjectSettings/ProjectVersion.txt` | the CAS Unity plugin, guide [llm-guides/cas-unity.md](../../../../llm-guides/cas-unity.md) |
| No `.uproject`, an Android or Xcode project only | the cas-android or cas-ios skill |
| `pubspec.yaml` | the cas-flutter skill |
| `react-native` in `package.json` | the cas-react-native skill |
| A Kotlin Multiplatform `shared` module | the cas-kmp skill |
| `config.xml` with a Cordova plugin list | the cas-cordova skill |

Say which one and stop when it is not this skill.

## Phase 1 and 2 - research, then ask

[references/questions.md](references/questions.md) holds the read-only research table and
the questions for this platform. The shape of a question is in
[references/question-format.md](references/question-format.md), the same in every CAS
guide: research first, show a short **Found** block, then ask in rounds of at most three
questions with lettered options and a recommended answer.

Do not ask what the project answers. Do not ask who will turn test mode off; that is a
rule, not a choice.

The answers that block everything else are the engine version, C++ or Blueprint, the two
CAS App IDs, which platforms are shipped, whether the engine installation may be modified,
the audience, and the ad formats with their moments in the game.

## Phase 3 - plan

Write the plan before touching files, and split it in two: **what the agent changes** —
the plugin folder, `<Project>.Build.cs`, the C++ that initializes and shows ads,
`Config/DefaultEngine.ini`, the `Target.cs` files on UE4 — and **what the publisher clicks**
— every field of `CAS.AI Config - Android` and `CAS.AI Config - iOS`, the built-in AdMob
switch, and the packaging options. List the engine-level edits separately again, as things
that affect other projects. Get agreement, then apply.

## Phase 4 - apply

In this order, because each step needs the previous one:

0. **Removing the previous mediation**, when this is a migration -
   [references/migration.md](references/migration.md). It comes first: the old SDK goes
   before the plugin arrives.
1. **The plugin, the module, and the per-platform prerequisites** -
   [references/setup-plugin.md](references/setup-plugin.md)
2. **Initialization and consent** - [references/initialize.md](references/initialize.md)
   and [references/consent-privacy.md](references/consent-privacy.md)
3. **Ad formats** - [references/ad-formats.md](references/ad-formats.md)

Keep the project compiling between steps, and **package** both platforms the publisher
ships. An editor build says nothing about whether the Android Gradle step or the iOS pod
integration works, and those are where this platform fails. If a step cannot be finished,
stop there, leave the project compiling and say what is missing.

## Phase 5 - verify

Follow [references/verify-and-release.md](references/verify-and-release.md). The short
version: package and run on each platform, call `ValidateAdsIntegration()` once, and read
the device log. The integration counts as verified when initialization reports no error,
every chosen network is VERIFIED, a test ad loads and shows for each format that was added,
and the log holds no integration errors. Report the exact log lines, then remove the
integration helper call.

When a build or an ad fails, look it up in
[references/troubleshooting.md](references/troubleshooting.md) before guessing a cause —
this platform's failures are mostly link and packaging errors with known answers.

## Phase 6 - hand over

End with a short checklist for the publisher: what changed, every settings-window field
that has to be set or unset before release, what belongs in the Google Play and App Store
privacy answers, and anything still waiting on the dashboard or the account manager.

## Documentation and samples

**The documentation is in this repository, under `docs/`.** Read the page from the working
tree rather than fetching the website: it is the same source, on the same branch, and it
costs no network call. Every reference file links the page it was written from, and the
map of all of them is in [references/docs-map.md](references/docs-map.md). When this skill
and the documentation disagree, the documentation is right.

A reference gives the shape of the integration and the constraints around it; the page
gives the full, current code. Read both before writing, and do not copy code out of a
reference when the page is one link away. The pages also carry screenshots of the Blueprint
nodes and of the settings window, which are the fastest way to tell a publisher what to
click.

The plugin's source is `https://github.com/cleveradssolutions/CAS-Unreal`, and its releases
carry both the source archive and precompiled packages built for specific engine versions.

This is not the only guide for agents here. `llm-guides/` holds the same kind of material
for the rest of the surface: a Unity project belongs to `cas-unity.md`, the CAS Publishing
program to `cas-publishing-hub.md`, and the studio's Firebase setup to
`firebase-ga4-setup.md`. They use the same question format as this skill.
