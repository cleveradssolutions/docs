# Unreal: research the project, then ask

The shape of a question is in [question-format.md](question-format.md), the same in every
CAS guide. This file is the Unreal questions only; every platform has its own, because
the answers that matter are not the same.

## Research, read-only

| What | Where to look |
|---|---|
| Not an Unreal project at all | No `.uproject`. A Unity project (`ProjectSettings/ProjectVersion.txt`), `pubspec.yaml`, `react-native` in `package.json`, a Kotlin Multiplatform `shared` module, a Cordova `config.xml`, or a bare Android or Xcode project each belong to their own CAS skill or guide. Stop and say which one. |
| **Engine version** | `EngineAssociation` in the `.uproject`. UE4 needs engine-level prerequisites that UE5 does not, and those edits affect every project on the machine. Settle it before planning anything. |
| **C++ or Blueprint-only** | A `Source/` folder with `<Project>.Build.cs` and `<Project>.Target.cs`, or none. An agent cannot author Blueprint graphs — `.uasset` is binary — so on a Blueprint-only project the deliverable is settings plus node-by-node instructions, and that has to be said before work starts, not after. |
| Which platforms ship | The platforms configured under `Edit > Project Settings > Platforms`, and what CI packages. Everything here is set per platform, twice. |
| Package name and App Store id | `Project Settings > Platforms > Android > Package Name`, and the iOS bundle identifier. These give the two CAS App IDs, and on iOS the CAS App ID is the **App Store id**, not the bundle identifier. |
| Plugin already present | `Plugins/CAS-Unreal/CleverAdsSolutions.uplugin`, `CleverAdsSolutions` in `PublicDependencyModuleNames`, `UCASMobileAds` anywhere in `Source/`, a `[CASPluginBuildConfig]` section in `Config/DefaultEngine.ini`. Then: which solution is checked per platform, whether `TestAdsMode` is still on, and which autoload formats are selected. |
| Engine's built-in AdMob | `Project Settings > Platform > Android > Google Play Services > Include AdMob support for ads`. It has to be **off**, and any use of the engine's own AdMob Blueprint nodes has to be replaced. |
| Other ad plugins | Other plugins in `Plugins/` and in the `.uproject` plugin list — an AppLovin, IronSource, Unity Ads or Google Mobile Ads plugin. Two mediation SDKs cannot run side by side. |
| Plugins that share iOS frameworks | Firebase, analytics and attribution plugins. They are the reason for duplicate-symbol link failures, and they decide what goes into `+ExcludeFrameworks`. Note them now; it saves a failed package later. |
| Not an ad SDK, do not touch | Attribution and analytics plugins, in-app purchases, the consent platform. If unsure whether something is an ad SDK, ask. |
| Leftovers of an old integration | Ad network keys and `GADApplicationIdentifier` in the Android manifest additions or the iOS plist additions, hand-written UPL files, `SKAdNetworkItems` added by hand. The CAS plugin writes its own; a migration has to remove these. |
| Where ads would go | The game flow: level end, menu transitions, a death or retry screen, a reward button in the shop. Which of them are Blueprint-driven and which are C++. Also whether the game pauses — an interstitial over a running game loop is a bug of its own. |
| Existing ad formats | Calls of an existing ad plugin, and whether a banner has a fixed position on screen. They are the formats and placements to reproduce. |
| Consent platform | A UMP or CMP plugin, or none. |
| Build machine | Windows or macOS, and whether CocoaPods is installed (`pod --version`) if iOS ships. On Windows, whether the project folder and the engine installation are on the **same drive** — they have to be. |
| Version control | `git status`. An uncommitted tree before an integration is worth naming, and so is a `Plugins/` folder that is gitignored. |

Show a short **Found** block first: engine version, C++ or Blueprint, which platforms ship,
package name and App Store id, what is already installed, the built-in AdMob state, which
other plugins bring iOS frameworks, and the build machine. Then the questions.

## Questions

Ask only those that still apply. The last column is a rule for the agent, not part of what
the publisher sees: it says how to pick the Recommended option.

