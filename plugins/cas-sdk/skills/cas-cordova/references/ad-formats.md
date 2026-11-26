# Cordova: ad formats

Canonical, one page per format in [docs/cordova/](../../../../../docs/cordova):
[Banner-Ads.mdx](../../../../../docs/cordova/banner-ads.mdx),
[MRec-Ads.mdx](../../../../../docs/cordova/mrec-ads.mdx),
[Interstitial-Ads.mdx](../../../../../docs/cordova/interstitial-ads.mdx),
[Rewarded-Ads.mdx](../../../../../docs/cordova/rewarded-ads.mdx),
[App-Open-Ads.mdx](../../../../../docs/cordova/app-open-ads.mdx).
Read the page for the format being added. **The notes below are the shape of the flow, not
an API reference.** Every page shows the same flow three times — Promise, async/await and
document events — so read the tab that matches the project's style.

## One object per format, on the global `casai`

`casai.interstitialAd`, `casai.rewardedAd`, `casai.appOpenAd`, `casai.bannerAd`,
`casai.mrecAd`. There is nothing to construct and nothing to keep: **one ad of each format
per app**, so two screens that both want an interstitial share one loaded state and the
pacing has to be planned centrally. Cordova has banner **and** MRec as separate formats,
and no native format at all.

| What | The actual API |
|---|---|
| Load | `casai.interstitialAd.load({ autoReload: true })` — a Promise, resolved on load, rejected with an `AdError` |
| Load options | `autoReload`, `autoShow`, `minInterval`, and for banners `adSize`, `maxWidth`, `maxHeight`, `refreshInterval` |
| Ready check | `await casai.interstitialAd.isLoaded()` — it is a **Promise**, not a property |
| Show, full screen | `await casai.interstitialAd.show()` |
| Show, reward | `const info = await casai.rewardedAd.show()`, then `info.isUserEarnReward` |
| Show, banner | `casai.bannerAd.show({ position, offsetX, offsetY })` |
| Hide, banner | `casai.bannerAd.hide()` |
| Release | `casai.interstitialAd.destroy()` |
| Sizes | `casai.Size.ADAPTIVE`, `.INLINE`, `.SMART`, `.LEADERBOARD`, `.BANNER` |
| Positions | `casai.Position.BOTTOM_CENTER` and the eight others |
| Formats | `casai.Format.INTERSTITIAL`, `.REWARDED`, `.APP_OPEN`, `.BANNER`, `.MREC` |
| Sound | `casai.setAdSoundsMuted(true)`, global |

Document events, for the projects that prefer them:
`casai_ad_loaded`, `casai_ad_load_failed`, `casai_ad_showed`, `casai_ad_show_failed`,
`casai_ad_dismissed`, `casai_ad_clicked`, `casai_ad_reward`, `casai_ad_impressions`.
**They are global, not per format** — every handler checks `event.format` against
`casai.Format.*` first. And every `addEventListener` needs its `removeEventListener`, or a
page that is entered twice reports two impressions for one ad.

## `show()` resolves when the ad is dismissed

This is the one place where Cordova inverts the rule every other CAS platform has.

```js
try {
  await casai.interstitialAd.show();   // resolves on dismiss
  goToNextScreen();                     // correct here
} catch (error) {
  goToNextScreen();                     // show failed; the user must not be stuck
}
```

Elsewhere "call show and navigate in the same handler" is the classic bug. Here `await
show()` **is** the dismissal callback, so continuing after it is right — provided the
`catch` does the same thing, because a failed show never reaches the line after `await`.
A `try` without a matching `catch` is how a user gets stranded on a screen that never
advances.

The rewarded ad resolves with an object instead: `info.isUserEarnReward` is `true` only
when the user watched enough. **Grant the reward there and nowhere else** — a resolved
Promise alone means dismissed, not earned.

## The banner is a native view over the WebView

It is not in the DOM. It floats above the page, positioned by an anchor and an offset in
density-independent pixels, and **nothing in the CSS knows it is there**:

```js
casai.bannerAd.show({
  position: casai.Position.BOTTOM_CENTER,
  offsetX: 0,
  offsetY: 0,
});
```

Three consequences worth writing into the plan:

- **The app's layout has to give up the room.** A bottom banner over a bottom tab bar or a
  fixed footer covers it. Add padding to the page — a CSS variable the publisher can tune
  — and say which element lost the space.
- **Changing the position is another `show()` call** with new parameters, not a destroy and
  reload. And `show()` may be called before the ad is ready: the banner appears by itself
  when it arrives.
- **Rotation and tablets re-clamp the size.** Adaptive and inline banners resize on
  orientation change, so the reserved room has to hold in both orientations.

`maxWidth` and `maxHeight` bound an `ADAPTIVE` or `INLINE` banner in DP; by default they
use the full device width. The refresh interval defaults to 30 seconds, counts only while
the banner is visible, and `0` disables it. MRec works the same way, with its own object.

## Shared truths

- **Register event listeners before calling `load()`**, and remove them when the page is
  left. With Promises this is automatic, which is a good reason to prefer them in a
  single-page app.
- Never call `load()` from a failure handler. On a bad network it turns into a request
  loop. `autoReload: true`, or an explicit rate-limited retry.
- One load is one impression. **Every format reloads after it is dismissed**, not just the
  interstitial — `autoReload` is what removes this class of bug.
- When an error occurs during an impression, only `casai_ad_show_failed` fires;
  `casai_ad_dismissed` does not, because the impression did not count. With Promises, the
  `catch` is that branch.
- A full screen ad after an `await` needs the page to still be the current one. In a
  single-page app, check before showing, or the ad shows over a view the user already left.
- Nothing works before `deviceready`, and nothing works on the `browser` platform.

## Where to load, and where to show

The publisher knows their app; the agent knows what each format needs. Propose a placement
from this table, name the screen and the moment, and let the publisher correct it. An ad in
the wrong place costs more than no ad: it earns accidental clicks, which are invalid
traffic, and it is the first thing a user complains about.

| Format | Load | Show |
|---|---|---|
| Interstitial | Once after `deviceready` with `autoReload: true`, or when entering the screen that will show it. | On a transition the user is already making: a step completed, a screen closed, a level finished. Never in the middle of a task, never on a tap that means something else. |
| Rewarded | Early, with `autoReload: true`, so the button is never pressed on an empty cache. | Only from a button the user pressed, with the reward stated before the press. Grant it on `info.isUserEarnReward`. Gate the button on `await isLoaded()`. |
| Banner | `load({ adSize, autoReload: true })` once; it refreshes itself. | `show()` on the screens that should have it, `hide()` on the ones that should not — a full screen game view, a checkout, a form. Reserve the room in CSS. |
| MRec | Same as the banner, with its own object. | A pause screen, a results panel, a content break — somewhere the user is reading. It is large; it does not belong over interactive content. |
| App open | `load({ autoShow: true })` and let the SDK pick the moment. | Only on a return to the app, or on a cold start while the splash is still up. Never once the user has reached app content, and never over another full screen ad. |

## Minimum interval between interstitials

`minInterval: 30` is an option of `load()`, counted from the moment the previous
interstitial was dismissed. A show attempt inside the window rejects with
`NotPassedInterval` — expected behaviour, not a defect, and worth saying in the hand-over
before a tester files it. The timer is shared across interstitial instances while the value
is per instance.

Working code for every format: `example/src/` in
`https://github.com/cleveradssolutions/CAS-Cordova`. Each format page links its screen;
they are shorter than the documentation and always run.
