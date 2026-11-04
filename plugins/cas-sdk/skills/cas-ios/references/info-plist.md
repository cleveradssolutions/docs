# iOS: Info.plist

Canonical, in this repository: [docs/ios/index.mdx](../../../../../docs/ios/index.mdx), section
"Configure Info.plist", and [docs/ios/manually-configure-project.mdx](../../../../../docs/ios/manually-configure-project.mdx)
for the manual path. Apple attribution: [docs/general/apple-ad-attribution.mdx](../../../../../docs/general/apple-ad-attribution.mdx).
Script: `Script XCodeConfig/` in `https://github.com/cleveradssolutions/CAS-iOS`.

## When and how to run it

**After the dependencies are installed, and from the project directory.** The script
takes the CAS ID: `ruby casconfig.rb <CAS ID>`, or `demo` while the app is not registered.

It edits the **`.xcodeproj`**, not the workspace, which is correct even in a CocoaPods
project, where you then keep working in the `.xcworkspace`. It also adds `-ObjC` to
`OTHER_LDFLAGS` at the target level.

Pass `--no-gad` when the integration has no Google demand. Without it the script writes a
`GADApplicationIdentifier` that the app does not use, which later reads as a
misconfiguration rather than as an unused key.

A run reports exactly what it changed, for example 265 `SKAdNetworkItems`, 6
`AdNetworkIdentifiers`, App Transport Security and the tracking usage description. Those
numbers show up again on the test ad screen, which is how you confirm it worked.

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
the publisher's to answer; point at `/general/appstore-data-disclosure` and
`/general/apple-ad-attribution`, and do not fill it in for them.

For a child-directed app, the Apple Kids Category has its own rules and the Families
solution is **not** intended for apps in that category. If the publisher says the app is
in the Kids Category, stop and send them to the documentation page before choosing a
solution.
