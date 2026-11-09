# Unreal: ad formats

Canonical, one page per format in [docs/unreal/](../../../../../docs/unreal):
[Banner-Ads.mdx](../../../../../docs/unreal/banner-ads.mdx),
[MREC-Ads.mdx](../../../../../docs/unreal/mrec-ads.mdx),
[Interstitial-Ads.mdx](../../../../../docs/unreal/interstitial-ads.mdx),
[Rewarded-Ads.mdx](../../../../../docs/unreal/rewarded-ads.mdx),
[App-Open-Ads.mdx](../../../../../docs/unreal/app-open-ads.mdx).
Read the page for the format being added. **The notes below are the shape of the flow, not
an API reference.** The pages also carry a screenshot of every Blueprint node, which is the
fastest way to tell a publisher what to place.

## The shape is different from every other CAS platform

**There are no ad objects.** Every call is static on `UCASMobileAds`, and there is exactly
**one ad of each format per app**. So two places that both want an interstitial share the
same loaded ad and the same events, and pacing has to be planned centrally rather than per
screen. Nothing has a lifetime to manage, which removes a whole class of bugs and
introduces one: an event handler registered twice fires twice.

Unreal also has **MREC as its own format**, with a complete API parallel to the banner, and
**no native ad format at all**.

| What | Interstitial | Rewarded | App Open |
|---|---|---|---|
| Load | `LoadInterstitialAd()` | `LoadRewardedAd()` | `LoadAppOpenAd()` |
| Autoload | `SetAutoloadInterstitialAd(true)` | `SetAutoloadRewardedAd(true)` | `SetAutoloadAppOpenAd(true)` |
| Show | `ShowInterstitialAd()` | `ShowRewardedAd()` | `ShowAppOpenAd()` |
| Auto show | `AutoshowInterstitialAd()` / `DisableAutoshowInterstitialAd()` | — | `AutoshowAppOpenAd()` / `DisableAutoshowAppOpenAd()` |
| Ready | `IsInterstitialAdReady()` | `IsRewardedAdReady()` | `IsAppOpenAdReady()` |
| Release | `DestroyInterstitialAd()` | `DestroyRewardedAd()` | `DestroyAppOpenAd()` |

Events, per format, with `Interstitial` replaced by `Rewarded` or `AppOpen`:
`OnInterstitialAdLoaded`, `OnInterstitialAdLoadFailed(ECASError)`,
`OnInterstitialAdDisplayed`, `OnInterstitialAdShowFailed(ECASError)`,
`OnInterstitialAdClicked`, `OnInterstitialAdDismissed`. Rewarded adds
**`OnRewardedAdEarnedReward`**.

Banner and MREC, with `Banner` replaced by `MRec`:

| What | The actual API |
|---|---|
| Load | `LoadBannerAd(ECASBannerSize::Adaptive)`, and `LoadMRecAd()` with no size |
| Sizes | `ECASBannerSize::Adaptive`, `::Smart`, `::Leaderboard`, `::Banner` — banner only |
| Autoload | `SetAutoloadBannerAd(true)` |
| Show and hide | `ShowBannerAd()`, `HideBannerAd()` |
| Position | `SetBannerAdPosition(ECASPosition::TopCenter)`, bottom centre by default; changing it does **not** destroy or reload the ad |
| Refresh | `SetBannerAdRefreshInterval(30)`, `0` disables it |
| Events | `OnBannerAdLoaded`, `OnBannerAdFailed(ECASError)`, `OnBannerAdClicked` |
| Ready | `IsBannerAdReady()` |
| Release | `DestroyBannerAd()` |

Note that the banner's failure event is `OnBannerAdFailed`, not the load/show pair the full
screen formats have.

An error is an `ECASError`, and `UCASMobileAds::GetAdsErrorMessage(Error)` turns it into
text for the log. `ECASError::IntervalNotYetPassed` is the one value the documentation names
by hand, and it is the expected result of showing an interstitial inside its minimum
interval.

## Autoload is a setting first, and a call second

The default comes from `CAS.AI Config > Autoload [Format] Ads`, per platform, and
`SetAutoload*Ad(true)` overrides it at runtime. With autoload on, a new ad loads after each
impression and failures are retried automatically — which is the sane default for a game,
because the alternative is a reload call after every dismissal.

