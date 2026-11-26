# Cordova: research the project, then ask

The shape of a question is in [question-format.md](question-format.md), the same in every
CAS guide. This file is the Cordova questions only; every platform has its own, because
the answers that matter are not the same.

## Research, read-only

| What | Where to look |
|---|---|
| Not a Cordova app at all | No `config.xml` with a `<widget>` root. `pubspec.yaml`, `react-native` in `package.json`, a Kotlin Multiplatform `shared` module, an Unreal `.uproject`, a Unity `ProjectSettings/`, or a bare Android or Xcode project each belong to their own CAS skill or guide. Stop and say which one. |
| **Which platforms are added** | `platforms/` and the `<platform name="...">` sections of `config.xml`. A `browser` platform takes no ads and must not break — guard the ad code behind `deviceready` and a platform check. |
| **Cordova or Capacitor** | A `capacitor.config.*` beside `config.xml`. Capacitor runs Cordova plugins but does not consume `config.xml` plugin variables the same way; confirm how this project supplies them before planning anything. |
| The framework on top | Ionic with Angular, React or Vue, or plain Cordova. It does not change the API, but it decides where `deviceready` is handled and who owns the layout a native banner has to fit into. |
| Bundle id and App Store id | The `id` attribute of `<widget>` gives the Android CAS ID, which the plugin reads automatically. The **App Store id** is what `IOS_CAS_ID` needs, and it is a number only the publisher has. |
| Minimum platform versions | `deployment-target` for iOS and `android-targetSdkVersion` in `config.xml`. The setup page states the minimums, and they are a prerequisite rather than a suggestion. |
| CAS already present | `cordova-plugin-casai` in `config.xml` and `package.json`, `casai.` anywhere in the web sources, and which variables are already set. Then: which solution per platform, and whether `forceTestAds` is tied to the build. |
| Other ad plugins | `cordova-plugin-admob-free`, `cordova-plugin-admob-plus`, `admob-plus-cordova`, an AppLovin or IronSource Cordova plugin, and their initialize calls. Two mediation SDKs cannot run side by side. |
| Not an ad SDK, do not touch | Attribution and analytics plugins, in-app purchase plugins, the consent platform. If unsure whether something is an ad SDK, ask. |
| Native leftovers of an old integration | Ad network keys in `config.xml` `<edit-config>` and `<config-file>` blocks, `GADApplicationIdentifier`, `SKAdNetworkItems`, hand-added Gradle lines in a `<framework>` or a hook. The CAS tooling writes its own. |
| Consent platform | A UMP plugin, Didomi, Usercentrics, or none. |
| Ad formats used today | The calls of an existing ad plugin, and whether a banner has a fixed position. They are the formats and placements to reproduce. |
| **Where a banner would fit** | The page layout and the CSS. A Cordova banner is a **native view over the WebView**: it covers whatever is under it, and nothing in the DOM knows it is there. Find the fixed footer, the tab bar, the safe-area padding, and say which one has to give up room. |
| Where the full screen moments are | Route changes, level ends, a reward button. In Ionic, the router events; in plain Cordova, whatever drives the page switch. |
| Hooks and build scripts | `hooks/`, and any script that rewrites `config.xml`. A generated `config.xml` means the plugin variables have to be added at the generator, not the file. |
| Version control | `git status`, and whether `platforms/` and `plugins/` are committed or gitignored. It decides whether a remove-and-re-add is a local step or a tracked change. |

Show a short **Found** block first: which platforms are added, Cordova or Capacitor, the
framework, the bundle id, what is already installed, which ad plugin was found, and where a
banner could sit. Then the questions.

## Questions

Ask only those that still apply. The last column is a rule for the agent, not part of what
the publisher sees: it says how to pick the Recommended option.

