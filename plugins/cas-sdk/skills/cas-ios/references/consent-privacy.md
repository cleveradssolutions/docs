# iOS: consent and privacy

Canonical, in this repository: [docs/iOS/User-Consent-Flow.mdx](../../../../../docs/iOS/User-Consent-Flow.mdx)
Published: `https://docs.page/cleveradssolutions/docs/iOS/User-Consent-Flow`

Two mutually exclusive paths. Pick one in the interview and never mix them.

## Path A, the built-in flow

On by default. The form appears when the SDK initializes, and the initialization handler
fires after the user finishes it. Nothing to write.

It can be disabled through the builder when the publisher has their own platform. All
configuration has to be applied before the manager is created.

### Privacy options entry

Some forms require the user to be able to change their decision later. Add an entry in the
app settings that shows the form again, and keep it reachable. Presenting a form is main
thread work and needs a view controller that is actually on screen.

## Path B, the publisher's own CMP

Disable the CAS flow and set the privacy options **before** the manager is created, so
that the ad networks start with the user's actual decision. Setting them later means the
networks were already initialized under the wrong assumption.

If Google demand is used, the Google CMP requirements apply and have to be reviewed before
the integration, not after.

## App Tracking Transparency is a separate thing

Consent under GDPR and the ATT prompt are two different decisions, and neither replaces
the other. The order between them is a product decision: ask the publisher. The
initialization handler reports `config.isATTrackingAuthorized` so the app can react.

Never add an ATT request on the agent's own initiative, and never word the usage
description for the publisher beyond offering the examples from the documentation.

## Rules that hold on both paths

- Never cache a consent status or a consent string in the app.
- Never build a second consent dialog on top of a flow that is already running.
- Never put personal data into the user identifier or targeting: no email, user name,
  IDFA or IDFV.
- Location is collected only if the app has its own valid reason to use it, never for ads
  alone.
- The App Store privacy answers are the publisher's to fill in.
