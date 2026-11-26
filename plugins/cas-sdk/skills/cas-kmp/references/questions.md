# Kotlin Multiplatform: research the project, then ask

The shape of a question is in [question-format.md](question-format.md), the same in every
CAS guide. This file is the Kotlin Multiplatform questions only; every platform has its own,
because the answers that matter are not the same.

## Research, read-only

| What | Where to look |
|---|---|
| Not a Kotlin Multiplatform app at all | No `kotlin("multiplatform")` and no `commonMain`. A plain Android module belongs to the cas-android skill, an Xcode project alone to cas-ios; `pubspec.yaml`, `react-native` in `package.json`, a Unity `ProjectSettings/`, a Cordova `config.xml` or an Unreal `.uproject` each belong to their own CAS plugin. Stop and say which one. |
| **Which targets** | The `kotlin { }` block. CAS supports `iosArm64` and `iosSimulatorArm64` only — there is no x86_64 simulator target, so **an Intel Mac cannot run the simulator build**. A `jvm`, `js`, `wasmJs` or desktop target takes no ads, and shared ad code must still compile there. |
| **Compose Multiplatform or native UI** | `androidx.compose` in `commonMain`, a `composeApp` module, a `MainViewController` wrapping `ComposeUIViewController`. Banner and Native are composables; with platform-native UI they cannot be placed from shared code. Settle this before promising those two formats. |
| Kotlin and Compose versions | The version catalog or the root build script. The library has a minimum for both, on the setup page. |
| Module layout | Which module holds the Android target, where `iosApp/` and its `.xcodeproj` are, and the Gradle path of the shared module — `:shared` in the documentation, often something else in a real project. The run script phase and the framework `baseName` follow it. |
| Application id and App Store id | `applicationId` in the Android target's `defaultConfig`, `PRODUCT_BUNDLE_IDENTIFIER` in the Xcode project. These are two different CAS IDs, and on iOS the CAS ID is the App Store id rather than the bundle id. |
| CAS already present | `com.cleveradssolutions:cas-kmp` in `commonMain.dependencies`, `CASMobileAds.initialize` in the code, a `cas { }` block, the CAS Swift Package in the Xcode project, `CASAIAppIdentifier` in `Info.plist`. Then: which solution is switched on **per platform**, and whether `forceTestAds` is bound to the build. |
| Other ad SDKs | `com.google.android.gms:play-services-ads`, AppLovin, IronSource or Unity Ads dependencies on the Android side; a Google Mobile Ads or MAX pod or package on the iOS side; their initializers in `androidMain`/`iosMain`. Two mediation SDKs cannot run side by side. |
| Not an ad SDK, do not touch | Attribution and analytics (AppsFlyer, Adjust, Firebase), in-app purchases, the consent platform. If unsure whether something is an ad SDK, ask. |
| Native leftovers of an old integration | Ad network keys in `AndroidManifest.xml` and `Info.plist`, `GADApplicationIdentifier`, `SKAdNetworkItems`, hand-added repositories, hand-added adapter dependencies. A migration has to remove these, and the CAS tooling writes its own. |
| Consent platform | A UMP dependency, Didomi, Usercentrics, or none. |
| Ad formats used today | The calls of an existing ad SDK, on either platform. They are the formats to reproduce, and they may currently exist on one platform only. |
| Where ads would go | The navigation of the shared UI: which screens compose and leave composition, which transitions exist for a full screen ad, where a banner could sit outside the scroll. A `remember`ed ad instance and a `DisposableEffect` are written against this. |
| The Xcode project's state | Whether the run script phase that embeds the Kotlin framework exists, whether **Other Linker Flags** carries `-ObjC`, and whether `casconfig.rb` has been run. An integration can be complete in Kotlin and dead in Xcode. |
| Version control | `git status`. An uncommitted tree before an integration is worth naming. |

Show a short **Found** block first: the targets, whether the UI is Compose Multiplatform,
Kotlin and Compose versions, the module that holds the Android target, application id and
App Store id, what is already installed, which ad SDK was found. Then the questions.

## Questions

Ask only those that still apply. The last column is a rule for the agent, not part of what
the publisher sees: it says how to pick the Recommended option.

