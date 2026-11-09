# Kotlin Multiplatform: the library and the two platform setups

Canonical:
[docs/KMP/index.mdx](../../../../../docs/KMP/index.mdx). Read it before writing anything —
the version numbers, the exact Gradle block, the Swift Package product names and the full
Maven repository list live there, and they change.

The library in `commonMain` is one line. Everything that decides whether ads serve is in
the two platform setups below, and they are **two different mechanisms for the same
decision**, done separately and kept in step by hand.

## 1. The library

```kotlin
// <project>/shared/build.gradle.kts
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.cleveradssolutions:cas-kmp:<see the page>")
        }
    }
}
```

Then `import com.cleveradssolutions.plugin.kmp.*` in common code, and
`com.cleveradssolutions.plugin.kmp.composable.*` for the Banner and Native composables.

**The Android artifact declares the CAS Android SDK as `compileOnly`.** So this dependency
compiles against the API and brings no SDK with it: the SDK and every adapter arrive from
the Android setup below. A project that stops after this line compiles, and crashes or
finds no ad source at runtime.

### Three version numbers, one decision

| Number | Tracks | Where it is written |
|---|---|---|
| `cas-kmp` | the Kotlin Multiplatform library | `commonMain.dependencies` |
| CAS Gradle plugin | the CAS **Android** SDK | the `plugins` block of the module with the Android target |
| Swift Package version | the CAS **iOS** SDK | the Xcode package dependency, or `Package.swift` |

They are three different numbers and they move together. The page gives the matching set;
take all three from it at the same time, and move all three on an upgrade. A mismatch
surfaces at runtime, not at build time, which is why it is worth writing the three values
into the hand-over.

## 2a. Android: the CAS Gradle plugin

Applied to the module that holds the Android target, inside `plugins`, **after** the Android
plugin itself:

```kotlin
// <project>/shared/build.gradle.kts
plugins {
    kotlin("multiplatform")
    id("com.android.application") // before CAS
    id("com.cleveradssolutions.gradle-plugin") version "<see the page>"
}

cas {
    includeOptimalAds = true   // or includeFamiliesAds, or includeVPNCompliantAds
    useAdvertisingId = true
}
```

The plugin adds the CAS Android SDK and the adapters of the chosen solution. An empty
`cas { }` block builds and serves nothing beyond test ads — exactly one solution flag,
chosen in the questions, or a hand-picked list under `adapters { }`.

`useAdvertisingId` must be **off** when the app targets only children, or when it is built
for devices without Google Play Services.

### Add the Maven repositories — this is the step that is missed

Several ad networks do not publish to Maven Central, and **the CAS Gradle plugin does not
add their repositories for you.** Copy the repositories block from the Android setup page
into `dependencyResolutionManagement` of `settings.gradle.kts`.

Without them the Android build fails while resolving dependencies:

```
Could not find com.mbridge.msdk.oversea:mbridge_android_sdk
```

and the same for `com.pangle.global`, `com.ysocorp` and the other self-hosting networks.
Copy the block from the page rather than from memory, because the list grows.

This is the one place where Kotlin Multiplatform differs from Flutter, where the tooling
adds the repositories itself. Expect the build to fail here on a first integration if the
step was skipped, and recognise the message rather than treating it as a version problem.

## 2b. iOS: the CAS Swift Package

In Xcode, **File ▸ Add Package Dependencies…** with the repository URL:

```
https://github.com/cleveradssolutions/CAS-iOS
```

Dependency Rule **Up to Next Minor Version**, and the version from the page. Then pick the
library products — the ad network choice happens here, as an Xcode step rather than a code
change:

| Choice | Product to add |
|---|---|
| Optimal solution | `AdsOptimalSolutions` |
| Families solution | `AdsFamiliesSolutions` |
| VPN Compliant solution | `AdsVPNCompliantSolutions` |
| Hand-picked | `CleverAdsSolutionsSPM`, the SDK itself, plus one `CASMediation*` per network |

