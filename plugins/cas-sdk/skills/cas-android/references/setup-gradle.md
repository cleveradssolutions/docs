# Android: Gradle setup and ad networks

Canonical, in this repository: [docs/Android/index.mdx](../../../../../docs/Android/index.mdx)
Manual path without the plugin: [docs/Android/Manual-setup.mdx](../../../../../docs/Android/Manual-setup.mdx)
Published: `https://docs.page/cleveradssolutions/docs/Android`

Confirm the current SDK version and the minimum toolchain on the documentation page
before writing versions into the project.

## 1. Prerequisites, before adding anything

The app must meet the minimum `minSdk`, `compileSdk` and Kotlin version from the
documentation. Check them first and tell the publisher what has to move, because each one
fails late and with a message that does not name CAS:

| Below the minimum | What you get, and when |
|---|---|
| `compileSdk` | `checkDebugAarMetadata` fails with "Dependency ... requires libraries and applications that depend on it to compile against version N or later". Not at sync, at build. |
| Kotlin | `compileDebugKotlin` fails with "Module was compiled with an incompatible version of Kotlin. The binary version of its metadata is X, expected version is Y", repeated for every file of an ad network's library. The documented minimum has been behind the adapters before, so when this appears, raise Kotlin to the next minor and try again. |
| `minSdk` | The manifest merger fails, naming the adapter that needs more. |

Raise **only** what blocks the build, with the publisher's agreement. `compileSdk` is a
build-time setting and raising it is usually safe; `targetSdk` changes runtime behaviour
and is the publisher's product decision, so leave it alone unless they ask.

There also has to be an Android SDK to build against: `local.properties` with `sdk.dir`,
or `ANDROID_HOME`. A fresh clone often has neither.

## 2. Ad network repositories

Several networks publish outside Maven Central. Add their repositories **where the project
already keeps repositories**: the `dependencyResolutionManagement` block in
`settings.gradle(.kts)`, or the `allprojects` block in the root `build.gradle`, which is
what older projects and most published samples still use. Do not introduce
`dependencyResolutionManagement` into a project that does not have it.

Copy the block from the documentation page rather than from memory: the list changes with
the adapter set, and each entry carries a `content { includeGroup(...) }` filter that
keeps Gradle from searching every repository for every artifact.

## 3. The CAS Gradle plugin

Recommended path. In the app-level Gradle file, in whichever DSL the project uses.

Kotlin, `build.gradle.kts`:

```kotlin
plugins {
    id("com.android.application") // before the CAS plugin
    id("com.cleveradssolutions.gradle-plugin") version "<current version>"
}

cas {
    // configuration
}
```

Groovy, `build.gradle`:

```groovy
plugins {
    id 'com.android.application'
    id 'com.cleveradssolutions.gradle-plugin' version '<current version>'
}

cas {
    includeOptimalAds = true
}
```

Match the project. Converting a Groovy build to Kotlin is not part of an ad integration.

The plugin resolves the adapter dependencies from the `cas { }` block, so adapters are
never declared by hand.

In a library module the CAS ID cannot be derived from an `applicationId`, so it is set
explicitly:

```kotlin
cas {
    casId = "com.yourcompany.yourproject"
}
```

With product flavours in an application module the final `applicationId`, including
suffixes, is the CAS ID, and every one of them has to be registered separately in the
dashboard. In a library module each flavour is mapped with `addCasIdVariant`.

## 4. Choose the ad networks

Exactly one of these, decided in the interview:

```kotlin
cas { includeOptimalAds = true }        // general or mixed audience
cas { includeFamiliesAds = true }       // child-directed, Families policy
cas { includeVPNCompliantAds = true }   // VPN or proxy apps
```

Or a hand-picked set, when the publisher knows what they want:

```kotlin
cas {
    adapters {
        googleAds = true
        unityAds = true
        // ...
    }
}
```

The network list behind each solution is on the documentation page and changes between
releases, so quote it from there if the publisher asks what is included.

During development with test ads, no network is required at all. That is the right way to
check the plumbing before the account is fully set up.

## 5. Advertising identifier

```kotlin
cas { useAdvertisingId = true }
```

Off for a child-directed app, and off for builds that target devices without Google Play
services, such as Amazon or Huawei. This is the publisher's call, not the agent's.

## 6. Release build

Leave `isMinifyEnabled = true` and the project's ProGuard files alone. The SDK and the
adapters ship their own rules inside the AARs; nothing has to be added, and nothing should
be removed to make a release build pass.

## 7. Check

`./gradlew :app:assembleDebug` succeeds and the dependency tree contains the CAS SDK and
the expected adapters. A missing repository shows up here as an unresolved adapter
artifact, not later at runtime.
