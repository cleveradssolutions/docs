# React Native: migrating from another mediation

CAS cannot run next to another mediation or ad network SDK: both native libraries are
linked, both initialize, and the user is asked for consent twice. The other SDK is
**replaced**, in the same change, or the work stops and the publisher decides.

Almost every React Native migration comes from `react-native-google-mobile-ads`, so that is
what the tables name. `react-native-applovin-max`, `ironsource-mediation` or
`react-native-fbads` are the same job with different symbols.

## What has to go

Search the JavaScript, the Expo config and — on a bare project — `android/` and `ios/`.
Remove **all** of it, not only the npm package:

| Leftover | Where | Why it matters |
|---|---|---|
| The package | `package.json`, the lockfile, and every `import` | An unused ad package still ships its native SDK and its initializer. |
| The SDK initializer | `mobileAds().initialize()` in the root component or `index.js` | CAS initializes the networks. A second initializer is the classic broken migration. |
| Ad unit ids | `adUnitId` constants, `TestIds.*`, the usual `__DEV__ ? TestIds.INTERSTITIAL : '<real>'` ternary | CAS has no per-placement ad unit in the app; the dashboard holds them, and `forceTestAds: __DEV__` replaces the ternary. |
| Request configuration | `setRequestConfiguration({ tagForChildDirectedTreatment, testDeviceIdentifiers })` | Becomes the `targetAudience` and `testDeviceIds` options of `CASMobileAds.initialize`. |
| The package's own config block | the `react-native-google-mobile-ads` key in `app.json` or `app.config.*`, with `android_app_id` and `ios_app_id` | Expo projects keep the CAS plugin entry here instead; leaving the old block makes the next prebuild configure both SDKs. |
| The Android app id | `android/app/src/main/AndroidManifest.xml`, `com.google.android.gms.ads.APPLICATION_ID` | **Replaced**, not deleted, when Google demand is used: the value has to be the one CAS generated. Delete it only while the app is not registered yet, and say so in the hand-over. |
| The iOS app id and plist keys | the app target's `Info.plist`: `GADApplicationIdentifier`, `SKAdNetworkItems`, `GADDelayAppMeasurementInit` | The CAS build phase writes its own set. A hand-written `SKAdNetworkItems` left behind goes stale and is never updated. Remember the real target is named after the project, not `Runner`. |
| The consent platform | `AdsConsent`, `AdsConsentStatus` from the same package | See the note below: this one is a decision, not a deletion. |
| The ad inspector | `mobileAds().openAdInspector()` and any debug menu entry | CAS has its own test ad screen instead. |

## The consent platform is the one real decision

A Google UMP integration — `AdsConsent` in this package — is **not** a third-party CMP to
keep. The built-in CAS consent flow is itself driven by UMP, so keeping the app's own
consent code means two flows competing for the same form. Remove it and let
`showConsentFormIfRequired: true` run, unless the publisher uses a genuinely different
platform — Didomi, Usercentrics — that they intend to keep. Then the CAS flow stays off and
their decision is applied before `initialize`.

## Mapping

The shapes do not correspond one to one, and that is the point of the table:

| react-native-google-mobile-ads | CAS |
|---|---|
| `mobileAds().initialize()` | `CASMobileAds.initialize(casId, { ... })`, once, from the root effect |
| `InterstitialAd.createForAdRequest(adUnitId)` | nothing — there is no instance to create |
| `interstitial.load()` | `InterstitialAd.loadAd()`, on the **static module** |
| `interstitial.show()` | `InterstitialAd.showAd()` — note `showAd`, not `show` |
| `addAdEventListener(AdEventType.LOADED, ...)` | `InterstitialAd.addAdLoadedEventListener(...)` |
| `AdEventType.ERROR` | `addAdFailedToLoadEventListener` and `addAdFailedToShowEventListener`, which are separate here |
| `AdEventType.CLOSED` | `addAdDismissedEventListener` |
| `RewardedAdEventType.EARNED_REWARD` | `RewardedAd.addAdUserEarnRewardEventListener` |
| `<BannerAd unitId={} size={BannerAdSize.ANCHORED_ADAPTIVE_BANNER} />` | `<BannerAdView size={BannerAdSize.ADAPTIVE} />`, with `onAdLoadedCallback` and friends as props |
| `AppOpenAd` plus an `AppState` listener to pick the moment | `AppOpenAd.setAutoshowEnabled(true)` |
| one instance per ad unit id | **one static module per format, per app** |

> **The last row is the migration's real work.** The old package gives an instance per ad
> unit, so two screens could each hold their own interstitial. CAS React Native has one
> `InterstitialAd` module for the whole app, with one loaded state. Two screens that each
> loaded their own now share it, and the pacing has to be planned centrally. Find every
> `createForAdRequest` in the project before promising the migration is mechanical.

What survives unchanged: **every listener is removed by the effect that added it.** If the
old code got that right, keep the structure and swap the calls; if it did not, this is the
moment to fix it, because duplicate impressions and duplicate rewards look the same in both
SDKs.

## Order

1. Remove, in one commit, everything in the first table except the app id decision.
2. Add the package and the native side — Expo config plugin, or the bare setup — see
   [setup-plugin.md](setup-plugin.md).
3. Add `CASMobileAds.initialize`, see [initialize.md](initialize.md), and settle consent,
   see [consent-privacy.md](consent-privacy.md).
4. Port the formats one by one, see [ad-formats.md](ad-formats.md), keeping the placements
   the app already had unless they break a placement rule.
5. Rebuild **both** platforms — not a Metro reload — then verify on test ads, see
   [verify-and-release.md](verify-and-release.md).

On Expo, step 1 and step 2 both touch the config, and `npx expo prebuild` has to run
afterwards. Confirm the generated `android/app/build.gradle` has the CAS plugin and no
trace of the old one.

Keep the app's own behaviour. A migration that changes when ads appear is a product change
the publisher did not ask for.

## What the publisher has to do after

Register the app in the CAS dashboard, take the AdMob App ID from there if Google demand is
used, and remove the old mediation account from the build and release notes. Their old ad
unit ids are not reused anywhere.
