---
name: cas-android
description: Integrate, upgrade or debug the CAS.AI (Clever Ads Solutions) ad mediation SDK in an Android app - Gradle setup, initialization, consent flow, banner, interstitial, rewarded, app open and native ads, and verification on test ads. Use when the user mentions CAS.AI, cleveradssolutions, the CAS Gradle plugin, or asks to add ad monetization or migrate from AppLovin MAX, LevelPlay or AdMob mediation on Android.
license: Apache-2.0
---

# CAS.AI on Android

CAS.AI is an ad mediation SDK: one integration, many ad networks bidding for each
impression. It ships no inventory of its own, so an integration without ad networks shows
nothing.

Work through the phases in order. Each one ends with something the publisher can check.

## Before anything else

Four rules decide whether this integration is safe. The rest are in
[references/agent-rules.md](references/agent-rules.md), read it before writing code.

1. **Every non-release build runs test ads.** Clicking a live ad during development is
   invalid traffic and can get the publisher's account flagged. Bind test mode to the
   debug flag and never suggest clicking a live ad to check the work.
2. **Never invent an identifier.** The CAS ID, the AdMob App ID and test device ids come
   from the CAS dashboard or from the publisher. An id copied out of a sample is not a
   placeholder, it is someone else's account.
3. **CAS initializes the ad networks.** Do not call AdMob, AppLovin, Unity Ads or any
   other network initializer, and do not add their dependencies by hand.
4. **Verified means run.** The integration is done when the app builds, the SDK
   initializes, a test ad loads and shows, and the log is clean. Not before.

## Phase 1 - survey

Read the project and write down what you found. Do not ask about anything in this list.

- `applicationId` per flavour, the module that will host the integration, and whether it
  is an application or a library module.
- `minSdk`, `compileSdk`, AGP, Gradle and Kotlin versions; Kotlin or Java; Views or
  Compose.
- `settings.gradle(.kts)`: is `dependencyResolutionManagement` used, and with which
  `repositoriesMode`.
- Ad SDKs already present and where they are initialized. A second mediation SDK is a
  migration, not an addition.
- An existing consent platform (Google UMP, Didomi, Usercentrics).
- MMP and analytics SDKs and their initialization sites, they have to move after consent.
- Screens that are candidates for each ad format.

Report the list in one short block and ask the publisher to correct it.

## Phase 2 - interview

Ask only what the project cannot answer. The full list with the reasoning is in
[references/integration-interview.md](references/integration-interview.md); the ones that
block the work are:

- the **CAS ID** and whether the app is registered in the CAS dashboard,
- **new integration or migration**, and from what,
- **which formats, on which screens**,
- **the audience**: child-directed, mixed, adult, or a VPN or proxy app,
- **which solution**: Optimal, Families, VPN compliant, or a hand-picked adapter list,
- **consent**: the built-in CAS flow or an existing CMP,
- **how this will be tested**, on which device and which build variant.

If the publisher does not know which solution to pick: Optimal for a general audience,
Families for a child-directed app, VPN compliant for a VPN or proxy app, and the account
manager decides anything finer.

## Phase 3 - plan

Write the plan before touching files: which files change, which dependencies arrive, where
initialization goes, which formats land on which screens, and what the publisher has to do
outside the code, in the dashboard or the store listing. Get agreement, then apply.

## Phase 4 - apply

In this order, because each step needs the previous one:

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

Every reference file names the page it was written from. The map is in
[references/docs-map.md](references/docs-map.md). When this skill and the documentation
disagree, the documentation is right.
