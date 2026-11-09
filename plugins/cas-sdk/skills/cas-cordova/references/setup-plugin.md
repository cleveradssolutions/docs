# Cordova: the plugin and its variables

Canonical:
[docs/Cordova/index.mdx](../../../../../docs/Cordova/index.mdx). Read it before writing
anything — the minimum platform versions, the exact variable names and the solution values
live there, and they change.

Everything that decides whether ads serve is a **plugin variable in `config.xml`**. There
is no CAS ID in the JavaScript, no Gradle block to edit and no `Info.plist` to touch: the
CAS Gradle plugin on Android and the CAS Ruby script on iOS do that work from the variables
you set here.

## 1. The prerequisites, in config.xml

```xml
<platform name="ios">
    <preference name="deployment-target" value="<see the page>" />
</platform>
<platform name="android">
    <preference name="android-targetSdkVersion" value="<see the page>" />
</platform>
```

Below those minimums the native SDKs will not build. Set them before adding the plugin.

## 2. The plugin, with its variables

```xml
<plugin name="cordova-plugin-casai" spec="*">
    <variable name="IOS_CAS_ID" value="123456789" />
    <variable name="ANDROID_CAS_SOLUTIONS" value="optimal" />
    <variable name="IOS_CAS_SOLUTIONS" value="optimal" />
    <variable name="ANDROID_USE_AD_ID" value="true" />
    <variable name="IOS_USER_TRACKING_USAGE" value="..." />
</plugin>
```

or on the command line:

```shell
cordova plugin add cordova-plugin-casai --variable IOS_CAS_ID=demo
```

> **Changing a variable does nothing until the plugin is re-added.** This is the single
> most important operational fact on this platform:
>
> ```shell
> cordova plugin rm cordova-plugin-casai --nosave && cordova plugin add cordova-plugin-casai
> ```
>
> A rebuild alone keeps the old values. Every time a variable changes — the CAS ID, the
> solution, the adapter list — this pair of commands runs, and it belongs in the hand-over
> and in any CI script.

## 3. The two CAS IDs are supplied differently

| Platform | Where the CAS ID comes from |
|---|---|
| Android | **Automatic.** The plugin reads the bundle package name, the `id` attribute of `<widget>`. Nothing to set. |
| iOS | `IOS_CAS_ID`, the **App Store id** — the number from `apps.apple.com/.../id123456789`, not the bundle identifier. |

**Without `IOS_CAS_ID` the iOS app silently serves test ads**, for ever, with no error. It
is the most expensive missing value on this platform precisely because nothing complains.
Confirm it is the App Store id and not the bundle identifier before shipping; the two are
easy to confuse and look equally plausible in the file.

## 4. Ad networks, once per platform

The solution is a variable per platform, and both have to be set:

```xml
<variable name="ANDROID_CAS_SOLUTIONS" value="optimal" />
<variable name="IOS_CAS_SOLUTIONS" value="optimal" />
```

`optimal`, `families` or `vpnCompliant`. Shipping one platform with a solution and the
other without it is a common and silent mistake — the app earns on Android and shows
nothing on iOS.

For a hand-picked set, `ANDROID_CAS_ADAPTERS` and `IOS_CAS_ADAPTERS` take several names
separated by a comma, a semicolon or a space:

```xml
<variable name="IOS_CAS_ADAPTERS" value="GoogleAds CASExchange DTExchange" />
<variable name="ANDROID_CAS_ADAPTERS" value="googleAds casExchange dtExchange" />
```

> **The adapter names are spelled differently on the two platforms.** iOS uses the Adapter
> name exactly as the iOS adapters table's first column writes it — `GoogleAds` — and
> Android uses the Android table's first column — `googleAds`. Copying one list into the
> other variable is wrong even though it looks right. Take each list from its own table:
> [docs/Android/Mediated-Networks.mdx](../../../../../docs/Android/Mediated-Networks.mdx)
> and [docs/iOS/Mediated-Networks.mdx](../../../../../docs/iOS/Mediated-Networks.mdx),
> plus `Adapters/README.md` in each native repository for the current list.

The two lists are also expected to differ: not every network ships an adapter for both
platforms, and the store policies for child-directed apps do not approve the same networks.

During development no ad network is needed at all: test ads work without them, which is
what makes it tempting to stop here. Say plainly in the hand-over that the solution has to
be on, on both platforms, before release.

Some networks need extra steps — Meta Audience Network has its own page,
[Additional-Meta-AudienceNetwork-steps.mdx](../../../../../docs/Cordova/Additional-Meta-AudienceNetwork-steps.mdx).

## 5. The advertising identifier, also per platform

| Variable | What it does |
|---|---|
| `ANDROID_USE_AD_ID` | `true` or `false`. Must be **false** when the app targets only children, or when it is built for devices without Google Play Services. |
| `IOS_USER_TRACKING_USAGE` | The App Tracking Transparency prompt text. Without it the IDFA is zeroed out and revenue drops, with nothing in the log to say why. |

The publisher writes the tracking text. The agent offers the examples from the setup page
and does not word it for them.

## 6. Capacitor

Capacitor can run Cordova plugins, but it does not consume `config.xml` plugin variables
the way Cordova does. Before planning, confirm how this project supplies them — and if it
cannot, say so plainly rather than writing variables into a file nothing reads.

## 7. What to check before moving on

- `cordova plugin ls` shows `cordova-plugin-casai`, and `config.xml` holds every variable
  the plan named.
- `IOS_CAS_ID` is an App Store id, digits only — or deliberately `demo`, and that is stated.
- Both solution variables are set, not just one.
- A debug build succeeds for each platform the publisher ships, and the app **runs**: the
  Gradle plugin and the Ruby script do their work at build time, and a build that never ran
  proves nothing.
- Any later variable change was followed by `plugin rm` and `plugin add`.
