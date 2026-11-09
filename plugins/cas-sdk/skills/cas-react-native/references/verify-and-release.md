# React Native: verify, then hand over

An integration is verified by running it. Reading the diff is not verification, and **a
Metro reload is not a build**: everything that decides whether ads serve lives in the native
projects.

## Verify

1. A full rebuild and run on each platform the publisher ships, on a device if there is one,
   on an emulator or simulator otherwise. **Neither can be tapped from the command line**, so
   a full screen format cannot be triggered headlessly: either the publisher taps, or the
   verification build shows the ad once from a timer, which is removed afterwards. Say which
   of the two was done.
2. Expo: confirm `npx expo prebuild` ran and the generated `android/app/build.gradle` really
   contains the CAS Gradle plugin. That file is the proof the config plugin did its work;
   nothing in the JavaScript says so.
3. Confirm test ad mode is on for this build. Test full screen ads announce themselves and
   show CAS integration details, including a Mediation Adapters section that lists every
   network that initialized correctly. That screen is the fastest proof the solution flag on
   Android and the pods on iOS are right — and it is per platform, so read it on both.
4. Watch the native console, not the Metro output: `adb logcat` on Android, the Xcode console
   on iOS. **The consent form comes first**: on a clean device in a region that requires
   consent the built-in flow shows a form, and `initialize` does not resolve until it is
   answered. Nothing loads before that, and it is not a failure.
5. Read the result of `initialize`: `status.error` must be absent. A `demo` id is announced on
   the test ad screen, which is the reminder to replace it, not an error.
6. For every format that was added: load it, show it, and watch for the loaded listener then
   the showed listener. A format that only loads is not done. For a rewarded ad, confirm
   `addAdUserEarnRewardEventListener` fires **once** and that the reward is granted there and
   not on dismissal.
7. Check the two things only React Native can get wrong:
   - each listener is removed by the effect that added it — remount the screen twice and
     confirm the loaded event does not arrive two or three times;
   - the CAS ID actually used on each platform, logged once at startup. An iOS build running
     the Android id fails as `ConfigurationError` and nothing else says why.

Report the exact log lines. "It works" is not a result.

## Before release

| What | Why |
|---|---|
| `forceTestAds: __DEV__`, not a literal | A release with test ads earns nothing, and development on live ads risks the account. |
| `debugGeography` behind the same condition | It stays in effect in production sessions once set. |
| One ad network solution on **each** platform | The `cas { }` flag on Android and the pods on iOS, or the Expo config for both. One side without it earns nothing, silently. |
| Bare iOS: `CASAIAppIdentifier` present, and the build phase intact | React Native adds the phase; a reinstall or an Xcode cleanup can drop it. |
| Bare Android: the CAS Maven repositories present | Without them the release build fails to resolve network artifacts. |
| Test device ids reviewed | Leave only the ones the publisher wants in the shipped build. |
| Real CAS IDs, both platforms | `demo` serves test ads for ever. |
| Gradle plugin and pods versions match their native SDKs | Neither tracks the npm package version. |

## Hand over

A short checklist, not a narrative:

- what changed, by file, and whether the native projects are generated (Expo) or committed;
- what the publisher must do in the CAS dashboard — register the app, confirm the solution,
  confirm the audience;
- the store answers that this integration changes: the Google Play data disclosure, the App
  Store privacy questions, Apple's ad attribution, and the Kids Category if it applies;
- anything still waiting on the account manager, closed beta networks included;
- what a tester should not file as a bug: no ads inside a trial ad-free interval or inside the
  interstitial interval, no ads in a release build before the solution is on, `NoFill` while
  test ads work.