| # | Question | Options | Recommended is |
|---|---|---|---|
| 1 | Which platforms does this app ship? | A. Android and iOS; B. Android only; C. iOS only | What `platforms/` and CI show. A `browser` platform is neither — it takes no ads, and the ad code has to tolerate it. |
| 2 | Which CAS ID on iOS? | A. the numeric App Store id, digits only; B. leave `IOS_CAS_ID` at `demo`, the app is not on the App Store yet | **Not the bundle identifier.** B unless the publisher gives the App Store id. A real id only the publisher has: `none`. Android needs no answer — the plugin reads the bundle package name itself. |
| 3 | Is the app already registered in CAS? | A. No, or do not know; B. Yes | A. Until CAS registers the app nothing serves. The integration is still done, on test ads, and the publisher's CAS contact registers it. |
| 4 | New integration, or a migration from another mediation? | A. New; B. Migration from the plugin found in the research; C. Migration from another one | B when the research found another ad plugin, else A. CAS cannot run next to one, so B means removing it — plugin, native keys and all — in the same change. |
| 5 | Which ad formats, and on which screens? | A. the formats the existing ad code uses, on the same screens; B. banner, interstitial, rewarded; C. another combination | A when an existing ad plugin was found, else B. Follow up for the screen and the moment of each one, see the placement rules in `ad-formats.md`. Cordova has banner **and** MRec as separate formats, and no native format. |
| 6 | Where does the banner go, and what gives up the room? | A. bottom, with the page gaining bottom padding; B. top, with the header gaining space; C. no banner | A when a banner is wanted. The banner floats over the WebView, so **something in the CSS has to make room** or it covers content. This is a layout decision the publisher owns. |
| 7 | What is the audience? | A. General or mixed; B. Child-directed; C. VPN or proxy app | A. Most apps are general. If nothing in the project says otherwise and the publisher does not know, count it as A. |
| 8 | Only when 7 is B: Google Play Families, and is the iOS app in the Apple Kids Category? | A. Families on Android, not in the Kids Category; B. Both | A. On the Kids Category, stop and point at the Kids Category page: the Families solution is not intended for it, and the choice has to be agreed with the account manager. |
| 9 | Which solution? | A. `optimal`; B. `families`; C. `vpnCompliant`; D. a hand-picked adapter list | Follows from questions 7 and 8, and is set **twice**: `ANDROID_CAS_SOLUTIONS` and `IOS_CAS_SOLUTIONS`. D only when the publisher asks for it, and then the adapter names are spelled differently per platform. |
| 10 | Consent: the built-in CAS flow, or your own platform? | A. the built-in flow, with `showConsentFormIfRequired: true`; B. the consent platform found in the research | B when a consent platform was found, else A. On B the built-in flow stays off and the decision is applied before initialization. |
| 11 | Which AdMob App ID? | A. the one already in the native config; B. the one from the CAS dashboard | `none` unless one is already present and came from CAS. It must be the identifier CAS generated; the publisher's own AdMob App ID is not valid. Not asked when Google demand is not used. |
| 12 | Where will this be verified? | A. a connected device per platform; B. an emulator and a simulator; C. the publisher runs it themselves | A when a device is available, else B. **A browser build is not an option** — there is no SDK there. The build under test serves test ads either way; that is a rule, not an option. |
| 13 | The tree has uncommitted changes. | A. Go on; B. Stop, I commit first | A. Not asked at all when the tree is clean. |

## Follow-ups, only when relevant

| # | Question | Why |
|---|---|---|
| 14 | Is `config.xml` generated by a script or a hook? | Then the plugin variables belong at the generator, or the next build overwrites them. |
| 15 | Are `platforms/` and `plugins/` committed? | It decides whether the remove-and-re-add that applies a variable change is a local step or a tracked one, and whether CI repeats it. |
| 16 | Several build variants with different bundle ids? | Each shipped app is its own CAS ID, and on iOS that means a different `IOS_CAS_ID` per variant — which plugin variables do not express well. Plan it before promising it. |
| 17 | Does the app change orientation, or run on tablets? | Adaptive and inline banners resize and are re-clamped on rotation, so the room reserved in CSS has to hold in both orientations. |
| 18 | A minimum interval between interstitials? | `minInterval` is an option of `load()`, and an ad refused inside the window reports `NotPassedInterval` — correct behaviour, not a defect. |
| 19 | Impression level data needed for analytics? | The `casai_ad_impressions` document event carries `revenue`, `sourceName` and the cumulative totals. Changes what is wired at initialization. |
| 20 | A trial ad-free interval after install? | `casai.setTrialAdFreeInterval` exists for it, and it changes what a tester sees — an ad that does not show inside that window is correct behaviour, not a bug. |
| 21 | After a migration, do the reports have to stay comparable with the old ones? | Affects placement names and the impression level data wiring. |

## What the publisher does, not the agent

Registering the app in the CAS dashboard, supplying the App Store id for `IOS_CAS_ID`,
deciding whether the audience is child-directed, deciding what gives up layout room to the
banner, answering the Google Play data disclosure and the App Store privacy questions,
writing the `IOS_USER_TRACKING_USAGE` text, and getting access to closed beta networks. The
agent reminds and links, and does not decide.
