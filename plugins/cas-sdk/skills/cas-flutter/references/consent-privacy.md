# Flutter: consent and privacy

Canonical, in this repository:
[docs/flutter/user-consent-flow.mdx](../../../../../docs/flutter/user-consent-flow.mdx)
Published: `https://docs.page/cleveradssolutions/docs/flutter/user-consent-flow`

Two mutually exclusive paths. Pick one in the interview and never mix them.

## Path A, the built-in flow

The form is an argument of initialization: `showConsentFormIfRequired: true`. It appears
while the SDK initializes, and the `initialize` `Future` completes after the user has
finished with it. Nothing else to write.

The status arrives on the result as `status.consentFlowStatus`, against the
`ConsentFlow.status*` constants — `ConsentFlow.statusObtained` is the one that means the
user answered.

### Showing the form yourself

`ConsentFlow.create().showIfRequired()` presents it only when it is required;
`ConsentFlow.create().show()` presents it unconditionally, which is what a settings entry
needs. Both resolve to the same status `int`.

### Privacy options entry

Some forms require the user to be able to change their decision later. After the flow
completes, `isConsentObtained` says whether to show that entry in the app's settings; wire
it to `ConsentFlow.create().show()`. Keep it reachable — this is a compliance requirement,
not a nicety.

## Path B, the publisher's own CMP

Leave `showConsentFormIfRequired` off and apply the publisher's decision **before**
`initialize`, so the ad networks start with the user's actual choice. Applying it later
means the networks were already initialized under the wrong assumption, and the first
session is wrong in a way nothing reports.

If Google demand is used, the Google CMP requirements apply and have to be reviewed before
the integration, not after.

## Debug geography is a development setting

`ConsentFlow.create(debugGeography: ...)` forces the flow to behave as if the device were in
a given region. It **stays active in production sessions** once set, so it belongs behind the
same build condition as test ads, and never in a release path. Leave it unset, or at the
unknown value, to let the system decide.

## Audience is declared at initialization

`audience: Audience.notChildren` and its siblings are part of the `initialize` call, and
they have to match what the publisher declared in the CAS dashboard and in both stores. An
app that targets children and adults needs a **neutral age screen** — an age gate that does
not nudge the user to misrepresent their age — before ads unsuitable for children can be
withheld correctly. That is a product decision: the agent asks, links the page, and does not
decide.

## The store side has no plugin copy

Both stores ask the publisher questions that an integration changes the answers to, and
those pages are the native ones:
[Google-Play-data-disclosure.mdx](../../../../../docs/general/google-play-data-disclosure.mdx)
and
[AppStore-data-disclosure.mdx](../../../../../docs/general/appstore-data-disclosure.mdx),
plus [Apple-Ad-Attribution.mdx](../../../../../docs/general/apple-ad-attribution.mdx) and
[Apple-Kids-Category.mdx](../../../../../docs/general/apple-kids-category.mdx). Name them in
the hand-over; the publisher answers them.

## Rules that hold on both paths

- Third-party SDKs — attribution, analytics — are initialized **from inside** the
  initialization callback, after consent. Earlier means they cannot read the relevant
  identifiers, and the publisher loses measurement and revenue.
- Never add an App Tracking Transparency request on the agent's own initiative, and never
  word the usage description for the publisher beyond offering the examples from the
  documentation. Consent under GDPR and the ATT prompt are two different decisions, and
  neither replaces the other.
- The agent does not decide whether the audience is child-directed. It asks, and it stops
  when the answer is the Apple Kids Category.
