# iOS: reading failures

Canonical, in this repository: [docs/iOS/Ad-errors.mdx](../../../../../docs/iOS/Ad-errors.mdx)
Published: `https://docs.page/cleveradssolutions/docs/iOS/Ad-errors`

Error codes have defined meanings and match the Android ones. Look the code up before
offering a cause.

| Code | Constant | What it usually means here |
|---|---|---|
| 0 | `INTERNAL_ERROR` | SDK side. Collect the full console output before reporting it. |
| 1 | `NOT_READY` | Present was called before an ad was loaded. |
| 2 | `REJECTED` | The device does not qualify, by region or OS version. |
| 3 | `NO_FILL` | No ad available. **If test ads show, the integration is correct** and fill is an account and inventory question. |
| 6 | `REACHED_CAP` | Daily cap for this creative, mostly cross promotion. |
| 7 | `NOT_INITIALIZED` | Ads requested before initialization. |
| 8 | `TIMEOUT` | The ad source did not answer in time. The SDK keeps waiting. |
| 9 | `NO_CONNECTION` | No network on the device. |
| 10 | `CONFIGURATION_ERROR` | The CAS ID is unknown, unregistered, or an ad source is misconfigured. Check the CAS ID first, then ask the account manager. |
| 11 | `NOT_PASSED_INTERVAL` | The interstitial interval, or the trial ad-free interval, has not passed. Expected, not a defect. |
| 12 | `ALREADY_DISPLAYED` | Another full screen ad is on screen. |
| 13 | `NOT_FOREGROUND` | The app is not in the foreground. |

## Symptoms that are not error codes

**The package will not resolve, or the download never ends.** The package carries binary
targets and the first resolve is large. Let it finish before concluding anything.

**Link errors after adding products.** A network product was added without its
dependencies, or the project mixes a CocoaPods installation with a package one. Pick one
manager.

**Nothing loads and the console has no CAS lines.** The SDK was never initialized, or
initialization lives in a view lifecycle that did not run. Put it in the app delegate.

**Ads load but never present.** The present call is not on the main thread, the view
controller is not on screen, or another full screen ad is up.

**Attribution missing, SKAdNetwork postbacks absent.** The `Info.plist` identifier list is
stale or was edited by hand. Re-run the configuration script.

**The ATT prompt never appears.** `NSUserTrackingUsageDescription` is missing, the request
is made before the app is active, or the user already answered. This is the publisher's
flow, not something to patch around.

**The consent form does not appear.** The user is outside a region that requires it,
consent was already obtained, or the flow was disabled. The initialization handler's
`isConsentRequired` and `consentFlowStatus` tell which one.

## When to escalate instead of guessing

`CONFIGURATION_ERROR` that survives a correct CAS ID, an adapter that never appears in the
test ad's Mediation Adapters list, or a closed beta network: these are account questions.
Collect the full console output with debug mode on and send the publisher to their account
manager.
