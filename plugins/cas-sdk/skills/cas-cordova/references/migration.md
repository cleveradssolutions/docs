# Cordova: migrating from another mediation

CAS cannot run next to another mediation or ad network SDK: both native libraries are
linked, both initialize, and the user is asked for consent twice. The other SDK is
**replaced**, in the same change, or the work stops and the publisher decides.

Most Cordova migrations come from an AdMob plugin — `admob-plus-cordova`,
`cordova-plugin-admob-free` or one of their forks — so that is what the tables name. An
AppLovin or IronSource Cordova plugin is the same job with different symbols.

## What has to go

Search the web sources, `config.xml` and `package.json`. Remove **all** of it, not only
the plugin entry:

| Leftover | Where | Why it matters |
|---|---|---|
| The plugin | `<plugin>` in `config.xml`, the `cordova.plugins` list in `package.json`, and `plugins/` | An unused ad plugin still ships its native SDK and its initializer. `cordova plugin rm` removes it properly; deleting the directory does not. |
| Its plugin variables | the `<variable>` tags inside that `<plugin>` | `APP_ID_ANDROID`, `APP_ID_IOS` and their relatives. They are the old integration's configuration, and they are easy to leave behind because they look like text. |
| The SDK initializer | `admob.start()`, `admob.initialize()` or the equivalent in the `deviceready` handler | CAS initializes the networks. A second initializer is the classic broken migration. |
| Ad unit ids | the per-format id constants, and the usual test/production ternary | CAS has no per-placement ad unit in the app; the dashboard holds them, and `forceTestAds` replaces the ternary. |
| Request configuration | child-directed flags, content rating, test device arrays | Becomes the `targetAudience` and `testDeviceIds` options of `casai.initialize`. |
| Native keys added by hand | `<edit-config>` and `<config-file>` blocks in `config.xml` for `com.google.android.gms.ads.APPLICATION_ID`, `GADApplicationIdentifier`, `SKAdNetworkItems` | **Replaced**, not deleted, when Google demand is used: the value has to be the one CAS generated. The CAS tooling writes the SKAdNetwork list itself, and a hand-written one goes stale. |
| Hooks that patch the native projects | `hooks/`, and any script that edits Gradle or the `Info.plist` for ads | They will keep patching after the plugin is gone, and the failure looks like a CAS bug. |
| The consent platform | a UMP plugin, or consent code inside the ad plugin | See the note below: this one is a decision, not a deletion. |
| Banner layout workarounds | CSS added for the old plugin's banner — padding, a spacer div, a resize listener | Keep the **room**, drop the workaround. The CAS banner is also a native overlay and still needs space reserved, so this is a port rather than a deletion. |

After removing the old plugin and adding CAS, re-add the CAS plugin once more if any
variable changed in the meantime — `config.xml` variables apply at install time, not at
build time.

## The consent platform is the one real decision

A Google UMP integration is **not** a third-party CMP to keep. The built-in CAS consent
flow is itself driven by UMP, so keeping the app's own consent code means two flows
competing for the same form. Remove it and let `showConsentFormIfRequired: true` run,
unless the publisher uses a genuinely different platform — Didomi, Usercentrics — that they
intend to keep. Then the CAS flow stays off and their decision is applied before
`initialize`.

## Mapping

| An AdMob Cordova plugin | CAS |
|---|---|
| `admob.start()` in `deviceready` | `casai.initialize({ ... })` in `deviceready` |
| an app id per platform, as plugin variables | Android automatic; iOS `IOS_CAS_ID` only |
| an ad unit id per format | nothing — the dashboard holds them |
| `admob.interstitial.load({ id })` | `casai.interstitialAd.load({ autoReload: true })` |
| `admob.interstitial.show()` | `await casai.interstitialAd.show()`, which **resolves on dismissal** |
| `admob.rewarded.on('reward')` | the resolved `info.isUserEarnReward`, or the `casai_ad_reward` event |
| `admob.banner.show({ position })` | `casai.bannerAd.show({ position: casai.Position.BOTTOM_CENTER })` |
| `admob.banner.hide()` | `casai.bannerAd.hide()` |
| per-plugin event names | the global `casai_ad_*` document events, each checked against `event.format` |
| a test id constant swapped for a real one | `forceTestAds`, bound to the build |

> **`show()` resolving on dismissal changes the control flow.** Plugins that fire an event
> for dismissal make the caller register a listener; here `await show()` is that point, and
> the `catch` is the failure branch. A migration that keeps the old listener structure ends
> up with two paths doing the same thing, or with a user stranded when a show fails. Decide
> on one style — Promises or document events — and port to it.

## Order

1. Remove, in one commit, everything in the first table except the app id decision:
   `cordova plugin rm <old-plugin>`, then the variables, the hooks and the ad code.
2. Add `cordova-plugin-casai` with its variables, see
   [setup-plugin.md](setup-plugin.md).
3. Add `casai.initialize` in the `deviceready` handler, see
   [initialize.md](initialize.md), and settle consent, see
   [consent-privacy.md](consent-privacy.md).
4. Port the formats one by one, see [ad-formats.md](ad-formats.md), keeping the placements
   the app already had unless they break a placement rule — and keeping the layout room the
   old banner had.
5. Build **both** platforms and run on a device, then verify on test ads, see
   [verify-and-release.md](verify-and-release.md).

Keep the app's own behaviour. A migration that changes when ads appear is a product change
the publisher did not ask for.

## What the publisher has to do after

Register the app in the CAS dashboard, supply the App Store id for `IOS_CAS_ID`, take the
AdMob App ID from the dashboard if Google demand is used, and remove the old mediation
account from the build and release notes. Their old ad unit ids are not reused anywhere.
