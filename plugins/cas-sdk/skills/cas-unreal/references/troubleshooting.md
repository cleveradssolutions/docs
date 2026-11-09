# Unreal: reading failures

Canonical, in this repository:
[docs/Unreal/FAQ.mdx](../../../../../docs/Unreal/FAQ.mdx) — read it before guessing,
because most Unreal failures are **build** failures with a known, documented answer.
Published: `https://docs.page/cleveradssolutions/docs/Unreal/FAQ`

Two different kinds of failure live here: the package does not build, or it builds and no
ad appears. Separate them before investigating.

## Build failures, iOS

| Symptom | Cause and fix |
|---|---|
| `ARM64 branch out of range (-135091044 max is +/-128MB)` | Pangle's size. Either enable **Packaging For Distribution**, which turns on the needed optimizations, or exclude adapters during development — which also makes the build loop much faster. |
| `Undefined symbols: __swift_FORCE_LOAD_$_swiftCompatibility56` | Xcode is too old and lacks the Swift compatibility components. Update it to the documented minimum. |
| `duplicate symbol ... in PromisesObjC.framework and FBLPromises.framework` | Another plugin packages the same framework. Add `+ExcludeFrameworks="FBLPromises"` under `[CASPluginBuildConfig]` in `Config/DefaultEngine.ini`, with the name only, no extension. Firebase is the usual source. |
| The adapter selection changed and the package still links the old set | `Project/Binaries/IOS` caches the previous build's frameworks. Delete it. |
| Clang 15 errors across engine code, on UE4 | The documented `-Wno-...` flags belong in the constructors of `Source/<Project>.Target.cs` and `Source/<Project>Editor.Target.cs`, with `bOverrideBuildEnvironment = true`. |
| Frameworks are not integrated at all | CocoaPods is missing. `pod --version`, then `brew install cocoapods`. |

## Build failures, Android

| Symptom | Cause and fix |
|---|---|
| `AAPT: error: unexpected element <property> found in <manifest><application>` | AdMob needs Android Gradle plugin 4.2.0+ and Gradle 6.7.1+. Both live in the **engine installation**, and the Android include page has the two paths. |
| `AAPT2 ... Unexpected error during link, attempting to stop daemon` | Target SDK 34 with Build Tools below 34.0.0, caused by the DTExchange library. Override `android.aapt2FromMavenOverride` to an AAPT2 from Build Tools 34.0.0, through a UPL file's `gradleProperties`. The FAQ has the block. |
| `setAppCache*` compile errors on UE4 with API 34 | `WebViewControl.java` in the engine uses the removed AppCache API; the three calls have to be commented out. Engine installation again. |
| Manifest element rejected on UE4 with API 32 | `android:exported="true"` goes into **Extra Tags for UE4 GameActivity `<activity>` node**, a project setting. |
| Paths fail to resolve while packaging on Windows | The project folder and the engine installation are on different drives. They have to be on the same one. |

Anything that edits a path under `Engine/` affects **every project on the machine** and is
lost on an engine update. Say so, get agreement, and write it into the hand-over.

## It builds, and no ad appears

**The game crashes on iOS launch** with `GADForwardedUserDefaults` or
`GADApplicationVerifyPublisherInitializedCorrectly` in the symbols. The plugin failed to
write the Google App ID into `Info.plist`. The account manager supplies the identifier, and
it goes into `Config/DefaultEngine.ini`:

```ini
[CASPluginBuildConfig]
GoogleAppIdIOS=ca-app-pub-0000000000000000~0000000000
```

**Nothing initializes, and the log is silent.** Play-in-editor has no SDK at all. Every
verification is a packaged build on a device.

**A Blueprint graph shows no ad, and nothing fails.** It is calling the **engine's**
built-in node. `Show Interstitial Ad` and `Show Ad Banner` exist in both the engine's AdMob
support and in CAS, with the same names. Only the nodes from the **CAS Mobile Ads** category
work here, and the engine's AdMob support has to be off.

**Events fire for some ads and not others, or stop firing.** In Blueprint the events are
registered on the Load and Show nodes, and **each new Load or Show node cancels the previous
node's events**. One place owns the wiring. With autoload on, also check the `Event Once`
checkbox: with it ticked the Loaded/Failed event fires a single time rather than for every
retry.

**Every network is MISSING or NOT VERIFIED** in `ValidateAdsIntegration`. MISSING is normal
for networks that were deliberately left out. NOT VERIFIED means the adapter is selected but
its dependency did not arrive: on iOS a pod integration or an excluded framework, on Android
a Gradle resolution failure earlier in the log.

**It works on Android and shows nothing on iOS, or the other way round.** Every field of
`CAS.AI Config` is per platform. In order: the CAS App ID of that panel is the wrong kind of
identifier (iOS wants the App Store id, not the bundle identifier); the solution is checked
on one platform only; `TestAdsMode` differs between the two panels.

**An interstitial refuses to show** with `ECASError::IntervalNotYetPassed`. The minimum
interval, or the trial ad-free interval, has not passed. Expected behaviour.

**The frame rate drops near ads.** Something is polling `Is*AdReady()` every frame. It calls
into the native SDK; subscribe to `On*AdLoaded` and keep a flag instead.

**The game stays paused, or black, after an ad.** The resume is only wired to
`On*AdDismissed`, and the show failed instead. `On*AdShowFailed` needs the same step, and so
does the path where no ad was loaded.

**A third-party SDK is initialized twice.** `OnAdsInitialized` fires again after an error,
until the error is resolved. The handler has to be idempotent — this plugin is the one where
that event is not once-only.

## Error values

Failures arrive as an `ECASError` on `On*AdLoadFailed`, `On*AdShowFailed` and
`On*AdFailed`, and `UCASMobileAds::GetAdsErrorMessage(Error)` turns one into text — which is
what belongs in the log, because the Unreal pages name only
`ECASError::IntervalNotYetPassed` by hand.

The values and their meanings are the native ones, and the native pages are the reference:
[docs/Android/Ad-errors.mdx](../../../../../docs/Android/Ad-errors.mdx) and
[docs/iOS/Ad-errors.mdx](../../../../../docs/iOS/Ad-errors.mdx). The codes are the same
numbers on every CAS platform, so the "no fill" entry there is the same no fill here — and
it means the integration is correct and fill is an account question, provided test ads show.

## When the answer is not here

`UCASMobileAds::SetVerboseAdsLogs(true)` first, then read the **device** log — `adb logcat`
or the Xcode device console — under the tags `CAS.AI` and, for the integration helper,
`CASIntegrationHelper`. The editor's output log will not have it. The plugin is a thin layer
over the native SDKs, so a failure that makes no sense in C++ usually makes sense there.
