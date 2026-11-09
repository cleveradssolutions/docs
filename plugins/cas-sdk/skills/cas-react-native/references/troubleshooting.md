# React Native: reading failures

Canonical, in this repository:
[docs/reactnative/ad-errors.mdx](../../../../../docs/reactnative/ad-errors.mdx)
Published: `https://docs.page/cleveradssolutions/docs/reactnative/ad-errors`

Errors arrive on the failure listeners with a code and a message. The codes are the same
numbers on every CAS platform; the constant names here are the `AdErrorCode` members below.
Look the code up before offering a cause.

| Code | Constant | What it usually means here |
|---|---|---|
| 0 | `InternalError` | SDK side. Collect the full native console output before reporting it. |
| 1 | `NotReady` | `showAd()` was called before an ad was loaded, or after one was consumed and not reloaded. |
| 2 | `Rejected` | The device does not qualify, by region or OS version. |
| 3 | `NoFill` | No ad available. **If test ads show, the integration is correct** and fill is an account and inventory question. |
| 6 | `ReachedCap` | Daily cap for this creative, mostly cross promotion. |
| 7 | `NotInitialized` | Ads requested before initialization. In React Native this often means the root effect that calls `initialize` has not run yet, or ran and threw. |
| 8 | `Timeout` | The ad source did not answer in time. The SDK keeps waiting. |
| 9 | `NoConnection` | No network on the device. |
| 10 | `ConfigurationError` | The CAS ID is unknown, unregistered, or an ad source is misconfigured. Check the CAS ID for **that platform** first — a wrong iOS id looks exactly like this — then ask the account manager. |
| 11 | `NotPassedInterval` | The interstitial interval, or the trial ad-free interval, has not passed. Expected, not a defect. |
| 12 | `AlreadyDisplayed` | Another full screen ad is on screen. |
| 13 | `NotForeground` | The app is not in the foreground. |

## Symptoms that are not error codes

**Two impressions, or two rewards, for one ad.** A listener was added in a component that
remounted and never removed. Every `addAd...EventListener` returns a subscription, and the
`useEffect` that added it must remove it. Under fast refresh this happens constantly, which
is why it is the first thing to check on React Native.

**Nothing happens after `loadAd()` and no listener fires.** The listeners were registered
after the load, and the loaded event arrived first. Register them before loading.

**It works on Android and shows nothing on iOS, or the other way round.** Three usual
causes, in this order: the CAS ID for that platform is the wrong kind (iOS wants the App
Store id, not the bundle id); the solution was switched on for one side only; or on bare iOS
`CASAIAppIdentifier` is missing from the `Info.plist`.

**Expo: the native side looks untouched.** The config plugin runs at `npx expo prebuild`.
Until then nothing is configured, and hand edits to `android/` or `ios/` are overwritten by
the next prebuild. Read the generated `android/app/build.gradle` to confirm the plugin ran.

**Bare Android: Gradle cannot resolve an ad network artifact.** The CAS Maven repositories
were not added. Several networks publish to their own repositories, and only Expo adds them
for you. The full list is on the setup page.

**Duplicate classes on Android.** An ad network dependency was added by hand beside the CAS
Gradle plugin, which adds its own. Remove the hand-written one.

**The Gradle plugin version does not exist.** It tracks the CAS **Android SDK**, not the npm
package version, and the two are different numbers. Same for the iOS pods, which track the
CAS iOS SDK.

**A Metro reload "fixed" or "broke" it.** Neither. Native configuration only changes on a
rebuild, and ad state survives a JavaScript reload in confusing ways. Verify on a full
rebuild.

**An ad shows over the wrong screen.** `showAd()` was called and the app navigated in the
same handler. Navigate from the dismissed listener.

## When the answer is not here

The native error pages carry the same codes with native wording and sometimes more detail:
[docs/android/ad-errors.mdx](../../../../../docs/android/ad-errors.mdx) and
[docs/ios/ad-errors.mdx](../../../../../docs/ios/ad-errors.mdx). The plugin is a thin layer,
so a failure that makes no sense in TypeScript usually makes sense in the native log —
`adb logcat` or the Xcode console, not the Metro output.
