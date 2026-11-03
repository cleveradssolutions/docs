# iOS: verify, then hand over

An integration is verified by running it. Reading the diff is not verification.

## Verify

1. Build and run the debug configuration, on a device if the publisher has one, on a
   simulator otherwise. **A simulator cannot be tapped from the command line**, so a
   full screen format cannot be triggered headlessly: either the publisher taps, or the
   verification build presents the ad once from a timer, which is removed afterwards. Say
   which of the two was done.
2. Confirm test ad mode is on for this build. Test full screen ads announce themselves and
   show CAS integration details, including a Mediation Adapters section that lists every
   network that initialized correctly. That screen is the fastest proof the product or pod
   selection is right.
3. Turn the SDK's debug logging on and watch the console. **The consent form comes
   first**: on a clean device in a region that requires consent the built-in flow shows a
   form, and initialization does not complete until it is answered. Nothing loads before
   that, and it is not a failure.
4. Read the initialization handler: `config.error` must be nil. The console line
   `Main > Initialization > complete ... in TEST AD` confirms test mode is really on.
   A `demo` id is reported on the test ad screen as "You are using a DEMO CAS Id", which
   is the reminder to replace it, not an error.
5. For every format that was added: load it, present it, and watch for the load callback
   then the present callback. A format that only loads is not done.
6. **Check what happens after the ad closes**, which is where placements break: the
   screen the user was going to opens after the dismiss and a new ad is loaded; the
   reward is granted from the reward callback and the rewarded ad reloads, so press it
   twice; the banner sits in its reserved space with nothing of the app covered.
7. Leave and re-enter the screen that owns a banner, and confirm nothing keeps loading
   after the owner is gone.
8. Confirm the `Info.plist` holds the keys the script wrote. The test ad screen states
   them as numbers, `SKAdNetwork Support`, `AdAttributionKit Support`, `App Transport
   Security`, `Minimum iOS` and `CASBaseResources.bundle`, so compare that screen with
   what the script reported rather than reading the plist by hand.

Report what actually happened, with the console lines. If a format failed, name the format
and the error code, finish the rest, and say plainly that one is unverified.

## Hand over

A short list for the publisher, not a wall of text:

- what changed in the project, file by file, including `Info.plist` keys and the package
  or pod list;
- **test mode is bound to the debug configuration** - confirm it is off in release, and
  that no test device id is hardcoded;
- the CAS ID used, and that it is the App Store id;
- which solution or adapter products are in, and that the account manager decides anything
  beyond them;
- what is still waiting on the dashboard: app registration, AdMob App ID, closed beta
  adapters;
- the App Store privacy answers and the ATT text are theirs, with links;
- the consent path chosen, and where the privacy options entry lives;
- **re-run the Info.plist script on every SDK update.**

## Before release, the publisher checks

- test ad mode off in the release configuration, test device ids removed;
- debug logging off;
- `NSUserTrackingUsageDescription` present and localized if the app asks for tracking;
- `SKAdNetworkItems` current for the adapter set actually shipping;
- for a child-directed app, that the chosen solution matches the Apple category;
- an archive build runs and shows an ad.
