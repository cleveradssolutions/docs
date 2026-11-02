# iOS: dependencies and ad networks

Canonical, in this repository: [docs/iOS/index.mdx](../../../../../docs/iOS/index.mdx)
Networks and adapter products: [docs/iOS/Mediated-Networks.mdx](../../../../../docs/iOS/Mediated-Networks.mdx)
Published: `https://docs.page/cleveradssolutions/docs/iOS`

Confirm the current SDK version and the minimum deployment target on the documentation
page before writing versions into the project. Use the dependency manager the project
already uses; do not convert a working CocoaPods project to Swift Package Manager, or the
other way round, unless the publisher asks.

## Swift Package Manager

1. **File > Add Package Dependencies**
2. Package URL: `https://github.com/cleveradssolutions/CAS-iOS`
3. Dependency rule: **Up to Next Minor Version**, starting from the current SDK version.
4. Xcode then asks which products to add to the app target. That choice is the ad network
   choice below.

A note worth telling the publisher before they click: resolving the package downloads the
binary targets, which is a large download the first time.

### Products

| Product | When |
|---|---|
| `AdsOptimalSolutions` | general or mixed audience |
| `AdsFamiliesSolutions` | child-directed app, not in the Apple Kids Category |
| `AdsVPNCompliantSolutions` | VPN or proxy apps |
| `CASMediation<Network>` | a hand-picked set, one product per network |
| `CleverAdsSolutionsSPM` | the SDK alone, no networks, for trying test ads |

Each adapter product already brings the base framework, so it is not added separately.
The solution products exist from SDK `4.8.3`; on anything older the adapters go in one by
one.

## CocoaPods

At the top of the `Podfile`:

```ruby
source 'https://github.com/CocoaPods/Specs.git'
source 'https://github.com/cleveradssolutions/CAS-Specs.git'

use_frameworks!
```

Then one solution, or a list of networks:

```ruby
pod 'CleverAdsSolutions-SDK/Optimal', '<current version>'
```

`Families` and `VPNCompliant` are the other two subspecs; the per-network subspecs are on
the Mediated Networks page. Run `pod install`, then work in the `.xcworkspace`, never in
the `.xcodeproj`.

## Which networks, in one sentence

The network list behind each solution is on the documentation page and changes between
releases, so quote it from there when the publisher asks what is included. During
development with test ads no network is required at all, which is the right way to check
the plumbing before the account is fully set up.

## Build settings

Check the deployment target against the documentation minimum, and raise it only with the
publisher's agreement. If the project needs `-ObjC` or other linker flags, the
documentation setup page states it; do not add linker flags on a hunch.

## Check

The app target builds and links, and the CAS framework appears in the target's frameworks.
A missing network product shows up here as a link error, not later at runtime.
