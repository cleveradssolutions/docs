# React Native: research the project, then ask

The shape of a question is in [question-format.md](question-format.md), the same in every
CAS guide. This file is the React Native questions only; every platform has its own, because
the answers that matter are not the same.

## Research, read-only

| What | Where to look |
|---|---|
| Not a React Native app at all | No `react-native` in `package.json`. A native Android or iOS project belongs to the cas-android or cas-ios skill; `pubspec.yaml`, a Unity `ProjectSettings/`, a Cordova `config.xml`, an Unreal `.uproject` or a Kotlin Multiplatform `shared` module each belong to their own CAS plugin. Stop and say which one. |
| **Expo or bare** | `expo` in `package.json`, an `app.json` or `app.config.*`, and whether `android/` and `ios/` are committed. This decides every native step, so settle it before anything else. Expo with committed native folders is still Expo: the next `npx expo prebuild` overwrites hand edits. |
| React Native and Expo versions | `package.json`, and the Expo SDK version if present. New architecture on or off (`newArchEnabled`), because a module that has not been migrated shows up here first. |
| Which platforms ship | `android/` and `ios/`, or the Expo config's platform list, and whether both are built in CI. A web target does not take ads. |
| Application id and bundle id | `applicationId` in `android/app/build.gradle`, `PRODUCT_BUNDLE_IDENTIFIER` in the Xcode project, or the `android.package` and `ios.bundleIdentifier` in the Expo config. These are two different CAS IDs, and on iOS the CAS ID is the App Store id rather than the bundle id. |
| The iOS app target name | The scheme and target in the Xcode project. The documentation writes `Runner`, which is the Flutter name; a React Native target is named after the project, and the `Podfile` block and `Info.plist` path follow the real name. |
| CAS already present | `react-native-cas` in `package.json`, `CASMobileAds.initialize` in the code, a `cas { }` block in `android/app/build.gradle`, `CASAIAppIdentifier` in the `Info.plist`, or the plugin entry in the Expo config. Then: which solution is switched on, and whether `forceTestAds` is bound to `__DEV__`. |
| Other ad SDKs | npm packages `react-native-google-mobile-ads`, `react-native-applovin-max`, `ironsource-mediation`, `react-native-unity-ads`, `react-native-fbads`, and their initialize calls. Two mediation SDKs cannot run side by side. |
| Not an ad SDK, do not touch | Attribution and analytics (`react-native-appsflyer`, `react-native-adjust`, Firebase), in-app purchases, the consent platform. If unsure whether something is an ad SDK, ask. |
| Native leftovers of an old integration | Ad network keys in `AndroidManifest.xml` and `Info.plist`, `GADApplicationIdentifier`, `SKAdNetworkItems`, repositories added by hand in Gradle. A migration has to remove these, and the CAS tooling writes its own. |
| Consent platform | A UMP package, Didomi, Usercentrics, or none. |
| Ad formats used today | The calls of an existing ad package: interstitial, rewarded, banner, app open, native. They are the formats to reproduce. |
| Where ads would go | The navigator and the screens: which screens mount and unmount, which transitions exist for a full screen ad, where a banner could sit outside the scroll. Note the navigation library, because the dismissed-then-navigate rule is written against it. |
| Version control | `git status`. An uncommitted tree before an integration is worth naming. |

Show a short **Found** block first: Expo or bare, which platforms ship, React Native version
and new architecture state, application id and bundle id, the real iOS target name, what is
already installed, which ad SDK was found. Then the questions.

## Questions

Ask only those that still apply. The last column is a rule for the agent, not part of what
the publisher sees: it says how to pick the Recommended option.

