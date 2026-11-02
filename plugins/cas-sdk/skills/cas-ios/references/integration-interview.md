# What to ask the publisher before integrating CAS

**Research first, ask second.** Everything that can be read from the project is read, not
asked. Do the read-only checks below, show what was found, and put what is left into
questions.

## Question format

The same format as every other CAS guide for agents, see `llm-guides/` in this
repository.

- One question = one short bold line, then `Found:` (the evidence and where it came from,
  or "nothing found"), then a table of 2-4 ready options with a letter (two columns only:
  the letter and the answer). Right after the table, one line:
  `**Recommended:** Option A - the reason in a few words`. Add a last option "Other" only
  when a free value is possible. When there is nothing to recommend, a value only the
  publisher has, write `**Recommended:** none - <why>`.
- Do not ask what the research settled. List it under `Found:` so the publisher can object.
- **Rounds of at most 3 questions**, numbered through all rounds (Q1-Q3, then Q4-Q6),
  each round titled "Round 1 of 3". Send one round, wait, then the next. Order them so a
  question an earlier answer decides comes after it, and skip one an earlier answer made
  pointless.
- Separate the parts of a question with empty lines, and the questions from each other
  with a `---` line.
- End each round with: "Reply `ok` to accept every Recommended answer of this round, or
  list the changes, for example `2B`."
- `ok` accepts every Recommended answer of the round. A question with
  `Recommended: none` stays open: ask only that one again, in a short follow-up, before
  the next round. Never re-ask what `ok` settled.
- After the reply, say in one line what was chosen and start at once.

Example:

> **Q1. Which CAS ID?**
> Found: `applicationId` is `com.yourcompany.yourgame` (`app/build.gradle.kts`).
>
> | Option | Answer |
> |---|---|
> | A | `com.yourcompany.yourgame` |
> | B | Other |
>
> **Recommended:** Option A - the application id of the module being integrated.

## Research, read-only

| What | Where to look |
|---|---|
| Module to integrate, application id | Android: `applicationId` in the app-level Gradle file, per flavour, and whether the module is an application or a library. iOS: `PRODUCT_BUNDLE_IDENTIFIER` per target and configuration. |
| Toolchain | Android: `minSdk`, `compileSdk`, AGP, Gradle, Kotlin. iOS: deployment target, Xcode version. |
| Language and UI | Kotlin or Java, Views or Compose. Swift or Objective-C, UIKit or SwiftUI. |
| Dependency manager | Android: `settings.gradle(.kts)`, is `dependencyResolutionManagement` used and with which `repositoriesMode`. iOS: `Podfile` and a workspace, or `Package.swift` and the package list. |
| CAS already present | Android: the `com.cleveradssolutions.gradle-plugin` id and a `cas { }` block. iOS: the `CAS-iOS` package or a `CleverAdsSolutions-SDK` pod. Then: which solution, and is test ad mode already set. |
| Other ad SDKs | Dependencies and initialization calls for `applovin`, `ironsource`, `levelplay`, `admob` / `google.ads`, `unity ads`, `appodeal`, `yandex`, `chartboost`, `vungle`, `liftoff`, `pangle`, `mintegral`, `inmobi`, `fyber`, `ogury`. Two mediation SDKs cannot run side by side. |
| Not an ad SDK, do not touch | Attribution and analytics (AppsFlyer, Adjust, Tenjin, Firebase), in-app purchases, the consent platform. If unsure whether something is an ad SDK, ask. |
| Consent platform | Google UMP, Didomi, Usercentrics, or none. |
| iOS property list | `SKAdNetworkItems`, `GADApplicationIdentifier`, `NSUserTrackingUsageDescription`, App Transport Security exceptions. |
| Ad formats used today | The calls of an existing ad SDK, if there is one: interstitial, rewarded, banner, app open, native. |
| Version control | `git status`. An uncommitted tree before an integration is a risk worth naming. |

Show a short **Found** block first: module, toolchain, dependency manager, what is already
installed, which ad SDK was found. Then the questions.

## Questions

Ask only those that still apply. The last column is a rule for the agent, not part of what
the publisher sees: it says how to pick the Recommended option.

