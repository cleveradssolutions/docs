# Kotlin Multiplatform: ad formats

Canonical, one page per format in [docs/KMP/](../../../../../docs/KMP):
[Banner-Ads.mdx](../../../../../docs/KMP/Banner-Ads.mdx),
[Interstitial-Ads.mdx](../../../../../docs/KMP/Interstitial-Ads.mdx),
[Rewarded-Ads.mdx](../../../../../docs/KMP/Rewarded-Ads.mdx),
[App-Open-Ads.mdx](../../../../../docs/KMP/App-Open-Ads.mdx),
[Native-Ads.mdx](../../../../../docs/KMP/Native-Ads.mdx).
Read the page for the format being added. **The notes below are the shape of the flow, not
an API reference.**

Every format lives in `commonMain`. Banner and Native need **Compose Multiplatform**,
because the only documented way to display them is a composable; the three full screen
formats need no UI at all.

## The three full screen formats

They are real instances with a lifetime — unlike React Native's static modules — and they
share one callback interface and one set of properties.

| What | The actual API |
|---|---|
| Import | `com.cleveradssolutions.plugin.kmp.CASInterstitialAd`, `CASRewardedAd`, `CASAppOpenAd` |
| Create | `CASInterstitialAd(casId = ..., autoLoad = true, autoShow = false, callback = ...)` |
| Callback | `CASScreenAdContentCallback`, every method with a default empty body |
| Load | `ad.load()`, or `autoLoad = true` / `ad.isAutoLoadEnabled = true` |
| Ready check | `ad.isLoaded` |
| Show | `ad.show()` |
| Auto show | `autoShow = true` at creation, or `ad.isAutoShowEnabled = true` |
| Reward | `rewardedAd.show(rewardEarnedListener = CASRewardEarnedListener { ad -> ... })` |
| Impressions | `ad.impressionCallback = CASAdImpressionCallback { ad -> ... }` |
| Pacing | `interstitialAd.minInterval = 30`, `interstitialAd.restartInterval()` |
| Placement | `ad.placement = "LevelComplete"`, 100 characters at most |
| Server-side reward | `rewardedAd.serverSideVerificationData = "user-123"` |
| Release | `ad.destroy()` |

The callback methods are `onAdLoaded(ad: CASAdContentInfo)`,
`onAdFailedToLoad(format: CASAdFormat, error: CASError)`,
`onAdFailedToShow(format, error)`, `onAdShowed(ad)`, `onAdClicked(ad)` and
`onAdDismissed(ad)`. All of them are invoked on the main thread.

With `autoLoad = true` a new ad loads after each impression and failures are retried
automatically. Manual mode is for a single ad per screen on screens that change often. The
lifecycle diagram on the interstitial page is worth reading once.

### The Compose lifetime, which is the thing to get right

```kotlin
val interstitialAd = remember {
    CASInterstitialAd(casId = casId, autoLoad = true)
}

DisposableEffect(interstitialAd) {
    onDispose { interstitialAd.destroy() }
}
```

Without `remember`, every recomposition creates a new instance, each one loading its own ad
and leaking the previous. Without `onDispose { destroy() }`, an instance outlives its screen
and keeps refreshing. **Those two lines are the whole lifetime contract**, and they are the
most common Kotlin Multiplatform specific bug in a CAS integration.

The exception is deliberate: an instance kept in a shared object for reuse across screens
is **not** destroyed on dispose, which is how a loaded ad shows without delay on the next
screen. Decide which of the two it is per format, and write it down.

### Reward

`show()` on a rewarded ad requires a `CASRewardEarnedListener`, and it is invoked only when
the user has watched enough. **That is the only correct place to grant a reward** — never
`onAdDismissed`, because a user who closes early earns nothing. If the ad was not ready,
`onAdFailedToShow` fires and the reward listener does not.

A rewarded impression does **not** reset the interstitial interval. Call
`interstitialAd.restartInterval()` from the rewarded ad's `onAdDismissed` if the publisher
wants the interval counted from there, and the same for an app open ad.

Rewarded ads stay available inside the trial ad-free interval, so reward mechanics work from
the first launch while other formats are suppressed.

## Banner

`CASBannerAdContent` loads and owns the ad in `commonMain`; the `CASBannerAd` composable
displays it. Two objects, on purpose: the content survives recomposition and the composable
does not.

| What | The actual API |
|---|---|
| Content | `CASBannerAdContent(casId, size, autoLoad, refreshInterval, placement, callback)` |
| Composable | `CASBannerAd(adContent = ..., modifier = ...)` from `...kmp.composable` |
| Sizes | `CASBannerSize.BANNER` (320x50), `.LEADERBOARD` (728x90), `.MEDIUM_RECTANGLE` (300x250) |
| Adaptive | `CASBannerSize.getSmartBanner()`, `.getAdaptiveBanner(maxWidthDP)`, `.getInlineBanner(width, maxHeight)` |
| Callback | `CASBannerAdContentCallback`: `onBannerAdLoaded(info)`, `onBannerAdFailed(error)`, `onBannerAdClicked(info)`, `onBannerAdImpression(info)` |
| Refresh | `refreshInterval = 30` seconds, `0` disables it |
| Rest | `load()`, `isLoaded`, `impressionCallback`, `destroy()` |

Three constraints that are not obvious:

- **`CASBannerAdContent` must be created and used on the main thread.** Inside a composable
  that is automatic; from a view model or a coroutine it is not.
- **One content instance can be shown by one `CASBannerAd` at a time.** Banners on several
  screens at once means one content instance per screen.
- Until the ad loads the composable renders nothing visible, so it is safe to place from the
  start — which is also how the layout space is reserved.