| # | Question | Options | Recommended is |
|---|---|---|---|
| 1 | Expo with the config plugin, or bare native setup? | A. the one the research found; B. the other one | A, always. This is a fact about the project, not a preference; it is asked only to confirm what the research says, and skipped when the answer is unambiguous. Never convert a project from one to the other. |
| 2 | Which platforms does this app ship? | A. Android and iOS; B. Android only; C. iOS only | What the tree and CI show. Everything else follows: a single-platform app needs one CAS ID and one native setup. |
| 3 | Which CAS ID on Android? | A. the application id found in the research; B. `demo`, the app is not on Google Play yet | A when an application id is set and the app is registered, else B. |
| 4 | Which CAS ID on iOS? | A. the numeric App Store id, digits only; B. `demo`, the app is not on the App Store yet | **Not the bundle identifier.** B unless the publisher gives the App Store id. A real id only the publisher has: `none`. |
| 5 | Is the app already registered in CAS? | A. No, or do not know; B. Yes | A. Until CAS registers the app nothing serves. The integration is still done, on test ads, and the publisher's CAS contact registers it. |
| 6 | New integration, or a migration from another mediation? | A. New; B. Migration from the package found in the research; C. Migration from another one | B when the research found another mediation package, else A. CAS cannot run next to one, so B means removing it — package, native keys and all — in the same change. |
| 7 | Which ad formats, and on which screens? | A. the formats the existing ad code uses, on the same screens; B. banner, interstitial, rewarded; C. another combination | A when an existing ad package was found, else B. Follow up for the screen and the moment of each one, see the placement rules in `ad-formats.md`. |
| 8 | What is the audience? | A. General or mixed; B. Child-directed; C. VPN or proxy app | A. Most apps are general. If nothing in the project says otherwise and the publisher does not know, count it as A. |
| 9 | Only when 8 is B: Google Play Families, and is the iOS app in the Apple Kids Category? | A. Families on Android, not in the Kids Category; B. Both | A. On the Kids Category, stop and point at the Kids Category page: the Families solution is not intended for it, and the choice has to be agreed with the account manager. |
| 10 | Which solution? | A. Optimal; B. Families; C. VPN Compliant; D. a hand-picked adapter list | Follows from questions 8 and 9, and is set per platform. D only when the publisher asks for it. |
| 11 | Consent: the built-in CAS flow, or your own platform? | A. The built-in flow, with `showConsentFormIfRequired`; B. The consent platform found in the research | B when a consent platform was found, else A. On B the built-in flow stays off and the status is set before initialization. |
| 12 | Which AdMob App ID? | A. the one already in the native projects or the Expo config; B. the one from the CAS dashboard | `none` unless one is already present and came from CAS. It must be the identifier CAS generated; the publisher's own AdMob App ID is not valid. Not asked when Google demand is not used. |
| 13 | Where will this be verified? | A. a connected device per platform; B. an emulator and a simulator; C. the publisher runs it themselves | A when a device is available, else B. The build under test serves test ads either way; that is a rule, not an option. |
| 14 | The tree has uncommitted changes. | A. Go on; B. Stop, I commit first | A. Not asked at all when the tree is clean. |

## Follow-ups, only when relevant

| # | Question | Why |
|---|---|---|
| 15 | Several build variants or schemes with different ids? | Each shipped app is its own CAS ID, so the id becomes a build-time value rather than a constant. |
| 16 | Native ads wanted, and with a template or a custom layout? | The manual path means binding every required asset and honouring the ad choices placement. It is the one format where a half-done job is a policy problem. |
| 17 | Two screens that both need an interstitial? | The screen formats are static modules, one of each per app, so the loaded state is shared. Pacing has to be planned centrally. |
| 18 | Impression level data needed for analytics? | Changes what is wired at initialization. |
| 19 | A minimum interval between interstitials? | `InterstitialAd.setMinInterval` exists for it, and the timer is shared across instances while the value is per instance. |
| 20 | Should a rewarded ad fall back to an interstitial when there is no fill? | `RewardedAd.setExtraFillByInterstitialAd(true)`. The reward event still fires, which is a product decision rather than a technical one. |
| 21 | After a migration, do the reports have to stay comparable with the old ones? | Affects placement names and the impression level data wiring. |

## What the publisher does, not the agent

Registering the app in the CAS dashboard, deciding whether the audience is child-directed,
answering the Google Play data disclosure and the App Store privacy questions, writing the
App Tracking Transparency text, and getting access to closed beta networks. The agent
reminds and links, and does not decide.
