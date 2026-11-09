# Unreal: the plugin, the module, and the two platforms

Canonical, in this repository:
[docs/unreal/index.mdx](../../../../../docs/unreal/index.mdx),
[Configuring-SDK.mdx](../../../../../docs/unreal/configuring-sdk.mdx),
[Include-Android.mdx](../../../../../docs/unreal/include-android.mdx) and
[Include-iOS.mdx](../../../../../docs/unreal/include-ios.mdx). Read them before writing
anything — the engine versions, the Gradle versions and the exact menu paths live there,
and they change.

Four parts, and only the first two are code.

## 1. The plugin and the module

There is no package manager. The plugin is **source dropped into the project**:

1. Create a `Plugins` folder at the project root, beside `Source`.
2. Unpack the latest source archive from the CAS-Unreal releases into it, or clone the
   repository there.

```
UnrealProjectRoot
├── Content
├── Plugins
│   └── CAS-Unreal
│       └── CleverAdsSolutions.uplugin
├── Source
```

The releases also carry **precompiled packages with binaries**, built for specific engine
versions only. They are faster, and they are unusable on any other engine version — so
read the release notes before picking one, and prefer the source archive when the engine
version is not exactly listed.

Then the module dependency, in `Source/<Project>/<Project>.Build.cs`:

```csharp
PublicDependencyModuleNames.Add("CleverAdsSolutions");
```

and `#include "CASMobileAds.h"` where ads are used. On a Blueprint-only project there is no
`.Build.cs` and no include: the nodes are available once the plugin is enabled, in the
**CAS Mobile Ads** category.

On a **Windows** build machine the project folder and the engine installation must be on
the same drive. Unreal resolves paths during packaging in a way that fails across drives,
with errors that look like anything but the real cause.

## 2. The settings window, which is most of the integration

`Edit > Project Settings > Plugins > CAS.AI Config - [Platform]`, and the `[Platform]` part
is the point: **every field is set separately for Android and iOS.** The agent cannot click
these. Name each one explicitly in the plan and in the hand-over, per platform:

| Field | What to say about it |
|---|---|
| **CAS App ID** | The Android package name on Android, the **App Store id** on iOS. Two different values. |
| **Include Ads Solution** | Optimal, or Families for a children's audience. Nothing serves beyond test ads until one is checked, **on each platform**. |
| **Advanced** mediation list | The hand-picked alternative to a solution. Beta sources need the account manager. CrossPromotion is installed and removed here too. |
| **Autoload ad formats** | Per format: Banner, MRec, Interstitial, Rewarded, App Open. Check only the formats the game actually shows — an autoloading format that is never displayed costs performance for nothing. |
| **TestAdsMode** | On for development, off for release. This is a checkbox, not a build flag, so it is a release checklist item. With it on, any non-empty CAS App ID works, `demo` included. |
| **Target app audience** | Mixed, Children or NotChildren. `Children` brings the Families restrictions and the Google Play Families Program. |
| **Auto Consent Flow** | On by default. Off only when the project has its own CMP. |
| **Use Advertising Id** | Off for a children-only app, and for builds for devices without Google Play Services. |
| **User Tracking Usage Description** | iOS only, and only meaningful with the advertising id in use. The publisher writes the text; the agent offers the documented examples and does not word it for them. |
| **Privacy Policy URL** | Shown in the consent dialog. Without it the dialog links the CAS policy rather than the game's. |
| **User Debug Geography** | Development only, and it works **only** with test ads mode and an active test device. |

A screenshot of this window is on the configuration page — linking it is usually faster
than describing a field.

## 3a. Android prerequisites

**Turn off the engine's own AdMob support** first:
`Project Settings > Platform > Android > Google Play Services > Include AdMob support for
ads`. The CAS plugin configures AdMob itself as part of mediation, and both at once is a
duplicate integration.

> **The engine ships Blueprint nodes with the same names as the CAS ones** — `Show
> Interstitial Ad`, `Show Ad Banner`. Only the nodes from the **CAS Mobile Ads** category
> do anything here. A graph that calls the engine's node looks correct, compiles, and shows
> no ad. On a migration from built-in AdMob, every one of those nodes has to be replaced,
> and this is the single most likely thing to be missed.

Then the Gradle requirements. Google's AdMob SDK needs **Android Gradle plugin 4.2.0+ and
Gradle 6.7.1+**, and on older engines those are below the minimum:

