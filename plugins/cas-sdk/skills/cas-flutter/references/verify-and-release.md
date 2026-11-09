# Flutter: verify, then hand over

An integration is verified by running it. Reading the diff is not verification. On Flutter
that means **both platforms the publisher ships**: the Dart code is shared, the parts that
decide whether ads serve are not.

## Verify

1. `flutter run` on each platform, on a device if the publisher has one, on an emulator or
   simulator otherwise. **Neither can be tapped from the command line**, so a full screen
   format cannot be triggered headlessly: either the publisher taps, or the verification
   build shows the ad once from a timer, which is removed afterwards. Say which of the two
   was done.
2. Confirm test ad mode is on for this build. Test full screen ads announce themselves and
   show CAS integration details, including a Mediation Adapters section that lists every
   network that initialized correctly. That screen is the fastest proof the solution flag on
   Android and the products or pods on iOS are right — and it is per platform, so read it on
   both.
3. Watch the console with the SDK's debug logging on. `flutter logs` surfaces the native
   log, which is where the real detail is; the Dart side only sees callbacks. **The consent
   form comes first**: on a clean device in a region that requires consent the built-in flow
   shows a form, and `initialize` does not complete until it is answered. Nothing loads
   before that, and it is not a failure.
4. Read the result of `initialize`: `status.error` must be null. The native console line
   about initialization completing in test ad mode confirms test mode is really on. A `demo`
   id is announced on the test ad screen, which is the reminder to replace it, not an error.
5. For every format that was added: load it, show it, and watch for the load callback then
   the show callback. A format that only loads is not done. For a rewarded ad, confirm
   `onUserEarnedReward` fires and that the reward is granted there and not in the dismiss
   callback.
6. Check the two things only Flutter can get wrong:
   - the CAS ID actually used on each platform, printed or logged once at startup — an iOS
     build running the Android id fails as `codeConfigurationError` and nothing else says
     why;
   - a hot reload does not re-run `initState`, so an ad instance created there survives a
     reload in a confusing state. Verify on a **full restart**, not a hot reload.

Report the exact log lines. "It works" is not a result.

## Before release

| What | Why |
|---|---|
| `forceTestAds` bound to the build, not a literal | A release with test ads earns nothing, and development on live ads risks the account. `kDebugMode \|\| kProfileMode`. |
| `debugGeography` unset or behind the same condition | It stays active in production sessions once set. |
| One ad network solution on **each** platform | The `cas { }` flag on Android and the products or pods on iOS. One side without it earns nothing, silently. |
| `casconfig.rb` re-run after the last CAS update | What it writes changes with the ad network set. |
| Test device ids reviewed | Leave only the ones the publisher wants in the shipped build. |
| Real CAS IDs, both platforms | `demo` serves test ads for ever. |
| Gradle plugin version matches the CAS Android SDK | It does not track the Flutter package version. |
| One iOS dependency path | A Swift package and CAS pods together is a duplicate-symbols failure. |

## Hand over

A short checklist, not a narrative:

- what changed, by file;
- what the publisher must do in the CAS dashboard — register the app, confirm the solution,
  confirm the audience;
- the store answers that this integration changes: the Google Play data disclosure, the App
  Store privacy questions, Apple's ad attribution, and the Kids Category if it applies;
- anything still waiting on the account manager, closed beta networks included;
- what a tester should not file as a bug: no ads inside a trial ad-free interval, no ads in a
  release build before the solution is on, `codeNoFill` while test ads work.
