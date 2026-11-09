# Unreal: initialization

Canonical:
[docs/Unreal/Initialize-CAS.mdx](../../../../../docs/Unreal/Initialize-CAS.mdx),
[Enabling-test-ads.mdx](../../../../../docs/Unreal/Enabling-test-ads.mdx) and
[Verify-Integration.mdx](../../../../../docs/Unreal/Verify-Integration.mdx).
Read the pages for the current signatures; the shape below is what matters around them.

Everything is static on `UCASMobileAds`. There are no ad objects and no SDK instance to
hold — which also means there is nothing to pass around and nothing whose lifetime can be
got wrong.

```cpp
#include "CASMobileAds.h"

UCASMobileAds::OnAdsInitialized.AddLambda([this](const FCASInitialConfig& Config) {
    if (Config.Error.IsEmpty()) {
        // initialized; start attribution and analytics SDKs here, not earlier
    }
    FString Country = Config.CountryCode;
    ECASConsentFlowStatus Status = Config.ConsentFlowStatus;
});

UCASMobileAds::InitializeMobileAds();
```

**Subscribe before calling `InitializeMobileAds()`.** The configuration — CAS App ID, test
mode, audience, solution, consent flow — comes from the `CAS.AI Config` window, so this call
takes no arguments and nothing in the code says which app it is initializing.

## The things that are easy to get wrong

| What | Why it matters |
|---|---|
| **`OnAdsInitialized` can fire more than once** | On an error the SDK reinitializes and **the event is called again until the error is resolved**. This is the opposite of most CAS plugins, where the listener fires once. So the handler must be idempotent: a third-party SDK started from it would otherwise be started twice. Guard it. |
| **No CAS ID in the code** | Both ids live in the two settings panels. An agent cannot verify them by reading the project, so log the SDK version and the platform at startup and ask the publisher to confirm the ids on screen. A wrong id surfaces only as a configuration error at ad load. |
| **Third-party SDKs wait for consent** | Attribution and analytics SDKs are started **from inside** `OnAdsInitialized`, which fires only after the consent dialog is dismissed. Started earlier, they cannot read the relevant identifiers, and the publisher loses measurement and revenue. This is the one ordering rule with a money consequence. |
| **Test mode is a checkbox, not a build flag** | There is no `#if UE_BUILD_DEBUG` to bind it to. `TestAdsMode` is per platform in the settings window, so turning it off before release is a human step — put it at the top of the hand-over checklist, and never ship an integration whose safety depends on someone remembering. |
| **Where the call goes** | Once, at app launch: `GameInstance::Init`, or the `BeginPlay` of a persistent startup actor. Not in a level that can be reloaded, and not per level. In Blueprint, the equivalent node on the game instance. |
| **There is no SDK in the editor** | Play-in-editor initializes nothing. Every verification is a packaged build on a device. |

`UCASMobileAds::IsInitializedAds()` answers whether it already completed, which is what
code reached after initialization should check instead of waiting for an event that already
fired.

## Test devices

The device id is printed in the device log on first run:

```
To get test ads on this device, set "12345678901234567890123456789012"
```

Read it from there, never invent one, and put it in the settings window. A test device
serves test ads with production-looking fill while the CAS App ID stays real, which is how
a release build is tested safely. Remove the ids before release.

## Verify the integration, then remove the call

```cpp
UCASMobileAds::ValidateAdsIntegration();
```

Logs under the tag **CASIntegrationHelper**, and marks every network VERIFIED, MISSING or
NOT VERIFIED. MISSING is expected for networks that were deliberately not included;
**NOT VERIFIED** is the real finding. This is the fastest proof the solution checkbox and
the framework integration actually worked, and it is per platform.

**Remove the call once it has been read.** It is a development tool, and the documentation
says so.

## Optional, and worth knowing before a tester reports a bug

- `UCASMobileAds::SetVerboseAdsLogs(true)` — the `CAS.AI` tag in the device log. Turn it on
  for the verification build; it is what makes every other question answerable. Off by
  default, and leaving it on costs performance.
- `UCASMobileAds::GetMobileAdsVersion()` — log it once at startup together with the
  platform. On a two-platform integration that one line answers most of what goes wrong.
- `UCASMobileAds::SetTrialAdFreeInterval(604800)` — seconds from install during which no ads
  show **except Rewarded and App Open**. An ad that does not appear inside that window is
  correct behaviour; say so in the hand-over or it gets filed as a bug.
- `UCASMobileAds::SetAdsMuted(true)` — for a game that manages its own audio.
- `UCASMobileAds::SetInterstitialAdMinimumInterval(Seconds)` — pacing. The documentation
  recommends setting it **before** initialization, so the value can be overridden remotely
  from the dashboard.
- Targeting, all optional and all from
  [Targeting-options.mdx](../../../../../docs/Unreal/Targeting-options.mdx):
  `SetUserIDForAds`, `SetUserAgeForAds`, `SetUserGenderForAds(ECASGender::Male)`. Set them
  before loading ads, and only with values the publisher actually has.
- `UCASMobileAds::OnAdsImpression` with `FCASImpressionInfo` — `RevenueCPM`, `Network`,
  `UnitID`, `CreativeID`, `PlacementType`, `TotalImpressions`, `LifetimeRevenue`. The page
  is [Impression-Level-Data.mdx](../../../../../docs/Unreal/Impression-Level-Data.mdx), and
  the plugin can also report revenue to Google Analytics and Tenjin automatically — ask the
  account manager to enable it rather than wiring it by hand.
- `app-ads.txt` on the publisher's domain. Not code, but it belongs in the hand-over.
