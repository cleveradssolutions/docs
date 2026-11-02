# Rules for an agent integrating CAS

Everything below is a hard rule. When a rule and a request conflict, say so and stop,
do not work around it.

## Money and accounts

1. **Test ads are not a question, they are the rule, and it cuts both ways.**
   - Never test on live ads. Any build that is run during the integration serves test
     ads, because a click on a live ad is invalid traffic and can get the publisher's
     account flagged.
   - Never release with test ads on. They earn nothing.

   So test mode is bound to the build, not to a decision someone has to remember: the
   debug build flag on Android, the debug configuration on iOS. Do not ask the publisher
   who will turn it off; wire it so that nobody has to.
2. **Never tell the publisher to click a live ad** to check the integration. Verification
   happens on test ads.
3. **Never invent an identifier.** CAS ID, AdMob App ID, ad unit ids and test device ids
   come from the CAS dashboard or from the publisher. An id copied from a sample app or
   from another publisher's project is never acceptable, not even as a placeholder.
4. **Never leave a test device id hardcoded.** A test device id is for one machine during
   one integration, and it is removed when the work is done.
5. **Never promise revenue, fill rate or eCPM.** Those are account and inventory
   questions, not integration questions.

## Mediation ownership

6. **Never initialize a mediated ad SDK directly.** No AdMob, AppLovin, Unity Ads or any
   other network initialization call. CAS initializes them. A parallel initialization
   causes integration failures that are hard to diagnose.
7. **Never add a network SDK or adapter dependency by hand.** Adapters come from the CAS
   solution or the adapter list in the CAS configuration block. Hand added dependencies
   produce version conflicts the SDK cannot resolve.
8. **Never leave a second mediation SDK running.** If the app is migrating, the previous
   mediation and its adapters are removed in the same change, or the work stops and the
   publisher decides.
9. **Never skip choosing a solution or an adapter set.** CAS has no inventory of its own;
   an integration without adapters serves nothing and looks broken.

## Order of operations

10. **Never request or show an ad before initialization completes.** Ad calls belong after
    the initialization callback, not next to it.
11. **Never initialize an MMP or analytics SDK before the consent flow finishes.** They go
    inside the initialization completion callback, otherwise attribution and reporting
    break.
12. **Never call `load()` from inside a failure callback.** The SDK refuses it, and it
    produces a request loop on bad networks. Retry logic is explicit and rate limited.
13. **Never call UI bound APIs off the main thread.** Showing a consent form, binding
    native ad content and showing an ad are main thread work.

## Privacy and data

14. **Never build a custom consent UI on top of the built-in flow.** Either the CAS flow
    runs, or it is disabled and the publisher's CMP sets privacy options before
    initialization. Not both.
15. **Never cache or hardcode a consent status or a consent string.** Expired consent
    taken from cache produces consent framework errors.
16. **Never put personal data into targeting.** No email, user name, advertising id or
    vendor id as the user identifier.
17. **Never enable location collection just for ads.** The app needs its own valid reason
    to use location.
18. **Never enable the advertising identifier for a child directed app**, and never mark
    an app as child directed or not on the publisher's behalf. That is their legal call.
19. **Never fill in store privacy disclosures for the publisher.** Point at the data
    disclosure pages and let them answer.

## Project integrity

20. **Never change the application id or bundle id to match a CAS ID.** The registration
    follows the app, not the other way around.
21. **Never silently upgrade an unrelated dependency, the Gradle plugin, the Kotlin
    version or the deployment target to make the build pass.** Report what blocks the
    build and let the publisher decide.
22. **Never hand edit generated or vendored content**: the pods directory, resolved
    package files, lock files, or anything a CAS script generates. Re-run the generator
    instead.
23. **Never prune the SKAdNetwork or ad attribution lists by hand**, and never drop the
    AdMob application identifier or the tracking usage description from the property list.
    Use the configuration script and re-run it on every SDK update.
24. **Never disable minification or strip ProGuard rules to fix a release build.** The SDK
    and adapters ship their own rules.
25. **Never commit, print or paste credentials, keystores, signing material or account
    data**, and never push a branch, open a pull request or change CI without being asked.
26. **Never leave the project in a non building state.** If the integration cannot be
    finished, revert to something that compiles and explain what is missing.

## Ad experience

27. **Never show an app open ad on a cold start outside a loading screen**, and never show
    one after the user has already reached app content.
28. **Never stack full screen formats** or place an interstitial on every user action.
    Keep the minimum interval and say where the ad is shown.
29. **Never leave an ad instance dangling.** Every banner view, and every loaded but
    unshown full screen or native ad, is destroyed with the screen that owns it on
    Android, and released with it on iOS. An instance nobody holds keeps loading, and the
    ad it loads is lost.
30. **Never attach an ad view to a layout it cannot measure**, and never change the ad size
    after it has been attached.

## Reporting

31. **Never claim the integration is verified without running it.** Verified means the
    project builds, the SDK initializes, a test ad loads and shows, and the log has no
    integration errors. Anything else is reported as not verified, with the exact reason.
32. **Never hide a failing step.** If one format does not load, finish the rest and say
    which one failed and what the error code was.
33. **Never guess at an error.** Ad error codes have documented meanings; look the code up
    before offering a cause.
