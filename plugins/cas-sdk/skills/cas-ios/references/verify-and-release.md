# iOS: verify, then hand over

An integration is verified by running it. Reading the diff is not verification.

## Verify

1. Build and run the debug configuration, on a device if the publisher has one, on a
   simulator otherwise.
2. Confirm test ad mode is on for this build. Test full screen ads announce themselves and
   show CAS integration details, including a Mediation Adapters section that lists every
   network that initialized correctly. That screen is the fastest proof the product or pod
   selection is right.
3. Turn the SDK's debug logging on and watch the console.
4. Read the initialization handler: `config.error` must be nil. If it is not, the message
   names the cause; a wrong or unregistered CAS ID shows up here.
5. For every format that was added: load it, present it, and watch for the load callback
   then the present callback. A format that only loads is not done.
6. Leave and re-enter the screen that owns a banner, and confirm nothing keeps loading
   after the owner is gone.
7. Confirm the `Info.plist` holds the keys the script wrote, and that
   `SKAdNetworkItems` is populated.

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