| # | Question | Options | Recommended is |
|---|---|---|---|
| 1 | Which engine version, and may its installation be modified? | A. the version the research found, engine edits allowed; B. the same version, engine must not be touched | A on UE5, where no engine edit is needed anyway. On UE4 the prerequisites edit `Engine/Build/Android/...`, which is shared by every project on the machine — ask explicitly, and on B say which API levels then cannot be targeted. |
| 2 | C++ or Blueprint? | A. the one the research found; B. add a C++ module to this Blueprint project | A. This is a fact about the project. B converts a Blueprint-only project into a code project, which changes how it builds and who can open it — only on the publisher's explicit request. |
| 3 | Which platforms does this project ship? | A. Android and iOS; B. Android only; C. iOS only | What the packaging settings and CI show. Everything else follows: a single-platform project needs one CAS App ID and one set of prerequisites. |
| 4 | Which CAS App ID on Android? | A. the package name found in the research; B. `demo`, the game is not on Google Play yet | A when a package name is set and the app is registered, else B. |
| 5 | Which CAS App ID on iOS? | A. the numeric App Store id, digits only; B. `demo`, the game is not on the App Store yet | **Not the bundle identifier.** B unless the publisher gives the App Store id. A real id only the publisher has: `none`. |
| 6 | Is the app already registered in CAS? | A. No, or do not know; B. Yes | A. Until CAS registers the app nothing serves. The integration is still done, on test ads, and the publisher's CAS contact registers it. |
| 7 | New integration, or a migration? | A. New; B. Migration from the ad plugin found in the research; C. Migration from the engine's built-in AdMob | B or C when the research found either, else A. C is common here and means replacing the engine's own `Show Interstitial Ad` and `Show Ad Banner` nodes, which have the same names as the CAS ones. |
| 8 | Which ad formats, and at which moments in the game? | A. the formats the existing ad code uses, at the same moments; B. banner, interstitial, rewarded; C. another combination | A when existing ad code was found, else B. Follow up for the exact moment of each one, see the placement rules in `ad-formats.md`. Unreal has banner **and** MREC as separate formats, and no native format at all. |
| 9 | Which formats should autoload? | A. only the ones being added; B. all of them | A. Autoload is a per-format checkbox per platform, and selecting a format that is never shown costs performance for nothing. |
| 10 | What is the audience? | A. Mixed; B. Children; C. NotChildren | A unless the project says otherwise. These are the three values of the settings window, and `Children` means the Families solution and the Google Play Families Policy. |
| 11 | Only when 10 is Children: is the iOS app in the Apple Kids Category? | A. No, Google Play Families only; B. Yes | A. On the Kids Category, stop and point at the Kids Category page: the Families solution is **not** intended for it, and the choice has to be agreed with the account manager. |
| 12 | Which ads solution? | A. Optimal; B. Families; C. a hand-picked adapter list from the Advanced section | Follows from questions 10 and 11, and is checked **twice**, once per platform panel. C only when the publisher asks for it; beta sources need the account manager. |
| 13 | Consent: the built-in CAS flow, or your own? | A. the built-in flow, `Auto Consent Flow` checked; B. a CMP the project already has, with the CAS flow off and the status set before initialization | B when a consent platform was found, else A. On A, ask for the Privacy Policy URL; without one the dialog links the CAS policy instead of the game's. |
| 14 | Where will this be verified? | A. a packaged build on a device per platform; B. the publisher packages and runs it themselves | A when a device and a working packaging pipeline exist, else B. **Playing in the editor is not an option**: there is no SDK there. The build under test has test ads on; that is a rule, not an option. |
| 15 | The tree has uncommitted changes. | A. Go on; B. Stop, I commit first | A. Not asked at all when the tree is clean. |

## Follow-ups, only when relevant

| # | Question | Why |
|---|---|---|
| 16 | Do other plugins bring iOS frameworks that CAS also packages? | Duplicate symbols at link time. The answer becomes `+ExcludeFrameworks` entries in `Config/DefaultEngine.ini`, and the FAQ names Firebase's `FBLPromises` as the usual case. |
| 17 | Is the iOS build failing with "ARM64 branch out of range"? | Pangle's size. Either `Packaging For Distribution`, or exclude adapters during development — which is also the faster loop. |
| 18 | Should the game pause while a full screen ad shows? | Almost always yes: audio, the game loop and any intense computation. It is engine work, not SDK work, and it belongs in the plan rather than as an afterthought. |
| 19 | A minimum interval between interstitials? | `SetInterstitialAdMinimumInterval` exists for it, and the documentation recommends setting it **before** initialization so it can be overridden remotely from the dashboard. |
| 20 | A fixed banner position? | `SetBannerAdPosition` takes an `ECASPosition`, defaults to bottom centre, and changing it does not reload the ad. Worth agreeing before the HUD is laid out. |
| 21 | Impression level data needed for analytics? | `OnAdsImpression` with `FCASImpressionInfo` carries the revenue and the network. Changes what is wired at initialization. |
| 22 | Is the build machine Windows? | The project folder and the engine installation must be on the same drive, or paths fail to resolve during packaging. |

## What the publisher does, not the agent

Everything in the `CAS.AI Config` windows, because it is an editor UI: the CAS App IDs, the
solution, the audience, test mode, the autoload formats, the consent flow, the Privacy
Policy URL and the ATT usage description. Plus registering the app in the CAS dashboard,
deciding whether the audience is child-directed, answering the Google Play data disclosure
and the App Store privacy questions, and getting access to closed beta networks. The agent
tells them exactly which field and links the page — screenshots included — and does not
decide.