`getAdaptiveBanner(maxWidthDP = maxWidth.value.toInt())` inside `BoxWithConstraints` is the
documented way to fill the available width, and the one worth using by default.

## Native

Three pieces: `CASNativeAdLoader` requests, `CASNativeAdContent` holds the assets of one
loaded ad, and either `CASNativeAd` with your own layout or `CASNativeAdTemplate` renders it.

| What | The actual API |
|---|---|
| Loader | `CASNativeAdLoader(casId, adChoicesPlacement, startVideoMuted, placement, callback)` |
| Request | `loader.load()`, or `loader.load(maxNumberOfAds = 5)` for several at once |
| Callback | `CASNativeAdContentCallback`: `onNativeAdLoaded(ad)` — the only one worth overriding always — plus `onNativeAdFailedToLoad(error)`, `onNativeAdFailedToShow(ad, error)`, `onNativeAdClicked(ad)` |
| AdChoices | `CASNativeAdChoicesPlacement.TOP_RIGHT` and its siblings, chosen at the loader |
| Video | `startVideoMuted`, enabled by default |
| Template | `CASNativeAdTemplate(adContent, maxWidth, maxHeight)` — both sizes required, they pick the layout |
| Template styling | component colours and `CASNativeTemplateFontStyle` parameters |
| Custom layout | `CASNativeAd(adContent, modifier) { ... }` with the `NativeAdScope` composables |
| Assets | `AdHeadlineText()`, `AdBodyText()`, `AdCallToAction()`, `AdIconImage()`, `AdMedia()`, `AdChoices()`, `AdAdvertiserText()`, `AdLabelText()`, `AdPriceText()`, `AdStoreText()`, `AdStarRating()`, `AdReviewCountText()` |
| Release | `adContent.destroy()`, per loaded content |

Two rules carry a policy consequence:

> **Render only the `NativeAdScope` composables inside `CASNativeAd`.** Drawing the asset
> values with your own `Text` or `Image` prevents the SDK from registering clicks and
> impressions, which breaks reporting and can be treated as invalid traffic. The assets are
> readable as properties — `headline`, `body`, `callToAction`, `advertiser`, `price`,
> `store`, `starRating`, `reviewCount`, `adLabel` — and reading them is for deciding what to
> lay out, not for drawing it.

> **Every asset except `contentInfo` can be `null`,** because each mediated network provides
> a different subset. A layout that assumes an icon and a star rating is empty on half the
> networks. And `isExpired` has to be checked before rendering content loaded a while ago;
> request a new ad instead of showing an expired one.

When several ads are preloaded, destroy every instance that is not displayed.

## Shared truths

- Register the callback **at creation**, not after `load()`. An ad that loads before the
  callback is attached reports to nobody.
- Never call `load()` from a failure callback. On a bad network it turns into a request loop.
  `autoLoad = true`, or an explicit rate-limited retry.
- A screen ad shown after a suspend point needs the screen to still be there. Check before
  showing, or the ad covers a screen the user already left.
- One load is one impression. **Every format reloads after it is dismissed**, not just the
  interstitial. A rewarded ad that is not reloaded fails the next press with code 1, and the
  button looks broken. `autoLoad = true` is what removes this class of bug.
- When an error occurs during an impression, only `onAdFailedToShow` runs — `onAdDismissed`
  does not, because the impression did not count. Code that navigates from `onAdDismissed`
  needs the same step in `onAdFailedToShow`.
- `CASMobileAds.adSoundsMuted` is global, not per instance.

## Where to load, and where to show

The publisher knows their app; the agent knows what each format needs. Propose a placement
from this table, name the screen and the moment, and let the publisher correct it. An ad in
the wrong place costs more than no ad: it earns accidental clicks, which are invalid
traffic, and it is the first thing a user complains about.

| Format | Load | Show |
|---|---|---|
| Interstitial | At the screen that will show it, `remember`ed, with `autoLoad = true`. | On a transition the user is already making: a level finished, a screen closed, a step completed. Never in the middle of a task, never on a tap that means something else. |
| Rewarded | Early, with the screen that holds the button, so the button is never pressed on an empty cache. | Only from a button the user pressed, with the reward stated before the press. Grant it in the reward listener. Gate the button on `isLoaded`. |
| Banner | The content loads itself with `autoLoad = true`. | Outside the scrolling content, at a screen edge, never over a button or game controls, never two at once. The composable reserves its own space. |
| App open | `autoLoad = true` and `autoShow = true` is the whole integration; the SDK keeps one ready. | Only on a return to the app, or on a cold start while the loading screen is still up. Never on the very first launch before the app is usable, and never on top of another full screen ad. |
| Native | With the screen or the list that will hold it, ahead of the position where it appears. | Inside the app's own layout, with the `AdLabelText()` and `AdChoices()` present. In a lazy list, far enough apart that two are never on screen together. |

**The navigation waits for the ad, the ad does not race the navigation.** Calling `show()`
and navigating in the same handler is the most common way to get an integration that looks
right and shows nothing: the next screen covers the ad immediately. Call `show()`, and
navigate from `onAdDismissed`, with the same step in `onAdFailedToShow` and when nothing is
loaded, so the user is never stuck.

## When the UI is not Compose Multiplatform

The three full screen formats still work entirely from `commonMain` — they are presented by
the native SDK over all app content and are not part of any view tree. Banner and Native
are only documented as composables, so with SwiftUI or UIKit on iOS and Views on Android
they have to be placed in native code with the native SDKs, which is the cas-android and
cas-ios skills' territory. Say so before promising those two formats, not after.

Working code for every format: the demo app screens in
`https://github.com/cleveradssolutions/CAS-KMP`, under
`cas-demo-app/src/commonMain/kotlin/.../screens/`. Each format page links the screen for
that format; they are shorter than the documentation and always compile.
