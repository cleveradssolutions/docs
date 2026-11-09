# Cordova: initialization

Canonical:
[docs/Cordova/Initialize-CAS.mdx](../../../../../docs/Cordova/Initialize-CAS.mdx) and
[docs/Cordova/Enabling-test-ads.mdx](../../../../../docs/Cordova/Enabling-test-ads.mdx).
Read the page for the current option list; the shape below is what matters around it.

One call, from `deviceready`, returning a `Promise<InitializationStatus>`:

```js
function onDeviceReady() {
  document.removeEventListener('deviceready', onDeviceReady, false);

  casai.initialize({
    forceTestAds: isDebugBuild,
    showConsentFormIfRequired: true,
    targetAudience: 'notchildren',
  }).then((status) => {
    if (status.error) {
      // log it; the SDK retries on its own and this Promise will not fire again
    }
    // third-party SDKs start here, not before
  });
}

document.addEventListener('deviceready', onDeviceReady, false);
```

## `initialize` takes no CAS ID

That is the thing to internalise. The options object carries test mode, the audience, the
consent flow and the test devices — but not the app's identity. Android reads the bundle
package name itself; iOS takes `IOS_CAS_ID` from a `config.xml` variable. See
[setup-plugin.md](setup-plugin.md).

The consequence is that **nothing in the JavaScript tells you which app is being
initialized.** A misconfigured iOS build looks identical to a correct one and quietly
serves test ads. Log `casai.getSDKVersion()` and the platform at startup, and confirm the
variables by reading `config.xml` rather than the sources.

## The things that are easy to get wrong

| What | Why it matters |
|---|---|
| **It must run after `deviceready`** | Before that there is no plugin bridge, and `casai` is undefined. In Ionic, that is the framework's platform-ready hook, not a component's constructor. |
| **The Promise fires once** | On an error the SDK retries internally and the Promise is **not** updated. To see a later status, call `initialize` again. So do not gate the UI on the first result for ever. |
| **Third-party SDKs wait for consent** | Attribution and analytics SDKs are initialized **from inside** the `then`, not before the call. Started earlier, they cannot read the relevant identifiers, and the publisher loses measurement and revenue. This is the one ordering rule with a money consequence. |
| **`forceTestAds` comes from the build** | Cordova has no `__DEV__`. Produce one boolean — from the build pipeline, an environment-specific bundle, or a generated config module — and pass it. A literal `true` ships an app that earns nothing; a literal `false` turns development clicks into invalid traffic. Never a flag a human has to flip. |
| **`debugGeography` is a development setting** | `'eea'`, `'us'`, `'unregulated'`, `'unknown'`. It applies to test sessions, so it belongs behind the same condition as test ads. |
| **The audience strings are lowercase** | `'children'`, `'notchildren'`, or left out for a mixed audience. Not an enum object — plain strings, and the spelling is `notchildren` without a separator. |
| **A browser platform has no SDK** | Everything here fails or is undefined there. Guard the ad code so the browser build still runs; a publisher who develops in the browser will hit this on day one. |

## Where the call goes

Once, in the `deviceready` handler, before any ad code. Two caveats:

- In an Ionic app the `deviceready` equivalent is the framework's platform-ready promise;
  resolve it once at app start rather than per page, and keep a module-level flag so a hot
  reload during development does not initialize twice.
- If the app shows a splash screen, that is the natural place to also show an app open ad,
  and that format wants the splash still up. Plan the two together rather than bolting the
  ad on later.

## Test devices

`testDeviceIds: ['...']` makes a release build serve test ads on named devices, which is
how a publisher tests a store build safely. The ids are printed in the native device log on
first run — read them from there, never invent one, and keep them out of the shipped array
unless the publisher wants them there.

## Initialization status

`status.error` is null on success. `status.countryCode` is the ISO-2 country or null,
`status.isConsentRequired` says whether a privacy options button is needed, and
`status.consentFlowStatus` carries the flow result — see
[consent-privacy.md](consent-privacy.md).

## Optional, and worth knowing before a tester reports a bug

- `casai.setDebugLoggingEnabled(true)` — verbose native logging. Turn it on for the
  verification build; it is what makes every other question answerable.
- `casai.getSDKVersion()` — log it once at startup with the platform.
- `casai.setAdSoundsMuted(true)` — for an app that manages its own audio.
- `casai.setTrialAdFreeInterval(604800)` — seconds from install during which no ads show
  except rewarded. An ad that does not appear inside that window is correct behaviour; say
  so in the hand-over or it gets filed as a bug.
- Targeting, all optional and all from
  [Targeting-options.mdx](../../../../../docs/Cordova/Targeting-options.mdx):
  `setUserAge`, `setUserGender('male')`, `setLocationCollectionEnabled`, `setAppKeywords`,
  `setAppContentUrl`. Set them before loading ads, and only with values the publisher
  actually has.
- The `casai_ad_impressions` document event carries `revenue`, `sourceName`,
  `sourceUnitId`, `creativeId`, `impressionDepth` and `revenueTotal` — see
  [Impression-Level-Data.mdx](../../../../../docs/Cordova/Impression-Level-Data.mdx). CAS
  can also report revenue to Google Analytics and Tenjin automatically; ask the account
  manager to enable it rather than wiring it by hand.
