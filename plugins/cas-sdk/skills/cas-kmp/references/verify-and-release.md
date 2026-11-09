# Kotlin Multiplatform: verify, then hand over

An integration is verified by running it. Reading the diff is not verification, and
**a Gradle sync is not a build**: `:shared:compileKotlinIosArm64` succeeding says nothing
about whether the Xcode project links the framework, finds the adapters, or has a CAS ID.

## Verify

1. A full build and run on each platform the publisher ships, on a device if there is one,
   on an emulator or simulator otherwise. The iOS simulator needs an **Apple Silicon Mac**,
   because there is no x86_64 target. **Neither a device nor a simulator can be tapped from
   the command line**, so a full screen format cannot be triggered headlessly: either the
   publisher taps, or the verification build shows the ad once from a timer, which is
   removed afterwards. Say which of the two was done.
2. `CASMobileAds.debugLoggingEnabled = true` for this build, and log
   `CASMobileAds.sdkVersion` and the CAS ID actually used, once at startup. On a two-platform
   integration that one line answers most of what goes wrong next.
3. Confirm test ad mode is on for this build. Test full screen ads announce themselves and
   show CAS integration details, including a **Mediation Adapters** section that lists every
   network that initialized correctly. That screen is the fastest proof the `cas { }` flag on
   Android and the Swift Package products on iOS are right — and it is per platform, so read
   it on both. On iOS it is also the only visible symptom of a missing `-ObjC` flag: the SDK
   initializes and the list is empty.
4. Watch the native console: `adb logcat` on Android, the Xcode console on iOS, neither of
   them the Gradle output. **The consent form comes first**: on a clean device in a region
   that requires consent the built-in flow shows a form, and the initialization listener does
   not fire until it is answered. Nothing loads before that, and it is not a failure.
5. Read the initialization result: `config.error` must be `null`. The demo CAS ID is
   announced on the test ad screen, which is the reminder to replace it, not an error.
6. For every format that was added: load it, show it, and watch for `onAdLoaded` then
   `onAdShowed`. A format that only loads is not done. For a rewarded ad, confirm the
   `CASRewardEarnedListener` fires **once** and that the reward is granted there and not in
   `onAdDismissed`.
7. Check the three things only Kotlin Multiplatform can get wrong:
   - every ad instance is `remember`ed and destroyed by a `DisposableEffect` — leave the
     screen and return twice, and confirm one instance loads one ad rather than three;
   - the `actual val casId` of each platform is the right kind of identifier, which step 2
     makes visible;
   - the iOS run script phase produced a fresh `Shared.framework` for this run, rather than
     linking a stale one.

Report the exact log lines. "It works" is not a result.

## Before release

| What | Why |
|---|---|
| `forceTestAds` from the build, not a literal | A release with test ads earns nothing, and development on live ads risks the account. |
| `debugGeography` only where it is safe | Harmless in `initialize`, where it applies to test sessions only; in `showConsentForm` it stays in effect in production, so it belongs behind the debug condition. |
| One ad network solution on **each** platform | The `cas { }` flag on Android and a Swift Package product on iOS. One side without it earns nothing, silently. |
| iOS: `-ObjC` in Other Linker Flags | Without it every adapter is stripped and nothing fills, with no error. |
| iOS: `casconfig.rb` re-run for this release | It writes the `SKAdNetworkItems` and AdAttributionKit identifiers of the current adapter set, and they change with every update. |
| Android: the CAS Maven repositories present | Without them the release build fails to resolve network artifacts. |
| Android: `useAdvertisingId` matching the audience | Off for a children-only app, and for builds without Google Play Services. |
| The three versions in step | `cas-kmp`, the CAS Gradle plugin and the Swift Package are three different numbers that move together. |
| Test device ids reviewed | Leave only the ones the publisher wants in the shipped build. |
| Real CAS IDs, both platforms | The demo id serves test ads for ever. |
| Every ad instance destroyed | Except the ones deliberately kept in a shared object for reuse, which should be named in the hand-over. |

## Hand over

A short checklist, not a narrative:

- what changed, by file, separating the shared Kotlin from the Android Gradle setup and the
  Xcode setup, and naming the three version numbers;
- what the publisher must do in the CAS dashboard — register the app, confirm the solution,
  confirm the audience;
- the store answers that this integration changes: the Google Play data disclosure, the App
  Store privacy questions, Apple's ad attribution, and the Kids Category if it applies;
- the Xcode steps that a project reset or a fresh clone loses: the Swift Package products,
  the run script phase, `-ObjC`, and `casconfig.rb`;
- anything still waiting on the account manager, closed beta networks included;
- what a tester should not file as a bug: no ads inside a trial ad-free interval or inside the
  interstitial interval, no ads in a release build before the solution is on, code 3 while
  test ads work, and an absent consent form outside the regulated regions.
