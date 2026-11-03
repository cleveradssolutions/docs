# Android: migrating from another mediation

CAS cannot run next to another mediation or ad network SDK: the native libraries collide,
both initialize, and the user is asked for consent twice. The other SDK is **replaced**,
in the same change, or the work stops and the publisher decides.

Verified against Google's own AdMob sample, `kotlin/admob/InterstitialExample` in
`googleads/googleads-mobile-android-examples`. The list below is what that migration
actually touched.

## What has to go

Search the sources, never `build/`, and remove **all** of it, not only the dependency:

| Leftover | Where | Why it matters |
|---|---|---|
| The SDK dependency | `app/build.gradle(.kts)`, version catalog | An unused AdMob dependency still ships its initializer. |
| The consent platform dependency | `com.google.android.ump`, Didomi, Usercentrics | See the note below, this one is a decision, not a deletion. |
| The ad network app id | `AndroidManifest.xml`, for AdMob `com.google.android.gms.ads.APPLICATION_ID` | **Replaced**, not deleted, when Google demand is used: the value has to be the one CAS generated. Delete it only while the app is not registered yet, and say so in the handover. |
| The SDK initializer | `MobileAds.initialize`, `AppLovinSdk.initializeSdk`, `IronSource.init` | CAS initializes the networks. A second initializer is the classic broken migration. |
| Ad unit ids | constants like `AD_UNIT_ID` | CAS has no per-placement ad unit in the app; the dashboard holds them. |
| Test device ids | `TEST_DEVICE_HASHED_ID`, `RequestConfiguration` | Replaced by test ad mode bound to the debug build. |
| Format classes and callbacks | `InterstitialAd`, `InterstitialAdLoadCallback`, `FullScreenContentCallback`, `AdRequest` | Mapped onto the CAS format, see the table below. |
| Request configuration | `MobileAds.setRequestConfiguration`, child-directed and content rating flags | Becomes `CAS.settings` and the audience choice before initialization. |
| The ad inspector, mediation test suite | menu entries, debug screens | CAS has its own test ad screen instead. |

## The consent platform is the one real decision

A Google UMP integration is **not** a third-party CMP to keep. The built-in CAS consent
flow is itself driven by UMP, so keeping the app's own UMP manager means two flows
competing for the same form. Remove it and let the CAS flow run, unless the publisher
uses a genuinely different platform, Didomi or Usercentrics, that they intend to keep.

After the migration the CAS test ad screen shows `Active CMP: Google UMP`, which is the
built-in flow working, not the old code surviving.

## Mapping

| AdMob | CAS |
|---|---|
| `InterstitialAd.load(ctx, unitId, AdRequest, callback)` | `CASInterstitial(casId).load(activity)` |
| `InterstitialAdLoadCallback.onAdLoaded/onAdFailedToLoad` | `ScreenAdContentCallback.onAdLoaded/onAdFailedToLoad` |
| `FullScreenContentCallback.onAdDismissedFullScreenContent` | `onAdDismissed` |
| `onAdFailedToShowFullScreenContent` | `onAdFailedToShow` |
| `interstitialAd.show(activity)`, then null the reference | `interstitialAd.show(activity)`, then `load()` again on dismiss |
| `AdView` with `AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize` | `CASBannerView` with `AdSize.getAdaptiveBannerInScreen(context)` |
| `RewardedAd.show(activity) { rewardItem -> }` | `CASRewarded.show(activity) { }`, reward granted in the listener |
| `MobileAds.initialize` in an Activity | `CAS.buildManager()...build(this)` in `Application.onCreate()` |

A sample usually has no `Application` class, because AdMob initializes from the Activity.
Add one and register it as `android:name` in the manifest.

## Order

1. Remove, in one commit, everything in the first table except the app id decision.
2. Add the CAS plugin, the repositories and the solution, see
   [setup-gradle.md](setup-gradle.md).
3. Add the `Application` class and initialization, see [initialize.md](initialize.md).
4. Port the formats one by one, see [ad-formats.md](ad-formats.md), keeping the
   placements the app already had unless they break a placement rule.
5. Build, then verify on test ads, see [verify-and-release.md](verify-and-release.md).

Keep the app's own behaviour. In the sample the retry button showed the ad and the game
restarted afterwards; it still does. A migration that changes when ads appear is a
product change the publisher did not ask for.

## What the publisher has to do after

Register the app in the CAS dashboard, take the AdMob App ID from there if Google demand
is used, and remove the old mediation account from the build and release notes. Their old
ad unit ids are not reused anywhere.
