# iOS: research the project, then ask

The shape of a question is in [question-format.md](question-format.md), the same in every
CAS guide. This file is the iOS questions only; Android has its own, and so will each
plugin, because the answers that matter are not the same.

## Research, read-only

| What | Where to look |
|---|---|
| Not a native iOS app at all | `pubspec.yaml` (Flutter), `react-native` in `package.json`, `ProjectSettings/ProjectVersion.txt` (Unity), a Kotlin Multiplatform `shared` module, `config.xml` (Cordova). These have their own CAS plugin and their own guide. Stop and say which one. |
| Target to integrate | `PRODUCT_BUNDLE_IDENTIFIER` per target and configuration. Extensions and widgets are not the app target. |
| Toolchain | Deployment target and Xcode version against the minimums on the setup page. Do not raise anything yet. |
| Language and UI | Swift or Objective-C; UIKit, SwiftUI, or both. Whether the app has a `SceneDelegate` or a SwiftUI `App`. Which concurrency style the screens use, async/await or completion handlers. |
| Dependency manager | `Podfile` and a `.xcworkspace`, or `Package.swift` and the resolved package list. **Both present, or neither**, is a question, not a guess: a project with no manager at all is common in samples and small apps, and introducing one is the publisher's call. |
| CAS already present | The `CAS-iOS` package or a `CleverAdsSolutions-SDK` pod, `CAS.buildManager` in the code. Then: which solution products or subspecs, and whether test ad mode is bound to anything. |
| Other ad SDKs | Pods or packages and initialization calls for `AppLovin`, `IronSource` / `LevelPlay`, `Google-Mobile-Ads-SDK`, `UnityAds`, `Appodeal`, `YandexMobileAds`, `Chartboost`, `Vungle`, `Pangle`, `Mintegral`, `InMobi`, `Fyber`, `Ogury`. Two mediation SDKs cannot run side by side. |
| Not an ad SDK, do not touch | Attribution and analytics (AppsFlyer, Adjust, Tenjin, Firebase), StoreKit, the consent platform. If unsure whether something is an ad SDK, ask. |
| Property list | `SKAdNetworkItems`, `GADApplicationIdentifier`, `NSUserTrackingUsageDescription`, App Transport Security exceptions. Note which are missing, they are what the configuration script will add. |
| Consent platform | Google UMP, Didomi, Usercentrics, or none. |
| Ad formats used today | The calls of an existing ad SDK: interstitial, rewarded, banner, app open, native. They are the formats to reproduce. |
| Screens | View controllers or SwiftUI scenes with their lifecycle. A banner needs constraints; an interstitial needs a view controller to present from. |
| Version control | `git status`. An uncommitted tree before an integration is worth naming. |

Show a short **Found** block first: target and bundle id, toolchain, UI framework,
dependency manager, what is already installed, which ad SDK was found. Then the questions.

## Questions

Ask only those that still apply. The last column is a rule for the agent, not part of what
the publisher sees: it says how to pick the Recommended option.

| # | Question | Options | Recommended is |
|---|---|---|---|
| 1 | Which CAS ID? | A. the numeric App Store id, digits only, from `apps.apple.com/.../id123456789`; B. `demo`, the app is not on the App Store yet | **The CAS ID on iOS is the App Store id, not the bundle identifier.** The id already in the project, else `demo`. A real id only the publisher has: `none`. |
| 2 | Is the app already registered in CAS? | A. No, or do not know; B. Yes | A. Until CAS registers the app nothing serves. The integration is still done, on test ads, and the publisher's CAS contact registers it. |
| 3 | New integration, or a migration from another mediation? | A. New; B. Migration from the SDK found in the research; C. Migration from another one | B when the research found another mediation SDK, else A. CAS cannot run next to one, so B means removing it in the same change. |
| 4 | Which ad formats, and on which screens? | A. the formats the existing ad code uses, on the same screens; B. banner, interstitial, rewarded; C. another combination | A when an existing ad SDK was found, else B. Follow up for the screen and the moment of each one, see the placement rules in `ad-formats.md`. |
| 5 | What is the audience? | A. General or mixed; B. Child-directed; C. VPN or proxy app | A. Most apps are general. If nothing in the project says otherwise and the publisher does not know, count it as A. |
| 6 | Only when 5 is B: is the app in the Apple Kids Category? | A. No; B. Yes | A. On B, stop and point at the Kids Category page: the Families solution is not intended for it, and the choice has to be agreed with the account manager. |
| 7 | Which solution? | A. Optimal; B. Families; C. VPN Compliant; D. a hand-picked adapter list | Follows from questions 5 and 6. D only when the publisher asks for it. Anything finer is the account manager's call. |
| 8 | Swift Package Manager or CocoaPods? | A. the one already in the project; B. the other one; C. the project has neither, introduce one | A. Not asked when only one is possible. Never convert a working project from one to the other unasked. On C recommend Swift Package Manager, unless the publisher's other dependencies are pods. |
| 9 | Consent: the built-in CAS flow, or your own platform? | A. The built-in flow; B. The consent platform found in the research | B when a consent platform was found, else A. On B the built-in flow is disabled and privacy options are set before initialization. |
| 10 | Will the app ask for App Tracking Transparency? | A. Yes, with the text already in the property list; B. Yes, with a new text the publisher gives; C. No | A when `NSUserTrackingUsageDescription` is present, else B. Follow up for the moment the prompt should appear; that is a product decision, not a technical one. |
| 11 | Which AdMob App ID? | A. the one in the property list; B. the one from the CAS dashboard | `none` unless the property list already holds one that came from CAS. It must be the identifier CAS generated; the publisher's own AdMob App ID is not valid. Not asked when Google demand is not used. |
| 12 | Where will this be verified? | A. a connected device; B. a simulator; C. the publisher runs it themselves | A when a device is available, else B. The build under test serves test ads either way; that is a rule, not an option. |
| 13 | The tree has uncommitted changes. | A. Go on; B. Stop, I commit first | A. Not asked at all when the tree is clean. |

## Follow-ups, only when relevant

| # | Question | Why |
|---|---|---|
| 14 | Several targets or configurations with different bundle ids? | Each shipped app is its own App Store id and its own CAS ID. |
| 15 | Impression level data needed for analytics? | Changes what is wired at initialization. |
| 16 | In-app purchases reported to CAS? | A separate call, see the purchase page. |
| 17 | A minimum interval between interstitials, or a banner refresh interval? | Defaults exist; a publisher with a pacing policy will want their own. |
| 18 | After a migration, do the reports have to stay comparable with the old ones? | Affects placement names and the impression level data wiring. |

## What the publisher does, not the agent

Registering the app in the CAS dashboard, deciding whether the audience is child-directed,
writing the App Tracking Transparency text, answering the App Store privacy questions, and
getting access to closed beta networks. The agent reminds and links, and does not decide.
