# Android: reading failures

Canonical: `https://docs.page/cleveradssolutions/docs/Android/Ad-errors`

Error codes have defined meanings. Look the code up before offering a cause.

| Code | Constant | What it usually means here |
|---|---|---|
| 0 | `INTERNAL_ERROR` | SDK side. Collect the full log before reporting it. |
| 1 | `NOT_READY` | Show was called before an ad was loaded. Load first, or use autoload and wait. |
| 2 | `REJECTED` | The device does not qualify, by region or OS version. Not an integration bug. |
| 3 | `NO_FILL` | No ad available. **If test ads show, the integration is correct** and fill is an account and inventory question. |
| 6 | `REACHED_CAP` | Daily cap for this creative, mostly cross promotion. |
| 7 | `NOT_INITIALIZED` | Ads requested before initialization. Move the request after the callback. |
| 8 | `TIMEOUT` | The ad source did not answer in time. The SDK keeps waiting. |
| 9 | `NO_CONNECTION` | No network on the device. |
| 10 | `CONFIGURATION_ERROR` | The CAS ID is unknown, unregistered, or an ad source is misconfigured. Check the CAS ID first, then ask the account manager. |
| 11 | `NOT_PASSED_INTERVAL` | The interstitial interval, or the trial ad-free interval, has not passed. Expected, not a defect. |
| 12 | `ALREADY_DISPLAYED` | Another full screen ad is on screen. Fix the show logic. |
| 13 | `NOT_FOREGROUND` | The app is not in the foreground. |

## Symptoms that are not error codes

**Nothing loads and the log has no CAS lines.** The SDK was never initialized, or the
initialization is in a process that does not run. `Application.onCreate()` runs for every
process; guard it with a main process check if the app has several.

**An adapter artifact does not resolve.** A repository is missing from
`dependencyResolutionManagement`. Each network has its own, see the setup page.

**Ads load but never show.** The show call is not on the main thread, or the activity is
finishing, or another full screen ad is up.

**"Loading a new ad directly from the onAdFailedToLoad() callback is not supported."**
Exactly what it says. Remove the retry from the failure callback.

**"The ad content has been loaded, but the ad instance has been lost."** An ad instance
was garbage collected while still loading, because nobody called `destroy()` and nobody
held it. Hold it for the lifetime of its screen and destroy it there.

**Assertion failures in a debuggable build.** The SDK's internal assertions are enabled by
D8 in debuggable builds. They point at a real integration mistake; read the message rather
than suppressing them.

**The consent form does not appear.** The user is outside a region that requires it, or
consent was already obtained, or the flow was disabled. `config.isConsentRequired` and
`config.consentFlowStatus` from the initialization callback tell which one.

## When to escalate instead of guessing

`CONFIGURATION_ERROR` that survives a correct CAS ID, an adapter that never appears in the
test ad's Mediation Adapters list, or a closed beta network: these are account questions.
Collect the full log with debug mode on and send the publisher to their account manager.
