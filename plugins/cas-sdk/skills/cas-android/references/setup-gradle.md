# Android: Gradle setup and ad networks

Canonical: `https://docs.page/cleveradssolutions/docs/Android`
Manual path without the plugin: `.../Android/Manual-setup`

Confirm the current SDK version and the minimum toolchain on the documentation page
before writing versions into the project.

## 1. Prerequisites

The app must meet the minimum `minSdk`, `compileSdk` and Kotlin version from the
documentation. If the project is below any of them, say so and let the publisher decide
whether to raise it. Never raise it silently.

## 2. Ad network repositories

Several networks publish outside Maven Central. Add their repositories to the
`dependencyResolutionManagement` block in `settings.gradle.kts`, or to `allprojects` in
the root `build.gradle` on Gradle below 7.

Copy the block from the documentation page rather than from memory: the list changes with
the adapter set, and each entry carries a `content { includeGroup(...) }` filter that
keeps Gradle from searching every repository for every artifact.

## 3. The CAS Gradle plugin

Recommended path. In the app-level Gradle file:

```kotlin
plugins {
    id("com.android.application") // before the CAS plugin
    id("com.cleveradssolutions.gradle-plugin") version "<current version>"
}

cas {
    // configuration
}
```

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
