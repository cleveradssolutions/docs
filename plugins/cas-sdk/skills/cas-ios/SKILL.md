---
name: cas-ios
description: Integrate, upgrade or debug the CAS.AI (Clever Ads Solutions) ad mediation SDK in an iOS app - Swift Package Manager or CocoaPods setup, Info.plist configuration, initialization, consent flow, banner, interstitial, rewarded, app open and native ads, and verification on test ads. Use when the user mentions CAS.AI, cleveradssolutions, CleverAdsSolutions, the CAS-iOS package, or asks to add ad monetization or migrate from AppLovin MAX, LevelPlay or AdMob mediation on iOS.
license: Apache-2.0
---

# CAS.AI on iOS

CAS.AI is an ad mediation SDK: one integration, many ad networks bidding for each
impression. It ships no inventory of its own, so an integration without ad networks shows
nothing.

Work through the phases in order. Each one ends with something the publisher can check.

## Before anything else

Four rules decide whether this integration is safe. The rest are in
[references/agent-rules.md](references/agent-rules.md), read it before writing code.

1. **Every non-release build runs test ads.** Clicking a live ad during development is
   invalid traffic and can get the publisher's account flagged. Bind test mode to the
   debug configuration and never suggest clicking a live ad to check the work.
2. **Never invent an identifier.** The CAS ID, the AdMob App ID and test device ids come
   from the CAS dashboard or from the publisher. An id copied out of a sample is not a
   placeholder, it is someone else's account. On iOS the CAS ID is the **App Store id**,
   the number from `apps.apple.com/.../id123456789`, not the bundle identifier.
3. **CAS initializes the ad networks.** Do not call AdMob, AppLovin, Unity Ads or any
   other network initializer, and do not add their pods or packages by hand.
4. **Verified means run.** The integration is done when the app builds, the SDK
   initializes, a test ad loads and shows, and the console is clean. Not before.

## Phase 1 - survey

Read the project and write down what you found. Do not ask about anything in this list.

- Bundle identifier per target and configuration, and which target gets the SDK.
- Deployment target, Xcode version, Swift or Objective-C, UIKit or SwiftUI.
- Dependency manager in use: `Package.swift` and the package list, or `Podfile` and
  whether a workspace already exists. If both are possible, this becomes a question.
- Ad SDKs already present and where they are initialized. A second mediation SDK is a
  migration, not an addition.
- `Info.plist`: `SKAdNetworkItems`, `GADApplicationIdentifier`,
  `NSUserTrackingUsageDescription`, App Transport Security exceptions.
- An existing consent platform, and the MMP and analytics SDKs with their initialization
  sites, they have to move after consent.
- View controllers or scenes that are candidates for each ad format.

Report the list in one short block and ask the publisher to correct it.

## Phase 2 - interview

Ask only what the project cannot answer. The full list with the reasoning is in
[references/integration-interview.md](references/integration-interview.md); the ones that
block the work are:

- the **CAS ID**, the App Store id, and whether the app is registered in the CAS dashboard,
- **new integration or migration**, and from what,
- **which formats, on which screens**,
- **the audience**: child-directed, mixed, adult, or a VPN or proxy app. A child-directed
  app also raises the Apple Kids Category question, which changes which solution is legal,
- **which solution**: Optimal, Families, VPN compliant, or a hand-picked adapter list,
- **consent**: the built-in CAS flow or an existing CMP,
- **App Tracking Transparency**: will the app ask, with what text, and when,
- **how this will be tested**, on which device or simulator and which configuration.

If the publisher does not know which solution to pick: Optimal for a general audience,
Families for a child-directed app that is not in the Apple Kids Category, VPN compliant
for a VPN or proxy app, and the account manager decides anything finer.

## Phase 3 - plan

Write the plan before touching files: which files change, which packages or pods arrive,
what the `Info.plist` script will add, where initialization goes, which formats land on
which screens, and what the publisher has to do outside the code, in the dashboard, in App
Store Connect or in the privacy nutrition labels. Get agreement, then apply.

## Phase 4 - apply

In this order, because each step needs the previous one:

1. **Dependencies and ad networks** -
   [references/setup-dependencies.md](references/setup-dependencies.md)
2. **Info.plist** - [references/info-plist.md](references/info-plist.md)
3. **Initialization and consent** - [references/initialize.md](references/initialize.md)
   and [references/consent-privacy.md](references/consent-privacy.md)
4. **Ad formats** - [references/ad-formats.md](references/ad-formats.md)

Keep the project building between steps. If a step cannot be finished, stop there, leave
the project building and say what is missing.

## Phase 5 - verify

Follow [references/verify-and-release.md](references/verify-and-release.md). The short
version: build, run, and read the console. The integration counts as verified when the
initialization handler reports no error, a test ad loads and shows for every format that
was added, and the console holds no integration errors. Report the exact log lines.

When an ad fails, look the error code up in
[references/troubleshooting.md](references/troubleshooting.md) before guessing a cause.

## Phase 6 - hand over

End with a short checklist for the publisher: what changed, what has to be turned off
before release, what belongs in the App Store privacy answers, and anything still waiting
on the dashboard or the account manager.

## Documentation and samples

Every reference file names the page it was written from. The map is in
[references/docs-map.md](references/docs-map.md). When this skill and the documentation
disagree, the documentation is right.