Two details that only exist here:

- **`LoadInterstitialAd()` still has to be called once** to create the ad instance, even
  with autoload on.
- After `Destroy*Ad()`, call `Load*Ad()` again to recreate it and restore autoload mode.

## Shared truths

- **Register events before loading or showing.** In C++, once at startup. In Blueprint the
  events are registered *on the Load and Show nodes*, and **each new Load or Show node
  cancels the previous node's events** — so one place owns the wiring, not every graph that
  shows an ad. The `Event Once` checkbox fires the Loaded/Failed event a single time, which
  is what you want with autoload on, since otherwise it fires for every retry.
- **Never poll `Is*AdReady()` per frame.** It calls into the native SDK and will cost frame
  time. Subscribe to `On*AdLoaded` and keep a flag. The documentation says this explicitly,
  and it is the most Unreal-specific mistake available.
- Never call `Load*Ad()` from a failure event. On a bad network it becomes a request loop.
  Autoload, or an explicit rate-limited retry.
- One load is one impression. **Every format reloads after it is dismissed**, not just the
  interstitial — autoload is what removes this class of bug.
- **Pause the game while a full screen ad shows.** Audio, the game loop, and any intense
  computation. Resume in `On*AdDismissed`, and also in `On*AdShowFailed`, or a failed show
  leaves the game paused for ever. This is engine work that the SDK cannot do for you, and
  it is the difference between an integration that works and one that is reported as a
  freeze.

## Where to load, and where to show

The publisher knows their game; the agent knows what each format needs. Propose a placement
from this table, name the moment, and let the publisher correct it. An ad in the wrong place
costs more than no ad: it earns accidental clicks, which are invalid traffic, and it is the
first thing a player complains about.

| Format | Load | Show |
|---|---|---|
| Interstitial | Autoload on, plus one `LoadInterstitialAd()` at startup. | At a natural break the player is already taking: a level finished, a run ended, a menu closed. Never mid-level, never on a tap that means something else, and never over a running game loop. |
| Rewarded | Autoload on, so the button is never pressed on an empty cache. | Only from a button the player pressed, with the reward stated before the press. Grant it in **`OnRewardedAdEarnedReward`** and nowhere else: a player who closes the ad early earns nothing. Gate the button on the loaded event. |
| Banner | Autoload on; the ad loads itself. | `ShowBannerAd()` on menus and non-interactive screens, hidden during gameplay with `HideBannerAd()`. Pick the position once, away from buttons and controls. |
| MREC | Autoload on. | A pause screen, a shop, a between-level panel — somewhere the player is reading rather than playing. It is large; it does not belong over gameplay. |
| App Open | Autoload on. | `AutoshowAppOpenAd()` and let the SDK pick the moment on a return to the app. Never on the very first launch before the game is playable, and never on top of another full screen ad. |

**Show the ad, then continue the flow from the dismissed event.** Calling `ShowInterstitialAd()`
and immediately loading the next level is the most common way to get an integration that
looks right and shows nothing. Continue from `On*AdDismissed`, with the same step in
`On*AdShowFailed` and when nothing is loaded, so the player is never stuck on a black
screen.

## Minimum interval between interstitials

`SetInterstitialAdMinimumInterval(Seconds)`, counted from the moment the previous
interstitial **closes**. A show attempt inside the window fires `OnInterstitialAdShowFailed`
with `ECASError::IntervalNotYetPassed` — expected behaviour, not a defect, and worth saying
in the hand-over before a tester files it.

`RestartInterstitialAdInterval()` starts the countdown by hand: after a rewarded ad, after
an app open ad, or at the start of a session so the first interstitial is delayed.

Set the interval **before initialization** where possible, so the dashboard can override it
remotely.

## Blueprint-only projects

The agent cannot author a Blueprint graph — `.uasset` files are binary. What it can do, and
should do well:

- name every node to place, in order, with the event pins to connect, pointing at the
  screenshot on the format's page;
- say which nodes come from the **CAS Mobile Ads** category, because the engine's built-in
  AdMob nodes share the names `Show Interstitial Ad` and `Show Ad Banner` and do nothing
  here;
- write out the settings-window fields that replace the code, per platform;
- state plainly that the wiring itself is the publisher's step, before the work starts
  rather than at the end of it.
