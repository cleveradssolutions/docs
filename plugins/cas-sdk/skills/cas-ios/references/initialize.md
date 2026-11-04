# iOS: initialization

Canonical, in this repository: [docs/iOS/Initialize-CAS.mdx](../../../../../docs/iOS/Initialize-CAS.mdx)
Published: `https://cleveradssolutions.docs.page/docs/iOS/Initialize-CAS`
Samples: `DemoApp Swift/CASSample/AppDelegate.swift`, `DemoApp Objective-C/` and
`DemoApp SwiftUI/` in `https://github.com/cleveradssolutions/CAS-iOS`

## Where it goes

Once, at launch, from `application(_:didFinishLaunchingWithOptions:)`. The CAS ID is a
constant on the app delegate, reused by every ad instance.

```swift
import CleverAdsSolutions

@main
class AppDelegate: UIResponder, UIApplicationDelegate {
    static let casID: String = "123456789"   // App Store id, from the CAS dashboard

    var window: UIWindow?   // some mediated frameworks expect it

    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        initializeCAS()
        return true
    }
}
```

`"demo"` is the id to use when the app is not registered yet and the point is to see the
plumbing work. It is not a fallback to leave in the project.

## The call

```swift
func initializeCAS() {
    let builder = CAS.buildManager()

    #if DEBUG
    builder.withTestAdMode(true)
    #endif

    builder.withCompletionHandler { config in
        let error: String? = config.error
        let userCountryISO2: String? = config.countryCode
        let protectionApplied: Bool = config.isConsentRequired
        let consentStatus = config.consentFlowStatus
        let trackingAuthorized: Bool = config.isATTrackingAuthorized

        // Third party SDKs that need consent go here, not earlier.
    }

    builder.create(withCasId: AppDelegate.casID)
}
```

Test ad mode in a development build is not optional. It is the one line that keeps the
publisher's account out of invalid traffic.

The handler can arrive with an error. The SDK keeps retrying and calls it again, so treat
the error as information, not as a terminal state.

## What must not happen around it

- No ad network initializer anywhere in the app. CAS starts them.
- No ad request before the handler. Create ad instances freely, request after.
- No MMP or analytics initialization before the handler. Started earlier, they cannot read
  the identifiers the consent and ATT decide on, and attribution and revenue reporting
  suffer.

## Scene based apps

With a `SceneDelegate`, initialization still belongs in the app delegate; the scene
delegate only owns the window. In SwiftUI, use an app delegate adaptor rather than
initializing from a view's lifecycle, which can run more than once.

## Audience

A child-directed app is tagged before initialization, the same way as on Android, and the
Apple Kids Category has extra rules of its own. Implement what the publisher states, never
guess it.

## Debug logging

Turn the SDK's debug mode on while verifying and off for release. It is noisy by design,
and the full console output is what support asks for when something is wrong.
