# Android: consent and privacy

Canonical, in this repository: [docs/Android/User-Consent-Flow.mdx](../../../../../docs/Android/User-Consent-Flow.mdx)
Published: `https://docs.page/cleveradssolutions/docs/Android/User-Consent-Flow`

Two mutually exclusive paths. Pick one in the interview and never mix them.

## Path A, the built-in flow

On by default. The form appears when the SDK initializes, and the initialization callback
fires after the user finishes it. Nothing to write.

To turn it off:

```kotlin
builder.withConsentFlow(ConsentFlow(/* isEnabled = */ false))
```

All configuration has to be applied before `builder.build()`.

### Privacy options button

Some forms require the user to be able to change their decision later. Add an entry in the
app settings that shows the form again, and keep it reachable; a consent form the user
cannot reopen is a compliance problem, not a UX detail.

`show()` and `showIfRequired()` are main thread calls.

### Status values

`OBTAINED`, `NOT_REQUIRED`, `UNAVAILABLE`, `INTERNAL_ERROR`, `NETWORK_ERROR`,
`CONTEXT_INVALID`, `FLOW_STILL_SHOWING`. Treat everything except the first two as "no
personalized signal available", not as a failure to show ads.

## Path B, the publisher's own CMP

If the app already has a consent platform, disable the CAS flow and set the privacy
options **before** initialization, so that the ad networks start with the user's actual
decision. Setting them later means the networks were already initialized under the wrong
assumption.

If Google demand is used, the Google CMP requirements apply and have to be reviewed before
the integration, not after.

## Rules that hold on both paths

- Never cache a consent status or a consent string in the app. Expired consent read from
  cache produces consent framework errors.
- Never build a second consent dialog on top of a flow that is already running.
- Never put personal data into the user identifier or targeting: no email, user name,
  advertising id or any other direct identifier.
- Location is collected only if the app has its own valid reason to use it, never for ads
  alone.
- The store data disclosure is the publisher's to fill in. Point at
  `/general/Google-Play-data-disclosure` and let them answer.

## Testing the flow

A debug geography option exists for testing the form in a region the device is not in. It
only applies when the device is a registered test device or forced testing is on, so it
cannot leak into production behaviour.
