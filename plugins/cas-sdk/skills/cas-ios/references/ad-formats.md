# iOS: ad formats

Canonical, in this repository, one page per format in [docs/iOS/](../../../../../docs/iOS):
[Banner-Ads.mdx](../../../../../docs/iOS/Banner-Ads.mdx),
[Interstitial-Ads.mdx](../../../../../docs/iOS/Interstitial-Ads.mdx),
[Rewarded-Ads.mdx](../../../../../docs/iOS/Rewarded-Ads.mdx),
[App-Open-Ads.mdx](../../../../../docs/iOS/App-Open-Ads.mdx),
[Load-Native-Ads.mdx](../../../../../docs/iOS/Load-Native-Ads.mdx),
[Display-Native-Ad.mdx](../../../../../docs/iOS/Display-Native-Ad.mdx).
Read the page for the format being added; the code below is the shape, not a substitute.
Working code for every format: `DemoApp Swift/`, `DemoApp Objective-C/` and
`DemoApp SwiftUI/` in `https://github.com/cleveradssolutions/CAS-iOS`. Read the sample for
the format being added, it is shorter than the documentation and always compiles.

Shared truths:

- One instance per placement, created with the CAS ID.
- The owner keeps the instance alive for the lifetime of its screen. An instance released
  while loading loses the ad it was loading.
- Never call `loadAd()` from a failure delegate callback. On a bad network it turns into a
  request loop. Autoload or an explicit, rate-limited retry instead.
- Presenting is main thread work, from a view controller that is on screen.

## Where to load, and where to show

The publisher knows their app; the agent knows what each format needs. Propose a placement
from this table, name the screen and the moment, and let the publisher correct it. An ad in
the wrong place costs more than no ad: it earns accidental clicks, which are invalid
traffic, and it is the first thing a user complains about.

| Format | Load | Show |
|---|---|---|
| Interstitial | In `viewDidLoad()` of the view controller that will show it, or with `isAutoloadEnabled`. One load is one impression, so reload after the dismiss callback. | On a transition the user is already making: a level finished, a screen closed, a step completed. Never in the middle of a task, never on a tap that means something else. |
| Rewarded | Early, in `viewDidLoad()` of the screen with the button, and again after every dismiss, so the button is never pressed on an empty cache. | Only from a button the user pressed, with the reward stated before the press. Grant the reward in the reward callback, never in the dismiss callback: a user who closes the ad early earns nothing. Keep the button visible but disabled, or hidden, while no ad is ready. |
| Banner | The view loads itself once it is in the hierarchy with constraints and `isAutoloadEnabled` is on. | Pinned to the safe area at the top or the bottom, outside the scrolling content, and never over a button, a tab bar or game controls. Reserve the height so the layout does not jump when the ad arrives. |
| App open | While the app is in the foreground, for the *next* foreground. | Only on a return to the app, or on a cold start from a loading screen while that screen is still up. Never once the user has reached app content. |
| Native | With the screen or the list that will hold it, ahead of the position where it appears. | Inside the app's own layout, marked as an ad, with the required assets present. In a list, far enough apart that two ads are never on screen together. |

**The transition waits for the ad, the ad does not race the transition.** Showing a full
screen ad and navigating in the same handler is the single most common way to get an
integration that looks right and shows nothing: the next screen covers the ad
immediately. Call show, and push the next screen from the dismiss callback, with the same step repeated in
the present-failure callback and when no ad is loaded, so the user is never stuck.

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

```swift
class MyViewController: UIViewController, CASScreenContentDelegate {
    let interstitialAd = CASInterstitial(casID: AppDelegate.casID)

    override func viewDidLoad() {
        super.viewDidLoad()
        interstitialAd.delegate = self
        interstitialAd.loadAd()          // or isAutoloadEnabled = true
    }

    func showAd() {
        interstitialAd.present(from: self)
    }
}
```

`CASScreenContentDelegate` carries `screenAdDidLoadContent`,
`screenAd(_:didFailToLoadWithError:)`, `screenAd(_:didFailToPresentWithError:)`,
`screenAdWillPresentContent`, `screenAdDidClickContent` and `screenAdDidDismissContent`.

Rewarded ads grant the reward in their reward callback and nowhere else, never in the
dismiss callback, because a user who closes the ad early earns nothing.

Ads expire after about an hour. A cache of preloaded ads has to be refreshed, not held.

### Interstitial pacing

A minimum interval between impressions is enforced; a present attempt inside it fails with
the interval error, which is expected behaviour, not an error to work around. Pause sound
and gameplay before presenting and resume on dismiss. Load well before the moment of
presenting, so the user never waits.

### App open

Shown when the user foregrounds the app, with one hard limitation: on a cold start, show
it only from a loading screen, and only while the loading screen is still up. Once the
user is in app content, do not show it. Keep loading assets on a background queue while
the ad is on screen, and do not show an app open ad to a user who has just returned from
clicking one.

## Banner

```swift
class MyViewController: UIViewController, CASBannerDelegate {
    var bannerView: CASBannerView!

    override func viewDidLoad() {
        super.viewDidLoad()
        bannerView = CASBannerView(adSize: CASSize.getAdaptiveBanner(forMaxWidth: 360),
                                   casID: AppDelegate.casID)
        bannerView.delegate = self
        // add to the view hierarchy with constraints that match the ad size
    }
}
```

Sizes: `getAdaptiveBanner(forMaxWidth:)` is the default choice, plus
`getInlineBanner(width:maxHeight:)`, `getSmartBanner()`, `mediumRectangle`, `leaderboard`
and `banner`. Pick the size from the container the publisher actually has; an ad view
without constraints that give it the right size shows nothing.

Banners refresh themselves. Release the view with the screen that owns it, and in SwiftUI
own it in a wrapper that survives view updates rather than recreating it on every render.

## Native

Loaded through the native loader with a delegate, then bound to the publisher's layout.
Two rules that are easy to miss:

- keep every loaded native ad alive until it is released, a released one cannot be shown
  or billed;
- binding the content is a main thread call.

Layout and asset rules, including what may and may not be changed in the ad view, are on
the display page. Native ads have the strictest presentation requirements of the formats,
so follow that page rather than improvising a layout.
