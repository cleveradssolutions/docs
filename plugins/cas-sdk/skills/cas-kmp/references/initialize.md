# Kotlin Multiplatform: initialization

Canonical:
[docs/kmp/initialize-cas.mdx](../../../../../docs/kmp/initialize-cas.mdx)
and
[docs/kmp/enabling-test-ads.mdx](../../../../../docs/kmp/enabling-test-ads.mdx).
Read the page for the current argument list; the shape below is what matters around it.

One call, from `commonMain`. It returns immediately and reports through a listener:

```kotlin
import com.cleveradssolutions.plugin.kmp.CASInitializationListener
import com.cleveradssolutions.plugin.kmp.CASMobileAds
import com.cleveradssolutions.plugin.kmp.CASTaggedAudience

fun initializeAds(context: Any?, isDebugBuild: Boolean) {
    CASMobileAds.initialize(
        // Android: an Activity, Application or any other Context. iOS: null.
        context = context,
        casId = casId,
        forceTestAds = isDebugBuild,
        showConsentFormIfRequired = true,
        targetAudience = CASTaggedAudience.NOT_CHILDREN,
        listener = CASInitializationListener { config ->
            if (config.error != null) {
                // log it; the SDK retries on its own and this listener will not fire again
            }
            // third-party SDKs start here, not before
        },
    )
}
```

## The CAS ID is an expect/actual, not a constant

Android takes the application id, iOS takes the **App Store id**. The documentation spells
out the declaration, and it is the right one:

```kotlin
// commonMain
expect val casId: String

// androidMain
actual val casId: String = BuildConfig.APPLICATION_ID   // or the literal package name

// iosMain
actual val casId: String = "123456789"
```

A single literal shared by both platforms is a bug on one of them, and it surfaces only as
a configuration error at ad load — error code 10, which says nothing about which id was
wrong. `CASMobileAds.TEST_ID` is the demo id, and it serves test ads on every device.

## The things that are easy to get wrong

| What | Why it matters |
|---|---|
| **`context` is `Any?`** | Android uses it, iOS ignores it. So shared code passes whatever the platform already has without branching: the `Activity` from `MainActivity.onCreate`, and `null` from `MainViewController`. An `Any?` parameter that is silently unused on one platform is easy to fill with something wrong; pass the Activity, not a view or a coroutine scope. |
| **The listener fires once** | On an error the SDK reinitializes internally and the listener is **not** called again. To see a later status, call `initialize` again. So do not gate the UI on the first result for ever. |
| **Third-party SDKs wait for consent** | Attribution and analytics SDKs are initialized **from inside** the listener, not before the call. Started earlier, they cannot read the relevant identifiers, and the publisher loses measurement and revenue. This is the one ordering rule with a money consequence. |
| **`forceTestAds` comes from the build** | A literal `true` ships an app that earns nothing; a literal `false` turns development clicks into invalid traffic. Kotlin Multiplatform has no `__DEV__`, so the value has to be produced: an `expect val isDebugBuild: Boolean`, actualised from `BuildConfig.DEBUG` on Android and from a build-configuration flag of the Xcode target on iOS. Never a flag a human has to flip. |
| **`debugGeography` is a development setting** | In `initialize` it applies only to test sessions, which makes it safe here — but it is **not** safe on `showConsentForm`, where it stays in effect in production. See `consent-privacy.md`. |
| **Audience is a declaration, not a filter** | `CASTaggedAudience.UNDEFINED`, `.CHILDREN`, `.NOT_CHILDREN`. It has to match what the publisher declared in the CAS dashboard and in both stores. The agent asks; it does not decide. |
| **Skipping initialize works, and should not be relied on** | The first ad load initializes the SDK with the CAS ID of the ad instance. That is a fallback, not a design: it skips the consent flow ordering and gives no status. Call `initialize`. |

`CASMobileAds.isInitialized` answers whether it already completed, which is what a screen
that was composed late should check instead of waiting for a listener that already fired.

## Where the call goes

Once, as early as the shared code has a platform context. Two shapes, both correct:

- **Compose Multiplatform:** a `LaunchedEffect(Unit)` in the root composable, with the
  context passed into `App(context = ...)` — the Activity on Android,
  `null` from `MainViewController`.
- **Platform entry points:** a `commonMain` function called from `MainActivity.onCreate`
  and from the iOS app's startup. Better when the UI is native per platform.

Guard it against a second call. On Android a configuration change recreates the Activity,
and a `LaunchedEffect` keyed on anything but `Unit` runs again; a module-level
`private var initialized = false`, or `CASMobileAds.isInitialized`, is enough.

If the app shows a splash or loading screen, that is the natural place to also show an app
open ad, and that format wants the loading screen still up. Plan the two together rather
than bolting the ad on later.

## Test devices

`testDeviceIds = setOf("...")` makes a release build serve test ads on named devices, which
is how a publisher tests a store build safely. The ids are printed in the native device log
on first run — read them from there, never invent one, and keep them out of the shipped set
unless the publisher wants them there.

## Optional, and worth knowing before a tester reports a bug

- `CASMobileAds.debugLoggingEnabled = true` — the `CAS` tag in the native log. The one
  switch that makes every other question answerable; turn it on for the verification build.
- `CASMobileAds.sdkVersion` — log it once at startup, together with the CAS ID actually
  used. It is the fastest answer to "which platform is misconfigured".
- `CASMobileAds.adSoundsMuted = true` — for an app that manages its own audio.
- `CASMobileAds.trialAdFreeInterval = 604800` — seconds from install during which no ads
  show except rewarded. An ad that does not appear inside that window is correct behaviour;
  say so in the hand-over or it gets filed as a bug.
- `CASMobileAds.targetingOptions` — `userId`, `age`, `gender`, `keywords`, `contentUrl`,
  `locationCollectionEnabled`, all optional, all from
  [Targeting-options.mdx](../../../../../docs/kmp/targeting-options.mdx). Set them before
  loading ads, and only with values the publisher actually has.
- `mediationExtras = mapOf(...)` — key-value settings passed to the networks, used only
  when a network's documentation asks for one.

## Initialization result

`CASInitializationResult` carries `error` (null on success), `countryCode`,
`isConsentRequired` and `consentFlowStatus`. The last one is what a privacy options button
is gated on; see `consent-privacy.md`.
