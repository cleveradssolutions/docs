# Where the canonical answer lives

This repository never becomes the source of truth. Every reference file here carries the
documentation link it was written from. When the two disagree, the documentation wins and
this repository is wrong and has to be fixed.

## Documentation

Base: `https://docs.page/cleveradssolutions/docs`

| Topic | Android | iOS |
|---|---|---|
| Project setup | `/Android` | `/iOS` |
| Manual setup | `/Android/Manual-setup` | `/iOS/Manually-configure-project` |
| Initialization | `/Android/Initialize-CAS` | `/iOS/Initialize-CAS` |
| Test ads | `/Android/Enabling-test-ads` | `/iOS/Enabling-test-ads` |
| Consent flow | `/Android/User-Consent-Flow` | `/iOS/User-Consent-Flow` |
| Banner | `/Android/Banner-Ads` | `/iOS/Banner-Ads` |
| Interstitial | `/Android/Interstitial-Ads` | `/iOS/Interstitial-Ads` |
| Rewarded | `/Android/Rewarded-Ads` | `/iOS/Rewarded-Ads` |
| App open | `/Android/App-Open-Ads` | `/iOS/App-Open-Ads` |
| Native, load | `/Android/Load-Native-Ads` | `/iOS/Load-Native-Ads` |
| Native, display | `/Android/Display-Native-Ad` | `/iOS/Display-Native-Ad` |
| Ad networks | `/Android/Mediated-Networks` | `/iOS/Mediated-Networks` |
| Targeting | `/Android/Targeting-options` | `/iOS/Targeting-options` |
| Impression level data | `/Android/Impression-Level-Data` | `/iOS/Impression-Level-Data` |
| Error codes | `/Android/Ad-errors` | `/iOS/Ad-errors` |
| Purchases | `/Android/Purchase-Info` | `/iOS/Purchase-Info` |
| Families policy | `/Android/Google-Plays-Families-Policy` | `/general/Apple-Kids-Category` |
| Store disclosures | `/general/Google-Play-data-disclosure` | `/general/AppStore-data-disclosure` |
| Apple ad attribution | — | `/general/Apple-Ad-Attribution` |

Whole docs as markdown, for an agent that wants to search rather than browse:
`https://docs.page/cleveradssolutions/docs/llms.txt` lists every page,
`https://docs.page/cleveradssolutions/docs/llms-full.txt` is the full text in one file
and is large, roughly a quarter of a million tokens, so fetch single pages instead unless
there is a reason not to.

## Sample apps

Android, `https://github.com/cleveradssolutions/CAS-Android`:
`kotlinSample/`, `javaSample/`, `composeSample/`, and `Adapters/README.md` for adapter
versions.

iOS, `https://github.com/cleveradssolutions/CAS-iOS`:
`DemoApp Swift/`, `DemoApp Objective-C/`, `DemoApp SwiftUI/`, `Adapters/README.md`, and
`Script XCodeConfig/` for the `Info.plist` configuration script.

## Versions

The SDK version and the minimum toolchain move. Read the current values from the
documentation page for the platform rather than from any file in this repository. At the
time of writing: SDK `4.8.3`; Android `minSdk 24`, `compileSdk 36`, Kotlin `2.1.21`; iOS
deployment target `15.0`, Xcode `26.2`.
