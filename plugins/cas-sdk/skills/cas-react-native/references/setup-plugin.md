# React Native: the package and the two native sides

Canonical:
[docs/reactnative/index.mdx](../../../../../docs/reactnative/index.mdx). Read it before
writing anything — the version numbers, the exact Gradle block and the full Maven repository
list live there, and they change.

**Everything native depends on one answer: Expo or bare.** The two paths touch different
files, and doing the bare steps in an Expo project means editing native projects that the
next `npx expo prebuild` overwrites.

## 1. The package

```shell
npm install react-native-cas          # bare React Native
npx expo install react-native-cas     # Expo
```

Then `import * as CASModule from 'react-native-cas';`, or named imports as each format
needs.

## 2a. Expo: the config plugin

Add the plugin to `app.json`, `app.config.js` or `app.config.ts`, with its options:

```js
{
  "expo": {
    "plugins": [
      ["react-native-cas", { "iosAppId": "<iOS App Store id>" }]
    ]
  }
}
```

`npx expo prebuild` then configures both native projects: the CAS Gradle plugin, the CAS
Maven repositories, and the iOS side including the CAS ID. That is the whole native setup —
there is nothing to edit in `android/` or `ios/` by hand, and anything edited there is lost
on the next prebuild.

Read the page for the current option names before writing them; `iosAppId` is the one that
carries the iOS CAS ID.

## 2b. Bare React Native: Android

In `android/app/build.gradle`, the plugin goes **after** the Android application plugin, and
the `cas { }` block configures the ad networks:

```gradle
plugins {
  id("com.android.application")                   // before CAS
  id("com.cleveradssolutions.gradle-plugin") version "<see the page>"
}

cas {
  includeOptimalAds = true   // or includeFamiliesAds, or includeVPNCompliantAds
}
```

**Then the Maven repositories**, and this is the step that is unique to bare React Native:
several ad networks publish to their own repositories — Mintegral, Pangle, Chartboost,
Ogury, Smaato, Verve, SuperAwesome, PubMatic, DisplayIO and more — and Gradle cannot resolve
them without being told where to look. The full block is on the setup page; copy it from
there rather than from memory, because the list grows. Expo adds these for you; a bare
project that skips them fails to resolve artifacts at build time.

Three things go wrong here, in this order of frequency:

- **The plugin version and the npm package version drift apart.** The Gradle plugin version
  tracks the CAS **Android SDK**, and it has to be moved every time `react-native-cas` is
  updated. A mismatch surfaces at runtime, not at build time.
- **The `cas { }` block is empty.** It builds, and nothing serves beyond test ads. Exactly
  one solution flag, chosen in the questions, or a hand-picked adapter list.
- **Network dependencies added by hand** beside the plugin, which adds its own. Two copies
  means a duplicate-class failure.

## 2c. Bare React Native: iOS

React Native **adds an Xcode build phase itself** that runs the CAS configuration script, so
unlike Flutter there is no Ruby command to run by hand. What you do supply is the CAS ID, as
an `Info.plist` key:

| Key | Type | Value |
|---|---|---|
| `CASAIAppIdentifier` | String | the iOS CAS ID, the App Store id |

The page writes this path as `ios/Runner/Info.plist`. **`Runner` is the Flutter target
name**; in a React Native project the target is named after the project, so read the actual
target name out of the Xcode project rather than copying the path. The same applies to the
`target '...' do` block in the `Podfile` below.

Then the pods. Add the CAS source at the top of `ios/Podfile`, beside the CocoaPods one:

```ruby
source 'https://github.com/CocoaPods/Specs.git'
source 'https://github.com/cleveradssolutions/CAS-Specs.git'
```

Put the mediation pods inside the app target's block, then `pod update` (or
`pod install`) inside `ios/`. The pods version tracks the CAS **iOS SDK**, which is again a
different number from the npm package.

## 3. Ad networks, per platform

CAS has no inventory of its own. The solution chosen in the questions is switched on
separately on each side: the `cas { }` flag on Android, the pods on iOS, or the config
plugin options under Expo. Shipping one platform with a solution and the other without it is
a common and silent mistake — the app earns on Android and shows nothing on iOS.

The adapter tables are native, with no React Native copy:
[docs/android/mediated-networks.mdx](../../../../../docs/android/mediated-networks.mdx) and
[docs/ios/mediated-networks.mdx](../../../../../docs/ios/mediated-networks.mdx), plus
`Adapters/README.md` in each native repository for the current versions.

During development no ad network is needed at all: test ads work without them, which is what
makes it tempting to stop here. Say plainly in the hand-over that the solution has to be on
before release.

## 4. What to check before moving on

- The package installs and the import resolves.
- Expo: `npx expo prebuild` completes, and the generated `android/app/build.gradle` really
  contains the CAS plugin. Read it — that is the proof the config plugin ran.
- Bare: a debug Android build succeeds with the plugin applied, one solution flag set, and
  the Maven repositories present.
- iOS builds after `pod install`, with the build phase present and `CASAIAppIdentifier` set.
- Both native projects are still buildable from their own tooling, because that is what CI
  and the stores use.
