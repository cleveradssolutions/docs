# Flutter: research the project, then ask

The shape of a question is in [question-format.md](question-format.md), the same in every
CAS guide. This file is the Flutter questions only; every platform has its own, because the
answers that matter are not the same.

## Research, read-only

| What | Where to look |
|---|---|
| Not a Flutter app at all | No `pubspec.yaml`, or one without a `flutter:` section. A native Android or iOS project belongs to the cas-android or cas-ios skill; `react-native` in `package.json`, a Unity `ProjectSettings/`, a Cordova `config.xml`, an Unreal `.uproject` or a Kotlin Multiplatform `shared` module each belong to their own CAS plugin. Stop and say which one. |
| App or module | A full app has `android/` and `ios/` in the tree. A Flutter **module** has `.android/` and `.ios/` generated and is embedded in a native host, which owns Gradle and the Xcode project. Name which it is before planning anything native. |
| Which platforms ship | `android/` and `ios/` present, and whether both are built in CI. A desktop or web target does not take ads at all. |
| Flutter and Dart versions | `environment:` in `pubspec.yaml`, `flutter --version`. Against the minimums on the setup page. The iOS dependency path depends on this: Flutter `3.47` and newer build the plugin as a Swift package, earlier versions use CocoaPods. |
| Application id and bundle id | `applicationId` in `android/app/build.gradle`, `PRODUCT_BUNDLE_IDENTIFIER` in the Xcode project. These are two different CAS IDs, and on iOS the CAS ID is the App Store id rather than the bundle id. |
| Swift Package Manager state | `flutter config` output, or a `Podfile` plus `Podfile.lock` under `ios/`. **Both paths present**, a Swift package and CAS pods, is a duplicate-symbols build failure waiting to happen, not a preference. |
| CAS already present | `clever_ads_solutions` in `pubspec.yaml`, `CASMobileAds.initialize` in the Dart code, a `cas { }` block in `android/app/build.gradle`, a `casconfig.rb` under `ios/`. Then: which solution is switched on, and whether `forceTestAds` is bound to the build. |
| Other ad SDKs | Flutter packages `google_mobile_ads`, `applovin_max`, `ironsource_mediation`, `unity_ads_plugin`, `appodeal_flutter`, `facebook_audience_network`, and their `initialize` calls. Two mediation SDKs cannot run side by side. |
| Not an ad SDK, do not touch | Attribution and analytics (`appsflyer_sdk`, `adjust_sdk`, `firebase_analytics`), `in_app_purchase`, the consent platform. If unsure whether something is an ad SDK, ask. |
| Native leftovers of an old integration | Ad network keys in `AndroidManifest.xml` and `Info.plist`, `GADApplicationIdentifier`, `SKAdNetworkItems`, repositories added by hand in Gradle. A migration has to remove these, and the CAS tooling writes its own. |
| Consent platform | `google_mobile_ads` UMP, Didomi, Usercentrics, or none. |
| Ad formats used today | The calls of an existing ad package: interstitial, rewarded, banner, app open, native. They are the formats to reproduce. |
| Where ads would go | The widget tree: which screens are `StatefulWidget`s with a lifecycle to load in, where a banner could be pinned, which transitions a full screen ad could sit on. A banner needs a fixed height reserved; an interstitial needs a transition that already exists. |
| Version control | `git status`. An uncommitted tree before an integration is worth naming. |

Show a short **Found** block first: app or module, which platforms ship, Flutter version and
the iOS dependency path that follows from it, application id and bundle id, what is already
installed, which ad SDK was found. Then the questions.

## Questions

Ask only those that still apply. The last column is a rule for the agent, not part of what
the publisher sees: it says how to pick the Recommended option.

