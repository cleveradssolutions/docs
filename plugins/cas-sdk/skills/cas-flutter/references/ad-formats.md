# Flutter: ad formats

Canonical, one page per format in [docs/flutter/](../../../../../docs/flutter):
[Banner-Ads.mdx](../../../../../docs/flutter/banner-ads.mdx),
[Interstitial-Ads.mdx](../../../../../docs/flutter/interstitial-ads.mdx),
[Rewarded-Ads.mdx](../../../../../docs/flutter/rewarded-ads.mdx),
[App-Open-Ads.mdx](../../../../../docs/flutter/app-open-ads.mdx),
[Native-Ads.mdx](../../../../../docs/flutter/native-ads.mdx).
Read the page for the format being added. **The notes below are the shape of the flow, not
an API reference.** Every argument name is taken from the page or from
`https://pub.dev/documentation/clever_ads_solutions/latest/`, never from memory.

The ones that are easy to get wrong:

| What | The actual API |
|---|---|
| Creating a screen ad | `CASInterstitial.createAndLoad(...)`, and the same `createAndLoad` on `CASRewarded` and `CASAppOpen`. It creates **and** starts the load; there is no separate constructor step to forget. |
| No CAS ID per instance | Unlike the native SDKs, an ad instance takes no `casId`. The id is set once, in `initialize`. |
| Autoload | `autoReload:` — not `isAutoloadEnabled` as on Android, nor `isAutoloadEnabled` as on iOS. |
| Loaded check | `isAdLoaded` |
| Showing | `show()` on the instance |
| Reward | `onUserEarnedReward`, which is **not** `onAdDismissed`: a user can dismiss without earning. |
| Errors | `AdError` with `code` and `message`. The `code` constants are `codeNoFill`, `codeNotReady` and the rest, listed in `troubleshooting.md`. |
| Banner and native are widgets | `CASBanner.createAndLoad(size:, autoReload:, ...)` and `CASNativeContent.load(...)` produce an instance; a `CASWidget(...)` wrapped around it is what actually appears in the tree. One `CASWidget` per instance. |
| Sizes | `AdSize.banner`, `AdSize.mediumRectangle`, `AdSize.leaderboard`, `AdSize.getAdaptiveBanner(...)`, `AdSize.getInlineBanner(...)`, `AdSize.getSmartBanner(...)` |
| Disposal | `dispose()` on the instance, from the widget's `dispose()`. Flutter is the one platform where the framework will not do this for you. |

Working code for every format: `example/` in
`https://github.com/cleveradssolutions/CAS-Flutter`. Read the example for the format being
added; it is shorter than the documentation and always compiles.

## Shared truths

- One instance per placement.
- **The widget's `State` owns the instance.** Create it in `initState`, dispose it in
  `dispose`, and never in `build` — `build` runs again on every rebuild, and an instance
  created there loads an ad per frame.
- Disposing a banner before creating a new one is the documented pattern when the size
  changes. Two live banner instances for one slot is a leak with a cost.
- Never call `load()` from a failure callback. On a bad network it turns into a request
  loop. `autoReload`, or an explicit rate-limited retry.
- A screen ad after `await` needs the widget to still be mounted. Check `mounted` before
  showing, or the ad shows over a screen the user already left.

## Where to load, and where to show

The publisher knows their app; the agent knows what each format needs. Propose a placement
from this table, name the screen and the moment, and let the publisher correct it. An ad in
the wrong place costs more than no ad: it earns accidental clicks, which are invalid
traffic, and it is the first thing a user complains about.

| Format | Load | Show |
|---|---|---|
| Interstitial | In `initState` of the widget that will show it, or with `autoReload`. One load is one impression, so reload after the dismiss callback. | On a transition the user is already making: a level finished, a screen closed, a step completed. Never in the middle of a task, never on a tap that means something else. |
| Rewarded | Early, in `initState` of the screen with the button, and again after every dismiss, so the button is never pressed on an empty cache. | Only from a button the user pressed, with the reward stated before the press. Grant the reward in `onUserEarnedReward`, never in the dismiss callback: a user who closes the ad early earns nothing. Gate the button on `isAdLoaded`. |
| Banner | The instance loads itself when created with `autoReload`; the `CASWidget` shows it once it is in the tree. | Pinned outside the scrolling content — a `bottomNavigationBar` slot or a `Column` edge — and never over a button or game controls. **Reserve the height** so the layout does not jump when the ad arrives. |
| App open | While the app is in the foreground, for the *next* foreground. | Only on a return to the app, or on a cold start from a loading screen while that screen is still up. Never once the user has reached app content. |
| Native | With the screen or the list that will hold it, ahead of the position where it appears. | Inside the app's own layout, marked as an ad. In a `ListView`, far enough apart that two ads are never on screen together. |

**The navigation waits for the ad, the ad does not race the navigation.** Showing a full
screen ad and calling `Navigator.push` in the same handler is the single most common way to
get an integration that looks right and shows nothing: the next route covers the ad
immediately. Call `show()`, and navigate from the dismiss callback, with the same step
repeated in the show-failure callback and when no ad is loaded, so the user is never stuck.

**Every format reloads after it is dismissed, not just the interstitial.** One load is one
impression. A rewarded ad that is not reloaded fails the next press with `codeNotReady`,
and the button looks broken.

## Native ads need native setup

Native is the one format with work outside Dart, on both platforms:
[Native-Ads-Android-Setup.mdx](../../../../../docs/flutter/native-ads-android-setup.mdx)
and
[Native-Ads-iOS-Setup.mdx](../../../../../docs/flutter/native-ads-ios-setup.mdx), plus a
template per platform,
[Native-Ads-templates.mdx](../../../../../docs/flutter/native-ads-templates.mdx). Plan that
work before promising the format; it is not a Dart-only change, and it is why native is
worth asking about separately rather than bundling into "all formats".
