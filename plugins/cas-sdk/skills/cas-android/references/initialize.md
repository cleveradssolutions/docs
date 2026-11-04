# Android: initialization

Canonical, in this repository: [docs/Android/Initialize-CAS.mdx](../../../../../docs/Android/Initialize-CAS.mdx)
Published: `https://cleveradssolutions.docs.page/docs/Android/Initialize-CAS`
Samples: `kotlinSample/.../SampleApplication.kt`, `javaSample/.../SampleApplication.java`
in `https://github.com/cleveradssolutions/CAS-Android`

## Before the code

Three things the documentation page does not spell out, each of which stops the
integration dead:

- The `Application` subclass has to be registered in `AndroidManifest.xml` as
  `android:name=".MyApplication"`. An unregistered class simply never runs, and nothing
  initializes.
- `BuildConfig` needs `buildFeatures { buildConfig = true }` on AGP 8, otherwise
  `BuildConfig.APPLICATION_ID` and `BuildConfig.DEBUG` do not resolve.
- **The Kotlin snippets on the documentation page use Java setters.** `setDebugMode`,
  `setTaggedAudience`, `setTrialAdFreeInterval`, `setUserConsent` and `setCcpaStatus` are
  Kotlin **properties** on `CAS.settings`: write `CAS.settings.debugMode = true`. The
  snippet as printed does not compile from Kotlin.

## Where it goes

Once, at app launch, normally in `Application.onCreate()`. The CAS ID is a constant, not
`context.packageName`, which is not safe to use here.

```kotlin
class MyApplication : Application() {
    companion object {
        const val CAS_ID: String = BuildConfig.APPLICATION_ID
    }

    override fun onCreate() {
        super.onCreate()
        initializeCAS()
    }
}
```

The SDK initializes itself before the first ad load if nobody calls it, but an explicit
call is what gives access to the configuration and to the consent flow state, so make it.

## The call

```kotlin
import com.cleversolutions.ads.InitialConfiguration
import com.cleversolutions.ads.InitializationListener
import com.cleversolutions.ads.android.CAS

private fun initializeCAS() {
    CAS.settings.debugMode = BuildConfig.DEBUG

    val builder = CAS.buildManager()
        .withCasId(CAS_ID)
        .withTestAdMode(BuildConfig.DEBUG)
        .withCompletionListener(object : InitializationListener {
            override fun onCASInitialized(config: InitialConfiguration) {
                val initErrorOrNull: String? = config.error
                val userCountryISO2OrNull: String? = config.countryCode
                val protectionApplied: Boolean = config.isConsentRequired
                val consentFlowStatus: Int = config.consentFlowStatus

                // Third party SDKs that need consent go here, not earlier.
            }
        })

    builder.build(this)
}
```

`withTestAdMode(BuildConfig.DEBUG)` is not optional in a development build. It is the one
line that keeps the publisher's account out of invalid traffic.

`onCASInitialized` can arrive with an error. The SDK keeps retrying and calls the listener
again, so treat the error as information, not as a terminal state.

## What must not happen around it

- No ad network initializer anywhere in the app. CAS starts them.
- No ad request before the callback. Create ad instances freely, request after.
- No MMP or analytics initialization before the callback. Started earlier, they cannot
  read the identifiers the consent decides on, and attribution and revenue reporting
  suffer.

## Audience

For a child-directed app, before initialization:

```kotlin
CAS.settings.setTaggedAudience(Audience.CHILDREN)
```

An app that mixes children and adults needs a neutral age screen, an age gate that does
not nudge the user into misreporting, and everything shown to a child has to stay
COPPA-safe. This is the publisher's decision; implement what they state, never guess it.

## Debug logging

```kotlin
CAS.settings.debugMode = true
```

**The log tag is `CAS.AI`, not `CAS`.** `adb logcat -s CAS:V` shows nothing at all, which
reads like a dead integration. Use `adb logcat -s CAS.AI:V`. Keep debug logging on while
verifying and bind it to the debug build, it is noisy and costs performance.

## Legacy API

`MediationManager` still works and is still supported, but new code uses `CASInterstitial`,
`CASRewarded`, `CASAppOpen`, `CASBannerView` and `CASNativeLoader`. Do not migrate a
publisher's working legacy code unless they ask for it.
