# Cordova: consent and privacy

Canonical, in this repository:
[docs/cordova/user-consent-flow.mdx](../../../../../docs/cordova/user-consent-flow.mdx)
Published: `https://docs.page/cleveradssolutions/docs/cordova/user-consent-flow`

Two mutually exclusive paths. Pick one in the interview and never mix them.

## Path A, the built-in flow

`showConsentFormIfRequired: true` in the `initialize` options. The form appears while the
SDK initializes, and the Promise resolves after the user has finished with it. Nothing else
to write.

The status arrives as `status.consentFlowStatus`, and the values are **strings**:
`'Obtained'`, `'Not required'`, `'Unavailable'`, `'Internal error'`, `'Network error'`,
`'Invalid context'`, `'Still presenting'`. Note the spaces — these are not identifiers, and
comparing against `'NotRequired'` silently never matches.

### Showing the form yourself

```js
casai.showConsentFlow({ ifRequired: true }).then((status) => {
  if (status === 'Obtained') {
    // consent obtained
  }
});
```

`showConsentFlow` returns a `Promise<ConsentFlowStatus>` that resolves when the form is
dismissed, **or immediately when consent is not required**. So it is safe to call without
checking first, but a UI that blocks until it resolves will flash. Note the option is
`ifRequired`, not `ifRequiredOnly` as on some other CAS platforms.

### Privacy options entry

Some forms require the user to be able to change their decision later. After `initialize`
resolves, `status.consentFlowStatus === 'Obtained'` says whether to show that entry in the
app's settings; wire it to `casai.showConsentFlow({ ifRequired: false })`, which presents
the form regardless. `status.isConsentRequired` from initialization says the same thing
from the other side. Keep it reachable — this is a compliance requirement, not a nicety.

## Path B, the publisher's own CMP

Leave `showConsentFormIfRequired` off and let the publisher's flow complete **before**
`casai.initialize`, so the ad networks start with the user's actual choice. Applying it
later means the networks were already initialized under the wrong assumption, and the first
session is wrong in a way nothing reports.

If Google demand is used, the Google CMP requirements apply and have to be reviewed before
the integration, not after.

## Debug geography is a development setting

`debugGeography: 'eea'` makes the flow behave as if the device were in the European
Economic Area; `'us'` is a regulated US state, `'unregulated'` a region with none, and
`'unknown'` lets the SDK decide. Passed to `initialize` it applies to test sessions, so it
belongs behind the same condition as `forceTestAds` and never in a release path.

## Audience is declared at initialization

`targetAudience: 'notchildren'`, `'children'`, or the option left out for an app that
targets both. Lowercase strings, and `notchildren` has no separator. It has to match what
the publisher declared in the CAS dashboard and in both stores.

An app that targets children and adults needs a **neutral age screen**, an age gate that
does not nudge the user to misrepresent their age. That is a product decision: the agent
asks, links the page, and does not decide.

Users subject to COPPA do not see the consent dialog at all, and neither do users outside
the regulated regions. An absent form is not a broken flow.

## App Tracking Transparency, from a config.xml variable

The prompt text is `IOS_USER_TRACKING_USAGE`, a plugin variable — see
[setup-plugin.md](setup-plugin.md). Without it the IDFA is zeroed out and revenue drops,
with nothing in the log to say why. Remember that changing the variable needs the plugin
removed and re-added.

Never add the request on the agent's own initiative, and never word the usage description
for the publisher beyond offering the documented examples. Consent under GDPR and the ATT
prompt are two different decisions, and neither replaces the other.

On Android the equivalent switch is `ANDROID_USE_AD_ID`, which must be `false` for a
children-only app and for a build for devices without Google Play Services.

## The store side has no Cordova copy

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
  initialization `then`, after consent. Earlier means they cannot read the relevant
  identifiers, and the publisher loses measurement and revenue.
- Never cache the consent status or a saved consent string in the app. An expired consent
  then produces a TCF 3.3 error, and the SDK is the one that knows when to ask again.
- Meta Audience Network's CCPA "Limited Data Use" flag is the publisher's own obligation,
  not something CAS handles. The page links the extra steps.
- The agent does not decide whether the audience is child-directed. It asks, and it stops
  when the answer is the Apple Kids Category.
