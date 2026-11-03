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

## Where to load, and where to show

The publisher knows their app; the agent knows what each format needs. Propose a placement
from this table, name the screen and the moment, and let the publisher correct it. An ad in
the wrong place costs more than no ad: it earns accidental clicks, which are invalid
traffic, and it is the first thing a user complains about.

| Format | Load | Show |
|---|---|---|
| Interstitial | In `onCreate()` of the screen that will show it, or with `isAutoloadEnabled`. One load is one impression, so reload after `onAdDismissed()`. | On a transition the user is already making: a level finished, a screen closed, a step completed. Never in the middle of a task, never on a tap that means something else. |
| Rewarded | Early, in `onCreate()` of the screen with the button, and again after every dismiss, so the button is never pressed on an empty cache. | Only from a button the user pressed, with the reward stated before the press. Grant the reward in `OnRewardEarnedListener`, never in `onAdDismissed()`: a user who closes the ad early earns nothing. Keep the button visible but disabled, or hidden, while no ad is ready. |
| Banner | The view loads itself once it is attached and `isAutoloadEnabled` is on. | In a fixed container at the top or the bottom, outside the scrolling content, and never over a button, a navigation bar or game controls. Reserve the height so the layout does not jump when the ad arrives. |
| App open | While the app is in the foreground, for the *next* foreground. | Only on a return to the app, or on a cold start from a loading screen while that screen is still up. Never once the user has reached app content. |
| Native | With the screen or the list that will hold it, ahead of the position where it appears. | Inside the app's own layout, marked as an ad, with the required assets present. In a list, far enough apart that two ads are never on screen together. |

**The transition waits for the ad, the ad does not race the transition.** Showing a full
screen ad and navigating in the same handler is the single most common way to get an
integration that looks right and shows nothing: the next screen covers the ad
immediately. Call show, and start the next activity from `onAdDismissed()`, with the same step repeated in
`onAdFailedToShow()` and when no ad is loaded, so the user is never stuck.

**Every format reloads after it is dismissed, not just the interstitial.** One load is one
impression. A rewarded ad that is not reloaded fails the next press with `NOT_READY`, and
the button looks broken. Gate the button on the loaded state so it is never pressed on an
empty cache.

Rules that hold for every format:

- Pause sound, video and gameplay before a full screen ad and resume on dismiss.
- A placement name is a reporting label, set it per position, not per format.
- One instance per placement. Two interstitials racing on one screen produce
  `ALREADY_DISPLAYED`, not two impressions.
- If the publisher asks for a placement that breaks one of these, say what it costs and
  propose the nearest one that does not. Then do what they decide.

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
bannerView.size = AdSize.getAdaptiveBannerInScreen(this)
bannerView.isAutoloadEnabled = true

adContainerView.removeAllViews()
adContainerView.addView(bannerView, LayoutParams(WRAP_CONTENT, WRAP_CONTENT))
```

Or from XML, then set `casId` in code.

Sizes: `getAdaptiveBannerInScreen(context)` for a full width banner, or
`getAdaptiveBanner(context, maxWidthDp)` for a narrower one. **Both take a context**; the
documentation page prints the call without it and that snippet does not compile. Also
`getInlineBanner`, `getSmartBanner`, `MEDIUM_RECTANGLE`, `LEADERBOARD` and `BANNER`.

**Reserve the height the ad size actually reports, not a round number.** An adaptive
banner is taller than 50dp on most screens, so a 50dp container clips it. Read
`adSize.height` and give the container that, or let the container wrap the view and
accept one layout pass.

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
