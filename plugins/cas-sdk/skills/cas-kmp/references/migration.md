# Kotlin Multiplatform: migrating from another mediation

CAS cannot run next to another mediation or ad network SDK: both native libraries are
linked, both initialize, and the user is asked for consent twice. The other SDK is
**replaced**, in the same change, or the work stops and the publisher decides.

Kotlin Multiplatform migrations have a shape of their own. There is no dominant
cross-platform ad package here, so what a project usually has is **two native integrations
with a hand-written `expect`/`actual` layer over them**: the Google Mobile Ads Android SDK
in `androidMain`, GoogleMobileAds in the Xcode project, and the project's own interface in
`commonMain` holding them together.

That layer is the thing being replaced. **CAS already is the `expect` API**, so the
project's own abstraction is deleted rather than ported — and that is usually more code
than the ad calls themselves.

## What has to go

Search every source set and both platform projects:

| Leftover | Where | Why it matters |
|---|---|---|
| The Android SDK dependency | `com.google.android.gms:play-services-ads` in the Android target's dependencies, or a version catalog entry | An unused dependency still ships its initializer. |
| The iOS framework | the GoogleMobileAds pod in the `Podfile`, or its Swift Package in the Xcode project | Two ad SDKs linked into one binary is where the duplicate-symbol and double-consent problems start. |
| A community KMP ad wrapper | `commonMain.dependencies` | Same rule: one mediation SDK. |
| The project's own `expect`/`actual` ad layer | `commonMain` interface plus its `androidMain` and `iosMain` actuals | **This is the deletion that matters.** Keeping it as a thin shim over CAS is a choice, not a requirement; duplicating an abstraction that the plugin already provides is not. |
| The SDK initializers | `MobileAds.initialize(context)` in `MainActivity` or an `Application`, and the iOS `GADMobileAds.sharedInstance().start()` in the `AppDelegate` or `@main` struct | CAS initializes the networks, from `commonMain`. Two initializers is the classic broken migration. |
| Ad unit ids | constants in either source set, and any test/production ternary | CAS has no per-placement ad unit in the app; the dashboard holds them, and `forceTestAds` replaces the ternary. |
| Request configuration | `RequestConfiguration` with child-directed flags and test device ids | Becomes the `targetAudience` and `testDeviceIds` arguments of `CASMobileAds.initialize`. |
| The Android app id | `AndroidManifest.xml`, `com.google.android.gms.ads.APPLICATION_ID` | **Replaced**, not deleted, when Google demand is used: the value has to be the one CAS generated. Delete it only while the app is not registered yet, and say so in the hand-over. |
| The iOS app id and plist keys | `iosApp/Info.plist`: `GADApplicationIdentifier`, `SKAdNetworkItems`, `GADDelayAppMeasurementInit` | `casconfig.rb` writes its own set. A hand-written `SKAdNetworkItems` left behind goes stale and is never updated. |
| Platform ad views | an `AndroidView` wrapping `AdView`, and a `UIKitView` wrapping `GADBannerView` | `CASBannerAd` is a composable on both platforms, so both wrappers go. |
| The consent platform | `com.google.android.ump` on Android and `UserMessagingPlatform` on iOS | See the note below: this one is a decision, not a deletion. |

## The consent platform is the one real decision

A Google UMP integration is **not** a third-party CMP to keep. The built-in CAS consent
flow is itself driven by UMP, so keeping the app's own UMP code — on either platform —
means two flows competing for the same form. Remove it and let
`showConsentFormIfRequired = true` run, unless the publisher uses a genuinely different
platform, Didomi or Usercentrics, that they intend to keep. Then the CAS flow stays off and
their decision is applied before `initialize`.

A per-platform consent implementation is also the most likely place where the two platforms
have quietly drifted apart. Read both before deleting either.

## Mapping

| Google Mobile Ads, per platform | CAS, in `commonMain` |
|---|---|
| `MobileAds.initialize(context)` + `GADMobileAds...start()` | one `CASMobileAds.initialize(context, casId, ...)`, with `context = null` on iOS |
| two ad unit id constants, one per platform | one `expect val casId: String`, actualised per platform |
| `InterstitialAd.load(context, unitId, AdRequest, callback)` | `CASInterstitialAd(casId = casId, autoLoad = true, callback = ...)` |
| `InterstitialAdLoadCallback.onAdLoaded` / `onAdFailedToLoad` | `CASScreenAdContentCallback.onAdLoaded` / `onAdFailedToLoad` |
| `FullScreenContentCallback.onAdDismissedFullScreenContent` | `onAdDismissed` |
| `onAdFailedToShowFullScreenContent` | `onAdFailedToShow` |
| `interstitialAd.show(activity)`, then null the reference | `ad.show()`, with `autoLoad = true` keeping the next one coming |
| `RewardedAd.show(activity) { rewardItem -> }` | `rewardedAd.show(rewardEarnedListener = CASRewardEarnedListener { })` |
| `AdView` in an `AndroidView`, `GADBannerView` in a `UIKitView` | `CASBannerAdContent` + the `CASBannerAd` composable |
| `NativeAd` with a platform layout each | `CASNativeAdLoader` + `CASNativeAd` or `CASNativeAdTemplate` |

**Where the old code is Compose, the lifetime rules change.** A `remember`ed
`CASInterstitialAd` with `DisposableEffect { onDispose { destroy() } }` replaces whatever
held the old instance — see [ad-formats.md](ad-formats.md). An ad object created in a
composable without `remember` loads an ad per recomposition, and a migration is exactly
when that gets introduced.

## When the UI is not Compose Multiplatform

Then the migration is partial by nature: the three full screen formats move into
`commonMain`, and Banner and Native stay in native code because CAS exposes them only as
composables. Say so before starting, and say which of the two native skills owns the
remaining half.

## Order

1. Remove, in one commit, everything in the first table except the app id decision — both
   platforms at once, because half a removal still links two SDKs.
2. Add the library, the CAS Gradle plugin, the Maven repositories and the Swift Package,
   see [setup-plugin.md](setup-plugin.md).
3. Add `CASMobileAds.initialize` and the `expect val casId`, see
   [initialize.md](initialize.md), and settle consent, see
   [consent-privacy.md](consent-privacy.md).
4. Port the formats one by one, see [ad-formats.md](ad-formats.md), keeping the placements
   the app already had unless they break a placement rule.
5. Build **and run** both platforms, then verify on test ads, see
   [verify-and-release.md](verify-and-release.md). A shared module that compiles proves
   nothing about the Xcode link step.

Keep the app's own behaviour. A migration that changes when ads appear is a product change
the publisher did not ask for.

## What the publisher has to do after

Register the app in the CAS dashboard, take the AdMob App ID from there if Google demand is
used, and remove the old mediation account from the build and release notes. Their old ad
unit ids are not reused anywhere.
