# Android: initialization

Canonical, in this repository: [docs/Android/Initialize-CAS.mdx](../../../../../docs/Android/Initialize-CAS.mdx)
Published: `https://docs.page/cleveradssolutions/docs/Android/Initialize-CAS`
Samples: `kotlinSample/.../SampleApplication.kt`, `javaSample/.../SampleApplication.java`
in `https://github.com/cleveradssolutions/CAS-Android`

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
private fun initializeCAS() {
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
CAS.settings.setDebugMode(true)
```

Log tag `CAS`. Keep it on while verifying and turn it off for release; it is noisy and
costs performance.

## Legacy API

`MediationManager` still works and is still supported, but new code uses `CASInterstitial`,
`CASRewarded`, `CASAppOpen`, `CASBannerView` and `CASNativeLoader`. Do not migrate a
publisher's working legacy code unless they ask for it.
