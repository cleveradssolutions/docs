# Cordova: verify, then hand over

An integration is verified by running it on a device. Reading the diff is not verification,
and **a browser build is not a test**: there is no SDK on the `browser` platform, so
`casai` is undefined there for reasons that have nothing to do with the integration.

## Verify

1. Build and run on each platform the publisher ships, on a device if there is one, on an
   emulator or simulator otherwise. **Neither can be tapped from the command line**, so a
   full screen format cannot be triggered headlessly: either the publisher taps, or the
   verification build shows the ad once from a timer, which is removed afterwards. Say
   which of the two was done.
2. Confirm the plugin was **re-added** after the last variable change:
   `cordova plugin rm cordova-plugin-casai --nosave && cordova plugin add cordova-plugin-casai`.
   A build alone keeps the old values, and this is the most common reason a correct-looking
   `config.xml` has no effect.
3. `casai.setDebugLoggingEnabled(true)` for this build, and log `casai.getSDKVersion()` and
   the platform once at startup. Since no CAS ID appears in the JavaScript, that line is
   most of what you have to confirm which app is running.
4. Confirm test ad mode is on. Test full screen ads announce themselves and show CAS
   integration details, including a **Mediation Adapters** section listing every network
   that initialized correctly. That screen is the fastest proof the solution variables are
   right — and it is per platform, so read it on both.
5. Watch the **native** console, not the WebView inspector: `adb logcat` on Android, the
   Xcode console on iOS. **The consent form comes first**: on a clean device in a region
   that requires consent the built-in flow shows a form, and `initialize` does not resolve
   until it is answered. Nothing loads before that, and it is not a failure.
6. Read the result of `initialize`: `status.error` must be absent.
7. For every format that was added: load it, show it, and watch for the loaded then the
   showed event. A format that only loads is not done. For a rewarded ad, confirm the
   resolved `info.isUserEarnReward` is `true` only after a full watch, and that the reward
   is granted on that flag rather than on resolution.
8. Check the four things only Cordova can get wrong:
   - **`IOS_CAS_ID` is really set**, and is the App Store id. The symptom of a missing one
     is test ads with no error, so read the value, do not infer it from behaviour;
   - the banner does not cover the app's own UI, in **both** orientations, and the page
     gives up the room it needs;
   - every document event listener is removed when its page is left — navigate away and
     back twice and confirm one ad does not report two impressions;
   - nothing touches `casai` before `deviceready`, and the `browser` platform still runs.

Report the exact log lines. "It works" is not a result.

## Before release

| What | Why |
|---|---|
| `forceTestAds` from the build, not a literal | A release with test ads earns nothing, and development on live ads risks the account. |
| `debugGeography` behind the same condition | It is a development setting and has no place in a release path. |
| `IOS_CAS_ID` is the real App Store id | `demo`, or a missing value, serves test ads for ever with no error. |
| One solution on **each** platform | `ANDROID_CAS_SOLUTIONS` and `IOS_CAS_SOLUTIONS`. One side without it earns nothing, silently. |
| Adapter lists spelled per platform | iOS `GoogleAds`, Android `googleAds`. A copied list is wrong even though it looks right. |
| `ANDROID_USE_AD_ID` matching the audience | `false` for a children-only app, and for builds without Google Play Services. |
| `IOS_USER_TRACKING_USAGE` present | Without it the IDFA is zeroed and revenue drops, with nothing in the log. |
| The plugin re-added after the last variable change | Otherwise the shipped build carries the previous values. |
| Test device ids reviewed | Leave only the ones the publisher wants in the shipped build. |

## Hand over

A short checklist, not a narrative:

- what changed, by file, with **every `config.xml` variable listed by name and value** —
  that file is the integration, more than the JavaScript is;
- the remove-and-re-add command, and whether CI has to run it;
- the layout concession the banner needs, and which element gave up the space;
- what the publisher must do in the CAS dashboard — register the app, supply the App Store
  id, confirm the solution, confirm the audience;
- the store answers that this integration changes: the Google Play data disclosure, the App
  Store privacy questions, Apple's ad attribution, and the Kids Category if it applies;
- anything still waiting on the account manager, closed beta networks included;
- what a tester should not file as a bug: no ads inside a trial ad-free interval or inside
  the interstitial interval, `NoFill` while test ads work, no ads at all in a browser build,
  and an absent consent form outside the regulated regions.
