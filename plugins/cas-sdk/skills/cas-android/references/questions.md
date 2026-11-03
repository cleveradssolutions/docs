# Android: research the project, then ask

The shape of a question is in [question-format.md](question-format.md), the same in every
CAS guide. This file is the Android questions only; iOS has its own, and so will each
plugin, because the answers that matter are not the same.

## Research, read-only

| What | Where to look |
|---|---|
| Not an Android app at all | `pubspec.yaml` (Flutter), `react-native` in `package.json`, `ProjectSettings/ProjectVersion.txt` (Unity), a Kotlin Multiplatform `shared` module, `config.xml` (Cordova). These have their own CAS plugin and their own guide. Stop and say which one. |
| Module to integrate | The module with `com.android.application`, or the library module the publisher names. `applicationId` per flavour, including `applicationIdSuffix`. |
| Toolchain | `minSdk`, `compileSdk`, the Android Gradle plugin, Gradle and Kotlin versions. Compare with the minimums on the setup page, do not raise anything yet. |
| Language and UI | Kotlin or Java; Views, Compose, or both. Which one the screens that will host ads use. |
| Repositories | `settings.gradle(.kts)`: is `dependencyResolutionManagement` used, and with which `repositoriesMode`. Gradle below 7 puts repositories in the root `build.gradle` instead. |
| CAS already present | The `com.cleveradssolutions.gradle-plugin` id, a `cas { }` block, `CAS.buildManager` in the code. Then: which solution, and whether test ad mode is already bound to anything. |
| Other ad SDKs | Dependencies and initialization calls for `applovin`, `ironsource` / `levelplay`, `google.android.gms:play-services-ads` / `admob`, `unity-ads`, `appodeal`, `yandex`, `chartboost`, `vungle`, `pangle`, `mintegral`, `inmobi`, `fyber`, `ogury`. Two mediation SDKs cannot run side by side. |
| Not an ad SDK, do not touch | Attribution and analytics (AppsFlyer, Adjust, Tenjin, Firebase), billing, the consent platform. If unsure whether something is an ad SDK, ask. |
| Consent platform | Google UMP (`com.google.android.ump`), Didomi, Usercentrics, or none. |
| Ad formats used today | The calls of an existing ad SDK: interstitial, rewarded, banner, app open, native. They are the formats to reproduce. |
| Screens | Activities and fragments, or composable destinations, with their lifecycle owners. A banner needs a container; an interstitial needs a transition. |
| Manifest | `com.google.android.gms.ads.APPLICATION_ID`, and whether hardware acceleration was disabled anywhere. |
| Version control | `git status`. An uncommitted tree before an integration is worth naming. |

Show a short **Found** block first: module and application id, toolchain, UI toolkit, what
is already installed, which ad SDK was found. Then the questions.

## Questions

Ask only those that still apply. The last column is a rule for the agent, not part of what
the publisher sees: it says how to pick the Recommended option.

| # | Question | Options | Recommended is |
|---|---|---|---|
| 1 | Which CAS ID? | A. the `applicationId` found; B. another value | The `applicationId` of the module being integrated. Several flavours: ask which, each one is a separate CAS ID. A value only the publisher has: `none`. |
| 2 | Is the app already registered in CAS? | A. No, or do not know; B. Yes | A. Until CAS registers the app nothing serves. The integration is still done, on test ads, and the publisher's CAS contact registers it. |
| 3 | New integration, or a migration from another mediation? | A. New; B. Migration from the SDK found in the research; C. Migration from another one | B when the research found another mediation SDK, else A. CAS cannot run next to one, so B means removing it in the same change. |
| 4 | Which ad formats, and on which screens? | A. the formats the existing ad code uses, on the same screens; B. banner, interstitial, rewarded; C. another combination | A when an existing ad SDK was found, else B. Follow up for the screen and the moment of each one, see the placement rules in `ad-formats.md`. |
| 5 | What is the audience? | A. General or mixed; B. Child-directed, Google Play Families policy; C. VPN or proxy app | A. Most apps are general. If nothing in the project says otherwise and the publisher does not know, count it as A. B and C change which solution is allowed and whether the advertising id may be collected. |
| 6 | Which solution? | A. Optimal; B. Families; C. VPN Compliant; D. a hand-picked adapter list | Follows from question 5. D only when the publisher asks for it. Anything finer is the account manager's call. |
| 7 | Consent: the built-in CAS flow, or your own platform? | A. The built-in flow; B. The consent platform found in the research | B when a consent platform was found, else A. On B the built-in flow is disabled and privacy options are set before initialization. |
| 8 | Collect the Google Advertising ID? | A. Yes; B. No | B when the audience is child-directed, or the build targets devices without Google Play services such as Amazon or Huawei. Else A. |
| 9 | Which AdMob App ID? | A. the one in the manifest; B. the one from the CAS dashboard | `none` unless the manifest already holds one that came from CAS. It must be the identifier CAS generated; the publisher's own AdMob App ID is not valid. Not asked when Google demand is not used. |
| 10 | Where will this be verified? | A. a connected device; B. an emulator; C. the publisher runs it themselves | A when a device is connected (`adb devices`), else B. The build under test serves test ads either way; that is a rule, not an option. |
| 11 | The tree has uncommitted changes. | A. Go on; B. Stop, I commit first | A. Not asked at all when the tree is clean. |
| 12 | The integration goes into a library module. Which CAS ID? | A. the value the publisher gives; B. one per flavour with `addCasIdVariant` | `none`. A library module has no `applicationId`, so `cas.casId` has to be set explicitly. Only asked for a library module. |

## Follow-ups, only when relevant

| # | Question | Why |
|---|---|---|
| 13 | Several flavours with different application ids? | Each final `applicationId`, suffixes included, is a separate CAS ID and is registered separately. |
| 14 | Impression level data needed for analytics? | Changes what is wired at initialization. |
| 15 | In-app purchases reported to CAS? | A separate call, see the purchase page. |
| 16 | A minimum interval between interstitials, or a banner refresh interval? | Defaults exist; a publisher with a pacing policy will want their own. |
| 17 | After a migration, do the reports have to stay comparable with the old ones? | Affects placement names and the impression level data wiring. |

## What the publisher does, not the agent

Registering the app in the CAS dashboard, deciding whether the audience is child-directed,
answering the Google Play data disclosure, and getting access to closed beta networks or
cross promotion. The agent reminds and links, and does not decide.