| # | Question | Options | Recommended is |
|---|---|---|---|
| 1 | Which platforms does this app ship? | A. Android and iOS; B. Android only; C. iOS only | What the tree and CI show. Everything else follows: a single-platform app needs one CAS ID and one native setup. |
| 2 | Which CAS ID on Android? | A. the `applicationId` found in the research; B. `demo`, the app is not on Google Play yet | A when an `applicationId` is set and the app is registered, else B. |
| 3 | Which CAS ID on iOS? | A. the numeric App Store id, digits only; B. `demo`, the app is not on the App Store yet | **Not the bundle identifier.** B unless the publisher gives the App Store id. A real id only the publisher has: `none`. |
| 4 | Is the app already registered in CAS? | A. No, or do not know; B. Yes | A. Until CAS registers the app nothing serves. The integration is still done, on test ads, and the publisher's CAS contact registers it. |
| 5 | New integration, or a migration from another mediation? | A. New; B. Migration from the package found in the research; C. Migration from another one | B when the research found another mediation package, else A. CAS cannot run next to one, so B means removing it — package, native keys and all — in the same change. |
| 6 | Which ad formats, and on which screens? | A. the formats the existing ad code uses, on the same screens; B. banner, interstitial, rewarded; C. another combination | A when an existing ad package was found, else B. Follow up for the widget and the moment of each one, see the placement rules in `ad-formats.md`. |
| 7 | What is the audience? | A. General or mixed; B. Child-directed; C. VPN or proxy app | A. Most apps are general. If nothing in the project says otherwise and the publisher does not know, count it as A. |
| 8 | Only when 7 is B: Google Play Families, and is the iOS app in the Apple Kids Category? | A. Families on Android, not in the Kids Category; B. Both | A. On the Kids Category, stop and point at the Kids Category page: the Families solution is not intended for it, and the choice has to be agreed with the account manager. |
| 9 | Which solution? | A. Optimal; B. Families; C. VPN Compliant; D. a hand-picked adapter list | Follows from questions 7 and 8, and is set per platform — the Gradle `cas { }` block on Android, the package products or pods on iOS. D only when the publisher asks for it. |
| 10 | iOS dependencies: Swift Package Manager or CocoaPods? | A. the path the Flutter version implies; B. the other one | A. Flutter `3.47` and newer default to Swift Package Manager. Never mix the two for CAS: that links two copies of the SDK. On a project that keeps a `Podfile` for other reasons, say which path CAS itself takes. |
| 11 | Consent: the built-in CAS flow, or your own platform? | A. The built-in flow, with `showConsentFormIfRequired`; B. The consent platform found in the research | B when a consent platform was found, else A. On B the built-in flow stays off and the status is set before initialization. |
| 12 | Which AdMob App ID? | A. the one already in the native projects; B. the one from the CAS dashboard | `none` unless the native projects already hold one that came from CAS. It must be the identifier CAS generated; the publisher's own AdMob App ID is not valid. Not asked when Google demand is not used. |
| 13 | Where will this be verified? | A. a connected device per platform; B. an emulator and a simulator; C. the publisher runs it themselves | A when a device is available, else B. The build under test serves test ads either way; that is a rule, not an option. |
| 14 | The tree has uncommitted changes. | A. Go on; B. Stop, I commit first | A. Not asked at all when the tree is clean. |

## Follow-ups, only when relevant

| # | Question | Why |
|---|---|---|
| 15 | Flavours or schemes with different application ids? | Each shipped app is its own CAS ID, so the id becomes a build-time value rather than a constant. |
| 16 | Native ads wanted? | They need extra native setup on both platforms, see the Native Ads Android and iOS setup pages, and a template or a layout per platform. |
| 17 | Impression level data needed for analytics? | Changes what is wired at initialization. |
| 18 | In-app purchases reported to CAS? | A separate call, see the purchase page. Flutter is the only plugin platform with that page. |
| 19 | A minimum interval between interstitials, or a banner refresh interval? | Defaults exist; a publisher with a pacing policy will want their own. |
| 20 | A trial ad-free interval after install? | `CASMobileAds.setTrialAdFreeInterval` exists for it, and it changes what a tester sees — an ad that does not show inside that window is correct behaviour, not a bug. |

## What the publisher does, not the agent

Registering the app in the CAS dashboard, deciding whether the audience is child-directed,
answering the Google Play data disclosure and the App Store privacy questions, writing the
App Tracking Transparency text, and getting access to closed beta networks. The agent
reminds and links, and does not decide.
