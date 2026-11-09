# Kotlin Multiplatform: consent and privacy

Canonical, in this repository:
[docs/kmp/user-consent-flow.mdx](../../../../../docs/kmp/user-consent-flow.mdx)
Published: `https://docs.page/cleveradssolutions/docs/kmp/user-consent-flow`

Two mutually exclusive paths. Pick one in the interview and never mix them.

## Path A, the built-in flow

The form is an argument of initialization: `showConsentFormIfRequired = true`. It appears
while the SDK initializes, and the initialization listener is invoked after the user has
finished with it. Nothing else to write.

The status arrives on the result as `config.consentFlowStatus`, against
`CASConsentFlowStatus`:

| Status | Meaning |
|---|---|
| `UNKNOWN` | Not known yet. |
| `OBTAINED` | The user answered. Personalized versus non-personalized is not distinguished. |
| `NOT_REQUIRED` | Consent is not required for this user. |
| `UNAVAILABLE` | Consent unavailable. |
| `INTERNAL_ERROR`, `NETWORK_ERROR` | The flow failed; the SDK carries on. |
| `CONTEXT_INVALID` | No valid context to present the form — on Android, no Activity. |
| `FLOW_STILL_SHOWING` | Another form is already on screen. |

### Showing the form yourself

`CASMobileAds.showConsentForm(ifRequiredOnly = true, dismissListener = ...)` loads a form
and presents it only when consent is required. The dismiss listener is invoked after the
form is dismissed, **or immediately when consent is not required** — so it is safe to call
without checking first, but a UI that shows a spinner until it returns will flash.

```kotlin
CASMobileAds.showConsentForm(
    ifRequiredOnly = true,
    dismissListener = CASConsentFormDismissListener { status ->
        if (status == CASConsentFlowStatus.OBTAINED) {
            // consent obtained
        }
    },
)
```

**`showConsentForm()` must be called from the main thread.** From a coroutine, that means
the main dispatcher, not whatever context the caller happened to be on.

### Privacy options entry

Some forms require the user to be able to change their decision later. After initialization,
`config.consentFlowStatus == CASConsentFlowStatus.OBTAINED` says whether to show that entry
in the app's settings; wire it to `showConsentForm(ifRequiredOnly = false)`, which presents
the form regardless. Keep it reachable — this is a compliance requirement, not a nicety. The
consent page has the whole settings-screen composable.

## Path B, the publisher's own CMP

Leave `showConsentFormIfRequired` off and let the publisher's flow **complete before**
`CASMobileAds.initialize`, so the ad networks start with the user's actual choice. Applying
it later means the networks were already initialized under the wrong assumption, and the
first session is wrong in a way nothing reports.

A CMP that is IAB TCF v2 compliant is read by the SDK on its own: CAS sends the TCF v2
consent to the networks, and the built-in flow is then not shown. That is the cleanest
version of path B, and it needs no bridging code in `commonMain`.

If Google demand is used, the Google CMP requirements apply and have to be reviewed before
the integration, not after.

## Debug geography behaves differently in the two places

`CASConsentDebugGeography` is `DISABLED`, `EEA`, `REGULATED_US_STATE` or `OTHER`.

- Passed to **`initialize`**, it applies **only to test sessions**. Safe to leave in.
- Passed to **`showConsentForm`**, it **stays active even in production sessions**. So in
  the manual flow it belongs behind the same debug condition as test ads:
  `if (isDebugBuild) CASConsentDebugGeography.EEA else CASConsentDebugGeography.DISABLED`.

This asymmetry is documented and is the opposite of what the other CAS plugins do, where
the setting is dangerous everywhere. Read the page before copying a snippet from another
platform's guide.

## Audience is declared at initialization

`targetAudience = CASTaggedAudience.NOT_CHILDREN`, with `UNDEFINED` and `CHILDREN` as the
other values. It has to match what the publisher declared in the CAS dashboard and in both
stores. An app that targets children and adults needs a **neutral age screen**, an age gate
that does not nudge the user to misrepresent their age. That is a product decision: the agent
asks, links the page, and does not decide.

Users subject to COPPA do not see the consent dialog at all, and neither do users outside
the regulated regions. An absent form is not a broken flow.

## App Tracking Transparency, on the iOS side only

The prompt needs `NSUserTrackingUsageDescription` in `iosApp/Info.plist`;
`casconfig.rb` writes it, and the KMP index page lists example texts and their
localizations. Without the key the IDFA is zeroed out and revenue drops, with nothing in the
log to say why.

Never add the request on the agent's own initiative, and never word the usage description
for the publisher beyond offering the documented examples. Consent under GDPR and the ATT
prompt are two different decisions, and neither replaces the other.

On Android the equivalent switch is `useAdvertisingId` in the `cas { }` block, which must be
off for a children-only app or a build for devices without Google Play Services.

## The store side has no Kotlin Multiplatform copy

Both stores ask the publisher questions that an integration changes the answers to, and those
pages are the general ones:
[Google-Play-data-disclosure.mdx](../../../../../docs/general/google-play-data-disclosure.mdx)
and
[AppStore-data-disclosure.mdx](../../../../../docs/general/appstore-data-disclosure.mdx), plus
[Apple-Ad-Attribution.mdx](../../../../../docs/general/apple-ad-attribution.mdx) and
[Apple-Kids-Category.mdx](../../../../../docs/general/apple-kids-category.mdx). Name them in
the hand-over; the publisher answers them.

## Rules that hold on both paths

- Third-party SDKs — attribution, analytics — are initialized **from inside** the
  initialization listener, after consent. Earlier means they cannot read the relevant
  identifiers, and the publisher loses measurement and revenue.
- Never cache the consent status or a saved consent string in the app. An expired consent
  then produces a TCF 3.3 error, and the SDK is the one that knows when to ask again.
- Meta Audience Network's CCPA "Limited Data Use" flag is the publisher's own obligation,
  not something CAS handles. The page links the extra steps.
- The agent does not decide whether the audience is child-directed. It asks, and it stops
  when the answer is the Apple Kids Category.
