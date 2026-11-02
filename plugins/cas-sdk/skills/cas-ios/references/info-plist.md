# iOS: Info.plist

Canonical: `https://docs.page/cleveradssolutions/docs/iOS` (Configure Info.plist) and
`.../iOS/Manually-configure-project` for the manual path.
Script: `Script XCodeConfig/` in `https://github.com/cleveradssolutions/CAS-iOS`.

## Use the script

The configuration script writes everything below from the current remote configuration.
Running it is the supported path; editing the keys by hand is the fallback and is marked
as not recommended in the documentation.

What it writes:

- the **SKAdNetwork and AdAttributionKit** identifiers of every mediated network,
- `GADApplicationIdentifier`, when Google demand is used,
- `NSUserTrackingUsageDescription`,
- the App Transport Security exceptions the ad networks need.

**Re-run it before every SDK update.** The identifier lists change with the adapter set,
and a stale list quietly costs attribution.

## Rules

- **Never prune the SKAdNetwork list by hand** and never trim it to "the networks we
  actually use". The list covers the whole mediation stack.
- **The AdMob App ID is the one CAS generated**, taken from the CAS dashboard. The
  publisher's own AdMob App ID is not valid here, and the documentation says so in as many
  words.
- Do not remove ATS exceptions to satisfy a security checklist without asking; ad creatives
  come from many origins.

## App Tracking Transparency

`NSUserTrackingUsageDescription` is required if the app asks for tracking authorization.
The text is the publisher's, it appears in the system prompt, and it can be localized.

The moment of the request is a product decision, not a technical one: ask the publisher
when the prompt should appear, and respect it. The initialization handler reports
`config.isATTrackingAuthorized` so the app can react to the outcome.

Do not call the ATT request from the agent's own code path "to make it work". If the
publisher has no ATT flow, say what is lost and let them decide.

## App Store privacy

The data the SDK and its networks collect has to be declared in App Store Connect. That is
the publisher's to answer; point at `/general/AppStore-data-disclosure` and
`/general/Apple-Ad-Attribution`, and do not fill it in for them.

For a child-directed app, the Apple Kids Category has its own rules and the Families
solution is **not** intended for apps in that category. If the publisher says the app is
in the Kids Category, stop and send them to the documentation page before choosing a
solution.
