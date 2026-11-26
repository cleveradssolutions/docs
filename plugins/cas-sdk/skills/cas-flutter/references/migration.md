# Flutter: migrating from another mediation

CAS cannot run next to another mediation or ad network SDK: both native libraries are
linked, both initialize, and the user is asked for consent twice. The other SDK is
**replaced**, in the same change, or the work stops and the publisher decides.

Almost every Flutter migration comes from `google_mobile_ads`, so that is what the tables
name. An AppLovin or IronSource Flutter package is the same job with different symbols.

## What has to go

Search `lib/`, `android/` and `ios/`, never the build output, and remove **all** of it, not
only the dependency:

| Leftover | Where | Why it matters |
|---|---|---|
| The package | `pubspec.yaml`, and its `import` lines | An unused `google_mobile_ads` still ships its initializer and its native SDK. |
| The SDK initializer | `MobileAds.instance.initialize()` in `main()` | CAS initializes the networks. A second initializer is the classic broken migration. |
| Ad unit ids | `adUnitId:` constants, `TestIds.*`, the usual `kDebugMode ? TestIds.x : '<real>'` ternary | CAS has no per-placement ad unit in the app; the dashboard holds them, and test mode replaces the ternary. |
| Request configuration | `updateRequestConfiguration(RequestConfiguration(...))`, `tagForChildDirectedTreatment`, `testDeviceIds` | Becomes the `audience:` and `testDeviceIds:` arguments of `CASMobileAds.initialize`. |
| The Android app id | `android/app/src/main/AndroidManifest.xml`, `com.google.android.gms.ads.APPLICATION_ID` | **Replaced**, not deleted, when Google demand is used: the value has to be the one CAS generated. Delete it only while the app is not registered yet, and say so in the hand-over. |
| The iOS app id and plist keys | `ios/Runner/Info.plist`: `GADApplicationIdentifier`, `SKAdNetworkItems`, `GADDelayAppMeasurementInit` | `casconfig.rb` writes its own set. A hand-written `SKAdNetworkItems` left behind goes stale and is never updated. |
| Native ad factories | `GoogleMobileAdsPlugin.registerNativeAdFactory` in `MainActivity.kt`, `FLTGoogleMobileAdsPlugin registerNativeAdFactory` in `AppDelegate.swift`, and the layout XML and XIB they point at | CAS native ads are built in Dart, so this whole platform-side layer **disappears**. It is the largest single deletion in a Flutter migration. |
| The consent platform | `ConsentInformation`, `ConsentForm`, `ConsentRequestParameters` from `google_mobile_ads` | See the note below: this one is a decision, not a deletion. |
| The ad inspector | `MobileAds.instance.openAdInspector` and any debug menu entry | CAS has its own test ad screen instead. |

## The consent platform is the one real decision

A Google UMP integration is **not** a third-party CMP to keep. The built-in CAS consent
flow is itself driven by UMP, so keeping the app's own `ConsentInformation` means two flows
competing for the same form. Remove it and let `showConsentFormIfRequired: true` run,
unless the publisher uses a genuinely different platform — Didomi, Usercentrics — that they
intend to keep. Then the CAS flow stays off and their decision is applied before
`initialize`.

## Mapping

| google_mobile_ads | CAS |
|---|---|
| `MobileAds.instance.initialize()` | `CASMobileAds.initialize(casId: ...)` in `main()`, before `runApp` |
| `InterstitialAd.load(adUnitId:, request:, adLoadCallback:)` | `CASInterstitial.createAndLoad(...)` — creates **and** loads |
| `InterstitialAdLoadCallback(onAdLoaded:, onAdFailedToLoad:)` | the content callback's `onAdLoaded` / `onAdFailedToLoad` |
| `FullScreenContentCallback.onAdDismissedFullScreenContent` | `onAdDismissed` |
| `onAdFailedToShowFullScreenContent` | `onAdFailedToShow` |
| `ad.show()`, then `ad.dispose()` and null the field, then load the next | `show()`, then `autoReload: true` keeps the next one coming |
| `BannerAd(size:, adUnitId:, listener:)` + `AdWidget(ad: ...)` | `CASBanner.createAndLoad(size: AdSize.banner, autoReload: true)` + `CASWidget(...)` |
| `AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(width)` | `AdSize.getAdaptiveBanner(...)` |
| `RewardedAd.show(onUserEarnedReward: (ad, reward) { })` | `show()`, with the reward granted in `onUserEarnedReward` |
| `AppOpenAd.load(...)` and an `AppStateEventNotifier` to pick the moment | `CASAppOpen.createAndLoad(...)` |
| `NativeAd(factoryId: 'listTile')` + a platform-side factory | `CASNativeContent.load(...)` + `CASWidget`, all in Dart |
| `ad.dispose()` | `dispose()`, still from the widget's `dispose()` |

One shape survives the migration unchanged: **the `State` owns the instance.** Create it in
`initState`, dispose it in `dispose`, never in `build`. If the old code got that right, keep
the structure and swap the types.

## Order

1. Remove, in one commit, everything in the first table except the app id decision.
2. Add the package, the Gradle plugin and the iOS configuration, see
   [setup-plugin.md](setup-plugin.md).
3. Add `CASMobileAds.initialize` in `main()`, see [initialize.md](initialize.md), and
   settle consent, see [consent-privacy.md](consent-privacy.md).
4. Port the formats one by one, see [ad-formats.md](ad-formats.md), keeping the placements
   the app already had unless they break a placement rule.
5. Build **both** platforms, then verify on test ads, see
   [verify-and-release.md](verify-and-release.md).

Keep the app's own behaviour. A migration that changes when ads appear is a product change
the publisher did not ask for.

## What the publisher has to do after

Register the app in the CAS dashboard, take the AdMob App ID from there if Google demand is
used, and remove the old mediation account from the build and release notes. Their old ad
unit ids are not reused anywhere.
