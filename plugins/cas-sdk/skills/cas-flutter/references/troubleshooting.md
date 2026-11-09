# Flutter: reading failures

Canonical, in this repository:
[docs/flutter/ad-errors.mdx](../../../../../docs/flutter/ad-errors.mdx)
Published: `https://docs.page/cleveradssolutions/docs/flutter/ad-errors`

Errors arrive as an `AdError` on `onAdFailedToLoad` and `onAdFailedToShow`: `code` for the
machine, `message` for the human. The codes are the same numbers on every CAS platform; the
Dart constants are the `code*` names below. Look the code up before offering a cause.

| Code | Dart constant | What it usually means here |
|---|---|---|
| 0 | `codeInternalError` | SDK side. Collect the full console output before reporting it. |
| 1 | `codeNotReady` | `show()` was called before an ad was loaded, or after one was consumed and not reloaded. |
| 2 | `codeRejected` | The device does not qualify, by region or OS version. |
| 3 | `codeNoFill` | No ad available. **If test ads show, the integration is correct** and fill is an account and inventory question. |
| 6 | `codeReachedCap` | Daily cap for this creative, mostly cross promotion. |
| 7 | `codeNotInitialized` | Ads requested before initialization. On Flutter this often means `initialize` was never called and the implicit init did not carry the right CAS ID. |
| 8 | `codeTimeout` | The ad source did not answer in time. The SDK keeps waiting. |
| 9 | `codeNoConnection` | No network on the device. |
| 10 | `codeConfigurationError` | The CAS ID is unknown, unregistered, or an ad source is misconfigured. Check the CAS ID for **that platform** first — a wrong iOS id looks exactly like this — then ask the account manager. |
| 11 | `codeNotPassedInterval` | The interstitial interval, or the trial ad-free interval, has not passed. Expected, not a defect. |
| 12 | `codeAlreadyDisplayed` | Another full screen ad is on screen. |
| 13 | `codeNotForeground` | The app is not in the foreground. |

## Symptoms that are not error codes

**It works on Android and shows nothing on iOS, or the other way round.** The most common
Flutter-specific failure, and it has three usual causes, in this order: the CAS ID for that
platform is the wrong kind (iOS wants the App Store id, not the bundle id); the solution was
switched on for one side only, the `cas { }` block or the iOS products; or `casconfig.rb`
was never run, or was not re-run after a CAS update.

**`flutter run` works, the store build does not.** Test ads bound to `kDebugMode` are off in
release, which is correct — so a release build with no ad network solution shows nothing.
This is the expected end state of a development-only integration, not a bug.

**Duplicate symbols on iOS.** The project has both the Swift package and CAS pods. Pick one
path, as in `setup-plugin.md`.

**Duplicate classes on Android.** An ad network dependency was added by hand beside the CAS
Gradle plugin, which adds its own. Remove the hand-written one.

**The Gradle plugin version does not exist.** It tracks the CAS **Android SDK**, not the
Flutter package version, and the two are different numbers. Take it from the setup page.

**A banner appears and then the layout jumps.** No height was reserved for it. That is a
layout bug in the app, not an SDK one, and it is worth fixing before release because it
also produces accidental clicks.

**An ad shows over the wrong screen.** `show()` was called and the app navigated in the same
handler, or the widget was unmounted between an `await` and the show. Navigate from the
dismiss callback, and check `mounted`.

**Nothing at all happens and no callback fires.** On the native side an ad loaded before the
app reached the foreground, or the consent flow is waiting. Turn the SDK's debug logging on
and read the native console — `flutter logs` shows it — rather than guessing from Dart.

## When the answer is not here

The native error pages carry the same codes with native wording and sometimes more detail:
[docs/android/ad-errors.mdx](../../../../../docs/android/ad-errors.mdx) and
[docs/ios/ad-errors.mdx](../../../../../docs/ios/ad-errors.mdx). The plugin is a thin layer,
so a failure that makes no sense in Dart usually makes sense in the native log.