| # | Question | Options | Recommended is |
|---|---|---|---|
| 1 | Which platforms does this app ship? | A. Android and iOS; B. Android only; C. iOS only | What the targets and CI show. Everything else follows: a single-platform app needs one CAS ID and one platform setup. A non-mobile target in the same project takes no ads either way. |
| 2 | Is the UI Compose Multiplatform, or native per platform? | A. the one the research found; B. the other one | A, always. This is a fact about the project, not a preference. On native UI, say plainly that Banner and Native cannot be placed from shared code, and that those two formats belong to the cas-android and cas-ios skills while the full screen formats still work from `commonMain`. |
| 3 | Which CAS ID on Android? | A. the application id found in the research; B. the demo id, the app is not on Google Play yet | A when an application id is set and the app is registered, else B. |
| 4 | Which CAS ID on iOS? | A. the numeric App Store id, digits only; B. the demo id, the app is not on the App Store yet | **Not the bundle identifier.** B unless the publisher gives the App Store id. A real id only the publisher has: `none`. |
| 5 | Is the app already registered in CAS? | A. No, or do not know; B. Yes | A. Until CAS registers the app nothing serves. The integration is still done, on test ads, and the publisher's CAS contact registers it. |
| 6 | New integration, or a migration from another mediation? | A. New; B. Migration from the SDK found in the research; C. Migration from another one | B when the research found another mediation SDK, else A. CAS cannot run next to one, so B means removing it — dependency, pods, native keys and all — in the same change, on both platforms. |
| 7 | Which ad formats, and on which screens? | A. the formats the existing ad code uses, on the same screens; B. banner, interstitial, rewarded; C. another combination | A when existing ad code was found, else B. Follow up for the screen and the moment of each one, see the placement rules in `ad-formats.md`. |
| 8 | What is the audience? | A. General or mixed; B. Child-directed; C. VPN or proxy app | A. Most apps are general. If nothing in the project says otherwise and the publisher does not know, count it as A. |
| 9 | Only when 8 is B: Google Play Families, and is the iOS app in the Apple Kids Category? | A. Families on Android, not in the Kids Category; B. Both | A. On the Kids Category, stop and point at the Kids Category page: the Families solution is not intended for it, and the choice has to be agreed with the account manager. |
| 10 | Which solution? | A. Optimal; B. Families; C. VPN Compliant; D. a hand-picked adapter list | Follows from questions 8 and 9, and is **set twice**: a `cas { }` flag on Android and a Swift Package product on iOS. D only when the publisher asks for it. |
| 11 | Consent: the built-in CAS flow, or your own platform? | A. The built-in flow, with `showConsentFormIfRequired = true`; B. The consent platform found in the research | B when a consent platform was found, else A. On B the built-in flow stays off and the decision is applied before initialization. |
| 12 | Which AdMob App ID? | A. the one already in the native projects; B. the one from the CAS dashboard | `none` unless one is already present and came from CAS. It must be the identifier CAS generated; the publisher's own AdMob App ID is not valid. Not asked when Google demand is not used. |
| 13 | Where will this be verified? | A. a connected device per platform; B. an emulator and a simulator; C. the publisher runs it themselves | A when a device is available, else B — and B needs an Apple Silicon Mac, because there is no x86_64 simulator target. The build under test serves test ads either way; that is a rule, not an option. |
| 14 | The tree has uncommitted changes. | A. Go on; B. Stop, I commit first | A. Not asked at all when the tree is clean. |

## Follow-ups, only when relevant

| # | Question | Why |
|---|---|---|
| 15 | Several build variants or schemes with different ids? | Each shipped app is its own CAS ID, so the `actual val casId` becomes a build-time value rather than a literal — `BuildConfig.APPLICATION_ID` on Android is the documented way. |
| 16 | Native ads wanted, and with a template or a custom layout? | `CASNativeAdTemplate` is a few parameters; the custom path means rendering every asset through the `NativeAdScope` composables and honouring the AdChoices placement. It is the one format where a half-done job is a policy problem. |
| 17 | Two screens that both need an interstitial? | Instances are real objects here, so two are possible — but the minimum-interval timer is shared across all interstitial instances while the value is per instance. Plan the pacing centrally, or share one instance in a shared object. |
| 18 | Impression level data needed for analytics? | `impressionCallback` per ad instance, and `CASAdContentInfo` carries the revenue. Changes what is wired at ad creation. |
| 19 | A minimum interval between interstitials? | `interstitialAd.minInterval` exists for it, and `restartInterval()` is how a rewarded or app open impression resets it. |
| 20 | Server-side reward verification? | `rewardedAd.serverSideVerificationData` carries the user identifier, and the publisher's backend has to be ready for the callback. |
| 21 | Does an Intel Mac have to build this? | There is no x86_64 simulator target. It is a hardware answer, and it is better given before the verification step than during it. |
| 22 | After a migration, do the reports have to stay comparable with the old ones? | Affects placement names and the impression level data wiring. |

## What the publisher does, not the agent

Registering the app in the CAS dashboard, deciding whether the audience is child-directed,
answering the Google Play data disclosure and the App Store privacy questions, writing the
App Tracking Transparency text, and getting access to closed beta networks. The agent
reminds and links, and does not decide.
