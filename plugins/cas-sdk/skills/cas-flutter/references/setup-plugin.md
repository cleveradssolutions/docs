# Flutter: the package and the two native sides

Canonical: [docs/Flutter/index.mdx](../../../../../docs/Flutter/index.mdx). Read it before
writing anything — the version numbers and the exact Gradle block live there, and they
change.

**The Dart package is the small part.** What decides whether ads serve is native: the CAS
Gradle plugin on Android and the `casconfig.rb` script on iOS. A `flutter pub add` alone
gives an app that builds, initializes and shows nothing.

## 1. The package

```shell
flutter pub add clever_ads_solutions
```

Then `import 'package:clever_ads_solutions/clever_ads_solutions.dart';` in Dart.

Pin nothing by hand out of this file: the current version is what `pub add` resolves, and
the page renders the variables from `docs.json`.

## 2. Android: the CAS Gradle plugin

In `android/app/build.gradle` (or `.kts`), the plugin goes **after** the Android
application plugin, and the `cas { }` block configures the ad networks:

```kotlin
plugins {
  id("com.android.application")                   // before CAS
  id("com.cleveradssolutions.gradle-plugin") version "<see the page>"
}

cas {
  includeOptimalAds = true   // or includeFamiliesAds, or includeVPNCompliantAds
}
```

Three things go wrong here, in this order of frequency:

- **The plugin version and the Flutter package version drift apart.** The Gradle plugin
  version tracks the CAS **Android SDK**, and it has to be moved every time the Flutter
  package is updated. A mismatch surfaces as a runtime failure, not a build error.
- **The `cas { }` block is empty.** It builds. It also means no ad networks, so nothing
  serves beyond test ads. Exactly one solution flag, chosen in the questions, or a
  hand-picked adapter list.
- **Network dependencies added by hand** beside the plugin. The plugin adds them; a
  hand-written AdMob or AppLovin dependency produces two copies and a duplicate-class
  failure.

## 3. iOS: the configuration script, then the SDK

The script writes the `Info.plist` entries, the `SKAdNetworkItems` and the AdMob App ID that
the networks need. It is not optional, and it is **re-run on every CAS update**, because
what it writes changes with the ad network set.

```shell
cd ios
ruby casconfig.rb <CAS ID>
```

The `<CAS ID>` here is the **iOS** one: the App Store id, digits only. Download the script
from the CAS-iOS release the page names.

The SDK itself arrives one of two ways, and the Flutter version decides which:

| Flutter | Path | What you do |
|---|---|---|
| `3.47` and newer | Swift Package Manager, on by default | Nothing for the SDK: `flutter build ios` resolves the package through the plugin and links it into `Runner`. Ad network adapters are separate products of the same package and are added to the `Runner` target. |
| Earlier, or SPM turned off | CocoaPods | Add the CAS source to the top of `ios/Podfile` beside the CocoaPods one, put the mediation pods in the `target 'Runner'` block, then `pod update` inside `ios/`. |

**Never both.** A project that keeps the Swift package and also adds CAS pods links two
copies of the SDK and fails with duplicate symbols. To take the adapters as pods, turn
Swift Package Manager off first (`flutter config --no-enable-swift-package-manager`), and
say so in the plan — Flutter has announced that turning it off will stop being allowed.

## 4. Ad networks, per platform

CAS has no inventory of its own. The solution chosen in the questions is switched on
separately on each side: the `cas { }` flag on Android, the package products or the pods on
iOS. Shipping one platform with a solution and the other without it is a common and silent
mistake — the app earns on Android and shows nothing on iOS.

The adapter tables are native, with no Flutter copy:
[docs/Android/Mediated-Networks.mdx](../../../../../docs/Android/Mediated-Networks.mdx) and
[docs/iOS/Mediated-Networks.mdx](../../../../../docs/iOS/Mediated-Networks.mdx), plus
`Adapters/README.md` in each native repository for the current versions.

During development no ad network is needed at all: test ads work without them, which is
what makes it tempting to stop here. Say plainly in the hand-over that the solution has to
be on before release.

## 5. What to check before moving on

- `flutter pub get` succeeds and the import resolves.
- `flutter build apk --debug` (or `flutter run`) succeeds, with the CAS Gradle plugin
  applied and one solution flag set.
- `flutter build ios --debug --no-codesign` succeeds after the script has been run, with
  one dependency path and not two.
- Both native projects are still buildable from their own tooling, because that is what CI
  and the stores use.
