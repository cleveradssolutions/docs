# Android: ad formats

Canonical, in this repository, one page per format in [docs/Android/](../../../../../docs/Android):
[Banner-Ads.mdx](../../../../../docs/Android/Banner-Ads.mdx),
[Interstitial-Ads.mdx](../../../../../docs/Android/Interstitial-Ads.mdx),
[Rewarded-Ads.mdx](../../../../../docs/Android/Rewarded-Ads.mdx),
[App-Open-Ads.mdx](../../../../../docs/Android/App-Open-Ads.mdx),
[Load-Native-Ads.mdx](../../../../../docs/Android/Load-Native-Ads.mdx),
[Display-Native-Ad.mdx](../../../../../docs/Android/Display-Native-Ad.mdx).
Read the page for the format being added; the code below is the shape, not a substitute.
Working code for every format: `kotlinSample/`, `javaSample/` and `composeSample/` in
`https://github.com/cleveradssolutions/CAS-Android`. Read the sample for the format being
added, it is shorter than the documentation and always compiles.

Shared truths:

- One instance per placement, created with the CAS ID.
- `destroy()` belongs in the owner's `onDestroy()`, for every format. A leaked instance
  keeps loading, and the ad it loads is lost.
- Never call `load()` from a failure callback. The SDK refuses it, and on a bad network it
  turns into a request loop. Autoload or an explicit, rate-limited retry instead.
- `placement` is an optional label for reporting, not an ad unit id.

## Full screen formats: interstitial, rewarded, app open

Same shape for all three, `CASInterstitial`, `CASRewarded`, `CASAppOpen`:

```kotlin
private val interstitialAd = CASInterstitial(MyApplication.CAS_ID)

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    interstitialAd.contentCallback = adContentCallback
    interstitialAd.load(this)          // or isAutoloadEnabled = true
}

override fun onDestroy() {
    interstitialAd.destroy()
    super.onDestroy()
}
```

`ScreenAdContentCallback` carries `onAdLoaded`, `onAdFailedToLoad`, `onAdFailedToShow`,
`onAdShowed`, `onAdClicked` and `onAdDismissed`, all optional. Show with
`interstitialAd.show(this)`.

Rewarded ads need `OnRewardEarnedListener`. The reward is granted there and nowhere else,
not in `onAdDismissed`, because a user who closes the ad early earns nothing.

### Interstitial pacing

`minInterval` sets the minimum seconds between impressions; the countdown starts when any
interstitial is dismissed, across instances. A show attempt inside the interval fails with
`NOT_PASSED_INTERVAL`, which is expected behaviour, not an error to work around.
`restartInterval()` restarts it manually, for example after a rewarded ad.

Pause sound and gameplay before showing, and resume in `onAdDismissed`. Load well before
the moment of showing, so the user never waits.

### App open

Shown when the user foregrounds the app, with one hard limitation: on a cold start, show
it only from a loading screen, and only while the loading screen is still up. Once the
user is in app content, do not show it, an out-of-context full screen ad there is a bad
experience and a policy risk. Keep loading assets on a background thread while the ad is
on screen.

Do not show an app open ad to a user who has just returned from clicking one.

## Banner

```kotlin
bannerView = CASBannerView(this, MyApplication.CAS_ID)
bannerView.size = AdSize.getAdaptiveBanner(maxWidthDp)
bannerView.isAutoloadEnabled = true

adContainerView.removeAllViews()
adContainerView.addView(bannerView, LayoutParams(WRAP_CONTENT, WRAP_CONTENT))
```

Or from XML, then set `casId` in code.

Sizes: `getAdaptiveBanner(maxWidth)` is the default choice, plus `getInlineBanner`,
`getSmartBanner`, `MEDIUM_RECTANGLE`, `LEADERBOARD` and `BANNER`. Pick the size from the
container the publisher actually has; an ad view in a container that cannot measure it
shows nothing.

Banners refresh themselves; 30 seconds is the recommended `refreshInterval`. Destroy the
view with the screen, and in Compose destroy it in `onDispose`.

## Native

`CASNativeLoader(context, casId, callback)` with `NativeAdContentCallback`. Two rules that
are easy to miss:

- Keep every loaded `NativeAdContent` in memory until it is destroyed; a collected one
  cannot be shown or billed.
- `bindAdContent()` is a main thread call.

Layout and asset rules, including what may and may not be changed in the ad view, are on
the display page. Native ads have the strictest presentation requirements of the formats,
so follow that page rather than improvising a layout.

## Hardware acceleration

Activities that display ads need hardware acceleration enabled, which is the platform
default. If the publisher disabled it application-wide, re-enable it for the activities
that show ads.
