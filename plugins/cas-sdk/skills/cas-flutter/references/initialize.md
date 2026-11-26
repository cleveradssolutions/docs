# Flutter: initialization

Canonical:
[docs/flutter/initialize-cas.mdx](../../../../../docs/flutter/initialize-cas.mdx) and
[docs/flutter/enabling-test-ads.mdx](../../../../../docs/flutter/enabling-test-ads.mdx).
Read the page for the current argument list; the shape below is what matters around it.

One call does everything: the CAS ID, test mode, the audience, the consent flow and the
test devices are all arguments of `CASMobileAds.initialize`, and it returns a
`Future<InitializationStatus>`.

```dart
final casId = Platform.isAndroid ? '<android application id>' : '<iOS App Store id>';

CASMobileAds.initialize(
  casId: casId,
  forceTestAds: kDebugMode || kProfileMode,
  showConsentFormIfRequired: true,
  audience: Audience.notChildren,
).then((InitializationStatus status) {
  if (status.error != null) {
    // log it; the SDK retries on its own and this Future will not fire again
  }
});
```

## The things that are easy to get wrong

| What | Why it matters |
|---|---|
| **Two CAS IDs** | Android takes the application id, iOS takes the **App Store id**. A single string for both is wrong unless both are `demo`. `Platform.isAndroid` is the switch; a constant is a bug on one of the two platforms. |
| **`forceTestAds` bound to the build** | `kDebugMode \|\| kProfileMode` is the whole rule. A literal `true` ships an app that earns nothing; a literal `false` turns development clicks into invalid traffic. Never a plain bool a human has to flip. |
| **The Future fires once** | On an error the SDK retries internally and this `Future` is **not** updated. To see a later status, call `initialize` again. So do not build a UI that waits forever on the first result. |
| **Initialization is optional, in a bad way** | If `initialize` is never called, the first ad load initializes the SDK with whatever CAS ID the load carries. That hides a missing call until something else breaks, so call it explicitly in `main`. |
| **Third-party SDKs wait for consent** | Attribution and analytics SDKs must be initialized **from inside** the completion callback, not before it. Started earlier, they cannot read the relevant identifiers, and the publisher loses measurement and revenue. This is the one ordering rule with a money consequence. |
| **Audience is a declaration, not a filter** | `Audience.notChildren` and its siblings have to match what the publisher declared in the CAS dashboard and in the stores. The agent asks; it does not decide. |

## Where the call goes

In `main()`, before `runApp`, is the usual place, and the one the example app uses. Two
caveats:

- `initialize` is async and `main` is not a good place to await it. Fire it and let the
  `Future` resolve while the first frame renders; nothing in the UI should block on it.
- If the app shows a splash or loading screen, that is the natural place to also show an
  app open ad, and the app open format wants the loading screen still up. Plan the two
  together rather than bolting the ad on later.

## Test devices

`testDeviceIds: [...]` makes a release build serve test ads on named devices, which is how
a publisher tests a store build safely. The ids are printed in the device log on first run
— read them from there, never invent one, and keep them out of the shipped list unless the
publisher wants them there.

## Optional, and worth knowing before a tester reports a bug

- `CASMobileAds.setTrialAdFreeInterval(seconds)` — a window after install during which no
  ads show except rewarded. An ad that does not appear inside that window is correct
  behaviour. Say so in the hand-over or it gets filed as a defect.
- `CAS.getSDKVersion()` — the runtime version, useful in a bug report.
- `CASMobileAds.setAdSoundsMuted(bool)` — for an app that manages its own audio session.