| What | Where | Note |
|---|---|---|
| Gradle wrapper | `Engine/Build/Android/Java/gradle/gradle/wrapper/gradle-wrapper.properties`, `distributionUrl` | Nothing to do if the engine already uses a newer Gradle. |
| Android Gradle plugin | `Engine/Build/Android/Java/gradle/build.gradle`, the `com.android.tools.build:gradle` classpath | At least 4.2.2 in the documented example. |

> **These paths are inside the engine installation, not the project.** Every project on the
> machine shares them, and an engine update overwrites them. Get the publisher's explicit
> agreement before editing, note it in the hand-over, and never do it silently. On a
> source-built engine it is a local change that has to survive a rebuild.

Two more, on **Unreal Engine 4 only**:

- **API 34:** `Engine/Build/Android/Java/src/com/epicgames/ue4/WebViewControl.java` uses the
  removed AppCache API. The three `setAppCache*` calls have to be commented out. Engine
  installation again.
- **API 32:** add `android:exported="true"` under
  `Project Settings > Platform > Android > Advanced APK Packaging > Extra Tags for UE4
  GameActivity <activity> node`. This one is a project setting, so it is safe.

## 3b. iOS prerequisites

**CocoaPods is required**, because that is how the plugin resolves the frameworks of the
selected networks:

```sh
pod --version           # check
brew install cocoapods  # or: sudo gem install cocoapods
```

The plugin then **integrates the third-party frameworks and resource bundles itself**,
before packaging, for whatever is selected in the CAS Config window — and it also **writes
the SKAdNetwork identifiers into `Info.plist`** for you. There is no pod file to maintain
and no plist editing to do, which is the one place where Unreal is easier than the other
plugins.

Three things the project still has to do:

- **Disable Bitcode**, under `Edit > Project Settings > Platforms > iOS`. Apple deprecated
  it in Xcode 14, and Unreal 5.2 and later no longer use it at all.
- **Delete `Project/Binaries/IOS`** whenever the selected adapter set changes. It caches the
  previous build's frameworks, and without the delete the next package links the old set —
  a failure that looks like the settings did not apply.
- **Exclude frameworks that another plugin already packages**, in
  `Config/DefaultEngine.ini`:

```ini
[CASPluginBuildConfig]
+ExcludeFrameworks="FBLPromises"
```

  One entry per framework name, without the extension. Firebase's `FBLPromises` is the
  usual case. The package will not build if the excluded code is genuinely referenced, so
  exclude the *duplicate*, not the only copy.

On **Unreal Engine 4** with Xcode 15, add the documented Clang flags to the constructors of
`Source/<Project>.Target.cs` and `Source/<Project>Editor.Target.cs` — `bOverrideBuildEnvironment`
plus the `-Wno-...` arguments. These are project files, so they are safe to edit, and the
page has the exact block.

## 4. Ad networks are chosen twice, and the two lists differ

The solution is a checkbox in the Android panel and a checkbox in the iOS panel. Shipping
one platform with a solution and the other without it is a common and silent mistake — the
game earns on Android and shows nothing on iOS.

The two lists are also expected to differ: not every network ships an adapter for both
platforms, and the store policies for child-directed apps do not approve the same networks.
Specifically, the Families solution is compatible with the Google Play Families Policy on
Android and is **not** intended for the Apple Kids Category on iOS.

During development no ad network is needed at all: test ads work without them — and
excluding the adapters makes the iOS package build far faster, which the FAQ recommends for
the development loop. Say plainly in the hand-over that the solution has to be on, on both
platforms, before release.

The adapter tables are native, with no Unreal copy:
[docs/android/mediated-networks.mdx](../../../../../docs/android/mediated-networks.mdx) and
[docs/ios/mediated-networks.mdx](../../../../../docs/ios/mediated-networks.mdx). Some
networks need extra steps — Meta Audience Network has its own page,
[Additional-Meta-AudienceNetwork-steps.mdx](../../../../../docs/unreal/additional-meta-audiencenetwork-steps.mdx).

## 5. What to check before moving on

- The plugin is in `Plugins/`, enabled, and the editor shows `CAS.AI Config - Android` and
  `CAS.AI Config - iOS`.
- `CleverAdsSolutions` is in `PublicDependencyModuleNames` and the project compiles.
- The engine's built-in AdMob support is off, and no engine AdMob node remains in any graph.
- Both config panels have a CAS App ID and one solution, or the publisher has been told
  exactly which fields are still empty.
- The project **packages** for each platform being shipped. Not an editor build: the Gradle
  step and the pod integration only run when packaging, and they are where this fails.
