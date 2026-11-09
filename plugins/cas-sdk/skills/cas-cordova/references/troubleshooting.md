# Cordova: reading failures

Canonical, in this repository:
[docs/cordova/ad-errors.mdx](../../../../../docs/cordova/ad-errors.mdx)
Published: `https://docs.page/cleveradssolutions/docs/cordova/ad-errors`

A failure arrives as a rejected Promise carrying an `AdError`, or on the
`casai_ad_load_failed` and `casai_ad_show_failed` document events. `AdError` has `code`,
`message` and `format`. The codes are the same numbers on every CAS platform.

| Code | Name | What it usually means here |
|---|---|---|
| 0 | `InternalError` | SDK side. Collect the full native console output before reporting it. |
| 1 | `NotReady` | `show()` was called before an ad loaded, or after one was consumed and not reloaded. |
| 2 | `Rejected` | The device does not qualify, by region or OS version. |
| 3 | `NoFill` | No ad available. **If test ads show, the integration is correct** and fill is an account and inventory question. |
| 6 | `ReachedCap` | Daily cap for this creative, mostly cross promotion. |
| 7 | `NotInitialized` | Ads requested before `initialize`, or before `deviceready` resolved at all. |
| 8 | `Timeout` | The ad source did not answer in time. The SDK keeps waiting. |
| 9 | `NoConnection` | No network on the device. |
| 10 | `ConfigurationError` | The CAS ID is unknown, unregistered, or an ad source is misconfigured. On iOS check `IOS_CAS_ID` first; on Android check the `<widget id>` against what is registered. |
| 11 | `NotPassedInterval` | The interstitial interval, or the trial ad-free interval, has not passed. Expected, not a defect. |
| 12 | `AlreadyDisplayed` | Another full screen ad is on screen. |
| 13 | `NotForeground` | The app is not in the foreground. |

## Symptoms that are not error codes

**`casai` is undefined.** The code ran before `deviceready`, or it is running on the
`browser` platform, where there is no plugin at all. Both are common in an Ionic app whose
services construct eagerly.

**A variable was changed and nothing happened.** `config.xml` plugin variables are applied
when the plugin is **installed**, not when the project is built:

```shell
cordova plugin rm cordova-plugin-casai --nosave && cordova plugin add cordova-plugin-casai
```

This is the first thing to check whenever a setting appears to be ignored.

**iOS serves test ads even though test mode is off.** `IOS_CAS_ID` is missing or wrong.
Without it the plugin falls back to test ads **silently** — there is no error and no log
line. Confirm the value is the App Store id, digits only, and not the bundle identifier.

**It works on Android and shows nothing on iOS, or the other way round.** In order:
`IOS_CAS_ID` missing or the wrong kind of identifier; the solution variable set for one
platform only; the adapter list copied between `ANDROID_CAS_ADAPTERS` and
`IOS_CAS_ADAPTERS` without re-spelling the names — iOS writes `GoogleAds`, Android writes
`googleAds`.

**Two impressions, or two rewards, for one ad.** A document event listener was added by a
page that was entered twice and never removed. Every `addEventListener` needs its
`removeEventListener`; in a single-page app this happens on every navigation. Promises do
not have this problem, which is a good reason to prefer them.

**An event handler fires for the wrong ad.** The document events are **global**, not per
format. Every handler must check `event.format` against `casai.Format.*`.

**The banner covers the app's own UI.** It is a native view over the WebView, outside the
DOM, so no CSS rule can move it and nothing in the layout reserves its space. Either the
page adds padding, or the banner moves to another anchor with `show({ position, offsetX,
offsetY })`. The same applies after a rotation, where an adaptive banner is re-clamped.

**The banner does not appear although it loaded.** `show()` was never called — loading does
not display it — or `hide()` is still in effect from another screen.

**The user is stranded after an interstitial.** `await show()` resolves on dismissal, so
the code after it runs then; but a **failed** show rejects and never reaches that line. The
`catch` has to continue the flow too, and so does the path where nothing was loaded.

**A reward was granted to someone who closed the ad early.** The resolved value was used
instead of `info.isUserEarnReward`. Resolution means dismissed; the flag means earned.

**The app builds but no ad network is present.** No solution variable, or only one of the
two. Check `ANDROID_CAS_SOLUTIONS` and `IOS_CAS_SOLUTIONS`, then re-add the plugin.

**Capacitor: the variables seem to be ignored entirely.** Capacitor runs Cordova plugins
but does not consume `config.xml` plugin variables the same way. Confirm how the project
supplies them before debugging further.

## When the answer is not here

`casai.setDebugLoggingEnabled(true)` first, then read the **native** log — `adb logcat` on
Android, the Xcode console on iOS — not the WebView inspector. The plugin is a thin bridge,
so a failure that makes no sense in JavaScript usually makes sense there.

The native error pages carry the same codes with native wording and sometimes more detail:
[docs/android/ad-errors.mdx](../../../../../docs/android/ad-errors.mdx) and
[docs/ios/ad-errors.mdx](../../../../../docs/ios/ad-errors.mdx).
