# Kotlin Multiplatform: reading failures

Canonical, in this repository:
[docs/kmp/ad-errors.mdx](../../../../../docs/kmp/ad-errors.mdx)
Published: `https://cleveradssolutions.docs.page/docs/kmp/ad-errors`

Failures arrive as a `CASError` on `onAdFailedToLoad`, `onAdFailedToShow`,
`onBannerAdFailed` and `onNativeAdFailedToLoad`. `CASError` is a `data class` with `code`
and `message`, and **the codes are the same numbers on both platforms**, so shared code can
branch on `error.code` without a platform check. The Kotlin Multiplatform page lists them as
numbers; the named constants belong to the native SDKs.

Look the code up before offering a cause.

| Code | What it usually means here |
|---|---|
| 0 | Internal SDK error. Collect the full native console output before reporting it. |
| 1 | Not ready: `show()` was called before an ad loaded, or after one was consumed and not reloaded. |
| 2 | The device is rejected, by region or OS version. |
| 3 | No fill. **If test ads show, the integration is correct** and fill is an account and inventory question. |
| 6 | Daily cap for this creative, mostly cross promotion. |
| 7 | Not initialized. Ads were requested before `initialize`, or the shared init function is never reached from one of the two platform entry points. |
| 8 | Timeout: the ad source did not answer in time. The SDK keeps waiting. |
| 9 | No internet connection on the device. |
| 10 | Configuration error. The CAS ID is unknown, unregistered, or an ad source is misconfigured. **Check the CAS ID of that platform first** — a wrong `actual val casId` looks exactly like this — then ask the account manager. |
| 11 | The interstitial interval, or the trial ad-free interval, has not passed. Expected, not a defect. |
| 12 | Another full screen ad is on screen. |
| 13 | The app is not in the foreground. |

## Symptoms that are not error codes

**Several ads load for one screen, or memory grows.** The ad instance was created without
`remember`, so every recomposition built another one. Each one loads independently and the
previous is never destroyed. `remember { CASInterstitialAd(...) }` plus
`DisposableEffect { onDispose { destroy() } }` is the fix, and this is the first thing to
check on Kotlin Multiplatform.

**An ad shows over a screen the user already left.** The instance outlived its screen because
nothing destroyed it, or `show()` was called after a suspend point without re-checking.

**Nothing fires after `load()`.** The callback was attached after the load rather than at
creation, and the loaded event arrived first.

**It works on Android and shows nothing on iOS, or the other way round.** Four usual
causes, in this order: the `actual val casId` for that platform is the wrong kind (iOS wants
the App Store id, not the bundle id); the solution was switched on for one side only — the
`cas { }` flag on Android, a Swift Package product on iOS; `casconfig.rb` was never run; or
`-ObjC` is missing from the iOS linker flags.

**iOS: every network is unavailable, but the SDK initialized fine and the build succeeded.**
`-ObjC` is missing from **Other Linker Flags**. The adapters are discovered by class name,
and without the flag the linker strips their Objective-C classes. There is no error message
for this — the Mediation Adapters list of the test ad screen is how you see it.

**Android: Gradle cannot resolve an ad network artifact,** for example
`Could not find com.mbridge.msdk.oversea:mbridge_android_sdk`. The CAS Maven repositories
were not copied into `dependencyResolutionManagement` of `settings.gradle.kts`. The CAS
Gradle plugin does **not** add them here, unlike on some other CAS platforms. The full list
is on the setup page.

**Android: no ad source at all, and the SDK classes are missing at runtime.** The `cas-kmp`
artifact declares the CAS Android SDK as `compileOnly`, so the Gradle plugin is what brings
it. Either the plugin is not applied, or it is applied to a module that does not hold the
Android target.

**Duplicate classes on Android.** An ad network dependency was added by hand beside the CAS
Gradle plugin, which adds its own. Remove the hand-written one.

**A version does not exist, or behaviour does not match the documentation.** Three numbers
have to agree: the `cas-kmp` library, the CAS Gradle plugin (which tracks the CAS **Android**
SDK) and the Swift Package version (which tracks the CAS **iOS** SDK). None of them is the
same number, and a mismatch surfaces at runtime.

**iOS: the Xcode build cannot find `Shared.framework`, or runs stale Kotlin code.** The run
script phase is missing, or sits after Compile Sources instead of before it, or the Gradle
task path does not match the real module path. `embedAndSignAppleFrameworkForXcode` is what
produces the framework on every build.

**The simulator build will not configure at all on a Mac.** Only `iosArm64` and
`iosSimulatorArm64` exist — there is no x86_64 simulator target, so an Intel Mac cannot run
it. This is hardware, not configuration.

**Banner or Native cannot be placed.** They are Compose Multiplatform composables. In a
project with platform-native UI they do not exist in shared code, and those two formats
belong to the native skills.

**A banner is blank although it loaded.** The same `CASBannerAdContent` is bound to more than
one `CASBannerAd` composable at a time. One content instance per place it is shown.

**A native ad looks half-empty, or clicks are not counted.** Assets are nullable and each
network provides a different subset; and asset values drawn with your own `Text` or `Image`
instead of the `NativeAdScope` composables are not registered for clicks or impressions,
which is also an invalid-traffic risk.

**Crashes or silence from a background thread.** `CASBannerAdContent` must be created and
used on the main thread, and `showConsentForm()` must be called from it. All callbacks are
delivered on the main thread, so no dispatch is needed inside them.

## When the answer is not here

`CASMobileAds.debugLoggingEnabled = true` first — everything below is easier with the `CAS`
tag in the log.

The native error pages carry the same codes with native wording and sometimes more detail:
[docs/android/ad-errors.mdx](../../../../../docs/android/ad-errors.mdx) and
[docs/ios/ad-errors.mdx](../../../../../docs/ios/ad-errors.mdx). The plugin is a thin layer
over them, so a failure that makes no sense in Kotlin usually makes sense in the native log
— `adb logcat` or the Xcode console, not the Gradle output.
