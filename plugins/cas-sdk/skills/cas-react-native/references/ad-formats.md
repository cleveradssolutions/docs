# React Native: ad formats

Canonical, one page per format in [docs/reactnative/](../../../../../docs/reactnative):
[Banner-Ads.mdx](../../../../../docs/reactnative/banner-ads.mdx),
[Interstitial-Ads.mdx](../../../../../docs/reactnative/interstitial-ads.mdx),
[Rewarded-Ads.mdx](../../../../../docs/reactnative/rewarded-ads.mdx),
[App-Open-Ads.mdx](../../../../../docs/reactnative/app-open-ads.mdx),
[Native-Ads.mdx](../../../../../docs/reactnative/native-ads.mdx).
Read the page for the format being added. **The notes below are the shape of the flow, not
an API reference.**

## The shape is different from every other CAS platform

**Screen ads are static modules, not instances.** There is no object to create, keep or
pass around: `InterstitialAd`, `RewardedAd` and `AppOpenAd` are imported and called
directly. That removes the lifetime problem the native SDKs have, and it introduces a
different one — **there is one of each per app**, so two screens cannot hold two
independently loaded interstitials.

| What | The actual API |
|---|---|
| Import | `import { InterstitialAd } from 'react-native-cas';` and the same for `RewardedAd`, `AppOpenAd` |
| Load | `InterstitialAd.loadAd()` |
| Autoload | `InterstitialAd.setAutoloadEnabled(true)` |
| Ready check | `InterstitialAd.isAdLoaded()` |
| Show | `InterstitialAd.showAd()` — not `show()` |
| Auto show | `AppOpenAd.setAutoshowEnabled(true)`, also on the interstitial |
| Events | `addAdLoadedEventListener`, `addAdFailedToLoadEventListener`, `addAdShowedEventListener`, `addAdFailedToShowEventListener`, `addAdDismissedEventListener`, `addAdClickedEventListener`, `addAdImpressionEventListener` |
| Reward | `RewardedAd.addAdUserEarnRewardEventListener` — the only place a reward is granted |
| Release | `InterstitialAd.destroy()` |
| Pacing | `InterstitialAd.setMinInterval(seconds)`, `InterstitialAd.restartInterval()` |
| Rewarded fallback | `RewardedAd.setExtraFillByInterstitialAd(true)` serves an interstitial when no rewarded fill exists, and the reward event still fires |
| Banner | the `<BannerAdView size={...} />` component, with `BannerAdSize.BANNER`, `.MREC`, `.ADAPTIVE`, `.INLINE`, `.LEADERBOARD`, `.SMART` |
| Banner callbacks | props: `onAdLoadedCallback`, `onAdFailedCallback`, `onAdImpressionCallback`, `onAdClickedCallback` |
| Banner control | `useRef<BannerAdViewRef>(null)` on the component |

An interstitial cannot be put in the component tree; it is an overlay over everything. The
banner is the opposite: it is a real component and belongs in the tree.

Working code for every format: the example app in
`https://github.com/cleveradssolutions/CAS-ReactNative`. Read it for the format being added;
it is shorter than the documentation and always compiles.

## Shared truths

- **Every `addAd...EventListener` returns a subscription, and `useEffect` must remove it.**
  A listener added in a component that remounts — and under fast refresh everything
  remounts — fires several times for one ad, which looks like duplicate impressions and
  duplicate rewards. This is the most common React Native specific bug in an ad
  integration.
- Register listeners **before** calling `loadAd()`, or the loaded event can arrive first and
  be missed.
- Never call `loadAd()` from a failure listener. On a bad network it turns into a request
  loop. `setAutoloadEnabled(true)`, or an explicit rate-limited retry.
- A screen ad after an `await` needs the screen to still be mounted. Check before showing,
  or the ad shows over a screen the user already left.
- One of each screen format per app. If two places need an interstitial, they share the
  module and its loaded state; plan the pacing centrally rather than per screen.

## Where to load, and where to show

The publisher knows their app; the agent knows what each format needs. Propose a placement
from this table, name the screen and the moment, and let the publisher correct it. An ad in
the wrong place costs more than no ad: it earns accidental clicks, which are invalid
traffic, and it is the first thing a user complains about.

| Format | Load | Show |
|---|---|---|
| Interstitial | In a mount effect of the screen that will show it, or with `setAutoloadEnabled(true)` once at startup. One load is one impression, so reload after the dismissed event. | On a transition the user is already making: a level finished, a screen closed, a step completed. Never in the middle of a task, never on a tap that means something else. |
| Rewarded | Early, in the mount effect of the screen with the button, and again after every dismissal, so the button is never pressed on an empty cache. | Only from a button the user pressed, with the reward stated before the press. Grant the reward in `addAdUserEarnRewardEventListener`, never on dismissal: a user who closes the ad early earns nothing. Gate the button on `isAdLoaded()`. |
| Banner | The component loads itself once mounted. | Outside the scrolling content, at a screen edge, never over a button or game controls. **Reserve the height** so the layout does not jump when the ad arrives. |
| App open | While the app is in the foreground, for the *next* foreground. `setAutoshowEnabled(true)` makes the SDK handle the moment. | Only on a return to the app, or on a cold start from a loading screen while that screen is still up. Never once the user has reached app content. |
| Native | With the screen or the list that will hold it, ahead of the position where it appears. | Inside the app's own layout, marked as an ad. In a `FlatList`, far enough apart that two ads are never on screen together. |

**The navigation waits for the ad, the ad does not race the navigation.** Calling `showAd()`
and `navigation.navigate(...)` in the same handler is the single most common way to get an
integration that looks right and shows nothing: the next screen covers the ad immediately.
Call `showAd()`, and navigate from the dismissed listener, with the same step repeated in the
failed-to-show listener and when no ad is loaded, so the user is never stuck.

**Every format reloads after it is dismissed, not just the interstitial.** One load is one
impression. A rewarded ad that is not reloaded fails the next press with `NOT_READY`, and
the button looks broken.

## Native ads need more than the component

Native has a templates page and a manual path:
[Native-Ads-Templates.mdx](../../../../../docs/reactnative/native-ads-templates.mdx) for the
ready-made layouts, and
[Native-Ad-Manual.mdx](../../../../../docs/reactnative/native-ad-manual.mdx) when the
publisher wants their own. The manual path means binding every required asset and honouring
the ad choices placement; it is the one format where a half-done job is a policy problem and
not just an empty space. Plan it before promising the format.
