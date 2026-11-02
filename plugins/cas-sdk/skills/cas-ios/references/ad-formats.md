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
