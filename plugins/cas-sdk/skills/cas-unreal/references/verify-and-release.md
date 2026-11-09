# Unreal: verify, then hand over

An integration is verified by packaging it and running it on a device. Reading the diff is
not verification, and **play-in-editor is not a test**: there is no SDK in the editor, so
nothing initializes, no ad loads, and the log is empty for reasons that have nothing to do
with the integration.

## Verify

1. **Package** for each platform the publisher ships, and run on a device. An emulator or
   simulator will do when there is no device, but the packaging step is the part that
   matters: the Android Gradle step and the iOS pod integration only run then, and they are
   where this platform fails. **Neither a device nor a simulator can be tapped from the
   command line**, so a full screen format cannot be triggered headlessly: either the
   publisher taps, or the verification build shows the ad once from a timer, which is
   removed afterwards. Say which of the two was done.
2. `UCASMobileAds::SetVerboseAdsLogs(true)` for this build, and log
   `GetMobileAdsVersion()` and the platform once at startup. On a two-platform integration
   that one line answers most of what goes wrong next, because the CAS App IDs are not in
   the code and cannot be read from the project.
3. `UCASMobileAds::ValidateAdsIntegration()` once, and read the **CASIntegrationHelper**
   tag. Every network that was chosen must be VERIFIED. MISSING is expected for networks
   deliberately left out; NOT VERIFIED is the finding. Run it **per platform** — it is the
   fastest proof the solution checkbox and the framework integration worked. **Then remove
   the call**, as the documentation says.
4. Confirm `TestAdsMode` is on in the config panel of **each** platform. Test full screen
   ads announce themselves and show CAS integration details, including a Mediation Adapters
   section listing every network that initialized correctly — the same information from the
   device's point of view rather than the helper's.
5. Read the device log, not the editor output: `adb logcat` on Android, the Xcode device
   console on iOS. **The consent form comes first**: on a clean device in a region that
   requires consent the built-in flow shows a form, and `OnAdsInitialized` does not fire
   until it is dismissed. Nothing loads before that, and it is not a failure.
6. Confirm initialization: `Config.Error` must be empty. Remember the event can fire again
   after an error, so read the whole log rather than the first occurrence. A `demo` CAS App
   ID is announced on the test ad screen, which is the reminder to replace it, not an error.
7. For every format that was added: load it, show it, and watch for `On*AdLoaded` then
   `On*AdDisplayed`. A format that only loads is not done. For a rewarded ad, confirm
   `OnRewardedAdEarnedReward` fires **once** and that the reward is granted there and not on
   dismissal.
8. Check the four things only Unreal can get wrong:
   - the game **pauses and resumes** around a full screen ad, including on
     `On*AdShowFailed`, not only on dismissal;
   - nothing polls `Is*AdReady()` per frame;
   - no graph calls the engine's built-in AdMob node of the same name, and the engine's
     AdMob support is off;
   - both config panels are filled, not just the one for the platform being tested.

Report the exact log lines. "It works" is not a result.

## Before release

Most of this list is a settings window rather than code, which is exactly why it has to be
written down.

| What | Why |
|---|---|
| `TestAdsMode` **off** in both config panels | A release with test ads earns nothing. There is no build flag to bind it to, so this is a human step. |
| Real CAS App IDs in both panels | `demo` serves test ads for ever, and the Android and iOS fields are different values. |
| Test device ids removed | They turn a release build into test ads on those devices. |
| One ads solution checked on **each** platform | One side without it earns nothing, silently. |
| `ValidateAdsIntegration()` call removed | It is a development tool. |
| `SetVerboseAdsLogs` off | On by choice only; it costs performance. |
| The engine's built-in AdMob support still off | A later engine or project reset can re-enable it. |
| `Use Advertising Id` matching the audience | Off for a children-only game, and for builds without Google Play Services. |
| iOS: the User Tracking Usage Description present | Without it the IDFA is zeroed and revenue drops, with nothing in the log. |
| Privacy Policy URL set | Otherwise the consent dialog links the CAS policy rather than the game's. |
| iOS: `Binaries/IOS` deleted after the last adapter change | Or the shipped package links the previous framework set. |
| `app-ads.txt` published on the developer domain | Not code, and it is the publisher's step. |

## Hand over

A short checklist, not a narrative. Split it in two, because that is the shape of this
platform:

**What changed in the project:** the `Plugins/CAS-Unreal` folder, `<Project>.Build.cs`, the
C++ that initializes and shows ads, `Config/DefaultEngine.ini`, the `Target.cs` files on
UE4 — and, listed separately and unmistakably, **any file edited inside the engine
installation**, which every other project on that machine shares and which an engine update
will overwrite.

**What the publisher still does:**

- every field of `CAS.AI Config - Android` and `CAS.AI Config - iOS`, named one by one, with
  the release switches from the table above;
- on a Blueprint-only project, the node wiring, with the per-format instructions already
  written out;
- register the app in the CAS dashboard, confirm the solution, confirm the audience;
- the store answers this integration changes: the Google Play data disclosure, the App Store
  privacy questions, Apple's ad attribution, and the Kids Category if it applies;
- anything still waiting on the account manager, closed beta networks and a Google App ID
  for `GoogleAppIdIOS` included;
- what a tester should not file as a bug: no ads inside a trial ad-free interval or inside
  the interstitial interval, `IntervalNotYetPassed` on a quick second show, no fill in a
  release build before the solution is checked, and no ads at all in the editor.