| # | Question | Options | Recommended is |
|---|---|---|---|
| 1 | Which CAS ID? | Android: A. the application id found; B. another value. iOS: A. the numeric App Store id, digits only, from `apps.apple.com/.../id123456789`; B. `demo`, not on the App Store yet | Android: the application id from the project. iOS: the id already in the project, else `demo`. A value only the publisher has: `none`. |
| 2 | Is the app already registered in CAS? | A. No, or do not know; B. Yes | A. Until CAS registers the app, nothing serves. The integration is still done, on test ads, and the publisher's CAS contact registers it. |
| 3 | New integration, or a migration from another mediation? | A. New; B. Migration from the SDK found in the research; C. Migration from another one | B when the research found another mediation SDK, else A. CAS cannot run next to one, so B means removing it in the same change. |
| 4 | Which ad formats, and on which screens? | A. the formats the existing ad code uses; B. banner, interstitial, rewarded; C. another combination | A when an existing ad SDK was found, else B. Follow up for the screens and the moments. |
| 5 | What is the audience? | A. General or mixed; B. Child-directed, Families policy; C. VPN or proxy app | A. Most apps are general. If nothing in the project says otherwise and the publisher does not know, count it as A. B and C change which solution is allowed. |
| 6 | iOS only, and only when 5 is B: is the app in the Apple Kids Category? | A. No; B. Yes | A. On B, stop and point at the Kids Category page: the Families solution is not intended for it. |
| 7 | Which solution? | A. Optimal; B. Families; C. VPN Compliant; D. a hand-picked adapter list | Follows from question 5: A for general, B for child-directed, C for VPN. D only when the publisher asks. Anything finer is the account manager's call. |
| 8 | Consent: the built-in CAS flow, or your own platform? | A. The built-in flow; B. The consent platform found in the research | B when a consent platform was found, else A. On B the built-in flow is disabled and privacy options are set before initialization. |
| 9 | Collect the Google Advertising ID? | A. Yes; B. No | B when the audience is child-directed or the build targets devices without Google Play services, else A. |
| 10 | Which AdMob App ID? | A. the one in the project; B. the one from the CAS dashboard | `none` unless the project already holds one that came from CAS. It must be the identifier CAS generated; the publisher's own AdMob App ID is not valid. Not asked when Google demand is not used. |
| 11 | iOS: will the app ask for App Tracking Transparency? | A. Yes, with the text already in the property list; B. Yes, with a new text the publisher gives; C. No | A when `NSUserTrackingUsageDescription` is present, else B. Follow up for when the prompt should appear. |
| 12 | iOS: Swift Package Manager or CocoaPods? | A. the one already in the project; B. the other one | A. Not asked when only one is possible. Never convert a working project from one to the other unasked. |
| 13 | Where will this be verified? | A. a device; B. a simulator or emulator; C. the publisher runs it themselves | A when a device is connected, else B. The build under test runs on test ads either way; that is a rule, not an option. |
| 14 | Version control: the tree has uncommitted changes. | A. Go on; B. Stop, I commit first | A when the tree is clean, in which case the question is not asked at all. |

## Follow-ups, only when relevant

| # | Question | Why |
|---|---|---|
| 15 | Several flavours or schemes with different application ids? | Each one is a separate CAS ID and is registered separately. |
| 16 | The integration goes into a library module? | `casId` has to be set explicitly; it cannot come from an `applicationId`. |
| 17 | Impression level data needed for analytics? | Changes what is wired at initialization. |
| 18 | In-app purchases reported to CAS? | A separate call, see the purchase page. |
| 19 | A minimum interval between interstitials, or a banner refresh interval? | Defaults exist; a publisher with a pacing policy will want their own. |
| 20 | After a migration, do the reports have to stay comparable with the old ones? | Affects placement names and the impression level data wiring. |

## What the publisher does, not the agent

Registering the app in the CAS dashboard, deciding whether the audience is child-directed,
writing the App Tracking Transparency text, answering the store data disclosures, and
getting access to closed beta networks or cross promotion. The agent reminds and links,
and does not decide.
