# Android: verify, then hand over

An integration is verified by running it. Reading the diff is not verification.

## Verify

1. Build and install the debug variant.
2. Enable debug logging, `CAS.settings.debugMode = true`. **The log tag is `CAS.AI`**:
   `adb logcat -s CAS.AI:V`. Filtering on `CAS` returns nothing and looks like a dead
   integration.
3. Launch. **The consent form comes first.** On a clean device in a region that requires
   consent, the built-in flow shows a Google UMP form, and initialization does not
   complete until someone answers it. Nothing loads before that, and it is not a failure.
   Dismiss it, then continue.
4. Read the initialization callback: `config.error` must be null. If the callback never
   arrives, the consent form is still up, or the app is waiting on the network.
   An unregistered CAS ID logs
   `Main > Initialization > CAS ID is not registered in system: <id>` as an error and then
   `Apply test ads mode configuration`: the integration still runs on test ads, and the
   publisher registers the app before anything can serve live.
5. Show each full screen format. A test ad opens the CAS integration screen with a
   **Mediation adapters N/M** row, the CAS ID, the SDK version, the minimum API, the
   consent state and the advertising id permission. That screen, not the log, is the
   fastest proof the adapter set is right; open the adapters row and look for the ones
   that did not initialize.
6. For every format that was added: load it, show it, and watch for `onAdLoaded` then
   `onAdShowed`. A format that only loads is not done.
7. **Check what happens after the ad closes**, which is where placements break:
   - the interstitial: the screen the user was going to actually opens, after the dismiss,
     and a new ad is loaded;
   - the rewarded: the reward is granted from the reward listener, and the ad reloads, so
     a second press works. Press it twice;
   - the banner: it sits in its reserved space, nothing of the app is covered, and the
     bottom of the creative is not clipped.
8. Rotate the device or background and foreground the app on a screen with a banner, then
   leave the screen, and confirm `destroy()` runs and nothing keeps loading afterwards.
9. Re-read the log for integration warnings. `CAS.validateIntegration(activity)` reports
   integration status in debug builds and is worth one run.

Report what actually happened, with the log lines. If a format failed, name the format and
the error code, finish the rest, and say plainly that one is unverified.

## Hand over

A short list for the publisher, not a wall of text:

- what changed in the project, file by file;
- **test mode is bound to the debug build** - confirm it is off in the release variant,
  and that no test device id is hardcoded;
- the CAS ID used, and for each flavour if there are several;
- which solution or adapters are in, and that the account manager decides anything beyond
  them;
- what is still waiting on the dashboard: app registration, AdMob App ID, closed beta
  adapters, cross promotion;
- the store data disclosure is theirs to fill in, with a link to the relevant page;
- the consent path chosen, and where the privacy options entry lives.

## Before release, the publisher checks

- test ad mode off in the release build, test device ids removed;
- debug logging off;
- the Families policy settings match the app's real audience, if it is child-directed;
- the advertising identifier setting matches the audience and the target devices;
- a release build with minification enabled installs and shows an ad.
