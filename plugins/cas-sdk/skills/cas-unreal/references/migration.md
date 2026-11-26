# Unreal: migrating from another mediation

CAS cannot run next to another mediation or ad network SDK: both native libraries are
packaged, both initialize, and the user is asked for consent twice. The other SDK is
**replaced**, in the same change, or the work stops and the publisher decides.

On Unreal the previous integration is usually one of two things, and they are not the same
job:

- **The engine's own AdMob support**, switched on under
  `Project Settings > Platform > Android > Google Play Services`, with its ad unit ids and
  its Blueprint nodes. Common, and the trickiest to remove cleanly.
- **A third-party ad plugin** in `Plugins/`, with its own settings section and its own iOS
  frameworks.

## The node names collide, and that is the whole risk

The engine's ad support ships Blueprint nodes called **`Show Interstitial Ad`** and
**`Show Ad Banner`** — the same names as the CAS ones. A graph left pointing at the
engine's node compiles, looks correct in review, and shows nothing.

So on this platform, removal is not deletion: it is **re-pointing every node**, one at a
time, and confirming each one is from the **CAS Mobile Ads** category. Find them before
promising the migration is small:

- search the project for every graph that calls an ad node, including level blueprints and
  widget blueprints, which are easy to miss;
- after re-pointing, switch the engine's AdMob support off, so the old nodes can no longer
  do anything at all;
- in C++, the engine's advertising calls go the same way — they are a different API from
  `UCASMobileAds`, so the compiler finds those for you.

## What has to go

| Leftover | Where | Why it matters |
|---|---|---|
| **Include AdMob support for ads** | `Project Settings > Platform > Android > Google Play Services` | Must be **off**. The CAS plugin configures AdMob itself as part of mediation; both at once is a duplicate integration. |
| The engine's ad unit ids | the same settings section | CAS has no per-placement ad unit in the project; the dashboard holds them. |
| The engine's ad nodes | every Blueprint graph, and any C++ advertising call | Re-pointed, not deleted, as above. |
| A third-party ad plugin | `Plugins/<name>`, its entry in the `.uproject`, its `Config/DefaultEngine.ini` section | A disabled plugin that is still present can still package its frameworks. |
| That plugin's iOS frameworks | what it adds to the package | They are also the usual source of duplicate-symbol failures. If a framework is genuinely shared, it becomes a `+ExcludeFrameworks` entry rather than a deletion — see [troubleshooting.md](troubleshooting.md). |
| Hand-written UPL files for ads | `Build/Android/`, or wherever the project keeps them | Manifest entries, Gradle properties and ad network keys added by hand. The CAS plugin writes its own. |
| Hand-written plist additions | the iOS packaging settings: `GADApplicationIdentifier`, `SKAdNetworkItems` | The CAS plugin writes the SKAdNetwork identifiers of the current adapter set. A hand-written list goes stale and is never updated. |
| Ad network app ids | wherever the old plugin kept them | **Replaced**, not deleted, when Google demand is used: the value has to be the one CAS generated — and on iOS it has a documented fallback, `GoogleAppIdIOS` in `[CASPluginBuildConfig]`. |
| The old consent flow | a CMP plugin, or hand-written UMP code | See the note below: this one is a decision, not a deletion. |

Then, once the adapter set has changed: **delete `Project/Binaries/IOS`**. It caches the
previous build's frameworks, and without that the next package links the old set — which
looks exactly like a migration that did not take.

## The consent platform is the one real decision

A Google UMP integration is **not** a third-party CMP to keep. The built-in CAS consent
flow is itself driven by UMP, so keeping the old consent code means two flows competing for
the same form. Remove it and leave `Auto Consent Flow` checked, unless the publisher uses a
genuinely different platform that they intend to keep. Then uncheck it on both platforms
and apply their decision with `SetUserConsentForAds` before `InitializeMobileAds()` — see
[consent-privacy.md](consent-privacy.md).

## Mapping

| The engine's AdMob support | CAS |
|---|---|
| `Include AdMob support for ads` plus ad unit ids | the CAS App ID and one solution, per platform, in `CAS.AI Config` |
| `Load Interstitial Ad` | `LoadInterstitialAd()`, or autoload in the settings window |
| `Is Interstitial Ad Available` | `IsInterstitialAdReady()` — and do not poll it per frame |
| `Show Interstitial Ad` | `ShowInterstitialAd()`, **from the CAS Mobile Ads category** |
| `Show Ad Banner` / `Hide Ad Banner` | `ShowBannerAd()` / `HideBannerAd()`, with `SetBannerAdPosition` for the placement |
| no rewarded support at all | `LoadRewardedAd()` / `ShowRewardedAd()`, with the reward granted in `OnRewardedAdEarnedReward` |
| no app open, no MREC | `CASAppOpenAd` and the MREC format are new capability, not a port |
| no load/show events to speak of | the `On*Ad*` delegates, which is where pausing and resuming the game belongs |

The last three rows matter for the plan: a migration from the engine's AdMob support is
also a **feature addition**, and the publisher should decide whether rewarded and app open
are in scope now or later rather than discovering them in the hand-over.

## Order

1. Re-point every ad node and every advertising call, then switch the engine's AdMob
   support off and remove the old plugin — in one commit, because half a removal still
   packages two SDKs.
2. Add the plugin, the module dependency and the per-platform prerequisites, see
   [setup-plugin.md](setup-plugin.md). The publisher fills both `CAS.AI Config` panels.
3. Add initialization, see [initialize.md](initialize.md), and settle consent, see
   [consent-privacy.md](consent-privacy.md).
4. Port the formats one by one, see [ad-formats.md](ad-formats.md), keeping the moments the
   game already had unless they break a placement rule. Add the pause and resume around
   full screen ads if the old integration had none.
5. **Package** each platform and run on a device, then verify on test ads and with
   `ValidateAdsIntegration()`, see [verify-and-release.md](verify-and-release.md).

Keep the game's own behaviour. A migration that changes when ads appear is a product change
the publisher did not ask for.

## What the publisher has to do after

Register the app in the CAS dashboard, fill both config panels, take the AdMob App ID from
there if Google demand is used, and remove the old mediation account from the build and
release notes. Their old ad unit ids are not reused anywhere.
