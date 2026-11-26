# React Native: initialization

Canonical:
[docs/reactnative/initialize-cas.mdx](../../../../../docs/reactnative/initialize-cas.mdx)
and
[docs/reactnative/enabling-test-ads.mdx](../../../../../docs/reactnative/enabling-test-ads.mdx).
Read the page for the current option list; the shape below is what matters around it.

One call does everything. The CAS ID is a positional argument and the rest is an options
object, and it returns a `Promise<InitializationStatus>`:

```ts
import { Platform } from 'react-native';
import CASMobileAds, {
  InitializationStatus, Audience, ConsentFlowStatus,
} from 'react-native-cas';

const casId = Platform.select({
  ios: '<iOS App Store id>',
  android: '<Android application id>',
});

CASMobileAds.initialize(casId, {
  forceTestAds: __DEV__,
  showConsentFormIfRequired: true,
  targetAudience: Audience.NOT_CHILDREN,
}).then((status: InitializationStatus) => {
  if (status.error) {
    // log it; the SDK retries on its own and this Promise will not fire again
  }
});
```

## The things that are easy to get wrong

| What | Why it matters |
|---|---|
| **Two CAS IDs** | Android takes the application id, iOS takes the **App Store id**. `Platform.select` is the switch; a single constant is a bug on one of the two platforms, and it surfaces only as a configuration error at ad load. |
| **The option is `targetAudience`** | Not `audience` as on Flutter. The enum is `Audience.NOT_CHILDREN` and its siblings — screaming case here, unlike the Dart names. |
| **`forceTestAds: __DEV__`** | That is the whole rule. A literal `true` ships an app that earns nothing; a literal `false` turns development clicks into invalid traffic. Never a flag a human has to flip. |
| **The Promise fires once** | On an error the SDK retries internally and this `Promise` is **not** updated. To see a later status, call `initialize` again. So do not gate the UI on the first result for ever. |
| **Third-party SDKs wait for consent** | Attribution and analytics SDKs are initialized **from inside** the `then`, not before the call. Started earlier, they cannot read the relevant identifiers, and the publisher loses measurement and revenue. This is the one ordering rule with a money consequence. |
| **`debugGeography` is a development setting** | `PrivacyGeography.EUROPEAN_ECONOMIC_AREA` forces the flow to behave as if the device were in the EEA. It stays in effect in production sessions, so it belongs behind the same `__DEV__` condition as test ads. |
| **Audience is a declaration, not a filter** | It has to match what the publisher declared in the CAS dashboard and in both stores. The agent asks; it does not decide. |

## Where the call goes

Once, as early as the app has a JavaScript entry point: the root component's `useEffect`
with an empty dependency array, or a module-level init called from `index.js`. Two caveats:

- **`useEffect` runs again on a fast refresh.** Guard it so a reload during development does
  not call `initialize` repeatedly; a module-level `let initialized = false` is enough.
- If the app shows a splash or loading screen, that is the natural place to also show an app
  open ad, and that format wants the loading screen still up. Plan the two together rather
  than bolting the ad on later.

## Test devices

`testDeviceIds: [...]` makes a release build serve test ads on named devices, which is how a
publisher tests a store build safely. The ids are printed in the native device log on first
run — read them from there, never invent one, and keep them out of the shipped list unless
the publisher wants them there.

## Optional, and worth knowing before a tester reports a bug

- `CASMobileAds.setAdSoundsMuted(bool)` — for an app that manages its own audio.
- `InterstitialAd.setMinInterval(seconds)` and `InterstitialAd.restartInterval()` — pacing.
  An interstitial refused inside that window reports `AdErrorCode.NOT_PASSED_INTERVAL`,
  which is correct behaviour, not a defect. The interval timer is **shared across all
  interstitial instances**, while the value can differ per instance.
- A trial ad-free interval, if the publisher uses one, has the same effect: an ad that does
  not show inside the window is right. Say so in the hand-over or it gets filed as a bug.