A solution product brings the SDK with it. The `CASMediation*` name is the network's row in
the adapters table of the CAS iOS repository with the prefix added — `CASMediationGoogleAds`
for `GoogleAds` — and `CASExchange` is the one exception, whose product is
`CASMediationExchange`. Xcode lists the exact products when the package is added, so there
is never a reason to guess. The solution products arrived in CAS `4.8.3`; on an earlier
version, add the `CASMediation*` products one by one.

**Nothing is imported or called in code for an adapter to work.** Each product carries the
adapter binary and the SDK discovers it by class name at runtime. Which is also why:

> **`-ObjC` must be in Other Linker Flags.** Without it the linker strips the adapters'
> Objective-C classes. The project builds, the SDK initializes, and every network stays
> unavailable with no error that says so. `casconfig.rb` sets the flag; a project that
> configures Xcode by hand has to set it itself.

The **SPM** column of the adapters table says whether a network ships through Swift Package
Manager. A few do not, because of limitations of the ad frameworks themselves; a project
that needs one of those uses the CocoaPods integration instead, which is the cas-ios
skill's territory.

## 2c. iOS: deliver the Kotlin framework to Xcode

The shared module compiles to an Apple framework that the Xcode project links. In the
shared module:

```kotlin
kotlin {
    listOf(iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
}
```

`iosArm64` and `iosSimulatorArm64` are the supported targets. **There is no x86_64 simulator
target**, so an Intel Mac cannot run the simulator build at all — a hardware fact worth
stating before the verification step rather than during it.

Then connect it in Xcode: a **New Run Script Phase** on the application target, moved
**before** Compile Sources, running
`./gradlew :shared:embedAndSignAppleFrameworkForXcode` from the project root; the framework
output directory added to **Framework Search Paths**; and, after one build,
`Shared.framework` under **Frameworks, Libraries, and Embedded Content**. The page has the
exact script and path, and the Gradle task name follows the real module path rather than
`:shared`.

## 2d. iOS: run casconfig.rb

The Xcode project needs `SKAdNetworkItems` and the AdAttributionKit identifiers of every
mediated network, `NSUserTrackingUsageDescription`, `GADApplicationIdentifier`, App
Transport Security and the linker flags. CAS maintains all of it in a Ruby script so the
values stay correct as the networks change:

```shell
cd iosApp
ruby casconfig.rb <CAS ID>
```

Download it from the CAS-iOS release named on the page, and place it next to the
`.xcodeproj`. `--project=` points it elsewhere, `--help` lists the rest. **Run it again
before every app update**, because the network identifiers change with the adapters. The
alternative is the manual configuration page, and it is long.

## 3. Ad networks are chosen twice, and the two lists differ

The `cas { }` flag on Android and the Swift Package product on iOS are the same decision
made in two places. Shipping one platform with a solution and the other without it is a
common and silent mistake — the app earns on Android and shows nothing on iOS.

The two lists are also **expected to differ**: not every network ships an adapter for both
platforms, the store policies for child-directed apps do not approve the same networks, and
individual iOS adapters raise the minimum deployment target. Pick per platform rather than
mirroring one list onto the other.

The adapter tables are native, with no Kotlin Multiplatform copy:
[docs/Android/Mediated-Networks.mdx](../../../../../docs/Android/Mediated-Networks.mdx) and
[docs/iOS/Mediated-Networks.mdx](../../../../../docs/iOS/Mediated-Networks.mdx), plus
`Adapters/README.md` in each native repository for the current versions and, on iOS, the
SPM column.

During development no ad network is needed at all: test ads work without them, which is
what makes it tempting to stop here. Say plainly in the hand-over that the solution has to
be on, on both platforms, before release.

## 4. What to check before moving on

- The library resolves and `import com.cleveradssolutions.plugin.kmp.*` compiles in
  `commonMain`.
- A debug Android build succeeds with the plugin applied, one solution flag set, and the
  Maven repositories present in `settings.gradle.kts`.
- The Xcode project builds: the Swift Package resolved, the products on the target, the run
  script phase before Compile Sources, `Shared.framework` embedded, `-ObjC` present.
- `casconfig.rb` has been run with the iOS CAS ID, and `Info.plist` shows the
  `SKAdNetworkItems` it wrote.
- The app **runs** on both platforms. A shared module that compiles for `iosArm64` proves
  nothing about the Xcode link step, and that step is where this platform fails.
