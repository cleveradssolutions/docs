# Android: verify, then hand over

An integration is verified by running it. Reading the diff is not verification.

## Verify

1. Build and install the debug variant.
2. Confirm test ad mode is on for this build. In the log, test full screen ads announce
   themselves and show CAS integration details, including a Mediation Adapters section
   that lists every network that initialized correctly. That screen is the fastest proof
   the adapter set is right.
3. Enable debug logging, `CAS.settings.setDebugMode(true)`, tag `CAS`.
4. Launch and read the initialization callback: `config.error` must be null. If it is not,
   the message names the cause; a wrong or unregistered CAS ID shows up here.
5. For every format that was added: load it, show it, and watch for `onAdLoaded` then
   `onAdShowed`. A format that only loads is not done.
6. Rotate the device or background and foreground the app on a screen with a banner, then
   leave the screen, and confirm `destroy()` runs and nothing keeps loading afterwards.
7. Re-read the log for integration warnings. `CAS.validateIntegration(activity)` reports
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
